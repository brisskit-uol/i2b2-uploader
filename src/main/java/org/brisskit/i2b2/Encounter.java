/**
 * 
 */
package org.brisskit.i2b2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author jeff
 *
 */
public class Encounter {

	private static Log log = LogFactory.getLog( Encounter.class ) ;
	
	public static final String ENCOUNTER_MAP_INSERT_COMMAND = 
			"SET SCHEMA '<DB_SCHEMA_NAME>';" +
			"" +
			"INSERT INTO <DB_SCHEMA_NAME>.ENCOUNTER_MAPPING" +  
			    "( ENCOUNTER_IDE" +
			    ", ENCOUNTER_IDE_SOURCE" +
			    ", PROJECT_ID" +
			    ", PATIENT_IDE" +    
			    ", PATIENT_IDE_SOURCE" +
			    ", ENCOUNTER_IDE_STATUS" +
			    ", UPLOAD_DATE" +
			    ", UPDATE_DATE" +
			    ", DOWNLOAD_DATE" +
			    ", IMPORT_DATE" +
			    ", SOURCESYSTEM_CD" +
			    ", UPLOAD_ID ) " +
			"VALUES" +
			   "( <ENCOUNTER_IDE>" +
			   ", <ENCOUNTER_IDE_SOURCE>" +
			   ", <PROJECT_ID>" + 
			   ", <PATIENT_IDE>" +
			   ", <PATIENT_IDE_SOURCE>" +
			   ", NULL" +  						// patient_ide_status       
			   ", now()" +
			   ", now()" +
			   ", now()" +
			   ", now()" +
			   ", <SOURCESYSTEM_CD>" +
			   ", NULL ) ;" ;	
	
	
	public static final String VISIT_DIM_INSERT_COMMAND = 
			"SET SCHEMA '<DB_SCHEMA_NAME>';" +
			"" +
			"INSERT INTO <DB_SCHEMA_NAME>.VISIT_DIMENSION" +
			       "( ENCOUNTER_NUM" + 			// INT NOT NULL,
				   ", PATIENT_NUM" + 			// INT NOT NULL,
				   ", ACTIVE_STATUS_CD" + 		// VARCHAR(50) NULL,
				   ", START_DATE" + 			// TIMESTAMP NULL,
				   ", END_DATE" + 				// TIMESTAMP NULL,
				   ", INOUT_CD" + 				// VARCHAR(50) NULL,
				   ", LOCATION_CD" + 			// VARCHAR(50) NULL,
				   ", LOCATION_PATH" + 			// VARCHAR(900) NULL,
				   ", LENGTH_OF_STAY" + 		// INT NULL,
				   ", VISIT_BLOB" + 			// TEXT NULL,
				   ", UPDATE_DATE" + 			// TIMESTAMP NULL,
				   ", DOWNLOAD_DATE" + 			// TIMESTAMP NULL,
				   ", IMPORT_DATE" + 			// TIMESTAMP NULL,
				   ", SOURCESYSTEM_CD" + 		// VARCHAR(50) NULL ,
				   ", UPLOAD_ID ) " +      		// INT NULL, 
			"VALUES ( <ENCOUNTER_NUM>" +
	               ", <PATIENT_NUM>" +
	               ", NULL" +		  			// active_status_cd	
	               ", <START_DATE>" +
	               ", NULL" +    				// end_date 
	               ", NULL" +					// inout_cd
	               ", NULL" +					// location_cd
	               ", NULL" + 					// location_path
	               ", NULL" +					// length_of_stay
	               ", NULL" +					// visit_blob
	               ", now()" +
	               ", now()" +
	               ", now()" +
	               ", <SOURCESYSTEM_CD>" +
	               ", NULL ) ;" ;				// upload id
	
	
	private ProjectUtils utils ;
	
	private String schema_name = null ;
	
	private String encounter_ide = null ;
	private String encounter_ide_source = null ;
	private Integer encounter_num = null ;
	private String encounter_ide_status = null ;
					
	private String patient_ide = null ;
	private String patient_ide_source = null ;
	private Integer patient_num = null ;
	
	private String project_id = null ;
	private String sourcesystem_id = null ;
	
	private Date startDate = null ;
	
	public Encounter( ProjectUtils utils ) {
		this.utils = utils ;
	}
	
	
	public void serializeToDatabase( Connection connection ) throws UploaderException {
		enterTrace( "Encounter.serializeToDatabase()" ) ;
		try {
			//
			// Do the encounter mapping first...
			String sqlCmd = ENCOUNTER_MAP_INSERT_COMMAND ;			
			sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", schema_name ) ;	
			sqlCmd = sqlCmd.replace( "<ENCOUNTER_IDE>", utils.enfoldString( encounter_ide ) ) ;
			sqlCmd = sqlCmd.replace( "<ENCOUNTER_IDE_SOURCE>", utils.enfoldString( encounter_ide_source ) ) ;
			sqlCmd = sqlCmd.replace( "<PROJECT_ID>", utils.enfoldString( project_id ) ) ;
			sqlCmd = sqlCmd.replace( "<PATIENT_IDE>", utils.enfoldString( patient_ide ) ) ;
			sqlCmd = sqlCmd.replace( "<PATIENT_IDE_SOURCE>", utils.enfoldString( patient_ide_source ) ) ;			
			sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( sourcesystem_id ) ) ;			
			Statement st = connection.createStatement();			
			st.execute( sqlCmd ) ;
			ResultSet rs = st.executeQuery( "select currval( 'ENCOUNTER_MAPPING_ENCOUNTER_NUM_seq' );" ) ;
			rs.next();
			encounter_num = rs.getInt(1) ;		
			rs.close() ;
			//
			// Do the visit dimension second...
			sqlCmd = VISIT_DIM_INSERT_COMMAND ;			
			sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", schema_name ) ;	
			sqlCmd = sqlCmd.replace( "<ENCOUNTER_NUM>", utils.enfoldInteger( encounter_num ) ) ;
			sqlCmd = sqlCmd.replace( "<PATIENT_NUM>", utils.enfoldInteger( patient_num ) ) ;
			sqlCmd = sqlCmd.replace( "<START_DATE>", utils.enfoldDate( this.startDate ) ) ;			
			sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( sourcesystem_id ) ) ;			
//			st = connection.createStatement();			
			st.execute( sqlCmd ) ;
		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert into patient mapping.", sqlx ) ;
		}
		finally {
			exitTrace( "Encounter.serializeToDatabase()" ) ;
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


	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}


	public void setSourcesystem_id(String sourcesystem_id) {
		this.sourcesystem_id = sourcesystem_id;
	}


	public String getEncounter_ide() {
		return encounter_ide;
	}


	public void setEncounter_ide(String encounter_ide) {
		this.encounter_ide = encounter_ide;
	}


	public String getEncounter_ide_source() {
		return encounter_ide_source;
	}


	public void setEncounter_ide_source(String encounter_ide_source) {
		this.encounter_ide_source = encounter_ide_source;
	}


	public Integer getEncounter_num() {
		return encounter_num;
	}


	public void setEncounter_num(Integer encounter_num) {
		this.encounter_num = encounter_num;
	}


	public String getEncounter_ide_status() {
		return encounter_ide_status;
	}


	public void setEncounter_ide_status(String encounter_ide_status) {
		this.encounter_ide_status = encounter_ide_status;
	}


	public Date getStartDate() {
		return startDate;
	}


	public void setStartDate( Date encounterStartDate ) {
		this.startDate = encounterStartDate;
	}
}
