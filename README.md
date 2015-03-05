# DAO
DAO.jar - a fancy database abstraction system

It was developed during the Vopro of team Juwel, and cleaned for re-use by Pietervdvn.

Terms of usage: patches wich are usefull for everyone should be sent back.


# Used libs

- Postgres DB as backend
- Postgres JDBC: [https://jdbc.postgresql.org/]
- Ostermiller utils for the CSV Parser: [http://ostermiller.org/utils/download.html]
- Apache POI (for excelsheets): [https://poi.apache.org/download.html]


# A quick overview

- DataAccessContext/Provider: used to get a connection to the db and to get the DAO's
- Internal: all the superclasses you'll need
- tools: all kind of usefull things
- records: your own records
- fields: your own field names (as enums)
- dao: your interfaces
	- dao.internal: the implementation of these interfaces

## Internal

- Tablename.java: one enum for each table you have
- Record: superclasses of records. The record is a normal object you get from a DAO
- dao: superinterfaces of objects representing dao's
- jdbc: actual implementation of these dao's
- Type: types the postgres-db knows about, and utils to convert those to java types
- dao: your own daos
- dao.internal: the implementation of these dao's


## Tools

- Init.java : executable program to nuke and reload
- Documentator: makes a github-md overview of the relations
- SQL-generator: creates your basic queries. Use filters instead
- filter: build sql-queries with type-safety
- Utils: resetting the db, loading from zip
	- Utils.Utils is used by this class
- csv: parsing from and to csv
- excel: make excelsheets. See voprojuwel for examples
- logging: classes for easy logging to file. Give a logging connection to the DataAccessContext (instead of a postgresql one) and you have logs
