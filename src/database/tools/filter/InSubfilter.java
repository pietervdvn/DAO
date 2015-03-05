package database.tools.filter;

import java.util.List;

import database.internal.Field;

/**
 * Model class for a subfilter in AbstractFilter.
 * @author Sander
 */
public class InSubfilter {

    private SelectFilter subfilter;
    private Field fieldOfSubfilter;
    private Field fieldIn;
    private boolean reversed;

    protected InSubfilter(SelectFilter subfilter, Field fieldOfSubfilter, Field fieldIn, boolean reversed) {
        this.subfilter = subfilter;
        this.fieldOfSubfilter = fieldOfSubfilter;
        this.fieldIn = fieldIn;
        this.reversed = reversed;
    }

    public List<Object> getValues() {
        return subfilter.getValues();
    }

    public String getFilterSQL() {
        return fieldIn + " " + (reversed ? "NOT IN" : "IN") + " ( " + subfilter.getSQL(fieldOfSubfilter) + " ) ";
    }
}
