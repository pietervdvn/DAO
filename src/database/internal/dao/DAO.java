package database.internal.dao;

import java.util.List;

import database.DataAccessException;
import database.internal.Field;
import database.internal.record.Record;
import database.tools.filter.SelectFilter;

/**
 * The superinterface of all normal DAO's, with all common operations.
 *
 * @param <T> Record class
 * @param <F> Field enum
 */
public interface DAO<T extends Record, F extends Field> extends SimpleDAO<F>{

    /**
     * Returns actual item (with filled id)
     * @param item
     * @throws DataAccessException
     */
    T add(T item) throws DataAccessException;
    @SuppressWarnings("unchecked")
    void add(T... item) throws DataAccessException;
    void update(T item) throws DataAccessException;
    void delete(T item) throws DataAccessException;

    /**
     * Use 'new SelectFilter(dao)' instead, or another convenient constructor, such as
     * 'new SelectFilter(dao, field, value)'
     */
    @Deprecated
    SelectFilter createFilter();
    /**
     * Executes given filter.
     * @param filter
     * @return
     * @throws DataAccessException
     */
    List<T> executeFilter(SelectFilter filter) throws DataAccessException;
     
    /**
     * Gets a single element that matches the filter, throws an exception if
     * more or no elements match
     * @param filter
     * @return
     * @throws DataAccessException 
     */
    T executeSingletonFilter(SelectFilter filter) throws DataAccessException;
    
    List<T> getAll() throws DataAccessException;
    
}
