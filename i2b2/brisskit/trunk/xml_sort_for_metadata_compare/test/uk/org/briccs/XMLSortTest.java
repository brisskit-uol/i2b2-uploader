package uk.org.briccs ;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;

public class XMLSortTest extends XMLTestCase {
	
	private static String onyxInputPath = null ;
	private static String onyxOutputPath = null ;
	private static String onyxExpectedPath = null ;
	private static String onyxStylesheetPath = null ;
	private static String refinedInputPath = null ;
	private static String refinedOutputPath = null ;
	private static String refinedExpectedPath = null ;
	private static String refinedStylesheetPath = null ;
	private static TransformerFactory transformerFactory = TransformerFactory.newInstance( "net.sf.saxon.TransformerFactoryImpl",null ) ;
    private static Transformer onyxTransformer ;
    private static Transformer refinedTransformer ;
	private static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	private static DocumentBuilder dBuilder ;
	
	public XMLSortTest() {}

	@Override
	protected void setUp() throws Exception {
		System.out.println( "We're here in setUp" ) ;
		super.setUp();
		if( onyxInputPath == null ) {
			readProperties();
			deleteOutputFiles() ;
			initTransformers() ;
		}	
		
		if( dBuilder == null ) {
			dBuilder = dbFactory.newDocumentBuilder();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		System.out.println( "We're here in tearDown" ) ;
		super.tearDown();
	}
	
	public void testBloodSamplesCollection() throws Exception {
		doOnyxTest("variables_bloodSamplesCollection.xml") ;
	}
	
	public void testConclusionsQuestionnaire() throws Exception {
		doOnyxTest("variables_conclusionsQuestionnaire.xml") ;
	}
	
	public void testConsent() throws Exception {
		doOnyxTest("variables_consent.xml") ;
	}
	
	public void testDataSubmissionQuestionnaire() throws Exception {
		doOnyxTest("variables_dataSubmissionQuestionnaire.xml") ;
	}
	
	public void testEndContractQuestionnaire() throws Exception {
		doOnyxTest("variables_endContractQuestionnaire.xml") ;
	}
	
	public void testManualConsentQuestionnaire() throws Exception {
		doOnyxTest("variables_manualConsentQuestionnaire.xml") ;
	}
	
	public void testMedicalHistoryInterviewQuestionnaire() throws Exception {
		doOnyxTest("variables_medicalHistoryInterviewQuestionnaire.xml") ;
	}
	
	public void testMedicalHistoryQuestionnaire() throws Exception {
		doOnyxTest("variables_medicalHistoryQuestionnaire.xml") ;
	}
	
	public void testParticipants() throws Exception {
		doOnyxTest("variables_participants.xml") ; 	
	}
	
	public void testRecruitmentContextQuestionnaire() throws Exception {
		doOnyxTest("variables_recruitmentContextQuestionnaire.xml") ; 	
	}
	
	public void testRiskFactorQuestionnaire() throws Exception {
		doOnyxTest("variables_riskFactorQuestionnaire.xml") ; 	
	}
	
	public void testSamplePreliminaryQuestionnaire() throws Exception {
		doOnyxTest("variables_samplePreliminaryQuestionnaire.xml") ; 	
	}
	
	public void testUrineSamplesCollection() throws Exception {
		doOnyxTest("variables_urineSamplesCollection.xml") ; 	
	}
	
	public void testVerbalConsentQuestionnaire() throws Exception {
		doOnyxTest("variables_verbalConsentQuestionnaire.xml") ; 	
	}
	
	public void testRefinedMetadata1() throws Exception {
		doRefinedTest("nominal-refined-metadata1.xml") ; 	
	}
	
	public void testRealRefinedMetadata1() throws Exception {
		doRefinedTest("real-refined-metadata1.xml") ; 	
	}
	
	private void readProperties() throws IOException {
        Properties props = new Properties();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream( "config.properties" ) ;
        props.load( inputStream ) ;
        //
        // get paths to input and output files from the config file...
        onyxInputPath = (String)props.get( "onyxInputDirectory" ) ;
        System.out.println( onyxInputPath ) ;
        onyxOutputPath = (String)props.get( "onyxOutputDirectory" ) ;
        System.out.println( onyxOutputPath ) ;
        onyxStylesheetPath = (String)props.get( "onyxStylesheet" ) ;
        System.out.println( onyxStylesheetPath ) ;
        onyxExpectedPath = (String)props.get( "onyxExpectedDirectory" ) ;
        System.out.println( onyxExpectedPath ) ;        
        refinedInputPath = (String)props.get( "refinedInputDirectory" ) ;
        System.out.println( refinedInputPath ) ;
        refinedOutputPath = (String)props.get( "refinedOutputDirectory" ) ;
        System.out.println( refinedOutputPath ) ;
        refinedStylesheetPath = (String)props.get( "refinedStylesheet" ) ;
        System.out.println( refinedStylesheetPath ) ;
        refinedExpectedPath = (String)props.get( "refinedExpectedDirectory" ) ;
        System.out.println( refinedExpectedPath ) ;
    }
	
	private void deleteOutputFiles() {
	    //
        // deleting all files in the output directory
		File dir = new File( onyxOutputPath ) ;
        File[] files = dir.listFiles() ;
        for (File file : files) {
			file.delete() ;
		}
        dir = new File( refinedOutputPath ) ;
        files = dir.listFiles() ;
        for (File file : files) {
			file.delete() ;
		}
	}
	
	public void initTransformers()	throws TransformerConfigurationException {
		StreamSource stylesheetSource = new StreamSource( new File( onyxStylesheetPath ) );
		XMLSortTest.onyxTransformer = XMLSortTest.transformerFactory.newTransformer( stylesheetSource ) ;
		System.out.println( "Initialized Onyx transformer" ) ;		 
		stylesheetSource = new StreamSource( new File( refinedStylesheetPath ) );
		XMLSortTest.refinedTransformer = XMLSortTest.transformerFactory.newTransformer( stylesheetSource ) ;
		System.out.println( "Initialized Refined transformer" ) ;
	}
	
	
	private void doOnyxTest(String fileName) throws Exception {
		File inputFile = new File( onyxInputPath + File.separator + fileName) ;
		File outputFile = new File( onyxOutputPath + File.separator + fileName) ;				
		File expectedFile = new File( onyxExpectedPath + File.separator + fileName) ;
		StreamSource source = new StreamSource( inputFile ) ;
		StreamResult result = new StreamResult( outputFile ) ;  
		onyxTransformer.transform( source, result ) ; 	
		source = null ;
		result = null ;
		
		Document outputDoc = dBuilder.parse( outputFile );
		outputDoc.normalize();
		Document expectedDoc = dBuilder.parse( expectedFile );        
        expectedDoc.normalize();
        
        // Using xmlunit to compare documents
        assertXMLEqual("Output does not parse to what is expected!", outputDoc, expectedDoc) ;
        expectedDoc = null ;
        
        Document inputDoc = dBuilder.parse( inputFile );
        inputDoc.normalize();
        
        // Using xmlunit to compare documents
        assertXMLNotEqual("Input and Output files compare equal!", inputDoc, outputDoc) ;
	}
	
	
	private void doRefinedTest(String fileName) throws Exception {
		File inputFile = new File( refinedInputPath + File.separator + fileName) ;
		File outputFile = new File( refinedOutputPath + File.separator + fileName) ;				
		File expectedFile = new File( refinedExpectedPath + File.separator + fileName) ;
		StreamSource source = new StreamSource( inputFile ) ;
		StreamResult result = new StreamResult( outputFile ) ;  
		refinedTransformer.transform( source, result ) ; 	
		source = null ;
		result = null ;
		
		Document outputDoc = dBuilder.parse( outputFile );
		outputDoc.normalize();
		Document expectedDoc = dBuilder.parse( expectedFile );        
        expectedDoc.normalize();
        
        // Using xmlunit to compare documents
        assertXMLEqual("Output does not parse to what is expected!", outputDoc, expectedDoc) ;
        expectedDoc = null ;
        
        Document inputDoc = dBuilder.parse( inputFile );
        inputDoc.normalize();
        
        // Using xmlunit to compare documents
        assertXMLNotEqual("Input and Output files compare equal!", inputDoc, outputDoc) ;
	}
	
}
