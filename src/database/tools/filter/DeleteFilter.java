package database.tools.filter;

import database.internal.Field;
import database.internal.dao.SimpleDAO;

/**
 * Filter for executing DELETE statements.
 * 
 * @author Sander
 */
public class DeleteFilter extends AbstractFilter {
    
    public DeleteFilter(SimpleDAO<?> dao){
        super(dao);
    }

    public <F extends Field> DeleteFilter(SimpleDAO<?> dao, F field, int value){
        super(dao);
        fieldEquals(field, value);
    }
    
    public <F extends Field> DeleteFilter(SimpleDAO<?> dao, F field, int value, String errormsg){
        this(dao, field, value);
        setErrorMessage(errormsg);
    }
    
    public <F extends Field> DeleteFilter(SimpleDAO<?> dao, F field, String value){
        super(dao);
        fieldEquals(field, value);
    }
    
    public <F extends Field> DeleteFilter(SimpleDAO<?> dao, F field, String value, String errormsg){
        this(dao, field, value);
        setErrorMessage(errormsg);
    }
    

    @Override
    public String getSQL() {
        String r = "DELETE ";
        r += "FROM " + tableName + " ";
        if (!joins.isEmpty()) {
            r += "USING ";
            for (int i = 0; i < joins.size() - 1; i++) {
                r += joins.get(i) + ", ";

            }
            r += joins.get(joins.size() - 1) + " ";
        }
        if (!joins.isEmpty()) {
            
            r += joinOn1.get(0) + " = " + joinOn2.get(0) + " ";
            for (int i = 1; i < joinOn1.size(); i++) {
                r += "AND " + joinOn1.get(i) + " = " + joinOn2.get(i) + " ";
            }
        }
        r += getWhereSQL();
        return r;
    }

}
