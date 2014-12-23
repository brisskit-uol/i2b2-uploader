/* reinitialization sql for a project with id "project1" 
   NB: Will delete everything from PM where domain_id = 'BRISSKIT' */

drop schema project1data cascade ;
drop user project1data ;
drop schema project1meta cascade ;
drop user project1meta ;
drop schema project1im cascade ;
drop user project1im ;
drop schema project1work cascade ;
drop user project1work ;

set schema 'i2b2metadata';
drop table project1 ;

set schema 'i2b2hive';
delete from i2b2hive.crc_db_lookup where c_project_path = '/project1/' ;
delete from i2b2hive.ONT_DB_LOOKUP where c_project_path = 'project1/' ;
delete from i2b2hive.WORK_DB_LOOKUP where c_project_path = 'project1/' ;
delete from i2b2hive.IM_DB_LOOKUP where c_project_path = 'project1/' ;

set schema 'i2b2pm';
delete from i2b2pm.PM_HIVE_DATA where domain_id = 'BRISSKIT' ;
DELETE FROM i2b2pm.PM_PROJECT_DATA where PROJECT_ID = 'project1' ;
delete from i2b2pm.PM_PROJECT_USER_ROLES where PROJECT_ID = 'project1' ;

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