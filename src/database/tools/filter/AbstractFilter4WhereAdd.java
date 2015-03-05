package database.tools.filter;

import static database.internal.type.TypeUtils.checkFloat;
import static database.internal.type.TypeUtils.checkInteger;
import static database.internal.type.TypeUtils.checkString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import database.internal.Field;
import database.internal.dao.SimpleDAO;
import database.internal.type.TypeUtils;

/**
 * Lots and lots of little "add" methods... A class of boilerplate code!
 * 
 * @author pietervdvn
 * 
 */
abstract class AbstractFilter4WhereAdd extends AbstractFilter3Where {

    public AbstractFilter4WhereAdd(SimpleDAO<?> dao) {
        super(dao);
    }

    public AbstractFilter4WhereAdd(SimpleDAO<?> dao, String error) {
        super(dao, error);
    }

    /**
     * Returns records where the given fields falls between d1 (including) and
     * d2 (excluding)
     * 
     * @param f
     * @param d
     */
    public void fieldsBetween(Field f, Date d1, Date d2) {
        if (d1.after(d2)) {
            throw new IllegalArgumentException(
                    "In fieldsBetween, the first value should be less then the second ("
                            + d1 + " > " + d2 + ")");
        }
        fieldGreaterThan(f, d1);
        fieldLessThan(f, d2);
    }

    /**
     * Returns record of which the period f1 -> f2 overlaps with d1 -> d2 Note:
     * d2 is not included, thus f1 == d2 -> not included
     */
    public void fieldsOverlap(Field f1, Field f2, Date d1, Date d2) {
        TypeUtils.checkDate(f1);
        TypeUtils.checkDate(f2);
        if (d1.after(d2)) {
            throw new IllegalArgumentException(
                    "In fieldsInRange, the first value should be less then the second ("
                            + d1 + " > " + d2 + ")");
        }

        // ........... d1 -------------- d2
        // ...... f1 --------- f2
        // ........................... f1 ------- f2
        // ................... f1 - f2
        // ..f1-f2
        // ...........................................f1-f2
        // thus: (d1 <= f2 || f1 < d2) && f1 < d2 && d1 <= f2
        checkFieldInTable(f1);
        checkFieldInTable(f2);

        fieldLessThan(f1, d2);
        fieldGreaterThanOrEqual(f2, d1);
    }

    public void fieldWeekdayEquals(Field f, WeekOfDay day){
        addWeekdayOperator(f, "=", day);
    }

    /**
     * Adds a WHERE clause of the form "(f = x1 OR f = x2 OR f = x3)".
     */
    public void fieldsEqualsOrString(Field f, List<String> x) {
        checkString(f);
        fieldsOrObject(f, new ArrayList<Object>(x), "=");
    }

    /**
     * Adds a WHERE clause of the form "(f = x1 OR f = x2 OR f = x3)".
     */
    public void fieldsEqualsOrInteger(Field f, List<Integer> x) {
        checkInteger(f);
        fieldsOrObject(f, new ArrayList<Object>(x), "=");
    }

    /**
     * Adds a WHERE clause of the form "(f = x1 OR f = x2 OR f = x3)".
     */
    public void fieldsEqualsOrFloat(Field f, List<Float> x) {
        checkFloat(f);
        fieldsOrObject(f, new ArrayList<Object>(x), "=");
    }

    /**
     * Adds a WHERE clause of the form "(f = x1 OR f = x2 OR f = x3)".
     */
    public void fieldsEqualsOrDate(Field f, List<Date> x) {
        fieldsOrDate(f, x, "=");
    }

    /**
     * Adds a WHERE clause of the form "f != x".
     */
    public void fieldNotEquals(Field f, String x) {
        add(f, "!=", x);
    }

    /**
     * Adds a WHERE clause of the form "f != x".
     */
    public void fieldNotEquals(Field f, int x) {
        add(f, "!=", x);
    }

    /**
     * Adds a WHERE clause of the form "f != x".
     */
    public void fieldNotEquals(Field f, float x) {
        add(f, "!=", x);
    }

    /**
     * Adds a WHERE clause of the form "f != x".
     */
    public void fieldNotEquals(Field f, Date x) {
        add(f, "!=", x);
    }

    /**
     * Adds a WHERE clause of the form "f = x".
     */
    public void fieldEquals(Field f, String x) {
        add(f, "=", x);
    }

    /**
     * Adds a WHERE clause of the form "f = x".
     */
    public void fieldEquals(Field f, int x) {
        add(f, "=", x);
    }

    /**
     * Adds a WHERE clause of the form "f = x".
     */
    public void fieldEquals(Field f, float x) {
        add(f, "=", x);
    }

    /**
     * Adds a WHERE clause of the form "f = x".
     */
    public void fieldEquals(Field f, Date x) {
        add(f, "=", x);
    }

    /**
     * Adds a WHERE clause of the form "f = x".
     */
    public void fieldEquals(Field f, boolean x) {
        add(f, x);
    }

    /**
     * Adds a WHERE clause of the form "f <= x".
     */
    public void fieldLessThanOrEqual(Field f, int x) {
        add(f, "<=", x);
    }

    /**
     * Adds a WHERE clause of the form "f <= x".
     */
    public void fieldLessThanOrEqual(Field f, float x) {
        add(f, "<=", x);
    }

    /**
     * Adds a WHERE clause of the form "f <= x".
     */
    public void fieldLessThanOrEqual(Field f, Date x) {
        add(f, "<=", x);
    }

    /**
     * Adds a WHERE clause of the form "f < x".
     */
    public void fieldLessThan(Field f, int x) {
        add(f, "<", x);
    }

    /**
     * Adds a WHERE clause of the form "f < x".
     */
    public void fieldLessThan(Field f, float x) {
        add(f, "<", x);
    }

    /**
     * Adds a WHERE clause of the form "f < x".
     */
    public void fieldLessThan(Field f, Date x) {
        add(f, "<", x);
    }

    /**
     * Adds a WHERE clause of the form "f >= x".
     */
    public void fieldGreaterThanOrEqual(Field f, int x) {
        add(f, ">=", x);
    }

    /**
     * Adds a WHERE clause of the form "f >= x".
     */
    public void fieldGreaterThanOrEqual(Field f, float x) {
        add(f, ">=", x);
    }

    /**
     * Adds a WHERE clause of the form "f >= x".
     */
    public void fieldGreaterThanOrEqual(Field f, Date x) {
        add(f, ">=", x);
    }

    /**
     * Adds a WHERE clause of the form "f > x".
     */
    public void fieldGreaterThan(Field f, int x) {
        add(f, ">", x);
    }

    /**
     * Adds a WHERE clause of the form "f > x".
     */
    public void fieldGreaterThan(Field f, float x) {
        add(f, ">", x);
    }

    /**
     * Adds a WHERE clause of the form "f > x".
     */
    public void fieldGreaterThan(Field f, Date x) {
        add(f, ">", x);
    }

    /**
     * Adds a WHERE clause of the form "f ILIKE %s%".
     */
    public void fieldContains(Field f, String s) {
        add(f, "ILIKE ", "%" + s + "%");
    }

    /**
     * Adds a WHERE clause of the form "f ILIKE s%".
     */
    public void fieldStartsWith(Field f, String s) {
        add(f, "ILIKE ", s + "%");
    }

    /**
     * Adds a WHERE clause of the form "f ILIKE %s".
     */
    public void fieldEndsWith(Field f, String s) {
        add(f, "ILIKE ", "%" + s);
    }

    /**
     * Adds a WHERE clause of the form "f = TRUE".
     */
    public void fieldIsTrue(Field f) {
        add(f, true);
    }

    /**
     * Adds a WHERE clause of the form "f = FALSE".
     */
    public void fieldIsFalse(Field f) {
        add(f, false);
    }
    
    public void fieldIsNull(Field f){
        query.add(f+" IS NULL");
    }

    
    
}
