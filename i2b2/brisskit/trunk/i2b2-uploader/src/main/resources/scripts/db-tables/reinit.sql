drop schema <SCHEMA_NAME> cascade ;
drop user <SCHEMA_NAME> ;

-- ===========================================================
set schema 'i2b2hive';
-- ===========================================================

delete from i2b2hive.CRC_DB_LOOKUP where C_DB_FULLSCHEMA = '<SCHEMA_NAME>' ;
delete from i2b2hive.crc_db_lookup where c_project_path = '/<SCHEMA_NAME>/' ;
delete from i2b2hive.ONT_DB_LOOKUP where C_DB_FULLSCHEMA = '<SCHEMA_NAME>' ;
delete from i2b2hive.WORK_DB_LOOKUP where C_DB_FULLSCHEMA = '<SCHEMA_NAME>' ;
delete from i2b2hive.IM_DB_LOOKUP where C_DB_FULLSCHEMA = '<SCHEMA_NAME>' ;

-- ===========================================================
set schema 'i2b2pm';
-- ===========================================================
delete from i2b2pm.PM_HIVE_DATA where domain_id = 'BRISSKIT' ;
DELETE FROM i2b2pm.PM_PROJECT_DATA where PROJECT_ID = '<SCHEMA_NAME>' ;
delete from i2b2pm.PM_PROJECT_USER_ROLES where PROJECT_ID = '<SCHEMA_NAME>' ;