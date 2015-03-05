package database;

/**
 * Data Access Provider This class represents an abstraction of the hole
 * database. Used to access the DataAccessContext which is in turn used to
 * access the different DAO's
 */
public interface DataAccessProvider {

    public DataAccessContext getDataAccessContext(int userid) throws DataAccessException;
    public DataAccessContext getDataAccessContext() throws DataAccessException;
}
