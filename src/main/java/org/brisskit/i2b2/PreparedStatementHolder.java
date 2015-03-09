package org.brisskit.i2b2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class PreparedStatementHolder {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger( PreparedStatementHolder.class ) ;
	
	private Connection connection ;
	
	private Map<String,PreparedStatement> psMap = new ConcurrentHashMap<String,PreparedStatement>() ;
	
	@SuppressWarnings("unused")
	private PreparedStatementHolder() {}
	
	public PreparedStatementHolder( Connection connection ) {
		this.connection = connection ;
	}
	
	public void setPreparedStatement( String key, String sql, boolean autogenerateKeys ) throws UploaderException {	
		try {
			PreparedStatement ps = null ;
			if( psMap.containsKey( key ) ) {
				throw new UploaderException( "PreparedStatement key: [" + key + "] already exists." ) ;
	 		}			
			if( autogenerateKeys == true ) {
				ps = connection.prepareStatement( sql, PreparedStatement.RETURN_GENERATED_KEYS ) ;
			}
			else {
				ps = connection.prepareStatement( sql ) ;
			}	
			psMap.put( key, ps ) ;
		}
		catch( SQLException sqlex ) {
			logger.error( "Failed to prepare statement:\n\"" + sql + "\"", sqlex ) ;
			throw new UploaderException( sqlex ) ;
		}
	}
	
	
	public void setPreparedStatement( String key, String sql ) throws UploaderException {	
		setPreparedStatement( key, sql, false ) ;
	}
	
	public PreparedStatement getPreparedStatement( String key ) throws UploaderException {	
		PreparedStatement ps = psMap.get( key ) ;
		if( ps == null ) {
			throw new UploaderException( "PreparedStatement key: [" + key + "] does not exist." ) ;
		}
		try {
			if( ps.isClosed() ) {
				throw new UploaderException( "PreparedStatement key: [" + key + "] is closed." ) ;
			}
		}
		catch( SQLException sqlex ) {
			throw new UploaderException( "PreparedStatement key: [" + key + "] could be closed.", sqlex ) ;
		}		
		return ps ;
	}
	
	public void empty() {
		Iterator<PreparedStatement> it = psMap.values().iterator() ;
		while( it.hasNext() ) {
			try {
				it.next().close() ;
			}
			catch( SQLException sqlex ) {
				logger.info( "Failed to close prepared statement" ) ;
			}
		}
		psMap.clear() ;
	}
	
}
