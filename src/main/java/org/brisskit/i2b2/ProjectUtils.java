package org.brisskit.i2b2;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

public class ProjectUtils {
	
	private static Logger logger = Logger.getLogger( ProjectUtils.class ) ;
	
	//
	// 
	private CreateDBPG dbAccess ;
	//
	//
	private PreparedStatementHolder psHolder ;
	
	//
	// Postgres example of TIMESTAMP ’2004-10-19 10:23:54’
	private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) ;
	//
	// Postgres example of Decimal 18,5   123456789012345678.12345
	private DecimalFormat decimalFormat = new DecimalFormat( "##################.00000" ) ;
	//
	// The simplest object to format values from a cell into a string...
	private DataFormatter stringFormat = new DataFormatter() ;
		
	private Pattern numericPattern = Pattern.compile( "^-?\\d+(\\.\\d+)?$" ) ;  //match a number with optional '-' and decimal
	private Pattern integerPattern = Pattern.compile( "^\\d+$" ) ;	
	
	//
	// We are accepting dates in spreadsheet cells only in the following 
	// long or short formats (to begin with!)...
	private Object[][] patternsAndDateFormats = {
			{ Pattern.compile( "^(\\d{4})\\-(\\d{2})\\-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})$" ) , new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ) },
			{ Pattern.compile( "^(\\d{4})\\/(\\d{2})\\/(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})$" ) , new SimpleDateFormat( "yyyy/MM/dd'T'HH:mm:ss" ) },
			{ Pattern.compile( "^(\\d{4})\\-(\\d{2})\\-(\\d{2}).(\\d{2}):(\\d{2}):(\\d{2})$" ) , new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) },
			{ Pattern.compile( "^(\\d{4})\\/(\\d{2})\\/(\\d{2}).(\\d{2}):(\\d{2}):(\\d{2})$" ) , new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" ) },
			{ Pattern.compile( "^(\\d{4})\\-(\\d{2})\\-(\\d{2})$" ) , new SimpleDateFormat( "yyyy-MM-dd" ) },
			{ Pattern.compile( "^(\\d{4})\\/(\\d{2})\\/(\\d{2})$" ) , new SimpleDateFormat( "yyyy/MM/dd" ) },
			{ Pattern.compile( "^(\\d{2})\\-(\\d{2})\\-(\\d{4})T(\\d{2}):(\\d{2}):(\\d{2})$" ) , new SimpleDateFormat( "dd-MM-yyyy'T'HH:mm:ss" ) },
			{ Pattern.compile( "^(\\d{2})\\/(\\d{2})\\/(\\d{4})T(\\d{2}):(\\d{2}):(\\d{2})$" ) , new SimpleDateFormat( "dd/MM/yyyy'T'HH:mm:ss" ) },
			{ Pattern.compile( "^(\\d{2})\\-(\\d{2})\\-(\\d{4}).(\\d{2}):(\\d{2}):(\\d{2})$" ) , new SimpleDateFormat( "dd-MM-yyyy HH:mm:ss" ) },
			{ Pattern.compile( "^(\\d{2})\\/(\\d{2})\\/(\\d{4}).(\\d{2}):(\\d{2}):(\\d{2})$" ) , new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" ) },
			{ Pattern.compile( "^(\\d{2})\\-(\\d{2})\\-(\\d{4})$" ) , new SimpleDateFormat( "dd-MM-yyyy" ) },
			{ Pattern.compile( "^(\\d{2})\\/(\\d{2})\\/(\\d{4})$" ) , new SimpleDateFormat( "dd/MM/yyyy" ) },
		} ;
	
	private FormulaEvaluator formulaeEvaluator = null ;
	
	public ProjectUtils() {
		for( int i=0; i<patternsAndDateFormats.length; i++ ) {
			( (SimpleDateFormat) patternsAndDateFormats[i][1] ).setLenient( false ) ;
		}		
	}


	public String _getValueAsString( Cell cell ) {
		String value = stringFormat.formatCellValue( cell ) ;
		if( value != null ) {
			value = value.trim() ;
		}
		return value ;
	}
	
	
	public String getValueAsString( Cell cell ) {
		if( cell == null ) {
			return "" ;
		}
		if( this.formulaeEvaluator == null ) {
			formulaeEvaluator = cell.getRow()
									.getSheet()
									.getWorkbook()
									.getCreationHelper()
									.createFormulaEvaluator() ;
		}
		try {
			formulaeEvaluator.evaluateInCell( cell ) ;
		}
		catch( Exception ex ) {
			StringBuilder b = new StringBuilder() ;
			b.append( "Formulae evaluator failed. " )
			 .append( "Cell: ["  ).append( cell.getColumnIndex() )
			 .append( "] Row: [" ).append( cell.getRowIndex() )
			 .append( "] Sheet: [" ).append( cell.getSheet().getSheetName() ).append( "]" ) ;
			logger.error( b.toString(), ex ) ;
			String value = stringFormat.formatCellValue( cell ) ;
			if( value != null ) {
				value = value.trim() ;
				return value ;
			}
			else {
				return "" ;
			}
		}
		switch ( cell.getCellType() ) {
        case Cell.CELL_TYPE_BOOLEAN:
        case Cell.CELL_TYPE_NUMERIC:
        case Cell.CELL_TYPE_STRING:
        	String value = stringFormat.formatCellValue( cell ) ;
			if( value != null ) {
				value = value.trim() ;
				return value ;
			}
        default:
            return "" ;
		}
	}
	
	
	public boolean isEmpty( String value ) {
		if( value == null ) {
			return true ;
		}
		value = value.trim() ;
		if( value.equalsIgnoreCase( "null" ) ) {
			return true ;
		}
		if( value.equalsIgnoreCase( "nul" ) ) {
			return true ;
		}
		if( value.length() == 0 ) {
			return true ;
		}
		return false ;
	}
	
	
	public boolean isNumeric( String value ) {
		return numericPattern.matcher( value ).matches() ;  //match a number with optional '-' and decimal
	}

	
	public boolean isInteger( String value ) {		
		return integerPattern.matcher( value ).matches() ;
	}
	
	
	public boolean isDate( String value ) {
		for( int i=0; i<patternsAndDateFormats.length; i++ ) {
			if( ((Pattern)patternsAndDateFormats[i][0]).matcher(value).matches() ) {
				return true ;
			}		
		}
		return false ;
	}
	
	
	public Date parseDate( String value ) throws ParseException {
		for( int i=0; i<patternsAndDateFormats.length; i++ ) {
			if( ((Pattern)patternsAndDateFormats[i][0]).matcher(value).matches() ) {
				return ((SimpleDateFormat)patternsAndDateFormats[i][1]).parse( value ) ;
			}			
		}
		throw new ParseException( "String is not a valid date: " + value, 0 ) ;
	}
	
	
	public Date parseDateIfDate( String value ) {
		for( int i=0; i<patternsAndDateFormats.length; i++ ) {
			if( ((Pattern)patternsAndDateFormats[i][0]).matcher(value).matches() ) {
				try {
					return ((SimpleDateFormat)patternsAndDateFormats[i][1]).parse( value ) ;
				}
				catch( ParseException pex ) {
					;
				}				
			}			
		}
		return null ;
	}

	
	public String formatDate( Date date ) {
		return dateFormat.format( date ) ;
	}

	public CreateDBPG getDbAccess() throws UploaderException {
		if( dbAccess == null ) {
			this.dbAccess = new CreateDBPG() ;
		}		
		return dbAccess;
	}

	public PreparedStatementHolder getPsHolder() throws UploaderException {
		if( psHolder == null ) {
			this.psHolder = new PreparedStatementHolder( getDbAccess().getSimpleConnectionPG() ) ;
		}
		return psHolder;
	}
	
}
