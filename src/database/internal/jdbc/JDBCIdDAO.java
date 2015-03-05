package database.internal.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.DataAccessException;
import database.JDBCDataAccessContext;
import database.internal.Field;
import database.internal.TableName;
import database.internal.dao.IdDAO;
import database.internal.record.RecordWithSingleId;
import database.internal.type.Types;
import database.tools.filter.DeleteFilter;
import database.tools.filter.SelectFilter;
import database.tools.logging.PreparedStatement;

/**
 * This is the abstract super class for all jdbcdao's which have a record with a
 * single id. It implements the generic delete
 * 
 * @author pietervdvn
 * 
 */
public abstract class JDBCIdDAO<T extends RecordWithSingleId, F extends Field>
        extends JDBCDAO<T, F> implements IdDAO<T, F> {
    // ik hou van lange klassenamen
    protected final F idField;

    public JDBCIdDAO(JDBCDataAccessContext dac,
            TableName name, F idField) {
        super(dac, name);
        this.idField = idField;
    }

    public F getIdField() {
        return idField;
    }

    // used for auto_generated_id
    private String[] getIdFieldAsList() {
        String[] str = new String[1];
        str[0] = idField.getNameColumn();
        return str;
    }

    public final String getIdColumn() {
        return idField.toString();
    }

    /**
     * Generic add. Works by creating the query (done by the table) and
     * requesting to the concrete class to fill in all the fields
     */
    @SuppressWarnings("unchecked")
    @Override
    public T add(T item) throws DataAccessException {
        item.checkNoId();
        item.checkRecord();
        checkConstraints(item);
        String query = queries().getInsertQuery(true);
        try (PreparedStatement ps = connection.prepareStatement(query,
                getIdFieldAsList())) {
            int i = 1;
            for (Field f : getFields()) {

                if (f != idField && f.getTypeEnum() != Types.OID) {
                    insertInStatement((F) f, item, ps, i++);
                }
            }
            /* \\//\\// */
            ps.execute();
            /* //\\//\\ */

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                item.setId(keys.getInt(1));
            } else {
                throw new SQLException("No keyvalue in the generatedkeyset");
            }
            return item;
        } catch (SQLException e) {
            throw new DataAccessException("Could not insert value " + item
                    + " into table " + getTableName(), e);
        }
    }

    /**
     * Generic update. Works by creating the query (done by the table) and
     * requesting to the concrete class to fill in all the fields
     */
    @Override
    public void update(T item) throws DataAccessException {
        item.checkId();
        item.checkRecord();
        String query = queries().getUpdateQuery();
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            int i = 1;
            List<F> flds = queries().getUpdateFieldOrder(getFields());
            for (F f : flds) {
                insertInStatement(f, item, ps, i++);
            }

            /* \\//\\// */
            ps.execute();
            /* //\\//\\ */

        } catch (SQLException e) {
            throw new DataAccessException("Could not update value " + item
                    + " in table " + getTableName(), e);
        }
    }

    /**
     * Updates a single field in the db
     * 
     * @throws DataAccessException
     */
    @SuppressWarnings("unchecked")
    protected void update(T item, F... fields) throws DataAccessException {
        item.checkId();
        item.checkRecord();
        String query = queries().getUpdateFieldQuery(fields);
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            int i = 1;
            for (F f : fields) {
                insertInStatement(f, item, ps, i++);
            }
            insertInStatement(getIdField(), item, ps, i);

            /* \\//\\// */
            ps.execute();
            /* //\\//\\ */

        } catch (SQLException e) {
            throw new DataAccessException("Could not update value " + item
                    + " in table " + getTableName(), e);
        }
    }

    @Override
    public void delete(T item) throws DataAccessException {
        item.checkId();
        delete(item.getId());
        item.invalidateId();
    }
    
    @Override
    public void delete(int id) throws DataAccessException{
        if(id < 0){
            throw new IllegalArgumentException("Trying to delete a "+getTableName()+" with id "+id);
        }
        executeFilter(new DeleteFilter(this, getIdField(), id));
    }

    public T getById(int id) throws DataAccessException {
        if (id < 1) {
            throw new IllegalArgumentException("Trying to lookup by id from "
                    + getTableName() + ", but you passed an invalid id: " + id
                    + " (id's should be >= 1)");
        }
        SelectFilter filter = new SelectFilter(this, idField, id);
        filter.setErrorMessage("Trying to get by id on table " + getTableName());
        return executeSingletonFilter(filter);
    }

}
