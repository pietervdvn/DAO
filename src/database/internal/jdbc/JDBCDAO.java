package database.internal.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DataAccessException;
import database.JDBCDataAccessContext;
import database.internal.Field;
import database.internal.TableName;
import database.internal.dao.DAO;
import database.internal.record.Record;
import database.tools.filter.AbstractFilter;
import database.tools.filter.SelectFilter;
import database.tools.logging.PreparedStatement;

/**
 * The default superclass of a concrete dao with accompanying DAO.
 * 
 * It supports default methods such as add, delete and contains lots of
 * convenience methods such as getById, getWithFilter etc...
 * 
 * @author Sander
 */
public abstract class JDBCDAO<T extends Record, F extends Field>
        extends JDBCSimpleDAO<F> implements DAO<T, F> {

    public JDBCDAO(JDBCDataAccessContext dac, TableName name) {
        super(dac, name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(T... item) throws DataAccessException {
        for (T t : item) {
            add(t);
        }
    }

    /**
     * Checks constraints in the DB, e.g. duplicate mails. Get's called just
     * before adding into the DB, thé moment to throw errors!
     * 
     * @param item
     * @throws DataAccessException
     */
    protected abstract void checkConstraints(T item) throws DataAccessException;

    protected abstract T createWithCurrent(ResultSet rs) throws SQLException;

    /**
     * Insert, on position index, the value corresponding with field out of item
     * into the preparedstament
     */
    protected abstract void insertInStatement(F field, T item,
            PreparedStatement ps, int index) throws SQLException,
            DataAccessException;

    // ------------------------- FILTER RELATED STUFF --------------------- //

    @Override
    @Deprecated
    public SelectFilter createFilter() {
        return new SelectFilter(this);
    }

    @Override
    public List<T> executeFilter(SelectFilter filter)
            throws DataAccessException {
        try (PreparedStatement ps = filter.prepStatement(connection)) {
            return createListWithCurrent(ps.executeQuery());
        } catch (SQLException e) {
            throw filter.prepException(e);
        }
    }

    @Override
    public T executeSingletonFilter(SelectFilter filter)
            throws DataAccessException {
        try {
            return unpack(filter, executeFilter(filter));
        } catch (Exception e) {
            throw filter.prepException(e);
        }
    }

    // filter is passed for error messages only
    private T unpack(AbstractFilter filter, List<T> results)
            throws DataAccessException {
        if (results.size() <= 0) {
            throw new DataAccessException(
                    "Error while executing singleton filter: no elements match "
                            + filter.getSQL(), null);
        }
        if (results.size() > 1) {
            throw new DataAccessException(
                    "Error while executing singleton filter: multiple elements match "
                            + filter.getSQL(), null);
        }
        return results.get(0);
    }

    // -------------------- SOME CONVENIENCE METHODS ------------ //

    @Override
    public List<T> getAll() throws DataAccessException {
        return executeFilter(new SelectFilter(this, "Could not get all for "
                + getTableName()));
    }

    protected List<T> createListWithCurrent(ResultSet rs) throws SQLException {
        List<T> list = new ArrayList<>();
        T record = createWithCurrent(rs);
        while (record != null) {
            list.add(record);
            record = createWithCurrent(rs);
        }
        return list;
    }

}
