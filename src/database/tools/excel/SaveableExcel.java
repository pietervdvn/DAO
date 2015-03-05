package database.tools.excel;

import java.io.File;
import java.io.IOException;

import database.DataAccessException;

/**
 * Simple interface for excelsheets that contain all needed info
 * @author pietervdvn
 *
 */
public interface SaveableExcel {
    
    void saveTo(File f) throws IOException, DataAccessException;

}
