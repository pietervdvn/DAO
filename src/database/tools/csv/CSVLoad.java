package database.tools.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import database.DataAccessException;
import database.internal.Field;
import database.internal.jdbc.JDBCSimpleDAO;
import database.tools.utils.MVCUtils.Listener;
import database.tools.utils.MVCUtils.RelationStatus;

public class CSVLoad extends CSVRead{
    
    public CSVLoad(JDBCSimpleDAO<?> dao) {
        super(dao);
    }

    /**
     * Loads a CSV-string into the DB. First line is expected to be the header.
     * This method will append all entries in the DB, and ignore the ID-field
     * 
     * @throws DataAccessException
     */
    @SuppressWarnings("unchecked")
    public int load(String csv, Field idField,
            Listener<RelationStatus>... listeners) throws DataAccessException {
        return load(csv, idField, false, listeners);
    }

    @SuppressWarnings("unchecked")
    public int load(String csv, Field idField) throws DataAccessException{
        return load(csv, idField, empty);
    }
            

    /**
     * Same as load, but from file.
     */
    @SuppressWarnings("unchecked")
    public int load(File f, Field idField,
            Listener<RelationStatus>... listeners)
            throws FileNotFoundException, DataAccessException {
        return load(f, idField, false, listeners);
    }

    @SuppressWarnings("unchecked")
    public int load(File f, Field idField) throws FileNotFoundException, DataAccessException {
        return load(f, idField, empty);
    }

    /**
     * Same as load, but from file. Returns number of loaded entries
     */
    @SafeVarargs
    public final int load(File f, Field idField, boolean overwrite,
            Listener<RelationStatus>... listeners)
            throws FileNotFoundException, DataAccessException {
        try (Scanner sc = new Scanner(f)) {
            sc.useDelimiter("\n");
            StringBuilder b = new StringBuilder();
            while (sc.hasNext()) {
                b.append(sc.next());
                b.append("\n");
            }
            return load(b.toString(), idField, overwrite, listeners);
        }
    }

    @SuppressWarnings("unchecked")
    public int load(String csv, Field idField, boolean overwrite) throws DataAccessException{
        return load(csv, idField, overwrite, empty);
    }

}
