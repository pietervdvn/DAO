package database.tools.csv;

import static database.tools.csv.CSVUtils.convertData;
import static database.tools.csv.CSVUtils.parseCSV;
import static database.tools.csv.CSVUtils.parseHeader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import database.DataAccessException;
import database.internal.Field;
import database.internal.TableName;
import database.internal.jdbc.JDBCSimpleDAO;
import database.internal.type.TypeUtils;
import database.tools.filter.AbstractFilter;
import database.tools.logging.PreparedStatement;
import database.tools.utils.MVCUtils.Listener;
import database.tools.utils.MVCUtils.RelationStatus;

/**
 * Reading the CSV
 * @author pietervdvn
 *
 */
class CSVRead extends CSVSave{
    
    public CSVRead(JDBCSimpleDAO<?> dao) {
        super(dao);
    }

    /**
     * Loads a CSV-string into the DB. First line is expected to be the header.
     * When overwrite is true, this method crashes when a duplicate ID exists!
     * When false, the ID-field is ignored and all values are appended.
     */
    @SuppressWarnings("unchecked")
    public int load(String csv, Field idField, boolean overwrite,
            Listener<RelationStatus>... listeners) throws DataAccessException {

        String[][] parsed = parseCSV(csv);
        if(parsed == null){
            System.out.println("Parsed is null");
        }
        Field[] header = parseHeader(dao.getFields(), parsed[0],
                dao.getTableName());
        List<Map<Field, String>> data = new ArrayList<>();
        for (int i = 1; i < parsed.length; i++) {
            if (parsed[i] != null) {
                try {
                    data.add(convertData(header, parsed[i]));
                } catch (Exception e) {
                    throw new DataAccessException("Error in row " + i
                            + " with data " + Arrays.asList(parsed[i]), e);
                }
            }
        }

        RelationStatus rs = new RelationStatus(dao.getTableName(), data.size());

        for (Listener<RelationStatus> l : listeners) {
            rs.addListener(l);
        }

        if (overwrite) {
            return loadOverwrite(header, data, rs);
        } else {
            Field[] newHeader = new Field[header.length - 1];
            int j = 0;
            for (int i = 0; i < header.length; i++) {
                if (header[i] != idField) {
                    newHeader[j++] = header[i];
                }
            }
            return loadOverwrite(newHeader, data, rs);
        }
    }

    /**
     * Loads all data in chunks
     * 
     * @param header
     * @param datas
     * @throws DataAccessException
     */
    private int loadOverwrite(Field[] header, List<Map<Field, String>> datas,
            RelationStatus rs) throws DataAccessException {
        int chunkSize = 1024;
        rs.factor = chunkSize;

        int loaded = 0;

        while (datas.size() > chunkSize) {
            loaded += loadOverwriteFullChunk(header,
                    datas.subList(0, chunkSize));
            datas = datas.subList(chunkSize, datas.size());
            rs.setCurrentEntry(loaded);
        }
        if (datas.size() > 0) {
            loaded += loadOverwriteFullChunk(header, datas);
            rs.setCurrentEntry(loaded);
        }

        // update the internal counters of postgres, that auto-gen keys do work

        // SELECT setval('user_userid_seq', (SELECT MAX(userid) FROM user) + 1)

        try {
            updateSeq(dao.getTableNameEnum());
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Data are added to the db, but the counters did nog get updated.",
                    e);
        }

        return loaded;
    }

    /**
     * Updates the internal counter
     * 
     * @param table
     * @throws SQLException
     */
    private void updateSeq(TableName table) throws SQLException {

        List<Field> idfields = Arrays.asList(table.getIdFields());
        idfields.removeAll(table.getReferences());

        if (idfields.size() == 0) {
            // no counters to be updated
            return;
        }
        if (idfields.size() == 1) {
            Field id = idfields.get(0);
            String tn = table.toString().replace("\"", "");
            String idn = id.getNameColumn();
            PreparedStatement ps = con.prepareStatement("SELECT setval('" + tn
                    + "_" + idn + "_seq', (SELECT MAX(" + id.getNameColumn()
                    + ") FROM " + table.toString() + ") +1)");
            ps.execute();
        }

    }

    /**
     * Tries to insert including the primary key; will crash when a primary key
     * already is in the DB
     * 
     * @param header
     * @param datas
     * @throws DataAccessException
     */
    private int loadOverwriteFullChunk(Field[] header,
            List<Map<Field, String>> datas) throws DataAccessException {
        AbstractFilter filter = genInsertFilter(header, datas.size());
        try (PreparedStatement ps = filter.prepStatement(con)) {
            int i = 1; // keeps track of the current argument
            for (Map<Field, String> data : datas) {
                for (Field f : header) {
                    TypeUtils.setArg(ps, i++, data.get(f), f.getTypeEnum());
                }
            }
            ps.execute();
            return datas.size();
        } catch (SQLException e) {
            throw new DataAccessException("Could not insert values into "
                    + dao.getTableName(), e);
        }

    }

    private AbstractFilter genInsertFilter(final Field[] fields, final int nr) {
        return new AbstractFilter(dao) {
            @Override
            public String getSQL() {
                return genInsert(fields, nr);
            }
        };
    }

    private String genInsert(Field[] fields, int nrOfValues) {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ");

        builder.append(dao.getTableName());

        builder.append("(");
        for (Field field : fields) {
            builder.append(field.getNameColumn());
            builder.append(',');
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(") VALUES ");

        for (int j = 0; j < nrOfValues; j++) {
            builder.append("(?");
            for (int i = 1; i < fields.length; i++) {
                builder.append(",?");
            }
            builder.append("),");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

}
