package database.internal.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import database.DataAccessException;
import database.JDBCDataAccessContext;
import database.JDBCDataAccessProvider;
import database.internal.Field;
import database.internal.TableName;
import database.internal.dao.SimpleDAO;
import database.tools.SQLGenerator;
import database.tools.csv.CSV;
import database.tools.filter.DeleteFilter;
import database.tools.filter.SelectFilter;
import database.tools.logging.LoggingConnection;
import database.tools.logging.PreparedStatement;

/**
 * Abstract class JDBC DAO
 * 
 * JDBCAbstractDAO keeps data about a connection and the metainfo.
 * 
 * Most (concrete) DAO's will use the JDBCAbstractRecord dao (which is a
 * JDBAbstractDAO too). Exceptions to this rule, such as ownerdao, will use this
 * class as superclass.
 * 
 * @param <T>
 *            Record
 * @param <F>
 *            Field
 */
public class JDBCSimpleDAO<F extends Field> implements SimpleDAO<F> {

    protected LoggingConnection connection;
    protected JDBCDataAccessContext dac;
    protected final TableName tableName;
    private final Map<String, Field> fieldMap = new HashMap<>();

    public JDBCSimpleDAO(JDBCDataAccessContext dac, TableName tableName) {
        this.connection = dac.getConnection();
        this.dac = dac;
        this.tableName = tableName;

        for (Field field : getFields()) {
            fieldMap.put(field.getNameColumn(), field);
        }

    }

    @Override
    public CSV getCSV() {
        return new CSV(this);
    }

    public Set<TableName> getReverseDeps() {
        return JDBCDataAccessProvider.getDepsFor(tableName);
    }

    /* FILTER RELATED STUFF */

    @Override
    public void executeFilter(DeleteFilter filter) throws DataAccessException {
        try (PreparedStatement ps = filter.prepStatement(connection)) {
            ps.execute();
        } catch (SQLException e) {
            throw filter.prepException(e);
        }
    }

    @Override
    public int executeCount(SelectFilter filter) throws DataAccessException {
        try (PreparedStatement ps = filter.prepCountStatement(connection)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (Exception e) {
            Exception prepped = filter.prepException(e);
            throw new DataAccessException("Could not perform count for filter",
                    prepped);
        }
    }

    @Override
    public int count() throws DataAccessException {
        return executeCount(new SelectFilter(this));
    }

    @Override
    public boolean isEmpty() throws DataAccessException {
        return 0 == executeCount(new SelectFilter(this));
    }

    /* GETTERS AND SETTERS */

    @Override
    public String getTableName() {
        return tableName.toString();
    }

    @Override
    public TableName getTableNameEnum() {
        return tableName;
    }

    protected SQLGenerator queries() {
        return new SQLGenerator(getTableNameEnum());
    }

    @SuppressWarnings("unchecked")
    @Override
    public final F[] getFields() {
        return (F[]) tableName.getFields();
    }

    public final Field getField(String name) {
        return fieldMap.get(name);
    }

    public JDBCDataAccessContext getDac() {
        return dac;
    }

    @Override
    public String toString() {
        return "DAO of "+getTableName();
    }
    
}
