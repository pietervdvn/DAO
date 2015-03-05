package database.tools.csv;

import java.util.HashMap;
import java.util.Map;

import com.Ostermiller.util.CSVParser;

import database.internal.Field;

/**
 * Some helper methods to generate CSV-stuff
 * 
 * @author pietervdvn
 * Is package private
 */
class CSVUtils {

    public static Map<Field, String> convertData(Field[] header, String[] data) {
        Map<Field, String> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            try{
            map.put(header[i], data[i]);
            }catch(IndexOutOfBoundsException e){
                throw new IndexOutOfBoundsException("To little data in row, field "+header[i]+" is missing");
            }
        }
        return map;
    }

    /**
     * Returns a matrix of the CSV-values FIRST ROW == HEADER! Some rows might
     * be null.
     * 
     * @param csv
     * @return
     */
    public static String[][] parseCSV(String csv) {
        return CSVParser.parse(csv);
    }


    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        s = s.replace("\\", "\\\\");
        s = s.replace("\"", "\\\"");
        return '"' + s + '"';
    }

    public static Field[] parseHeader(Field[] available, String[] header,
            String tablename) {
        Map<String, Field> fields = new HashMap<>();
        for (Field field : available) {
            fields.put(field.getNameColumn(), field);
        }

        Field[] res = new Field[header.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = fields.get(header[i]);
            if (res[i] == null) {
                throw new IllegalArgumentException("Field " + header[i]
                        + " is invalid for loading in table " + tablename
                        + " it doesn't exists as column");
            }
        }
        return res;
    }

}
