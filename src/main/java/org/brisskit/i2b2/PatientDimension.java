/**
 * 
 */
package org.brisskit.i2b2;

import java.sql.Connection;
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
	
	public static final String PATIENT_DIM_INSERT_COMMAND = 
			"SET SCHEMA '<DB_SCHEMA_NAME>';" +
			"" +
			"INSERT INTO <DB_SCHEMA_NAME>.PATIENT_DIMENSION" +
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
			 "VALUES( <PATIENT_NUM>" +
				   ", <VITAL_STATUS_CD>" +
				   ", <BIRTH_DATE>" +
				   ", <DEATH_DATE>" +         
				   ", <SEX_CD>" +   
				   ", <AGE_IN_YEARS_NUM>" +
				   ", <LANGUAGE_CD>" +
				   ", <RACE_CD>" +
				   ", <MARITAL_STATUS_CD>" +
				   ", <RELIGION_CD>" +
				   ", <ZIP_CD>" +
				   ", <STATECITYZIP_PATH>" +
				   ", <INCOME_CD>" +
				   ", NULL" +			// patient blob
				   ", now()" +
				   ", now()" +
				   ", now()" +
				   ", <SOURCESYSTEM_CD>" +
				   ", NULL ) ;" ;		// upload id
	
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
	
	
	public void serializeToDatabase( Connection connection ) throws UploaderException {
		enterTrace( "PatientDimension.serializeToDatabase()" ) ;
		try {

			String sqlCmd = PATIENT_DIM_INSERT_COMMAND ;
			
			sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", schema_name ) ;
					
			sqlCmd = sqlCmd.replace( "<PATIENT_NUM>", utils.enfoldInteger( patient_num ) ) ;
			sqlCmd = sqlCmd.replace( "<VITAL_STATUS_CD>", utils.enfoldNullableString( vital_status_cd ) ) ;
			sqlCmd = sqlCmd.replace( "<BIRTH_DATE>", utils.enfoldNullableDate( birth_date ) ) ;
			sqlCmd = sqlCmd.replace( "<DEATH_DATE>", utils.enfoldNullableDate( death_date ) ) ;
			sqlCmd = sqlCmd.replace( "<SEX_CD>", utils.enfoldNullableString( sex_cd ) ) ;
			sqlCmd = sqlCmd.replace( "<AGE_IN_YEARS_NUM>", utils.enfoldNullableInteger( age_in_years ) ) ;
			sqlCmd = sqlCmd.replace( "<LANGUAGE_CD>", utils.enfoldNullableString( language_cd ) ) ;
			sqlCmd = sqlCmd.replace( "<RACE_CD>", utils.enfoldNullableString( race_cd ) ) ;
			sqlCmd = sqlCmd.replace( "<MARITAL_STATUS_CD>",  utils.enfoldNullableString( marital_status_cd ) ) ;
			sqlCmd = sqlCmd.replace( "<RELIGION_CD>",  utils.enfoldNullableString( religion_cd ) ) ;
			sqlCmd = sqlCmd.replace( "<ZIP_CD>",  utils.enfoldNullableString( zip_cd ) ) ;
			sqlCmd = sqlCmd.replace( "<STATECITYZIP_PATH>",  utils.enfoldNullableString( statecityzip_path ) ) ;
			sqlCmd = sqlCmd.replace( "<INCOME_CD>",  utils.enfoldNullableString( income_cd ) ) ;
			// PATIENT_BLOB   missed out
			// UPDATE_DATE    defaults to now()
			// DOWNLOAD_DATE  defaults to now()
			// IMPORT_DATE    defaults to now()		
			sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>",  utils.enfoldNullableString( sourcesystem_cd ) ) ;
			// UPLOAD_ID missed out
			
			Statement st = connection.createStatement();
			
			st.execute( sqlCmd ) ;

		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert into patient dimension.", sqlx ) ;
		}
		finally {
			exitTrace( "PatientDimension.serializeToDatabase()" ) ;
		}
	}
	
	public boolean patientExists( Connection connection ) throws UploaderException {
		enterTrace( "PatientDimension.patientExists()" ) ;
		boolean exists = false ;
		try {
			//
			// See whether the appropriate patient already exists in the db...
			Statement st = connection.createStatement() ;
			st.executeQuery( "select COUNT(*) from " + schema_name + ".PATIENT_DIMENSION "  
				           + " where " 
				           + " PATIENT_NUM = '" + patient_num + "' ;" ) ;			
		    ResultSet rs = st.getResultSet() ;
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
