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