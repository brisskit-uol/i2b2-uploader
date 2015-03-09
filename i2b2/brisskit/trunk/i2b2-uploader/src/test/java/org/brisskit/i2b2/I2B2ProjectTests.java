package org.brisskit.i2b2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
	
	
	public void _test00_MajorCleanup() { 
		enterTrace( "==>>test00_MajorCleanup()" ) ;
		String[] projectIds = {
			"threadtest1",
			"threadtest2",	
			"threadtest3",	
			"threadtest4",	
			"threadtest5",	
			"threadtest6",	
			"threadtest7",	
			"threadtest8",	
			"threadtest9",	
			"threadtest10",	
			"testaddmoredata",	
			"testnn",	
			"test01_with_empty_row",	
			"lahearts",	
			"testthreading",	
			"project3",	
			"project1",	
			"test01v01jeff",	
			"maxrowsexceeded",	
			"startdatecol",
			"project5",
			"project2",
			"project6",
			"project7",
			"project8",
			"project0",
			"project1",
			"project9",
			"project10",
			"test01",
			"biomed2",
			"test01portalversion",
			"project4",
			"testaddnewmeta",
			"gpcut1",
			"laheart",
			"test03_startdatecols",
			"testnnsi",
			"test01obsdatecol",
			"pharma",
			"test21manydfs"
		} ;
		try {
			for( int i=0; i<projectIds.length; i++ ) {
				I2B2Project.Factory.deleteIfProjectExists( projectIds[i] ) ;
			}
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>test00_MajorCleanup()" ) ;
		}
		
	}
	
	public void _test01_CreateNewProject_WithEmptyRow() { 
		enterTrace( "==>>test01_CreateNewProject_WithEmptyRow()" ) ;
		File spreadsheetFile = 
				new File( getClass().getClassLoader().getResource( "spreadsheets/test-01-with-empty-row.xls").getFile() ) ;		
		String projectId = "test01_with_empty_row" ;
		I2B2Project project = null ;
		try {
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
			project = I2B2Project.Factory.newInstance( projectId ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
			project.dispose() ;
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }			
			exitTrace( "==>>test01_CreateNewProject_WithEmptyRow()" ) ;
		}
		
	}
	
	public void _test02_SpreadsheetBeyondMaxRows() { 
		enterTrace( "==>>test02_SpreadsheetBeyondMaxRows()" ) ;
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/GP_CUT1_more_than_maxrows.xlsx").getFile());		
		String projectId = "maxrowsexceeded" ;
		I2B2Project project = null ;
		try {
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;	
			project = I2B2Project.Factory.newInstance( projectId ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
			project.dispose() ;
			project = null ;
			fail( "Created project with greater than max rows." ) ;
		}
		catch( UploaderException cex ) {			
			logger.debug( cex.getLocalizedMessage() ) ;			
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test02_SpreadsheetBeyondMaxRows()" ) ;
		}	
	}

	
	public void _test03_CreateNewLaHeartProject() { 
		enterTrace( "==>>test03_CreateNewLaHeartProject()" ) ;
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/EG1-laheart.xlsx").getFile());		
		String projectId = "laheart" ;
		I2B2Project project = null ;
		try {
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;		
			project = I2B2Project.Factory.newInstance( projectId ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
			project.dispose() ;
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test03_CreateNewLaHeartProject()" ) ;
		}		
	}
	
	
	public void _test04_CreateNewTest01Project() { 
		enterTrace( "==>>test04_CreateNewTest01Project()" ) ;
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/test-01.xls").getFile());		
		String projectId = "test01" ;
		I2B2Project project = null ;
		try {
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;	
			project = I2B2Project.Factory.newInstance( projectId ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
			project.dispose() ;
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test04_CreateNewTest01Project()" ) ;
		}		
	}
	
	
	public void _test05_CreateNewProjectWithStartDateColumn() { 
		enterTrace( "==>>test05_CreateNewProjectWithStartDateColumn()" ) ;
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/test-03-startdatecol.xls").getFile() ) ;	
		String projectId = "startdatecol" ;
		I2B2Project project = null ;
		try {
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;	
			project = I2B2Project.Factory.newInstance( projectId ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
			project.dispose() ;
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test05_CreateNewProjectWithStartDateColumn()" ) ;
		}
		
	}

	
	public void _test06_SupplementingExistingProject() { 
		enterTrace( "==>>test06_SupplementingExistingProject()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource("spreadsheets/test-01.xls").getFile());
		File spreadsheetFile2 = new File(getClass().getClassLoader().getResource("spreadsheets/test-02.xls").getFile());
		String projectId = "testnn" ;
		I2B2Project project = null ;
		try {
			//
			// Delete project if it already exists...
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
			//
			// Create new project with all it db artifacts
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			project.dispose();
			//
			// Get a new instance of the project...
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// And attempt to process subsequent spreadsheet...
			project.processSpreadsheet( spreadsheetFile2 ) ;
			project.dispose() ;
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test06_SupplementingExistingProject()" ) ;
		}
		
	}
	
	public void _test07_SupplementingExistingProject_AdditionalData() { 
		enterTrace( "==>>test07_SupplementingExistingProject_AdditionalData()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource("spreadsheets/test-01.xls").getFile());
		File spreadsheetFile2 = new File(getClass().getClassLoader().getResource("spreadsheets/test-01-samepeople.xls").getFile());
		String projectId = "testaddmoredata" ;
		I2B2Project project = null ;
		try {
			//
			// Delete project if it already exists...
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
			//
			// Create new project with all it db artifacts
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			project.dispose();
			//
			// Get a new instance of the project...
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// And attempt to process subsequent spreadsheet...
			project.processSpreadsheet( spreadsheetFile2 ) ;
			project.dispose();
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test07_SupplementingExistingProject_AdditionalData()" ) ;
		}
	}
	
	
	
	public void _test08_SupplementingExistingProject_AdditionalMetadata() { 
		enterTrace( "==>>test08_SupplementingExistingProject_AdditionalMetadata()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource("spreadsheets/test-01.xls").getFile());
		File spreadsheetFile2 = new File(getClass().getClassLoader().getResource("spreadsheets/test-02-additionalmetadata.xls").getFile());		
		String projectId = "testaddnewmeta" ;
		I2B2Project project = null ;
		try {
			//
			// Delete project if it already exists...
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
			//
			// Create new project with all it db artifacts
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			project.dispose();
			//
			// Get a new instance of the project...
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// And attempt to process subsequent spreadsheet...
			project.processSpreadsheet( spreadsheetFile2 ) ;
			project.dispose() ;
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test08_SupplementingExistingProject_AdditionalMetadata()" ) ;
		}
		
	}
		
		
		public void _test09_SupplementingExistingProject_SameInstance() { 
			enterTrace( "==>>test09_SupplementingExistingProject_SameInstance()" ) ;
			File spreadsheetFile1 = new File(getClass().getClassLoader().getResource("spreadsheets/test-01.xls").getFile());
			File spreadsheetFile2 = new File(getClass().getClassLoader().getResource("spreadsheets/test-02.xls").getFile());	
			String projectId = "testnnsi" ;
			I2B2Project project = null ;
			try {
				//
				// Delete project if it already exists...
				I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
				//
				// Create new project with all it db artifacts
				project = I2B2Project.Factory.newInstance( projectId ) ;
				//
				// Process the first spreadsheet...
				project.processSpreadsheet( spreadsheetFile1 ) ;
				//
				// And attempt to process subsequent spreadsheet...
				project.processSpreadsheet( spreadsheetFile2 ) ;
				project.dispose() ;
				project = null ;
			}
			catch( UploaderException cex ) {			
				cex.printStackTrace( System.out ) ;
				fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
			}
			finally {
				if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
				exitTrace( "==>>test09_SupplementingExistingProject_SameInstance()" ) ;
			}
		
	}
	
	
	public void _test10_DeletionOfProject() { 
		enterTrace( "==>>test10_DeletionOfProject()" ) ;
		String projectId = "test02dele" ;
		I2B2Project project = null ;
		try {
			//
			// First - if need be - create the project and deploy to JBoss...			
			if( !I2B2Project.Factory.projectExists( projectId ) ) {
				project = I2B2Project.Factory.newInstance( projectId ) ;
			}
			//
			// Now delete it...
			I2B2Project.Factory.delete( project ) ;
			project.dispose() ;
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "CreationException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test10_DeletionOfProject()" ) ;
		}
	}

	
	public void _test11_DeletionOfNonExistentProject() {
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

	public void _test12_ReadSpreadsheet() {
		enterTrace( "==>>test12_ReadSpreadsheet()" ) ;
//		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/test-02.xls").getFile());
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/EG1-laheart.xlsx").getFile());		
		I2B2Project project = null ;
		try {
			project = new I2B2Project() ;
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
			logger.error( "Failed to read spreadsheet", cex ) ;
			fail( "CreationException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test12_ReadSpreadsheet()" ) ;
		}
						
	}
	
	
	public void _test13_SpreadsheetWithLotsEmptyRows() { 
		enterTrace( "==>>test13_SpreadsheetWithLotsEmptyRows()" ) ;
		logger.debug( "==>>test13 GP_CUT1" ) ;
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/GP_CUT1.xlsx").getFile());		
		String projectId = "gpcut1" ;
		I2B2Project project = null ;
		try {
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
			project = I2B2Project.Factory.newInstance( projectId ) ;
			logger.debug( "About to process spreadsheet" ) ;
			project.processSpreadsheet( spreadsheetFile ) ;
			logger.debug( "Finished processing spreadsheet" ) ;
			project.dispose() ;
			project = null ;
		}
		catch( UploaderException cex ) {
			logger.error( "Failed to process spreadsheet with lots of empty rows.", cex ) ;
			fail( "Failed to process spreadsheet with lots of empty rows." ) ;
						
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			logger.debug( "<<==test13 GP_CUT1" ) ;
			exitTrace( "==>>test13_SpreadsheetWithLotsEmptyRows()" ) ;
		}	
	}

	
	public void _test14_SupplementingExistingProject_MalcsProblem() { 
		enterTrace( "==>>_test14_SupplementingExistingProject_MalcsProblem()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource("spreadsheets/Pharma1-shortened.xls").getFile());
		File spreadsheetFile2 = new File(getClass().getClassLoader().getResource("spreadsheets/pharma2-shortened.xlsx").getFile());
		String projectId = "pharma" ;
		I2B2Project project = null ;
		try {
			//
			// Delete project if it already exists...
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
			//
			// Create new project with all it db artifacts
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			project.dispose();
			//
			// Get a new instance of the project...
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// And attempt to process subsequent spreadsheet...
			project.processSpreadsheet( spreadsheetFile2 ) ;
			project.dispose();
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>_test14_SupplementingExistingProject_MalcsProblem()" ) ;
		}
		
	}
	
	
	public void _test15_TranslateSpecialCharacters() { 
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
				fail( "Could not translate special characters. After strings did not match." ) ;
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
	
	
	public void _test16_CreateBiomed2_duffdatecolumn() { 
		enterTrace( "==>>test16_CreateBiomed2_duffdatecolumn()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource( "spreadsheets/Biomed2.xlsx").getFile() ) ;
		String projectId = "biomed2" ;
		I2B2Project project = null ;
		try {
			//
			// Delete project if it already exists...
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
			//
			// Create new project with all it db artifacts
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			fail( "Should not be able to process a spreadsheet with a duff numeric/date column." ) ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test16_CreateBiomed2_duffdatecolumn()" ) ;
		}
		
	}
	
	
	public void _test17_Create_test01v01jeff() { 
		enterTrace( "==>>test17_Create_test01v01jeff()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource( "spreadsheets/test-01.xls").getFile() ) ;
		String projectId = "test01v01jeff" ;
		I2B2Project project = null ;
		try {
			//
			// Delete project if it already exists...
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
			//
			// Create new project with all it db artifacts
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			project.dispose() ;
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test17_Create_test01v01jeff()" ) ;
		}
		
	}
	
	
	public void _test18_Create_test01obsdatecol() { 
		enterTrace( "==>>_test18_Create_test01obsdatecol()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource( "spreadsheets/test-01-obsdatecol.xls").getFile() ) ;
		String projectId = "test01obsdatecol" ;
		I2B2Project project = null ;
		try {
			//
			// Delete project if it already exists...
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
			//
			// Create new project with all it db artifacts
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			project.dispose();
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>_test18_Create_test01obsdatecol()" ) ;
		}
		
	}

	
	public void _test19_Threading() { 
		logger.debug( "==>>test19_Threading()" ) ;
		ClassLoader cl = getClass().getClassLoader() ;
		File[] spreadsheets = {
				new File( cl.getResource( "spreadsheets/EG1-laheart.xlsx").getFile() ),
				new File( cl.getResource( "spreadsheets/GP_CUT1.xlsx" ).getFile() ),
				new File( cl.getResource( "spreadsheets/test-01-obsdatecol.xls" ).getFile() ),
				new File( cl.getResource( "spreadsheets/test-01-with-empty-row.xls" ).getFile() ),
				new File( cl.getResource( "spreadsheets/Pharma1-shortened.xls" ).getFile() ),
				new File( cl.getResource( "spreadsheets/pharma2-shortened.xlsx" ).getFile() ),
				new File( cl.getResource( "spreadsheets/test-03-startdatecol.xls" ).getFile() ),
				new File( cl.getResource( "spreadsheets/EG1-laheart.xlsx" ).getFile() ),
				new File( cl.getResource( "spreadsheets/GP_CUT1_more_than_maxrows.xlsx" ).getFile() ),
				new File( cl.getResource( "spreadsheets/GP_CUT1.xlsx" ).getFile() )
		} ;
		Set<ConcurrencyTest> cts = new HashSet<ConcurrencyTest>() ;
		try {
			
			for( int i=0; i<spreadsheets.length; i++ ) {			
				ConcurrencyTest ct = new ConcurrencyTest( "project" + (i+1), spreadsheets[i] ) ;
				cts.add( ct ) ;
				( new Thread( ct ) ).start() ;
			}
			
			while( cts.size() > 0 ) {
				Thread.sleep( 10000 ) ;
				Iterator<ConcurrencyTest> it = cts.iterator() ;
				while( it.hasNext() ) {
					if( it.next().isFinished() == true ) {
						it.remove() ;
					}
				}				
			}
			
		}
		catch( Exception ex ) {			
			logger.error( "Exception thrown", ex ) ;
			fail( "Exception thrown: " + ex.getLocalizedMessage() ) ;
		}
		finally {
			logger.debug( "<<==test19_Threading()" ) ;
		}
		
	}
	
	
	public void _test20_Dates() { 
		enterTrace( "==>>_test20_Dates()" ) ;

		Date parsedDate = null ;
		String dateString1 = "2015-03-02T12:59:56" ;
		String dateString2 = "2015/03/02T12:59:56" ;
		String dateString3 = "02/03/2015" ;
		ProjectUtils utils = new ProjectUtils() ;		
		try {
			
			parsedDate = utils.parseDateIfDate( dateString1 ) ;
			if( parsedDate == null ) {				
				logger.debug( "Unrecognized date: " + dateString2 ) ;
				fail( "Could not process date: " + dateString1 ) ;
			}
			else {
				logger.debug( "Date was successfully parsed: " + parsedDate ) ;
			}
			parsedDate = utils.parseDateIfDate( dateString2 ) ;
			if( parsedDate == null ) {				
				logger.debug( "Unrecognized date: " + dateString2 ) ;
				fail( "Could not process date: " + dateString2 ) ;
			}
			else {
				logger.debug( "Date was successfully parsed: " + parsedDate ) ;
			}
			parsedDate = utils.parseDateIfDate( dateString3 ) ;
			if( parsedDate == null ) {				
				logger.debug( "Unrecognized date: " + dateString3 ) ;
				fail( "Could not process date: " + dateString3 ) ;
			}
			else {
				logger.debug( "Date was successfully parsed: " + parsedDate ) ;
			}
		}
		catch( Exception cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "Could not process date: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			exitTrace( "==>>_test20_Dates()" ) ;
		}
		
	}
	
	
	public void test21_ManyDateFormats() { 
		enterTrace( "==>>test21_ManyDateFormats()" ) ;
		File spreadsheetFile1 = new File(getClass().getClassLoader().getResource( "spreadsheets/test-01-manydateformats.xls").getFile() ) ;
		String projectId = "test21manydfs" ;
		I2B2Project project = null ;
		try {
			//
			// Delete project if it already exists...
			I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
			//
			// Create new project with all it db artifacts
			project = I2B2Project.Factory.newInstance( projectId ) ;
			//
			// Process the first spreadsheet...
			project.processSpreadsheet( spreadsheetFile1 ) ;
			project.dispose();
			project = null ;
		}
		catch( UploaderException cex ) {			
			cex.printStackTrace( System.out ) ;
			fail( "UploaderException thrown: " + cex.getLocalizedMessage() ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
			exitTrace( "==>>test21_ManyDateFormats()" ) ;
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
    
    
    public static class ConcurrencyTest implements Runnable {

    	private String projectId ;
    	private File spreadsheetFile ;
    	private boolean finished = false ;
    	
    	public ConcurrencyTest( String projectId, File spreadsheetFile ) {
    		this.projectId = projectId ;
    		this.spreadsheetFile = spreadsheetFile ;
    	}
    	
    	public boolean isFinished() {
    		synchronized( this ) {
    			return this.finished ;
    		}
    	}
    	
    	private void setFinished() {
    		synchronized( this ) {
    			this.finished = true ;
    		}
    	}
    	
		@Override
		public void run() {
			logger.debug( "==>>ConcurrencyTest.run() " + projectId ) ;
			I2B2Project project = null ;
			try {
				//
				// Delete project if it already exists...
				I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
				//
				// Create new project with all its db artifacts
				project = I2B2Project.Factory.newInstance( projectId ) ;
				//
				// Process the spreadsheet...
				project.processSpreadsheet( spreadsheetFile ) ;
				project.dispose();
				project = null ;
			}
			catch( UploaderException cex ) {			
				logger.error( "Thread number " + projectId + " failed.", cex ) ;
			}
			finally {
				if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
				try {
					setFinished() ;
				}
				catch( Exception ex ) {
					logger.error( "setFinished() failed [" + projectId + "]", ex ) ;
				}
				logger.debug( "<<==ConcurrencyTest.run() " + projectId ) ;				
			}
			
		}
    	
    }
	
}
