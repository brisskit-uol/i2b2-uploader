/**
 * 
 */
package uk.org.briccs.dataimport;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.datatype.DatatypeFactory;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.StatusType;

import edu.harvard.i2b2.crc.loader.datavo.loader.query.DataFormatType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.DataListType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.FactLoadOptionType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.InputOptionListType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadOptionType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.OutputOptionListType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.OutputOptionType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.PublishDataRequestType;
import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.eclipse.login.LoginHelper;
import edu.harvard.i2b2.eclipse.plugins.fr.ws.CrcServiceDriver;
import edu.harvard.i2b2.eclipse.plugins.fr.ws.FrServiceDriver;
import edu.harvard.i2b2.eclipse.plugins.fr.ws.GetPublishDataResponseMessage;
import edu.harvard.i2b2.eclipse.plugins.fr.ws.SendfileResponseMessage;
import edu.harvard.i2b2.eclipse.plugins.importData.utils.MD5;
import edu.harvard.i2b2.fr.datavo.fr.query.SendfileRequestType;
import edu.harvard.i2b2.pm.datavo.pm.CellDataType;
import edu.harvard.i2b2.pm.datavo.pm.CellDatasType;
import edu.harvard.i2b2.pm.datavo.pm.PasswordType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectType;

/**
 * 
 * NB: The System property "java.endorsed.dirs" must be set to the endorsed_lib
 * directory of this project on invocation of the JVM. For example...
 * 
 * -Djava.endorsed.dirs=/home/jl99/workspace-i2b2wb/i2b2-importpdo/endorsed_lib
 * 
 * 
 * 
 * @author jl99
 *
 */
/**
 * @author jl99
 *
 */
public class ImportPdo {

	private static Log log = LogFactory.getLog( ImportPdo.class ) ;
	
	private static final String USER = "import.user" ;
	private static final String PASSWORD = "import.password" ;
	private static final String DOMAIN_ID = "import.domainID" ;
	private static final String PROJECT_ID = "import.projectID" ;
	private static final String PM_URL = "import.pmURL" ;

	/**
	 *  USAGE.
	 *  
	 */
	private static final String USAGE =
		"Usage: ImportPdo {Parameters}\n" +       
		"Parameters:\n" +
		" -config=path to config file\n" +
		" -import=path to i2b2 PDO file\n" +
		" -append=true/false" ;
	
	private File pdoFile ;
	private Boolean appendFlag = null ;
	
	private File configFile ;
	
	private String pmUrl ;
	private String user ;
	private PasswordType password ;
	private String domainId ;
	private String projectId ;
	
	private UserInfoBean userInfoBean ;
	
	private LoginHelper loginHelper = new LoginHelper();

	/**
	 * Should follow usage statement.
	 * 
	 * @param args
	 */
	public static void main( String[] args ) {

		ImportPdo importer = new ImportPdo() ;
		//
		// Retrieve command line arguments and initialize...
		boolean good = importer.retrieveArgs( args ) ;		
		if( !good ) {
			log.error( USAGE ) ; 
			System.exit(1) ;
		}
		//
		// Process the config file...
		good = importer.retrieveConfigDetails() ;
		if( !good ) {
			System.exit(1) ;
		}
		//
		// Do the import process...
		good = importer.exec() ;
		if( !good ) {
			log.debug( "Failure!" ) ;
			System.exit( 1 ) ;
		}
		//
		// All appears to have gone well...
		log.debug( "Done!" ) ;	
		System.exit( 0 ) ;

	}
	
	public ImportPdo() {
		System.setProperty( "webServiceMethod", "REST" ) ;
	}
	
	public boolean exec() {
		boolean success = false ;
		//
		// Login to the PM cell...
		success = login() ;
		if( success == false ) 
			return false ;
		//
		// Upload the PDO file to the File Repository...
		success = uploadToFileRepo() ;
		if( success == false )
			return false ;
		//
		// Invoke the load function within the CRC Cell...
		return processPdo() ;

	}
	
	public boolean login() {

		try {
			this.userInfoBean = loginHelper.getUserInfo( this.user, this.password, this.pmUrl, this.domainId, false);
			
			List<String> cellList = this.userInfoBean.getCellList() ;
			
			//
			// If there is no cell info, the login failed...
			if( cellList == null ) {
				log.error( "Failed to login to PM cell. " +
						   "User: [" + this.user + "] " +
						   "PM url: [" + this.pmUrl + "] " +
						   "Domain Id: [" + this.domainId + "]"  ) ;
				return false ;
			}
			//
			// Give some details if we are developing...
			if( log.isDebugEnabled() ) {
				for (String cell : cellList) {
					log.debug( cell + ": " + this.userInfoBean.getCellDataUrl( cell ) ) ;
				}
			}
			
			//
			// OK, we're logged in. Now we need to match the project...
			List<ProjectType> l = UserInfoBean.getInstance().getProjects();
			
			Iterator<ProjectType> i = l.iterator() ;
			while( i.hasNext() ) {
				ProjectType pt = i.next() ;
				if( log.isDebugEnabled() ) {
					log.debug( pt.getKey() + " | " + pt.getId() + " | " + pt.getName() + " | " + pt.getDescription() ) ;
				}
				//
				// Check for a match (I've chosen to ignore case):
				if( pt.getId().equalsIgnoreCase( this.projectId ) ) {
					UserInfoBean.selectedProject( pt ) ;
					break ;
				}
			}
			//
			// If project not found, something is fundamentally wrong:
			// Give the user feedback and bail out...
			if( UserInfoBean.selectedProject() == null ) {
				log.error( "Failed to find matching project: [" + this.projectId + "]" ) ;
			    return false ;
			}
			//
			// Adjust web service addresses for other cells to conform with PM cell...
			boolean adjusted = adjustWebServiceAddresses() ;
			if( !adjusted ) {
				//
				// We bail out if any problems...
				return false ;
			}
		} 
		catch(Exception ex) {
			log.error( "System error during login. Please consult your systems admin. Remember to preserve your log file.", ex ) ;
			return false ;
		}
		//
		// Success!
		log.info( "Logged in to PM cell." ) ;
		return true ;
		
	}
	
	/**
	 * Overcomes a tricky problem with respect to differing cell addresses when cells are co-located and executing
	 * behind a load balancer or edge server. Read on ...
	 * <br/><br/>
	 * The critical assumption here is that all of the hive cells are co-located together. If this is not the case,
	 * then the install and admin scripts for BRICCS and BRISSkit are invalid to start with, and none of this code
	 * will work in any case. (Separate remote location of cells is a future possibility, so bear this in mind ).
	 * <br/><br/>
	 * Given the above assumption, we then have a number of possible contexts in which ImportPdo could be working: <br>
	 * (1) It is executing remotely from the hive with or without SSL being implemented. <br/>
	 * (2) It is executing co-located with the hive without SSL. <br/>
	 * (3) It is executing co-located with the hive with SSL implemented by the hive (ie: by JBoss). <br/>
	 * (4) It is executing co-located with the hive but with SSL implemented by a separate load balancer or edge server.
	 *     ie: behind some sort of external firewall which is doing the burden of encryption/decryption of messages. 
	 * <br/> <br/>
	 * It is the latter situation (4) where problems can occur. In all of the others the protocol and ports of the PM cell's URL should
	 * match those of the FR and CRC cells and the code below is simply redundant. In situation (4) the PM cell will return cell URL's
	 * to the user where the protocol and port do not match the PM's URL taken from the ImportPdo configuration file. 
	 * So, something like the following will occur:
	 * <br/>
	 * PM:  http://localhost:8080/i2b2/rest/PMService/
	 * FR:  https://lamp-lbi-16.rcs.le.ac.uk:8443/i2b2/services/FRService/
	 * CRC: https://lamp-lbi-16.rcs.le.ac.uk:8443/i2b2/rest/QueryToolService/
	 * <br/><br/>
	 * Under these circumstances, attempting to use the https protocol with the 'encrypted' port for the FR and CRC can -
	 * and probably will - fail. That is, ImportPdo will attempt to use these addresses BEHIND the firewall, which will then intervene. 
	 * It is possible that the load balancing layer could be configured to allow such a convoluted route for processing of messages. 
	 * But the current routine simply allows the localhost and a normal/un-encrypted port to be substituted.
	 * <br/> <br/>
	 * Failure will log an error message and prevent any further processing by ImportPdo.
	 * 
	 * @return boolean - true if addresses successfully adjusted, false otherwise.
	 */
	private boolean adjustWebServiceAddresses() {
		
		List<String> cellList = this.userInfoBean.getCellList() ;		
		//
		// Give some details before change, if we are developing...
		if( log.isDebugEnabled() ) {
			for (String cell : cellList) {
				log.debug( cell + ": " + this.userInfoBean.getCellDataUrl( cell ) ) ;
			}
		}
		
		try {
			//
			// Retrieve the relevant protocol and port for the PM cell...
			CellDatasType cellDatas = userInfoBean.getCellDatas() ;
			URL pmurl = new URL( this.pmUrl ) ;
			String pmProtocol = pmurl.getProtocol() ;
			int pmPort = pmurl.getPort() ;
			if( pmPort == -1 ) {
				pmPort = pmurl.getDefaultPort() ;
			}
			//
			// Retrieve the pm Host...
			String pmHost = pmurl.getHost() ;
			//
			// Change the url for each cell to be in line with the PM cell...
			for (CellDataType cellData :cellDatas.getCellData())
			{
				 URL url = new URL( cellData.getUrl() ) ;
				 URL replacementUrl = null ;
				 
				 if( pmPort == -1 ) {
					 replacementUrl = new URL( pmProtocol
		                                     , pmHost.equals( "localhost") ? "localhost" : url.getHost()
		                                     , url.getFile() ) ;
				 }
				 else {
					 replacementUrl = new URL( pmProtocol
		                                     , pmHost.equals( "localhost") ? "localhost" : url.getHost()
		                                     , pmPort
		                                     , url.getFile() ) ;
				 }				 
				 cellData.setUrl( replacementUrl.toString() ) ;
			}
			//
			// Show the changes if we are developing...
			if( log.isDebugEnabled() ) {
				for (String cell : cellList) {
					log.debug( cell + ": " + this.userInfoBean.getCellDataUrl( cell ) ) ;
				}
			}
			
			return true ;
		}
		catch( Exception ex ) {
			log.error( "Unable to adjust web service addresses for FR and CRC cells", ex ) ;			
			return false ;
		}

	} // end of adjustWebServiceAddresses
	
	public boolean setPDOFile(String filename) {
		this.pdoFile = new File( filename ) ;	
		return true;
		
	}
	
	public boolean uploadToFileRepo() {
		try {
			
			SendfileRequestType parentType = new SendfileRequestType();

			edu.harvard.i2b2.fr.datavo.fr.query.File file = new edu.harvard.i2b2.fr.datavo.fr.query.File();

			file.setAlgorithm("MD5");
			file.setHash( MD5.asHex( MD5.getHash( this.pdoFile ) ) ) ;
			file.setName( this.pdoFile.getName() ) ;
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis( this.pdoFile.lastModified() ) ;
			file.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
			file.setOverwrite("true");
			file.setSize( BigInteger.valueOf( this.pdoFile.length() ) ) ;
			parentType.setUploadFile(file);		

			SendfileResponseMessage msg = new SendfileResponseMessage();
			StatusType procStatus = null;	

			while(procStatus == null || !procStatus.getType().equals("DONE")){

				String response = 
					FrServiceDriver.getSendfileRequest( new String[]{ this.pdoFile.getAbsolutePath()}, parentType, "CRC");

				procStatus = msg.processResult(response);
				if(procStatus.getType().equals( "ERROR" ) ) {	
					log.error( "File uploading failed. Process status returned: [" + procStatus.getValue() + "]" ) ;
					return false ;				 
				}	
			}

		} catch (Exception ex ) {
			log.error( "System error during file upload. Please consult your systems admin. Remember to preserve your log file.", ex ) ;
			return false ;
		}
		//
		// Success!
		log.info( "Uploaded file " + this.pdoFile.getName() + " to File Repository." ) ;
		return true ;

	}
	
	public boolean processPdo() {
		try {
			PublishDataRequestType parentType = new PublishDataRequestType();

			InputOptionListType ioType = new InputOptionListType();
			LoadType loadType = new LoadType();
			DataListType dlType = new DataListType();
			loadType.setClearTempLoadTables(Boolean.valueOf(System.getProperty("FRClearTempTables")));
			loadType.setCommitFlag(true);
			DataListType.LocationUri uri = new DataListType.LocationUri();
			uri.setValue(UserInfoBean.getInstance().getCellDataParam("FRC", "DestDir")
					+ UserInfoBean.getInstance().getCellDataParam("FRC", "PathSeparator")
					+ UserInfoBean.getInstance().getProjectId()
					+ UserInfoBean.getInstance().getCellDataParam("FRC", "PathSeparator")
					+ this.pdoFile.getName()
			);
			uri.setProtocolName(UserInfoBean.getInstance().getSelectedProjectParam("FRMethod"));
			if ((UserInfoBean.getInstance().getSelectedProjectParam("FRMethod") == null) || (uri.getProtocolName().equals("FR")))
			{
				uri.setProtocolName("FR"); //UserInfoBean.getInstance().getSelectedProjectParam("FRMethod"));
				uri.setValue(this.pdoFile.getName());
			}
			dlType.setLocationUri( uri ) ;
			dlType.setDataFormatType( DataFormatType.PDO ) ;
			dlType.setSourceSystemCd( "i2b2" ) ;
			dlType.setLoadLabel( UserInfoBean.getInstance().getProjectId() + "-" + this.pdoFile.getName() ) ;
			ioType.setDataFile( dlType ) ;

			LoadOptionType loType = new LoadOptionType();
			loType.setEncryptBlob(false);
			loType.setIgnoreBadData(true);

			FactLoadOptionType floType = new FactLoadOptionType();
			//
			// appendFlag is supplied by the command line args...
			floType.setAppendFlag( this.appendFlag ) ;
			//
			// NOTES:
			// (1) Need to consider the possible differences here.
			//     Should the appendFlag setting be the same in all cases?
			// (2) PatientSet and ObserverSet are not requested.
			//     PatientSet at least is an omission? 
			//     Corrected below but not yet tested.			
			loadType.setLoadPidSet( floType ) ;
			loadType.setLoadObservationSet( floType ) ;
			loadType.setLoadEidSet( floType ) ;
			loadType.setLoadEventSet( floType ) ;
			loadType.setLoadPatientSet( floType ) ;

			OutputOptionListType ooType = new OutputOptionListType();
			ooType.setDetail(true);
			OutputOptionType outputType = new OutputOptionType();
			outputType.setOnlykeys(true);
			ooType.setObservationSet(outputType);
			// The following is missing from Saj's version:
			ooType.setPatientSet(outputType);
			ooType.setPidSet(outputType);			
			ooType.setEventSet(outputType);
			ooType.setEidSet(outputType);	

			parentType.setInputList(ioType);
			parentType.setLoadList(loadType);
			parentType.setOutputList(ooType);

			GetPublishDataResponseMessage msg = new GetPublishDataResponseMessage();
			StatusType procStatus = null;	
			while(procStatus == null || !procStatus.getType().equals("DONE")){
				String response = CrcServiceDriver.getPublishDataRequest(parentType, "CRC");

				procStatus = msg.processResult(response);
				if (procStatus.getType().equals("ERROR")){		
					throw new I2B2Exception( procStatus.getValue() ) ;
				}			
			}

		} 
		catch( AxisFault afx ) {
			log.error( "System error during CRC processing of PDO. An AxisFault occurred. Please consult your systems admin. Remember to preserve your log file.", afx ) ;
			return false ;
		} 
		catch( Exception ex ) {
			log.error( "System error during CRC processing of PDO. Please consult your systems admin. Remember to preserve your log file.", ex ) ;
			return false ;
		}
		//
		// Success!
		log.info( "CRC has processed PDO file " + this.pdoFile.getName() ) ;
		return true ;
	}

   	/**
	 * @param args
	 * @return true if all the mandatory arguments were entered, false otherwise.
	 */
	public boolean retrieveArgs( String[] args ) {
		
		boolean retval = false ;
		
        if( args == null )
        	return retval ;
        if( args.length == 0 ) 
        	return retval ;
  
        for( int i=0; i<args.length; i++ ) {
        	
        	if( args[i].startsWith( "-config=" ) ) {
        		this.configFile = new File( args[i].substring(8) ) ;
        		continue ;
        	}
        	if( args[i].startsWith( "-c=" ) ) {
        		this.configFile = new File( args[i].substring(3) ) ;
        		continue ;
        	}
        	if( args[i].startsWith( "-import=" ) ) { 
        		this.pdoFile = new File( args[i].substring(8) ) ;
        		continue ;
        	}
        	if( args[i].startsWith( "-i=" ) ) { 
        		this.pdoFile = new File( args[i].substring(3) ) ;
        		continue ;
        	}
        	if( args[i].startsWith( "-append=" ) ) { 
        		String append = args[i].substring(8) ;
        		if( append.equalsIgnoreCase( "TRUE" ) ) {
        			this.appendFlag = Boolean.valueOf( true ) ;
        		}
        		else if( append.equalsIgnoreCase( "FALSE" ) ) {
        			this.appendFlag = Boolean.valueOf( false ) ;
        		}
        		continue ;
        	}
        	if( args[i].startsWith( "-a=" ) ) { 
        		String append = args[i].substring(3) ;
        		if( append.equalsIgnoreCase( "TRUE" ) ) {
        			this.appendFlag = Boolean.valueOf( true ) ;
        		}
        		else if( append.equalsIgnoreCase( "FALSE" ) ) {
        			this.appendFlag = Boolean.valueOf( false ) ;
        		}
        	}
        	
        } // end for
        
        //
        // Assume correctness...
        retval = true ;
        if( configFile == null ) {
        	log.error( "-config parameter is mandatory." ) ;
        	retval =  false ;
        }
        else if( configFile.exists() == false ) {
        	log.error( "Config file does not exist: [" + this.configFile.getAbsolutePath() + "]" ) ;
        	retval = false ;
        }
        else if( configFile.isFile() == false ) {
        	log.error( "Config file is not a normal file: [" + this.configFile.getAbsolutePath() + "]" ) ;
        	retval = false ;
        }

        if( pdoFile == null ) {
        	log.error( "-import parameter is mandatory." ) ;
        	retval =  false ;
        }
        else if( this.pdoFile.exists() == false ) {
        	log.error( "PDO file does not exist: " + this.pdoFile.getAbsolutePath() ) ;
        	retval = false ;
        } else if( this.pdoFile.isFile() == false ) {
        	log.error( "PDO file is not a normal file: " + this.pdoFile.getAbsolutePath() ) ;
        	retval = false ;
        }

        if( this.appendFlag == null ) {
        	log.error( "'-append=' argument is mandatory with a setting of either 'true' or 'false' " ) ;
        	retval = false ;
        }
 
        return retval ;
    }
	
	public boolean retrieveConfigDetails() {
		boolean retVal = true ;
		
		try {		
			Properties properties =  new Properties() ;
			properties.load( new FileReader( configFile ) ) ;
			
			this.user = properties.getProperty( USER ) ;
			if( user == null ) {
				log.error( USER + " missing from config file." ) ;
				retVal = false ;
			}
			
			String pw = properties.getProperty( PASSWORD ) ;
			if( pw == null ) {
				log.error( PASSWORD + " missing from config file." ) ;
				retVal = false ;
			}
			else {
				this.password = new PasswordType() ;
	    		this.password.setValue( pw ) ;
	    		this.password.setIsToken( false ) ;
			}
			
			this.domainId = properties.getProperty( DOMAIN_ID ) ;
			if( domainId == null ) {
				log.error( DOMAIN_ID + " missing from config file." ) ;
				retVal = false ;
			}
			
			this.projectId = properties.getProperty( PROJECT_ID ) ;
			if( projectId == null ) {
				log.error( PROJECT_ID + " missing from config file." ) ;
				retVal = false ;
			}
			
			this.pmUrl = properties.getProperty( PM_URL ) ;
			if( pmUrl == null ) {
				log.error( PM_URL + " missing from config file." ) ;
				retVal = false ;
			}
			
		}
		catch( IOException iox ) {
			log.error( "Unable to read config file: " + configFile.getAbsolutePath()  ) ;
			retVal = false ;
		}
    	
    	return retVal ;
	}

}
