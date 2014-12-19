set schema 'i2b2hive';

INSERT INTO i2b2hive.CRC_DB_LOOKUP (C_DOMAIN_ID, C_PROJECT_PATH, C_OWNER_ID, C_DB_FULLSCHEMA, C_DB_DATASOURCE, C_DB_SERVERTYPE, C_DB_NICENAME, C_DB_TOOLTIP, C_COMMENT, C_ENTRY_DATE, C_CHANGE_DATE, C_STATUS_CD)
VALUES ('BRISSKIT', '/<SCHEMA_NAME>/', '@', '<SCHEMA_NAME>', 'java:/<QTProject1DS>', 'POSTGRESQL', '<SCHEMA_NAME>', null, null, now(), now(), null);

INSERT INTO i2b2hive.ONT_DB_LOOKUP (C_DOMAIN_ID, C_PROJECT_PATH, C_OWNER_ID, C_DB_FULLSCHEMA, C_DB_DATASOURCE, C_DB_SERVERTYPE, C_DB_NICENAME, C_DB_TOOLTIP, C_COMMENT, C_ENTRY_DATE, C_CHANGE_DATE, C_STATUS_CD)
VALUES ('BRISSKIT', '<SCHEMA_NAME>/', '@', '<SCHEMA_NAME>', 'java:/<OProject1DS>', 'POSTGRESQL', '<SCHEMA_NAME>', null, null, now(), now(), null);

INSERT INTO i2b2hive.WORK_DB_LOOKUP (C_DOMAIN_ID, C_PROJECT_PATH, C_OWNER_ID, C_DB_FULLSCHEMA, C_DB_DATASOURCE, C_DB_SERVERTYPE, C_DB_NICENAME, C_DB_TOOLTIP, C_COMMENT, C_ENTRY_DATE, C_CHANGE_DATE, C_STATUS_CD)
VALUES ('BRISSKIT', '<SCHEMA_NAME>/', '@', '<SCHEMA_NAME>', 'java:/<WProject1DS>', 'POSTGRESQL', '<SCHEMA_NAME>', null, null, now(), now(), null);

INSERT INTO i2b2hive.IM_DB_LOOKUP (C_DOMAIN_ID, C_PROJECT_PATH, C_OWNER_ID, C_DB_FULLSCHEMA, C_DB_DATASOURCE, C_DB_SERVERTYPE, C_DB_NICENAME, C_DB_TOOLTIP, C_COMMENT, C_ENTRY_DATE, C_CHANGE_DATE, C_STATUS_CD)
VALUES ('BRISSKIT', '<SCHEMA_NAME>/', '@', '<SCHEMA_NAME>', 'java:/<IMProject1DS>', 'POSTGRESQL', '<SCHEMA_NAME>', null, null, now(), now(), null);
