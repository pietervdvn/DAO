package database.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;
import database.JDBCDataAccessProvider;
import database.internal.dao.DAO;
import database.internal.dao.SimpleDAO;
import database.internal.jdbc.JDBCPictureDAO;
import database.tools.logging.Logger;
import database.tools.logging.Out;
import database.tools.utils.ImageLoader;
import database.tools.utils.MVCDefaultListeners.CSVListener;

/**
 * This class contains java-driven code to nuke and reinit the DB with test
 * data, so that - the scripts don't have to be restarted by hand - that *all*
 * are triggered - are all triggered in the right order - Each object is loaded
 * into the DAO's, to test the automatic rules
 * 
 * USAGE: resetAndTest("/path/to/git/Vopro/Databank", dataAccessContext);
 * 
 * @author pietervdvn
 * 
 */
public class Init {

    public final static int VERSION = 5;

    /**
     * Relative to passed location
     */
    public static String TESTDATA_LOCATION = "Testdata/";
    private final static Out out = Logger.out;
    private final static Out err = Logger.err;

    /**
     * @param args
     * @throws IOException
     * @throws DataAccessException
     */
    public static void main(String[] args) throws DataAccessException,
            IOException {

        if (args.length == 0 || args[0].equals("-h")
                || args[0].equals("--help")) {
            printHelp();
            return;
        }

        Properties p = new Properties();
        String path = args[1];

        // no properties given, default load
        if (args.length == 2) {

        }

        // when path is given for properties, load from there
        if (args.length == 3) {
            try {
                p.load(new BufferedInputStream(new FileInputStream(new File(
                        args[2]))));
            } catch (FileNotFoundException e) {
                err.println("File not found");
            } catch (Exception e) {
                out.println("Could not load file");
            }
        }

        // manually set properties
        if (args.length == 5) {
            p.setProperty("user", args[2]);
            p.setProperty("password", args[3]);
            p.setProperty("url", args[4]);
        }
        DataAccessProvider dap = new JDBCDataAccessProvider(p);
        DataAccessContext dac = dap.getDataAccessContext();

        if (isArg(args[0], 'c')) {
            printScriptName("DATABANK CLEAR " + VERSION);
            clear(path, dac);
        } else if (isArg(args[0], 't')) {
            printScriptName("DATABANK TESTDATA TEST " + VERSION);
            test(dac);
        } else if (isArg(args[0], 'd')) {
            printScriptName("DATABANK TESTDATA CSV DOWNLOADER " + VERSION);
            download(path, dac);
        } else if (isArg(args[0], 'r')) {
            printScriptName("DATABANK RESET WITH CSV " + VERSION);
            try {
                resetCSV(path, dac);

            } catch (FileNotFoundException e) {
                out.println("Hi! It seems like you want to reset the DB, but the meaning of reset has changed in this version. We now load from .csv-files.\n"
                        + "You can use 'sqlreset' to load the old way, or get the .csv-files from the DB-branch");
            }
        } else {
            printHelp();
        }
        try{
        dac.commit();
        }catch(Exception e){
            Logger.err.println("Failed to commit!");
        }
        dac.close();
        out.println("This was Init.java v" + VERSION);
    }

    private static boolean isArg(String s, char start) {
        return s.toLowerCase().charAt(0) == start;
    }

    public static void clear(String path, DataAccessContext dac)
            throws FileNotFoundException, DataAccessException {
        dac.reset();
        out.println("DB is reset!");
    }

    public static void download(String target, DataAccessContext dac)
            throws IOException, DataAccessException {
        System.out.println(dac.getAllDAOs());
        for (SimpleDAO<?> dao : dac.getAllDAOs()) {
            String name = dao.getTableName();
            name = name.replace("\"", "");
            if (!dao.isEmpty()) {
                try {
                    dao.getCSV().saveTo(
                            new File(target + "/Testdata/" + name + ".csv"));

                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }

        for (SimpleDAO<?> dao : dac.getAllDAOs()) {
            if (dao instanceof JDBCPictureDAO<?, ?>) {
                JDBCPictureDAO<?, ?> fdao = (JDBCPictureDAO<?, ?>) dao;
                new ImageLoader<>(fdao).saveImages(new File(target
                        + "/Testdata"));
            }

        }

    }

    /**
     * Resets the db, using csv's. The target path should be the path too the
     * databank in the git repo, e.g. "/home/pietervdvn/Vopro/Databank" without
     * trailing / Or a map containing "Testdata/data.csv"
     */
    public static void resetCSV(String target, DataAccessContext dac)
            throws DataAccessException, FileNotFoundException {
        dac.reset();
        for (SimpleDAO<?> dao : dac.getAllDAOs()) {
            String name = dao.getTableName();
            name = name.replace("\"", "");
            try {
                dao.getCSV().load(
                        new File(target + "/Testdata/" + name + ".csv"), null,
                        true, new CSVListener(name));
                if (dao instanceof JDBCPictureDAO<?, ?>) {
                    ImageLoader<?, ?> il = new ImageLoader<>(
                            (JDBCPictureDAO<?, ?>) dao);
                    il.loadImages(new File(target + "/Testdata/"));
                }

            } catch (Exception e) {
                err.println();
                err.println(" >>>>> ERROR <<<<<");
                err.println();
                err.println(" >>> Could not load " + name
                        + ".csv, no values will be loaded");
                err.println(" >>> " + e.getMessage());
                err.println();
                e.printStackTrace();
            }
        }

    }

    /**
     * test all constraints! Loads *each* record into the java DAO, to see if
     * constraints are broken.
     * 
     * @throws DataAccessException
     */
    public static void test(DataAccessContext dac) {

        for (SimpleDAO<?> dao : dac.getAllDAOs()) {
            try {
                tic();
                if (dao instanceof DAO) {
                    ((DAO<?, ?>) dao).getAll();
                }
                out.println(dao.getTableName()
                        + " is good (no failed test data)");
            } catch (Exception e) {
                err.println("Assumption failed on " + dao.getTableName() + ": "
                        + e.getMessage());
            }
            toc(dao.getTableName() + ": testing");
        }

        out.println("\nAll tests are done!");
    }

    /*** LITTLE HELPING FUNCTIONS ***/

    private static void printTilde(int i) {
        for (int j = 0; j < i; j++) {
            out.print("~");
        }
        out.println();
    }

    private static long start;

    private static void printScriptName(String scriptName) {
        printTilde(scriptName.length() + 6);
        out.println(" ~ " + scriptName + " ~ ");
        printTilde(scriptName.length() + 6);
        out.println();
    }

    private static void printHelp() {
        printScriptName("DATABANK TEST DATA LOADER " + VERSION);

        int i = Logger.libVersion;
        out.println("DAO version: " + i);

        out.println("This little program resets the databank by dropping all tables,\nrestoring them and loading them with test data.");
        out.println("Usage: java -jar Dao.jar <reset|test|clear|download|sqlreset> \"path to git/Vopro/Databank\" \"path to your properties\"");
        out.println("Usage: java -jar Dao.jar <reset|test|clear|download|sqlreset> \"path to git/Vopro/Databank\" \"username\" \"password\" \"url\"");

        out.println("Notice: all the needed libs are packed into this .jar, there is thus no classpath needed");
        out.println("Properties must be of the format:");
        out.println("user : <user name in db>\npassword : <the password for that user name>\nurl: <url to db>");
        out.println();
        out.println("Clear: drop all tables and reinitiate them (with no data)");
        out.println("Reset: drop all tables, reinitiate them and load all data from the given path / data.csv");
        out.println("Test: only test the data from the DB (no data is modified)");
        out.println("Download: download all data as .csv from the DB to the given path (no data is modified)");
        out.println("SQLreset: drop all tables, reinitiate them and reload all data from the given path/data.sql by executing those scripts");
        out.println("\nNB: giving only the first char is good enough for an argument");
    }

    private static void tic() {
        start = System.currentTimeMillis();
    }

    private static void toc(String msg) {
        long stop = System.currentTimeMillis();
        int millis = (int) (stop - start);
        out.println(msg + " took " + (millis / 1000) + "." + (millis % 1000)
                + " sec");
    }

}
