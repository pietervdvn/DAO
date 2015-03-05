package database.tools.logging;

public class Logger {

    public static final int libVersion = 0; // up one when exporting
    
    public final static boolean printOut = true;
    public final static Out out = printOut ? new PrintOut(System.out, "DAO " + libVersion) : new SlashDevSlashNull();
    public final static Out err = new PrintOut(System.err, "DAO " + libVersion);

    static {
        out.println("DAO.jar version is " + libVersion);
        out.println("Hello! Welcome to the clean DAO.jar");
    }
   
}
