package database.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import database.DataAccessException;
import database.JDBCDataAccessContext;
import database.tools.utils.DatabaseResetter;
import database.tools.utils.DatabaseZipper;

/**
 * Databaseutils export all util functionality into one class. This way, used
 * classes can be kept package private
 * 
 * @author pietervdvn
 * 
 */
public class DatabaseUtils {

    private final JDBCDataAccessContext dac;

    public DatabaseUtils(JDBCDataAccessContext dac) {
        this.dac = dac;
    }

    public void reset() throws SQLException, DataAccessException {
        new DatabaseResetter(dac).reset();
    }

    public void saveToZip(OutputStream out) throws DataAccessException,
            IOException {
        new DatabaseZipper(dac).saveZipTo(out);
    }

    public void loadFromZip(InputStream in) throws IOException,
            DataAccessException {
        new DatabaseZipper(dac).loadFromZip(in);
    }

}
