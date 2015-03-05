package database;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import database.internal.Field;
import database.internal.TableName;
import database.tools.logging.Logger;

/**
 * 
 * JDBC Data Access Provider
 */
public class JDBCDataAccessProvider implements DataAccessProvider {

    private final Properties databaseProperties;

    public static final String URL = "url";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String LOG = "log";

    /**
     * This map contains all the reverse dependencies. The set you get by
     * reverseDeps.get(table) will contains all tables that need this table E.g.
     * if expense depends on user, `reverseDeps.get(USER).contains(EXPENSE) ==
     * true`
     * 
     */
    private static final Map<TableName, Set<TableName>> reverseDeps = createRevDeps();
    
    private static Map<TableName, Set<TableName>> createRevDeps(){
        HashMap<TableName, Set<TableName>> allDeps = new HashMap<>();
        for(TableName table : TableName.values()){
            Set<TableName> deps = new HashSet<>();
            
            for(TableName dep : TableName.values()){
                for(Field f : dep.getFields()){
                    if(f.getReference() == table){
                        deps.add(dep);
                        break;
                    }
                }
            }
            allDeps.put(table, deps);
            
        }
        return allDeps;
    }

    public JDBCDataAccessProvider(Properties databaseProperties) {
        this.databaseProperties = databaseProperties;
        Logger.out.println("Connecting to "
                + databaseProperties.getProperty(URL));
    }

    /**
     * @param resourceName
     *            name of the properties file for the connection with the
     *            database
     */
    public JDBCDataAccessProvider(String resourceName) {
        // Get database info
        try (InputStream inp = JDBCDataAccessProvider.class
                .getResourceAsStream(resourceName)) {
            databaseProperties = new Properties();
            databaseProperties.load(inp);
        } catch (Exception ex) {
            throw new RuntimeException("Could not read database properties", ex);
        }
    }
    
    public static Set<TableName> getDepsFor(TableName table){
        return reverseDeps.get(table);
    }

    /**
     * Default properties file "database.properties".
     */
    public JDBCDataAccessProvider() {
        this(getStandardDatabaseProperties());
    }

    /**
     * Test which properties file should be used.
     */
    private static String getStandardDatabaseProperties() {
        String resourceName = "database.properties";
        try (InputStream inp = JDBCDataAccessProvider.class
                .getResourceAsStream(resourceName)) {
            if (inp != null) {
                return resourceName;
            }
        } catch (IOException | NullPointerException ex) {
            return "/" + resourceName;
        }
        return "/" + resourceName;
    }

    /**
     * Opens a new conntection with the database.
     */
    private Connection getConnection() throws SQLException {
        String user = databaseProperties.getProperty(USER);
        String url = databaseProperties.getProperty(URL);
        String password = databaseProperties.getProperty(PASSWORD);

        if (url == null) {
            throw new IllegalArgumentException(
                    "The database.properties you provided does not have an URL (or the properties do not exists)");
        }

        if (user != null && password != null) {
            return DriverManager.getConnection(url, user, password);
        } else {
            return DriverManager.getConnection(url);
        }
    }

    @Override
    public JDBCDataAccessContext getDataAccessContext()
            throws DataAccessException {
        return getDataAccessContext(-1);
    }

    /**
     * 
     * @return DataAccessContext for the connection with the database
     * @throws DataAccessException
     */
    @Override
    public JDBCDataAccessContext getDataAccessContext(int id)
            throws DataAccessException {
        try {
            OutputStream out = getLogStream();
            return new JDBCDataAccessContext(getConnection(), id, out);
        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Could not create data access context", ex);
        }

    }

    private OutputStream getLogStream() {
        if (!databaseProperties.containsKey(LOG)) {
            Logger.out
                    .println("No property "
                            + LOG
                            + " found in the properties. This means we wont log all the actions");
            return null;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss")
                .format(new Date());
        try {
            File log = new File(databaseProperties.getProperty(LOG) + "/" + now);
            return new BufferedOutputStream(new FileOutputStream(log));
        } catch (FileNotFoundException e) {
            Logger.err.print("Could not open logfile!");
            e.printStackTrace();
            return null;
        }
    }
}
