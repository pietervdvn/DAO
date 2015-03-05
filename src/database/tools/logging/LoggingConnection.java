package database.tools.logging;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class LoggingConnection {

    private final Connection connection;
    private final OutputStream out;
    private final Out logStream;

    public LoggingConnection(Connection c, int u, OutputStream logStream) {
        this.connection = c;
        if (logStream != null) {
            this.logStream = new UserDateLogger(u, logStream);
        } else {
            this.logStream = new SlashDevSlashNull();
        }
        this.out = logStream;
    }

    public LoggingConnection getLoggingConnectionFor(int u) {
        return new LoggingConnection(connection, u, out);
    }

    public void close() throws SQLException {
        connection.close();
    }

    public PreparedStatement prepareStatement(String query) throws SQLException {
        return new PreparedStatement(connection.prepareStatement(query),
                logStream);
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Only use if you know what you are doing
     * 
     * @return
     */
    @Deprecated
    public Connection getConnection() {
        return connection;
    }

    public Out getLogStream() {
        return logStream;
    }

    public PreparedStatement prepareStatement(String query, String[] idFields)
            throws SQLException {
        return new PreparedStatement(connection.prepareStatement(query,
                idFields), logStream);
    }

    public void setAutoCommit(boolean b) throws SQLException {
        connection.setAutoCommit(b);
    }

    public void rollback() throws SQLException {
        connection.rollback();
    }

    public boolean isClosed() throws SQLException {
        return connection.isClosed();
    }

}
