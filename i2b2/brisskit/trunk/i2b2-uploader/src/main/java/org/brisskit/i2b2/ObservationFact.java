/**
 * 
 */
package org.brisskit.i2b2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.brisskit.i2b2.OntologyBranch.Type;

/**
 * @author jeff
 *
 */
public class ObservationFact {
	
private static Logger logger = Logger.getLogger( ObservationFact.class ) ;

public static final String OBSERVATION_FACT_INSERT_COMMAND = 
		"SET SCHEMA '<DB_SCHEMA_NAME>';" +
		"" +
		"INSERT INTO <DB_SCHEMA_NAME>.OBSERVATION_FACT" +
               "( " +
               "  ENCOUNTER_NUM" +    		// INT NOT NULL,
               ", PATIENT_NUM   " +  		// INT NOT NULL,
               ", CONCEPT_CD    " +  		// VARCHAR(50) NOT NULL,
               ", PROVIDER_ID   " +  		// VARCHAR(50) NOT NULL,
               ", START_DATE    " +  		// TIMESTAMP NOT NULL,
               ", VALTYPE_CD    " +  		// VARCHAR(50) NULL,
               ", TVAL_CHAR     " +  		// VARCHAR(255) NULL,
               ", NVAL_NUM      " +  		// DECIMAL(18,5) NULL,
               ", VALUEFLAG_CD  " +  		// VARCHAR(50) NULL,
               ", QUANTITY_NUM  " +  		// DECIMAL(18,5) NULL,
               ", UNITS_CD      " +  		// VARCHAR(50) NULL,
               ", END_DATE      " +  		// TIMESTAMP NULL,
               ", LOCATION_CD   " +  		// VARCHAR(50) NULL,
               ", OBSERVATION_BLOB" + 		// TEXT NULL,
               ", CONFIDENCE_NUM" +  		// DECIMAL(18,5) NULL,
               ", UPDATE_DATE   " +  		// TIMESTAMP NULL,
               ", DOWNLOAD_DATE " +  		// TIMESTAMP NULL,
               ", IMPORT_DATE   " +  		// TIMESTAMP NULL,
               ", SOURCESYSTEM_CD" + 		// VARCHAR(50) NULL, 
	           ", UPLOAD_ID" +          	// INT NULL,  
			   ") " +
		 "VALUES( " +
		       "  <ENCOUNTER_NUM>" +
			   ", <PATIENT_NUM>" +
			   ", <CONCEPT_CD>" +
			   ", <PROVIDER_ID>" +         
			   ", <START_DATE>" +   
			   ", <VALTYPE_CD>" +
			   ", <TVAL_CHAR>" +
			   ", <NVAL_NUM>" +
			   ", <VALUEFLAG_CD>" +
			   ", <QUANTITY_NUM>" +
			   ", <UNITS_CD>" +
			   ", <END_DATE>" +
			   ", <LOCATION_CD>" +
			   ", NULL" +					// observation blob
			   ", <CONFIDENCE_NUM>" +
			   ", now()" +
			   ", now()" +
			   ", now()" +
			   ", <SOURCESYSTEM_CD>" +
			   ", NULL ) ;" ;				// upload id

private ProjectUtils utils ;

private String schema_name ;
//
// Observation Fact columns...
private Integer encounter_num = null ;
private Integer patient_num = null ;
private String concept_cd = null ;
private String provider_id = null ;
private Date start_date = null ;
private String valtype_cd = null ;
private String tval_char = null ;
private Double nval_num = null ;
private String valueflag_cd = null ;
private Double quantity_num = null ;
private String units_cd = null ;
private Date end_date = null ;
private String location_cd = null ;
private Double confidence_num = null ;
private String sourcesystem_cd = null ;


public ObservationFact( ProjectUtils utils ) {
	this.utils = utils ;
}

public void serializeToDatabase( Connection connection ) throws UploaderException {
	enterTrace( "ObservationFact.serializeToDatabase()" ) ;
	
	String sqlCmd = OBSERVATION_FACT_INSERT_COMMAND ;
	
	try {
		
		sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", schema_name ) ;
		
		sqlCmd = sqlCmd.replace( "<ENCOUNTER_NUM>", utils.enfoldInteger( encounter_num ) ) ;		
		sqlCmd = sqlCmd.replace( "<PATIENT_NUM>", utils.enfoldInteger( patient_num ) ) ;
		sqlCmd = sqlCmd.replace( "<CONCEPT_CD>", utils.enfoldString( concept_cd ) ) ;
		sqlCmd = sqlCmd.replace( "<PROVIDER_ID>", utils.enfoldString( provider_id ) ) ;		
		sqlCmd = sqlCmd.replace( "<START_DATE>", utils.enfoldDate( start_date ) ) ;
		// MODIFIER_CD  missed out ( not null but has a default value of '@' )
		// INSTANCE_NUM missed out ( not null but has a default value of 1 )
		sqlCmd = sqlCmd.replace( "<VALTYPE_CD>", utils.enfoldNullableString( valtype_cd ) ) ;
		sqlCmd = sqlCmd.replace( "<TVAL_CHAR>", utils.enfoldNullableString( tval_char ) ) ;
		sqlCmd = sqlCmd.replace( "<NVAL_NUM>", utils.enfoldNullableDecimal( nval_num ) ) ;
		sqlCmd = sqlCmd.replace( "<VALUEFLAG_CD>", utils.enfoldNullableString( valueflag_cd ) ) ;
		sqlCmd = sqlCmd.replace( "<QUANTITY_NUM>", utils.enfoldNullableDecimal( quantity_num ) ) ;
		sqlCmd = sqlCmd.replace( "<UNITS_CD>", utils.enfoldNullableString( units_cd ) ) ;		
		sqlCmd = sqlCmd.replace( "<END_DATE>", utils.enfoldNullableDate( end_date ) ) ;		
		sqlCmd = sqlCmd.replace( "<LOCATION_CD>", utils.enfoldNullableString( location_cd ) ) ;
		// OBSERVATION_BLOB missed out
		sqlCmd = sqlCmd.replace( "<CONFIDENCE_NUM>", utils.enfoldNullableDecimal( confidence_num ) ) ;
		// UPDATE_DATE    defaults to now()
		// DOWNLOAD_DATE  defaults to now()
		// IMPORT_DATE    defaults to now()		
		sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( sourcesystem_cd ) ) ;
		// UPLOAD_ID missed out
		
		Statement st = connection.createStatement();
		
		st.execute( sqlCmd ) ;

	}
	catch( SQLException sqlx ) {
		logger.error( sqlx.getStackTrace() ) ;
		throw new UploaderException( "Failed to insert into observation fact table:\n" + sqlCmd, sqlx ) ;
	}
	finally {
		exitTrace( "ObservationFact.serializeToDatabase()" ) ;
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

public void setEncounter_num(Integer encounter_num) {
	this.encounter_num = encounter_num;
}

public void setPatient_num(Integer patient_num) {
	this.patient_num = patient_num;
}

public void setConcept_cd(String concept_cd) {
	this.concept_cd = concept_cd;
}

public void setProvider_id(String provider_id) {
	this.provider_id = provider_id;
}

public void setStart_date(Date start_date) {
	this.start_date = start_date;
}

public void setValtype_cd(String valtype_cd) {
	this.valtype_cd = valtype_cd;
}

public void setTval_char(String tval_char) {
	this.tval_char = tval_char;
}

public void setNval_num( Double nval_num ) {
	this.nval_num = nval_num;
}

public void setValueflag_cd( String valueflag_cd ) {
	this.valueflag_cd = valueflag_cd;
}

public void setQuantity_num( Double quantity_num ) {
	this.quantity_num = quantity_num;
}

public void setUnits_cd( String units_cd ) {
	this.units_cd = units_cd;
}

public void setEnd_date( Date end_date ) {
	this.end_date = end_date;
}

public void setLocation_cd( String location_cd ) {
	this.location_cd = location_cd;
}

public void setConfidence_num( Double confidence_num ) {
	this.confidence_num = confidence_num;
}

public void setSourcesystem_cd( String sourcesystem_cd ) {
	this.sourcesystem_cd = sourcesystem_cd;
}

public void setSchema_name(String schema_name) {
	this.schema_name = schema_name;
}

public String toString() {
	StringBuilder b = new StringBuilder() ;
	b.append( "schema_name: " ).append( schema_name ).append( "\n" )
	 .append( "encounter_num: " ).append( encounter_num ).append( "\n" )
	 .append( "patient_num: " ).append( patient_num ).append( "\n" )
	 .append( "concept_cd: " ).append( concept_cd ).append( "\n" )
	 .append( "provider_id: " ).append( provider_id ).append( "\n" )
	 .append( "start_date: " ).append( start_date ).append( "\n" )
	 .append( "valtype_cd: " ).append( valtype_cd ).append( "\n" )
	 .append( "tval_char: " ).append( tval_char ).append( "\n" )
	 .append( "nval_num: " ).append( nval_num ).append( "\n" )
	 .append( "valueflag_cd: " ).append( valueflag_cd ).append( "\n" )
	 .append( "quantity_num: " ).append( quantity_num ).append( "\n" )
	 .append( "units_cd: " ).append( units_cd ).append( "\n" ) 
	 .append( "end_date: " ).append( end_date ).append( "\n" )
	 .append( "location_cd: " ).append( location_cd ).append( "\n" )
	 .append( "valueflag_cd: " ).append( valueflag_cd ).append( "\n" )
	 .append( "confidence_num: " ).append( confidence_num ).append( "\n" )
	 .append( "sourcesystem_cd: " ).append( sourcesystem_cd ) ;
	return b.toString() ;
}
	
	
}

