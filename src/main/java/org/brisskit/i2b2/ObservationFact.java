/**
 * 
 */
package org.brisskit.i2b2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

//import java.sql.Date;

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

public static final String OBSERVATION_FACT_INSERT_SQL_KEY = "OBSERVATION_FACT_INSERT_SQL" ;
public static final String OBSERVATION_FACT_INSERT_SQL = 
		"INSERT INTO OBSERVATION_FACT" +
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
		       "  ?" +
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
			   ", NULL" +					// observation blob
			   ", ?" +
			   ", now()" +
			   ", now()" +
			   ", now()" +
			   ", ?" +
			   ", NULL ) ;" ;				// upload id

private PreparedStatement batchUsedPreparedStatement ;

private ProjectUtils utils ;

private String schema_name ;
//
// Observation Fact columns...
private Integer encounter_num = null ;
private Integer patient_num = null ;
private String concept_cd = null ;
private String provider_id = null ;
private java.util.Date start_date = null ;
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

 
public void serializeToDatabase() throws UploaderException {
	enterTrace( "ObservationFact.serializeToDatabase()" ) ;
	try {
		PreparedStatement ps = utils.getPsHolder().getPreparedStatement( OBSERVATION_FACT_INSERT_SQL_KEY ) ;
				    
		ps.setInt( 1, encounter_num ) ;
		ps.setInt( 2, patient_num ) ;
		ps.setString( 3, concept_cd ) ;
		ps.setString( 4, provider_id ) ;
		ps.setTimestamp( 5, new java.sql.Timestamp( start_date.getTime() ) ) ;
		if( valtype_cd == null ) {
			ps.setNull( 6, java.sql.Types.VARCHAR ) ;
		}
		else {
			ps.setString( 6, valtype_cd ) ;
		}
		if( tval_char == null ) {
			ps.setNull( 7, java.sql.Types.VARCHAR ) ;
		}
		else {
			ps.setString( 7, tval_char ) ;
		}
		if( nval_num == null ) {
			ps.setNull( 8, java.sql.Types.DOUBLE ) ; 
		}
		else {
			ps.setDouble( 8, nval_num ) ;
		}
		if( valueflag_cd == null ) {
			ps.setNull( 9, java.sql.Types.VARCHAR ) ;
		}
		else {
			ps.setString( 9, valueflag_cd ) ;
		}
		if( quantity_num == null ) {
			ps.setNull( 10, java.sql.Types.DOUBLE ) ;
		}
		else {
			ps.setDouble( 10, quantity_num ) ;
		}		
		if( units_cd == null ) {
			ps.setNull( 11, java.sql.Types.VARCHAR ) ;     
		}
		else {
			ps.setString( 11, units_cd ) ;
		}
		if( end_date == null ) {
			ps.setNull( 12, java.sql.Types.TIMESTAMP ) ;     // end_date
		}
		else {
			ps.setTimestamp( 12, new java.sql.Timestamp( end_date.getTime() ) ) ;
		}
		if( location_cd == null ) {
			ps.setNull( 13, java.sql.Types.VARCHAR ) ;    
		}
		else {
			ps.setString( 13, location_cd ) ;
		}
		if( confidence_num == null ) {
			ps.setNull( 14, java.sql.Types.DOUBLE ) ;    
		}
		else {
			ps.setDouble( 14, confidence_num ) ; 
		}
		if( sourcesystem_cd == null ) {
			ps.setNull( 15, java.sql.Types.VARCHAR ) ;  
		}
		else {
			ps.setString( 15, sourcesystem_cd ) ;
		}

		ps.addBatch() ;

	}
	catch( SQLException sqlx ) {
		logger.error( sqlx.getStackTrace() ) ;
		throw new UploaderException( "Failed to insert into observation fact table", sqlx ) ;
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

