package database.tools.filter;

import java.util.List;

import database.DataAccessException;
import database.internal.Field;

/**
 * Class for executing a filter for one single field.
 *
 * @author Sander
 */
public interface SingleFieldFilterExecutor {

    /**
     * Get a list of strings of all values of the filter for the field.
     *
     * @param filter
     * @param field
     * @return
     * @throws DataAccessException
     */
    public List<String> executeString(SelectFilter filter, Field field) throws DataAccessException;

    /**
     * Get a list of ints of all values of the filter for the field.
     *
     * @param filter
     * @param field
     * @return
     * @throws DataAccessException
     */
    public List<Integer> executeInt(SelectFilter filter, Field field) throws DataAccessException;
    
    
    /**
     * Get a list of ints of all values of the filter for the field
     */
    public List<Double> executeDouble(SelectFilter filter, Field field) throws DataAccessException;
    
    public List<Boolean> executeBoolean(SelectFilter filter, Field field) throws DataAccessException;

}
