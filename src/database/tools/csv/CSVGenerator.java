package database.tools.csv;

import static database.tools.csv.CSVUtils.escape;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DataAccessException;
import database.internal.Field;
import database.internal.jdbc.JDBCSimpleDAO;
import database.internal.type.Types;
import database.tools.filter.SelectFilter;
import database.tools.logging.LoggingConnection;
import database.tools.logging.PreparedStatement;
import database.tools.utils.MVCDefaultListeners.CSVListener;
import database.tools.utils.MVCUtils.RelationStatus;

/**
 * All stuff related to generating the CSV
 * @author pietervdvn
 *
 */
class CSVGenerator {
    
    
    protected final JDBCSimpleDAO<?> dao;
    protected final LoggingConnection con;

    public CSVGenerator(JDBCSimpleDAO<?> dao) {
        this.dao = dao;
        this.con = dao.getDac().getConnection();
    }

    /**
     * Generates the CSV as string for all values from the DAO. Exactly the same
     * as generate(new SelectFilter(dao)), as that is literally the
     * implementation. This documentation is longer then the actual method by
     * now;
     * 
     * @throws IOException
     */
    public String generate() throws DataAccessException, IOException {
        SelectFilter f = new SelectFilter(dao);
        return generate(f, getDefRS(f));
    }

    public String generate(SelectFilter filter) throws IOException,
            DataAccessException {
        return generate(filter, getDefRS(filter));
    }

    public String generate(RelationStatus rs) throws DataAccessException,
            IOException {
        return generate(new SelectFilter(dao), rs);
    }

    public String generate(SelectFilter filter, RelationStatus rs)
            throws IOException, DataAccessException {
        StringWriter b = new StringWriter();
        generate(filter, b, rs);
        return b.toString();
    }

    /**
     * Generates the CSV, for all values of the DAO that match given filter.
     * When the filter uses an inner join, *only* the columns in the DAO are
     * returned.
     * 
     * The csv format is as following: each value is added in an escaped string
     * (with escaped " and \). Null values are represented by empty strings.
     * There is no difference between empty string and null (will be treated as
     * null)
     * 
     */
    public void generate(SelectFilter filter, Writer w) throws IOException,
            DataAccessException {
        generate(filter, w, getDefRS(filter));
    }

    /**
     * Generates the CSV, for all values of the DAO that match given filter.
     * When the filter uses an inner join, *only* the columns in the DAO are
     * returned.
     * 
     * The csv format is as following: each value is added in an escaped string
     * (with escaped " and \). Null values are represented by empty strings.
     * There is no difference between empty string and null (will be treated as
     * null)
     * 
     */

    public void generate(SelectFilter filter, Writer w, RelationStatus rs)
            throws IOException, DataAccessException {

        w.append(generateHeader()).append("\n");
        
        rs.setCurrentEntry(0);

        String query = null;
        try (PreparedStatement ps = filter.prepStatement(con)) {
            query = ps.toString(); // query is used for debug reasons

            ResultSet set = ps.executeQuery();
            while (set.next()) {
                w.append(generateLine(set)).append("\n");
                rs.setCurrentEntry(rs.getCurrentEntry() + 1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load the csv of "
                    + dao.getTableName() + " with query " + query, e);
        }
    }

    /**
     * Creates a stringbuilder with the current line of the resultset. Does not
     * call resultset.next()
     * 
     * @throws SQLException
     *             when the resultset feels like throwing one
     */
    public StringBuilder generateLine(ResultSet set) throws SQLException {
        StringBuilder builder = new StringBuilder();

        Field[] columns = dao.getFields();
        for (Field f : columns) {
            if (!f.getTypeEnum().equals(Types.OID)) {
                String colname = f.getNameColumn();
                builder.append(escape(set.getString(colname)));
                builder.append(',');
            }
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder;
    }

    /**
     * Generates header for the CSV; e.g. userid, name, lastname, ...
     */
    public String generateHeader() {
        StringBuilder builder = new StringBuilder();
        Field[] columns = dao.getFields();
        for (Field f : columns) {
            if (!f.getTypeEnum().equals(Types.OID)) {
                builder.append(f.getNameColumn());
                builder.append(',');
            }
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }
    

    protected RelationStatus getDefRS(SelectFilter f) throws DataAccessException {
        RelationStatus rs = new RelationStatus(dao.getTableName(),
                dao.executeCount(f));
        rs.addListener(new CSVListener(dao.getTableName()));
        return rs;
    }

}
