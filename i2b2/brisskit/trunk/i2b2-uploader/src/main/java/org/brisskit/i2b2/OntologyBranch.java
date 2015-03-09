/**
 * 
 */
package org.brisskit.i2b2;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.* ;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *   
 * @author jeff
 */
public class OntologyBranch {
	
	private static Logger logger = Logger.getLogger( OntologyBranch.class ) ;
	
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
	public static final String METADATA_INSERT_SQL_KEY = "METADATA_INSERT_SQL" ;
	public static final String METADATA_INSERT_SQL = 
			"INSERT INTO <PROJECT_METADATA_TABLE>" +
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
	         "VALUES( ?" +
	               ", ?" +
	               ", ?" +
	               ", ?" +         // 1 char
	               ", ?" +   // 3 chars
	               ", NULL" +				  // totalnum
	               ", ?" +
	               ", ?" +
	               ", 'concept_cd'" +
	               ", 'concept_dimension'" +
	               ", 'concept_path'" +
	               ", ?" +
	               ", ?" +
	               ", ?" +
	               ", NULL" +				  // comment
	               ", ?" +
	               ", '@'" +				  // applied path
	               ", now()" +
	               ", now()" +
	               ", now()" +
	               ", ?" +
	               ", NULL ) ;" ;			  // valuetype_cd
	
	public static final int METADATA_INDEX_HLEVEL = 1 ;
	public static final int METADATA_INDEX_FULLNAME = 2 ;
	public static final int METADATA_INDEX_NAME = 3 ;
	public static final int METADATA_INDEX_SYNONYM_CD = 4 ;
	public static final int METADATA_INDEX_VISUALATTRIBUTES = 5 ;
	public static final int METADATA_INDEX_BASECODE = 6 ;
	public static final int METADATA_INDEX_METADATAXML = 7 ;
	public static final int METADATA_INDEX_COLUMNDATATYPE = 8 ;
	public static final int METADATA_INDEX_OPERATOR = 9 ;
	public static final int METADATA_INDEX_DIMCODE = 10 ;
	public static final int METADATA_INDEX_TOOLTIP = 11 ;
	public static final int METADATA_INDEX_SOURCESYSTEM_CD = 12 ;
	
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
	
	
	//
	// Required query for establishing whether the concept code
	// is already in the database.
	// NB:
	// (1) The ontcode can be extended for numeric enumerations.
	// (2) We are only interested in Leaf nodes.
	public static final String CONCEPT_CODE_SELECT_SQL_KEY = "CONCEPT_CODE_SELECT_SQL" ;
	public static final String CONCEPT_CODE_SELECT_SQL = 
			"SELECT * FROM <PROJECT_METADATA_TABLE> " +
			"WHERE C_BASECODE LIKE ? AND C_VISUALATTRIBUTES LIKE 'L%'" +
			"ORDER BY C_BASECODE;" ;
	
	public static final String CONCEPT_DIMENSION_INSERT_SQL_KEY = "CONCEPT_DIMENSION_INSERT_SQL" ;
	public static final String CONCEPT_DIMENSION_INSERT_SQL = 
			"INSERT INTO CONCEPT_DIMENSION" +
			       "( CONCEPT_PATH" +    // 	VARCHAR(700) NOT NULL
			       ", CONCEPT_CD" +      // 	VARCHAR(50) NULL
			       ", NAME_CHAR" +	     //  	VARCHAR(2000) NULL
			       ", CONCEPT_BLOB" +    //  	TEXT NULL
			       ", UPDATE_DATE" +     //  	TIMESTAMP NULL
			       ", DOWNLOAD_DATE" +   //  	TIMESTAMP NULL
			       ", IMPORT_DATE" +     //  	TIMESTAMP NULL
			       ", SOURCESYSTEM_CD" + //		VARCHAR(50) NULL
			       ", UPLOAD_ID )" +	 //		INT NULL
			"VALUES ( ?" +
	               ", ?" +
	               ", ?" +
	               ", NULL" +  			// concept blob       
	               ", now()" +
	               ", now()" +
	               ", now()" +
	               ", ?" +
	               ", NULL ) ;" ;		// upload id
	
	public static final int CONCEPT_DIMENSION_INDEX_CONCEPT_PATH = 1 ;
	public static final int CONCEPT_DIMENSION_INDEX_CONCEPT_CD = 2 ;
	public static final int CONCEPT_DIMENSION_INDEX_NAME_CHAR = 3 ;
	public static final int CONCEPT_DIMENSION_INDEX_SOURCESYSTEM_CD = 4 ;
	
	public static final String CONCEPT_COUNT_SELECT_SQL_KEY = "CONCEPT_COUNT_SELECT_SQL" ;
	public static final String CONCEPT_COUNT_SELECT_SQL = 
			"SELECT COUNT(*) FROM <PROJECT_METADATA_TABLE> WHERE C_FULLNAME = ?" ;
	
	public static final String BREAKDOWNS_INSERT_SQL_KEY = "BREAKDOWNS_INSERT_SQL" ;
	public static final String BREAKDOWNS_INSERT_SQL = 
			"INSERT INTO QT_BREAKDOWN_PATH" +
			                "( NAME" +
			                ", VALUE" +
			                ", CREATE_DATE" +
			                ", UPDATE_DATE" +
			                ", USER_ID ) " +
	         "VALUES( ?" +
	               ", ?" +
	               ", now()" +
	               ", now()" +
	               ", NULL ) ;" ;
	
	
	
	
	public static final String[][] SPECIAL_CHARS_TRANSLATION_TABLE  = {		
		{ "&", " and " } ,
		{ "'", " apostrophe " } ,
		{ "*", " asterisk " } ,
		{ "@", " at " } ,
		{ "`", " back quote " } ,
		{ "\\", " back slash " } ,
		{ "^", " carat " } ,
		{ "}", " close brace " } ,
		{ "]", " close bracket " } ,
		{ ")", " close parenthesis " } ,
		{ ":", " colon " } ,
		{ ",", " comma " } ,
		{ "$", " dollar " } ,
		{ "=", " equals " } ,
		{ "!", " exclamation mark " } ,
		{ ">", " greater than " } ,
		{ "<", " less than " } ,
		{ "-", " hyphen " } ,
		{ "{", " open brace " } ,
		{ "[", " open bracket " } ,
		{ "(", " open parenthesis " } ,
		{ "%", " percent " } ,
		{ "|", " pipe " } ,
		{ "+", " plus " } ,
		{ "#", " hash " } ,
		{ "\"", " quote " } , 
		{ ";", " semicolon " } ,
		{ "/", " forward slash " } ,
		{ "~", " tilde " } ,
		{ "_", " underscore " }			
	} ;

	//
	// Project id (required for schema and some column values )
	private String projectId ;
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
//	private PreparedStatement preparedStatment = 
//				utils.getDbAccess().getSimpleConnectionPG().prepareStatement( "" ) ;
	//
	// The range of values encountered within the spreadsheet for this particular column...
	private HashSet<String> values ;
	//
	// Only required if code lookups are provided 
	private Map<String,String> lookups ;
	
	private OntologyBranch() {}
	
	private OntologyBranch( String projectId
			              , ProjectUtils utils ) {
		this.projectId = projectId ;
		this.utils = utils ;
	}
	
	private OntologyBranch( String projectId
	  		              , String colName
			              , String toolTip
			              , String ontCode
			              , Type type
			              , String units
			              , Map<String,String> lookups
			              , HashSet<String> values
			              , ProjectUtils utils ) {
		enterTrace( "OntologyBranch()" ) ;
		this.projectId = projectId ;
		this.colName = colName ;
		this.toolTip = toolTip ;
		this.ontCode = ontCode ;
		this.type = type ;
		this.units = units.trim() ;  // ensures this MUST NOT BE null
		this.lookups = lookups ;
		this.values = values ;
		this.utils = utils ;
		exitTrace( "OntologyBranch()" ) ;
	}
	
	
	public void serializeToDatabase() throws UploaderException {
		enterTrace( "OntologyBranch.serializeToDatabase()" ) ;
		try {		
			if( this.colName.equalsIgnoreCase( "weight" ) ) {
				logger.debug( "time for a debug" ) ;
			}
			insertRoot() ;
			
			switch( type ) {
			case NUMERIC:
				if( units.equalsIgnoreCase( "enum" ) ) {
					insertEnumeratedNumeric() ;
				}
				else {
					insertNumeric() ;
				}				
				break ;
			case DATE:
				insertDate() ;
				break ;
			case STRING:
			default:
				if( units.equalsIgnoreCase( "text" ) ) {
					insertSearchableText() ;
				}
				else {
					insertEnumeratedString() ;
				}				
				break;
			}
		}
		finally {
			exitTrace( "OntologyBranch.serializeToDatabase()" ) ;
		}
	}
	
	
	public void serializeDifferencesToDatabase( OntologyBranch that ) throws UploaderException {
		enterTrace( "OntologyBranch.serializeDifferencesToDatabase()" ) ;
		try {	
			//
			// Deal with the nonsensical first...
			if( !this.projectId.equals( that.projectId ) ) {
				String message = "Project ids differ. Project this: [" + this.projectId + 
						         "] whilst Project that: [" + that.projectId + "]" ;
				logger.error( message ) ;
				throw new UploaderException( message ) ;
			}
			
			logger.debug( "\nontThis colName: [" + this.colName + "] \nontThat colName: [" + that.getColName() + "]" ) ;
			logger.debug( "\nontThis units: [" + this.units + "] \nontThat units: [" + that.getUnits() + "]" ) ;	
			logger.debug( "\nontThis type: [" + this.type + "] \nontThat type: [" + that.getType() + "]" ) ;
			//
			// If we get this far in the process, all additions must be enumerations...
			if( units.equalsIgnoreCase( "enum" ) ) {
				//
				// Merge previous values into the target first...
				this.values.addAll( that.values ) ;	
				//
				// Insert the differences...
				switch ( this.type ) {
				case NUMERIC:
					insertEnumeratedNumeric() ;
					break;
				case STRING:
					insertEnumeratedString() ;
					break;
				case DATE:
					insertDate() ;
				default:
					String message = "Differences must be enumerations (numerics or strings) or dates. Type was: " + this.type ;
					logger.error( message ) ;
					throw new UploaderException( message ) ;
				}
			}
			else if( units.equalsIgnoreCase( "text" ) ) {
				logger.debug( "units as text with column name: " + this.colName ) ;
			}
			else {
				String message = "Differences must be marked as enumerations. Units were: [" + units + "]";
				logger.error( message ) ;
				throw new UploaderException() ;
			}		
		}
		finally {
			exitTrace( "OntologyBranch.serializeDifferencesToDatabase()" ) ;
		}
	}
	
	
	public boolean equals( OntologyBranch that ) {
		enterTrace( "OntologyBranch.equals()" ) ;
		boolean equality = false ;
		try {
			//
			// I instituted a mainline scope because the debugger
			// showed some very strange behaviours going on with
			// the previous strategy, which was to have multiple
			// returns within this method.
			// The starting assumption is that the branches are unequal.
			mainline : {
				if( !this.projectId.equals( that.projectId ) ) {
					break mainline ;
				}
				if( !this.colName.equals( that.colName ) ) {
					break mainline ;
				}
				if( !this.ontCode.equals( that.ontCode ) ) {
					break mainline ;
				}
				if( !this.units.equals( that.units ) ) {
					break mainline ;
				}
				if( this.type != that.type ) {
					//
					// What the following is saying is that Type.STRING and Type.DATE
					// are for our purposes compatible (ie: both represent enumerated strings).
					// Any other combinations are incompatible!!!
					if( this.type == Type.NUMERIC || that.type == Type.NUMERIC ) {
						break mainline ;
					}
				}
				//
				// If either is of Type.DATE then the ontCode is the boss
				// and we have already tested for equality of ontCode...
				if( this.type == Type.DATE || that.type == Type.DATE ) {
					equality = true ;
					break mainline ;
				}
				//
				// We can only use values when the type is numeric and it is an enumeration.
				// Reason: we only need to test for equality between OntologyBranch(es)
				// when we are looking across two different spreadsheets.
				// We don't easily have the previous spreadsheets values.
				// We could get them from the database but it would serve no purpose here.
				if( this.type == Type.NUMERIC 
						&& 
						this.units.equalsIgnoreCase( "enum" ) ) {
					//
					// If the values collections differ in size there must be some difference...
					if( this.values.size() != that.values.size() ) {
						break mainline ;
					}
					//
					// But if not, then the values must match exactly...
					Iterator<String> it = this.values.iterator() ;
					while( it.hasNext() ) {
						String value = it.next() ;
						if( !that.values.contains( value ) ) {
							break mainline ;
						}
					}
	
				}
				
				//
				// If we get this far, we assume the two branches are equal
				equality = true ;
			
			} // end mainline
			
			return equality ;
			
		}
		finally {
			exitTrace( "OntologyBranch.equals()" ) ;
		}
	}
	
	
	private void insertRoot() throws UploaderException {
		enterTrace( "OntologyBranch.insertRoot()" ) ;
		PreparedStatement ps = null ;
		try {
			
			String fullName = "\\" + projectId + "\\" ;		
			if( !nodeExists( fullName ) ) {
				ps = utils.getPsHolder().getPreparedStatement( METADATA_INSERT_SQL_KEY ) ;
				fillMetadataPS( ps
							  , 0
							  , fullName
							  , projectId
							  , "N"
							  , "FA"
							  , null
							  , null
							  , "T"
							  , "LIKE"
							  , fullName
							  , fullName
							  , projectId ) ;					
				ps.execute() ;
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
	
	
	private void fillMetadataPS( PreparedStatement ps 
							   , int hlevel 
							   , String fullName
							   , String name
							   , String synonymCd
							   , String visualAttributes
							   , String baseCode
							   , String metadataxml
							   , String columnDataType 
							   , String operator
							   , String dimcode
							   , String tooltip
							   , String sourceSystemId ) 
									  throws SQLException {
		ps.setInt( 1, hlevel ) ;
		ps.setString( 2, fullName ) ;
		ps.setString( 3, name ) ;
		ps.setString( 4, synonymCd ) ;
		ps.setString( 5, visualAttributes ) ;
		if( baseCode == null ) {
			ps.setNull( 6, java.sql.Types.VARCHAR ) ;
		}
		else {
			ps.setString( 6, baseCode ) ;
		}
		if( metadataxml == null ) {
			ps.setNull( 7, java.sql.Types.LONGVARCHAR ) ;
		}
		else {
			ps.setString( 7, metadataxml ) ;
		}
		ps.setString( 8, columnDataType ) ;
		ps.setString( 9, operator ) ;
		ps.setString( 10, dimcode ) ;
		if( tooltip == null ) {
			ps.setNull( 11, java.sql.Types.VARCHAR ) ;
		}
		else {
			ps.setString( 11, tooltip ) ;
		}
		if( sourceSystemId == null ) {
			ps.setNull( 12, java.sql.Types.VARCHAR ) ;
		}
		else {
			ps.setString( 12, sourceSystemId ) ;
		}

	}
	
	private void insertNumeric() throws UploaderException {
		enterTrace( "OntologyBranch.insertNumeric()" ) ;
		try {			
			String fullName = "\\" + projectId + "\\" + colName + "\\" ;
			if( !nodeExists( fullName ) ) {	
				PreparedStatement ps = utils.getPsHolder().getPreparedStatement( METADATA_INSERT_SQL_KEY ) ;			
				String date = utils.formatDate( new Date() ) ;
				
				String metadataxml = METADATAXML ;
				metadataxml = metadataxml.replace( "<date-time-goes-here>", date + " 00:00:00" ) ;
				metadataxml = metadataxml.replace( "<code-name-goes-here>", ontCode ) ;
				metadataxml = metadataxml.replace( "<name-goes-here>", colName ) ;
				metadataxml = metadataxml.replace( "<data-type-goes-here>", "PosFloat" ) ; 
				metadataxml = metadataxml.replace( "<units-go-here>", units ) ;				
				logger.debug( "For ontCode " + ontCode + " numeric units are: " + units ) ;
				
				fillMetadataPS( ps
							  , 1
							  , fullName
							  , colName
							  , "N"
							  , "LA"
							  , ontCode
							  , metadataxml
							  , "T"
							  , "LIKE"
							  , fullName
							  , toolTip
							  , projectId ) ;
				
				ps.execute() ;
				//
				// Insert concept into concept dimension...
				insertIntoConceptDimension( fullName, ontCode, colName ) ;
			}			
		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert decimal branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertNumeric()" ) ;
		}
	}
	
	
	private void insertEnumeratedNumeric() throws UploaderException {
		enterTrace( "OntologyBranch.insertEnumeratedNumeric(Connection,boolean)" ) ;
		try {
			//
			// Inserts are inserted as enumerations, so on two/three levels:
			// The base code; eg: Age
			// Possible ranges of values; eg: 1-10, 11-20, 21-30 etc
			// End point values; eg: 1, 2, 3, 4, 5 etc
			
			//
			// Insert the base code...
			String fullName = "\\" + projectId + "\\" + colName + "\\" ;
			PreparedStatement ps = utils.getPsHolder().getPreparedStatement( METADATA_INSERT_SQL_KEY ) ;	
			if( !nodeExists( fullName ) ) {
				fillMetadataPS( ps
						      , 1
						      , fullName
						      , colName
						      , "N"
						      , "FA"
						      , null
						      , null
						      , "T"
						      , "LIKE"
						      , fullName
						      , fullName
						      , projectId ) ;
				ps.execute() ;
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
			logger.debug( "format string is " + formatString ) ;
			
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
					if( !nodeExists( endPointFullName ) ) {
						fillMetadataPS( ps
									  , 2
									  , endPointFullName
									  , colName + ":" + paddedValue
									  , "N"
									  , "LA"
									  , ontCode + ":" + j
									  , null
									  , "T"
									  , "LIKE"
									  , endPointFullName
									  , toolTip + ":" + paddedValue
									  , projectId ) ;					
						ps.execute() ;
						//
						// Insert concept into concept dimension...
						insertIntoConceptDimension( endPointFullName
												  , ontCode + ":" + j
												  , colName + ":" + j ) ;						
					}
					
				} // end inner for
			}
			// Otherwise we use ranges of values based on a base range of 10...
			else {
				logger.debug( "lowestValue: [" + lowestValue + "] ; highestValue: [" + highestValue + "]" ) ;
				if( (highestValue - lowestValue) > 5000 ) {
					throw new UploaderException( "Column with name \"" + colName + "\" produces an enumerated numeric containing more than 5000 nodes.\n" +
							"Please define this as a numeric value column using the [] parameter.\nOr are you failing to format a date properly?" ) ;
				}
				//
				// Insert the ranges...
				String rangeFullName = null ;
				String rangeShortName = null ;
				for( int i=lowestValue; i<highestValue+1; i=i+10 ) {
					String lower = String.format( formatString, i ) ;
					String upper = String.format( formatString, i+9 ) ;
					rangeShortName = lower + "-" + upper ;
					rangeFullName = fullName + rangeShortName + "\\" ;	
					if( !nodeExists( rangeFullName ) ) {	
						fillMetadataPS( ps
									  , 2
									  , rangeFullName
									  , colName + ":" + rangeShortName
									  , "N"
									  , "FA"
									  , null
									  , null
									  , "T"
									  , "LIKE"
									  , rangeFullName
									  , toolTip + ": " + rangeShortName
									  , projectId ) ;
						ps.execute() ;						
					}	
					
					//
					// Insert the end points...
					String endPointFullName = null ;
					for( int j=i; j<i+10; j++ ) {
						String paddedValue = String.format( formatString, j ) ;
						// inclusion of colon is an error (but still works)
						endPointFullName = rangeFullName + paddedValue + "\\" ;	
						if( !nodeExists( endPointFullName ) ) {
							fillMetadataPS( ps
										  , 3
										  , endPointFullName
										  , colName + ":" + paddedValue
										  , "N"
										  , "LA"
										  , ontCode + ":" + j
										  , null
										  , "T"
										  , "LIKE"
										  , endPointFullName
										  , toolTip + ":" + paddedValue
										  , projectId ) ;
							ps.execute() ;
							//
							// Insert concept into concept dimension...
							insertIntoConceptDimension( endPointFullName
													  , ontCode + ":" + j
													  , colName + ":" + j ) ;
						}
						
					} // end inner for
									
				} // end outer for
				
			}
			
		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert integer branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertEnumeratedNumeric(Connection,boolean)" ) ;
		}
	}
	
	
	private void insertIntoConceptDimension( String conceptPath
										   , String conceptCode
										   , String conceptName ) throws UploaderException {
		enterTrace( "OntologyBranch.insertIntoConceptDimension()" ) ;
		try {
			PreparedStatement ps = utils.getPsHolder().getPreparedStatement( CONCEPT_DIMENSION_INSERT_SQL_KEY ) ;
			ps.setString( 1, conceptPath ) ;
			if( conceptCode == null ) {
				ps.setNull( 2, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 2, conceptCode ) ;
			}
			if( conceptName == null ) {
				ps.setNull( 3, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 3, conceptName ) ;
			}
			if( projectId == null ) {
				ps.setNull( 4, java.sql.Types.VARCHAR ) ;
			}
			else {
				ps.setString( 4, projectId ) ;
			}
			ps.addBatch() ;
		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert concept into concept dimension.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertIntoConceptDimension()" ) ;
		}
	}
	
	
	private void insertDate() throws UploaderException {
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
			if( !nodeExists( fullName ) ) {
				PreparedStatement ps = utils.getPsHolder().getPreparedStatement( METADATA_INSERT_SQL_KEY ) ;
				fillMetadataPS( ps
							  , 1
							  , fullName
							  , colName
							  , "N"
							  , "LA"
							  , ontCode
							  , null
							  , "T"
							  , "LIKE"
							  , fullName
							  , toolTip
							  , projectId ) ;
				ps.execute() ;
				//
				// Insert concept into concept dimension...
				insertIntoConceptDimension( fullName, ontCode, colName ) ;
			}

		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert date branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertDate(Connection,boolean)" ) ;
		}
	}
	
	
	private void insertSearchableText() throws UploaderException {
		enterTrace( "OntologyBranch.insertSearchableText()" ) ;
		try {

			String fullName = "\\" + projectId + "\\" + colName + "\\" ;
			if( !nodeExists( fullName ) ) {
				PreparedStatement ps = utils.getPsHolder().getPreparedStatement( METADATA_INSERT_SQL_KEY ) ;
				
				String date = utils.formatDate( new Date() ) ;
				String metadataxml = METADATAXML ;
				metadataxml = metadataxml.replace( "<date-time-goes-here>", date + " 00:00:00" ) ;
				metadataxml = metadataxml.replace( "<code-name-goes-here>", ontCode ) ;
				metadataxml = metadataxml.replace( "<name-goes-here>", colName ) ;
				metadataxml = metadataxml.replace( "<data-type-goes-here>", "String" ) ; 
				metadataxml = metadataxml.replace( "<units-go-here>", "" ) ;
				logger.debug( "For ontCode " + ontCode + " units are: " + units ) ;

				fillMetadataPS( ps
							  , 1
							  , fullName
							  , colName
							  , "N"
							  , "LA"
							  , ontCode
							  , metadataxml
							  , "T"
							  , "LIKE"
							  , fullName
							  , toolTip
							  , projectId ) ;
				ps.execute() ;
				//
				// Insert concept into concept dimension...
				insertIntoConceptDimension( fullName, ontCode, colName ) ;
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
	private void insertEnumeratedString() throws UploaderException {
		enterTrace( "OntologyBranch.insertEnumeratedString(Connection)" ) ;
		try {
			if( colName.equalsIgnoreCase( "CL_STATUS" ) 
				||
				colName.equalsIgnoreCase( "DEATH" ) ) {
					
				logger.debug( "About to process " + colName ) ;
					
			}
			//
			// Strings are inserted as enumerations, so on two levels:
			// The base code; eg: marital_status
			// The possible range of values; eg: single, married, widowed, separated, divorced ...
			
			//
			// Insert the base code...
			String fullName = "\\" + projectId + "\\" + colName + "\\" ;
			PreparedStatement ps = utils.getPsHolder().getPreparedStatement( METADATA_INSERT_SQL_KEY ) ;
			if( !nodeExists( fullName ) ) {
				fillMetadataPS( ps
							  , 1
							  , fullName
							  , colName
							  , "N"
							  , "FA"
							  , null
							  , null
							  , "T"
							  , "LIKE"
							  , fullName
							  , toolTip
							  , projectId ) ;		
				ps.execute() ;
			}
					
			//
			// Now insert the range of values in the enumeration ...
			
			Iterator<String> it = values.iterator() ;
			while( it.hasNext() ) {
				String subCategory = null ;
				String lookup = null ; 
				String baseCode = null ;
				String toolTip = null ;
				String name = null ;
				String conceptName = null ;
				subCategory = it.next() ;
				lookup = subCategory ; // default to what is there				
				if( lookups.containsKey( colName ) ) {
					lookup = lookups.get( colName + ":" + subCategory ) ;
				}
				baseCode = formEnumeratedBaseCode( ontCode, subCategory ) ;
				if( baseCode.length() > 200 ) {
					throw new UploaderException( "Ontology code exceeds 200 characters in length: " + baseCode ) ;
				}
				toolTip = formEnumeratedTooltip( this.toolTip, lookup ) ;
				//
				// NB: the backslashes are important. They must be there.
				//     They cannot be removed as per the above replaceAll()'s
				fullName = formEnumeratedFullName ( projectId, colName, lookup ) ;
				name = formEnumeratedName( lookup ) ;

				if( !nodeExists( fullName ) ) {
					fillMetadataPS( ps
								  , 2
								  , fullName
								  , name
								  , "N"
								  , "LA"
								  , baseCode
								  , null
								  , "T"
								  , "LIKE"
								  , fullName
								  , toolTip
								  , projectId ) ;
					ps.execute() ;			
					//
					// Insert concept into concept dimension...
					conceptName = formEnumeratedConceptName( colName, lookup) ;
					insertIntoConceptDimension( fullName
											  , baseCode
											  , conceptName ) ;		
				}
				
			} // end while
		}
		catch( SQLException sqlx ) {
			throw new UploaderException( "Failed to insert string branches into metadata table.", sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.insertEnumeratedString(Connection)" ) ;
		}
	}
	
	
	public static String formEnumeratedBaseCode( String ontCode, String enumValue ) {
		return ontCode + ':' + translateSpecialCharacters( enumValue ) ;
	}
	
	public static String formEnumeratedTooltip( String tooltip, String lookup ) {
		return tooltip + ':' + translateSpecialCharacters( lookup ) ;
	}
	
	public static String formEnumeratedFullName( String projectId, String colName, String lookup ) {
		return "\\" + projectId + "\\" + colName + "\\" + translateSpecialCharacters( lookup ) + "\\" ;
	}
	
	public static String formEnumeratedConceptName( String colName, String lookup ) {
		return colName + ' ' + translateSpecialCharacters( lookup ) ;
	}
	
	public static String formEnumeratedValue( String value ) {
		return translateSpecialCharacters( value ) ;	
	}
	
	public static String formEnumeratedName( String lookup ) {
		return translateSpecialCharacters( lookup ) ;
	}
	
	private static String translateSpecialCharacters( String fromString ) {
		// This version just removes all special characters...
		//String toString = fromString.replaceAll( "[^\\dA-Za-z ]", "" ) ;
		
		//
		// This version translates them to English words or phrases
		// (not terribly friendly, but consistent)...
		String toString = fromString ;
		for( int i=0; i<SPECIAL_CHARS_TRANSLATION_TABLE.length; i++ ) {
			toString = toString.replace( SPECIAL_CHARS_TRANSLATION_TABLE[i][0]
					                   , SPECIAL_CHARS_TRANSLATION_TABLE[i][1] ) ;
		}
		toString = toString.replaceAll( "  ", " " ) ;
		return toString ;
	}
	
	
	public boolean nodeExists( String fullName ) throws UploaderException {
		enterTrace( "OntologyBranch.nodeExists()" ) ;
		boolean exists = false ;
		try {
			//
			// See whether the base code exists in the db...
			PreparedStatement ps = utils.getPsHolder().getPreparedStatement( CONCEPT_COUNT_SELECT_SQL_KEY ) ;
			ps.setString( 1, fullName ) ;
			ResultSet rs = ps.executeQuery() ;			
		    if( rs.next() ) {
		    	int count = rs.getInt(1) ;
			    if( count > 0 ) {
			    	exists = true ;
			    }
				rs.close() ;
		    }
		    else {
		    	String message = "Failed to detect whether concept code was in DB or not. Count retrieved no result." ;
				logger.error( message ) ;
				throw new UploaderException( message ) ;
		    }
			return exists ;
		}
		catch( SQLException sqlx ) {
			String message = "Failed to detect whether concept code was in DB or not." ;
			logger.error( message, sqlx ) ;
			throw new UploaderException( message, sqlx ) ;
		}
		finally {
			exitTrace( "OntologyBranch.nodeExists()" ) ;
		}		
	}
	

	public String getProjectId() {
		return projectId;
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
	
	public String getColName() {
		return colName;
	}
	

	public String getToolTip() {
		return toolTip ;
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
    
    private static Type extractDataTypeFromMetadataXml( String metadataxml ) {
    	enterTrace( "OntologyBranch.extractDataTypeFromMetadataXml()" ) ;
    	try {
    		// <DataType><data-type-goes-here></DataType>
    		int fromIndex = metadataxml.indexOf( "<DataType>" ) + 10 ;
    		int toIndex = metadataxml.indexOf( "</DataType>" ) ;
    		String dataType = metadataxml.substring( fromIndex, toIndex ) ;
    		if( dataType.equalsIgnoreCase( "String" ) ) {
    			return Type.STRING ;
    		}
    		else if( dataType.equalsIgnoreCase( "PosFloat" ) ) {
    			return Type.NUMERIC ;
    		}
    		//
    		// If not, ensure things fall in a heap...
    		return null ;
    	}
    	finally {
    		exitTrace( "OntologyBranch.extractDataTypeFromMetadataXml()" ) ;
    	}
    }
    
    private static String extractUnitsFromMetadataXml( String metadataxml ) {
    	enterTrace( "OntologyBranch.extractUnitsFromMetadataXml()" ) ;
    	try {
    		// <NormalUnits><units-go-here></NormalUnits>
    		int fromIndex = metadataxml.indexOf( "<NormalUnits>" ) + 13 ;
    		int toIndex = metadataxml.indexOf( "</NormalUnits>" ) ;
    		String units = metadataxml.substring( fromIndex, toIndex ) ;
    		return units ;
    	}
    	finally {
    		exitTrace( "OntologyBranch.extractUnitsFromMetadataXml()" ) ;
    	}
    }

    public static class Factory {
    	
    	public static OntologyBranch newInstance( String projectId
								    			, String colName
									            , String toolTip
									            , String ontCode
									            , Type type
									            , String units
									            , Map<String,String> lookups
									            , HashSet<String> values
									            , ProjectUtils utils ) throws UploaderException {
			enterTrace( "OntologyBranch.Factory.newInstance()" ) ;
			OntologyBranch ob = null ;
			try {
				ob = new OntologyBranch( projectId
						    		   , colName
							           , toolTip
							           , ontCode
							           , type
							           , units
							           , lookups
							           , values
							           , utils ) ;
				return ob ;
			}
			finally {
				exitTrace( "OntologyBranch.Factory.newInstance()" ) ;
			}
		}
    	
    	
    	public static OntologyBranch newInstance( String projectId
								    			, String colName
								    			, String ontCode
								    			, String tooltip
								    			, Map<String,String> lookups
								    			, ProjectUtils utils ) throws UploaderException {
    		enterTrace( "OntologyBranch.Factory.newInstance(using DB)" ) ;
    		OntologyBranch ob = new OntologyBranch( projectId, utils ) ;
    		try {
    			PreparedStatement ps = utils.getPsHolder()
    								 		.getPreparedStatement( CONCEPT_CODE_SELECT_SQL_KEY ) ;
    			ps.setString( 1, ontCode + "%" ) ;
    			ResultSet rs = ps.executeQuery() ;
    			if( !rs.next() ) {
    				//
        			// NB: Returning a object null indicates this code does not exist in the database:
    				ob = null ;
    			}
    			else {
    				ob.projectId = projectId ;
    				ob.colName = colName ;
    				ob.ontCode = ontCode ; 
    				logger.debug( "\n" +
    						"ontCode: " + ontCode + "\n" +
    						"rs ontCode: " + rs.getString( "C_BASECODE" ) 
    				) ;
    				ob.toolTip = tooltip ;
    				ob.lookups = lookups ;
    				ob.utils = utils ;    				
    				ob.values = new HashSet<String>() ;
    				String metadataxml = rs.getString( "C_METADATAXML" ) ;
    				if( metadataxml != null ) {
    					ob.type = extractDataTypeFromMetadataXml( metadataxml ) ;
    					ob.units = extractUnitsFromMetadataXml( metadataxml ) ;
     				}
    				else {
    					ob.units = "enum" ;   					
    					String value = rs.getString( "C_BASECODE" ).trim() ;
    					int indexColon = value.indexOf( ':' ) ;
    					if( indexColon > 0 ) {
    						value = value.substring( indexColon+1 ) ;
    					}
    					//
    					// Collect all the "values"...
    					ob.values.add( value ) ;
    					while( rs.next() ) {
    						value = rs.getString( "C_BASECODE" ).trim() ;
    						indexColon = value.indexOf( ':' ) ;
    						value = value.substring( indexColon+1 ) ;
    						ob.values.add( value ) ;
    					}
    					ob.type = null ;
    					if( lookups.containsKey( colName ) ) {
    						// If there is a code lookup,
    						// we must treat this as of type STRING.
    						ob.type = Type.STRING ;
    					}
    					else {
    						//
    						// else we examine all the values
    						// NB: Date types are not applicable here
    						Iterator<String> it = ob.values.iterator() ;
        					while( it.hasNext() ) {
        						value = it.next() ;
        						if( utils.isNumeric( value ) ) {
        							if( ob.type == null ) {
        								ob.type = Type.NUMERIC ;
        							}
        							else if( ob.type == Type.NUMERIC ){
        								; // do nothing
        							}
        							else {
        								ob.type = Type.STRING ;
        							}						
        						}
        						else {
        							ob.type = Type.STRING ;
        						}
        					} // end while
    					}  					
    				}
    			}	
    			rs.close() ;
    			//
    			// NB: Returning null indicates this code does not exist in the database:
    			return ob ;
    		}
    		catch( SQLException sqlx ) {
    			throw new UploaderException( "Failed to detect whether concept code was in DB or not.", sqlx ) ;
    		}
    		finally {
    			exitTrace( "OntologyBranch.Factory.newInstance(using DB)" ) ;
    		}
    	}
    	
    }

}
