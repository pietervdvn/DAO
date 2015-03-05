package database.tools.filter;

import static database.internal.type.TypeUtils.checkBool;
import static database.internal.type.TypeUtils.checkDate;
import static database.internal.type.TypeUtils.checkFloat;
import static database.internal.type.TypeUtils.checkInteger;
import static database.internal.type.TypeUtils.checkString;
import static database.internal.type.TypeUtils.getDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import database.internal.Field;
import database.internal.dao.SimpleDAO;

/**
 * The part where the "where" clause gets implemented
 * 
 * @author pietervdvn
 * 
 */
abstract class AbstractFilter3Where extends AbstractFilter2InSub {

    /**
     * A list of all WHERE clauses
     */
    protected List<String> query = new ArrayList<>();

    public AbstractFilter3Where(SimpleDAO<?> dao) {
        super(dao);
    }

    public AbstractFilter3Where(SimpleDAO<?> dao, String error) {
        super(dao, error);
    }

    protected String getWhereSQL() {
        return getWhereSQL(false);
    }

    /**
     * Gets the actual WHERE sql. If no conditions, empty string is returned
     * 
     * @return
     */
    protected String getWhereSQL(boolean invert) {
        String c = getConditionSQL(invert);
        String sub = getWhereInSubfilterSQL();
        
        String r = "";
        if(!c.isEmpty() && !sub.isEmpty()){
           r = c + " AND "+sub;
        }else{
            //at most one is not the empty string
            r = c + sub;
        }
        
        if(!r.isEmpty()){
            r = "WHERE "+r;
        }
        
        return r;
    }

    /**
     * Returns the conditions on fields, without where or AND on the start. If
     * no conditions, the empty string is returned. If invert is true, all the
     * conditions are NOTted
     */
    private String getConditionSQL(boolean invert) {
        String r = "";
        if (query.size() > 0) {
            if (invert) {
                r += "NOT(";
            }
            for (int i = 0; i < query.size() - 1; i++) {
                r += query.get(i) + " AND ";
            }
            r += query.get(query.size() - 1) + " ";
            if (invert) {
                r += ")";
            }
        }
        return r;
    }

    /**
     * Adds a WHERE clause of the form "(f operator x1 OR f operator x2 OR f
     * operator x3)".
     */
    protected void fieldsOrDate(Field f, List<Date> x, String operator) {
        checkDate(f);
        // convert dates to sql dates
        List<Object> list = new ArrayList<>();
        for (Date d : x) {
            list.add(getDate(f, d));
        }
        fieldsOrObject(f, list, operator);
    }

    /**
     * Adds a WHERE clause of the form "(f operator x1 OR f operator x2 OR f
     * operator x3)".
     * 
     */
    protected void fieldsOrObject(Field f, List<Object> x, String operator) {
        checkFieldInTable(f);
        if (x == null || x.size() < 1) {
            throw new IllegalArgumentException(
                    "The list must contain least one object.");
        }
        String q = "( ";
        for (int i = 0; i < x.size() - 1; i++) {
            q += f + " " + operator + " ? OR ";
            values.add(x.get(i));
        }
        q += f + " " + operator + " ? ) ";
        values.add(x.get(x.size() - 1));
        query.add(q);
    }

    /* ******** ADD STUFF ******** */
    
    public static enum WeekOfDay {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    }
    
    protected void addWeekdayOperator(Field f, String operator, WeekOfDay w) {
        checkDate(f);
        checkFieldInTable(f);
        // extract (dow from expiration_date)
        query.add("extract (dow from "+f + ") " + operator + " ?");
        values.add(w.ordinal());
        
    }
    
    /**
     * Only use if you now what you are doing!
     * 
     * Manually add a piece of sql to the where-clause
     * @param sql
     * @param values
     */
    @Deprecated
    public void manualAdd(String sql, String... values){
        query.add(sql);
        for (String string : values) {
            this.values.add(string);
        }
    }
    
    /**
     * Adds a WHERE clause of the form "f operator x".
     */
    protected void add(Field f, String operator, String x) {
        checkString(f);
        checkFieldInTable(f);
        query.add(f + " " + operator + " ?");
        values.add(x);
    }
    

    /**
     * Adds a WHERE clause of the form "f operator x".
     */
    protected void add(Field f, String operator, int x) {
        checkInteger(f);
        checkFieldInTable(f);
        query.add(f + " " + operator + " ?");
        values.add(x);
    }

    /**
     * Adds a WHERE clause of the form "f operator x".
     */
    protected void add(Field f, String operator, float x) {
        checkFloat(f);
        checkFieldInTable(f);
        query.add(f + " " + operator + " ?");
        values.add(x);
    }

    /**
     * Adds a WHERE clause of the form "f operator x".
     */
    protected void add(Field f, boolean x) {
        checkBool(f);
        checkFieldInTable(f);
        query.add(f + " = ?");
        values.add(x);
    }

    /**
     * Adds a WHERE clause of the form "f operator x".
     */
    protected void add(Field f, String operator, Date x) {
        checkDate(f);
        checkFieldInTable(f);
        query.add(f + " " + operator + " ?");
        values.add(getDate(f, x));
    }

}
