package database.tools.excel;

import static database.tools.excel.ExcelSheetUtils.saveWorkbookTo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import database.DataAccessContext;
import database.DataAccessException;
import database.JDBCDataAccessContext;

/** Class to create an excelworkbook of multiple sheets */
public class Excelbook {

    protected final JDBCDataAccessContext dac;

    @SuppressWarnings("deprecation")
    public Excelbook(DataAccessContext dac) {
        this.dac = dac.breakAbstraction();
    }

    /**
     * Creates the excel workbook. You'll probably want to use saveTo
     */
    public Workbook createWorkbook(ExcelSheet... sheets)
            throws DataAccessException {
        Workbook wb = new HSSFWorkbook();
        createWorkbook(wb, sheets);
        return wb;
    }

    /**
     * Creates the excel workbook based on another workbook. Usefull to add more
     * excelbooks together
     */
    public void createWorkbook(Workbook wb, ExcelSheet... sheets)
            throws DataAccessException {
        try {
            for (ExcelSheet sheet : sheets) {
                sheet.addSheetTo(dac, wb);
            }
        } catch (Exception e) {
            throw new DataAccessException(
                    "Could not generate excel spreadsheet", e);
        }
    }

    /**
     * Creates the excel workbook based on another workbook. Usefull to add more
     * excelbooks together
     */
    public void createWorkbook(Workbook wb, List<ExcelSheet> sheets)
            throws DataAccessException {
        try {
            for (ExcelSheet sheet : sheets) {
                sheet.addSheetTo(dac, wb);
            }
        } catch (Exception e) {
            throw new DataAccessException(
                    "Could not generate excel spreadsheet", e);
        }
    }

    /**
     * Creates the workbook with createworkbook, and saves it to the given file.
     */
    public void saveTo(File f, Workbook wb, ExcelSheet... sheets)
            throws DataAccessException, IOException {
        if (f == null) {
            throw new NullPointerException(
                    "The file you want to write the excel is null.");
        }
        createWorkbook(wb, sheets);
        saveWorkbookTo(f, wb);
    }

    /**
     * Creates a fresh workbook with all the given sheets and saves to the given
     * file
     */
    public void saveTo(File f, ExcelSheet... sheets)
            throws DataAccessException, IOException {
        saveTo(f, new HSSFWorkbook(), sheets);
    }
}
