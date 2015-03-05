package database.tools.filter;

import java.util.ArrayList;
import java.util.List;

import database.internal.Field;
import database.internal.dao.SimpleDAO;

abstract class AbstractFilter2InSub extends AbstractFilter1Joins {

    /**
     * A list that contains all SelectFilters for the "IN" or "NOT IN" clauses
     */
    protected List<InSubfilter> inSubfilters = new ArrayList<>();
    
    public AbstractFilter2InSub(SimpleDAO<?> dao) {
        super(dao);
    }

    public AbstractFilter2InSub(SimpleDAO<?> dao, String error) {
        super(dao, error);
    }

    /**
     * Adds a WHERE clause of the form "fieldIn IN (SELECT fieldOfSubfilter FROM
     * subfilter)".
     */
    public void fieldInFilter(SelectFilter subfilter, Field fieldOfSubfilter, Field fieldIn) {
        checkFieldInTable(fieldIn);
        subfilter.checkFieldInTable(fieldOfSubfilter);
        inSubfilters.add(new InSubfilter(subfilter, fieldOfSubfilter, fieldIn, false));
    }
    
    /**
     * Adds a WHERE clause of the form "fieldIn NOT IN (SELECT fieldOfSubfilter
     * FROM subfilter)".
     */
    public void fieldNotInFilter(SelectFilter subfilter, Field fieldOfSubfilter, Field fieldIn) {
        checkFieldInTable(fieldIn);
        subfilter.checkFieldInTable(fieldOfSubfilter);
        inSubfilters.add(new InSubfilter(subfilter, fieldOfSubfilter, fieldIn, true));
    }
    
    /**
     * Returns the WHERE IN subfilter part of the SQL statement.
     * (It is returned as condition (thus without WHERE or AND in the begining)
     * Returns empty string if no subfilters
     */
    protected String getWhereInSubfilterSQL() {
        String r = "";
        if (!inSubfilters.isEmpty()) {
            for (int i = 0; i < inSubfilters. size() - 1; i++) {
                r += inSubfilters.get(0).getFilterSQL() + "AND ";
            }
            r += inSubfilters.get(inSubfilters.size() - 1).getFilterSQL() + " ";
        }
        return r;
    }
    
    /**
     * Returns a list of all the values ​​that must be entered in the SQL
     * statement.
     */
    public List<Object> getValues() {
        List<Object> v = new ArrayList<>(values);
        //Adding the values of the subfilters. It must be in this order. 
        //SubFilters are always at the end of the statement.
        for (InSubfilter isf : inSubfilters) {
            v.addAll(isf.getValues());
        }
        return v;
    }
    
    
}
