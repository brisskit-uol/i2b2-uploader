/**
 * 
 */
package org.brisskit.i2b2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author jeff
 *
 */
public class PatientMapping {

	private static Log log = LogFactory.getLog( PatientMapping.class ) ;
	
	public static final String PATIENT_MAP_INSERT_COMMAND = 
			"SET SCHEMA '<DB_SCHEMA_NAME>';" +
			"" +
			"INSERT INTO <DB_SCHEMA_NAME>.PATIENT_MAPPING" +
			    "( PATIENT_IDE" + 			//  VARCHAR(200)  NOT NULL,
			    ", PATIENT_IDE_SOURCE" + 	//	VARCHAR(50)  NOT NULL,
			    ", PATIENT_NUM" + 			//  INT NOT NULL,
			    ", PATIENT_IDE_STATUS" + 	//	VARCHAR(50) NULL,
			    ", PROJECT_ID" + 			//  VARCHAR(50) NOT NULL,
			    ", UPLOAD_DATE" + 			//  TIMESTAMP NULL,
			    ", UPDATE_DATE" + 			//  TIMESTAMP NULL,
			    ", DOWNLOAD_DATE" + 		//  TIMESTAMP NULL,
			    ", IMPORT_DATE" + 			//  TIMESTAMP NULL,
			    ", SOURCESYSTEM_CD" + 		//  VARCHAR(50) NULL,
			    ", UPLOAD_ID ) " + 			//  INT NULL,
			"VALUES" +
			   "( <PATIENT_IDE>" +
			   ", <PATIENT_IDE_SOURCE>" +
			   ", <PATIENT_NUM>" +
			   ", <PATIENT_IDE_STATUS>" +         
			   ", <PROJECT_ID>" +   
			   ", now()" +
			   ", now()" +
			   ", now()" +
			   ", now()" +
			   ", <SOURCESYSTEM_CD>" +
			   ", NULL ) ;" ;				// upload id
	
	private ProjectUtils utils ;
	
	private String schema_name = null ;
	
	private String patient_ide = null ;
	private String patient_ide_source = null ;
	private Integer patient_num = null ;
	private String patient_ide_status = null ;
	private String project_id = null ;
	private String sourcesystem_id = null ;
	
	public PatientMapping( ProjectUtils utils ) {
		this.utils = utils ;
	}
	
	
	public void serializeToDatabase( Connection connection ) throws UploaderException {
		enterTrace( "PatientMapping.serializeToDatabase()" ) ;
		try {

			String sqlCmd = PATIENT_MAP_INSERT_COMMAND ;
			
			sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", schema_name ) ;
			
			sqlCmd = sqlCmd.replace( "<PATIENT_IDE>", utils.enfoldString( patient_ide ) ) ;
			sqlCmd = sqlCmd.replace( "<PATIENT_IDE_SOURCE>", utils.enfoldString( patient_ide_source ) ) ;
			sqlCmd = sqlCmd.replace( "<PATIENT_NUM>", utils.enfoldInteger( patient_num ) ) ;
			sqlCmd = sqlCmd.replace( "<PATIENT_IDE_STATUS>", utils.enfoldNullableString( patient_ide_status ) ) ;
			sqlCmd = sqlCmd.replace( "<PROJECT_ID>", utils.enfoldString( project_id ) ) ;
			// UPLOAD_DATE		defaults to now()
		    // UPDATE_DATE 		defaults to now()
		    // DOWNLOAD_DATE 	defaults to now()
		    // IMPORT_DATE 		defaults to now()
			sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( sourcesystem_id ) ) ;
			// UPLOAD_ID 		missed out
			
			Statement st = connection.createStatement();
			
			st.execute( sqlCmd ) ;

		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert into patient mapping.", sqlx ) ;
		}
		finally {
			exitTrace( "PatientMapping.serializeToDatabase()" ) ;
		}
	}
	
	
	/**
	 * Utility routine to enter a structured message in the trace log that the given method 
	 * has been entered. 
	 * 
	 * @param entry: the name of the method entered
	 */
	public static void enterTrace( String entry ) {
		I2B2Project.enterTrace( log, entry ) ;
	}

    /**
     * Utility routine to enter a structured message in the trace log that the given method 
	 * has been exited. 
	 * 
     * @param entry: the name of the method exited
     */
    public static void exitTrace( String entry ) {
    	I2B2Project.exitTrace( log, entry ) ;
	}


	public void setSchema_name(String schema_name) {
		this.schema_name = schema_name;
	}


	public void setPatient_ide(String patient_ide) {
		this.patient_ide = patient_ide;
	}
	
	public String getPatient_ide() {
		return this.patient_ide ;
	}


	public void setPatient_ide_source(String patient_ide_source) {
		this.patient_ide_source = patient_ide_source;
	}


	public void setPatient_num(Integer patient_num) {
		this.patient_num = patient_num;
	}
	
	public Integer getPatient_num() {
		return this.patient_num ;
	}


	public void setPatient_ide_status(String patient_ide_status) {
		this.patient_ide_status = patient_ide_status;
	}


	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}


	public void setSourcesystem_id(String sourcesystem_id) {
		this.sourcesystem_id = sourcesystem_id;
	}
}
