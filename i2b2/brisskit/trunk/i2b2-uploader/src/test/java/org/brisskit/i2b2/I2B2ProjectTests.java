package org.brisskit.i2b2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.log4j.* ;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import junit.framework.TestCase;

public class I2B2ProjectTests extends TestCase {
	
	private static Logger logger = Logger.getLogger( I2B2ProjectTests.class ) ;

	public I2B2ProjectTests(String name) {
		super(name);
	}

	protected void setUp() throws Exception { 
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	public void test01_CreateNewProject_WithEmptyRow() { 
		enterTrace( "==>>test01_CreateNewProject_WithEmptyRow()" ) ;
		File spreadsheetFile = 
				new File( getClass().getClassLoader().getResource( "spreadsheets/test-01-with-empty-row.xls").getFile() ) ;		
		String projectId = "test01_with_empty_row" ;
		try {
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;
			}	
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>test01_CreateNewProject_WithEmptyRow()" ) ;
		}
		
	}
	
	public void test02_SpreadsheetBeyondMaxRows() { 
		enterTrace( "==>>test02_SpreadsheetBeyondMaxRows()" ) ;
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/GP_CUT1_more_than_maxrows.xlsx").getFile());		
		String projectId = "maxrowsexceeded" ;
		try {
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;
			}	
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
			fail( "Created project with greater than max rows." ) ;
		}
		catch( UploaderException cex ) {			
			logger.debug( cex.getLocalizedMessage() ) ;			
		}
		finally {
			exitTrace( "==>>test02_SpreadsheetBeyondMaxRows()" ) ;
		}	
	}

	
	public void test03_CreateNewLaHeartProject() { 
		enterTrace( "==>>test03_CreateNewLaHeartProject()" ) ;
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/EG1-laheart.xlsx").getFile());		
		String projectId = "laheart" ;
		try {
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;
			}	
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>test03_CreateNewLaHeartProject()" ) ;
		}		
	}
	
	
	public void test04_CreateNewTest01Project() { 
		enterTrace( "==>>test04_CreateNewTest01Project()" ) ;
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/test-01.xls").getFile());		
		String projectId = "test01" ;
		try {
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;
			}	
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>test04_CreateNewTest01Project()" ) ;
		}		
	}
	
	
	public void test05_CreateNewProjectWithStartDateColumn() { 
		enterTrace( "==>>test05_CreateNewProjectWithStartDateColumn()" ) ;
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/test-03-startdatecol.xls").getFile() ) ;	
		String projectId = "startdatecol" ;
		try {
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;
			}	
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>test05_CreateNewProjectWithStartDateColumn()" ) ;
		}
		
	}

	
	public void test06_SupplementingExistingProject() { 
		enterTrace( "==>>test06_SupplementingExistingProject()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource("spreadsheets/test-01.xls").getFile());
		File spreadsheetFile2 = new File(getClass().getClassLoader().getResource("spreadsheets/test-02.xls").getFile());
		String projectId = "testnn" ;
		try {
			//
			// Delete project if it already exists...
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;				
			}
			//
			// Create new project with all it db artifacts
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			//
			// Get a new instance of the project...
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// And attempt to process subsequent spreadsheet...
			project.processSpreadsheet( spreadsheetFile2 ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>test06_SupplementingExistingProject()" ) ;
		}
		
	}
	
	public void test07_SupplementingExistingProject_AdditionalData() { 
		enterTrace( "==>>test07_SupplementingExistingProject_AdditionalData()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource("spreadsheets/test-01.xls").getFile());
		File spreadsheetFile2 = new File(getClass().getClassLoader().getResource("spreadsheets/test-01-samepeople.xls").getFile());
		String projectId = "testaddmoredata" ;
		try {
			//
			// Delete project if it already exists...
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;				
			}
			//
			// Create new project with all it db artifacts
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			//
			// Get a new instance of the project...
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// And attempt to process subsequent spreadsheet...
			project.processSpreadsheet( spreadsheetFile2 ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>test07_SupplementingExistingProject_AdditionalData()" ) ;
		}
	}
	
	
	
	public void test08_SupplementingExistingProject_AddionalMetadata() { 
		enterTrace( "==>>test08_SupplementingExistingProject_AddionalMetadata()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource("spreadsheets/test-01.xls").getFile());
		File spreadsheetFile2 = new File(getClass().getClassLoader().getResource("spreadsheets/test-02-additionalmetadata.xls").getFile());		
		String projectId = "testaddnewmeta" ;
		try {
			//
			// Delete project if it already exists...
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;				
			}
			//
			// Create new project with all it db artifacts
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			//
			// Get a new instance of the project...
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// And attempt to process subsequent spreadsheet...
			project.processSpreadsheet( spreadsheetFile2 ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>test08_SupplementingExistingProject_AddionalMetadata()" ) ;
		}
		
	}
		
		
		public void test09_SupplementingExistingProject_SameInstance() { 
			enterTrace( "==>>test09_SupplementingExistingProject_SameInstance()" ) ;
			File spreadsheetFile1 = new File(getClass().getClassLoader().getResource("spreadsheets/test-01.xls").getFile());
			File spreadsheetFile2 = new File(getClass().getClassLoader().getResource("spreadsheets/test-02.xls").getFile());	
			String projectId = "testnnsi" ;
			try {
				//
				// Delete project if it already exists...
				if( I2B2Project.Factory.projectExists( projectId ) ) {
					I2B2Project.Factory.delete( projectId ) ;				
				}
				//
				// Create new project with all it db artifacts
				I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
				//
				// Process the first spreadsheet...
				project.processSpreadsheet( spreadsheetFile1 ) ;
				//
				// And attempt to process subsequent spreadsheet...
				project.processSpreadsheet( spreadsheetFile2 ) ;
			}
			catch( UploaderException cex ) {			
				cex.printStackTrace( System.out ) ;
				fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
			}
			finally {
				exitTrace( "==>>test09_SupplementingExistingProject_SameInstance()" ) ;
			}
		
	}
	
	
	public void test10_DeletionOfProject() { 
		enterTrace( "==>>test10_DeletionOfProject()" ) ;
		String projectId = "test02dele" ;
		try {
			//
			// First - if need be - create the project and deploy to JBoss...			
			if( !I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.newInstance( projectId ) ;
			}
			//
			// Now delete it...
			I2B2Project.Factory.delete( projectId ) ;
			
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "CreationException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>test10_DeletionOfProject()" ) ;
		}
	}

	
	public void test11_DeletionOfNonExistentProject() {
		enterTrace( "==>>test11_DeletionOfNonExistentProject()" ) ;
		try {
			I2B2Project.Factory.delete( "projectX" ) ;
			fail( "Should not be able to delete a non-existent project: projectX" ) ;
		}
		catch( UploaderException cex ) {			
			
		}
		finally {
			exitTrace( "==>>test11_DeletionOfNonExistentProject()" ) ;
		}
	}

	public void test12_ReadSpreadsheet() {
		enterTrace( "==>>test12_ReadSpreadsheet()" ) ;
//		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/test-02.xls").getFile());
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/EG1-laheart.xlsx").getFile());		
		try {
			I2B2Project project = new I2B2Project() ;
			project.setSpreadsheetFile( spreadsheetFile ) ;
			project.readSpreadsheet() ;
			
			Row columnNames = project.getColumnNames() ;
		    Row toolTips = project.getToolTips() ;
			Row ontologyCodes = project.getOntologyCodes() ;
			
			assertNotNull( "columnNames should not be null", columnNames ) ;
			assertNotNull( "toolTips should not be null", toolTips ) ;
			assertNotNull( "ontologyCodes should not be null", ontologyCodes ) ;
			
			logger.debug( "Column names: " ) ;
			Iterator<Cell> it = columnNames.cellIterator() ;
			while( it.hasNext() ) {
				Cell cell = it.next() ;
				logger.debug( cell.getStringCellValue() ) ;
			}
			
			logger.debug( "Tool tips: " ) ;
			it = toolTips.cellIterator() ;
			while( it.hasNext() ) {
				Cell cell = it.next() ;
				logger.debug( cell.getStringCellValue() ) ;
			}
			
			logger.debug( "Codes: " ) ;
			it = ontologyCodes.cellIterator() ;
			while( it.hasNext() ) {
				Cell cell = it.next() ;
				logger.debug( cell.getStringCellValue() ) ;
			}
			
			logger.debug( "========= Values: " ) ;
			DataFormatter df = new DataFormatter() ;
			Iterator<Row> rowIt = project.getSheetOne().rowIterator() ;
			rowIt.next() ; // tab past column names
			rowIt.next() ; // tab past tool tips
			rowIt.next() ; // tab past codes
			while( rowIt.hasNext() ) {
				Row row = rowIt.next() ;
				logger.debug( ">>> values for row number: " + row.getRowNum() ) ;
				it = row.cellIterator() ;
				while( it.hasNext() ) {
					Cell cell = it.next() ;
					logger.debug( df.formatCellValue( cell ).trim() ) ;		
				}
			}
					
		}
		catch( UploaderException cex ) {
			fail( "CreationException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>test12_ReadSpreadsheet()" ) ;
		}
						
	}
	
	
	public void test13_SpreadsheetWithLotsEmptyRows() { 
		enterTrace( "==>>test13_SpreadsheetWithLotsEmptyRows()" ) ;
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/GP_CUT1.xlsx").getFile());		
		String projectId = "gpcut1" ;
		try {
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;
			}	
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
		}
		catch( UploaderException cex ) {
			logger.error( "Failed to process spreadsheet with lots of empty rows.", cex ) ;
			fail( "Failed to process spreadsheet with lots of empty rows." ) ;
						
		}
		finally {
			exitTrace( "==>>test13_SpreadsheetWithLotsEmptyRows()" ) ;
		}	
	}

	
	public void test14_SupplementingExistingProject_MalcsProblem() { 
		enterTrace( "==>>_test14_SupplementingExistingProject_MalcsProblem()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource("spreadsheets/Pharma1-shortened.xls").getFile());
		File spreadsheetFile2 = new File(getClass().getClassLoader().getResource("spreadsheets/pharma2-shortened.xlsx").getFile());
		String projectId = "pharma" ;
		try {
			//
			// Delete project if it already exists...
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;				
			}
			//
			// Create new project with all it db artifacts
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			//
			// Get a new instance of the project...
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// And attempt to process subsequent spreadsheet...
			project.processSpreadsheet( spreadsheetFile2 ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>_test14_SupplementingExistingProject_MalcsProblem()" ) ;
		}
		
	}
	
	
	public void test15_TranslateSpecialCharacters() { 
		enterTrace( "==>>TranslateSpecialCharacters()" ) ;

		String awkwardString = 
				"The & opponents ' of * the @ Copenhagen ` interpretation \\ are ^ " +
				"still } in ] a ) small : minority, and $ may = well ! remain > so. " +
				"They < do - not { agree [ among ( themselves. % But | quite + a # " +
				"lot \" of ; disagreement /is ~ also _ discernible within the Copenhagen orthodoxy." ;
		String expectedString =
				"The and opponents apostrophe of asterisk the at Copenhagen back quote " +
				"interpretation back slash are carat still close brace in close bracket " +
				"a close parenthesis small colon minority comma and dollar may equals well " +
				"exclamation mark remain greater than so. They less than do hyphen not open " +
				"brace agree open bracket among open parenthesis themselves. percent " +
				"But pipe quite plus a hash lot quote of semicolon disagreement forward " +
				"slash is tilde also underscore discernible within the Copenhagen orthodoxy." ;
		String translatedString = null ;
		try {
			translatedString = OntologyBranch.formEnumeratedValue( awkwardString ) ;
			if( !translatedString.equals( expectedString ) ) {
				logger.debug( "translatedString: " + translatedString ) ;
				fail( "Could not translate special characters. Before and after strings did not match." ) ;
			}
		}
		catch( Exception cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "Could not translate special characters: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>TranslateSpecialCharacters()" ) ;
		}
		
	}
	
	
	public void test16_CreateBiomed2_duffdatecolumn() { 
		enterTrace( "==>>test16_CreateBiomed2_duffdatecolumn()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource( "spreadsheets/Biomed2.xlsx").getFile() ) ;
		String projectId = "biomed2" ;
		try {
			//
			// Delete project if it already exists...
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;				
			}
			//
			// Create new project with all it db artifacts
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;		
			fail( "Should not be able to process a spreadsheet with a duff numeric/date column." ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
		}
		finally {
			exitTrace( "==>>test16_CreateBiomed2_duffdatecolumn()" ) ;
		}
		
	}
	
	
	public void test17_Create_test01v01jeff() { 
		enterTrace( "==>>test17_Create_test01v01jeff()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource( "spreadsheets/test-01.xls").getFile() ) ;
		String projectId = "test01v01jeff" ;
		try {
			//
			// Delete project if it already exists...
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;				
			}
			//
			// Create new project with all it db artifacts
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>test17_Create_test01v01jeff()" ) ;
		}
		
	}
	
	
	public void test18_Create_test01obsdatecol() { 
		enterTrace( "==>>_test18_Create_test01obsdatecol()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource( "spreadsheets/test-01-obsdatecol.xls").getFile() ) ;
		String projectId = "test01obsdatecol" ;
		try {
			//
			// Delete project if it already exists...
			if( I2B2Project.Factory.projectExists( projectId ) ) {
				I2B2Project.Factory.delete( projectId ) ;				
			}
			//
			// Create new project with all it db artifacts
			I2B2Project project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>_test18_Create_test01obsdatecol()" ) ;
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
	
}
