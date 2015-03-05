package database.internal;

import database.internal.type.Types;

/**
 * Represents a field in a relation.
 * @author Sander
 */
public interface Field {

    /**
     * Returns something of the form "tableName.columnName".
     * @return
     */
    String toString();
    
    /**
     * Returns only the columnName, thus without tableName
     * @return
     */
    String getNameColumn();

    /**
     * Returns the type of the column as string. Eg: "integer", "boolean","text","date","oid" ...
     * @return
     */
    String getType();
    
    /**
     * Returns an enum representing the type
     */
    Types getTypeEnum();
    
    /**
     * Returns the table this key references to, null if no reference
     */
    TableName getReference();
    
    
    /**
     * Get's info about this specific field
     */
    String getDocs();
    
}
