package org.brisskit.i2b2 ; 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.* ;

public class CreateDBPG extends Base {
	
	/*
	 * Only change the order of the scripts with care.
	 * schema.sql and permissions.sql should top and tail the project specific scripts
	 * (ie: crc, ont, im and wrk)
	 */
	private static final String[] SCRIPT_FILE_NAMES = {
		"schema.sql",
		"crc.sql",
		"ont.sql",
		"im.sql",
		"wrk.sql",
		"permissions.sql",
		"hive.sql",
		"pm.sql"
	} ;
	
	private static final String SET_SCHEMA_CMD = "SET SCHEMA '<DB_SCHEMA_NAME>';" ;
	
	private static Logger logger = Logger.getLogger( CreateDBPG.class ) ;
	
	private static boolean insertProceduresDone = false ;
	
	public CreateDBPG() throws UploaderException {
		super() ;
	}
	
	public void createI2B2Database( String projectId ) throws UploaderException {
		enterTrace( "CreateDBPG.createI2B2Database()" ) ;
		
		try {
			//
			// Create project specific schemas with tables, 
			// plus relevant control inserts into pm and hive...
			for( int i=0; i<SCRIPT_FILE_NAMES.length; i++ ) {
				runScript( projectId, SCRIPT_FILE_NAMES[i] ) ;
			}
			//
			// Create project specific database procedures...
//			insertProcedures() ;			
			//
			// Deploy the JBoss dataset definitions required by new project...
			deployToJBoss( projectId ) ;
		}
		finally {
			exitTrace( "CreateDBPG.createI2B2Database()" ) ;
		}

	}
	
	
	public void setSchema( String schemaName ) throws UploaderException {
		enterTrace( "CreateDBPG.setSchema()" ) ;
		try {
			String sqlCmd = SET_SCHEMA_CMD ;
			sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", schemaName ) ;
			Statement st = connection.createStatement() ;	
			connection.setAutoCommit( true ) ;
			st.execute( sqlCmd ) ;
		}
		catch( SQLException sqlex ) {
			logger.error( "CreateDBPG.setSchema(): ", sqlex ) ;
			throw new UploaderException( "Error whilst setting database schema: " + schemaName, sqlex ) ;
		}
		finally {
			exitTrace( "CreateDBPG.setSchema()" ) ;
		}		
	}

	
	private void runScript( String projectId, String fileName ) throws UploaderException {
		enterTrace( "CreateDBPG.runScript()" ) ;
		
		String s = new String();
		StringBuffer sb = new StringBuffer();
		Connection connection = null ;
		try {
			//
			// Create project specific tables and insert project info into hive and pm tables...
			InputStreamReader isr = new InputStreamReader( CreateDBPG.class.getResourceAsStream("/scripts/db-tables/" + fileName ) ) ;
			BufferedReader br = new BufferedReader( isr );

			while ((s = br.readLine()) != null) {

				s = s.replaceAll("<DB_SCHEMA_NAME>", projectId ) ;
				s = s.replaceAll("<DB_USER_NAME>", projectId ) ;	
				s = s.replaceAll("<DB_PASSWORD>", projectId ) ;
				
				s = s.replaceAll("<PROJECT_ONTOLOGY>", projectId ) ;
				s = s.replaceAll("<PROJECT_METADATA_TABLE>", projectId ) ;
				
				s = s.replaceAll("<PROJECT_ID>", projectId ) ;

				sb.append(s);

			}
			br.close();

			String[] inst = sb.toString().split(";");

			connection = getSimpleConnectionPG();	
			
			Statement st = connection.createStatement();
			
			connection.setTransactionIsolation( Connection.TRANSACTION_SERIALIZABLE ) ;
			connection.setAutoCommit( false ) ;

			for( int i = 0; i < inst.length; i++ ) {
				if( inst[i].trim().length() > 0 ) {

					try {
						logger.debug( inst[i] ) ;
						st.addBatch( inst[i] ) ;					
					} 
					catch( Exception e ) {
						logger.error( "ERROR on statement : " + inst[i] ) ;
						throw e ;
					}
					
				}
			}
			st.executeBatch() ;
			connection.commit() ;
			connection.setTransactionIsolation( Connection.TRANSACTION_SERIALIZABLE ) ;
			connection.setAutoCommit( true ) ;

		}
		catch( Exception ex ) {
			if( connection != null ) try { connection.rollback() ; } catch( SQLException sqlx ) { ; }
			logger.error( "CreateDBPG.runScript(): ", ex ) ;
			throw new UploaderException( "Error whilst executing SQL commands in file " + fileName, ex ) ;
		}
		finally {
			exitTrace( "CreateDBPG.runScript()" ) ;
		}
	}
	
	
	@SuppressWarnings("unused")
	private synchronized void insertProcedures() throws UploaderException {
		enterTrace( "CreateDBPG.insertProcedures()" ) ;
		Connection connection = null ;
		try {
			if( insertProceduresDone == false  ) {
				//
				// Create project specific database procedures...
				connection = getSimpleConnectionPG();
				connection.setTransactionIsolation( Connection.TRANSACTION_SERIALIZABLE ) ;
				connection.setAutoCommit( false ) ;
				Statement st = connection.createStatement();
				st.setEscapeProcessing(false);							
				logger.debug("Procedure 1");
				st.execute("CREATE OR REPLACE FUNCTION create_temp_concept_table (tempConceptTableName IN text,      errorMsg OUT text)  RETURNS text AS $body$ BEGIN      EXECUTE 'create table ' ||  tempConceptTableName || ' (         CONCEPT_CD varchar(50) NOT NULL,          CONCEPT_PATH varchar(900) NOT NULL ,          NAME_CHAR varchar(2000),          CONCEPT_BLOB text,          UPDATE_DATE timestamp,          DOWNLOAD_DATE timestamp,          IMPORT_DATE timestamp,          SOURCESYSTEM_CD varchar(50)     ) WITH OIDS';     EXECUTE 'CREATE INDEX idx_' || tempConceptTableName || '_pat_id ON ' || tempConceptTableName || '  (CONCEPT_PATH)';     EXCEPTION     WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL;");
				logger.debug("Procedure 2");
				st.execute("CREATE OR REPLACE FUNCTION create_temp_eid_table (tempPatientMappingTableName IN text ,errorMsg OUT text)  RETURNS text AS $body$ BEGIN      EXECUTE 'create table ' ||  tempPatientMappingTableName || ' (         ENCOUNTER_MAP_ID        varchar(200) NOT NULL,         ENCOUNTER_MAP_ID_SOURCE     varchar(50) NOT NULL,         PROJECT_ID              VARCHAR(50) NOT NULL,         PATIENT_MAP_ID          varchar(200),          PATIENT_MAP_ID_SOURCE   varchar(50),          ENCOUNTER_ID            varchar(200) NOT NULL,         ENCOUNTER_ID_SOURCE     varchar(50) ,         ENCOUNTER_NUM           numeric,          ENCOUNTER_MAP_ID_STATUS    varchar(50),         PROCESS_STATUS_FLAG     char(1),         UPDATE_DATE timestamp,          DOWNLOAD_DATE timestamp,          IMPORT_DATE timestamp,          SOURCESYSTEM_CD varchar(50)     ) WITH OIDS';     EXECUTE 'CREATE INDEX idx_' || tempPatientMappingTableName || '_eid_id ON ' || tempPatientMappingTableName || '  (ENCOUNTER_ID, ENCOUNTER_ID_SOURCE, ENCOUNTER_MAP_ID, ENCOUNTER_MAP_ID_SOURCE, ENCOUNTER_NUM)';     EXECUTE 'CREATE INDEX idx_' || tempPatientMappingTableName || '_stateid_eid_id ON ' || tempPatientMappingTableName || '  (PROCESS_STATUS_FLAG)';       EXCEPTION     WHEN OTHERS THEN         RAISE NOTICE '%%%', SQLSTATE,  ' - ' , SQLERRM; END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 3");
				st.execute("CREATE OR REPLACE FUNCTION create_temp_modifier_table (tempModifierTableName IN text,    errorMsg OUT text)   RETURNS text AS $body$ BEGIN  EXECUTE 'create table ' ||  tempModifierTableName || ' (         MODIFIER_CD varchar(50) NOT NULL,          MODIFIER_PATH varchar(900) NOT NULL ,          NAME_CHAR varchar(2000),          MODIFIER_BLOB text,          UPDATE_DATE timestamp,          DOWNLOAD_DATE timestamp,          IMPORT_DATE timestamp,          SOURCESYSTEM_CD varchar(50)          ) WITH OIDS';  EXECUTE 'CREATE INDEX idx_' || tempModifierTableName || '_pat_id ON ' || tempModifierTableName || '  (MODIFIER_PATH)'; EXCEPTION         WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 4");
				st.execute("CREATE OR REPLACE FUNCTION create_temp_patient_table (tempPatientDimensionTableName IN text,      errorMsg OUT text )  RETURNS text AS $body$ BEGIN      EXECUTE 'create table ' ||  tempPatientDimensionTableName || ' (         patient_id varchar(200),          patient_id_source varchar(50),         patient_num numeric(38,0),         vital_status_cd varchar(50),          birth_date timestamp,          death_date timestamp,          sex_cd char(50),          age_in_years_num numeric(5,0),          language_cd varchar(50),          race_cd varchar(50 ),          marital_status_cd varchar(50),          religion_cd varchar(50),          zip_cd varchar(50),          statecityzip_path varchar(700),          patient_blob text,          update_date timestamp,          download_date timestamp,          import_date timestamp,          sourcesystem_cd varchar(50)     )';     EXECUTE 'CREATE INDEX idx_' || tempPatientDimensionTableName || '_pat_id ON ' || tempPatientDimensionTableName || '  (patient_id, patient_id_source,patient_num)';     EXCEPTION     WHEN OTHERS THEN         RAISE NOTICE '%%%', SQLSTATE,  ' - ' , SQLERRM; END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 5");
				st.execute("CREATE OR REPLACE FUNCTION create_temp_pid_table (tempPatientMappingTableName IN text, 	errorMsg OUT text )  RETURNS text AS $body$ BEGIN  	EXECUTE 'create table ' ||  tempPatientMappingTableName || ' ( 		PATIENT_MAP_ID varchar(200),  		PATIENT_MAP_ID_SOURCE varchar(50),  		PATIENT_ID_STATUS varchar(50),  		PATIENT_ID  varchar(200), 		PATIENT_ID_SOURCE varchar(50), 		PROJECT_ID   VARCHAR(50) , 		PATIENT_NUM numeric(38,0), 		PATIENT_MAP_ID_STATUS varchar(50),  		PROCESS_STATUS_FLAG char(1),  		UPDATE_DATE timestamp,  		DOWNLOAD_DATE timestamp,  		IMPORT_DATE timestamp,  		SOURCESYSTEM_CD varchar(50) 	) WITH OIDS'; 	EXECUTE 'CREATE INDEX idx_' || tempPatientMappingTableName || '_pid_id ON ' || tempPatientMappingTableName || '  ( PATIENT_ID, PATIENT_ID_SOURCE )'; 	EXECUTE 'CREATE INDEX idx_' || tempPatientMappingTableName || 'map_pid_id ON ' || tempPatientMappingTableName || '   	( PATIENT_ID, PATIENT_ID_SOURCE,PATIENT_MAP_ID, PATIENT_MAP_ID_SOURCE,  PATIENT_NUM )'; 	EXECUTE 'CREATE INDEX idx_' || tempPatientMappingTableName || 'stat_pid_id ON ' || tempPatientMappingTableName || '   	(PROCESS_STATUS_FLAG)'; 	EXCEPTION 	WHEN OTHERS THEN 		RAISE NOTICE '%%%', SQLSTATE,  ' - ' , SQLERRM; END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 6");
				st.execute("CREATE OR REPLACE FUNCTION create_temp_provider_table (tempProviderTableName IN text,      errorMsg OUT text)  RETURNS text AS $body$ BEGIN      EXECUTE 'create table ' ||  tempProviderTableName || ' (         PROVIDER_ID varchar(50) NOT NULL,          PROVIDER_PATH varchar(700) NOT NULL,          NAME_CHAR varchar(2000),          PROVIDER_BLOB text,          UPDATE_DATE timestamp,          DOWNLOAD_DATE timestamp,          IMPORT_DATE timestamp,          SOURCESYSTEM_CD varchar(50),          UPLOAD_ID numeric     ) WITH OIDS';     EXECUTE 'CREATE INDEX idx_' || tempProviderTableName || '_ppath_id ON ' || tempProviderTableName || '  (PROVIDER_PATH)';     EXCEPTION     WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;        END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 7");
				st.execute("CREATE OR REPLACE FUNCTION create_temp_table (tempTableName IN text, errorMsg OUT text)  RETURNS text AS $body$ BEGIN      EXECUTE 'create table ' ||  tempTableName || '  (         encounter_num  numeric(38,0),         encounter_id varchar(200) not null,          encounter_id_source varchar(50) not null,         concept_cd       varchar(50) not null,          patient_num numeric(38,0),          patient_id  varchar(200) not null,         patient_id_source  varchar(50) not null,         provider_id   varchar(50),         start_date   timestamp,          modifier_cd varchar(100),         instance_num numeric(18,0),         valtype_cd varchar(50),         tval_char varchar(255),         nval_num numeric(18,5),         valueflag_cd char(50),         quantity_num numeric(18,5),         confidence_num numeric(18,0),         observation_blob text,         units_cd varchar(50),         end_date    timestamp,         location_cd varchar(50),         update_date  timestamp,         download_date timestamp,         import_date timestamp,         sourcesystem_cd varchar(50) ,         upload_id integer     ) WITH OIDS';     EXECUTE 'CREATE INDEX idx_' || tempTableName || '_pk ON ' || tempTableName || '  ( encounter_num,patient_num,concept_cd,provider_id,start_date,modifier_cd,instance_num)';     EXECUTE 'CREATE INDEX idx_' || tempTableName || '_enc_pat_id ON ' || tempTableName || '  (encounter_id,encounter_id_source, patient_id,patient_id_source )';     EXCEPTION     WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;  END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 8");
				st.execute("CREATE OR REPLACE FUNCTION create_temp_visit_table (tempTableName IN text, errorMsg OUT text )  RETURNS text AS $body$ BEGIN       EXECUTE 'create table ' ||  tempTableName || ' (         encounter_id                    varchar(200) not null,         encounter_id_source             varchar(50) not null,          project_id                      varchar(50) not null,         patient_id                      varchar(200) not null,         patient_id_source               varchar(50) not null,         encounter_num                   numeric(38,0),          inout_cd                        varchar(50),         location_cd                     varchar(50),         location_path                   varchar(900),         start_date                      timestamp,          end_date                        timestamp,         visit_blob                      text,         update_date                     timestamp,         download_date                   timestamp,         import_date                     timestamp,         sourcesystem_cd                 varchar(50)     ) WITH OIDS';     EXECUTE 'CREATE INDEX idx_' || tempTableName || '_enc_id ON ' || tempTableName || '  ( encounter_id,encounter_id_source,patient_id,patient_id_source )';     EXECUTE 'CREATE INDEX idx_' || tempTableName || '_patient_id ON ' || tempTableName || '  ( patient_id,patient_id_source )';     EXCEPTION     WHEN OTHERS THEN             RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;     END; $body$ LANGUAGE PLPGSQL;");
				logger.debug("Procedure 9");
				st.execute("CREATE OR REPLACE FUNCTION insert_concept_fromtemp(tempConceptTableName IN text, upload_id IN bigint, errorMsg OUT text )  RETURNS text AS $body$ BEGIN        EXECUTE 'DELETE      FROM     ' || tempConceptTableName || ' t1      WHERE     oid > (SELECT           min(oid)          FROM          ' || tempConceptTableName || ' t2         WHERE          t1.concept_cd = t2.concept_cd          AND t1.concept_path = t2.concept_path     )';     EXECUTE ' UPDATE concept_dimension       SET       concept_cd=temp.concept_cd     ,name_char=temp.name_char     ,concept_blob=temp.concept_blob     ,update_date=temp.update_date     ,download_date=temp.download_date     ,import_date=Now()     ,sourcesystem_cd=temp.sourcesystem_cd     ,upload_id=' || UPLOAD_ID  || '     FROM      ' || tempConceptTableName || '  temp        WHERE      temp.concept_path = concept_dimension.concept_path      AND temp.update_date >= concept_dimension.update_date      AND EXISTS (SELECT 1          FROM ' || tempConceptTableName || ' temp           WHERE temp.concept_path = concept_dimension.concept_path          AND temp.update_date >= concept_dimension.update_date     )     ';     EXECUTE 'INSERT INTO concept_dimension  (         concept_cd         ,concept_path         ,name_char         ,concept_blob         ,update_date         ,download_date         ,import_date         ,sourcesystem_cd         ,upload_id     )     SELECT       concept_cd     ,concept_path     ,name_char     ,concept_blob     ,update_date     ,download_date     ,Now()     ,sourcesystem_cd     ,' || upload_id || '     FROM ' || tempConceptTableName || '  temp     WHERE NOT EXISTS (SELECT concept_cd          FROM concept_dimension cd          WHERE cd.concept_path = temp.concept_path)     ';     EXCEPTION     WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL;");
				logger.debug("Procedure 10");
				st.execute("CREATE OR REPLACE FUNCTION insert_eid_map_fromtemp (tempEidTableName IN text,  upload_id IN bigint,     errorMsg OUT text )  RETURNS text AS $body$ DECLARE  existingEncounterNum varchar(32); maxEncounterNum bigint; distinctEidCur REFCURSOR; disEncounterId varchar(100);  disEncounterIdSource varchar(100);  BEGIN     EXECUTE ' delete  from ' || tempEidTableName ||  ' t1  where      oid > (select min(oid) from ' || tempEidTableName || ' t2          where t1.encounter_map_id = t2.encounter_map_id         and t1.encounter_map_id_source = t2.encounter_map_id_source         and t1.encounter_id = t2.encounter_id         and t1.encounter_id_source = t2.encounter_id_source) ';     LOCK TABLE  encounter_mapping IN EXCLUSIVE MODE NOWAIT;     select max(encounter_num) into STRICT  maxEncounterNum from encounter_mapping ;      if coalesce(maxEncounterNum::text, '') = '' then          maxEncounterNum := 0;     end if;     open distinctEidCur for EXECUTE 'SELECT distinct encounter_id,encounter_id_source from ' || tempEidTableName ||' ' ;     loop         FETCH distinctEidCur INTO disEncounterId, disEncounterIdSource;         IF NOT FOUND THEN EXIT; END IF;              if  disEncounterIdSource = 'HIVE'  THEN                  begin                     select encounter_num into existingEncounterNum from encounter_mapping where encounter_num = CAST(disEncounterId AS numeric) and encounter_ide_source = 'HIVE';                     EXCEPTION  when NO_DATA_FOUND THEN                         existingEncounterNum := null;                 end;                 if (existingEncounterNum IS NOT NULL AND existingEncounterNum::text <> '') then                      EXECUTE ' update ' || tempEidTableName ||' set encounter_num = CAST(encounter_id AS numeric), process_status_flag = ''P''                     where encounter_id = $1 and not exists (select 1 from encounter_mapping em where em.encounter_ide = encounter_map_id                         and em.encounter_ide_source = encounter_map_id_source)' using disEncounterId;                 else                      if maxEncounterNum < CAST(disEncounterId AS numeric) then                          maxEncounterNum := disEncounterId;                     end if ;                     EXECUTE ' update ' || tempEidTableName ||' set encounter_num = CAST(encounter_id AS numeric), process_status_flag = ''P'' where                      encounter_id =  $1 and encounter_id_source = ''HIVE'' and not exists (select 1 from encounter_mapping em where em.encounter_ide = encounter_map_id                         and em.encounter_ide_source = encounter_map_id_source)' using disEncounterId;                 end if;                 else                  begin                     select encounter_num into STRICT  existingEncounterNum from encounter_mapping where encounter_ide = disEncounterId and                      encounter_ide_source = disEncounterIdSource ;                      EXCEPTION     WHEN NO_DATA_FOUND THEN         existingEncounterNum := null;                 end;                 if existingEncounterNum is not  null then                      EXECUTE ' update ' || tempEidTableName ||' set encounter_num = CAST($1 AS numeric) , process_status_flag = ''P''                     where encounter_id = $2 and not exists (select 1 from encounter_mapping em where em.encounter_ide = encounter_map_id                         and em.encounter_ide_source = encounter_map_id_source)' using existingEncounterNum, disEncounterId;                 else                      maxEncounterNum := maxEncounterNum + 1 ;                                          EXECUTE ' insert into ' || tempEidTableName ||' (encounter_map_id,encounter_map_id_source,encounter_id,encounter_id_source,encounter_num,process_status_flag                         ,encounter_map_id_status,update_date,download_date,import_date,sourcesystem_cd,project_id)                      values($1,''HIVE'',$2,''HIVE'',$3,''P'',''A'',Now(),Now(),Now(),''edu.harvard.i2b2.crc'',''HIVE'')' using maxEncounterNum,maxEncounterNum,maxEncounterNum;                      EXECUTE ' update ' || tempEidTableName ||' set encounter_num =  $1 , process_status_flag = ''P''                      where encounter_id = $2 and  not exists (select 1 from                          encounter_mapping em where em.encounter_ide = encounter_map_id                         and em.encounter_ide_source = encounter_map_id_source)' using maxEncounterNum, disEncounterId;                 end if ;                 end if;      END LOOP;     close distinctEidCur ;      EXECUTE 'UPDATE encounter_mapping SET  encounter_num = CAST(temp.encounter_id AS numeric) ,encounter_ide_status = temp.encounter_map_id_status ,update_date = temp.update_date ,download_date  = temp.download_date ,import_date = Now() ,sourcesystem_cd  = temp.sourcesystem_cd ,upload_id = ' || upload_id ||' FROM '|| tempEidTableName || '  temp WHERE  temp.encounter_map_id = encounter_mapping.encounter_ide  and temp.encounter_map_id_source = encounter_mapping.encounter_ide_source and temp.encounter_id_source = ''HIVE'' and coalesce(temp.process_status_flag::text, '''') = ''''   and coalesce(encounter_mapping.update_date,to_date(''01-JAN-1900'',''DD-MON-YYYY'')) <= coalesce(temp.update_date,to_date(''01-JAN-1900'',''DD-MON-YYYY'')) ';      EXECUTE ' insert into encounter_mapping (encounter_ide,encounter_ide_source,encounter_ide_status,encounter_num,patient_ide,patient_ide_source,update_date,download_date,import_date,sourcesystem_cd,upload_id,project_id)      SELECT encounter_map_id,encounter_map_id_source,encounter_map_id_status,encounter_num,patient_map_id,patient_map_id_source,update_date,download_date,Now(),sourcesystem_cd,' || upload_id || ' , project_id     FROM ' || tempEidTableName || '       WHERE process_status_flag = ''P'' ' ;      EXCEPTION     WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;     end;     $body$     LANGUAGE PLPGSQL;");
				logger.debug("Procedure 11");
				st.execute("CREATE OR REPLACE FUNCTION insert_encountervisit_fromtemp (tempTableName IN text, upload_id IN bigint,     errorMsg OUT text)  RETURNS text AS $body$ DECLARE  maxEncounterNum bigint;   BEGIN      EXECUTE 'DELETE FROM ' || tempTableName || ' t1 WHERE oid >      (SELECT  min(oid) FROM ' || tempTableName || ' t2         WHERE t1.encounter_id = t2.encounter_id          AND t1.encounter_id_source = t2.encounter_id_source         AND coalesce(t1.patient_id,'''') = coalesce(t2.patient_id,'''')         AND coalesce(t1.patient_id_source,'''') = coalesce(t2.patient_id_source,''''))';     LOCK TABLE  encounter_mapping IN EXCLUSIVE MODE NOWAIT;    EXECUTE 'INSERT INTO encounter_mapping (         encounter_ide         , encounter_ide_source         , encounter_num         , patient_ide         , patient_ide_source         , encounter_ide_status         , upload_id         , project_id     )     (SELECT          distinctTemp.encounter_id         , distinctTemp.encounter_id_source         , CAST(distinctTemp.encounter_id AS numeric)         , distinctTemp.patient_id         , distinctTemp.patient_id_source         , ''A''         ,  '|| upload_id ||'         , distinctTemp.project_id         FROM          (SELECT              distinct encounter_id             , encounter_id_source             , patient_id             , patient_id_source              , project_id             FROM ' || tempTableName || '  temp             WHERE              NOT EXISTS (SELECT encounter_ide                  FROM encounter_mapping em                  WHERE                  em.encounter_ide = temp.encounter_id                  AND em.encounter_ide_source = temp.encounter_id_source             )             AND encounter_id_source = ''HIVE''      )   distinctTemp ) ' ;     EXECUTE ' UPDATE ' ||  tempTableName     || ' SET encounter_num = (SELECT em.encounter_num         FROM encounter_mapping em         WHERE em.encounter_ide = '|| tempTableName ||'.encounter_id         and em.encounter_ide_source = '|| tempTableName ||'.encounter_id_source          and coalesce(em.patient_ide_source,'''') = coalesce('|| tempTableName ||'.patient_id_source,'''')         and coalesce(em.patient_ide,'''')= coalesce('|| tempTableName ||'.patient_id,'''')     )     WHERE EXISTS (SELECT em.encounter_num          FROM encounter_mapping em         WHERE em.encounter_ide = '|| tempTableName ||'.encounter_id         and em.encounter_ide_source = '||tempTableName||'.encounter_id_source         and coalesce(em.patient_ide_source,'''') = coalesce('|| tempTableName ||'.patient_id_source,'''')         and coalesce(em.patient_ide,'''')= coalesce('|| tempTableName ||'.patient_id,''''))';            EXECUTE ' UPDATE visit_dimension  SET       start_date =temp.start_date     ,end_date=temp.end_date     ,inout_cd=temp.inout_cd     ,location_cd=temp.location_cd     ,visit_blob=temp.visit_blob     ,update_date=temp.update_date     ,download_date=temp.download_date     ,import_date=Now()     ,sourcesystem_cd=temp.sourcesystem_cd     , upload_id=' || UPLOAD_ID  || '     FROM ' || tempTableName || '  temp            WHERE     temp.encounter_num = visit_dimension.encounter_num      AND temp.update_date >= visit_dimension.update_date      AND exists (SELECT 1          FROM ' || tempTableName || ' temp          WHERE temp.encounter_num = visit_dimension.encounter_num          AND temp.update_date >= visit_dimension.update_date     ) ';      EXECUTE 'INSERT INTO visit_dimension  (encounter_num,patient_num,start_date,end_date,inout_cd,location_cd,visit_blob,update_date,download_date,import_date,sourcesystem_cd, upload_id)     SELECT temp.encounter_num     , pm.patient_num,     temp.start_date,temp.end_date,temp.inout_cd,temp.location_cd,temp.visit_blob,     temp.update_date,     temp.download_date,     Now(),      temp.sourcesystem_cd,     '|| upload_id ||'     FROM      ' || tempTableName || '  temp , patient_mapping pm      WHERE      (temp.encounter_num IS NOT NULL AND temp.encounter_num::text <> '''') and      NOT EXISTS (SELECT encounter_num          FROM visit_dimension vd          WHERE          vd.encounter_num = temp.encounter_num)      AND pm.patient_ide = temp.patient_id      AND pm.patient_ide_source = temp.patient_id_source     ';     EXCEPTION     WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 12");
				st.execute("CREATE OR REPLACE FUNCTION insert_modifier_fromtemp (tempModifierTableName IN text, upload_id IN bigint, errorMsg OUT text )  RETURNS text AS $body$ BEGIN      EXECUTE 'DELETE FROM ' || tempModifierTableName || ' t1 WHERE oid >      (SELECT  min(oid) FROM ' || tempModifierTableName || ' t2         WHERE t1.modifier_cd = t2.modifier_cd          AND t1.modifier_path = t2.modifier_path     )';     EXECUTE ' UPDATE modifier_dimension  SET           modifier_cd=temp.modifier_cd         ,name_char=temp.name_char         ,modifier_blob=temp.modifier_blob         ,update_date=temp.update_date         ,download_date=temp.download_date         ,import_date=Now()         ,sourcesystem_cd=temp.SOURCESYSTEM_CD         ,upload_id=' || UPLOAD_ID  || '          FROM ' || tempModifierTableName || '  temp         WHERE          temp.modifier_path = modifier_dimension.modifier_path          AND temp.update_date >= modifier_dimension.update_date         AND EXISTS (SELECT 1              FROM ' || tempModifierTableName || ' temp               WHERE temp.modifier_path = modifier_dimension.modifier_path              AND temp.update_date >= modifier_dimension.update_date)         ';         EXECUTE 'INSERT INTO modifier_dimension  (             modifier_cd             ,modifier_path             ,name_char             ,modifier_blob             ,update_date             ,download_date             ,import_date             ,sourcesystem_cd             ,upload_id         )         SELECT           modifier_cd         ,modifier_path         ,name_char         ,modifier_blob         ,update_date         ,download_date         ,Now()         ,sourcesystem_cd         ,' || upload_id || '           FROM         ' || tempModifierTableName || '  temp         WHERE NOT EXISTs (SELECT modifier_cd              FROM modifier_dimension cd             WHERE cd.modifier_path = temp.modifier_path         )         ';         EXCEPTION     WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL;  ");
				logger.debug("Procedure 13");
				st.execute("CREATE OR REPLACE FUNCTION insert_patient_fromtemp (tempTableName IN text,upload_id IN bigint,     errorMsg OUT text)  RETURNS text AS $body$ DECLARE  maxPatientNum bigint;   BEGIN      LOCK TABLE  patient_mapping IN EXCLUSIVE MODE NOWAIT;     EXECUTE ' INSERT INTO patient_mapping (patient_ide,patient_ide_source,patient_num,patient_ide_status, upload_id)     (SELECT distinctTemp.patient_id, distinctTemp.patient_id_source, CAST(distinctTemp.patient_id AS numeric), ''A'',   '|| upload_id ||'         FROM          (SELECT distinct patient_id, patient_id_source from ' || tempTableName || '  temp             where  not exists (SELECT patient_ide from patient_mapping pm where pm.patient_ide = temp.patient_id and pm.patient_ide_source = temp.patient_id_source)             and patient_id_source = ''HIVE'' )   distinctTemp) ';      EXECUTE ' UPDATE ' ||  tempTableName     || ' SET patient_num = (SELECT pm.patient_num         FROM patient_mapping pm         WHERE pm.patient_ide = '|| tempTableName ||'.patient_id         AND pm.patient_ide_source = '|| tempTableName ||'.patient_id_source     )     WHERE EXISTS (SELECT pm.patient_num          FROM patient_mapping pm         WHERE pm.patient_ide = '|| tempTableName ||'.patient_id         AND pm.patient_ide_source = '||tempTableName||'.patient_id_source)';             EXECUTE ' UPDATE patient_dimension  SET       vital_status_cd = temp.vital_status_cd     , birth_date = temp.birth_date     , death_date = temp.death_date     , sex_cd = temp.sex_cd     , age_in_years_num = temp.age_in_years_num     , language_cd = temp.language_cd     , race_cd = temp.race_cd     , marital_status_cd = temp.marital_status_cd     , religion_cd = temp.religion_cd     , zip_cd = temp.zip_cd     , statecityzip_path = temp.statecityzip_path     , patient_blob = temp.patient_blob     , update_date = temp.update_date     , download_date = temp.download_date     , import_date = Now()     , sourcesystem_cd = temp.sourcesystem_cd      , upload_id =  ' || UPLOAD_ID  || '     FROM  ' || tempTableName || '  temp     WHERE      temp.patient_num = patient_dimension.patient_num      AND temp.update_date >= patient_dimension.update_date     AND EXISTS (select 1          FROM ' || tempTableName || ' temp           WHERE          temp.patient_num = patient_dimension.patient_num          AND temp.update_date >= patient_dimension.update_date     )    ';      EXECUTE 'INSERT INTO patient_dimension  (patient_num,vital_status_cd, birth_date, death_date,         sex_cd, age_in_years_num,language_cd,race_cd,marital_status_cd, religion_cd,         zip_cd,statecityzip_path,patient_blob,update_date,download_date,import_date,sourcesystem_cd,         upload_id)     SELECT temp.patient_num,     temp.vital_status_cd, temp.birth_date, temp.death_date,     temp.sex_cd, temp.age_in_years_num,temp.language_cd,temp.race_cd,temp.marital_status_cd, temp.religion_cd,     temp.zip_cd,temp.statecityzip_path,temp.patient_blob,     temp.update_date,     temp.download_date,     Now(),     temp.sourcesystem_cd,     '|| upload_id ||'     FROM      ' || tempTableName || '  temp      WHERE      NOT EXISTS (SELECT patient_num          FROM patient_dimension pd          WHERE pd.patient_num = temp.patient_num)      AND      (patient_num IS NOT NULL AND patient_num::text <> '''')     ';     EXCEPTION WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;  END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 14");
				st.execute("CREATE OR REPLACE FUNCTION insert_pid_map_fromtemp (tempPidTableName IN text,  upload_id IN bigint,  	errorMsg OUT text)  RETURNS text AS $body$ DECLARE existingPatientNum varchar(32); maxPatientNum bigint; distinctPidCur REFCURSOR; disPatientId varchar(100);  disPatientIdSource varchar(100); BEGIN 	EXECUTE ' delete  from ' || tempPidTableName ||  ' t1  where  	oid > (select min(oid) from ' || tempPidTableName || ' t2  		where t1.patient_map_id = t2.patient_map_id 		and t1.patient_map_id_source = t2.patient_map_id_source) '; 	LOCK TABLE  patient_mapping IN EXCLUSIVE MODE NOWAIT; 	select max(patient_num) into STRICT  maxPatientNum from patient_mapping ;  	if coalesce(maxPatientNum::text, '') = '' then  		maxPatientNum := 0; 	end if; 	open distinctPidCur for EXECUTE 'SELECT distinct patient_id,patient_id_source from ' || tempPidTableName || '' ; 	loop 		FETCH distinctPidCur INTO disPatientId, disPatientIdSource; 		IF NOT FOUND THEN EXIT;  	END IF;  	 	if  disPatientIdSource = 'HIVE'  THEN  		begin			 			select patient_num into existingPatientNum from patient_mapping where patient_num = CAST(disPatientId AS numeric) and patient_ide_source = 'HIVE'; 			EXCEPTION  when NO_DATA_FOUND THEN 				existingPatientNum := null; 		end; 		if (existingPatientNum IS NOT NULL AND existingPatientNum::text <> '') then  			EXECUTE ' update ' || tempPidTableName ||' set patient_num = CAST(patient_id AS numeric), process_status_flag = ''P'' 			where patient_id = $1 and not exists (select 1 from patient_mapping pm where pm.patient_ide = patient_map_id 				and pm.patient_ide_source = patient_map_id_source)' using disPatientId; 		else 			 			if maxPatientNum < CAST(disPatientId AS numeric) then  				maxPatientNum := disPatientId; 			end if ; 			EXECUTE ' update ' || tempPidTableName ||' set patient_num = CAST(patient_id AS numeric), process_status_flag = ''P'' where  			patient_id = $1 and patient_id_source = ''HIVE'' and not exists (select 1 from patient_mapping pm where pm.patient_ide = patient_map_id 				and pm.patient_ide_source = patient_map_id_source)' using disPatientId; 		end if;     	else  		begin 			select patient_num into STRICT  existingPatientNum from patient_mapping where patient_ide = disPatientId and  			patient_ide_source = disPatientIdSource ;  			EXCEPTION 	WHEN NO_DATA_FOUND THEN 		existingPatientNum := null; 		end; 		if (existingPatientNum IS NOT NULL AND existingPatientNum::text <> '') then  			EXECUTE ' update ' || tempPidTableName ||' set patient_num = CAST($1 AS numeric) , process_status_flag = ''P'' 			where patient_id = $2 and not exists (select 1 from patient_mapping pm where pm.patient_ide = patient_map_id 				and pm.patient_ide_source = patient_map_id_source)' using  existingPatientNum,disPatientId; 		else  			maxPatientNum := maxPatientNum + 1 ;  			EXECUTE 'insert into ' || tempPidTableName ||' ( 				patient_map_id 				,patient_map_id_source 				,patient_id 				,patient_id_source 				,patient_num 				,process_status_flag 				,patient_map_id_status 				,update_date 				,download_date 				,import_date 				,sourcesystem_cd 				,project_id)  			values( 				$1 				,''HIVE'' 				,$2 				,''HIVE'' 				,$3 				,''P'' 				,''A'' 				,Now() 				,Now() 				,Now() 				,''edu.harvard.i2b2.crc'' 			,''HIVE'' 			)' using maxPatientNum,maxPatientNum,maxPatientNum;  			EXECUTE 'update ' || tempPidTableName ||' set patient_num =  $1 , process_status_flag = ''P''  			where patient_id = $2 and  not exists (select 1 from  				patient_mapping pm where pm.patient_ide = patient_map_id 				and pm.patient_ide_source = patient_map_id_source)' using maxPatientNum, disPatientId  ; 		end if ; 	end if;  	END LOOP; 	close distinctPidCur ; EXECUTE ' UPDATE patient_mapping SET  patient_num = CAST(temp.patient_id AS numeric) ,patient_ide_status = temp.patient_map_id_status ,update_date = temp.update_date ,download_date  = temp.download_date ,import_date = Now() ,sourcesystem_cd  = temp.sourcesystem_cd ,upload_id = ' || upload_id ||' FROM '|| tempPidTableName || '  temp WHERE  temp.patient_map_id = patient_mapping.patient_ide  and temp.patient_map_id_source = patient_mapping.patient_ide_source and temp.patient_id_source = ''HIVE'' and coalesce(temp.process_status_flag::text, '''') = ''''   and coalesce(patient_mapping.update_date,to_date(''01-JAN-1900'',''DD-MON-YYYY'')) <= coalesce(temp.update_date,to_date(''01-JAN-1900'',''DD-MON-YYYY'')) '; 	EXECUTE ' insert into patient_mapping (patient_ide,patient_ide_source,patient_ide_status,patient_num,update_date,download_date,import_date,sourcesystem_cd,upload_id,project_id) 	SELECT patient_map_id,patient_map_id_source,patient_map_id_status,patient_num,update_date,download_date,Now(),sourcesystem_cd,' || upload_id ||', project_id from '|| tempPidTableName || '  	where process_status_flag = ''P'' ' ;  	EXCEPTION WHEN OTHERS THEN 		RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM; 	END; 	$body$ 	LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 15");
				st.execute("CREATE OR REPLACE FUNCTION insert_provider_fromtemp (tempProviderTableName IN text, upload_id IN bigint,     errorMsg OUT text) RETURNS text AS $body$ BEGIN      EXECUTE 'DELETE FROM ' || tempProviderTableName || ' t1 WHERE oid >      (SELECT  min(oid) FROM ' || tempProviderTableName || ' t2         WHERE t1.provider_id = t2.provider_id          AND t1.provider_path = t2.provider_path     )';     EXECUTE ' UPDATE provider_dimension  SET           provider_id =temp.provider_id         , name_char = temp.name_char         , provider_blob = temp.provider_blob         , update_date=temp.update_date         , download_date=temp.download_date         , import_date=Now()         , sourcesystem_cd=temp.sourcesystem_cd         , upload_id = ' || upload_id || '         FROM ' || tempProviderTableName || '  temp          WHERE          temp.provider_path = provider_dimension.provider_path and temp.update_date >= provider_dimension.update_date      AND EXISTS (select 1 from ' || tempProviderTableName || ' temp  where temp.provider_path = provider_dimension.provider_path          and temp.update_date >= provider_dimension.update_date) ';      EXECUTE 'insert into provider_dimension  (provider_id,provider_path,name_char,provider_blob,update_date,download_date,import_date,sourcesystem_cd,upload_id)     SELECT  provider_id,provider_path,      name_char,provider_blob,     update_date,download_date,     Now(),sourcesystem_cd, ' || upload_id || '     FROM ' || tempProviderTableName || '  temp     WHERE NOT EXISTS (SELECT provider_id          FROM provider_dimension pd          WHERE pd.provider_path = temp.provider_path      )';     EXCEPTION     WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL;");
				logger.debug("Procedure 16");
				st.execute("CREATE OR REPLACE FUNCTION merge_temp_observation_fact (upload_temptable_name text)  RETURNS VOID AS $body$ BEGIN         EXECUTE 'MERGE  INTO observation_fact obsfact                    USING ( select emap.encounter_num,patmap.patient_num,                      utemp.concept_cd,                                          utemp.provider_id,                                         utemp.start_date,                                          utemp.modifier_cd,                                         utemp.valtype_cd,                                         utemp.tval_char,                                         utemp.nval_num,                                         utemp.valueflag_cd,                                         utemp.quantity_num,                                         utemp.confidence_num,                                         utemp.observation_blob,                                         utemp.units_cd,                                         utemp.end_date,                                         utemp.location_cd,                                         utemp.update_date,                                         utemp.download_date,                                         utemp.import_date,                                         utemp.sourcesystem_cd,                                         utemp.upload_id                     from ' || upload_temptable_name  || '  utemp , encounter_mapping emap, patient_mapping patmap                     where utemp.encounter_ide = emap.encounter_ide and  utemp.patient_ide = patmap.patient_ide            ) temp                    on (                                 temp.encounter_num = obsfact.encounter_num                                 and                                 temp.concept_cd = obsfact.concept_cd                                 and                                 temp.patient_num = obsfact.patient_num             )                             when matched then                                  update  set                                          obsfact.provider_id = temp.provider_id,                                         obsfact.start_date = temp.start_date,                                         obsfact.modifier_cd = temp.modifier_cd,                                         obsfact.valtype_cd = temp.valtype_cd,                                         obsfact.tval_char = temp.tval_char,                                         obsfact.nval_num = temp.nval_num,                                         obsfact.valueflag_cd = temp.valueflag_cd,                                         obsfact.quantity_num = temp.quantity_num,                                         obsfact.confidence_num = temp.confidence_num,                                         obsfact.observation_blob = temp.observation_blob ,                                         obsfact.units_cd = temp.units_cd,                                         obsfact.end_date = temp.end_date,                                         obsfact.location_cd = temp.location_cd,                                         obsfact.update_date = temp.update_date,                                         obsfact.download_date = temp.download_date,                                         obsfact.import_date = temp.import_date,                                         obsfact.sourcesystem_cd = temp.sourcesystem_cd,                                         obsfact.upload_id = temp.upload_id                     where temp.update_date > obsfact.update_date                          when not matched then                                  insert (encounter_num,                                          concept_cd,                                          patient_num,                                         provider_id,                                         start_date,                                          modifier_cd,                                         valtype_cd,                                         tval_char,                                         nval_num,                                         valueflag_cd,                                         quantity_num,                                         confidence_num,                                         observation_blob,                                         units_cd,                                         end_date,                                         location_cd,                                         update_date,                                         download_date,                                         import_date,                                         sourcesystem_cd,                                         upload_id)                                  values (                                         temp.encounter_num,                                          temp.concept_cd,                                          temp.patient_num,                                         temp.provider_id,                                         temp.start_date,                                          temp.modifier_cd,                                         temp.valtype_cd,                                         temp.tval_char,                                         temp.nval_num,                                         temp.valueflag_cd,                                         temp.quantity_num,                                         temp.confidence_num,                                         temp.observation_blob,                                         temp.units_cd,                                         temp.end_date,                                         temp.location_cd,                                         temp.update_date,                                         temp.download_date,                                         temp.import_date,                                         temp.sourcesystem_cd,                                         temp.upload_id                                 )';              EXCEPTION         WHEN OTHERS THEN                 RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL;  ");
				logger.debug("Procedure 17");
				st.execute("CREATE OR REPLACE FUNCTION remove_temp_table ( tempTableName IN varchar, errorMsg OUT text ) RETURNS text AS $body$  DECLARE  BEGIN     EXECUTE 'DROP TABLE ' || tempTableName|| ' CASCADE ';  EXCEPTION  WHEN OTHERS THEN     RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 18");
				st.execute("CREATE OR REPLACE FUNCTION report_missing_dimension (upload_temptable_name IN text)  RETURNS VOID AS $body$ BEGIN EXECUTE 'INSERT ALL INTO missing_dimension_report  PERFORM concept_cd dimension_value,count(*) total_count,''C'' dimension, upload_id FROM ' ||upload_temptable_name ||' temp WHERE temp.concept_cd NOT IN (SELECT concept_cd FROM concept_dimension) group by concept_cd,upload_id'; EXECUTE 'INSERT ALL INTO missing_dimension_report SELECT encounter_ide dimension_value,count(*) total_count,''E'' dimension, upload_id FROM ' ||upload_temptable_name ||' temp WHERE temp.encounter_ide NOT IN (SELECT encounter_ide FROM encounter_mapping) group by encounter_ide,upload_id'; EXECUTE 'INSERT ALL INTO missing_dimension_report  PERFORM patient_ide dimension_value,count(*) total_count,''P'' dimension, upload_id FROM ' ||upload_temptable_name ||' temp WHERE temp.patient_ide NOT IN (SELECT patient_ide FROM patient_mapping) group by patient_ide,upload_id'; EXCEPTION         WHEN OTHERS THEN                 RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL;  ");
				logger.debug("Procedure 19");
				st.execute("CREATE OR REPLACE FUNCTION sync_clear_concept_table (tempConceptTableName in text, backupConceptTableName IN text, uploadId in bigint, errorMsg OUT text )   RETURNS text AS $body$ DECLARE   interConceptTableName  varchar(400);  BEGIN          interConceptTableName := backupConceptTableName || '_inter';         EXECUTE 'DELETE FROM ' || tempConceptTableName || ' t1 WHERE oid >                                             (SELECT  min(oid) FROM ' || tempConceptTableName || ' t2                                              WHERE t1.concept_cd = t2.concept_cd                                              AND t1.concept_path = t2.concept_path                                             )';     EXECUTE 'create table ' ||  interConceptTableName || ' (     CONCEPT_CD          varchar(50) NOT NULL,         CONCEPT_PATH            varchar(700) NOT NULL,         NAME_CHAR               varchar(2000) NULL,         CONCEPT_BLOB        text NULL,         UPDATE_DATE         timestamp NULL,         DOWNLOAD_DATE       timestamp NULL,         IMPORT_DATE         timestamp NULL,         SOURCESYSTEM_CD     varchar(50) NULL,         UPLOAD_ID               numeric(38,0) NULL,     CONSTRAINT '|| interConceptTableName ||'_pk  PRIMARY KEY(CONCEPT_PATH)          )';             EXECUTE 'insert into '|| interConceptTableName ||'  (concept_cd,concept_path,name_char,concept_blob,update_date,download_date,import_date,sourcesystem_cd,upload_id)                             PERFORM  concept_cd, substring(concept_path from 1 for 700),                         name_char,concept_blob,                         update_date,download_date,                         LOCALTIMESTAMP,sourcesystem_cd,                          ' || uploadId || '  from ' || tempConceptTableName || '  temp ';         EXECUTE 'alter table concept_dimension rename to ' || backupConceptTableName  ||'' ;             EXECUTE 'CREATE INDEX ' || interConceptTableName || '_uid_idx ON ' || interConceptTableName || '(UPLOAD_ID)';     EXECUTE 'CREATE INDEX ' || interConceptTableName || '_cd_idx ON ' || interConceptTableName || '(concept_cd)';         EXECUTE 'alter table ' || interConceptTableName  || ' rename to concept_dimension' ; EXCEPTION         WHEN OTHERS THEN                 RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL;  ");
				logger.debug("Procedure 20");
				st.execute("CREATE OR REPLACE FUNCTION sync_clear_modifier_table (tempModifierTableName in text, backupModifierTableName IN text, uploadId in bigint, errorMsg OUT text )   RETURNS text AS $body$ DECLARE   interModifierTableName  varchar(400);  BEGIN          interModifierTableName := backupModifierTableName || '_inter';         EXECUTE 'DELETE FROM ' || tempModifierTableName || ' t1 WHERE oid >                                             (SELECT  min(oid) FROM ' || tempModifierTableName || ' t2                                              WHERE t1.modifier_cd = t2.modifier_cd                                              AND t1.modifier_path = t2.modifier_path                                             )';     EXECUTE 'create table ' ||  interModifierTableName || ' (         MODIFIER_CD          varchar(50) NOT NULL,         MODIFIER_PATH           varchar(700) NOT NULL,         NAME_CHAR               varchar(2000) NULL,         MODIFIER_BLOB        text NULL,         UPDATE_DATE         timestamp NULL,         DOWNLOAD_DATE       timestamp NULL,         IMPORT_DATE         timestamp NULL,         SOURCESYSTEM_CD     varchar(50) NULL,         UPLOAD_ID               numeric(38,0) NULL,     CONSTRAINT '|| interModifierTableName ||'_pk  PRIMARY KEY(MODIFIER_PATH)          )';             EXECUTE 'insert into '|| interModifierTableName ||'  (modifier_cd,modifier_path,name_char,modifier_blob,update_date,download_date,import_date,sourcesystem_cd,upload_id)                             PERFORM  modifier_cd, substring(modifier_path from 1 for 700),                         name_char,modifier_blob,                         update_date,download_date,                         LOCALTIMESTAMP,sourcesystem_cd,                          ' || uploadId || '  from ' || tempModifierTableName || '  temp ';         EXECUTE 'alter table modifier_dimension rename to ' || backupModifierTableName  ||'' ;             EXECUTE 'CREATE INDEX ' || interModifierTableName || '_uid_idx ON ' || interModifierTableName || '(UPLOAD_ID)';         EXECUTE 'CREATE INDEX ' || interModifierTableName || '_cd_idx ON ' || interModifierTableName || '(modifier_cd)';                EXECUTE 'alter table ' || interModifierTableName  || ' rename to modifier_dimension' ; EXCEPTION         WHEN OTHERS THEN                 RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 21");
				st.execute("CREATE OR REPLACE FUNCTION sync_clear_provider_table (tempProviderTableName in text, backupProviderTableName IN text, uploadId in bigint, errorMsg OUT text )   RETURNS text AS $body$ DECLARE   interProviderTableName  varchar(400);  BEGIN          interProviderTableName := backupProviderTableName || '_inter';                         EXECUTE 'DELETE FROM ' || tempProviderTableName || ' t1 WHERE oid >                                             (SELECT  min(oid) FROM ' || tempProviderTableName || ' t2                                              WHERE t1.provider_id = t2.provider_id                                              AND t1.provider_path = t2.provider_path                                             )';     EXECUTE 'create table ' ||  interProviderTableName || ' (     PROVIDER_ID         varchar(50) NOT NULL,         PROVIDER_PATH       varchar(700) NOT NULL,         NAME_CHAR               varchar(850) NULL,         PROVIDER_BLOB       text NULL,         UPDATE_DATE             timestamp NULL,         DOWNLOAD_DATE       timestamp NULL,         IMPORT_DATE         timestamp NULL,         SOURCESYSTEM_CD     varchar(50) NULL,         UPLOAD_ID               numeric(38,0) NULL ,     CONSTRAINT  ' || interProviderTableName || '_pk PRIMARY KEY(PROVIDER_PATH,provider_id)          )';             EXECUTE 'insert into ' ||  interProviderTableName || ' (provider_id,provider_path,name_char,provider_blob,update_date,download_date,import_date,sourcesystem_cd,upload_id)                             PERFORM  provider_id,provider_path,                          name_char,provider_blob,                         update_date,download_date,                         LOCALTIMESTAMP,sourcesystem_cd, ' || uploadId || '                              from ' || tempProviderTableName || '  temp ';         EXECUTE 'alter table provider_dimension rename to ' || backupProviderTableName  ||'' ;             EXECUTE 'CREATE INDEX ' || interProviderTableName || '_id_idx ON ' || interProviderTableName  || '(Provider_Id,name_char)';     EXECUTE 'CREATE INDEX ' || interProviderTableName || '_uid_idx ON ' || interProviderTableName  || '(UPLOAD_ID)';                 EXECUTE 'alter table ' || interProviderTableName  || ' rename to provider_dimension' ; EXCEPTION         WHEN OTHERS THEN                 RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL;  ");
				logger.debug("Procedure 22");
				st.execute("CREATE OR REPLACE FUNCTION update_observation_fact (upload_temptable_name IN text, upload_id IN bigint, appendFlag IN bigint,     errorMsg OUT text) RETURNS text AS $body$ BEGIN          EXECUTE 'DELETE FROM ' || upload_temptable_name ||'  t1      WHERE oid > (select min(oid) FROM ' || upload_temptable_name ||' t2          WHERE t1.encounter_id = t2.encounter_id           AND         t1.encounter_id_source = t2.encounter_id_source         AND         t1.patient_id = t2.patient_id          AND          t1.patient_id_source = t2.patient_id_source         AND          t1.concept_cd = t2.concept_cd         AND          t1.start_date = t2.start_date         AND          coalesce(t1.modifier_cd,''xyz'') = coalesce(t2.modifier_cd,''xyz'')         AND          t1.instance_num = t2.instance_num         AND          t1.provider_id = t2.provider_id)';         EXECUTE 'DELETE FROM ' || upload_temptable_name ||'  t1                WHERE coalesce(t1.start_date::text, '''') = ''''      ';         EXECUTE 'UPDATE ' ||  upload_temptable_name     || ' SET encounter_num = (SELECT distinct em.encounter_num         FROM encounter_mapping em         WHERE em.encounter_ide = ' || upload_temptable_name||'.encounter_id         AND em.encounter_ide_source = '|| upload_temptable_name||'.encounter_id_source     )     WHERE EXISTS (SELECT distinct em.encounter_num         FROM encounter_mapping em         WHERE em.encounter_ide = '|| upload_temptable_name||'.encounter_id         AND em.encounter_ide_source = '||upload_temptable_name||'.encounter_id_source)';                         EXECUTE 'UPDATE ' ||  upload_temptable_name     || ' SET patient_num = (SELECT distinct pm.patient_num         FROM patient_mapping pm         WHERE pm.patient_ide = '|| upload_temptable_name||'.patient_id         AND pm.patient_ide_source = '|| upload_temptable_name||'.patient_id_source     )     WHERE EXISTS (SELECT distinct pm.patient_num          FROM patient_mapping pm         WHERE pm.patient_ide = '|| upload_temptable_name||'.patient_id         AND pm.patient_ide_source = '||upload_temptable_name||'.patient_id_source)';                         IF (appendFlag = 0) THEN                 EXECUTE 'INSERT INTO  archive_observation_fact          SELECT obsfact.*, ' || upload_id ||'         FROM observation_fact obsfact         WHERE obsfact.encounter_num IN          (SELECT temp_obsfact.encounter_num             FROM  ' ||upload_temptable_name ||' temp_obsfact             GROUP BY temp_obsfact.encounter_num           )';                 EXECUTE 'DELETE           FROM observation_fact          WHERE EXISTS (             SELECT archive.encounter_num             FROM archive_observation_fact  archive             WHERE archive.archive_upload_id = '||upload_id ||'             AND archive.encounter_num=observation_fact.encounter_num             AND archive.concept_cd = observation_fact.concept_cd             AND archive.start_date = observation_fact.start_date         )'; END IF; IF (appendFlag <> 0) THEN      EXECUTE '      UPDATE observation_fact f         SET valtype_cd = temp.valtype_cd ,     tval_char=temp.tval_char,      nval_num = temp.nval_num,     valueflag_cd=temp.valueflag_cd,     quantity_num=temp.quantity_num,     confidence_num=temp.confidence_num,     observation_blob =temp.observation_blob,     units_cd=temp.units_cd,     end_date=temp.end_date,     location_cd =temp.location_cd,     update_date=temp.update_date ,     download_date =temp.download_date,     import_date=temp.import_date,     sourcesystem_cd =temp.sourcesystem_cd,     upload_id = temp.upload_id      FROM ' || upload_temptable_name ||' temp     WHERE      temp.patient_num is not null      and temp.encounter_num is not null      and temp.encounter_num = f.encounter_num      and temp.patient_num = f.patient_num     and temp.concept_cd = f.concept_cd     and temp.start_date = f.start_date     and temp.provider_id = f.provider_id     and temp.modifier_cd = f.modifier_cd      and temp.instance_num = f.instance_num     and coalesce(f.update_date,to_date(''01-JAN-1900'',''DD-MON-YYYY'')) <= coalesce(temp.update_date,to_date(''01-JAN-1900'',''DD-MON-YYYY''))';      EXECUTE  'DELETE FROM ' || upload_temptable_name ||' temp WHERE EXISTS (SELECT 1          FROM observation_fact f          WHERE temp.patient_num is not null          and temp.encounter_num is not null          and temp.encounter_num = f.encounter_num          and temp.patient_num = f.patient_num         and temp.concept_cd = f.concept_cd         and temp.start_date = f.start_date         and temp.provider_id = f.provider_id         and temp.modifier_cd = f.modifier_cd          and temp.instance_num = f.instance_num     )';  END IF; EXECUTE 'INSERT INTO observation_fact(     encounter_num     ,concept_cd     , patient_num     ,provider_id     , start_date     ,modifier_cd     ,instance_num     ,valtype_cd     ,tval_char     ,nval_num     ,valueflag_cd     ,quantity_num     ,confidence_num     ,observation_blob     ,units_cd     ,end_date     ,location_cd     , update_date     ,download_date     ,import_date     ,sourcesystem_cd     ,upload_id) SELECT encounter_num ,concept_cd , patient_num ,provider_id , start_date ,modifier_cd ,instance_num ,valtype_cd ,tval_char ,nval_num ,valueflag_cd ,quantity_num ,confidence_num ,observation_blob ,units_cd ,end_date ,location_cd , update_date ,download_date ,Now() ,sourcesystem_cd ,temp.upload_id  FROM ' || upload_temptable_name ||' temp WHERE (temp.patient_num IS NOT NULL AND temp.patient_num::text <> '''') AND  (temp.encounter_num IS NOT NULL AND temp.encounter_num::text <> '''')';   EXCEPTION     WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 23");
				st.execute("CREATE OR REPLACE FUNCTION calculate_upload_status (uploadId IN bigint)  RETURNS VOID AS $body$ BEGIN      EXECUTE '       UPDATE upload_status      SET loaded_record = (         SELECT count(1)          FROM observation_fact obsfact          WHERE obsfact.upload_id= ' || uploadId ||')     WHERE upload_status.upload_id = '|| uploadId ||'';     EXECUTE 'UPDATE upload_status      SET no_of_record = (         SELECT count(1)          FROM temp_obsfact_'|| uploadId ||'     )     WHERE upload_status.upload_id = ' || uploadId ||'';     EXECUTE 'UPDATE upload_status      SET deleted_record = (         SELECT count(1)          FROM archive_observation_fact archiveobsfact          WHERE archiveobsfact.archive_upload_id= ' || uploadId ||'     )     WHERE upload_status.upload_id = ' || uploadId ||'';     EXCEPTION     WHEN OTHERS THEN         RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;                 END; $body$ LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 24");
				st.execute("CREATE OR REPLACE FUNCTION delete_upload_data (uploadId IN bigint)   RETURNS VOID AS $body$ BEGIN          EXECUTE '       DELETE      FROM observation_fact      WHERE upload_id = '|| uploadId ||'     ';         EXECUTE ' DELETE      FROM encounter_mapping      WHERE encounter_num IN (SELECT encounter_num          FROM visit_dimension          WHERE upload_id = '|| uploadId ||')     ';         EXECUTE ' DELETE      FROM visit_dimension      WHERE upload_id = '|| uploadId ||'     ';         EXECUTE ' UPDATE upload_status      SET load_status=''DELETED''      WHERE upload_id = '|| uploadId ||'     '; EXCEPTION         WHEN OTHERS THEN       RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;                 END; $body$ LANGUAGE PLPGSQL;");
				logger.debug("Procedure 25");
				st.execute("CREATE OR REPLACE FUNCTION insert_patient_map_fromtemp (tempPatientTableName IN text,  upload_id IN bigint,    errorMsg OUT text )   RETURNS text AS $body$ BEGIN          EXECUTE 'insert into patient_mapping (                 PERFORM distinct temp.patient_id, temp.patient_id_source,''A'',temp.patient_id ,' || upload_id || '                 from ' || tempPatientTableName ||'  temp                  where temp.patient_id_source = ''HIVE'' and                  not exists (select patient_ide from patient_mapping pm where pm.patient_num = temp.patient_id and pm.patient_ide_source = temp.patient_id_source)                  )';              EXECUTE 'MERGE  INTO patient_dimension pd                    USING ( select case when (ptemp.patient_id_source=''HIVE'') then  cast(ptemp.patient_id as int)                                        else pmap.patient_num end patient_num,                                   ptemp.VITAL_STATUS_CD,                                    ptemp.BIRTH_DATE,                                   ptemp.DEATH_DATE,                                    ptemp.SEX_CD ,                                   ptemp.AGE_IN_YEARS_NUM,                                   ptemp.LANGUAGE_CD,                                   ptemp.RACE_CD,                                   ptemp.MARITAL_STATUS_CD,                                   ptemp.RELIGION_CD,                                   ptemp.ZIP_CD,                                                                   ptemp.STATECITYZIP_PATH ,                                                                    ptemp.PATIENT_BLOB,                                                                    ptemp.UPDATE_DATE,                                                                    ptemp.DOWNLOAD_DATE,                                                                    ptemp.IMPORT_DATE,                                                                    ptemp.SOURCESYSTEM_CD                    from ' || tempPatientTableName || '  ptemp , patient_mapping pmap                    where   ptemp.patient_id = pmap.patient_ide(+)                    and ptemp.patient_id_source = pmap.patient_ide_source(+)            ) temp                    on (                                 pd.patient_num = temp.patient_num                     )                             when matched then                                  update  set                                          pd.VITAL_STATUS_CD= temp.VITAL_STATUS_CD,                     pd.BIRTH_DATE= temp.BIRTH_DATE,                     pd.DEATH_DATE= temp.DEATH_DATE,                     pd.SEX_CD= temp.SEX_CD,                     pd.AGE_IN_YEARS_NUM=temp.AGE_IN_YEARS_NUM,                     pd.LANGUAGE_CD=temp.LANGUAGE_CD,                     pd.RACE_CD=temp.RACE_CD,                     pd.MARITAL_STATUS_CD=temp.MARITAL_STATUS_CD,                     pd.RELIGION_CD=temp.RELIGION_CD,                     pd.ZIP_CD=temp.ZIP_CD,                                         pd.STATECITYZIP_PATH =temp.STATECITYZIP_PATH,                                         pd.PATIENT_BLOB=temp.PATIENT_BLOB,                                         pd.UPDATE_DATE=temp.UPDATE_DATE,                                         pd.DOWNLOAD_DATE=temp.DOWNLOAD_DATE,                                         pd.SOURCESYSTEM_CD=temp.SOURCESYSTEM_CD,                                         pd.UPLOAD_ID = '||upload_id||'                     where temp.update_date > pd.update_date                          when not matched then                                  insert (                                         PATIENT_NUM,                                         VITAL_STATUS_CD,                     BIRTH_DATE,                     DEATH_DATE,                     SEX_CD,                     AGE_IN_YEARS_NUM,                     LANGUAGE_CD,                     RACE_CD,                     MARITAL_STATUS_CD,                     RELIGION_CD,                     ZIP_CD,                                         STATECITYZIP_PATH,                                         PATIENT_BLOB,                                         UPDATE_DATE,                                         DOWNLOAD_DATE,                                         SOURCESYSTEM_CD,                                         import_date,                         upload_id                                         )                                  values (                                         temp.PATIENT_NUM,                                         temp.VITAL_STATUS_CD,                     temp.BIRTH_DATE,                     temp.DEATH_DATE,                     temp.SEX_CD,                     temp.AGE_IN_YEARS_NUM,                     temp.LANGUAGE_CD,                     temp.RACE_CD,                     temp.MARITAL_STATUS_CD,                     temp.RELIGION_CD,                     temp.ZIP_CD,                                         temp.STATECITYZIP_PATH,                                         temp.PATIENT_BLOB,                                         temp.UPDATE_DATE,                                         temp.DOWNLOAD_DATE,                                         temp.SOURCESYSTEM_CD,                                         LOCALTIMESTAMP,                                 '||upload_id||'                                 )'; EXCEPTION         WHEN OTHERS THEN                 RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;       END; $body$ LANGUAGE PLPGSQL;  ");
				logger.debug("Procedure 26");
				st.execute("CREATE OR REPLACE FUNCTION istableexists (tableName IN text)  RETURNS varchar AS $body$ DECLARE  flag varchar(10); countTableCur REFCURSOR; countTable varchar(1);   BEGIN      open countTableCur for EXECUTE 'SELECT count(1) FROM pg_catalogger.pg_class WHERE relname = '''||tableName||''' ' ;     LOOP         FETCH countTableCur INTO countTable;         IF countTable = '0'             THEN              flag := 'FALSE';             EXIT;         ELSE             flag := 'TRUE';             EXIT;     END IF;          END LOOP;     close countTableCur ;     return flag;      EXCEPTION WHEN OTHERS THEN     RAISE EXCEPTION 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;                     END;     $body$     LANGUAGE PLPGSQL; ");
				logger.debug("Procedure 27");
				connection.commit() ;
				connection.setTransactionIsolation( Connection.TRANSACTION_SERIALIZABLE ) ;
				connection.setAutoCommit( true ) ;
				insertProceduresDone = true ;
			}			
		}
		catch( Exception ex ) {
			if( connection != null ) try { connection.rollback() ; } catch( SQLException sqlx ) { ; }
			logger.error( "CreateDBPG.insertProcedures(): ", ex ) ;
			throw new UploaderException( "Error whilst inserting DB procedure ", ex ) ;
		}
		finally {
			exitTrace( "CreateDBPG.insertProcedures()" ) ;
		}
	}
	
	
	public void undeployFromJBoss( String projectId ) throws UploaderException {
		enterTrace( "CreateDBPG.undeployFromJBoss()" ) ;

		try {
			
			String fileName = projectId + "-ds.xml" ;						
			//
			// Delete old dodeploy control file if it exists.
			Files.deleteIfExists( Paths.get( jboss_deploy_dir 
					                       + System.getProperty( "file.separator") 
					                       + fileName 
					                       + ".dodeploy" ) ) ;

			//
			// Attempt to delete any old control file which says 
			// this has been already deployed...
			Files.deleteIfExists( Paths.get( jboss_deploy_dir 
					                       + System.getProperty( "file.separator") 
					                       + fileName 
					                       + ".deployed" ) ) ;
			
			//
			// Attempt to delete the real ds file...
			Files.deleteIfExists( Paths.get( jboss_deploy_dir 
					                       + System.getProperty( "file.separator") 
					                       + fileName ) ) ;
	
		}
		catch( Exception ex ) {
			String message = "Failed to undeploy from JBoss, project: " + projectId ;
			logger.error( message, ex ) ;
			throw new UploaderException( message, ex ) ;
		}
		finally {
			exitTrace( "CreateDBPG.undeployFromJBoss()" ) ;
		}
	}
	
	
	private void deployToJBoss( String projectId ) throws UploaderException {
		enterTrace( "CreateDBPG.deployToJBoss()" ) ;
		
		StringBuilder sb = new StringBuilder() ;
		try {
			//
			// Deploy the JBoss dataset definitions required by new project...

			//
			// Process the template into the buffer...
			InputStreamReader isr = new InputStreamReader( CreateDBPG.class.getResourceAsStream( "/project-template-ds.xml" ) ) ;
			BufferedReader br = new BufferedReader( isr) ;
			String s = null ;

			while( ( s = br.readLine()) != null ) {

				s = s.replaceAll("_projectId_", projectId ) ;
				s = s.replaceAll("_pg_db_url_", pg_db_url ) ;
				s = s.replaceAll("_pg_db_name_", pg_db_name ) ;
				
				s = s.replaceAll("_DB_SCHEMA_NAME_", projectId ) ;
				s = s.replaceAll("_DB_USER_NAME_", projectId ) ;	
				s = s.replaceAll("_DB_PASSWORD_", projectId ) ;

				sb.append(s).append( "\n" ) ;

			}
			br.close();
			
			//
			// Write the processed template into the JBoss deployment directory...
			String fileName = projectId + "-ds.xml" ;
			FileWriter fw = new FileWriter( jboss_deploy_dir 
					                      + System.getProperty( "file.separator") 
					                      + fileName ) ;
			
			fw.write( sb.toString() ) ;
			fw.flush() ;
			fw.close() ;
			
			//
			// Attempt to delete any old control file which says 
			// this has been already deployed...
			Files.deleteIfExists( Paths.get( jboss_deploy_dir 
					                       + System.getProperty( "file.separator") 
					                       + fileName 
					                       + ".deployed" ) ) ;
			
			//
			// Delete old dodeploy control file if it exists.
			// Then write the new control file which will trigger deployment...
			// ( The deletion and recreation are done as a precaution - 
			//   to ensure the creation date/time stamps are near for the two files ).
			Path doDeployPath = Paths.get( jboss_deploy_dir 
					                     + System.getProperty( "file.separator") 
					                     + fileName 
					                     + ".dodeploy" ) ;

			Files.deleteIfExists( doDeployPath ) ;
			Files.createFile( doDeployPath ) ;
		
		}
		catch( Exception ex ) {
			logger.error( "Failed to deploy to JBoss", ex ) ;
			throw new UploaderException( "Failed to deploy to JBoss", ex ) ;
		}
		finally {
			exitTrace( "CreateDBPG.deployToJBoss()" ) ;
		}
	}
	
	
	/**
	 * Utility routine to enter a structured message in the trace log that the given method 
	 * has been entered. 
	 * 
	 * @param entry: the name of the method entered
	 */
	public static void enterTrace( String entry ) {
		I2B2Project.enterTrace( logger, entry ) ;
	}

    /**
     * Utility routine to enter a structured message in the trace log that the given method 
	 * has been exited. 
	 * 
     * @param entry: the name of the method exited
     */
    public static void exitTrace( String entry ) {
    	I2B2Project.exitTrace( logger, entry ) ;
	}

}
