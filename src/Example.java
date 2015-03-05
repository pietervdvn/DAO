import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import database.DataAccessContext;
import database.DataAccessException;
import database.DataAccessProvider;
import database.JDBCDataAccessProvider;
import database.dao.UserDAO;
import database.records.User;

public class Example {

	public static void main(String[] args) throws DataAccessException,
			FileNotFoundException, IOException {
		
		// create provider
		DataAccessProvider provider = new JDBCDataAccessProvider(
				"../dbconfig.properties");
		// make actual connection
		DataAccessContext context = provider.getDataAccessContext();
		
		// builds the actual databasestructure. Don't do this in production!r
		context.reset();
		
		// gets the user dao
		UserDAO users = context.getUserDAO();
		users.add(new User("Pieter", "Vander Vennet"));
		File f = new File("/home/pietervdvn/DB.zip");
		f.createNewFile();
		context.saveToZip(new FileOutputStream(f));
	}

}
