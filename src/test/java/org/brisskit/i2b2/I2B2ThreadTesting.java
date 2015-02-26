/**
 * 
 */
package org.brisskit.i2b2;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * @author jeff
 *
 */
public class I2B2ThreadTesting {
	
	private static Logger logger = Logger.getLogger( I2B2ThreadTesting.class ) ;

	/**
	 * @param args
	 */
	public static void main(String[] args) {	
		I2B2ThreadTesting tt = new I2B2ThreadTesting() ;
		tt.exec() ;
	}
	
	public I2B2ThreadTesting() {
		
	}
	
	public synchronized void exec() {
		File spreadsheetFile = new File( I2B2ThreadTesting.class.getClassLoader().getResource( "spreadsheets/test-01-obsdatecol.xls").getFile() ) ;
		String projectId = "testthreading" ;
		I2B2Project project = null ;
		try {
			ConcurrencyTest ct = new ConcurrencyTest( projectId, spreadsheetFile, 1 ) ;
			Thread thread = new Thread( ct ) ;	
			thread.start() ;
			this.wait( 1200000 ) ;
		}
		catch( Exception ex ) {			
			logger.error( "Exception thrown", ex ) ;
		}
		finally {
			if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
		}
	}
	
	
	 public static class ConcurrencyTest implements Runnable {

	    	String projectId ;
	    	File spreadsheetFile ;
	    	int threadNumber ;
	    	
	    	ConcurrencyTest( String projectId, File spreadsheetFile, int threadNumber ) {
	    		this.projectId = projectId ;
	    		this.spreadsheetFile = spreadsheetFile ;
	    		this.threadNumber = threadNumber ;
	    	}
	    	
			@Override
			public synchronized void run() {
				I2B2Project project = null ;
				try {
					//
					// Delete project if it already exists...
					I2B2Project.Factory.deleteIfProjectExists( projectId ) ;
					//
					// Create new project with all it db artifacts
					project = I2B2Project.Factory.newInstance( projectId ) ;
					//
					// Process the spreadsheet...
					project.processSpreadsheet( spreadsheetFile ) ;
					project.dispose();
					project = null ;
				}
				catch( UploaderException cex ) {			
					logger.error( "Thread number " + threadNumber + " failed.", cex ) ;
				}
				finally {
					if( project != null ) {	try{ project.dispose() ; } catch( Exception ex ) { ; } }
					notifyAll() ;
				}
				
			}
	    	
	    }

}
