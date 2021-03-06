/*=====================================================
  Postgres SQL for project ontology or metadata tables
  
  	Substitutions required for:
	<PROJECT_ONTOLOGY>
	<PROJECT_METADATA_TABLE>
	
	The bulk inserts are programmatic.
  =====================================================*/ 
  
/*  DDL for Table SCHEMES  */

  CREATE TABLE SCHEMES 
   (	C_KEY VARCHAR(50)	 	NOT NULL,
	    C_NAME VARCHAR(50)		NOT NULL,
	    C_DESCRIPTION VARCHAR(100)	NULL,
	    CONSTRAINT SCHEMES_PK PRIMARY KEY(C_KEY)
   ) 
   ;
 
/*  DDL for Table TABLE_ACCESS  */

  CREATE TABLE TABLE_ACCESS
   (	C_TABLE_CD VARCHAR(50)	NOT NULL, 
	C_TABLE_NAME VARCHAR(50)	NOT NULL, 
	C_PROTECTED_ACCESS CHAR(1)	NULL,
	C_HLEVEL INT				NOT NULL, 
	C_FULLNAME VARCHAR(700)	NOT NULL, 
	C_NAME VARCHAR(2000)		NOT NULL, 
	C_SYNONYM_CD CHAR(1)	NOT NULL, 
	C_VISUALATTRIBUTES CHAR(3)	NOT NULL, 
	C_TOTALNUM INT			NULL, 
	C_BASECODE VARCHAR(200)	NULL, /* changed by Jeff Lusted */
	C_METADATAXML TEXT		NULL, 
	C_FACTTABLECOLUMN VARCHAR(50)	NOT NULL, 
	C_DIMTABLENAME VARCHAR(50)	NOT NULL, 
	C_COLUMNNAME VARCHAR(50)	NOT NULL, 
	C_COLUMNDATATYPE VARCHAR(50)	NOT NULL, 
	C_OPERATOR VARCHAR(10)	NOT NULL, 
	C_DIMCODE VARCHAR(700)	NOT NULL, 
	C_COMMENT TEXT	NULL, 
	C_TOOLTIP VARCHAR(900)	NULL, 
	C_ENTRY_DATE timestamp		NULL, 
	C_CHANGE_DATE timestamp	NULL, 
	C_STATUS_CD CHAR(1)		NULL,
	VALUETYPE_CD VARCHAR(50)	NULL
   ) 
   ;
 
/*  DDL for Table the project's initial and main ontology  */

	CREATE TABLE <PROJECT_METADATA_TABLE>
   (	C_HLEVEL INT			NOT NULL, 
	C_FULLNAME VARCHAR(700)	NOT NULL, 
	C_NAME VARCHAR(2000)		NOT NULL, 
	C_SYNONYM_CD CHAR(1)		NOT NULL, 
	C_VISUALATTRIBUTES CHAR(3)	NOT NULL, 
	C_TOTALNUM INT			NULL, 
	C_BASECODE VARCHAR(200)	NULL, /* changed by Jeff Lusted */
	C_METADATAXML TEXT		NULL, 
	C_FACTTABLECOLUMN VARCHAR(50)	NOT NULL, 
	C_TABLENAME VARCHAR(50)	NOT NULL, 
	C_COLUMNNAME VARCHAR(50)	NOT NULL, 
	C_COLUMNDATATYPE VARCHAR(50)	NOT NULL, 
	C_OPERATOR VARCHAR(10)	NOT NULL, 
	C_DIMCODE VARCHAR(700)	NOT NULL, 
	C_COMMENT TEXT			NULL, 
	C_TOOLTIP VARCHAR(900)	NULL, 
	M_APPLIED_PATH VARCHAR(700)	NOT NULL, 
	UPDATE_DATE timestamp		NOT NULL, 
	DOWNLOAD_DATE timestamp	NULL, 
	IMPORT_DATE timestamp	NULL, 
	SOURCESYSTEM_CD VARCHAR(50)	NULL, 
	VALUETYPE_CD VARCHAR(50)	NULL,
	M_EXCLUSION_CD	VARCHAR(25) NULL,
	C_PATH	VARCHAR(700)   NULL,
	C_SYMBOL	VARCHAR(50)	NULL
   ) ;

   CREATE INDEX META_FULLNAME_IDX_custom ON <PROJECT_METADATA_TABLE>(C_FULLNAME)
;

   CREATE INDEX META_APPLIED_PATH_IDX_custom ON <PROJECT_METADATA_TABLE>(M_APPLIED_PATH)
;

/*  DDL for Table ONT_PROCESS_STATUS  */
	
CREATE TABLE ONT_PROCESS_STATUS (
    PROCESS_ID			Serial PRIMARY KEY, 
    PROCESS_TYPE_CD		VARCHAR(50),
    START_DATE			timestamp, 
    END_DATE			timestamp,
    PROCESS_STEP_CD		VARCHAR(50),
    PROCESS_STATUS_CD   VARCHAR(50),
    CRC_UPLOAD_ID		INT,
    STATUS_CD			VARCHAR(50),
    MESSAGE				TEXT,
    ENTRY_DATE			timestamp,
    CHANGE_DATE			timestamp,
    CHANGEDBY_CHAR		CHAR(50)
);

/*  Basic inserts to allow project ontology to be used  */

INSERT INTO SCHEMES(C_KEY, C_NAME, C_DESCRIPTION)
  VALUES('<PROJECT_ONTOLOGY>:', '<PROJECT_ONTOLOGY>', '<PROJECT_ONTOLOGY>');

INSERT INTO SCHEMES(C_KEY, C_NAME, C_DESCRIPTION)
  VALUES('(null)', 'None', 'No scheme');

INSERT INTO TABLE_ACCESS(C_TABLE_CD, C_TABLE_NAME, C_HLEVEL, C_FULLNAME, C_NAME, C_SYNONYM_CD, C_VISUALATTRIBUTES, C_TOTALNUM, C_BASECODE, C_METADATAXML, C_FACTTABLECOLUMN, C_DIMTABLENAME, C_COLUMNNAME, C_COLUMNDATATYPE, C_OPERATOR, C_DIMCODE, C_COMMENT, C_TOOLTIP, C_ENTRY_DATE, C_CHANGE_DATE, C_STATUS_CD, C_PROTECTED_ACCESS, VALUETYPE_CD)
  VALUES('<PROJECT_ONTOLOGY>', '<PROJECT_METADATA_TABLE>', 0, '\<PROJECT_ONTOLOGY>\', '<PROJECT_ONTOLOGY>', 'N', 'CA', NULL, NULL, NULL, 'concept_cd', 'concept_dimension', 'concept_path', 'T', 'LIKE', '\<PROJECT_ONTOLOGY>\', NULL, '<PROJECT_ONTOLOGY>', NULL, NULL, NULL, 'N', NULL);



