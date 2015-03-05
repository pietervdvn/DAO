package database;

/**
 * Error while connecting to the database
 */
public class DataAccessException extends Exception {

    private static final long serialVersionUID = 3377151394140802531L;

    public DataAccessException(String message, Throwable th) {
        super(message, th);
    }

    @Override
    public String getMessage() {
        if (super.getCause() != null) {
            return super.getMessage() + "\n  > caused by: "
                    + super.getCause().getMessage()+" ("+ getCauseName() + ")";
        }
        return super.getMessage();
    }
    
    private String getCauseName(){
        String cause = super.getCause().getClass().getCanonicalName();
        while(cause.contains(".")){
            cause = cause.substring(cause.indexOf(".")+1);
        }
        return cause;
    }
}
