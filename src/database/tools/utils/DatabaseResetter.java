package database.tools.utils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import database.DataAccessException;
import database.JDBCDataAccessContext;
import database.internal.TableName;
import database.tools.SQLGenerator;
import database.tools.logging.PreparedStatement;
import database.tools.utils.MVCDefaultListeners.ResetListener;
import database.tools.utils.MVCUtils.GlobalStatus;
import database.tools.utils.MVCUtils.RelationStatus;

/**
 * This class resets the database, only using the TableName-enums and
 * RelationField-enums.
 * 
 * when reset is executed, all tables are dropped (if possible), and new
 * relations are created using the enums.
 * 
 * @author pietervdvn
 */
public class DatabaseResetter {

    private final JDBCDataAccessContext dac;

    public DatabaseResetter(JDBCDataAccessContext dac) {
        this.dac = dac;
    }

    public void reset(GlobalStatus gs) throws DataAccessException, SQLException {
        dropTables();

        Queue<TableName> toAdd = new LinkedList<>(Arrays.asList(TableName
                .values()));

        Set<TableName> initiated = new HashSet<>();
        while (!toAdd.isEmpty()) {
            TableName table = toAdd.poll();
            if (initiated.containsAll(table.getReferences())) {
                gs.setCurrentRelation(gs.getCurrentRelation() + 1,
                        new RelationStatus(table.toString(), 1));
                initTable(table);
                initiated.add(table);
            } else {
                // this table has still deps that need to be initiated, we'll do
                // it later
                toAdd.add(table);
            }
        }
    }

    public void reset() throws SQLException, DataAccessException {
        GlobalStatus gs = new GlobalStatus(TableName.values().length);
        gs.addListener(new ResetListener());
        reset(gs);
    }

    public void dropTables() throws SQLException {
        StringBuilder builder = new StringBuilder();
        builder.append("DROP TABLE IF EXISTS ");

        for (TableName table : TableName.values()) {
            builder.append(table.toString());
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        String query = builder.toString();
        PreparedStatement ps = dac.getConnection().prepareStatement(query);
        ps.execute();
    }

    private void initTable(TableName table) throws SQLException {
        String query = new SQLGenerator(table).getCreateStatement();
        dac.getConnection().prepareStatement(query).execute();
    }

}
