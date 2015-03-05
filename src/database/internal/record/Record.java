package database.internal.record;


/**
 * Represents a record in the database.
 * @author Sander
 */
public interface Record {
       
    /**
     * 
     * Check if the Record is correct and can be added or updated to db
     */
    void checkRecord();
    
    String toString();
    
    
    /**
     * Check if the record has an id
     */
    public void checkId();
    /**
     * Check if the record has no id
     */
    public void checkNoId();
    /**
     * Check if the record has an id
     */
    public void checkId(String msg);
    /**
     * Check if the record has no id
     */
    public void checkNoId(String msg);
    
    public int hashCode();
    boolean equals(Object obj);
    
}
