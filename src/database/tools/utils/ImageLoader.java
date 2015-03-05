package database.tools.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import database.DataAccessException;
import database.internal.Field;
import database.internal.jdbc.JDBCPictureDAO;
import database.internal.record.RecordWithSingleId;
import database.internal.type.Types;
import database.tools.logging.Logger;

/***
 * utils to save and load images from file
 * 
 * @author pietervdvn
 * 
 */
public class ImageLoader<T extends RecordWithSingleId, F extends Field> {

    private final JDBCPictureDAO<T, F> figDao;

    public ImageLoader(JDBCPictureDAO<T, F> figureDAO) {
        this.figDao = figureDAO;
    }

    /**
     * Loads images found in datadir. Assumes accompanying records do exist<code>
     * Datadir // pass this one in file
     * | table.csv
     * + table
     * | | table1_field // these things are uploaded
     * | | table2_field
     * </code>
     * 
     * @param dataDir
     * @throws DataAccessException 
     */
    public void loadImages(File dataDir) throws DataAccessException {
        String path = dataDir.getAbsolutePath()+"/"+figDao.getTableName();
        File imageDir = new File(path);
        if(!imageDir.exists()){
            Logger.out.println("No images found");
            return;
        }
        File[] files = imageDir.listFiles();
        for (File image : files) {
            loadImage(image);
        }
    }
    
    public void loadImage(File target) throws DataAccessException{
        String path = target.getName();
        String tableName = figDao.getTableName();
        if(!(path.startsWith(tableName) && path.contains("_"))){
            return;
        }
        path = path.substring(tableName.length());
        String idstr = path.substring(0, path.indexOf("_"));
        int id = Integer.parseInt(idstr);
        String fieldStr = path.substring(path.indexOf("_")+1);
        Field field = figDao.getField(fieldStr);
        T record = figDao.getById(id);
        
        figDao.addPicture(record, field, target);
        Logger.out.println("Loaded "+target.getName());
    }
    
    /**
     * Creates a new directory for the table, saves all images into
     * @param allDatadir
     * @throws IOException 
     * @throws DataAccessException 
     * @throws FileNotFoundException 
     */
    public void saveImages(File allDatadir) throws FileNotFoundException, DataAccessException, IOException{
        List<T> records= figDao.getAll();
        File datadir = new File(allDatadir.getAbsolutePath()+"/"+figDao.getTableName().replace("\"", ""));
        if(!datadir.exists()){
            datadir.mkdir();
        }
        for (T t : records) {
            saveImagesOf(t, datadir);
        }
    }
    
    public void saveImagesOf(T record, File datadir) throws FileNotFoundException, DataAccessException, IOException{
        for (F field : figDao.getPictureFields()) {
            saveImagesOf(record, datadir, field);
        }
    }
    
    /**
     * Saves the image of the record in a new made file.
     * @param record
     * @param dataDir: the parent dir, in which a new file is made
     * @param f
     * @throws IOException 
     * @throws DataAccessException 
     * @throws FileNotFoundException 
     */
    public void saveImagesOf(T record, File dataDir, F f) throws FileNotFoundException, DataAccessException, IOException{
        if(f.getTypeEnum() != Types.OID || !figDao.hasPicture(record, f)){
            return;
        }
        
        File image = new File(dataDir.getAbsolutePath()+"/"+figDao.getTableName().replace("\"", "")+record.getId()+"_"+f.getNameColumn());
        figDao.getPicture(record, f, image);
        Logger.out.println("saved"+image.getName());

    }
}
