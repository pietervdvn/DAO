package database.tools.filter;

import java.util.ArrayList;
import java.util.List;

import database.internal.Field;
import database.internal.dao.SimpleDAO;
/**
 * Abstract class Filter.
 *
 * A filter is an abstraction of a SQL statement.
 * 
 * This is the end of the spagghetti inheritacne AbstractFilter0 -> 4.
 * In here are some leftover modifiers.
 *
 * @author Sander
 */
public abstract class AbstractFilter extends AbstractFilter4WhereAdd{
    
    /**
     * A list of all the fields for which the filter must be unique.
     */
    protected List<Field> distinct = new ArrayList<>();
    
    /**
     * Something of the form "PAGING x OFFSET y", with x, y are numbers.
     */
    protected String paging = null;
    /**
     * Something of the form "ORDER BY x", with x a columnName.
     */
    protected String orderBy = null;
    
    /**
     * @param tableName Table for the "FROM" in the SQL statement.
     * @param error: the error message given in the exception when this filter fails
     */
    public AbstractFilter(SimpleDAO<?> dao, String error) {
        super(dao, error);
    }

    /**
     * @param tableName Table for the "FROM" in the SQL statement.
     */
   public AbstractFilter(SimpleDAO<?> dao) {
        super(dao);
    }
    

    /**
     * Adds a field for which the filter must be unique.
     */
    public AbstractFilter distinct(Field f) {
        // check if fields are in a table
        checkFieldInTable(f);
        distinct.add(f);
        return this;
    }

    /**
     * Adds a clause of the form "LIMIT limt OFFSET offset". There will be
     * $limit returned values, from $offset to $(offset+limit). e.g., limit =
     * 10, and offset = 20. This means that 10 values will be returned, namely
     * the ones in the resultset on row 20,21,22,...29
     */
    public AbstractFilter setPaging(int limit, int offset) {
        paging = "LIMIT " + Integer.toString(limit) + " OFFSET "
                + Integer.toString(offset);
        return this;
    }

    /**
     * Sets the order of the returned list of the filter.
     */
    public AbstractFilter setOrder(Field f, boolean ascending) {
        orderBy = "ORDER BY " + f + " " + (ascending ? "ASC" : "DESC");
        return this;
    }

   
}
