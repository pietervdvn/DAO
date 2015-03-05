package database.tools.logging;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PreparedStatement implements AutoCloseable {

    private final java.sql.PreparedStatement ps;
    private final Out log;

    public PreparedStatement(java.sql.PreparedStatement ps, Out log) {
        this.ps = ps;
        this.log = log;
    }

    public void setObject(int i, Object object) throws SQLException {
        ps.setObject(i, object);
    }

    @Override
    public void close() throws SQLException {
        ps.close();
    }

    public ResultSet executeQuery() throws SQLException {
        log.println("EQ " + ps.toString());
        return ps.executeQuery();
    }

    public boolean execute() throws SQLException {
        log.println("E " + ps.toString());
        return ps.execute();
    }

    public void setLong(int i, Long l) throws SQLException {
        ps.setLong(i, l);
    }

    public void setInt(int i, int integer) throws SQLException {
        ps.setInt(i, integer);
    }

    public void executeUpdate() throws SQLException {
        log.println("EU " + ps.toString());
        ps.executeUpdate();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return ps.getGeneratedKeys();
    }

    public void setBoolean(int i, boolean b) throws SQLException {
        ps.setBoolean(i, b);
    }

    public void setDouble(int i, double d) throws SQLException {
        ps.setDouble(i, d);
    }

    public void setNull(int i, int j) throws SQLException {
        ps.setNull(i, j);
    }

    public void setDate(int i, Date date) throws SQLException {
        ps.setDate(i, date);
    }

    public void setTimestamp(int i, Timestamp timestamp)
            throws SQLException {
        ps.setTimestamp(i, timestamp);
    }

    public void setString(int i, String s) throws SQLException {
        ps.setString(i, s);
    }

    @Override
    public String toString() {
        return ps.toString();
    }
    
}