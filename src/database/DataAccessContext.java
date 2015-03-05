package database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import database.dao.UserDAO;
import database.internal.Field;
import database.internal.dao.PictureDAO;
import database.internal.dao.SimpleDAO;
import database.internal.record.RecordWithSingleId;
import database.tools.filter.SingleFieldFilterExecutor;

/**
 * Data Access Context This class is an abstraction of a databaseconnection All
 * actions with the DAO's will be performed on the same connection Every time
 * you make a new DataAccesContext-object you make a new connection to the
 * database. Used to access the different DAO's
 */
public interface DataAccessContext  {

    public void close() throws DataAccessException;

    public void begin() throws DataAccessException;

    public void commit() throws DataAccessException;

    public void rollback() throws DataAccessException;

    public void startAutoCommit() throws DataAccessException;
    
    public boolean isClosed() throws DataAccessException;

    public DataAccessContext getDacFor(int id) throws DataAccessException;
    
    public SingleFieldFilterExecutor getSingleFieldFilterExecutor();

    public List<SimpleDAO<?>> getAllDAOs();
    
    public UserDAO getUserDAO();


    /**
     * Uses TableName to reïnitiate all tables.
     * REMOVES ALL DATA IN DB!
     * I repeat:
     * REMOVES ALL DATA IN DB!
     */
    void reset() throws DataAccessException;
    
    /**
     * Checks if each DAO (except role-dao) is empty
     */
    public boolean isEmpty() throws DataAccessException;

    public List<PictureDAO<RecordWithSingleId, Field>> getPictureDAOs();

    /**
     * Breaks the abstraction by giving the actual JDBC-implementation.
     * USE WITH CAUTION AND ONLY IF YOU KNOW WHAT YOU DO!
     */
    @Deprecated
    public JDBCDataAccessContext breakAbstraction();
    
    public void loadFromZip(InputStream in) throws IOException, DataAccessException;
    public void saveToZip(OutputStream out) throws DataAccessException, IOException;


}
