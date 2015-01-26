/* reinitialization sql for a project with id "searchable_text" 
   NB: Will delete everything from PM where domain_id = 'BRISSKIT' */

drop schema searchable_text cascade ;
drop user searchable_text ;

set schema 'i2b2metadata';
drop table searchable_text ;

set schema 'i2b2hive';
delete from i2b2hive.crc_db_lookup where c_project_path = '/searchable_text/' ;
delete from i2b2hive.ONT_DB_LOOKUP where c_project_path = 'searchable_text/' ;
delete from i2b2hive.WORK_DB_LOOKUP where c_project_path = 'searchable_text/' ;
delete from i2b2hive.IM_DB_LOOKUP where c_project_path = 'searchable_text/' ;

set schema 'i2b2pm';
delete from i2b2pm.PM_HIVE_DATA where domain_id = 'BRISSKIT' ;
DELETE FROM i2b2pm.PM_PROJECT_DATA where PROJECT_ID = 'searchable_text' ;
delete from i2b2pm.PM_PROJECT_USER_ROLES where PROJECT_ID = 'searchable_text' ;




/* reinitialization sql for a project with id "laheart" 
   NB: Will delete everything from PM where domain_id = 'BRISSKIT' */

drop schema laheart cascade ;
drop user laheart ;


set schema 'i2b2metadata';
drop table laheart ;

set schema 'i2b2hive';
delete from i2b2hive.crc_db_lookup where c_project_path = '/laheart/' ;
delete from i2b2hive.ONT_DB_LOOKUP where c_project_path = 'laheart/' ;
delete from i2b2hive.WORK_DB_LOOKUP where c_project_path = 'laheart/' ;
delete from i2b2hive.IM_DB_LOOKUP where c_project_path = 'laheart/' ;

set schema 'i2b2pm';
delete from i2b2pm.PM_HIVE_DATA where domain_id = 'BRISSKIT' ;
DELETE FROM i2b2pm.PM_PROJECT_DATA where PROJECT_ID = 'laheart' ;
delete from i2b2pm.PM_PROJECT_USER_ROLES where PROJECT_ID = 'laheart' ;


/* reinitialization sql for a project with id "gpcut" 
   NB: Will delete everything from PM where domain_id = 'BRISSKIT' */

drop schema gpcut cascade ;
drop user gpcut ;


set schema 'i2b2metadata';
drop table gpcut ;

set schema 'i2b2hive';
delete from i2b2hive.crc_db_lookup where c_project_path = '/gpcut/' ;
delete from i2b2hive.ONT_DB_LOOKUP where c_project_path = 'gpcut/' ;
delete from i2b2hive.WORK_DB_LOOKUP where c_project_path = 'gpcut/' ;
delete from i2b2hive.IM_DB_LOOKUP where c_project_path = 'gpcut/' ;

set schema 'i2b2pm';
delete from i2b2pm.PM_HIVE_DATA where domain_id = 'BRISSKIT' ;
DELETE FROM i2b2pm.PM_PROJECT_DATA where PROJECT_ID = 'gpcut' ;
delete from i2b2pm.PM_PROJECT_USER_ROLES where PROJECT_ID = 'gpcut' ;