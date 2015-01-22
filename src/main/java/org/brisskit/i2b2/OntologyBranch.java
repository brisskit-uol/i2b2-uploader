/**
 * 
 */
package org.brisskit.i2b2;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
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
			"SET SCHEMA '<DB_SCHEMA_NAME>';" +
			"" +
			"INSERT INTO <DB_SCHEMA_NAME>.<PROJECT_METADATA_TABLE>" +
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
	public static final String 	METADATAXML = 
			  "<?xml version=\"1.0\"?>"
			+ "<ValueMetadata>" 
			+ " <Version>3.02</Version>"
			+ " <CreationDateTime><date-time-goes-here></CreationDateTime>"
			+ " <TestID><code-name-goes-here></TestID>"
			+ " <TestName><name-goes-here></TestName>"
			+ " <DataType><data-type-goes-here></DataType>" 
			+ " <CodeType/>" 
			+ " <Loinc/>" 
			+ " <Flagstouse></Flagstouse>"
			+ " <Oktousevalues>Y</Oktousevalues>" 
			+ " <MaxStringLength/>" 
			+ " <LowofLowValue></LowofLowValue>" 
			+ " <HighofLowValue></HighofLowValue>" 
			+ " <LowofHighValue></LowofHighValue>" 
			+ " <HighofHighValue></HighofHighValue>" 
			+ " <LowofToxicValue/>" 
			+ " <HighofToxicValue/>" 
			+ " <EnumValues></EnumValues>" 
			+ " <CommentsDeterminingExclusion>" 
			+ "   <Com/>" 
			+ " </CommentsDeterminingExclusion>"
			+ " <UnitValues>" 
			+ "   <NormalUnits><units-go-here></NormalUnits>" 
			+ "   <EqualUnits/>" 
			+ "   <ExcludingUnits/>" 
			+ "   <ConvertingUnits>" 
			+ "     <Units/>" 
			+ "     <MultiplyingFactor/>" 
			+ "   </ConvertingUnits>" 
			+ " </UnitValues>" 
			+ " <Analysis>" 
			+ "   <Enums/>" 
			+ "   <Counts/>" 
			+ "   <New/> " 
			+ " </Analysis>" 
			+ "</ValueMetadata>";
	

	public static final String CONCEPT_DIM_SQL_INSERT_COMMAND = 
			"SET SCHEMA '<DB_SCHEMA_NAME>';" +
			"" +
			"INSERT INTO <DB_SCHEMA_NAME>.CONCEPT_DIMENSION" +
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
		this.units = units.trim() ;  // ensures this MUST NOT BE null
		this.lookups = lookups ;
		this.pathsAndCodes = pathsAndCodes ;
		this.values = values ;
		this.utils = utils ;
	}
	
	
	public void serializeToDatabase( Connection connection ) throws UploaderException {
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
				if( units.equalsIgnoreCase( "text" ) ) {
					insertSearchableText( connection ) ;
				}
				else {
					insertEnumeratedString( connection ) ;
				}				
				break;
			}

		}
		finally {
			exitTrace( "OntologyBranch.serializeToDatabase()" ) ;
		}
	}
	
	
	private void insertRoot( Connection connection ) throws UploaderException {
		enterTrace( "OntologyBranch.insertRoot()" ) ;
		try {
			String fullName = "\\" + projectId + "\\" ;		
			
			if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {

				String sqlCmd = METADATA_SQL_INSERT_COMMAND ;			
					
				sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", projectId ) ;
				sqlCmd = sqlCmd.replace( "<PROJECT_METADATA_TABLE>", projectId ) ;
				
				sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 0 ) ) ;
				sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( projectId ) ) ;
				sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
				sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "FA" ) ) ;
				sqlCmd = sqlCmd.replace( "<BASECODE>", "NULL" ) ;
				sqlCmd = sqlCmd.replace( "<METADATAXML>", "NULL" ) ;
				sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "T" ) ) ;
				sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
				sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( fullName ) ) ;
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
			throw new UploaderException( "OntologyBranch.insertRoot(): Failed to create metadata table root.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertRoot()" ) ;
		}
	}
	
	private void insertNumeric( Connection connection ) throws UploaderException {
		enterTrace( "OntologyBranch.insertNumeric()" ) ;
		try {
			String fullName = "\\" + projectId + "\\" + colName + "\\" ;
			if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {
				
				String sqlCmd = METADATA_SQL_INSERT_COMMAND ;
				
				String date = utils.formatDate( new Date() ) ;
				String metadataxml = METADATAXML ;
				metadataxml = metadataxml.replace( "<date-time-goes-here>", date + " 00:00:00" ) ;
				metadataxml = metadataxml.replace( "<code-name-goes-here>", ontCode ) ;
				metadataxml = metadataxml.replace( "<name-goes-here>", colName ) ;
				metadataxml = metadataxml.replace( "<data-type-goes-here>", "PosFloat" ) ; 
				metadataxml = metadataxml.replace( "<units-go-here>", units ) ;
				
				log.debug( "For ontCode " + ontCode + " numeric units are: " + units ) ;
				
				sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", projectId ) ;
				sqlCmd = sqlCmd.replace( "<PROJECT_METADATA_TABLE>", projectId ) ;
				
				sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 1 ) ) ;
				sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( colName ) ) ;
				sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
				sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "LA" ) ) ;
				sqlCmd = sqlCmd.replace( "<BASECODE>", utils.enfoldString( ontCode ) ) ;
				sqlCmd = sqlCmd.replace( "<METADATAXML>", utils.enfoldNullableString( metadataxml ) ) ;
				sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "T" ) ) ;
				sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
				sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( fullName ) ) ;
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
			throw new UploaderException( "Failed to insert decimal branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertNumeric()" ) ;
		}
	}
	
	
	private void insertEnumeratedNumeric( Connection connection ) throws UploaderException {
		enterTrace( "OntologyBranch.insertEnumeratedNumeric()" ) ;
		try {
//			if( colName.equalsIgnoreCase( "CL_STATUS" ) 
//				||
//				colName.equalsIgnoreCase( "SMOKED_AGE_STARTED" ) ) {
//				
//				log.debug( "About to process " + colName ) ;
//				
//			}
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
				
				sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", projectId ) ;
				sqlCmd = sqlCmd.replace( "<PROJECT_METADATA_TABLE>", projectId ) ;
								
				sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 1 ) ) ;
				sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( colName ) ) ;
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
			
			//
			// Set lowest and highest in 10's
			// (ie; 3 as a lowest becomes 0, 13 as a highest becomes 20)...
			lowestValue = ( lowestValue / 10 ) * 10 ;
					
			if( highestValue % 10 != 0 ) {
				highestValue = ( ( highestValue / 10 ) * 10 ) + 10;
			}
			//
			// The formatString helps keep data sensibly in collating sequence...
			// (ie: 3 is displayed as 03, 10 as 10, and so on,
			//  otherwise 10 would come before 3 visually (in aphabetical sequence)
			long numberOfDigits = Math.round( Math.log10( highestValue ) / Math.log10( 10 ) ) ;
			if( highestValue % 10 == 0) {
				numberOfDigits++ ;
			}
			String formatString = "%0" + String.valueOf( numberOfDigits ) + "d" ;
			log.debug( "format string is " + formatString ) ;
			
			//
			// If the total range of values is below 21, we use just endpoints
			// and don't bother with ranges...
			if( (highestValue - lowestValue) < 21 ) {
				//
				// Insert the end points...
				String endPointFullName = null ;
				for( int j=lowestValue; j<highestValue+1; j++ ) {
					String paddedValue = String.format( formatString, j ) ;
					endPointFullName = fullName + paddedValue + "\\" ;
					if( !pathsAndCodes.contains( PATH_PREFIX + endPointFullName ) ) {
						sqlCmd = METADATA_SQL_INSERT_COMMAND ;
						
						sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", projectId ) ;
						sqlCmd = sqlCmd.replace( "<PROJECT_METADATA_TABLE>", projectId ) ;
						
						sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 2 ) ) ;
						sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( endPointFullName ) ) ;
						sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( colName + ":" + paddedValue ) ) ;
						sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
						sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "LA" ) ) ;
						sqlCmd = sqlCmd.replace( "<BASECODE>", utils.enfoldString( ontCode + ":" + j ) ) ;
						sqlCmd = sqlCmd.replace( "<METADATAXML>", "NULL" ) ;
						sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "T" ) ) ;
						sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
						sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( endPointFullName ) ) ;
						sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( toolTip + ":" + paddedValue ) ) ;
						sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
						
						st.execute( sqlCmd ) ;
						
						//
						// Insert concept into concept dimension...
						insertIntoConceptDimension( st, endPointFullName, ontCode + ":" + j, colName + ":" + j ) ;
						//
						// Record the path name so we don't try and duplicate it next time...
						pathsAndCodes.add( PATH_PREFIX + fullName ) ;
					}
					
				} // end inner for
			}
			// Otherwise we use ranges of values based on a base range of 10...
			else {
				//
				// Insert the ranges...
				String rangeFullName = null ;
				String rangeShortName = null ;
				for( int i=lowestValue; i<highestValue+1; i=i+10 ) {
					String lower = String.format( formatString, i ) ;
					String upper = String.format( formatString, i+9 ) ;
					rangeShortName = lower + "-" + upper ;
					rangeFullName = fullName + rangeShortName + "\\" ;
					if( !pathsAndCodes.contains( PATH_PREFIX + rangeFullName ) ) {
						sqlCmd = METADATA_SQL_INSERT_COMMAND ;
						
						sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", projectId ) ;
						sqlCmd = sqlCmd.replace( "<PROJECT_METADATA_TABLE>", projectId ) ;
						
						sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 2 ) ) ;
						sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( rangeFullName ) ) ;
						sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( colName + ":" + rangeShortName ) ) ;
						sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
						sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "FA" ) ) ;
						sqlCmd = sqlCmd.replace( "<BASECODE>", "NULL" ) ;
						sqlCmd = sqlCmd.replace( "<METADATAXML>", "NULL" ) ;
						sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "T" ) ) ;
						sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
						sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( rangeFullName ) ) ;
						sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( toolTip + ": " + rangeShortName ) ) ;
						sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
						
						st.execute( sqlCmd ) ;
						//
						// Record the path name so we don't try and duplicate it next time...
						pathsAndCodes.add( PATH_PREFIX + rangeFullName ) ;
					}
					
					//
					// Insert the end points...
					String endPointFullName = null ;
					for( int j=i; j<i+10; j++ ) {
						String paddedValue = String.format( formatString, j ) ;
						endPointFullName = rangeFullName + ":" + paddedValue + "\\" ;
						if( !pathsAndCodes.contains( PATH_PREFIX + endPointFullName ) ) {
							sqlCmd = METADATA_SQL_INSERT_COMMAND ;
							
							sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", projectId ) ;
							sqlCmd = sqlCmd.replace( "<PROJECT_METADATA_TABLE>", projectId ) ;
							
							sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 3 ) ) ;
							sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( endPointFullName ) ) ;
							sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( colName + ":" + paddedValue ) ) ;
							sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
							sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "LA" ) ) ;
							sqlCmd = sqlCmd.replace( "<BASECODE>", utils.enfoldString( ontCode + ":" + j ) ) ;
							sqlCmd = sqlCmd.replace( "<METADATAXML>", "NULL" ) ;
							sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "T" ) ) ;
							sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
							sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( endPointFullName ) ) ;
							sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( toolTip + ":" + paddedValue ) ) ;
							sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
							
							st.execute( sqlCmd ) ;
							
							//
							// Insert concept into concept dimension...
							insertIntoConceptDimension( st, endPointFullName, ontCode + ":" + j, colName + ":" + j ) ;
							//
							// Record the path name so we don't try and duplicate it next time...
							pathsAndCodes.add( PATH_PREFIX + endPointFullName ) ;
						}
						
					} // end inner for
									
				} // end outer for
				
			}	
			
		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert integer branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertEnumeratedNumeric()" ) ;
		}
	}
	
	
	private void insertIntoConceptDimension( Statement statement
			                               , String conceptPath
			                               , String conceptCode
			                               , String conceptName ) throws UploaderException {
		enterTrace( "OntologyBranch.insertIntoConceptDimension()" ) ;
		try {
			String sqlCmd = CONCEPT_DIM_SQL_INSERT_COMMAND ;
			
			sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", projectId ) ;			
			
			sqlCmd = sqlCmd.replace( "<CONCEPT_PATH>", utils.enfoldString( conceptPath ) ) ;
			sqlCmd = sqlCmd.replace( "<CONCEPT_CD>", utils.enfoldNullableString( conceptCode ) ) ;
			sqlCmd = sqlCmd.replace( "<NAME_CHAR>", utils.enfoldNullableString( conceptName ) ) ;
			sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
				
			statement.execute( sqlCmd ) ;			
		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert concept into concept dimension.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertIntoConceptDimension()" ) ;
		}
	}
	
	
	private void insertDate( Connection connection ) throws UploaderException {
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
				
				sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", projectId ) ;
				sqlCmd = sqlCmd.replace( "<PROJECT_METADATA_TABLE>", projectId ) ;
				
				sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 1 ) ) ;
				sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( colName ) ) ;
				sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
				sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "LA" ) ) ;
				sqlCmd = sqlCmd.replace( "<BASECODE>", utils.enfoldString( ontCode ) ) ;
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
			throw new UploaderException( "Failed to insert date branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertDate()" ) ;
		}
	}
	
	
	private void insertSearchableText( Connection connection ) throws UploaderException {
		enterTrace( "OntologyBranch.insertSearchableText()" ) ;
		try {
			
			Statement st = connection.createStatement() ;
			String sqlCmd = null ;
			String fullName = "\\" + projectId + "\\" + colName + "\\" ;
			if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {
				
				sqlCmd = METADATA_SQL_INSERT_COMMAND ;
				
				String date = utils.formatDate( new Date() ) ;
				String metadataxml = METADATAXML ;
				metadataxml = metadataxml.replace( "<date-time-goes-here>", date + " 00:00:00" ) ;
				metadataxml = metadataxml.replace( "<code-name-goes-here>", ontCode ) ;
				metadataxml = metadataxml.replace( "<name-goes-here>", colName ) ;
				metadataxml = metadataxml.replace( "<data-type-goes-here>", "String" ) ; 
				metadataxml = metadataxml.replace( "<units-go-here>", "" ) ;
				
				log.debug( "For ontCode " + ontCode + " numeric units are: " + units ) ;
				
				sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", projectId ) ;
				sqlCmd = sqlCmd.replace( "<PROJECT_METADATA_TABLE>", projectId ) ;
				
				sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 1 ) ) ;
				sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( colName ) ) ;
				sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
				sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "LA" ) ) ;
				sqlCmd = sqlCmd.replace( "<BASECODE>", utils.enfoldString( ontCode ) ) ;
				sqlCmd = sqlCmd.replace( "<METADATAXML>", utils.enfoldNullableString( metadataxml ) ) ;
				sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "T" ) ) ;
				sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
				sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
				
				st = connection.createStatement();
				
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
			throw new UploaderException( "Failed to insert string branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertSearchableText()" ) ;
		}
	}	
	
	//
	// Need to cater for lookups for bottom leaves
	// The subcategory would be replaced
	private void insertEnumeratedString( Connection connection ) throws UploaderException {
		enterTrace( "OntologyBranch.insertEnumeratedString()" ) ;
		try {
			if( colName.equalsIgnoreCase( "CL_STATUS" ) 
				||
				colName.equalsIgnoreCase( "DEATH" ) ) {
					
				log.debug( "About to process " + colName ) ;
					
			}
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
				
				sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", projectId ) ;
				sqlCmd = sqlCmd.replace( "<PROJECT_METADATA_TABLE>", projectId ) ;
				
				sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 1 ) ) ;
				sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
				sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( colName ) ) ;
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
				String lookup = subCategory ; // default to what is there
				
				if( lookups.containsKey( colName ) ) {
					lookup = lookups.get( colName + ":" + subCategory ) ;
				}
				
				fullName = "\\" + projectId + "\\" + colName + "\\" + lookup + "\\" ;
				if( !pathsAndCodes.contains( PATH_PREFIX + fullName ) ) {
					
					sqlCmd = METADATA_SQL_INSERT_COMMAND ;
					
					sqlCmd = sqlCmd.replaceAll( "<DB_SCHEMA_NAME>", projectId ) ;
					sqlCmd = sqlCmd.replace( "<PROJECT_METADATA_TABLE>", projectId ) ;
					
					sqlCmd = sqlCmd.replace( "<HLEVEL>", utils.enfoldInteger( 2 ) ) ;
					sqlCmd = sqlCmd.replace( "<FULLNAME>", utils.enfoldString( fullName ) ) ;
					sqlCmd = sqlCmd.replace( "<NAME>", utils.enfoldString( lookup ) ) ;
					sqlCmd = sqlCmd.replace( "<SYNONYM_CD>", utils.enfoldString( "N" ) ) ;
					sqlCmd = sqlCmd.replace( "<VISUALATTRIBUTES>", utils.enfoldString( "LA" ) ) ;
					sqlCmd = sqlCmd.replace( "<BASECODE>", utils.enfoldString( ontCode + ":" + subCategory ) ) ;
					sqlCmd = sqlCmd.replace( "<METADATAXML>", "NULL" ) ;
					sqlCmd = sqlCmd.replace( "<COLUMNDATATYPE>", utils.enfoldString( "T" ) ) ;
					sqlCmd = sqlCmd.replace( "<OPERATOR>", utils.enfoldString( "LIKE" ) ) ;
					sqlCmd = sqlCmd.replace( "<DIMCODE>", utils.enfoldString( fullName ) ) ;
					sqlCmd = sqlCmd.replace( "<TOOLTIP>", utils.enfoldNullableString( toolTip + ":" + lookup ) ) ;
					sqlCmd = sqlCmd.replace( "<SOURCESYSTEM_CD>", utils.enfoldNullableString( projectId ) ) ;
					
					st.execute( sqlCmd ) ;
					//
					// Insert concept into concept dimension...
					insertIntoConceptDimension( st, fullName, ontCode + ":" + subCategory, colName + " " + lookup ) ;
					//
					// Record the path name so we don't try and duplicate it next time...
					pathsAndCodes.add( PATH_PREFIX + fullName ) ;
				}
				
			}
		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert string branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertEnumeratedString()" ) ;
		}
	}
	

	public Type getType() {
		return type;
	}	
	
	
	public String getOntCode() {
		return ontCode;
	}
	
	
	public String getUnits() {
		return units ;
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


	private String getColName() {
		return colName;
	}

}
