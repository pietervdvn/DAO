package database.tools.csv;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import database.DataAccessException;
import database.internal.jdbc.JDBCSimpleDAO;
import database.tools.filter.SelectFilter;
import database.tools.logging.Logger;
import database.tools.utils.MVCUtils.Listener;
import database.tools.utils.MVCUtils.RelationStatus;


/**
 * Uses CSVGenerator to safe to file
 * @author pietervdvn
 *
 */
class CSVSave extends CSVGenerator{
    
    @SuppressWarnings("rawtypes")
    protected static final Listener[] empty = {};

    public CSVSave(JDBCSimpleDAO<?> dao) {
        super(dao);
    }
    
    public void saveTo(
            File f,
            @SuppressWarnings("unchecked") Listener<RelationStatus>... listeners)
            throws IOException, DataAccessException {
        saveTo(f, new SelectFilter(dao), listeners);
    }

    @SuppressWarnings("unchecked")
    public void saveTo(File f) throws IOException, DataAccessException {
        saveTo(f, new SelectFilter(dao), empty);
    }

    /**
     * Saves the csv to the given file.
     */
    @SuppressWarnings("unchecked")
    public void saveTo(File f, SelectFilter filter) throws IOException,
            DataAccessException {
        saveTo(f, filter, empty);
    }

    /**
     * Saves the csv to the given file.
     */
    public void saveTo(
            File f,
            SelectFilter filter,
            @SuppressWarnings("unchecked") Listener<RelationStatus>... listeners)
            throws IOException, DataAccessException {
        Logger.out.println(f.getPath());
        if (!f.exists()) {
            f.createNewFile();
        }
        try (OutputStreamWriter out = new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(f)))) {
            RelationStatus rs = new RelationStatus(dao.getTableName(),
                    dao.executeCount(filter));
            rs.setCurrentEntry(0);
            for (Listener<RelationStatus> listener : listeners) {
                rs.addListener(listener);
            }
            generate(filter, out, rs);
        }
    }

}
