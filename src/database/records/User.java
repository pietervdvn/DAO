package database.records;

import database.internal.record.RecordWithSingleId;

/**
 * Represents a user-record.
 */
public class User extends RecordWithSingleId {

    private String name;
    private String lastname;

    public User() {
    }

    public User(String name, String lastname) {
        super(-1);
        this.name = name;
        this.lastname = lastname;
    }

    /**
     * This one is used in createWithRS
     */
    public User(int userID, String name, String lastName) {
        this(name, lastName);
        this.id = userID;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public String toString() {
        return "#User#\nid: " + this.getId() + "\n" + "name: " + this.getName()
                + " " + this.getLastname();
    }

    @Override
    public void checkRecord() {
    	// Do validity checks
      
    }
}
