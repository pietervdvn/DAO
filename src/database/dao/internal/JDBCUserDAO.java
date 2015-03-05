package database.dao.internal;

import static database.internal.type.TypeUtils.getInt;
import static database.internal.type.TypeUtils.getString;
import static database.internal.type.TypeUtils.setArg;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.DataAccessException;
import database.JDBCDataAccessContext;
import database.dao.UserDAO;
import database.fields.UserField;
import database.internal.TableName;
import database.internal.jdbc.JDBCIdDAO;
import database.records.User;
import database.tools.logging.PreparedStatement;

/**
 * JDBC User DAO
 */
public class JDBCUserDAO extends JDBCIdDAO<User, UserField>
		implements UserDAO {

	public JDBCUserDAO(JDBCDataAccessContext dac) {
		super(dac, TableName.USER, UserField.ID);
	}

	@Override
	public User createWithCurrent(ResultSet rs) throws SQLException {
		if (rs.next()) {
			return new User(getInt(rs, UserField.ID),  getString(rs, UserField.NAME),
					getString(rs, UserField.LASTNAME));
		} else {
			return null;
		}
	}

	@Override
	protected void insertInStatement(UserField f, User item,
			PreparedStatement ps, int i) throws SQLException,
			DataAccessException {
		switch (f) {
		case ID:
			setArg(ps, i, item.getId(), f);
			break;
		case NAME:
			setArg(ps, i, item.getName(), f);
			break;
		case LASTNAME:
			setArg(ps, i, item.getLastname(), f);
			break;
		}
	}

	@Override
	protected void checkConstraints(User item) throws DataAccessException {
		// check against duplicate emails etc
	}

}
