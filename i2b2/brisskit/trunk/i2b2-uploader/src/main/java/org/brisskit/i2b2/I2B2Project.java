/**
 * 
 */
package org.brisskit.i2b2;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory ;

import org.brisskit.i2b2.OntologyBranch.Type ;

/**
 * @author jeff
 *
 */
public class I2B2Project {
	
	public static final int DATA_SHEET_INDEX = 0 ;
	public static final int LOOKUP_SHEET_INDEX = 1 ;
	
	//
	// Data sheet values...
	public static final int COLUMN_NAME_ROW_INDEX = 0 ;
	public static final int TOOLTIPS_ROW_INDEX = 1 ;
	public static final int ONTOLOGY_CODES_ROW_INDEX = 2 ;
	public static final int FIRST_DATA_ROW_INDEX = 3 ;
	
	//
	// Code lookup sheet values...
	public static final int LOOKUP_COLUMN_NAME_ROW_INDEX = 0 ;
	public static final int CONCEPT_COLUMN_INDEX = 0 ;
	public static final int CODE_COLUMN_INDEX = 1 ;
	public static final int DESCRIPTION_COLUMN_INDEX = 2 ;
	
	public static final String CONCEPT_COLNAME = "concept" ;
	public static final String CODE_COLNAME = "code" ;
	public static final String DESCRIPTION_COLNAME = "description" ;
	
	private static Log log = LogFactory.getLog( I2B2Project.class ) ;
	
	private static StringBuffer logIndent = null ;
	
	private String projectId ;
    private File spreadsheetFile ;
    private Workbook workbook ;
    private Sheet dataSheet ;
    private Sheet lookupSheet ;
    private Row columnNames ;
    private Row toolTips ;
    private Row ontologyCodes ;
    private int numberColumns ;
    
    private int encounterNumber = 0 ;
    private int patientNumber = 0 ;
    
    private Map<String,Integer> patientMappings = new HashMap<String,Integer>() ;
    
    private Map<String,String> lookups = new HashMap<String,String>() ;
    
    private Map<String,OntologyBranch> ontBranches = new HashMap<String,OntologyBranch>() ;
    private ArrayList<PatientDimension> patientDims = new ArrayList<PatientDimension>() ;
    private ArrayList<PatientMapping> patientMaps = new ArrayList<PatientMapping>() ;
    private ArrayList<ObservationFact> observatonFacts = new ArrayList<ObservationFact>() ;
    
    private ProjectUtils utils = new ProjectUtils() ;
    
    @SuppressWarnings("unused")
	private I2B2Project() {}
    
    //
    // Removed admin userid and password.
    // We are defaulting to the demo system setup.
    // The recommendation should be the user changes passwords immediately.
    // And creates whatever other users are required.
    //
    // projectId must be alpha-numeric starting with an alpha,
    // and with no spaces.
    public I2B2Project( String projectId
    		          , File spreadsheetFile ) {
    	enterTrace( "I2B2Project()" ) ;
    	this.projectId = projectId ;
    	this.spreadsheetFile = spreadsheetFile ;
    	exitTrace( "I2B2Project()" ) ;
    }
    
    
    public void processSpreadsheet() throws UploaderException {
    	enterTrace( "I2B2Project.processSpreadsheet" ) ;
    	try {
    		readSpreadsheet() ;
			produceOntology() ;
			producePatientMapping() ;
			producePatientDimension() ;		
			produceFacts() ;
    	}
    	finally {
    		exitTrace( "I2B2Project.processSpreadsheet" ) ;
    	}
    }

    
//	public void create() throws NewProjectException {
//		enterTrace( "create()" ) ;
//		try {
//			readSpreadsheet() ;
//			produceOntology() ;
//			producePatientDimension() ;	
//			producePatientMapping() ;	
//			produceFacts() ;
//			//
//			// So far everything is in memory.
//			// Only if we get this far do we start on creating db artifacts and inserting data...
//			createDBArtifacts() ;
//			populateProject() ;
//		}
//		finally {
//			exitTrace( "create()" ) ;
//		}
//	}

	public void createDBArtifacts() throws UploaderException {
		enterTrace( "I2B2Project.createDBArtifacts()" ) ;
		try {
			CreateDBPG.setUp( "configpointer.properties" ) ;
			CreateDBPG.createI2B2Database( projectId );
		}
		finally {
			exitTrace( "I2B2Project.createDBArtifacts()" ) ;
		}
	}
	
	public boolean populateProject() throws UploaderException {
		enterTrace( "populateProject()" ) ;
		Connection connection = null ;
		try {
			connection = Base.getSimpleConnectionPG() ;
			
			Iterator<OntologyBranch> itOb = ontBranches.values().iterator() ;
			while( itOb.hasNext() ) {
				OntologyBranch ob = itOb.next() ;
				ob.serializeToDatabase( connection ) ;
			}
			//
			// Patient stuff ...
			Iterator<PatientDimension> itPd = patientDims.listIterator() ;
			while( itPd.hasNext() ) {
				PatientDimension pd = itPd.next() ;
				pd.serializeToDatabase( connection ) ;
			}
			Iterator<PatientMapping> itPm = patientMaps.listIterator() ;
			while( itPm.hasNext() ) {
				PatientMapping pm = itPm.next() ;
				pm.serializeToDatabase( connection ) ;
			}
			//
			// Observation facts...
			Iterator<ObservationFact> itOf = observatonFacts.listIterator() ;
			while( itOf.hasNext() ) {
				ObservationFact of = itOf.next() ;
				of.serializeToDatabase( connection ) ;
			}
		}
		finally {
			if( connection != null ) {
				try{ connection.close() ; }
				catch( SQLException sqlx ) {
					log.warn( "Failed to close SQL connection" ) ;
				}
			}
			exitTrace( "populateProject()" ) ;
		}
		return false ;
	}
	
	protected void readSpreadsheet() throws UploaderException {
		enterTrace( "readSpreadsheet()" ) ;
		try {
			//
			// We read the file and create the workbook
			InputStream inp = new FileInputStream( spreadsheetFile ) ;
		    workbook = WorkbookFactory.create( inp ) ;
		    //
		    // Check we have enough sheets...
		    int noSheets = workbook.getNumberOfSheets() ;
		    if( noSheets == 0 ) {
		    	throw new UploaderException( "Spreadsheet has no contents" ) ;
		    }
		    //
	    	// Get the data sheet, which must be the first sheet... 
	    	dataSheet = workbook.getSheetAt( DATA_SHEET_INDEX ) ;
		    if( noSheets > 1 ) {		    	
		       	//
			    // Check we have sufficient rows for a lookup sheet (as the 2nd sheet)...
		    	// ( Many spreadsheets have 2nd and 3rd sheets present but empty by default! )		    	
			    int numberRows = workbook.getSheetAt( LOOKUP_SHEET_INDEX ).getLastRowNum() + 1 ;
				if( numberRows > 2 ) {				
					//
			    	// The lookup sheet is to map column coded values to some meaningful description
			    	//  eg: 1 = Coronary hd
			    	//      2 = Coronary and hypertensive hd and so on)
			    	// The vision is to use the code as an enumerated value, but place a meaningful
			    	// description in the ontology tree.
					lookupSheet = workbook.getSheetAt( LOOKUP_SHEET_INDEX ) ;
			    	injestLookupTables() ;
				}
		    }		    
		    //
		    // Check we have sufficient data rows...
		    int numberDataRows = dataSheet.getLastRowNum() - FIRST_DATA_ROW_INDEX + 1;
			if( numberDataRows < 1 ) {
				throw new UploaderException( "The workbook has insufficient data rows: " + numberDataRows ) ;
			}
		    //
		    // The first three rows contain required metadata...
			// (Perhaps in future one more row for ontology tree structure? 
			//  ie: a path statement)
		    columnNames = dataSheet.getRow( COLUMN_NAME_ROW_INDEX ) ;
		    toolTips = dataSheet.getRow( TOOLTIPS_ROW_INDEX ) ;
		    ontologyCodes = dataSheet.getRow( ONTOLOGY_CODES_ROW_INDEX ) ;
		    //
		    // Could do with some basic checks to see all rows have the same number of columns!
		    numberColumns = columnNames.getLastCellNum() ;
		}
		catch( Exception ex ) {
			throw new UploaderException( ex ) ;
		}
		finally {
			exitTrace( "readSpreadsheet()" ) ;
		}
	}
	
	
	private void injestLookupTables() throws UploaderException {
		enterTrace( "i2b2Project.injestLookupTables()" ) ;
		try {			
			//
		    // Check we have sufficient rows...
		    int numberRows = lookupSheet.getLastRowNum() + 1 ;
			if( numberRows < 2 ) {
				throw new UploaderException( "The lookup sheet has insufficient rows: " + numberRows ) ;
			}
			//
			// Check we have sufficient columns...
			Row columnNameRow = lookupSheet.getRow( I2B2Project.LOOKUP_COLUMN_NAME_ROW_INDEX ) ;
			int numberCols = columnNameRow.getLastCellNum() ;
			if( numberCols != 3 ) {
				throw new UploaderException( "The lookup sheet has insufficient rows: " + numberCols ) ;
			}
			//
			// Check the format of the first row...
			String conceptHeading = utils.getValueAsString( columnNameRow.getCell( I2B2Project.CONCEPT_COLUMN_INDEX ) ) ;
			String codeHeading = utils.getValueAsString( columnNameRow.getCell( I2B2Project.CODE_COLUMN_INDEX ) ) ;
			String descriptionHeading = utils.getValueAsString( columnNameRow.getCell( I2B2Project.DESCRIPTION_COLUMN_INDEX ) ) ;
			if( !conceptHeading.equalsIgnoreCase( I2B2Project.CONCEPT_COLNAME ) 
				||
				!codeHeading.equalsIgnoreCase( I2B2Project.CODE_COLNAME )
				||
				!descriptionHeading.equalsIgnoreCase( I2B2Project.DESCRIPTION_COLNAME ) ) {
				
				throw new UploaderException( "Lookup sheet incorrectly formatted: first row has incorrect column headings." ) ;
			}
			Iterator<Row> rowIt = lookupSheet.rowIterator() ;
			//
			// Tab past column headings' row...
			rowIt.next() ;
			//
			// Process code lookup rows...
			while( rowIt.hasNext() ) {
				Row lookupRow = rowIt.next() ;	
				String name = utils.getValueAsString( lookupRow.getCell( I2B2Project.CONCEPT_COLUMN_INDEX ) ) ;
				String code = utils.getValueAsString( lookupRow.getCell( I2B2Project.CODE_COLUMN_INDEX ) ) ;
				String description = utils.getValueAsString( lookupRow.getCell( I2B2Project.DESCRIPTION_COLUMN_INDEX ) ) ;
				//
				// Place a special singular name tag within the collection.
				// (Really to make it easier to see whether the collection contains mappings for one column)
				if( !lookups.containsKey( name ) ) {
					lookups.put( name, name ) ;
				}
				//
				// Place a suitable name/code to description mapping in the collection...
				lookups.put( name + ":" + code, description ) ;
			}
			
		}
		finally {
			exitTrace( "i2b2Project.injestLookupTables()" ) ;
		}
	}
	
	
	protected void producePatientMapping() throws UploaderException {
		enterTrace( "producePatientMapping()" ) ;
		String value = null ;
		String code = null ;
		try {
			Iterator<Row> rowIt = dataSheet.rowIterator() ;
			//
			// Tab past metadata rows...
			rowIt.next() ;
			rowIt.next() ;
			rowIt.next() ;
			//
			// Process data rows...
			while( rowIt.hasNext() ) {
				Row dataRow = rowIt.next() ;	
				
				PatientMapping	pMap = new PatientMapping( utils ) ;
				pMap.setSchema_name( projectId ) ;
				pMap.setSourcesystem_id( projectId ) ;
				
				Iterator<Cell> cellIt = dataRow.cellIterator() ;
				Iterator<Cell> codeIt = ontologyCodes.cellIterator() ;
				//
				// We process each cell according to its code...
				while( cellIt.hasNext() ) {
					value = utils.getValueAsString( cellIt.next() ) ;
					code = utils.getValueAsString( codeIt.next() ) ;
					if( code.equalsIgnoreCase( "id" ) ) {
							pMap.setPatient_ide( value ) ;
							pMap.setPatient_ide_source( projectId ) ;
							pMap.setProject_id( projectId ) ;
							pMap.setPatient_ide_status( "?" ) ;			
					}
					
				} // end of inner while - processing cell	
				
				//
				// We set the internal i2b2 number for the mapping to source...
				// NB: patientNumber must only be changed in the current method!!!!
				pMap.setPatient_num( patientNumber++ ) ;
				this.patientMappings.put( pMap.getPatient_ide(), pMap.getPatient_num() ) ;
				
				//
				// For the moment we are saving in memory until all is ready
				// (We need to think how we can back out or restart if doing things incrementally) 				
				patientMaps.add( pMap ) ;
				
			} // end of outer while - processing row
			
		}
		finally {
			exitTrace( "producePatientMapping()" ) ;
		}		
	}
	
	
	protected void producePatientDimension() throws UploaderException {
		enterTrace( "producePatientDimension()" ) ;
		String value = null ;
		String code = null ;
		String sourceSystemPatientID = null ;
		try {
			Iterator<Row> rowIt = dataSheet.rowIterator() ;
			//
			// Tab past metadata rows...
			rowIt.next() ;
			rowIt.next() ;
			rowIt.next() ;
			//
			// Process data rows...
			while( rowIt.hasNext() ) {
				
				Row dataRow = rowIt.next() ;
				
				PatientDimension pDim = new PatientDimension( utils ) ;
				pDim.setSchema_name( projectId ) ;
				pDim.setSourcesystem_cd( projectId ) ;
				
				//
				// We process each cell according to its code...
				Iterator<Cell> cellIt = dataRow.cellIterator() ;
				Iterator<Cell> codeIt = ontologyCodes.cellIterator() ;
								
				while( cellIt.hasNext() ) {
					value = utils.getValueAsString( cellIt.next() ) ;
					code = utils.getValueAsString( codeIt.next() ) ;
					if( code.startsWith( "p_dim:" ) ) {
						String[] parts = code.split( ":" ) ;
						if( parts[1].equalsIgnoreCase( "age" ) ) {
							pDim.setAge_in_years( Integer.valueOf( value ) ) ;
						}
						else if( parts[1].equalsIgnoreCase( "vital_status" ) ) {
							pDim.setVital_status_cd( value ) ;
						}
						else if( parts[1].equalsIgnoreCase( "birth_date" ) ) {
							pDim.setBirth_date( utils.parseDate( value ) ) ;
							Date presentDate = new Date() ;
							Date birthDate = utils.parseDate( value ) ;
							Calendar birthdateCalendar = Calendar.getInstance() ;
							birthdateCalendar.setTime( birthDate ) ;
							Calendar presentCalendar = Calendar.getInstance() ;	
							presentCalendar.setTime( presentDate ) ;
							int age_in_years = 
									  presentCalendar.get( Calendar.YEAR ) 
									- birthdateCalendar.get( Calendar.YEAR ) ;
							if( presentCalendar.get( Calendar.DAY_OF_YEAR ) 
								<
								birthdateCalendar.get( Calendar.DAY_OF_YEAR ) ) {
								age_in_years-- ;
							}
							pDim.setAge_in_years( age_in_years ) ;
						}
						else if( parts[1].equalsIgnoreCase( "death_date" ) ) {
							pDim.setDeath_date( utils.parseDate( value ) ) ;
						}
						else {
							//
							// And so on. However,
							// for the moment we are not concentrating on queries using patient dimension.
						}
					}
					else if( code.equalsIgnoreCase( "id" ) ) {
						sourceSystemPatientID = value ;
					}
					
				} // end of inner while - processing cell	
				
				//
				// We do not expect the spreadsheet to contain the i2b2 internal patient number.
				pDim.setPatient_num( this.patientMappings.get( sourceSystemPatientID ) ) ;
								
				//
				// For the moment we are saving in memory until all is ready
				// (We need to think how we can back out or restart if doing things incrementally) 
				patientDims.add( pDim ) ;
								
			} // end of outer while - processing row
		}
		catch( ParseException pex ) {
			throw new UploaderException( "Failed to parse date: " + value, pex ) ;
		}
		finally {
			exitTrace( "producePatientDimension()" ) ;
		}		
	}
	
	
	protected void produceOntology() throws UploaderException {
		enterTrace( "produceOntology()" ) ;
		try {
			Row codesRow = dataSheet.getRow( ONTOLOGY_CODES_ROW_INDEX ) ;
			//
			// pathsAndCodes is a collection to help us write out
			// hierarchical ontology paths in the DB without
			// attempting to (erroneously) insert duplicated nodes.
			// For example: path root-node/demographics/age involves
			// writing three nodes, but the first two will be repeated
			// for some other path, for example: root-node/demographics/marital-status
			HashSet<String> pathsAndCodes = new HashSet<String>() ;
			String colName = null ;
			String toolTip = null ;
			String ontCode = null ;

			//
			// Values is for the range of values contained in a column.
			HashSet<String> values = null ;
			//
			// We process each cell in the code row.
			//
			// But (!) we need to know the type of the column value(s)
			// eg: whether these represent a numeric, date or string.
			// In order to do this we examine each data value in a given column,
			// so we end up iterating over rows to gather the values of a given column.
			Iterator<Cell> cellIt = codesRow.cellIterator() ;
			while( cellIt.hasNext() ) {
				Cell codeCell = cellIt.next() ;
				//
				// The default value is String
				OntologyBranch.Type type = Type.STRING ;
				//
				// Units are derived from the ontCode value in the spreadsheet.
				// Square brackets, eg: [cms] will contain the unit measure.
				// If the brackets are empty, the units are an implied value (eg: age[] would imply years)
				// An implied value can be whatever the user wishes to interpret it as.
				// 
				// NB: The special value "enum" is used internally to indicate enumerated values.
				//     Currently for numerics or strings, enums are auto-generated
				//     dependent upon the range of values encountered in the columns.
				//
				// Thus, age[] would be a numeric field searched on by value.
				// Whereas, age without the square bracket, would be an enumeration of ages
				String units = "enum" ;
				//
				// Get the ontology code and make adjustments for units
				ontCode = utils.getValueAsString( codeCell ) ;
				if( ontCode.contains( "[" ) ) {
					log.debug( "concept column value: " + ontCode ) ;
					int firstBracket = ontCode.indexOf( "[" ) ;
					int secondBracket = ontCode.indexOf( "]" ) ;
					units = ontCode.substring( firstBracket, secondBracket ) ;
					ontCode = ontCode.substring( firstBracket ) ;
					log.debug( "which yields concept: " + ontCode + " with units: " + units ) ;
				}
								
				int colIndex = codeCell.getColumnIndex() ;
				colName = utils.getValueAsString( dataSheet.getRow( I2B2Project.COLUMN_NAME_ROW_INDEX ).getCell( colIndex ) ) ;
				toolTip = utils.getValueAsString( dataSheet.getRow( I2B2Project.TOOLTIPS_ROW_INDEX ).getCell( colIndex ) ) ;
				//
				// If there is a code lookup, we must treat this as of type STRING.
				// Thus we examine the range of values to determine numerics or dates 
				// ONLY if there is not a code lookup for this column...
				if( !lookups.containsKey( colName ) ) {
										
					values = new HashSet<String>() ;
					log.debug( "Processing column with colName: [" + colName + "] toolTip: [" + toolTip + "] ontCode: [" + ontCode + "]" ) ;

					Iterator<Row> rowIt = dataSheet.rowIterator() ;
					rowIt.next() ; // tab past column names
					rowIt.next() ; // tab past tool tips
					rowIt.next() ; // tab past codes

					while( rowIt.hasNext() ) {
						Row row = rowIt.next() ;
						Cell dataCell = row.getCell( colIndex ) ;
						String value = utils.getValueAsString( dataCell ) ;
						if( utils.isNull( value ) ) {
							log.debug( "Encountered a cell with null value" ) ;
							continue ;
						}
						//
						// Add to the range of values encountered...
						values.add( value ) ;
						//
						// Decide whether numeric, date or string...
						if( utils.isNumeric( value ) ) {
							log.debug( "Cell with numeric value: " + value ) ;
							if( type == Type.STRING ) {
								type = Type.NUMERIC ;
							}
							else if( type == Type.NUMERIC ) {
								// If type has already been established as a decimal, do nothing
							}
						}
						else if( utils.isDate( value ) ) {
							log.debug( "Cell with date value: " + value ) ;
							if( type == Type.STRING ) {
								type = Type.DATE ;
							}
							else if( type != Type.DATE ) {
								log.error( "Cells with incompatible values; current value: " + value ) ;
							}
						}
						else {
							// We assume a string
							log.debug( "Cell with string value: " + value ) ;
						}

					} // end inner while
					
				} // endif
									
				//
				// We build each branch in memory and save it in a collection 
				OntologyBranch 
					branch = new OntologyBranch( projectId
							                   , colName
							                   , toolTip
							                   , ontCode
							                   , type
							                   , units
							                   , lookups
							                   , values
							                   , pathsAndCodes
							                   , utils ) ;
				
				ontBranches.put( branch.getOntCode(), branch ) ;
				
			} // end outer while 
			
			
			//
			// Indicates that we are creating the project's ontology
			// here for the very first time, as derived from the spreadsheet.
			if( isVirginOntology() ) {
				Iterator<OntologyBranch> itOb = ontBranches.values().iterator() ;
				while( itOb.hasNext() ) {
					OntologyBranch ob = itOb.next() ;
					ob.serializeToDatabase( Base.getSimpleConnectionPG() ) ;
				}
			}
			
		}
		finally {
			exitTrace( "produceOntology()" ) ;
		}		
	}
	
	//
	// Indicates that we are creating the project's ontology
	// here for the very first time, as derived from the spreadsheet.
	// It means we must write the stuff to the database!!!
	// The setting is derived from the spreadsheet by implication; ie: if tooltips
	// are missing (row present but no values), then this is the situation where
	// we are simply processing the spreadsheet for data (observation facts etc)
	// and we need ontology data simply for producing observation facts.
	//
	// This method requires more work, or at least some experimentation.
	private boolean isVirginOntology() {
		boolean virginOntology = true ;
		//
		// The first column is for id and may not have a tool tip anyway
		for( int i=1; i<6; i++ ) {
			
			Cell toolTipCell = dataSheet.getRow( I2B2Project.TOOLTIPS_ROW_INDEX ).getCell( i ) ;
			try {
				String tooltip = utils.getValueAsString( toolTipCell ) ;
				if( tooltip == null ) {
					virginOntology = false ;
				}
				else if( utils.isNull( tooltip ) ) {
					virginOntology = false ;
				}
				else if( tooltip.length() == 0 ) {
					virginOntology = false ;
				}
			}
			catch( Throwable th ) {
				virginOntology = false ;
			}
		}
		return virginOntology ;
	}
	
	
	protected void produceFacts() throws UploaderException {
		enterTrace( "I2B2Project.produceFacts()" ) ;
		try {
			Iterator<Row> rowIt = dataSheet.iterator() ;
			//
			// Tab past metadata rows...
			rowIt.next() ;
			rowIt.next() ;
			rowIt.next() ;
			//
			// Process data rows...
			while( rowIt.hasNext() ) {
				Row dataRow = rowIt.next() ;
				int patientNumber = getPatientNumber( dataRow ) ;
				//
				// Process the cells for each row...
				Iterator<Cell> cellIt = dataRow.cellIterator() ;
				while( cellIt.hasNext() ) {
					Cell cell = cellIt.next() ;		
					if( utils.getValueAsString( cell ).equalsIgnoreCase( "NULL" ) ) {
						continue ;
					}
					String ontCode = getOntCode( cell ) ;
					//
					// We bypass any columns which are patient mapping or patient dimension...
					if( ontCode.startsWith( "p_map:" ) || ontCode.startsWith( "p_dim:" ) ) {
						continue ;
					}
					else {
						
						OntologyBranch.Type type = getOntologyBranch( ontCode ).getType() ;
						ObservationFact of = null ;
						switch ( type ) {
						case DATE:
							of = produceDateFact( patientNumber, ontCode, cell ) ;
							break ;
						case NUMERIC:
							of = produceNumericFact( patientNumber, ontCode, cell ) ;
							break ;
						case STRING:
						default:
							of = produceStringFact( patientNumber, ontCode, cell ) ;
							break;
						}
						//
						// For the moment we are saving in memory until all is ready
						// (We need to think how we can back out or restart if doing things incrementally) 
						observatonFacts.add( of ) ;
					}
					
				} // end of inner while - processing cell	
				
			} // end of outer while - processing row
		}
		finally {
			exitTrace( "I2B2Project.produceFacts()" ) ;
		}		
	}
	
	
	private ObservationFact produceDateFact( int patientNumber
			                               , String ontCode
			                               , Cell cell ) throws UploaderException {
		enterTrace( "I2B2Project.produceDateFact()" ) ;
		try {
			ObservationFact of = new ObservationFact( utils ) ;				
			of.setEncounter_num( encounterNumber++ ) ;
			of.setPatient_num( patientNumber ) ;
			
			of.setConcept_cd( ontCode ) ;
			of.setProvider_id( "@" ) ;

			String value = utils.getValueAsString( cell ) ;	
			//
			// For dates, the value becomes the start date of the observation...
			of.setStart_date( utils.parseDate( value ) ) ;			
			of.setValtype_cd( "T" ) ;
			//
			// For dates, the value is a choice between "yes" and "no"
			// (it could be "true" and "false")
			of.setTval_char( "yes" ) ;
			
			of.setSourcesystem_cd( projectId ) ;
			of.setSchema_name( projectId ) ;
			return of ;
		}
		catch( ParseException pex ) {
			throw new UploaderException( "Could not parse start date", pex ) ;
		}
		finally {
			exitTrace( "I2B2Project.produceDateFact()" ) ;
		}
	}
	
	
	private ObservationFact produceNumericFact( int patientNumber
                                              , String ontCode
                                              , Cell cell ) throws UploaderException {
		enterTrace( "I2B2Project.produceNumericFact()" ) ;
		try {
			ObservationFact of = new ObservationFact( utils ) ;				
			of.setEncounter_num( encounterNumber++ ) ;
			of.setPatient_num( patientNumber ) ;
			
			of.setConcept_cd( ontCode ) ;
			of.setProvider_id( "@" ) ;
			of.setStart_date( new Date() ) ;
			
			String value = utils.getValueAsString( cell ) ;			

			of.setValtype_cd( "N" ) ;
			of.setTval_char( "E" ) ;
			of.setNval_num( Double.valueOf( value ) ) ;
			
			of.setSourcesystem_cd( projectId ) ;
			of.setSchema_name( projectId ) ;
			return of ;
		}
		finally {
			exitTrace( "I2B2Project.produceNumericFact()" ) ;
		}
	}
	
	
	private ObservationFact produceStringFact( int patientNumber
                                             , String ontCode
                                             , Cell cell ) throws UploaderException {
		enterTrace( "I2B2Project.produceStringFact()" ) ;
		try {
			ObservationFact of = new ObservationFact( utils ) ;				
			of.setEncounter_num( encounterNumber++ ) ;
			of.setPatient_num( patientNumber ) ;
			
			of.setConcept_cd( ontCode ) ;
			of.setProvider_id( "@" ) ;
			of.setStart_date( new Date() ) ;
		
			of.setValtype_cd( "T" ) ;					
			String value = utils.getValueAsString( cell ) ;	
			of.setTval_char( value ) ;
			
			of.setSourcesystem_cd( projectId ) ;
			of.setSchema_name( projectId ) ;
			return of ;
		}
		finally {
			exitTrace( "I2B2Project.produceStringFact()" ) ;
		}
	}

	
	protected OntologyBranch getOntologyBranch( String ontCode) {
		return ontBranches.get( ontCode ) ;
	}
	
	protected String getOntCode( Cell dataCell ) {	
		return utils.getValueAsString( dataSheet.getRow( I2B2Project.ONTOLOGY_CODES_ROW_INDEX ).getCell( dataCell.getColumnIndex() ) ) ;
	}
	
	
	public int getPatientNumber( Row dataRow ) {
		Iterator<Cell> cellIt = dataRow.getSheet().getRow( I2B2Project.ONTOLOGY_CODES_ROW_INDEX ).cellIterator() ;
		int patientNumberIndex = -1 ;
		while( cellIt.hasNext() ) {
			Cell cell = cellIt.next() ;
			String value = utils.getValueAsString( cell ) ;
			//
			// Search for the source systems id...
			if( value.equalsIgnoreCase( "id" ) ) {
				patientNumberIndex =  cell.getColumnIndex() ;
				break ;
			}			
		}
		String sourcePatientNoAsString = utils.getValueAsString( dataRow.getCell( patientNumberIndex ) )  ;		
		//
		// Given the source system patient identifier (as a string), 
		// we use the mappings to get the i2b2 internal id...		
		return this.patientMappings.get( sourcePatientNoAsString ) ;
	}
	

	
	/**
	 * Utility routine to enter a structured message in the trace log that the given method 
	 * has been entered. 
	 * 
	 * @param entry: the name of the method entered
	 */
	public static void enterTrace( String entry ) {
		enterTrace( log, entry ) ;
	}
	
	public static void enterTrace( Log log, String entry ) {
		log.trace( getIndent().toString() + "enter: " + entry ) ;
		indentPlus() ;
	}

    /**
     * Utility routine to enter a structured message in the trace log that the given method 
	 * has been exited. 
	 * 
     * @param entry: the name of the method exited
     */
    public static void exitTrace( String entry ) {
    	exitTrace( log, entry ) ;
	}
    
	public static void exitTrace( Log log, String entry ) {
		indentMinus() ;
		log.trace( getIndent().toString() + "exit: " + entry ) ;
	}
	
    /**
     * Utility method used to maintain the structured trace log.
     */
    public static void indentPlus() {
		getIndent().append( ' ' ) ;
	}
	
    /**
     * Utility method used to maintain the structured trace log.
     */
    public static void indentMinus() {
        if( logIndent.length() > 0 ) {
            getIndent().deleteCharAt( logIndent.length()-1 ) ;
        }
	}
	
    /**
     * Utility method used for indenting the structured trace log.
     */
    public static StringBuffer getIndent() {
	    if( logIndent == null ) {
	       logIndent = new StringBuffer() ;	
	    }
	    return logIndent ;	
	}
    
    @SuppressWarnings("unused")
	private static void resetIndent() {
        if( logIndent != null ) { 
            if( logIndent.length() > 0 ) {
               logIndent.delete( 0, logIndent.length() )  ;
            }
        }   
    }
	
	public String getProjectId() {
		return projectId;
	}


	public File getSpreadsheetFile() {
		return spreadsheetFile;
	}


	public Workbook getWorkbook() {
		return workbook;
	}


	public Sheet getSheetOne() {
		return dataSheet;
	}


	public Row getColumnNames() {
		return columnNames;
	}


	public Row getToolTips() {
		return toolTips;
	}


	public Row getOntologyCodes() {
		return ontologyCodes;
	}
	
	public Row getDataRow( int rowOffset ) {
		return dataSheet.getRow( rowOffset ) ;
	}


	public ArrayList<ObservationFact> getObservatonFacts() {
		return observatonFacts;
	}

	
}
