package database.tools.filter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DataAccessException;
import database.internal.Field;
import database.internal.type.TypeUtils;
import database.tools.logging.LoggingConnection;
import database.tools.logging.PreparedStatement;

/**
 * JDBC implementation of SingleFieldFilterExecutor
 * 
 * @author Sander
 */
public class JDBCSingleFieldFilterExecutor implements SingleFieldFilterExecutor {

    private LoggingConnection connection;

    public JDBCSingleFieldFilterExecutor(LoggingConnection connection) {
        this.connection = connection;
    }

    @Override
    public List<String> executeString(SelectFilter filter, Field field)
            throws DataAccessException {
        TypeUtils.checkString(field);
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = prepStatement(filter, field)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw filter.prepException(e);
        }
        return list;
    }

    @Override
    public List<Integer> executeInt(SelectFilter filter, Field field)
            throws DataAccessException {
        TypeUtils.checkInteger(field);
        List<Integer> list = new ArrayList<>();
        try (PreparedStatement ps = prepStatement(filter, field)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw filter.prepException(e);
        }
        return list;
    }

    @Override
    public List<Boolean> executeBoolean(SelectFilter filter, Field field)
            throws DataAccessException {
        TypeUtils.checkBoolean(field);
        List<Boolean> list = new ArrayList<>();
        try (PreparedStatement ps = prepStatement(filter, field)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getBoolean(1));
            }
        } catch (SQLException e) {
            throw filter.prepException(e);
        }
        return list;
    }

    @Override
    public List<Double> executeDouble(SelectFilter filter, Field field)
            throws DataAccessException {
        TypeUtils.checkFloat(field);
        List<Double> list = new ArrayList<>();
        try (PreparedStatement ps = prepStatement(filter, field)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getDouble(1));
            }
        } catch (SQLException e) {
            throw filter.prepException(e);
        }
        return list;
    }

    /**
     * Helper method to prepare statements
     * 
     */
    private PreparedStatement prepStatement(SelectFilter filter, Field field)
            throws SQLException {
        PreparedStatement ps = connection
                .prepareStatement(filter.getSQL(field));
        List<Object> values = filter.getValues();
        for (int i = 0; i < values.size(); i++) {
            ps.setObject(i + 1, values.get(i));
        }
        return ps;
    }

}