package database.internal.dao;

import database.DataAccessException;
import database.internal.Field;
import database.internal.record.RecordWithSingleId;

/**
 * A DAO that gives records with an ID, and can thus be queried on ID.
 * @author pietervdvn
 *
 * @param <T>
 * @param <F>
 */
public interface IdDAO<T extends RecordWithSingleId, F extends Field> extends DAO<T, F>{
    
    T getById(int id) throws DataAccessException;
    void delete(int id) throws DataAccessException;

}
