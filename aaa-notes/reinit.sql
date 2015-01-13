/* reinitialization sql for a project with id "searchable_text" 
   NB: Will delete everything from PM where domain_id = 'BRISSKIT' */

drop schema searchable_textdata cascade ;
drop user searchable_textdata ;
drop schema searchable_textmeta cascade ;
drop user searchable_textmeta ;
drop schema searchable_textim cascade ;
drop user searchable_textim ;
drop schema searchable_textwork cascade ;
drop user searchable_textwork ;

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

drop schema laheartdata cascade ;
drop user laheartdata ;
drop schema laheartmeta cascade ;
drop user laheartmeta ;
drop schema laheartim cascade ;
drop user laheartim ;
drop schema laheartwork cascade ;
drop user laheartwork ;

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