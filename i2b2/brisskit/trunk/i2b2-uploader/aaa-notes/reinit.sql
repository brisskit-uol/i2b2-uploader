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


/* generalized reinitialization sql for a project with id "gpcut" 
   NB: Will delete everything from PM where domain_id = 'BRISSKIT' */

drop schema gpcut cascade ;
drop user gpcut ;

set schema 'i2b2hive';
delete from i2b2hive.crc_db_lookup where c_project_path = '/gpcut/' ;
delete from i2b2hive.ONT_DB_LOOKUP where c_project_path = 'gpcut/' ;
delete from i2b2hive.WORK_DB_LOOKUP where c_project_path = 'gpcut/' ;
delete from i2b2hive.IM_DB_LOOKUP where c_project_path = 'gpcut/' ;

set schema 'i2b2pm';
DELETE FROM i2b2pm.PM_PROJECT_DATA where PROJECT_ID = 'gpcut' ;
delete from i2b2pm.PM_PROJECT_USER_ROLES where PROJECT_ID = 'gpcut' ;

=============================================================================


INSERT INTO person (person_id, name)
SELECT 1, 'Me'
WHERE NOT EXISTS (SELECT 1 FROM person WHERE person_id = 1);

INSERT INTO i2b2pm.PM_HIVE_DATA(domain_id, helpurl, domain_name, environment_cd, active, change_date, entry_date, changeby_char, status_cd)
(
   SELECT 'BRISSKIT', 'www.brisskit.le.ac.uk', 'BRISSKIT', 'DEVELOPMENT', 1, now(), now(), '', 'A' 
   WHERE NOT EXISTS( SELECT * FROM I2B2PM.PM_HIVE_DATA WHERE DOMAIN_ID = 'BRISSKIT' ) 
) ;














