package database.tools.excel;

import static database.tools.excel.ExcelSheetUtils.asList;
import static database.tools.excel.ExcelSheetUtils.autoSize;
import static database.tools.excel.ExcelSheetUtils.calculateCodeForCol;
import static database.tools.excel.ExcelSheetUtils.createBoldStyle;
import static database.tools.excel.ExcelSheetUtils.createDateCellStyle;
import static database.tools.excel.ExcelSheetUtils.setValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import database.DataAccessException;
import database.JDBCDataAccessContext;
import database.internal.Field;
import database.internal.type.TypeUtils;
import database.tools.filter.SelectFilter;
import database.tools.logging.PreparedStatement;

/**
 * Represents a single worksheet within the excel file. (A single excel file
 * exists out of multiple sheets).
 * 
 * 
 * @author pietervdvn
 */
public class ExcelSheet {

    private final SelectFilter filter;
    private final String name;
    private final Field[] neededFields;
    private final String[] headerNames;
    private final Field[] calcTotal;
    private List<ExcelFormula> formulae = new ArrayList<>();
    private Map<Field, CellStyle> customFormat = new HashMap<>();

    /**
     * Creates a new excelsheet (thus a tab within a file). Fields of type OID
     * are not allowed and will not be included into the excel.
     * 
     * @param Name
     *            : the name for this excelsheettab
     * 
     * @param Filter
     *            : what information should get into the excelsheet
     * 
     * @param NeededFields
     *            : the fields that have to be included out of the given filter.
     * 
     * @param Headernames
     *            : (optional) for each field, what name it should have. When
     *            nothing given, the default field column name will be used
     * 
     * @param calcTotal
     *            : (optional) Calculates a total for this field (and appends
     *            this to the end of the sheet)
     */
    public ExcelSheet(String name, SelectFilter filter, Field[] neededFields,
            String[] headerNames, Field... calcTotal) {
        this.filter = filter;
        this.name = name;
        this.neededFields = TypeUtils.removeOID(neededFields);
        this.headerNames = headerNames;
        this.calcTotal = calcTotal;
        if (this.neededFields.length != this.headerNames.length) {
            throw new IllegalArgumentException(
                    "There should be an equal number of neededFields and headernames.");
        }

    }

    /**
     * Creates a new excelsheet (thus a tab within a file) Fields of type OID
     * are not allowed and will not be included into the excel.
     * 
     * 
     * @param Name
     *            : the name for this excelsheettab
     * 
     * @param Filter
     *            : what information should get into the excelsheet
     * 
     * @param NeededFields
     *            : the fields that have to be included out of the given filter.
     * 
     * @param Headernames
     *            : (optional) for each field, what name it should have. When
     *            nothing given, the default field column name will be used
     * 
     */
    public ExcelSheet(String name, SelectFilter filter, Field[] neededFields,
            String... names) {
        this(name, filter, neededFields, names, (Field[]) null);
    }

    /**
     * Creates a new excelsheet (thus a tab within a file). Fields of type OID
     * are not allowed and will not be included into the excel.
     * 
     * @param Name
     *            : the name for this excelsheettab
     * 
     * @param Filter
     *            : what information should get into the excelsheet
     * 
     * @param NeededFields
     *            : the fields that have to be included out of the given filter.
     * 
     * @param calcTotal
     *            : (optional) Calculates a total for this field (and appends
     *            this to the end of the sheet)
     */
    public ExcelSheet(String name, SelectFilter filter, Field[] neededFields,
            Field... calcTotal) {
        this(name, filter, neededFields, asList(neededFields), calcTotal);
    }

    /**
     * Creates a new excelsheet (thus a tab within a file). Fields of type OID
     * are not allowed and will not be included into the excel.
     * 
     * @param Name
     *            : the name for this excelsheettab
     * 
     * @param Filter
     *            : what information should get into the excelsheet
     * 
     * @param NeededFields
     *            : the fields that have to be included out of the given filter.
     * 
     */
    public ExcelSheet(String name, SelectFilter filter, Field... neededFields) {
        this(name, filter, neededFields, asList(neededFields));
    }

    public void addFormula(ExcelFormula formula) {
        formulae.add(formula);
    }

    public void addSheetTo(JDBCDataAccessContext dac, Workbook wb)
            throws DataAccessException {

        Sheet sheet = wb.createSheet(name);
        int rowNumber = 0;
        Row header = sheet.createRow(rowNumber++);

        // header
        for (int i = 0; i < headerNames.length; i++) {
            header.createCell(i).setCellValue(headerNames[i]);
        }

        int cellNr = headerNames.length;
        for (ExcelFormula formula : formulae) {
            header.createCell(cellNr++).setCellValue(formula.getName());
        }

        // calculate sums, we put these on top so users don't have to scroll all
        // the way down
        Row sums = null;
        if (calcTotal != null) {
            sums = sheet.createRow(rowNumber++);
        }

        CellStyle dateStyle = createDateCellStyle(wb, "dd/mm/yyyy");
        CellStyle timestampStyle = createDateCellStyle(wb, "dd/mm/yyyy hh:mm");

        // load all data
        cellNr = 0;
        try (PreparedStatement ps = filter.prepFullStatement(dac
                .getConnection())) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Row r = sheet.createRow(rowNumber++);
                cellNr = 0;
                for (Field f : neededFields) {
                    Cell cell = r.createCell(cellNr++);
                    setValue(rs, f, cell, dateStyle, timestampStyle, filter);
                    if (customFormat.get(f) != null) {
                        cell.setCellStyle(customFormat.get(f));
                    }
                }

                // add formula columns
                for (ExcelFormula formula : formulae) {
                    Cell cell = r.createCell(cellNr++);
                    try {

                        cell.setCellFormula(formula.getFormula(this, rowNumber));
                        // no minus one, as rowNumber is zero based and formulae
                        // are
                        // one based. There has been a plusplus alreade
                        if (formula.getCustomFormat() != null) {
                            cell.setCellStyle(formula.getCustomFormat());
                        }
                    } catch (Exception e) {
                        throw new DataAccessException("Could not add formula", e);
                    }

                }
            }

        } catch (SQLException e) {
            throw filter.prepException(e);
        }
        CellStyle bold = createBoldStyle(wb);
        if (calcTotal != null) {
            { // add a bold total cell to the far right
                Cell c = sums.createCell(cellNr);
                c.setCellValue("Total");
                c.setCellStyle(bold);
            }

            Set<Field> toCalcClost = new HashSet<>(Arrays.asList(calcTotal));
            for (int i = 0; i < neededFields.length; i++) {
                Field f = neededFields[i];
                if (toCalcClost.contains(f)) {
                    Cell c = sums.createCell(i);
                    String a = calculateCodeForCol(i);
                    c.setCellFormula("SUM(" + a + "3:" + a + rowNumber + ")");
                    c.setCellStyle(bold);
                }
            }

            // add formula columns totals
            for (int i = 0; i < formulae.size(); i++) {
                if (formulae.get(i).doCalculateTotal()) {
                    int c = i + neededFields.length;
                    Cell cell = sums.createCell(c);
                    String a = calculateCodeForCol(c);
                    cell.setCellFormula("SUM(" + a + "3:" + a + rowNumber + ")");
                    cell.setCellStyle(bold);
                }
            }
        }
        autoSize(sheet);
    }

    public String calculateCodeFor(Field f) {
        for (int i = 0; i < neededFields.length; i++) {
            if (f.equals(neededFields[i])) {
                return calculateCodeForCol(i);
            }
        }
        throw new IllegalArgumentException("The field " + f
                + " is not found in the current excelsheet");
    }

    public String getReferenceTo(Field f, int row) {
        return getReferenceTo(f) + row;
    }

    public String getReferenceTo(Field f) {
        return getName() + "!" + calculateCodeFor(f);
    }

    public String getReferenceTo(String formula, int row) {
        return getReferenceTo(formula) + row;
    }

    public String getReferenceTo(String formula) {
        int findex = -1;
        for (int i = 0; i < formulae.size(); i++) {
            if (formulae.get(i).getName().equals(formula)) {
                findex = i;
            }
        }
        if (findex == -1) {
            throw new IllegalArgumentException("Formula not found " + formula
                    + " in sheet " + getName());
        }

        return getName()
                + "!"
                + ExcelSheetUtils.calculateCodeForCol(neededFields.length
                        + findex);
    }

    public SelectFilter getFilter() {
        return filter;
    }

    public String getName() {
        return name;
    }

    public Field[] getNeededFields() {
        return neededFields;
    }

    public String[] getHeaderNames() {
        return headerNames;
    }

    public Field[] getCalcTotal() {
        return calcTotal;
    }

    public void addCustomFormatFor(Field f, CellStyle c) {
        customFormat.put(f, c);
    }

}
