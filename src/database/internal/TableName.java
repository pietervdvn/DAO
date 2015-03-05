package database.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import database.fields.UserField;

/**
 * Rerpresents all tables
 * @author Sander
 */
public enum TableName {

    /*
     * WARNING: due to java's incompetence, a table should be UNDER the tables
     * it references
     */
    USER(
            "\"user\"",
            "Alle gebruikers",
            "Een gebruiker in het systeem, inclusief admins, huurders en eigenaars. Een voorstelling van een persoon, een identiteit. Een mogelijkheid.",
            UserField.ID) 
    ;

    private String name;
    private Field[] idField;
    private Field[] fields;
    private Set<TableName> refs;
    private final Set<Field> idFieldSet;
    
    /**
     * Comments about this table, to automatically generate docs
     */
    private final String comments;
    private final String detailedComments;

    private TableName(String name, String comments, Field... idField) {
        this(name, comments, null, idField);
    }

    private TableName(String name, String comments, String detailedComments,
            Field... idField) {
        this.comments = comments;
        this.detailedComments = detailedComments;
        this.name = name;
        this.idField = idField;
        this.idFieldSet = new HashSet<>(Arrays.asList(idField));
        Enum<?> enumClass = (Enum<?>) idField[0];
        Field[] fields = (Field[]) enumClass.getDeclaringClass()
                .getEnumConstants();
        this.fields = fields;
        for (Field field : fields) {
            if (field == null) {
                throw new NullPointerException("Some idfield is null?!");
            }
        }
    }

    private TableName(String name, Field... idField) {
        this(name, null, idField);
    }

    /**
     * Returns the tableName.
     * 
     * @return
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns all fields of the table.
     * 
     * @return
     */
    public Field[] getFields() {
        return fields;
    }

    /**
     * Returns a list of (at least one) ID-field Special classes, e.g. owner
     * will return multiple fields
     * 
     * @return
     */
    public Field[] getIdFields() {
        return idField;
    }

    /**
     * Gives/calculates a set of all the (direct) references of this table
     * 
     * @return
     */
    public Set<TableName> getReferences() {
        if (refs == null) {
            refs = new HashSet<>();

            for (Field f : fields) {
                if (f.getReference() != null) {
                    refs.add(f.getReference());
                }
            }
        }
        return refs;
    }

    public String getDoc() {
        return comments;
    }

    public String getDetailedDoc() {
        return detailedComments;
    }
    
    public String getName() {
        return name;
    }
    
    public Set<Field> getIdFieldSet() {
        return idFieldSet;
    }

}
