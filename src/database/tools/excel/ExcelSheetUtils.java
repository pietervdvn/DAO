package database.tools.excel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import database.internal.Field;
import database.internal.type.TypeUtils;
import database.tools.filter.AbstractFilter;

/**
 * Some helping methods
 * 
 * @author pietervdvn
 * 
 */
class ExcelSheetUtils {

    /**
     * Help function to concat two arrays
     */
    public static Field[] add(Field[] seed, Field... toAdd) {
        Field[] concat = new Field[seed.length + toAdd.length];
        for (int i = 0; i < seed.length; i++) {
            concat[i] = seed[i];
        }

        for (int i = 0; i < toAdd.length; i++) {
            concat[i + seed.length]= toAdd[i];
        }
        return concat;

    }

    public static void saveWorkbookTo(File f, Workbook wb) throws IOException {
        try {
            OutputStream fileOut = new BufferedOutputStream(
                    new FileOutputStream(f));
            wb.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new IOException("Could not write excelsheet to "
                    + f.getName(), e);
        }
    }

    public static void setValue(ResultSet rs, Field f, Cell c,
            CellStyle dateStyle, CellStyle timestamp, AbstractFilter filter)
            throws SQLException {
        setValue(rs, f, rs.findColumn(f.getNameColumn()), c, dateStyle,
                timestamp, filter);
    }

    public static void setValue(ResultSet rs, Field f, int n, Cell c,
            CellStyle dateStyle, CellStyle timestamp, AbstractFilter filter)
            throws SQLException {

        if (rs.getString(n) == null) {
            c.setCellValue("<n/a>");
            return;
        }

        switch (f.getTypeEnum()) {
        case BOOL:
            c.setCellValue(rs.getBoolean(n));
            break;
        case TEXT:
        case CHAR:
            c.setCellValue(rs.getString(n));
            break;
        case CURRENCY:
        case INT:
            c.setCellValue(rs.getInt(n));
            break;
        case REAL:
            c.setCellValue(rs.getDouble(n));
            break;
        case DATE:
            c.setCellValue(rs.getDate(n));
            c.setCellStyle(dateStyle);
            break;
        case TIMESTAMP:
            c.setCellStyle(timestamp);
            c.setCellValue(rs.getDate(n));
            break;
        case OID:
            throw new UnsupportedOperationException(
                    "No excel for OID, please dont do this");
        default:
            throw new IllegalArgumentException("Unkown type! Please add "
                    + f.getType().toString() + " in ExcelSheetUtils");
        }
    }

    // unhash :: Int -> String
    // unhash 0 = []
    // unhash i
    // = let c = i `mod` 28 in
    // chr (96 + c):unhash ((i - c) `div` 28)

    public static String calculateCodeForCol(int i) {
        int limit = 26;
        if (i < limit) {
            return "" + (char) (65 + i % limit);
        }

        int c = i % limit;
        return calculateCodeForCol(i / limit - 1) + (char) (65 + c);
    }

    public static CellStyle createDateCellStyle(Workbook wb, String format) {
        CreationHelper createHelper = wb.getCreationHelper();

        // we style the second cell as a date (and time). It is important to
        // create a new cell style from the workbook otherwise you can end up
        // modifying the built in style and effecting not only this cell but
        // other cells.
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(
                format));
        return cellStyle;
    }

    /**
     * Expects that row 0 has been created
     * 
     * Out of the documention of POI: To calculate column width:
     * Sheet.autoSizeColumn uses Java2D classes that throw exception if
     * graphical environment is not available. In case if graphical environment
     * is not available, you must tell Java that you are running in headless
     * mode and set the following system property: java.awt.headless=true .
     * 
     * @param s
     */
    public static void autoSize(Sheet s) {
        for (int j = 0; j < s.getRow(0).getLastCellNum(); j++) {
            s.autoSizeColumn(j);
        }
    }

    public static CellStyle createBoldStyle(Workbook wb) {
        CellStyle cs = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        cs.setFont(font);
        return cs;
    }

    /**
     * equivalent to f.filter(.getType != OID).map(.getNameColumn())
     */
    public static String[] asList(Field[] f) {
        f = TypeUtils.removeOID(f);
        String[] names = new String[f.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = f[i].getNameColumn();
        }
        return names;
    }

    public static void createHeader(Row r, String... names) {
        createHeader(r, 0, names);
    }

    public static void createHeader(Row r, int start, String... names) {
        for (int i = 0; i < names.length; i++) {
            r.createCell(i + start).setCellValue(names[i]);
        }
    }

    public static void createFormulaRow(Row r, String name, String... formulae) {
        createFormulaRow(r, 0, name, formulae);
    }

    public static void createFormulaRow(Row r, int start, String name,
            String... formulae) {
        r.createCell(start).setCellValue(name);
        for (int i = 0; i < formulae.length; i++) {
            r.createCell(i + start + 1).setCellFormula(formulae[i]);
        }
    }

    public static Field[] l(Field... f) {
        return f;
    }
    
    

}
