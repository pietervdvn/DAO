package database.internal.type;

/**
 * Represents the known types in the DB
 * @author pietervdvn
 *
 */
public enum Types {

    BOOL("boolean"), TEXT("text"), INT("integer"), REAL("real"), DATE("date"), TIMESTAMP(
            "timestamp"), CHAR("character"), OID("oid"), CURRENCY("integer");

    private final String name;

    private Types(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
}
