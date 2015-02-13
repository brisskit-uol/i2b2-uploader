package org.brisskit.i2b2 ;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Base {
	
	private static Log log = LogFactory.getLog( Base.class ) ;

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
	static Properties props = new Properties();
	protected static String pg_db_name;
	protected static String pg_db_url;	
	protected static String jboss_deploy_dir ;
	protected static String pg_db_u;
	protected static String pg_db_p;
	
	public static Connection con;
	
	static public void setUp() throws UploaderException {
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

				log.info(env + "." + PG_DB_NAME + "= " + pg_db_name);				;
				log.info(env + "." + PG_DB_U + "= " + pg_db_u);
				log.info(env + "." + PG_DB_P + "= " + pg_db_p);	
				log.info(env + "." + PG_DB_URL + "= " + pg_db_url);
				log.info(env + "." + JBOSS_DEPLOYMENT_DIRECTORY + "= " + jboss_deploy_dir);

				try { 
					configPropertiesStream.close() ; 
				}
				catch( IOException iox ) {
					log.warn( "Could not close configPropertiesStream", iox ) ;
				}
			}
			
		} catch (IOException e) {
			throw new UploaderException( e ) ;
		}
		finally {
			exitTrace( "Base.setUp()" ) ; 
		}
		
	}
	
	
	static public Connection _getSimpleConnectionPG() throws UploaderException {
		enterTrace( "getSimpleConnectionPG()" ) ;
		
		String DB_CONN_STRING = "jdbc:postgresql://" + pg_db_url + "/"+ pg_db_name +"?user=" + pg_db_u+ "&password=" + pg_db_p;
		String DRIVER_CLASS_NAME = "org.postgresql.Driver";
		String USER_NAME = pg_db_u;
		String PASSWORD = pg_db_p;

		try {
			Class.forName(DRIVER_CLASS_NAME).newInstance();
		} 
		catch( Exception ex ) {
			log.error( "Check classpath. Cannot load db driver: " + DRIVER_CLASS_NAME ) ;
			throw new UploaderException( ex ) ;
		}

		if (con == null) {
			try {
				con = DriverManager.getConnection( DB_CONN_STRING, USER_NAME, PASSWORD ) ;
			} 
			catch (SQLException sqlex) {
				log.error( "Driver loaded, but cannot connect to db: " + DB_CONN_STRING ) ;
				throw new UploaderException( sqlex ) ;
			}
		}
		exitTrace( "getSimpleConnectionPG()" ) ;
		return con;
	}
	
	static public Connection getSimpleConnectionPG() throws UploaderException {
		enterTrace( "getSimpleConnectionPG()" ) ;
		try {
			if (con == null) {
				
				Class.forName( "org.postgresql.Driver" ).newInstance();
				
				con = DriverManager.getConnection( "jdbc:postgresql://" + pg_db_url + "/"+ pg_db_name +"?user=" + pg_db_u+ "&password=" + pg_db_p 
						                         , pg_db_name
						                         , pg_db_p ) ;
			}
			return con;
		}
		catch( InstantiationException iex ) {
			log.error( "Cannot load db driver: " + "org.postgresql.Driver", iex ) ;
			throw new UploaderException( iex ) ;
		}
		catch( IllegalAccessException iax ) {
			log.error( "Cannot load db driver: " + "org.postgresql.Driver", iax ) ;
			throw new UploaderException( iax ) ;
		}
		catch( ClassNotFoundException cnfex ) {
			log.error( "Cannot load db driver: " + "org.postgresql.Driver", cnfex ) ;
			throw new UploaderException( cnfex ) ;
		}
		catch( SQLException sqlex ) {
			log.error( "Driver loaded, but cannot connect to db: " + "jdbc:postgresql://" + pg_db_url + "/"+ pg_db_name +"?user=" + pg_db_u+ "&password=" + pg_db_p ) ;
			throw new UploaderException( sqlex ) ;
		}
		finally {
			exitTrace( "getSimpleConnectionPG()" ) ;
		}	
	}
	
	/**
	 * Utility routine to enter a structured message in the trace log that the given method 
	 * has been entered. 
	 * 
	 * @param entry: the name of the method entered
	 */
	public static void enterTrace( String entry ) {
		I2B2Project.enterTrace( log, entry ) ;
	}

    /**
     * Utility routine to enter a structured message in the trace log that the given method 
	 * has been exited. 
	 * 
     * @param entry: the name of the method exited
     */
    public static void exitTrace( String entry ) {
    	I2B2Project.exitTrace( log, entry ) ;
	}
}
