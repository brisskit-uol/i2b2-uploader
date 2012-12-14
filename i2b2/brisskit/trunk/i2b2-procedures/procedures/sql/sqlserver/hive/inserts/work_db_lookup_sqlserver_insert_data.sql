INSERT INTO WORK_DB_LOOKUP(c_domain_id, c_project_path, c_owner_id, c_db_fullschema, c_db_datasource, c_db_servertype, c_db_nicename, c_db_tooltip, c_comment, c_entry_date, c_change_date, c_status_cd)
  VALUES( '${domain.id}', '${project.name}/', '@', '${db.project.work.databasename}.dbo', 'java:${work.ds.jndi.name}', 'SQLSERVER', '${project.name}Workplace', NULL, NULL, NULL, NULL, NULL )
