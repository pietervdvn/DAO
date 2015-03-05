package database.tools.filter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DataAccessException;
import database.internal.TableName;
import database.tools.logging.LoggingConnection;
import database.tools.logging.PreparedStatement;

/**
 * Some simple data that each filter contains; the start of the
 * "spaghetti"-inheritance chain.
 * 
 * Notice that you can follow the chain with the number after
 * AbstractFilter*NUMBER*
 * 
 * @author pietervdvn
 * 
 */
abstract class AbstractFilter0Data {

    /**
     * A list of all the values ​​that must be entered in the SQL statement.
     */
    protected List<Object> values = new ArrayList<>();
    /**
     * The tableName for the FROM in the SQL statement.
     */
    protected final TableName tableName;
    /**
     * Errormessage that gets shown when the execution of the filter fails.
     */
    protected String errorMessage = null;

    public AbstractFilter0Data(TableName tableName) {
        this(tableName, null);
    }

    public AbstractFilter0Data(TableName tableName, String error) {
        this.tableName = tableName;
        this.errorMessage = error;
    }

    @Override
    public String toString() {
        return getSQL();
    }

    /**
     * Returns the SQL statement for the filter. It contains "?" which must be
     * filled in with the help of the values ​​from getValues().
     */
    public abstract String getSQL();

    /**
     * Returns a list of all the values ​​that must be entered in the SQL
     * statement.
     */
    public abstract List<Object> getValues();

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public TableName getTableName() {
        return tableName;
    }
    
    /**
     * Helper method to prepare exceptions
     */
    public DataAccessException prepException(Throwable cause) {
        DataAccessException e = new DataAccessException(
                "Could not execute select filter " + getSQL(), cause);
        if (getErrorMessage() != null) {
            e = new DataAccessException(getErrorMessage(), e);
        }
        return e;
    }

    
    protected PreparedStatement prepStatement(LoggingConnection connection, String sql)
            throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        List<Object> values = getValues();
        for (int i = 0; i < values.size(); i++) {
            ps.setObject(i + 1, values.get(i));
        }
        return ps;
    }

    /**
     * Helper method to prepare statements
     */
    public PreparedStatement prepStatement(LoggingConnection connection) throws SQLException {
        return prepStatement(connection, getSQL());
    }
    
}
