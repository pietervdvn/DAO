package database.tools.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import database.internal.Field;
import database.internal.TableName;
import database.internal.dao.SimpleDAO;

/**
 * The part of the code that is responsible for all kind of joins, and knows what tables and fiels are in this filter
 * @author pietervdvn
 */
abstract class AbstractFilter1Joins extends AbstractFilter0Data {

    /**
     * A list of all join tables.
     */
    protected List<TableName> joins = new ArrayList<>();
    /**
     * A list that contains for all join tables the first on field.
     */
    protected List<Field> joinOn1 = new ArrayList<>();
    /**
     * A list that contains for all join tables the second on field.
     */
    protected List<Field> joinOn2 = new ArrayList<>();
    /**
     * A list that contains all fields currently in the query.
     */
    protected List<Field> allFields = new ArrayList<>();
    
    public AbstractFilter1Joins(SimpleDAO<?> dao) {
        super(dao.getTableNameEnum());
        allFields.addAll(Arrays.asList(dao.getFields()));
    }

    public AbstractFilter1Joins(SimpleDAO<?> dao, String error) {
        super(dao.getTableNameEnum(), error);
        allFields.addAll(Arrays.asList(dao.getFields()));
    }
    
    public final Field[] getAllFields() {
        Field[] f = new Field[allFields.size()];
        for (int i = 0; i < f.length; i++) {
            f[i]=  allFields.get(i);
        }
        return f;
    }
    
    /**
     * Adds an innerJoin to the SQL statement. It will create something of the
     * form "INNER JOIN table ON on1 = on2 "
     *
     * If you want to inner join on an id, use innerJoin(TableName table), which
     * will figure out what the ID columns are.
     */
    public void innerJoin(TableName table, Field on1, Field on2) {
        // check fields not from same table
        if (on1.getClass().equals(on2.getClass())) {
            throw new IllegalArgumentException(
                    "Fields are from the same table: " + on1.getClass());
        }
        // check fields are from same type
        if (!on1.getType().equals(on2.getType())) {
            throw new IllegalArgumentException(
                    "Fields are not from the same type: " + on1.getNameColumn()
                    + ": " + on1.getType() + ", " + on2.getNameColumn()
                    + ": " + on2.getType());
        }
        joins.add(table);
        allFields.addAll(Arrays.asList(table.getFields()));
        // check if fields are in a table
        checkFieldInTable(on1);
        checkFieldInTable(on2);
        joinOn1.add(on1);
        joinOn2.add(on2);
    }
    
    /**
     * Does an inner join with the given table; will figure out on what key to
     * join.
     *
     * E.g. if the filter is on user, and you call "innerJoin(RIDE)", this
     * method will see that USERID references USER, which is an inner join on
     * USER.ID (fetched via TableName.USER.getIdFields). Assumes the refed field
     * only has *one* primary key (throws illarg if broken).
     *
     * Details: first, the refed tables of the given tables are looked to. The
     * first refed table is used to join on. If no ref tables exist, all fields
     * are looked into. An already in the filter field that references this
     * table, will be used to join on. If nothing is found, an illarg gets
     * thrown
     *
     *
     */
    public void innerJoin(TableName table) {
        Set<TableName> refed = table.getReferences();
        if (refed.size() > 0) {
            if (refed.contains(tableName)) {
                autoJoin(table, tableName);
                return;
            }
            for (TableName tableName : joins) {
                if (refed.contains(tableName)) {
                    autoJoin(table, tableName);
                    return;
                }
            }
        }

        // the new table does not contain a reference to a current table, so
        // we're searching all known fields for backrefs
        for (Field field : allFields) {
            if (field.getReference() == table) {
                innerJoin(table, field, table.getIdFields()[0]);
                return;
            }
        }
        throw new IllegalArgumentException(
                "There is no table that "
                + table.toString()
                + " references in the filter; or there is no table in the current filter that references "
                + table.toString() + "; Current tables are: "
                + tableName + " + " + joins);

    }
    
    /**
     * We do know that newtable refs table. Lets join them!
     */
    private void autoJoin(TableName newTable, TableName table) {
        // we search what field references the old table
        for (Field f : newTable.getFields()) {
            if (f.getReference() == table) {
                // found! time to join
                innerJoin(newTable, f, table.getIdFields()[0]);
                return;
            }
        }
    }
    
    /**
     * Verify that one of the tables contains the field f.
     */
    protected void checkFieldInTable(Field f) {
        boolean found = checkFieldInTable(f, tableName);
        if (!found) {
            for (TableName table : joins) {
                if (checkFieldInTable(f, table)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Field: " + f
                    + " not found in table(s). Current tables are: " + joins);
        }
    }
    
    /**
     * Verify that f is in the table.
     */
    private boolean checkFieldInTable(Field f, TableName table) {
        for (Field field : table.getFields()) {
            if (field.toString().equals(f.toString())) {
                return true;
            }
        }
        return false;
    }
    
    public int indexOf(Field f){
        return allFields.indexOf(f);
    }
    
    /**
     * Returns "FROM table INNER JOIN ... " part of query
     * @return
     */
    protected String getFromClause(){
        String r= " FROM " + tableName + " ";
        if (!joins.isEmpty()) {
            for (int i = 0; i < joins.size(); i++) {
                r += "INNER JOIN " + joins.get(i) + " ON " + joinOn1.get(i)
                        + " = " + joinOn2.get(i) + " ";
            }
        }
        return r;
    }
    
}
