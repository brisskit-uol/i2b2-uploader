package org.brisskit.i2b2;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

public class ProjectUtils {
	
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
	// We are accepting dates in spreadsheet cells only in the following format (to begin with!)...
	private SimpleDateFormat cellDateFormat = new SimpleDateFormat( "yyyy-MM-dd" ) ;
	
	
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
	
	
	public  boolean isNull( String value ) {
		if( value.equalsIgnoreCase( "null" ) ) {
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
		try {
			cellDateFormat.parse( value ) ;
		    return true ;
		} catch( ParseException pex ) {
		    return false ;
		}
	}
	
	public Date parseDate( String value ) throws ParseException {
		return cellDateFormat.parse( value ) ;
	}
	
	public String formatDate( Date date ) {
		return dateFormat.format( date ) ;
	}
	
}