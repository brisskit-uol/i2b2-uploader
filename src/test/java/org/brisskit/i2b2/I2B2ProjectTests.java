package org.brisskit.i2b2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import junit.framework.TestCase;

public class I2B2ProjectTests extends TestCase {
	
	private static Log log = LogFactory.getLog( I2B2ProjectTests.class ) ;

	public I2B2ProjectTests(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testI2B2Project() {
		
		I2B2Project project = new I2B2Project( "project1"
											 , "kjshf"
				                             , new File( "somespreadsheet.xls" ) ) ;
		
		assert( project.getProjectId().equals( "project1" ) ) ;
		assert( project.getSpreadsheetFile().getName().equals( "somespreadsheet.xls" ) ) ;
		
	}

	public void testCreate() { 
		
//		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/test-02.xls").getFile());
//		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/EG1-laheart.xlsx").getFile());
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/GP_CUT1.xlsx").getFile());
		I2B2Project project = new I2B2Project( "gpcut"
				                             , "qwerty"
                                             , spreadsheetFile ) ;
		
		try {
			project.createDBArtifacts() ;
			project.processSpreadsheet() ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "CreationException thrown: " + cex.getLocalizedMessage() ) ;
		}
		
	}

	
//	public void testPopulateProject() {
//		fail("Not yet implemented");
//	}

	public void testReadSpreadsheet() {
		
//		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/test-02.xls").getFile());
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/EG1-laheart.xlsx").getFile());
		I2B2Project project = new I2B2Project( "infarction", "kjshf", spreadsheetFile ) ;
		
		try {
			project.readSpreadsheet() ;
			
			Row columnNames = project.getColumnNames() ;
		    Row toolTips = project.getToolTips() ;
			Row ontologyCodes = project.getOntologyCodes() ;
			
			assertNotNull( "columnNames should not be null", columnNames ) ;
			assertNotNull( "toolTips should not be null", toolTips ) ;
			assertNotNull( "ontologyCodes should not be null", ontologyCodes ) ;
			
			log.debug( "Column names: " ) ;
			Iterator<Cell> it = columnNames.cellIterator() ;
			while( it.hasNext() ) {
				Cell cell = it.next() ;
				log.debug( cell.getStringCellValue() ) ;
			}
			
			log.debug( "Tool tips: " ) ;
			it = toolTips.cellIterator() ;
			while( it.hasNext() ) {
				Cell cell = it.next() ;
				log.debug( cell.getStringCellValue() ) ;
			}
			
			log.debug( "Codes: " ) ;
			it = ontologyCodes.cellIterator() ;
			while( it.hasNext() ) {
				Cell cell = it.next() ;
				log.debug( cell.getStringCellValue() ) ;
			}
			
			log.debug( "========= Values: " ) ;
			DataFormatter df = new DataFormatter() ;
			Iterator<Row> rowIt = project.getSheetOne().rowIterator() ;
			rowIt.next() ; // tab past column names
			rowIt.next() ; // tab past tool tips
			rowIt.next() ; // tab past codes
			while( rowIt.hasNext() ) {
				Row row = rowIt.next() ;
				log.debug( ">>> values for row number: " + row.getRowNum() ) ;
				it = row.cellIterator() ;
				while( it.hasNext() ) {
					Cell cell = it.next() ;
					log.debug( df.formatCellValue( cell ).trim() ) ;		
				}
			}
					
		}
		catch( UploaderException cex ) {
			fail( "CreationException thrown: " + cex.getLocalizedMessage() ) ;
		}
						
	}
	
}
