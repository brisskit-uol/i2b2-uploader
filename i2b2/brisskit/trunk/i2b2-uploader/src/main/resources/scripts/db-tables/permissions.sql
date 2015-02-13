/*===========================================================
  Postgres SQL for granting privileges on a project's schema
  
  Substitutions required for:
  	<DB_SCHEMA_NAME>
	<DB_USER_NAME>
	<DB_PASSWORD>
  ===========================================================*/ 

GRANT ALL PRIVILEGES ON SCHEMA <DB_SCHEMA_NAME> to <DB_SCHEMA_NAME> ;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA <DB_SCHEMA_NAME> TO <DB_SCHEMA_NAME> ;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA <DB_SCHEMA_NAME> TO <DB_SCHEMA_NAME> ;