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
				                             , "My First Project"
				                             , "admin"
				                             , "qwerty"
				                             , new File( "somespreadsheet.xls" ) ) ;
		
		assert( project.getProjectId().equals( "project1" ) ) ;
		assert( project.getProjectName().equals( "My First Project" ) ) ;
		assert( project.getAdminUserId().equals( "admin" ) ) ;
		assert( project.getPassword().equals( "qwerty" ) ) ;
		assert( project.getSpreadsheetFile().getName().equals( "somespreadsheet.xls" ) ) ;
		
	}

	public void testCreate() {
		
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/test-02.xls").getFile());
		I2B2Project project = new I2B2Project( "project1"
                                             , "My First Project"
                                             , "admin"
                                             , "admin..."
                                             , spreadsheetFile ) ;
		
		try {
//			I2B2ProjectTests.reinit() ;
			
			project.create() ;
					
		}
		catch( NewProjectException cex ) {
			fail( "CreationException thrown: " + cex.getLocalizedMessage() ) ;
		}
		
	}

	
//	public void testCreateDBArtifacts() {
//		
//		I2B2Project project = new I2B2Project( "project1"
//                , "My First Project"
//                , "admin"
//                , "qwerty"
//                , new File( "somespreadsheet.xls" ) ) ;
//		
//		try {
//			project.createDBArtifacts() ;
//		}
//		catch( CreationException cex ) {
//			fail( "CreationException thrown: " + cex.getLocalizedMessage() ) ;
//		}
//		
//	}

//	public void testPopulateProject() {
//		fail("Not yet implemented");
//	}

	public void testReadSpreadsheet() {
		
		File spreadsheetFile = new File(getClass().getClassLoader().getResource("spreadsheets/test-02.xls").getFile());
		I2B2Project project = new I2B2Project( "project1"
                , "My First Project"
                , "admin"
                , "qwerty"
                , spreadsheetFile ) ;
		
		try {
			project.readSpreadsheet() ;
			
			Row columnNames = project.getColumnNames() ;
		    Row toolTips = project.getToolTips() ;
			Row ontologyCodes = project.getOntologyCodes() ;
			
			assertNotNull( "columnNames should not be null", columnNames ) ;
			assertNotNull( "toolTips should not be null", toolTips ) ;
			assertNotNull( "ontologyCodes should not be null", ontologyCodes ) ;
			
//			log.debug( "Column names: " ) ;
//			Iterator<Cell> it = columnNames.cellIterator() ;
//			while( it.hasNext() ) {
//				Cell cell = it.next() ;
//				log.debug( cell.getStringCellValue() ) ;
//			}
//			
//			log.debug( "Tool tips: " ) ;
//			it = toolTips.cellIterator() ;
//			while( it.hasNext() ) {
//				Cell cell = it.next() ;
//				log.debug( cell.getStringCellValue() ) ;
//			}
//			
//			log.debug( "Codes: " ) ;
//			it = ontologyCodes.cellIterator() ;
//			while( it.hasNext() ) {
//				Cell cell = it.next() ;
//				log.debug( cell.getStringCellValue() ) ;
//			}
//			
//			log.debug( "========= Values: " ) ;
//			DataFormatter df = new DataFormatter() ;
//			Iterator<Row> rowIt = project.getSheetOne().rowIterator() ;
//			rowIt.next() ; // tab past column names
//			rowIt.next() ; // tab past tool tips
//			rowIt.next() ; // tab past codes
//			while( rowIt.hasNext() ) {
//				Row row = rowIt.next() ;
//				log.debug( ">>> values for row number: " + row.getRowNum() ) ;
//				it = row.cellIterator() ;
//				while( it.hasNext() ) {
//					Cell cell = it.next() ;
//					log.debug( df.formatCellValue( cell ).trim() ) ;		
//				}
//			}
					
		}
		catch( NewProjectException cex ) {
			fail( "CreationException thrown: " + cex.getLocalizedMessage() ) ;
		}
						
	}
	
	public static void reinit() {

		String s = new String();
		StringBuffer sb = new StringBuffer();
		Connection connection = null ;
		try {

			FileReader fr = new FileReader( new File( "/home/jeff/ws-brisskit/new-project-from-spreadsheet/src/main/resources/scripts/db-tables/reinit.sql" ) );
			BufferedReader br = new BufferedReader(fr);

			while ((s = br.readLine()) != null) {

				s = s.replaceAll("<SCHEMA_NAME>", "project1" ) ;
				s = s.replaceAll("<PROJECT_NAME>", "project1" ) ;


				sb.append(s);

			}
			br.close();

			String[] inst = sb.toString().split(";");

			connection = Base.getSimpleConnectionPG();	

			Statement st = connection.createStatement();

			for (int i = 0; i < inst.length; i++) {
				if (!inst[i].trim().equals("")) {

					try {
						st.executeUpdate(inst[i]);
					} catch (Exception e) {
						log.error( "ERROR on statement : " + inst[i], e ) ;
					}

					// log.debug( ">>"+inst[i] ) ;
				}
			}


		} catch (Exception e) {
			log.error( "*** Outer Error : ", e ) ;
			e.printStackTrace() ;
		}		
		finally {
			
		}

	}
}
