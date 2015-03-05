package database.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import database.internal.Field;
import database.internal.TableName;
import database.internal.dao.SimpleDAO;
import database.internal.type.Types;

/**
 * Generates all kind of queries, based on tables and related enums
 * 
 * @author pietervdvn
 * 
 */
public class SQLGenerator {

    private final TableName table;

    private final Field[] fields;

    public SQLGenerator(TableName table) {
        this.table = table;
        this.fields = table.getFields();
    }

    public SQLGenerator(TableName table, Field[] fields) {
        this.table = table;
        this.fields = fields;
    }
    
    public SQLGenerator(SimpleDAO<?> dao){
        this.table = dao.getTableNameEnum();
        this.fields = dao.getFields();
    }

    /**
     * Generates a UPDATE for this table. The arguments are in field order (thus
     * value of Field.values)
     * 
     * Assumes that there is only one id-field. ORDER OF INSERTION IN STATEMENT
     * IS getUpdateFieldOrder !!!
     * 
     * OID-fields are always ignored!
     */
    public String getUpdateQuery() {
        if (table.getIdFields().length != 1) {
            throw new IllegalArgumentException(
                    "Could not generate UPDATE for table without/more then one id field");
        }

        // remove id-field and OID-fields
        List<Field> fields = new ArrayList<>(Arrays.asList(table.getFields()));
        fields.remove(table.getIdFields()[0]);
        for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext();) {
            if (iterator.next().getTypeEnum() == Types.OID) {
                iterator.remove();
            }
        }
        // back to array
        Field[] fieldsArray = new Field[fields.size()];
        fields.toArray(fieldsArray);

        // delegate!
        return getUpdateFieldQuery(fieldsArray);
    }

    /**
     * Creates an updatequery of the form
     * 
     * UPDATE 'tablename' SET field1 = ?, field2 = ? WHERE idfield1 AND idfield2
     * 
     * use getUpdateFieldOrder to get the exact order (future proof)
     * 
     * @param fields
     * @return
     */
    @SuppressWarnings("unchecked")
    public <F extends Field> String getUpdateFieldQuery(F... fields) {
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ");
        builder.append(table.getName());
        builder.append(" SET ");
        for (F f : fields) {
            if (table.getIdFieldSet().contains(f)) {
                throw new IllegalArgumentException(
                        "The fields you try to update with 'getUpdateFieldQuery' should never be an idfield");
            }
            builder.append(f.getNameColumn());
            builder.append(" = ?,");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(" WHERE ");
        for (int i = 0; i < table.getIdFields().length - 1; i++) {
            builder.append(table.getIdFields()[i].getNameColumn());
            builder.append(" = ? AND ");
        }
        builder.append(table.getIdFields()[table.getIdFields().length - 1]
                .getNameColumn());
        builder.append(" = ?");

        return builder.toString();

    }

    @SuppressWarnings("unchecked")
    public <F extends Field> List<F> getUpdateFieldOrder(F[] fieldsArray) {
        List<F> fields = new ArrayList<>(Arrays.asList(fieldsArray));

        for (Iterator<F> iterator = fields.iterator(); iterator.hasNext();) {
            Field current = iterator.next();
            if (current.getTypeEnum() == Types.OID
                    || table.getIdFieldSet().contains(current)) {
                iterator.remove();
            }
        }

        for (Field f : table.getIdFields()) {
            fields.add((F) f);
        }
        return fields;

    }

    /**
     * Generates a INSERT for this table. The arguments are in field order (thus
     * value of Field.values)
     * 
     * @param nrOfValues
     *            : how many values the insert should have, e.g. nrOfValues == 2
     *            => INSERT INTO 'tablename' VALUES (?,?,?) (?,?,?)
     * @param excludeIdFields
     *            If excludeIDfields is set, idfields will not be in the query
     *            (and thus autogeneratable)
     * 
     * @return
     */
    public String getInsertQuery(int nrOfValues, boolean excludeIdFields) {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ");

        builder.append(table.toString());

        builder.append("(");
        List<Field> usedFields = new ArrayList<>(Arrays.asList(table
                .getFields()));
        if (excludeIdFields) {
            usedFields.removeAll(table.getIdFieldSet());
        }

        for (Iterator<Field> iterator = usedFields.iterator(); iterator
                .hasNext();) {
            if (iterator.next().getTypeEnum() == Types.OID) {
                iterator.remove();
            }
        }

        for (Field field : usedFields) {
            builder.append(field.getNameColumn());
            builder.append(',');
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(") VALUES ");

        for (int j = 0; j < nrOfValues; j++) {
            builder.append("(?");
            for (int i = 1; i < usedFields.size(); i++) {
                builder.append(",?");
            }
            builder.append("),");
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    /**
     * Generates a INSERT for this table. The arguments are in field order (thus
     * value of Field.values)
     * 
     * If excludeIDfields is set, idfields will not be in the query (and thus
     * autogeneratable)
     */
    public String getInsertQuery(boolean excludeIdFields) {
        return getInsertQuery(1, excludeIdFields);
    }

    public String getCreateStatement() {
        StringBuilder builder = new StringBuilder();

        builder.append("CREATE TABLE ");
        builder.append(table.toString());
        builder.append("(");

        for (Field f : fields) {
            builder.append(f.getNameColumn());
            builder.append(" ");
            if (table.getIdFields()[0] == f && f.getReference() == null) {
                builder.append("SERIAL");
            } else {
                builder.append(f.getType());
            }

            if (f.getReference() != null) {
                builder.append(" REFERENCES ");
                builder.append(f.getReference().toString());
                builder.append(" ON DELETE CASCADE");
            }

            builder.append(",");
        }

        builder.append("PRIMARY KEY (");
        Field[] ids = table.getIdFields();
        builder.append(ids[0].getNameColumn());

        for (int i = 1; i < ids.length; i++) {
            builder.append(",");
            builder.append(ids[i].getNameColumn());
        }

        builder.append(")");

        builder.append(")");

        return builder.toString();
    }

}
