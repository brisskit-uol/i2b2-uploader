package org.brisskit.i2b2;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

public class ProjectUtils {
	
	//
	// 
	private CreateDBPG dbAccess = new CreateDBPG() ;
	
	//
	// Postgres example of TIMESTAMP ’2004-10-19 10:23:54’
	private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) ;
	//
	// Postgres example of Decimal 18,5   123456789012345678.12345
	private DecimalFormat decimalFormat = new DecimalFormat( "##################.00000" ) ;
	//
	// The simplest object to format values from a cell into a string...
	private DataFormatter stringFormat = new DataFormatter() ;
	//
	// We are accepting dates in spreadsheet cells only in the following 
	// long or short formats (to begin with!)...
	private SimpleDateFormat yyyymmddThhmmss = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ) ;
	private SimpleDateFormat yyyymmddhhmmss = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) ;
	private SimpleDateFormat ddmmyyyyThhmmss = new SimpleDateFormat( "dd-MM-yyyy'T'HH:mm:ss" ) ;
	private SimpleDateFormat ddmmyyyyhhmmss = new SimpleDateFormat( "dd-MM-yyyy HH:mm:ss" ) ;
	private SimpleDateFormat yyyymmdd = new SimpleDateFormat( "yyyy-MM-dd" ) ;
	private SimpleDateFormat ddmmyyyy = new SimpleDateFormat( "dd-MM-yyyy" ) ;
	
//	private Pattern pattern_yyyymmddThhmmss1 = Pattern.compile( "^(\\d{4})\\-(\\d{2})\\-(\\d{2}.T.(\\d{2}):(\\d{2}):(\\d{2})$" ) ;
//	private Pattern pattern_yyyymmddhhmmss1 = Pattern.compile( "^(\\d{4})\\-(\\d{2})\\-(\\d{2}.(\\d{2}):(\\d{2}):(\\d{2})$" ) ;
//	private Pattern pattern_yyyymmddThhmmss2 = Pattern.compile( "^(\\d{4})\\/(\\d{2})\\/(\\d{2}.T.(\\d{2}):(\\d{2}):(\\d{2})$" ) ;
//	private Pattern pattern_yyyymmddhhmmss2 = Pattern.compile( "^(\\d{4})\\/(\\d{2})\\/(\\d{2}.(\\d{2}):(\\d{2}):(\\d{2})$" ) ;
	
//	^(0?[1-9]|[12][0-9]|3[01])[\/\-](0?[1-9]|1[012])[\/\-]\d{4}$	
	
	private SimpleDateFormat[] longCellDateFormats =
		{
			new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ) ,
			new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) ,
			new SimpleDateFormat( "dd-MM-yyyy'T'HH:mm:ss" ) ,
			new SimpleDateFormat( "dd-MM-yyyy HH:mm:ss" )	
		} ;
	private SimpleDateFormat[] shortCellDateFormats =
		{
			new SimpleDateFormat( "yyyy-MM-dd" ) ,
			new SimpleDateFormat( "dd-MM-yyyy" ) 	
		} ;
	
	
	
	private SimpleDateFormat cellDateFormat = new SimpleDateFormat( "yyyy-MM-dd" ) ;	
	private SimpleDateFormat cellDateTimeFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ) ;
	
	private FormulaEvaluator formulaeEvaluator = null ;
	
	
	public ProjectUtils() throws UploaderException {
	}
	
	
	public String enfoldNullableString( String value ) {	
		if( value == null ) {
			return "NULL" ;
		}
		else {
			return "'" + value + "'" ;
		}	
	}

	public String enfoldString( String value ) throws UploaderException {	
		if( value == null ) {
			throw new UploaderException( "Non-nullable String encountered:\n" + this.toString() ) ;
		}
		else {
			return "'" + value + "'" ;
		}	
	}


	public String enfoldNullableDecimal( Double value ) {
		if( value == null ) {
			return "NULL" ;
		}
		else {
			return decimalFormat.format( value ) ;
		}	
	}


	public String enfoldDecimal( Double value ) throws UploaderException {
		if( value == null ) {
			throw new UploaderException( "Non-nullable Long encountered:\n" + this.toString() ) ;
		}
		else {
			return decimalFormat.format( value ) ;
		}	
	}


	public String enfoldNullableInteger( Integer value ) {
		if( value == null ) {
			return "NULL" ;
		}
		else {
			return String.valueOf( value ) ;
		}	
	}

	public String enfoldInteger( Integer value ) throws UploaderException {
		if( value == null ) {
			throw new UploaderException( "Non-nullable Integer encountered:\n" + this.toString() ) ;
		}
		else {
			return String.valueOf( value ) ;
		}	
	}


	public String enfoldNullableDate( Date value ) {
		if( value == null ) {
			return "NULL" ;
		}
		else {
			return "'" + dateFormat.format( value ) + "'" ;
		}	
	}

	public String enfoldDate( Date value ) throws UploaderException {
		if( value == null ) {
			throw new UploaderException( "Non-nullable Date encountered:\n" + this.toString() ) ;
		}
		else {
			return "'" + dateFormat.format( value ) + "'" ;
		}	
	}
	
	public String getValueAsString( Cell cell ) {
		String value = stringFormat.formatCellValue( cell ) ;
		if( value != null ) {
			value = value.trim() ;
		}
		return value ;
	}
	
	
	public String _getValueAsString( Cell cell ) {
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
		formulaeEvaluator.evaluateInCell( cell ) ;
		switch ( cell.getCellType() ) {
        case Cell.CELL_TYPE_BOOLEAN:
        case Cell.CELL_TYPE_NUMERIC:
        case Cell.CELL_TYPE_STRING:
        	String value = stringFormat.formatCellValue( cell ) ;
			if( value != null ) {
				value = value.trim() ;
				return value ;
			}
			return "" ;
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
		return value.matches("^-?\\d+(\\.\\d+)?$");  //match a number with optional '-' and decimal
	}
	
	
	public boolean isInteger( String value ) {		
		return value.matches( "^\\d+$" ) ;
	}
	
	public boolean isDate( String value ) {
		if( value.matches( "....-..-..*:..:.." ) ) {
			try {
				this.yyyymmddThhmmss.parse( value ) ;
				return true ;
			}
			catch( ParseException pex1 ) {
				try {
					this.yyyymmddhhmmss.parse( value ) ;
					return true ;
				}
				catch( ParseException pex2 ) {
					return false ;
				}
			}
		}
		else if( value.matches( "..-..-....*:..:.." ) ) {
			try {
				this.ddmmyyyyThhmmss.parse( value ) ;
				return true ;
			}
			catch( ParseException pex1 ) {
				try {
					this.ddmmyyyyhhmmss.parse( value ) ;
					return true ;
				}
				catch( ParseException pex2 ) {
					return false ;
				}
			}
		}
		else if( value.matches( "....-..-.." ) ) {
			try {
				this.yyyymmdd.parse( value ) ;
				return true ;
			}
			catch( ParseException pex1 ) {
				return false ;
			}
		}
		else if( value.matches( "..-..-...." ) ) {
			try {
				this.ddmmyyyy.parse( value ) ;
				return true ;
			}
			catch( ParseException pex1 ) {
				return false ;
			}
		}
		return false ;
	}

	
	public Date parseDate( String value ) throws ParseException {
		if( value.matches( "....-..-..*:..:.." ) ) {
			try {
				return this.yyyymmddThhmmss.parse( value ) ;
			}
			catch( ParseException pex1 ) {
				return this.yyyymmddhhmmss.parse( value ) ;
			}
		}
		else if( value.matches( "..-..-....*:..:.." ) ) {
			try {
				return this.ddmmyyyyThhmmss.parse( value ) ;
			}
			catch( ParseException pex1 ) {
				return this.ddmmyyyyhhmmss.parse( value ) ;
			}
		}
		else if( value.matches( "....-..-.." ) ) {
			return this.yyyymmdd.parse( value ) ;
		}
		else if( value.matches( "..-..-...." ) ) {
			return this.ddmmyyyy.parse( value ) ;
		}
		throw new ParseException( "String is not a valide date: " + value, 0 ) ;
	}
	
	public String formatDate( Date date ) {
		return dateFormat.format( date ) ;
	}

	public CreateDBPG getDbAccess() {
		return dbAccess;
	}

	public void setDbAccess(CreateDBPG dbAccess) {
		this.dbAccess = dbAccess;
	}
	
}
