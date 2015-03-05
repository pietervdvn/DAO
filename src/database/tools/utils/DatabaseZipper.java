package database.tools.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import database.DataAccessException;
import database.JDBCDataAccessContext;
import database.internal.Field;
import database.internal.TableName;
import database.internal.dao.SimpleDAO;
import database.internal.jdbc.JDBCPictureDAO;
import database.internal.record.RecordWithSingleId;
import database.tools.filter.SelectFilter;
import database.tools.logging.Logger;
import database.tools.utils.MVCDefaultListeners.CSVListener;
import database.tools.utils.MVCUtils.Listener;
import database.tools.utils.MVCUtils.RelationStatus;

/**
 * Creates and loads .zip-files which contain a backup of the DB.
 * 
 * Zipfile structure:
 * 
 * At the root, all the .csv's are saved, and files which contain images. See
 * the structure example beneath. There are also dirs with all the images
 * 
 * @author pietervdvn
 */

/* Example (not all relations are given) */
// backup.zip
// ├── damagelogpictures
// │   ├── damagelogpictures1_picture
// │   ├── damagelogpictures2_picture
// │   └── damagelogpictures3_picture
// ├── car.csv
// ├── damagelog.csv
// ├── damagelogpictures.csv
// ├── damagerecord.csv
// └── user.csv

public class DatabaseZipper {

    private final JDBCDataAccessContext dac;
    public static boolean load = true;

    public DatabaseZipper(JDBCDataAccessContext dac) {
        this.dac = dac;
    }

    /* * SAVE TO ZIP * */

    /**
     * Saves the zipfile to the given file. Zipfile contains .csv's with the
     * relations and directories with images
     * 
     * @throws DataAccessException
     * @throws IOException
     * 
     */
    @SuppressWarnings("unchecked")
    public void saveZipTo(OutputStream out) throws DataAccessException,
            IOException {
    	Logger.out.println("Creating zip");
        ZipOutputStream zos = new ZipOutputStream(out);
        for (SimpleDAO<?> dao : dac.getAllDAOs()) {
            genCSV(zos, dao, new CSVListener(dao.getTableName()));
        }
        zos.close();
    }

    @SuppressWarnings("unchecked")
    private void genCSV(ZipOutputStream zos, SimpleDAO<?> dao,
            Listener<RelationStatus>... listeners) throws IOException,
            DataAccessException {
        String name = dao.getTableName();
        name = name.replace("\"", "");
        name += ".csv";
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);

        Writer w = new OutputStreamWriter(zos);

        RelationStatus rs = new RelationStatus(dao.getTableName(),
                dao.executeCount(new SelectFilter(dao)));
        rs.factor = 250;
        rs.addListener(listeners);

        dao.getCSV().generate(new SelectFilter(dao), w, rs);
        w.flush();

        if (dao instanceof JDBCPictureDAO) {
            saveImages(zos,
                    (JDBCPictureDAO<RecordWithSingleId, Field>) dao,
                    listeners);
        }

    }

    /**
     * Saves images to zip (if needed)
     * 
     * @throws DataAccessException
     * @throws IOException
     */
    private void saveImages(
            ZipOutputStream zos,
            JDBCPictureDAO<RecordWithSingleId, Field> dao,
            @SuppressWarnings("unchecked") Listener<RelationStatus>... listeners)
            throws DataAccessException, IOException {
        if (dao.getPictureFields().isEmpty()) {
            return;
        }

        String name = dao.getTableName();
        name = name.replace("\"", "");
        // create the subdir
        ZipEntry dir = new ZipEntry(name + "/");
        zos.putNextEntry(dir);

        List<RecordWithSingleId> records = dao.getAll();
        int nrOfRecord = records.size();

        RelationStatus rs = new RelationStatus(
                dao.getTableName() + " (images)", nrOfRecord);
        rs.factor = 250;

        for (Listener<RelationStatus> listener : listeners) {
            rs.addListener(listener);
        }

        for (RecordWithSingleId record : records) {
            String baseName = name + record.getId();

            for (Field f : dao.getPictureFields()) {

                String baseNameField = baseName + "_" + f.getNameColumn();

                try {
                    if (dao.hasPicture(record, f)) {
                        zos.putNextEntry(new ZipEntry(name + "/"
                                + baseNameField));
                        dao.getPicture(record.getId(), f, zos);
                    }
                } catch (Exception e) {
                    throw new DataAccessException("Loading image failed!", e);
                }
            }

            rs.setCurrentEntry(rs.getCurrentEntry() + 1);

        }

    }

    /* * LOAD FROM ZIP - INTO DB */
    @SuppressWarnings("unchecked")
    public void loadFromZip(InputStream zip) throws IOException,
            DataAccessException {
    	Logger.out.println("Loading from zip");
        ZipInputStream zis = new ZipInputStream(zip);

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String name = entry.getName();
            name = name.substring(0, name.length() - 4);
            TableName table = dac.getNameMapping().get(name);
            SimpleDAO<?> dao = dac.getDAO(table);
            if (dao == null) {
                loadImage(zis, entry);
                continue;
            }
            StringBuilder s = new StringBuilder();
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = zis.read(buffer, 0, 1024)) >= 0) {
                s.append(new String(buffer, 0, read));
            }
            String csv = s.toString();
            
            dao.getCSV().load(csv, null, true, new CSVListener(dao.getTableName()));
        }
    }

    @SuppressWarnings("unchecked")
    private void loadImage(ZipInputStream zis, ZipEntry entry)
            throws DataAccessException {
        if (entry.getName().endsWith("/")) {
            return;
        }

        String relation = entry.getName().substring(0,
                entry.getName().indexOf("/"));
        String name = entry.getName().substring(
                entry.getName().indexOf("/") + 1);
        String idStr = name.substring(relation.length(), name.indexOf("_"));
        int id = Integer.parseInt(idStr);

        String field = name.substring(name.indexOf("_") + 1, name.length());

        TableName tn = dac.getNameMapping().get(relation);
        JDBCPictureDAO<RecordWithSingleId, Field> dao = (JDBCPictureDAO<RecordWithSingleId, Field>) dac
                .getDAO(tn);

        dao.addPicture(id, dao.getField(field), zis);
        Logger.out.println("Picture "+entry.getName()+" loaded");
    }

}
