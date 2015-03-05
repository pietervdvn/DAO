package database.internal.jdbc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import database.DataAccessException;
import database.JDBCDataAccessContext;
import database.internal.Field;
import database.internal.TableName;
import database.internal.dao.PictureDAO;
import database.internal.record.RecordWithSingleId;
import database.tools.SQLGenerator;
import database.tools.filter.SelectFilter;
import database.tools.logging.PreparedStatement;

/**
 * 
 * @author Eveline
 */
public abstract class JDBCPictureDAO<T extends RecordWithSingleId, F extends Field>
        extends JDBCIdDAO<T, F> implements PictureDAO<T, F> {

    protected final List<F> pictureFields = new ArrayList<>();

    @SafeVarargs
    public JDBCPictureDAO(JDBCDataAccessContext dac,
            TableName tableName, F idField, F... pictureField) {
        super(dac, tableName, idField);
        if (pictureField != null) {
            for (F f : pictureField) {
                pictureFields.add(f);
            }
        }
        if (pictureFields.size() == 0) {
            throw new IllegalArgumentException(
                    "At least one picturefield should be given in a picturefielddao");
        }
    }

    public List<F> getPictureFields() {
        return pictureFields;
    }

    /**
     * Adds a picture in db for the given record.
     * 
     * Specify the field if this table has multiple picture fields.
     * 
     */
    public void addPicture(T item, Field pictureField, File source)
            throws DataAccessException {
        if (item.getId() == 0) {
            add(item);
        }
        add(item.getId(),new SQLGenerator(tableName).getUpdateFieldQuery(pictureField), source);
    }

    public void addPicture(int id, Field pictureField, InputStream source)
            throws DataAccessException {
        try {
            add(id, new SQLGenerator(tableName).getUpdateFieldQuery(pictureField), source);
        } catch (IOException e) {
            throw new DataAccessException("Could not load image", e);
        }
    }

    /**
     * Adds a picture in db for the given record.
     * 
     * Uses the first picturefield given in the constructor
     * 
     */
    public void addPicture(T item, File source) throws DataAccessException {
        addPicture(item, pictureFields.get(0), source);
    }

    /* ************ GET **************** */

    /**
     * Get's a picture out of this DAO, for the given id. The picture will be
     * the one out of the picturefield, and saved to target. Throws a
     * DataAccessException is something went wrong
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void getPicture(int id, Field pictureField, File target)
            throws DataAccessException, FileNotFoundException, IOException {
        getPicture(id, pictureField, new BufferedOutputStream(
                new FileOutputStream(target)));
    }

    /**
     * Get's a picture out of this DAO, for the given id. The picture will be
     * the one out of the picturefield, and saved to target. Throws a
     * DataAccessException is something went wrong
     * 
     * @throws IOException
     */
    public void getPicture(int id, Field pictureField, OutputStream target)
            throws DataAccessException, IOException {
        getOne(id, getSelect(pictureField), target);
    }

    private String getSelect(Field pictureField) {
        return "SELECT " + pictureField.getNameColumn() + " FROM "
                + getTableName() + " WHERE " + getIdColumn() + " = ?";
    }

    /**
     * Gets the picture associated with this record, in the given picturefield,
     * and saves it to the given target file.
     */
    public void getPicture(T item, F pictureField, File target)
            throws DataAccessException, FileNotFoundException, IOException {
        item.checkId();
        getPicture(item.getId(), pictureField, target);
    }

    /**
     * Gets the picture associated with this record, and saves it to the given
     * target file. The first picturefield, passed in the constructor will be
     * used.
     */
    public void getPicture(T item, File target) throws DataAccessException,
            FileNotFoundException, IOException {
        item.checkId();
        getPicture(item, pictureFields.get(0), target);
    }

    @Override
    public boolean hasPicture(T item, F pictureField)
            throws DataAccessException {
        item.checkId();
        return hasPicture(item.getId(), getSelect(pictureField));
    }

    @Override
    public boolean hasPicture(T item) throws DataAccessException {
        return hasPicture(item, pictureFields.get(0));
    }
    
    /*------- WRITE TO DB - READ FROM DISK ------------*/

    /**
     * 
     * Adds a picture in the DB. The statement should be a statement in which
     * two args can be filled in. The first is the picture, the second is the id
     * of the picture/record.
     * 
     * @throws FileNotFoundException
     */
    private void add(int id, String statement, File file)
            throws DataAccessException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            add(id, statement, in);
        } catch (IOException e) {
            throw new DataAccessException("File not found: " + file.getPath(),
                    e);
        }
    }

    /**
     * 
     * Adds a picture in the DB. The statement should be a statement in which
     * two args can be filled in. The first is the picture, the second is the id
     * of the picture/record.
     * 
     * @throws IOException
     */
    private void add(int id, String statement, InputStream in)
            throws DataAccessException, IOException {
        try {
            // All LargeObject API calls must be within a transaction
            dac.begin();

            // Get the Large Object Manager to perform operations with
            @SuppressWarnings("deprecation")
            LargeObjectManager lobj = ((org.postgresql.PGConnection) connection.getConnection())
                    .getLargeObjectAPI();
            Long oid = readImage(lobj, in);

            // add to database
            try (@SuppressWarnings("deprecation")
            java.sql.PreparedStatement ps = connection.getConnection().prepareStatement(statement)) {
                ps.setLong(1, oid);
                ps.setInt(2, id);
                ps.executeUpdate();
                connection.getLogStream().println("Image added to "+getTableName()+", record "+id+" statement "+statement);
            }
            dac.commit();
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException("addPicture failed on " + statement,
                    ex);
        } finally {
            dac.rollback();
            dac.startAutoCommit();
        }
    }

    /**
     * Reads an image from disk into the Large object manager
     * 
     * @throws IOException
     */
    private long readImage(LargeObjectManager lobj, InputStream in)
            throws SQLException, DataAccessException, IOException {

        // create a new large object
        long oid = lobj.createLO(LargeObjectManager.READ
                | LargeObjectManager.WRITE);

        // open the large object for write
        LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);

        // Now open the file

        // copy the data from the file to the large object
        byte buf[] = new byte[2048];
        @SuppressWarnings("unused")
        int s, tl = 0;
        while ((s = in.read(buf, 0, 2048)) > 0) {
            obj.write(buf, 0, s);
            tl += s;
        }
        return oid;
    }

    /*------- READ FROM DB - WRITE TO DISK ------------*/

    /**
     * Writes the image to a file. Expects a statement where only one value can
     * be filled in: the id
     * 
     * @throws IOException
     */
    private void getOne(int id, String statement, OutputStream output)
            throws DataAccessException, IOException {

        try {
            // All LargeObject API calls must be within a transaction
            dac.begin();

            // Get the Large Object Manager to perform operations with
            @SuppressWarnings("deprecation")
            LargeObjectManager lobj = ((org.postgresql.PGConnection) connection.getConnection())
                    .getLargeObjectAPI();
            // database
            try (PreparedStatement ps = connection.prepareStatement(statement)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    writeImageTo(lobj, rs, output);
                } else {
                    throw new DataAccessException("No images found for id "
                            + id + " with the statement " + statement, null);
                }
            }
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException("getPicture failed on " + statement,
                    ex);
        } finally {
            dac.rollback();
            dac.startAutoCommit();
        }
    }

    /**
     * Writes the image to a file. Expects a statement where two values can be
     * filled in: the picture and id.
     */
    private boolean hasPicture(int id, String statement)
            throws DataAccessException {

        try (PreparedStatement ps = connection.prepareStatement(statement)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && 0 != rs.getLong(1);
        } catch (SQLException e) {
            throw new DataAccessException("hasPicture failed on " + statement,
                    e);
        }
    }

    /**
     * Executes the filter. Each record that got returned, it's picture will be
     * saved. All pics will be saved to parent/id_name.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    public List<File> getAll(SelectFilter f, F pictureField, File parent,
            String name) throws DataAccessException, FileNotFoundException,
            IOException {

        List<T> recs = executeFilter(f);
        List<File> results = new ArrayList<>();
        for (int i = 0; i < recs.size(); i++) {
            File target = new File(parent.getAbsoluteFile().getAbsolutePath()
                    + "/" + (i + 1) + "_" + name);
            getPicture(recs.get(i), pictureField, target);
            results.add(target);
        }
        return results;

    }

    /**
     * Executes the filter. Each record that got returned, it's picture will be
     * saved. All pics will be saved to parent/id_name.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    public List<File> getAll(SelectFilter f, File parent, String name)
            throws DataAccessException, FileNotFoundException, IOException {
        return getAll(f, pictureFields.get(0), parent, name);
    }

    /**
     * Writes the first object of the resultset to the given outputstream.
     * Returns true when succesfull or false when no object has been found
     */
    public void writeImageTo(LargeObjectManager lobj, ResultSet rs,
            OutputStream target) throws SQLException, IOException,
            DataAccessException {
        long oid = rs.getLong(1);
        if (oid != 0) {
            LargeObject obj = lobj.open(oid, LargeObjectManager.READ);

            // read the data
            byte buf[] = new byte[obj.size()];
            obj.read(buf, 0, obj.size());

            target.write(buf);
            // Close the object
            obj.close();
        } else {
            throw new DataAccessException("No image in the resultset", null);
        }
    }
}
