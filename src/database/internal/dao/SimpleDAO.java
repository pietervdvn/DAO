package database.internal.dao;

import database.DataAccessException;
import database.internal.Field;
import database.internal.TableName;
import database.tools.csv.CSV;
import database.tools.filter.DeleteFilter;
import database.tools.filter.SelectFilter;

/**
 * All methods that every DAO supports: getting some meta info and doing basic filters.
 * 
 * This is meant for these pesky daos that do not have primary keys, such as relationships
 * 
 * @author pietervdvn
 */
public interface SimpleDAO<F extends Field> {
    
    String getTableName();
    TableName getTableNameEnum();
    Field[] getFields();
    
    
    void executeFilter(DeleteFilter filter) throws DataAccessException;
    int executeCount(SelectFilter filter) throws DataAccessException;
    /**
     * Total number of records in the db
     * @return
     * @throws DataAccessException 
     */
    int count() throws DataAccessException;
    CSV getCSV();

    /**
     * Returns true if no values exist in this table.
     * @throws DataAccessException 
     */
    boolean isEmpty() throws DataAccessException;
}
