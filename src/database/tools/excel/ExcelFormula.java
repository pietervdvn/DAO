package database.tools.excel;

import org.apache.poi.ss.usermodel.CellStyle;

import database.internal.Field;
import database.internal.TableName;
import database.internal.type.Types;

public class ExcelFormula {

    private final String name;
    // a formula where each ? is replaced by a reference to a field
    private final String formula;
    private final Field[] args;
    private CellStyle customFormat;
    private boolean calculateTotal = true;

    public static final Field ROW_FIELD = new Field() {

        @Override
        public Types getTypeEnum() {
            return Types.INT;
        }

        @Override
        public String getType() {
            return getTypeEnum().getName();
        }

        @Override
        public TableName getReference() {
            return null;
        }

        @Override
        public String getNameColumn() {
            return "row";
        }

        @Override
        public String getDocs() {
            return null;
        }
    };

    public ExcelFormula(String name, String formula, Field... args) {
        this.name = name;
        this.formula = formula + " ";
        this.args = args;
        int l = this.formula.split("\\?").length - 1;
        if (l != args.length) {
            throw new IllegalArgumentException("Incorrect number of args: got "
                    + formula + " and " + args.length + " args (expected " + l
                    + " args)");
        }

    }

    public String getName() {
        return name;
    }

    public String getFormula(ExcelSheet sheet, int row) {
        String[] parts = formula.split("\\?");

        StringBuilder b = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {
            b.append(parts[i]);
            if (args[i] != ROW_FIELD) {
                b.append(sheet.calculateCodeFor(args[i]));
            }
            b.append(row);
        }
        b.append(parts[parts.length - 1]);
        return b.toString();
    }

    public Field[] getArgs() {
        return args;
    }

    public CellStyle getCustomFormat() {
        return customFormat;
    }

    public ExcelFormula setCustomFormat(CellStyle customFormat) {
        this.customFormat = customFormat;
        return this;
    }

    public boolean doCalculateTotal() {
        return calculateTotal;
    }

    public ExcelFormula setCalculateTotal(boolean calculateTotal) {
        this.calculateTotal = calculateTotal;
        return this;
    }

}
