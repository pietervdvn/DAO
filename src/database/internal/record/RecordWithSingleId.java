package database.internal.record;


/**
 * @author Pieter
 */
public abstract class RecordWithSingleId implements Record{

    protected int id;


    public RecordWithSingleId(int id) {
        this.id = id;
    }

    public RecordWithSingleId() {

    }


    @Override
    public void checkNoId(String msg) {
        if (getId() >= 0) {
            throw new IllegalArgumentException("Expected "+getClass().getName()+" without id: "+msg);
        }
    }

    @Override
    public void checkId(String msg) {
        if (getId() < 0) {
            throw new IllegalArgumentException("Expected "+getClass().getName()+" with id: "+msg);
        }
    }
    
    @Override
    public void checkId() {
    	checkId("");
    }
   
    @Override
    public void checkNoId() {
    	checkNoId("");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void invalidateId() {
        setId(0);
    }

    public boolean hasValidId() {
        return getId() > 0;
    }

    @Override
    public int hashCode() {
        return getId();
    }

    /**
     * test if equals op basis van een id
     * 
     * @param id
     * @param obj
     * @return
     */
    public boolean IDequals(Object obj) {
        if (!(obj instanceof RecordWithSingleId)) {
            return false;
        }
        RecordWithSingleId record = (RecordWithSingleId) obj;
        if (getId() == 0 || record.getId() == 0) {
            throw new IllegalArgumentException("One or more records has no id.");
        }
        if (getId() != record.getId()) {
            return false;
        }
        return true;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return (this.getClass().equals(obj.getClass())) && IDequals(obj);
    }



}
