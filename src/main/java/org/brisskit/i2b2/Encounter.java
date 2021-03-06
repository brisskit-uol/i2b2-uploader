/**
 * 
 */
package org.brisskit.i2b2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.* ;

/**
 * @author jeff
 *
 */
public class Encounter {

	private static Logger logger = Logger.getLogger( Encounter.class ) ;
	
	public static final String ENCOUNTER_MAP_INSERT_SQL_KEY = "ENCOUNTER_MAP_INSERT_SQL" ; 
	public static final String ENCOUNTER_MAP_INSERT_SQL = 
			"INSERT INTO ENCOUNTER_MAPPING" +  
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
			   "( ?" +
			   ", ?" +
			   ", ?" + 
			   ", ?" +
			   ", ?" +
			   ", NULL" +  						// patient_ide_status       
			   ", now()" +
			   ", now()" +
			   ", now()" +
			   ", now()" +
			   ", ?" +
			   ", NULL ) ;" ;	
	
	
	public static final String VISIT_DIM_INSERT_SQL_KEY = "VISIT_DIM_INSERT_SQL" ;
	public static final String VISIT_DIM_INSERT_SQL = 
			"INSERT INTO VISIT_DIMENSION" +
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
			"VALUES ( ?" +
	               ", ?" +
	               ", NULL" +		  			// active_status_cd	
	               ", ?" +
	               ", NULL" +    				// end_date 
	               ", NULL" +					// inout_cd
	               ", NULL" +					// location_cd
	               ", NULL" + 					// location_path
	               ", NULL" +					// length_of_stay
	               ", NULL" +					// visit_blob
	               ", now()" +
	               ", now()" +
	               ", now()" +
	               ", ?" +
	               ", NULL ) ;" ;				// upload id
	
	
	private ProjectUtils utils ;
	
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
	
	
	public void serializeToDatabase() throws UploaderException {
		enterTrace( "Encounter.serializeToDatabase()" ) ;
		try {
			//
			// Do the encounter mapping first...
			PreparedStatement ps =
					utils.getPsHolder()
						 .getPreparedStatement( Encounter.ENCOUNTER_MAP_INSERT_SQL_KEY ) ;
			ps.setString( 1, encounter_ide ) ;
			ps.setString( 2, encounter_ide_source ) ;
			ps.setString( 3, project_id ) ;
			ps.setString( 4, patient_ide ) ;
			ps.setString( 5, patient_ide_source ) ;		
			ps.setString( 6, sourcesystem_id ) ;
			ps.executeUpdate() ;
			ResultSet rs = ps.getGeneratedKeys() ;
			rs.next();
			encounter_num = rs.getInt(4) ;
//			logger.debug( "Autogenerated encounter_num: " + encounter_num ) ;
			rs.close() ;
			//
			// Do the visit dimension second...
			ps = utils.getPsHolder()
					  .getPreparedStatement( Encounter.VISIT_DIM_INSERT_SQL_KEY ) ;
			ps.setInt( 1, encounter_num ) ;
			ps.setInt( 2, patient_num ) ;
			ps.setTimestamp( 3, new java.sql.Timestamp( this.startDate.getTime() ) ) ;
			ps.setString( 4, sourcesystem_id ) ;		
			ps.executeUpdate() ;
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
