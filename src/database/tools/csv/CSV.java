package database.tools.csv;

import database.internal.jdbc.JDBCSimpleDAO;

/**
 * Creates a CSV of the given DAO.
 * 
 * @author pietervdvn
 * 
 */
public class CSV extends CSVLoad{

    public CSV(JDBCSimpleDAO<?> dao) {
        super(dao);
    }

}
