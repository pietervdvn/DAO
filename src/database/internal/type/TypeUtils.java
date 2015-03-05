package database.internal.type;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import database.DataAccessException;
import database.internal.Field;
import database.tools.logging.PreparedStatement;

/**
 * Class that works together with Types to prepare statments
 * 
 * @author pietervdvn
 * 
 */
public class TypeUtils {

    private static final String[] STRINGTYPES = { "text", "character" };
    private static final String[] INTTYPES = { "integer" };
    private static final String[] FLOATTYPES = { "real" };
    private static final String[] BOOLTYPES = { "boolean" };
    private static final String[] DATETYPES = { "date" };
    private static final String[] TIMESTAMP = { "timestamp" };
    private static final String[] ALLDATETYPES;

    static {
        // merge DATETYPES and TIMESTAMP into ALLDATETYPES
        ALLDATETYPES = new String[DATETYPES.length + TIMESTAMP.length];
        System.arraycopy(DATETYPES, 0, ALLDATETYPES, 0, DATETYPES.length);
        System.arraycopy(TIMESTAMP, 0, ALLDATETYPES, DATETYPES.length,
                TIMESTAMP.length);
    }

    private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat timestampformat = new SimpleDateFormat(
            "yyyy-MM-dd hh:mm:ss");

    /**
     * Verify that f is a kind of string.
     */
    public static void checkString(Field f) {
        checkType(f, STRINGTYPES);
    }

    /**
     * Verify that f is a kind of integer.
     */
    public static void checkInteger(Field f) {
        checkType(f, INTTYPES);
    }
    
    public static void checkBoolean(Field f){
        checkType(f, BOOLTYPES);
    }

    /**
     * Verify that f is a kind of float.
     */
    public static void checkFloat(Field f) {
        checkType(f, FLOATTYPES);
    }

    /**
     * Verify that f is a boolean.
     */
    public static void checkBool(Field f) {
        checkType(f, BOOLTYPES);
    }

    /**
     * Verify that f is a kind of date.
     */
    public static void checkDate(Field f) {
        checkType(f, ALLDATETYPES);
    }

    /**
     * Verify that the type of f is in types.
     */
    public static void checkType(Field f, String[] types) {
        if (!Arrays.asList(types).contains(f.getType())) {
            throw new IllegalArgumentException(
                    "Wrong type field: found field of" + " type " + f.getType()
                            + " but expected something of type: "
                            + Arrays.toString(types) + ".");
        }
    }

    public static void checkType(Field f, Types...types){
        if (!Arrays.asList(types).contains(f.getTypeEnum())) {
        throw new IllegalArgumentException(
                "Wrong type field: found field of" + " type " + f.getType()
                        + " but expected something of type: "
                        + Arrays.toString(types) + ".");
    }
    }
    
    /**
     * Converts the date to the right format for the SQL statement.
     */
    public static java.util.Date getDate(Field f, java.util.Date d) {
        if (d == null) {
            return null;
        }
        if (Arrays.asList(DATETYPES).contains(f.getType())) {
            return new java.sql.Date(d.getTime());
        } else {
            return new java.sql.Timestamp(d.getTime());
        }
    }

    public static void setArg(PreparedStatement ps, int i, boolean b, Field f)
            throws SQLException, DataAccessException {
        checkBool(f);
        ps.setBoolean(i, b);
    }

    public static void setArg(PreparedStatement ps, int i, int v, Field f)
            throws SQLException, DataAccessException {
        checkInteger(f);
        ps.setInt(i, v);
    }

    public static void setArg(PreparedStatement ps, int i, double d, Field f)
            throws SQLException, DataAccessException {
        checkFloat(f);
        ps.setDouble(i, d);
    }

    public static void setArg(PreparedStatement ps, int i, java.util.Date d,
            Field f) throws SQLException, DataAccessException {
        if (d == null) {
            ps.setNull(i, 0);
            return;
        }
        switch (f.getTypeEnum()) {
        case DATE:
            ps.setDate(i, new Date(d.getTime()));
            break;
        case TIMESTAMP:
            ps.setTimestamp(i, new Timestamp(d.getTime()));
            break;
        default:
            throw new IllegalArgumentException(
                    "Invalid type for setArg, you provided a date object");
        }
    }

    public static void setArg(PreparedStatement ps, int i, String s, Field f)
            throws SQLException, DataAccessException {
        setArg(ps, i, s, f.getTypeEnum());
    }

    public static void setArg(PreparedStatement ps, int i, String s, Types t)
            throws SQLException, DataAccessException {
        if((s == null || s.length() == 0) && t == Types.TEXT){
            ps.setString(i, "");
            return;
        }
        if (s == null || s.length() == 0) {
            ps.setNull(i, 0);
            return;
        }
        switch (t) {
        case BOOL:
            if (s.startsWith("t")) {
                ps.setBoolean(i, true);
            } else {
                ps.setBoolean(i, Boolean.parseBoolean(s));
            }
            break;
        case TEXT:
        case CHAR:
            ps.setString(i, s);
            break;
        case CURRENCY:
        case INT:
            ps.setInt(i, Integer.parseInt(s));
            break;
        case REAL:
            ps.setDouble(i, Double.parseDouble(s));
            break;
        case DATE:
            try {
                Date d = new Date(format.parse(s).getTime());
                ps.setDate(i, d);
            } catch (ParseException e) {
                throw new DataAccessException("Could not parse date '" + s
                        + "'", e);
            }
            break;
        case TIMESTAMP:
            try {
                Long d = timestampformat.parse(s).getTime();
                ps.setTimestamp(i, new Timestamp(d));
            } catch (ParseException e) {
                throw new DataAccessException("Could not parse timestamp '" + s
                        + "'", e);
            }
            break;
        case OID:
            throw new UnsupportedOperationException(
                    "No setArg for OID, please use JDBCAbstractFigureDAO to do this");
            
        default:
            throw new IllegalArgumentException("Unkown type! Please add "
                    + t.toString() + " in TypeUtils");
        }
    }

    public static Field[] removeAll(Field[] values, Types... toRemove) {
        List<Field> fields = new ArrayList<>(Arrays.asList(values));
        Set<Types> forbidden = new HashSet<>(Arrays.asList(toRemove));
        for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext();) {
            Field f = iterator.next();
            if (forbidden.contains(f.getTypeEnum())) {
                iterator.remove();
            }
        }
        Field[] result = new Field[fields.size()];
        fields.toArray(result);
        return result;
    }

    public static Field[] removeOID(Field[] values) {
        List<Field> fields = new ArrayList<>(Arrays.asList(values));
        for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext();) {
            Field f = iterator.next();
            if (f.getTypeEnum() == Types.OID) {
                iterator.remove();
            }
        }
        Field[] result = new Field[fields.size()];
        fields.toArray(result);
        return result;
    }

    public static int getInt(ResultSet rs, Field f) throws SQLException {
        checkInteger(f);
        return rs.getInt(f.getNameColumn());
    }

    
    public static boolean getBoolean(ResultSet rs, Field f) throws SQLException {
        checkBool(f);
        return rs.getBoolean(f.getNameColumn());
    }
    public static Date getDate(ResultSet rs, Field f) throws SQLException {
        switch (f.getTypeEnum()) {
        case TIMESTAMP:
            Timestamp ts = rs.getTimestamp(f.getNameColumn());
            if(ts == null){
                return null;
            }
            return new Date(ts.getTime());
        case DATE:
            java.sql.Date d = rs.getDate(f.getNameColumn());
               if(d == null){
                   return null;
               }
            return new Date(d.getTime());
        default:
            throw new UnsupportedOperationException("This is not a date type");
        }
    }

    public static String getString(ResultSet rs, Field f) throws SQLException {
        checkString(f);
        String s = rs.getString(f.getNameColumn());
        if(s == null){
            return "";
        }else{
            return s;
        }
    }
    
    public static Float getFloat(ResultSet rs, Field f) throws SQLException{
        checkFloat(f);
        return rs.getFloat(f.getNameColumn());
    }

}
