/**
 * 
 */
package org.brisskit.i2b2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 * CREATE TABLE <xxxx> 
 *    ( "C_HLEVEL" INT			        NOT NULL, 
 *      "C_FULLNAME" VARCHAR(700)	    NOT NULL, 
 *      "C_NAME" VARCHAR(2000)		    NOT NULL, 
 *      "C_SYNONYM_CD" CHAR(1)		    NOT NULL, 
 *      "C_VISUALATTRIBUTES" CHAR(3)	NOT NULL, 
 *      "C_TOTALNUM" INT			    NULL, 
 *      "C_BASECODE" VARCHAR(50)	    NULL, 
 *      "C_METADATAXML" TEXT		    NULL, 
 *      "C_FACTTABLECOLUMN" VARCHAR(50)	NOT NULL, 
 *      "C_TABLENAME" VARCHAR(50)	    NOT NULL, 
 *      "C_COLUMNNAME" VARCHAR(50)	    NOT NULL, 
 *      "C_COLUMNDATATYPE" VARCHAR(50)	NOT NULL, 
 *      "C_OPERATOR" VARCHAR(10)	    NOT NULL, 
 *      "C_DIMCODE" VARCHAR(700)	    NOT NULL, 
 *      "C_COMMENT" TEXT			    NULL, 
 *      "C_TOOLTIP" VARCHAR(900)	    NULL, 
 *      "UPDATE_DATE" DATETIME		    NOT NULL, 
 *      "DOWNLOAD_DATE" DATETIME	    NULL, 
 *      "IMPORT_DATE" DATETIME	        NULL, 
 *      "SOURCESYSTEM_CD" VARCHAR(50)	NULL, 
 *      "VALUETYPE_CD" VARCHAR(50)	    NULL
 *   ) ;
 *   
 *           0 |
 *           \BIRN\     | 
 *           BIRN   | 
 *           N            | 
 *           FA                 |
 *                       |
 *                       |
 *                       | 
 *           concept_cd        | 
 *           concept_dimension | 
 *           concept_path | 
 *           T                | 
 *           LIKE       | 
 *           \BIRN\    |
 *                      | 
 *           BIRN      | 
 *           @              | 
 *           2007-10-10 17:10:01 | 
 *           2007-10-10 17:10:28 | 
 *           2007-10-10 17:10:36 | 
 *           OASIS           |
 *                         |
 *                         |        
 *                         | 

	CREATE TABLE CONCEPT_DIMENSION ( 
				CONCEPT_PATH   		VARCHAR(700) NOT NULL,
				CONCEPT_CD     		VARCHAR(50) NULL,
				NAME_CHAR      		VARCHAR(2000) NULL,
				CONCEPT_BLOB   		TEXT NULL,
				UPDATE_DATE    		TIMESTAMP NULL,
				DOWNLOAD_DATE  		TIMESTAMP NULL,
				IMPORT_DATE    		TIMESTAMP NULL,
				SOURCESYSTEM_CD		VARCHAR(50) NULL,
			    UPLOAD_ID			INT NULL,
			    CONSTRAINT CONCEPT_DIMENSION_PK PRIMARY KEY(CONCEPT_PATH)
	)

 *   
 * @author jeff
 */
public class OntologyBranch {
	
	private static Log log = LogFactory.getLog( OntologyBranch.class ) ;
	
	public static enum Type {
	    NUMERIC, 
	    DATE,
	    STRING ;
	    
	    public String toString() {
	    	
	    	 switch (this) {
	            case NUMERIC:
	                return "NUMERIC" ;    
	            case DATE: 
	                return "DATE" ;
	            case STRING:
	            default:
	                return "STRING" ;
	        }
	    }
	}
	
	//
	// I have my doubts about some of the final columns in the create statement
	// for the ontology tables. The below are missing them.
	public static final String METADATA_SQL_INSERT_COMMAND = 
			"SET SCHEMA 'i2b2metadata';" +
			"" +
			"INSERT INTO i2b2metadata.<PROJECT_ONTOLOGY_TABLE_NAME>" +
			                "( C_HLEVEL" +
			                ", C_FULLNAME" +
			                ", C_NAME" +
			                ", C_SYNONYM_CD" +
			                ", C_VISUALATTRIBUTES" +
			                ", C_TOTALNUM" +
			                ", C_BASECODE" +
			                ", C_METADATAXML" +
			                ", C_FACTTABLECOLUMN" +
			                ", C_TABLENAME" +
			                ", C_COLUMNNAME" +
			                ", C_COLUMNDATATYPE" +
			                ", C_OPERATOR" +
			                ", C_DIMCODE" +
			                ", C_COMMENT" +
			                ", C_TOOLTIP" +
			                ", M_APPLIED_PATH" +
			                ", UPDATE_DATE" +
			                ", DOWNLOAD_DATE" +
			                ", IMPORT_DATE" +
			                ", SOURCESYSTEM_CD" +
			                ", VALUETYPE_CD ) " +
	         "VALUES( <HLEVEL>" +
	               ", <FULLNAME>" +
	               ", <NAME>" +
	               ", <SYNONYM_CD>" +         // 1 char
	               ", <VISUALATTRIBUTES>" +   // 3 chars
	               ", NULL" +				  // totalnum
	               ", <BASECODE>" +
	               ", <METADATAXML>" +
	               ", 'concept_cd'" +
	               ", 'concept_dimension'" +
	               ", 'concept_path'" +
	               ", <COLUMNDATATYPE>" +
	               ", <OPERATOR>" +
	               ", <DIMCODE>" +
	               ", NULL" +				  // comment
	               ", <TOOLTIP>" +
	               ", '@'" +				  // applied path
	               ", now()" +
	               ", now()" +
	               ", now()" +
	               ", <SOURCESYSTEM_CD>" +
	               ", NULL ) ;" ;			  // valuetype_cd
	
	//
	//
	// In the following, CreationDateTime should be something like 01/26/2011 00:00:00
	public static final String 	METEDATAXML = 
			  "<?xml version=\"1.0\"?>"
			+ "<ValueMetadata>" 
			+ "<Version>3.02</Version>"
			+ "<CreationDateTime><date-time-goes-here></CreationDateTime>"
			+ "<TestID><code-name-goes-here></TestID>"
			+ "<TestName><name-goes-here></TestName>"
			+ "<DataType>PosFloat</DataType>" 
			+ "<Flagstouse></Flagstouse>"
			+ "<Oktousevalues>Y</Oktousevalues>" 
			+ "<EnumValues></EnumValues>"
			+ "<UnitValues>" 
			+ "<NormalUnits><units-go-here></NormalUnits>"
			+ "</UnitValues>" 
			+ "</ValueMetadata>";
	

	public static final String CONCEPT_DIM_SQL_INSERT_COMMAND = 
			"SET SCHEMA <SCHEMA_NAME_1>;" +
			"" +
			"INSERT INTO <SCHEMA_NAME_2>.CONCEPT_DIMENSION" +
			       "( CONCEPT_PATH" +    // 	VARCHAR(700) NOT NULL
			       ", CONCEPT_CD" +      // 	VARCHAR(50) NULL
			       ", NAME_CHAR" +	     //  	VARCHAR(2000) NULL
			       ", CONCEPT_BLOB" +    //  	TEXT NULL
			       ", UPDATE_DATE" +     //  	TIMESTAMP NULL
			       ", DOWNLOAD_DATE" +   //  	TIMESTAMP NULL
			       ", IMPORT_DATE" +     //  	TIMESTAMP NULL
			       ", SOURCESYSTEM_CD" + //		VARCHAR(50) NULL
			       ", UPLOAD_ID )" +	 //		INT NULL
			"VALUES ( <CONCEPT_PATH>" +
	               ", <CONCEPT_CD>" +
	               ", <NAME_CHAR>" +
	               ", NULL" +  			// concept blob       
	               ", now()" +
	               ", now()" +
	               ", now()" +
	               ", <SOURCESYSTEM_CD>" +
	               ", NULL ) ;" ;		// upload id
	
	
	//
	// These two prefixes are to enable codes and paths to be stored in the same 
	// set (see pathsAndCodes below) without interfering with each other.
	public static final String CODE_PREFIX = "code->" ;
	public static final String PATH_PREFIX = "path->" ;

	//
	// Project id (required for schema and some column values )
	private String projectId ;
	//
	// (Possibly more important in future more complex spread sheets, but already partly present)
	// The OntologyBranch object holds data for one a basic code plus its ontology path,
	// (or rather, one basic code plus it's potential enumerations, and paths). This means that it
	// is possible for duplication to occur. The obvious one is the root, which is shared
	// by every ontology path, but also every intermediate node down to the leaf could be
	// a possible clash, meaning some repeated inserts would fail.
	// We place a string representation of each node and code here (the collection is passed in to the
	// constructor) and the same collection is present in EVERY OntologyBranch object.
	// This enables us to avoid duplicaton.
	private HashSet<String> pathsAndCodes ;
	//
	// Column name as it appears in the spreadsheet metadata row
	private String colName ;
	//
	// Tooltip as it appears in the spreadsheet metadata row
	private String toolTip ;
	//
	// Ontology code at is appears in the spreadsheet metadata row
	private String ontCode ;
	//
	// Data type for a given column (numeric, string or date) 
	private Type type ;
	//
	// If the type is numeric, this should give the units.
	// A value of blanks (or length 0) means implied units.
	// For enumerated types this is set to the default value of "enum".
	private String units ;
	//
	// 
	private ProjectUtils utils ;
	//
	// The range of values encountered within the spreadsheet for this particular column...
	private HashSet<String> values ;
	//
	// Only required if code lookups are provided 
	private Map<String,String> lookups ;
	
	public OntologyBranch( String projectId
			             , String colName
			             , String toolTip
			             , String ontCode
			             , Type type
			             , String units
			             , Map<String,String> lookups
			             , HashSet<String> values
			             , HashSet<String> pathsAndCodes
			             , ProjectUtils utils ) {
		this.projectId = projectId ;
		this.colName = colName ;
		this.toolTip = toolTip ;
		this.ontCode = ontCode ;
		this.type = type ;
		this.units = units.trim() ;  // ensures this MUST NOT BE null s
		this.lookups = lookups ;
		this.pathsAndCodes = pathsAndCodes ;
		this.values = values ;
		this.utils = utils ;
	}
	
	
	public void serializeToDatabase( Connection connection ) throws NewProjectException {
		enterTrace( "OntologyBranch.serializeToDatabase()" ) ;
		try {			
			insertRoot( connection) ;
			
			switch( type ) {
			case NUMERIC:
				if( units.equalsIgnoreCase( "enum" ) ) {
					insertEnumeratedNumeric( connection ) ;
				}
				else {
					insertNumeric( connection ) ;
				}				
				break ;
			case DATE:
				insertDate( connection ) ;
				break ;
			case STRING:
			default:
				insertString( connection ) ;
				break;
			}

		}
		finally {
			exitTrace( "OntologyBranch.serializeToDatabase()" ) ;
		}
	}
	
	
	private void insertRoot( Connection connection ) throws NewProjectException {
		enterTrace( "OntologyBranch.insertRoot()" ) ;
		try {
			String fullName = "\\" + projectId + "\\" ;		
			
			if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {

				String sqlCmd = METADATA_SQL_INSERT_COMMAND ;			
								
				sqlCmd = sqlCmd.replace( "<PROJECT_ONTOLOGY_TABLE_NAME>", projectId ) ;
				
				sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 0 ) ) ;
				sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
				sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "FA" ) ) ;
				sqlCmd = sqlCmd.replace( "<BASECODE>", "NULL" ) ;
				sqlCmd = sqlCmd.replace( "<METADATAXML>", "NULL" ) ;
				sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "T" ) ) ;
				sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
				sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( projectId ) ) ;
				sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
				
				Statement st = connection.createStatement();

				st.execute( sqlCmd ) ;

				//
				// Record the path name so we don't try and duplicate it next time...
				pathsAndCodes.add( PATH_PREFIX + fullName ) ;
			}
		}
		catch( SQLException sqlx ) {
			sqlx.printStackTrace() ;
			throw new NewProjectException( "OntologyBranch.insertRoot(): Failed to create metadata table root.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertRoot()" ) ;
		}
	}
	
	private void insertNumeric( Connection connection ) throws NewProjectException {
		enterTrace( "OntologyBranch.insertNumeric()" ) ;
		try {
			String fullName = "\\" + projectId + "\\" + colName + "\\" ;
			if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {
				
				String sqlCmd = METADATA_SQL_INSERT_COMMAND ;
				
				String date = utils.formatDate( new Date() ) ;
				String metadataxml = METEDATAXML ;
				metadataxml = metadataxml.replace( "<date-time-goes-here>", date + " 00:00:00" ) ;
				metadataxml = metadataxml.replace( "<code-name-goes-here>", ontCode ) ;
				metadataxml = metadataxml.replace( "<name-goes-here>", colName ) ;
				metadataxml = metadataxml.replace( "<units-go-here>", units ) ;
				
				sqlCmd = sqlCmd.replace( "<PROJECT_ONTOLOGY_TABLE_NAME>", projectId ) ;
				
				sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 1 ) ) ;
				sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
				sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "LA" ) ) ;
				sqlCmd = sqlCmd.replace( "<BASECODE>", "NULL" ) ;
				sqlCmd = sqlCmd.replace( "<METADATAXML>", utils.enfoldNullableString( metadataxml ) ) ;
				sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "N" ) ) ;
				sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
				sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( projectId ) ) ;
				sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
				
				Statement st = connection.createStatement();
				
				st.execute( sqlCmd ) ;
				//
				// Insert concept into concept dimension...
				insertIntoConceptDimension( st, fullName, ontCode, colName ) ;
				//
				// Record the path name so we don't try and duplicate it next time...
				pathsAndCodes.add( PATH_PREFIX + fullName ) ;
			}
			
		}
		catch( SQLException sqlx ) {
			throw new NewProjectException( "Failed to insert decimal branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertNumeric()" ) ;
		}
	}
	
	
	private void insertEnumeratedNumeric( Connection connection ) throws NewProjectException {
		enterTrace( "OntologyBranch.insertEnumeratedNumeric()" ) ;
		try {
			//
			// Inserts are inserted as enumerations, so on two/three levels:
			// The base code; eg: Age
			// Possible ranges of values; eg: 1-10, 11-20, 21-30 etc
			// End point values; eg: 1, 2, 3, 4, 5 etc
			//
			// Insert the base code...
			String fullName = "\\" + projectId + "\\" + colName + "\\" ;
			String sqlCmd = null ;
			Statement st = connection.createStatement();
			
			if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {
				sqlCmd = METADATA_SQL_INSERT_COMMAND ;
				
				sqlCmd = sqlCmd.replaceAll( "<project_name>", projectId ) ;
				sqlCmd = sqlCmd.replace( "<hlevel>", "1" ) ;
				sqlCmd = sqlCmd.replace( "<fullname>", fullName ) ;
				sqlCmd = sqlCmd.replace( "<synonym_cd>", "N" ) ;
				sqlCmd = sqlCmd.replace( "<visualattributes>", "FA" ) ;
				sqlCmd = sqlCmd.replace( "<basecode>", "NULL" ) ;
				sqlCmd = sqlCmd.replace( "<metadataxml>", "NULL" ) ;
				sqlCmd = sqlCmd.replace( "<columndatatype>", "T" ) ;
				sqlCmd = sqlCmd.replace( "<operator>", "LIKE" ) ;
				sqlCmd = sqlCmd.replace( "<dimcode>", fullName ) ;
				sqlCmd = sqlCmd.replace( "<tooltip>", toolTip ) ;
				
				st.execute( sqlCmd ) ;				
				//
				// Record the path name so we don't try and duplicate it next time...
				pathsAndCodes.add( PATH_PREFIX + fullName ) ;
			}
			
			
			//
			// Examine ranges...
			int lowestValue = Integer.MAX_VALUE ;
			int highestValue = Integer.MIN_VALUE ;
			Iterator<String> it = values.iterator() ;
			while( it.hasNext() ) {
				String val = it.next() ;
				int i = Integer.valueOf( val ) ;
				if( i < lowestValue ) {
					lowestValue = i ;
				}
				if( i > highestValue ) {
					highestValue = i ;
				}
			}
			
			lowestValue = ( lowestValue / 10 ) ;
					
			if( highestValue % 10 != 0 ) {
				highestValue = ( ( highestValue / 10 ) * 10 ) - 10;
			}
			else {
				highestValue = ( ( highestValue / 10 ) * 10 )  ;
			}
			
			//
			// Insert the ranges...
			for( int i=lowestValue; i<highestValue+1; i=i+10 ) {
				fullName = "\\" + projectId + "\\" + colName + "\\" + i + "-" + (i+10) + "\\" ;
				if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {
					sqlCmd = METADATA_SQL_INSERT_COMMAND ;
					
					sqlCmd = sqlCmd.replaceAll( "<project_name>", projectId ) ;
					sqlCmd = sqlCmd.replace( "<hlevel>", "2" ) ;
					sqlCmd = sqlCmd.replace( "<fullname>", fullName ) ;
					sqlCmd = sqlCmd.replace( "<synonym_cd>", "N" ) ;
					sqlCmd = sqlCmd.replace( "<visualattributes>", "FA" ) ;
					sqlCmd = sqlCmd.replace( "<basecode>", "NULL" ) ;
					sqlCmd = sqlCmd.replace( "<metadataxml>", "NULL" ) ;
					sqlCmd = sqlCmd.replace( "<columndatatype>", "T" ) ;
					sqlCmd = sqlCmd.replace( "<operator>", "LIKE" ) ;
					sqlCmd = sqlCmd.replace( "<dimcode>", fullName ) ;
					sqlCmd = sqlCmd.replace( "<tooltip>", toolTip + ": " + i + "-" + (i+10) ) ;
					
					st.execute( sqlCmd ) ;
					//
					// Record the path name so we don't try and duplicate it next time...
					pathsAndCodes.add( PATH_PREFIX + fullName ) ;
				}
								
			}
			
			//
			// Insert the end points...
			for( int i=lowestValue; i<highestValue+1; i++ ) {
				fullName = "\\" + projectId + "\\" + colName + "\\" + i + "\\" ;
				if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {
					sqlCmd = METADATA_SQL_INSERT_COMMAND ;
					
					sqlCmd = sqlCmd.replaceAll( "<project_name>", projectId ) ;
					sqlCmd = sqlCmd.replace( "<hlevel>", "3" ) ;
					sqlCmd = sqlCmd.replace( "<fullname>", fullName ) ;
					sqlCmd = sqlCmd.replace( "<synonym_cd>", "N" ) ;
					sqlCmd = sqlCmd.replace( "<visualattributes>", "LA" ) ;
					sqlCmd = sqlCmd.replace( "<basecode>", "NULL" ) ;
					sqlCmd = sqlCmd.replace( "<metadataxml>", "NULL" ) ;
					sqlCmd = sqlCmd.replace( "<columndatatype>", "T" ) ;
					sqlCmd = sqlCmd.replace( "<operator>", "LIKE" ) ;
					sqlCmd = sqlCmd.replace( "<dimcode>", fullName ) ;
					sqlCmd = sqlCmd.replace( "<tooltip>", toolTip + ": " + i ) ;
					
					st.execute( sqlCmd ) ;
					
					//
					// Insert concept into concept dimension...
					insertIntoConceptDimension( st, fullName, ontCode + ":" + i, colName + " " + i ) ;
					//
					// Record the path name so we don't try and duplicate it next time...
					pathsAndCodes.add( PATH_PREFIX + fullName ) ;
				}
				
			}
			
		}
		catch( SQLException sqlx ) {
			throw new NewProjectException( "Failed to insert integer branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertEnumeratedNumeric()" ) ;
		}
	}
	
	
	
	
	
	

	private void insertIntoConceptDimension( Statement statement
			                               , String conceptPath
			                               , String conceptCode
			                               , String conceptName ) throws NewProjectException {
		enterTrace( "OntologyBranch.insertIntoConceptDimension()" ) ;
		try {
			String sqlCmd = CONCEPT_DIM_SQL_INSERT_COMMAND ;
			
			sqlCmd = sqlCmd.replace( "<SCHEMA_NAME_1>", utils.enfoldString( projectId ) ) ;			
			sqlCmd = sqlCmd.replace( "<SCHEMA_NAME_2>", projectId ) ;
			
			sqlCmd = sqlCmd.replace( "<CONCEPT_PATH>", utils.enfoldString( conceptPath ) ) ;
			sqlCmd = sqlCmd.replace( "<CONCEPT_CD>", utils.enfoldNullableString( conceptCode ) ) ;
			sqlCmd = sqlCmd.replace( "<NAME_CHAR>", utils.enfoldNullableString( conceptName ) ) ;
			sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
				
			statement.execute( sqlCmd ) ;			
		}
		catch( SQLException sqlx ) {
			throw new NewProjectException( "Failed to insert concept into concept dimension.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertIntoConceptDimension()" ) ;
		}
	}
	
	
	private void insertDate( Connection connection ) throws NewProjectException {
		enterTrace( "OntologyBranch.insertDate()" ) ;
		try {
			//
			// A date is really an instance of an ontological code occurring.
			// The date is the fact start date.
			// So we treat dates like strings, but no enumeration, and 
			// rely upon the start date (of a fact) to distinguish occurrances.
			
			//
			// Insert the base code...
			String fullName = "\\" + projectId + "\\" + colName + "\\" ;
			if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {
				
				String sqlCmd = METADATA_SQL_INSERT_COMMAND ;
				
				sqlCmd = sqlCmd.replace( "<PROJECT_ONTOLOGY_TABLE_NAME>", projectId ) ;
				
				sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 1 ) ) ;
				sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
				sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "LA" ) ) ;
				sqlCmd = sqlCmd.replace( "<BASECODE>", "NULL" ) ;
				sqlCmd = sqlCmd.replace( "<METADATAXML>", "NULL" ) ;
				sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "T" ) ) ;
				sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
				sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( toolTip ) ) ;
				sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
				
				Statement st = connection.createStatement();
				
				st.execute( sqlCmd ) ;
				//
				// Insert concept into concept dimension...
				insertIntoConceptDimension( st, fullName, ontCode, colName ) ;
				//
				// Record the path name so we don't try and duplicate it next time...
				pathsAndCodes.add( PATH_PREFIX + fullName ) ;
			}
			
		}
		catch( SQLException sqlx ) {
			throw new NewProjectException( "Failed to insert date branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertDate()" ) ;
		}
	}
	
	
	private void insertString( Connection connection ) throws NewProjectException {
		enterTrace( "OntologyBranch.insertString()" ) ;
		try {
			//
			// Strings are inserted as enumerations, so on two levels:
			// The base code; eg: marital_status
			// The possible range of values; eg: single, married, widowed, separated, divorced ...
			
			//
			// Insert the base code...
			Statement st = connection.createStatement() ;
			String sqlCmd = null ;
			String fullName = "\\" + projectId + "\\" + colName + "\\" ;
			if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {
				
				sqlCmd = METADATA_SQL_INSERT_COMMAND ;
				
				sqlCmd = sqlCmd.replace( "<PROJECT_ONTOLOGY_TABLE_NAME>", projectId ) ;
				
				sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 1 ) ) ;
				sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
				sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "FA" ) ) ;
				sqlCmd = sqlCmd.replace( "<BASECODE>", "NULL" ) ;
				sqlCmd = sqlCmd.replace( "<METADATAXML>", "NULL" ) ;
				sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "T" ) ) ;
				sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
				sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( toolTip ) ) ;
				sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
				
				st.execute( sqlCmd ) ;
				
				//
				// Record the path name so we don't try and duplicate it next time...
				pathsAndCodes.add( PATH_PREFIX + fullName ) ;
			}
			
					
			//
			// Now insert the range of values in the enumeration ...
			Iterator<String> it = values.iterator() ;
			while( it.hasNext() ) {
				String subCategory = it.next() ;
				
				fullName = "\\" + projectId + "\\" + colName + "\\" + subCategory + "\\" ;
				if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {
					
					sqlCmd = METADATA_SQL_INSERT_COMMAND ;
					
					sqlCmd = sqlCmd.replace( "<PROJECT_ONTOLOGY_TABLE_NAME>", projectId ) ;
					
					sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 2 ) ) ;
					sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
					sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( fullName ) ) ;
					sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
					sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "LA" ) ) ;
					sqlCmd = sqlCmd.replace( "<BASECODE>", "NULL" ) ;
					sqlCmd = sqlCmd.replace( "<METADATAXML>", "NULL" ) ;
					sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "T" ) ) ;
					sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
					sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( fullName ) ) ;
					sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( toolTip + ":" + subCategory ) ) ;
					sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
					
					st.execute( sqlCmd ) ;
					//
					// Insert concept into concept dimension...
					insertIntoConceptDimension( st, fullName, ontCode + ":" + subCategory, colName + " " + subCategory ) ;
					//
					// Record the path name so we don't try and duplicate it next time...
					pathsAndCodes.add( PATH_PREFIX + fullName ) ;
				}
				
			}
		}
		catch( SQLException sqlx ) {
			throw new NewProjectException( "Failed to insert string branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertString()" ) ;
		}
	}
	

	public Type getType() {
		return type;
	}	
	
	
	public String getOntCode() {
		return ontCode;
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

}
