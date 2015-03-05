package database.fields;

import static database.internal.type.Types.INT;
import static database.internal.type.Types.TEXT;
import database.internal.Field;
import database.internal.TableName;
import database.internal.type.Types;

public enum UserField implements Field {

	ID("userid", INT), NAME("name", "voornaam", TEXT), LASTNAME("lastname",
			TEXT), ;

	private final String name;
	private final Types type;
	private final TableName reference;
	private final String docs;

	private UserField(String name, String docs, Types type, TableName ref) {
		this.name = name;
		this.type = type;
		this.reference = ref;
		this.docs = docs;
	}

	private UserField(String name, String doc, Types type) {
		this(name, doc, type, null);
	}

	private UserField(String name, Types t) {
		this(name, null, t, null);
	}

	@Override
	public String getDocs() {
		return docs;
	}

	public TableName getReference() {
		return reference;
	}

	@Override
	public String toString() {
		return TableName.USER + "." + name;
	}

	@Override
	public String getNameColumn() {
		return name;
	}

	@Override
	public String getType() {
		return type.getName();
	}

	@Override
	public Types getTypeEnum() {
		return type;
	}
}
