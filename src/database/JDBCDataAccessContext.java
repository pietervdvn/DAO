package database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import database.dao.UserDAO;
import database.dao.internal.JDBCUserDAO;
import database.internal.Field;
import database.internal.TableName;
import database.internal.dao.PictureDAO;
import database.internal.dao.SimpleDAO;
import database.internal.record.RecordWithSingleId;
import database.tools.DatabaseUtils;
import database.tools.filter.JDBCSingleFieldFilterExecutor;
import database.tools.filter.SingleFieldFilterExecutor;
import database.tools.logging.Logger;
import database.tools.logging.LoggingConnection;

/**
 * JDBC Data Access Context
 */
public class JDBCDataAccessContext implements DataAccessContext {

    private final LoggingConnection connection;

    private Map<TableName, SimpleDAO<?>> daos;
    private Map<String, TableName> names;

    private JDBCSingleFieldFilterExecutor singleFieldFilterExecutor;

    private boolean didReset = false;
    
    /**
     * The user that is talking with the server, used for logging shizzle
     */
    private int user = -1;
    
    
    /**
     * All the dao's. Can be null;
     */
    private UserDAO userDAO;

    public JDBCDataAccessContext(LoggingConnection loggingConn)
            throws DataAccessException {
        this.connection = loggingConn;
        try {
            if (connection.isClosed()) {
                throw new DataAccessException(
                        "The connection is closed. This should not happen when opening a new DAC",
                        null);
            }
        } catch (SQLException e1) {
            throw new DataAccessException("Polling connection went wrong", e1);
        }
    };
    
    public UserDAO getUserDAO() {
    	if(userDAO == null){
    		userDAO = new JDBCUserDAO(this);
    	}
		return userDAO;
	}

    public JDBCDataAccessContext(Connection connection, int user,
            OutputStream out) throws DataAccessException {
        this(new LoggingConnection(connection, user, out));
        this.user = user;
    }

    @Override
    public DataAccessContext getDacFor(int id) throws DataAccessException {
        return new JDBCDataAccessContext(connection.getLoggingConnectionFor(id));
    }
    
    @Override
    public List<SimpleDAO<?>> getAllDAOs() {
        SimpleDAO<?>[] all = { 
                new JDBCUserDAO(this)
                };
        return Arrays.asList(all);
    }

    /**
     * This method is added for DEBUG only (for the automatic init code, which
     * dumps lots of testdata in the DB) It should NEVER EVER be used in real
     * code. It's protected so that it's not visible outside of the .jar
     * 
     * @return
     */
    public LoggingConnection getConnection() {
        return connection;
    }

    @Override
    public SingleFieldFilterExecutor getSingleFieldFilterExecutor() {
        if (singleFieldFilterExecutor == null) {
            singleFieldFilterExecutor = new JDBCSingleFieldFilterExecutor(
                    connection);
        }
        return singleFieldFilterExecutor;
    }

    @Override
    public void close() throws DataAccessException {
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not close connection", ex);
        }
    }
    
    
    public SimpleDAO<?> getDAO(TableName t) {
        return getDAOMapping().get(t);
    }

    public Map<TableName, SimpleDAO<?>> getDAOMapping() {
        if (daos == null) {
            daos = new HashMap<>();
            for (SimpleDAO<?> dao : getAllDAOs()) {
                daos.put(dao.getTableNameEnum(), dao);
            }
        }
        return daos;
    }

    public Map<String, TableName> getNameMapping() {
        if (names == null) {
            names = new HashMap<>();
            for (TableName tn : TableName.values()) {
                String name = tn.toString().replace("\"", "");
                names.put(name, tn);
            }
        }
        return names;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PictureDAO<RecordWithSingleId, Field>> getPictureDAOs() {
        PictureDAO<?, ?>[] all = { /* TODO add all picturedaos here */};
        List<PictureDAO<RecordWithSingleId, Field>> list = new ArrayList<>();
        for (PictureDAO<?, ?> dao : all) {
            list.add((PictureDAO<RecordWithSingleId, Field>) dao);
        }
        return list;
    }

    @Override
    public void begin() throws DataAccessException {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            Logger.out.println(ex);
            throw new DataAccessException("SetAutoCommit failed", ex);
        }
    }

    @Override
    public void commit() throws DataAccessException {
        try {
            connection.commit();
        } catch (SQLException ex) {
            Logger.out.println(ex);
            throw new DataAccessException("Commit failed", ex);
        }
    }

    @Override
    public void rollback() throws DataAccessException {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            Logger.out.println(ex);
            throw new DataAccessException("Rollback failed", ex);
        }
    }

    @Override
    public void startAutoCommit() throws DataAccessException {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ex) {
            Logger.out.println(ex);
            throw new DataAccessException("Setting autocommit failed", ex);
        }
    }

    @Override
    public boolean isClosed() throws DataAccessException {
        try {
            return connection.isClosed();
        } catch (SQLException ex) {
            Logger.out.println(ex);
            throw new DataAccessException(
                    "Checking if connection is closed failed", ex);
        }
    }

    /**
     * Uses TableName to reïnitiate all tables. REMOVES ALL DATA IN DB! I
     * repeat: REMOVES ALL DATA IN DB!
     * 
     * @throws SQLException
     */
    @Override
    public void reset() throws DataAccessException {
        if (didReset) {
            throw new DataAccessException(
                    "You already did reset this DAC once. Get a fresh one",
                    null);
        }
        didReset = true;
        try {
            new DatabaseUtils(this).reset();
        } catch (SQLException e) {
            throw new DataAccessException("Could not reset the db", e);
        }
    }

    @Override
    public void loadFromZip(InputStream in) throws IOException,
            DataAccessException {
        new DatabaseUtils(this).loadFromZip(in);
    }

    @Override
    public void saveToZip(OutputStream out) throws DataAccessException,
            IOException {
        new DatabaseUtils(this).saveToZip(out);
    }

    /**
     * Checks if each DAO is empty
     * 
     * @return
     * @throws DataAccessException
     */
    @Override
    public boolean isEmpty() throws DataAccessException {
        boolean isEmpty = true;
        Queue<SimpleDAO<?>> q = new LinkedList<>(getAllDAOs());
        while (isEmpty && !q.isEmpty()) {
            SimpleDAO<?> dao = q.poll();
            isEmpty = isEmpty || dao.isEmpty();
        }
        return isEmpty;
    }

    @Override
    public JDBCDataAccessContext breakAbstraction() {
        return this;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }
}
