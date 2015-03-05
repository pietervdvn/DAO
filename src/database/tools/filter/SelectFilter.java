package database.tools.filter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.internal.Field;
import database.internal.dao.SimpleDAO;
import database.internal.type.Types;
import database.tools.logging.LoggingConnection;
import database.tools.logging.PreparedStatement;

/**
 * Filter for executing SELECT statements.
 * 
 * @author Sander
 * @param <F>
 */
public class SelectFilter extends AbstractFilter {

    /**
     * When true: count instead of returning entries
     */
    private boolean count = false;

    /**
     * When true: reverse all where clauses where
     */
    private boolean invert = false;

    /**
     * Creates a select filter. The filter will select records of the table
     * tableName.
     */
    public SelectFilter(SimpleDAO<?> dao) {
        super(dao);
    }

    public SelectFilter(SimpleDAO<?> dao, String errorMsg) {
        this(dao);
        setErrorMessage(errorMsg);
    }

    public SelectFilter(SimpleDAO<?> dao, Field field, int value) {
        this(dao);
        fieldEquals(field, value);
    }

    public SelectFilter(SimpleDAO<?> dao, Field field, String value) {
        this(dao);
        fieldEquals(field, value);
    }

    public SelectFilter invert() {
        return setInvert(true);
    }

    public SelectFilter setInvert(boolean invert) {
        this.invert = invert;
        return this;
    }

    public boolean getInvert() {
        return this.invert;
    }

    @Override
    public String getSQL() {
        return getSQL(tableName.getFields());
    }

    public String getFullSQL() {
        return getSQL(getAllFields());
    }

    public PreparedStatement prepFullStatement(LoggingConnection c)
            throws SQLException {
        return prepStatement(c, getFullSQL());
    }

    /**
     * Filter SQL with 1 single field (for InSubfilters).
     * 
     * @param field
     * @return
     */
    protected String getSQL(Field field) {
        checkFieldInTable(field);
        Field[] fields = { field };
        return getSQL(fields);
    }

    
    private String getSQL(Field[] fields) {
        List<Field> f = new ArrayList<>();
        for (Field field : fields) {
            if(field.getTypeEnum() != Types.OID){
                f.add(field);
            }
        }
        return getSQL(f);
    }
        
        
    
    /**
     * Generates the SQL-query, with all the specified fields. When dirtyNames
     * is true, fields will be of the named form user_id (with an _ instead of a
     * .) Used for fullSql
     */
    private String getSQL(List<Field> fields) {
        String r = "SELECT ";

        if (count) {
            r += "COUNT(*) ";
        } else {

            if (!distinct.isEmpty()) {
                r += "DISTINCT ON (";
                for (int i = 0; i < distinct.size() - 1; i++) {
                    r += distinct.get(i) + ", ";
                }
                r += distinct.get(distinct.size() - 1) + ") ";
            }
            
            for (Field field : fields) {
                r += field;
                r += ", ";
            }

            r = r.substring(0, r.length() - 2);
        }

        r += getFromClause();

        r += getWhereSQL(invert);
        
        if (orderBy != null) {
            r += " " + orderBy;
        }
        if (paging != null) {
            r += " " + paging;
        }
        return r;
    }

    public PreparedStatement prepCountStatement(LoggingConnection connection)
            throws SQLException {
        count = true;
        PreparedStatement ps = super.prepStatement(connection, getSQL());
        count = false;
        return ps;
    }
}
