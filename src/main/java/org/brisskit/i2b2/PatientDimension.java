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
public class PatientDimension {

	
	private static Logger logger = Logger.getLogger( PatientDimension.class ) ;
	
	public static final String PATIENT_DIM_INSERT_SQL_KEY = "PATIENT_DIM_INSERT_SQL" ;
	public static final String PATIENT_DIM_INSERT_SQL = 
			"INSERT INTO PATIENT_DIMENSION" +
	               "( PATIENT_NUM" +      
			       ", VITAL_STATUS_CD" + 
			       ", BIRTH_DATE" +
			       ", DEATH_DATE" + 
			       ", SEX_CD" +
			       ", AGE_IN_YEARS_NUM" +
			       ", LANGUAGE_CD" +
			       ", RACE_CD" +
			       ", MARITAL_STATUS_CD" +
			       ", RELIGION_CD" +
			       ", ZIP_CD" + 			//  VARCHAR(10) NULL,
			       ", STATECITYZIP_PATH" + 	//	VARCHAR(700) NULL,
			       ", INCOME_CD" + 			//	VARCHAR(50) NULL,
			       ", PATIENT_BLOB" + 		//  TEXT NULL,
			       ", UPDATE_DATE" + 		//  TIMESTAMP NULL,
			       ", DOWNLOAD_DATE" + 		//  TIMESTAMP NULL,
			       ", IMPORT_DATE" + 		//  TIMESTAMP NULL,
			       ", SOURCESYSTEM_CD" + 	//  VARCHAR(50) NULL,
			       ", UPLOAD_ID" + 			//  INT NULL, 		   
				   ") " +
			 "VALUES( ?" +
				   ", ?" +
				   ", ?" +
				   ", ?" +         
				   ", ?" +   
				   ", ?" +
				   ", ?" +
				   ", ?" +
				   ", ?" +
				   ", ?" +
				   ", ?" +
				   ", ?" +
				   ", ?" +
				   ", NULL" +			// patient blob
				   ", now()" +
				   ", now()" +
				   ", now()" +
				   ", ?" +
				   ", NULL ) ;" ;		// upload id
	
	public static final String PATIENT_DIM_SELECT_SQL_KEY = "PATIENT_DIM_SELECT_SQL" ;
	public static final String PATIENT_DIM_SELECT_SQL =
			"SELECT COUNT(*) FROM PATIENT_DIMENSION WHERE PATIENT_NUM = ?"  ;			
	
	private ProjectUtils utils ;

	private String schema_name ;
	
	private Integer patient_num = null ;
	private String vital_status_cd = null ;
	private Date birth_date = null ;
	private Date death_date = null ;
	private String sex_cd = null ;
	private Integer age_in_years = null ;
	private String language_cd = null ;
	private String race_cd = null ;
	private String marital_status_cd = null ;
	private String religion_cd = null ;
	private String zip_cd = null ;
	private String statecityzip_path = null ;
	private String income_cd = null ;
	private String sourcesystem_cd = null ;

	
	public PatientDimension( ProjectUtils utils ) {
		this.utils = utils ;
	}
	
	
	public void serializeToDatabase() throws UploaderException {
		enterTrace( "PatientDimension.serializeToDatabase()" ) ;
		try {
			PreparedStatement ps = 
					utils.getPsHolder()
						 .getPreparedStatement( PatientDimension.PATIENT_DIM_INSERT_SQL_KEY ) ;
			ps.setInt( 1, patient_num ) ;
			if( vital_status_cd == null ) {
				ps.setNull( 2, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 2, vital_status_cd ) ;
			}
			if( birth_date == null ) {
				ps.setNull( 3, java.sql.Types.TIMESTAMP ) ;
			}
			else {
				ps.setTimestamp( 3, new java.sql.Timestamp( birth_date.getTime() ) ) ;
			}
			if( death_date == null ) {
				ps.setNull( 4, java.sql.Types.TIMESTAMP ) ;
			}
			else {
				ps.setTimestamp( 4, new java.sql.Timestamp( death_date.getTime() ) ) ;
			}
			if( sex_cd == null ) {
				ps.setNull( 5, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 5, sex_cd ) ;
			}
			if( age_in_years == null ) {
				ps.setNull( 6, java.sql.Types.INTEGER ) ;
			}
			else {
				ps.setInt( 6, age_in_years ) ;
			}
			if( language_cd == null ) {
				ps.setNull( 7, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 7, language_cd ) ;
			}
			if( race_cd == null ) {
				ps.setNull( 8, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 8, race_cd ) ;
			}
			if( marital_status_cd == null ) {
				ps.setNull( 9, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 9, marital_status_cd ) ;
			}
			if( marital_status_cd == null ) {
				ps.setNull( 9, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 9, marital_status_cd ) ;
			}
			if( religion_cd == null ) {
				ps.setNull( 10, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 10, religion_cd ) ;
			}
			if( zip_cd == null ) {
				ps.setNull( 11, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 11, zip_cd ) ;
			}
			if( statecityzip_path == null ) {
				ps.setNull( 12, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 12, statecityzip_path ) ;
			}
			if( income_cd == null ) {
				ps.setNull( 13, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 13, income_cd ) ;
			}
			if( sourcesystem_cd == null ) {
				ps.setNull( 14, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 14, sourcesystem_cd ) ;
			}
			ps.executeUpdate() ;
		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert into patient dimension.", sqlx ) ;
		}
		finally {
			exitTrace( "PatientDimension.serializeToDatabase()" ) ;
		}
	}
	
	public boolean patientExists() throws UploaderException {
		enterTrace( "PatientDimension.patientExists()" ) ;
		boolean exists = false ;
		try {
			//
			// See whether the appropriate patient already exists in the db...
			PreparedStatement ps =
					utils.getPsHolder()
						 .getPreparedStatement( PatientDimension.PATIENT_DIM_SELECT_SQL_KEY ) ;
			ps.setInt( 1, patient_num ) ;					
		    ResultSet rs = ps.executeQuery() ;
		    if( rs.next() ) {
		    	int count = rs.getInt(1) ;
		    	if( count > 0 ) {
		    		exists = true ;
		    	}			 
				rs.close() ;
		    }
		    else {
		    	String message = "Failed to detect whether the appropriate patient already exists in the db.\n" +
		    			         " Retrieved no result." ;
				logger.error( message ) ;
				throw new UploaderException( message ) ;
		    }
			return exists ;
		}
		catch( SQLException sqlx ) {
			String message = "Failed to detect whether the appropriate patient already exists in the db." ;
			logger.error( message, sqlx ) ;
			throw new UploaderException( message, sqlx ) ;
		}
		finally {
			exitTrace( "PatientDimension.patientExists()()" ) ;
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


	public void setSchema_name(String schema_name) {
		this.schema_name = schema_name;
	}


	public void setPatient_num(Integer patient_num) {
		this.patient_num = patient_num;
	}
	
	public Integer getPatient_num() {
		return this.patient_num ;
	}


	public void setVital_status_cd(String vital_status_cd) {
		this.vital_status_cd = vital_status_cd;
	}


	public void setBirth_date(Date birth_date) {
		this.birth_date = birth_date;
	}


	public void setDeath_date(Date death_date) {
		this.death_date = death_date;
	}


	public void setSex_cd(String sex_cd) {
		this.sex_cd = sex_cd;
	}


	public void setAge_in_years(Integer age_in_years) {
		this.age_in_years = age_in_years;
	}


	public void setLanguage_cd(String language_cd) {
		this.language_cd = language_cd;
	}


	public void setRace_cd(String race_cd) {
		this.race_cd = race_cd;
	}


	public void setMarital_status_cd(String marital_status_cd) {
		this.marital_status_cd = marital_status_cd;
	}


	public void setReligion_cd(String religion_cd) {
		this.religion_cd = religion_cd;
	}


	public void setZip_cd(String zip_cd) {
		this.zip_cd = zip_cd;
	}


	public void setStatecityzip_path(String statecityzip_path) {
		this.statecityzip_path = statecityzip_path;
	}


	public void setIncome_cd(String income_cd) {
		this.income_cd = income_cd;
	}


	public void setSourcesystem_cd(String sourcesystem_cd) {
		this.sourcesystem_cd = sourcesystem_cd;
	}

}
