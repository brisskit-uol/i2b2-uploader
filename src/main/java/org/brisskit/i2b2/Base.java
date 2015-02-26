package org.brisskit.i2b2 ;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.* ;

public class Base {
	
	private static Logger logger = Logger.getLogger( Base.class ) ;
	
	//
	// These are the keys (or partial keys) to properties held within the config.properties file...	
	final static String ENVIRONMENT = "env" ;
	
	final static String PG_DB_NAME = "pg_db_name" ;
	final static String PG_DB_URL = "pg_db_url" ;
	final static String PG_DB_U = "pg_db_u";
	final static String PG_DB_P = "pg_db_p";		
	final static String SCRIPTS_LOCATION = "scripts_location";
	final static String JBOSS_DEPLOYMENT_DIRECTORY = "jboss_deploy_dir";
	final static String JBOSS_DATASET_DEFINITION_TEMPLATE = "jboss_dsfile_template" ;
			
	/* Properties */
	protected Properties props = new Properties();
	protected String pg_db_name;
	protected String pg_db_url;	
	protected String jboss_deploy_dir ;
	protected String pg_db_u;
	protected String pg_db_p;
	
	protected Connection connection;
	private boolean disposed = false ;
	
	public Base() throws UploaderException {
		this.setUp() ;
	}
	
	private void setUp() throws UploaderException {
		enterTrace( "Base.setUp()" ) ; 
		InputStream configPropertiesStream = null ;
		try {
			
			if( jboss_deploy_dir == null ) {
				
				configPropertiesStream = Base.class.getClassLoader().getResourceAsStream( "uploader.properties" ) ;			
				props.load( configPropertiesStream ) ;

				String env = props.getProperty( ENVIRONMENT );

				pg_db_name = props.getProperty( env + "." + PG_DB_NAME );
				pg_db_url  = props.getProperty( env + "." + PG_DB_URL );
				pg_db_u = props.getProperty( env + "." + PG_DB_U );
				pg_db_p = props.getProperty( env + "." + PG_DB_P );
				jboss_deploy_dir = props.getProperty( env + "." + JBOSS_DEPLOYMENT_DIRECTORY );

				logger.info(env + "." + PG_DB_NAME + "= " + pg_db_name);				;
				logger.info(env + "." + PG_DB_U + "= " + pg_db_u);
				logger.info(env + "." + PG_DB_P + "= " + pg_db_p);	
				logger.info(env + "." + PG_DB_URL + "= " + pg_db_url);
				logger.info(env + "." + JBOSS_DEPLOYMENT_DIRECTORY + "= " + jboss_deploy_dir);

				try { 
					configPropertiesStream.close() ; 
				}
				catch( IOException iox ) {
					logger.warn( "Could not close configPropertiesStream", iox ) ;
				}
			}
			
		} catch (IOException e) {
			throw new UploaderException( e ) ;
		}
		finally {
			exitTrace( "Base.setUp()" ) ; 
		}
		
	}

	
	public Connection getSimpleConnectionPG() throws UploaderException {
//		enterTrace( "Base.getSimpleConnectionPG()" ) ;
		try {
			if( disposed ) {
				String message = "Attempting to use JDBC connection after dispose call." ;
				logger.error( message ) ;
				throw new UploaderException( message ) ;
			}
			if( connection == null ) {
				
				Class.forName( "org.postgresql.Driver" ).newInstance();
				
				connection = DriverManager.getConnection( "jdbc:postgresql://" + pg_db_url + "/"+ pg_db_name +"?user=" + pg_db_u+ "&password=" + pg_db_p 
						                         , pg_db_name
						                         , pg_db_p ) ;
			}
			return connection;
		}
		catch( InstantiationException iex ) {
			logger.error( "Cannot load db driver: " + "org.postgresql.Driver", iex ) ;
			throw new UploaderException( iex ) ;
		}
		catch( IllegalAccessException iax ) {
			logger.error( "Cannot load db driver: " + "org.postgresql.Driver", iax ) ;
			throw new UploaderException( iax ) ;
		}
		catch( ClassNotFoundException cnfex ) {
			logger.error( "Cannot load db driver: " + "org.postgresql.Driver", cnfex ) ;
			throw new UploaderException( cnfex ) ;
		}
		catch( SQLException sqlex ) {
			logger.error( "Driver loaded, but cannot connect to db: " + "jdbc:postgresql://" + pg_db_url + "/"+ pg_db_name +"?user=" + pg_db_u+ "&password=" + pg_db_p ) ;
			throw new UploaderException( sqlex ) ;
		}
		finally {
//			exitTrace( "Base.getSimpleConnectionPG()" ) ;
		}	
	}
	
	
	public void dispose() throws UploaderException {
		enterTrace( "Base.dispose()" ) ;
		try {
			if( disposed ) {
				return ;
			}
			// Try to commit any outstanding transaction
			try{ 
				this.connection.commit() ; 
			}
			catch( SQLException sqlxcommit ) {
				;
			}
			this.connection.close() ;
			this.disposed = true ;
		}
		catch( SQLException sqlx ) {
			String message = "Did not successfully close JDBC connection on dispose call" ;
			logger.error( message, sqlx ) ;
			throw new UploaderException( message, sqlx ) ;
		}
		finally {
			exitTrace( "Base.dispose()" ) ;
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
