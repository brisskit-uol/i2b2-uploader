package org.brisskit.i2b2 ;

import java.io.FileNotFoundException;
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
	final static String PG_DB_SCHEMA = "pg_db_schema" ;
	final static String PG_DB_URL = "pg_db_url" ;
	final static String PG_DB_U = "pg_db_u";
	final static String PG_DB_P = "pg_db_p";
	
	final static String PROJECT_NAME = "project_name" ;
	
	final static String SOURCE_SYSTEM = "sourcesystem" ;	
	final static String SCRIPTS_LOCATION = "scripts_location";
	final static String I2B2_PROJECT_TITLE = "i2b2_project_title";	
		
	final static String MYSQL_DB_NAME = "mysql_db_name";
	final static String MYSQL_DB_URL = "mysql_db_url";
	final static String MYSQL_DB_U = "mysql_db_u";
	final static String MYSQL_DB_P = "mysql_db_p";
			
	/* Properties */
	static Properties props = new Properties();
	protected static String pg_db_name;
	protected static String pg_db_schema;
	protected static String project_name;
	protected static String pg_db_url;
	protected static String sourcesystem;
	protected static String scripts_location;
	protected static String i2b2_project_title;
	protected static String mysql_db_name;
	protected static String mysql_db_url;
	protected static String mysql_db_u;
	protected static String mysql_db_p;
		
	protected static String pg_db_u;
	protected static String pg_db_p;
	
	public static Connection con;
	
	static public void setUp( String propertiesFileName ) throws UploaderException {
		enterTrace( "Base.setUp()" ) ; 
		
		InputStream inputStream = Base.class.getClassLoader().getResourceAsStream( propertiesFileName ) ;
		
		try {

			if (inputStream == null) {
				log.info("Base Class - Properties Path Not Found") ;
				throw new UploaderException( "property file '" + propertiesFileName + "' not found in the classpath" ) ;
			}


			props.load(inputStream);

			String env = props.getProperty( ENVIRONMENT );

			pg_db_name = props.getProperty( env + "." + PG_DB_NAME );
			pg_db_schema = props.getProperty( env + "." + PG_DB_SCHEMA );
			project_name = props.getProperty( env + "." + PROJECT_NAME );
			pg_db_url  = props.getProperty( env + "." + PG_DB_URL );
			pg_db_u = props.getProperty( env + "." + PG_DB_U );
			pg_db_p = props.getProperty( env + "." + PG_DB_P );

			sourcesystem = props.getProperty( env + "." + SOURCE_SYSTEM );
			scripts_location = props.getProperty( env + "." + SCRIPTS_LOCATION );
			i2b2_project_title = props.getProperty( env + "." + I2B2_PROJECT_TITLE );

			mysql_db_name = props.getProperty( env + "." + MYSQL_DB_NAME );
			mysql_db_url = props.getProperty( env + "." + MYSQL_DB_URL );		
			mysql_db_u = props.getProperty( env + "." + MYSQL_DB_U );
			mysql_db_p = props.getProperty( env + "." + MYSQL_DB_P );

			log.info(env + "." + PG_DB_NAME + "= " + pg_db_name);				
			log.info(env + "." + PG_DB_SCHEMA + "= " + pg_db_schema);
			log.info(env + "." + PG_DB_U + "= " + pg_db_u);
			log.info(env + "." + PG_DB_P + "= " + pg_db_p);	

			log.info(env + "." + PROJECT_NAME + "= " + project_name);
			log.info(env + "." + PG_DB_URL + "= " + pg_db_url);
			log.info(env + "." + SOURCE_SYSTEM + "= " + sourcesystem);
			log.info(env + "." + SCRIPTS_LOCATION + "= " + scripts_location);
			log.info(env + "." + I2B2_PROJECT_TITLE + "= " + i2b2_project_title);

			log.info(env + "." + MYSQL_DB_NAME + "= " + mysql_db_name);
			log.info(env + "." + MYSQL_DB_URL + "= " + mysql_db_url);
			log.info(env + "." + MYSQL_DB_U + "= " + mysql_db_u);
			log.info(env + "." + MYSQL_DB_P + "= " + mysql_db_p);

		} catch (IOException e) {
			throw new UploaderException( e ) ;
		}
		finally {
			exitTrace( "Base.setUp()" ) ; 
		}
		
	}
	
	
	static public Connection getSimpleConnectionSQLMYSQL() {
		String DB_CONN_STRING = "jdbc:mysql://" + mysql_db_url + ":3306/" + mysql_db_name;

		String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
		String USER_NAME = mysql_db_u;
		String PASSWORD = mysql_db_p;

		Connection result = null;
		try {
			Class.forName(DRIVER_CLASS_NAME).newInstance();
		} catch (Exception ex) {
			System.out.println("Check classpath. Cannot load db driver: "
					+ DRIVER_CLASS_NAME);
		}

		try {
			result = DriverManager.getConnection(DB_CONN_STRING, USER_NAME,
					PASSWORD);
		} catch (SQLException e) {
			System.out.println("Driver loaded, but cannot connect to db: "
					+ DB_CONN_STRING);
		}
		return result;
	}
	
		

	
	
	static public Connection getSimpleConnectionPG() throws UploaderException {
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
