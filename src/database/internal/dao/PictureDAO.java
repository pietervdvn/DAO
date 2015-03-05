package database.internal.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import database.DataAccessException;
import database.internal.Field;
import database.internal.record.RecordWithSingleId;
import database.tools.filter.SelectFilter;

public interface PictureDAO<T extends RecordWithSingleId, F extends Field>
        extends IdDAO<T, F> {

    public List<F> getPictureFields();

    /**
     * Adds a picture in db for the given record.
     * 
     * Specify the field if this table has multiple picture fields.
     * 
     */
    void addPicture(T item, Field pictureField, File source)
            throws DataAccessException;

    /**
     * Adds a picture in db for the given record.
     * 
     * Uses the first picturefield given in the constructor
     * 
     */
    void addPicture(T item, File source) throws DataAccessException;

    boolean hasPicture(T item, F pictureField) throws DataAccessException;
    boolean hasPicture(T item) throws DataAccessException;

    
    /**
     * Gets the picture associated with this record, in the given picturefield,
     * and saves it to the given target file.
     * 
     * @throws DataAccessException
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    void getPicture(T item, F pictureField, File target)
            throws DataAccessException, FileNotFoundException, IOException;
    

    /**
     * Gets the picture associated with this record, and saves it to the given
     * target file. The first picturefield, passed in the constructor will be
     * used.
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    void getPicture(T item, File target) throws DataAccessException, FileNotFoundException, IOException;

    /**
     * Executes the filter. Each record that got returned, it's picture will be
     * saved. All pics will be saved to parent/id_name.
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public List<File> getAll(SelectFilter f, F pictureField, File parent,
            String name) throws DataAccessException, FileNotFoundException, IOException;

    /**
     * Executes the filter. Each record that got returned, it's picture will be
     * saved. All pics will be saved to parent/id_name.
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public List<File> getAll(SelectFilter f, File parent, String name)
            throws DataAccessException, FileNotFoundException, IOException;

}
