/*=======================================================
  Postgres SQL for creating a project's schema and user 
  
  Substitutions required for:
  	<DB_SCHEMA_NAME>
	<DB_USER_NAME>
	<DB_PASSWORD>
  ========================================================*/ 

create schema <DB_SCHEMA_NAME> ;
create user <DB_USER_NAME> password '<DB_PASSWORD>' ;
set schema '<DB_SCHEMA_NAME>' ;  