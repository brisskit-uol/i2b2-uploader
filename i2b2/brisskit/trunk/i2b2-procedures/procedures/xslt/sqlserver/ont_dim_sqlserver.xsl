<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns="http://briccs.org.uk/xml/onyxmetadata-refined/v1.0/omr" 
                xmlns:omr="http://briccs.org.uk/xml/onyxmetadata-refined/v1.0/omr" >
                
	<!--+
	    | Style sheet that derives SQL inserts for the Ontology dimension table of the CRC Cell from 
	    | the RefinedMetadata.xml file produced by the program MetadataRefiner.
	    |
	    | This is the SQLSERVER version of this style sheet.
	    |
	    | Only variables are blessed with inserts into the dimension table.
	    |
	    | Author Jeff Lusted.
	    | email: jl99@leicester.ac.uk
	    +-->

	<xsl:output method="text" indent="no" />
	<xsl:strip-space elements="*"/>
	
	<xsl:param name="adminDate"/>
	<xsl:param name="sourceSystem" select="BRICCS" />

	<!-- the root template -->
	<xsl:template match="/">
		<xsl:apply-templates select="*" />
	</xsl:template>

	<!-- the 'container' element -->
	<xsl:template match="omr:container">
		<xsl:apply-templates select="omr:folder" />
	</xsl:template>

	<!-- the "folder" element -->
	<xsl:template match="omr:folder">
		<xsl:choose>
			<!--+
			    | We ignore QuestionnaireRun and QuestionnaireMetric 
			    +-->
			<xsl:when test="@name = 'QuestionnaireRun'" />
			<xsl:when test="@name = 'QuestionnaireMetric'" />
			
			<!--+
			    | This next section ignores everything within the Participant-Entity
			    | folder except for the Participant folder itself.
			    | (For Brisskit, Participants folder)
			    +-->			
			<xsl:when test="@name = 'Participant-Entity'">
				<xsl:apply-templates select="omr:folder" />
			</xsl:when>
			<xsl:when test="@name = 'Participants'">
				<xsl:apply-templates select="omr:folder" />
			</xsl:when>
			<xsl:when test="@name = 'Interview'" />
			<xsl:when test="@name = 'ApplicationConfiguration'" />
			<xsl:when test="@name = 'Action'" />
			<xsl:when test="@name = 'StageInstance'" />			
			<!--+
			    | For the Participant-Entity/Admin folder, we only
			    | test for child folders (one of which is Participant!)...
			    | (For Brisskit, this is the Participants/Admin folder)
			    +-->
			<xsl:when test="@name = 'Admin'">
				<xsl:apply-templates select="omr:folder" />
			</xsl:when>			
			<!--+
			    | For the Participant, we're currently only interested in variables within sub-folders...
			    +-->
			<xsl:when test="@name = 'Participant'">
				<xsl:apply-templates select="omr:folder" />
			</xsl:when>
			
			<!--+
			    | This next section deals with DataSubmissionQuestionnaire.
			    | 
			    | other primary diagnoses, other secondary diagnoses and other symptoms
			    | are textual note fields which get folded into the associated "other"
			    | observation_fact.
			    +-->			
			<xsl:when test="@name = 'epi_symptomother_cat'" />
			<xsl:when test="@name = 'epi_pridiagother_cat'" />
			<xsl:when test="@name = 'epi_secdiagother_cat'" />
			<!--+
			    | Symptoms onset are ignored here and used instead as start date
			    | for the associated observation_fact...
			    +-->
			<xsl:when test="@name = 'epi_symponset'" />			
			<xsl:when test="@name = 'epi_symponset_cat'" />
			<xsl:when test="@name = 'epi_symponset_table'" />
			<xsl:when test="@name = 'epi_symponset_time_cat'" />
				
			<!--+
			    | This next section deals with EndContactQuestionnaire.
			    | 
			    | We don't want participant email
			    | at the moment...
			    +-->			
			<xsl:when test="@name = 'pat_email1_cat'" />
			<xsl:when test="@name = 'pat_email2_cat'" />	
						
			<!--+
			    | We process all other folders as is...
			    +-->
			<xsl:otherwise>
				<xsl:apply-templates select="omr:folder" />
				<xsl:apply-templates select="omr:variable" />
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
	
	<!-- the "variable" element -->
	<xsl:template match="omr:variable">
		<xsl:choose>
		    <!--+
		        | All of the following types generate enumerations 
		        | and are covered by separate files.
		        | They get ignored here...
		        +-->
			<xsl:when test="@type = 'AGE'"/>
			<xsl:when test="@type = 'YEAR'"/>
			<xsl:when test="@type = 'SHORTYEAR'"/>
			<xsl:when test="@type = 'RECENTTIME'"/>
			<xsl:when test="@type = 'HEARTRATE'"/>
			<xsl:when test="@type = 'SYSTOLICBP'"/>
			<xsl:when test="@type = 'DIASTOLICBP'"/>
			<xsl:when test="@type = 'HEIGHT'"/>
			<xsl:when test="@type = 'WEIGHT'"/>
			<xsl:when test="@type = 'WAIST'"/>
			<xsl:when test="@type = 'HIPS'"/>
			<xsl:when test="@type = 'BICEPS'"/>
			<xsl:when test="@type = 'TRICEPS'"/>
			<xsl:when test="@type = 'SUBSCAPULAR'"/>
			<xsl:when test="@type = 'SUPRAILIAC'"/>
			<xsl:when test="@type = 'SMALLNUMBER'"/>			
			<xsl:when test="@type = 'CIGARETTENUMBER'"/>
			<xsl:when test="@type = 'CIGARNUMBER'"/>
			<xsl:when test="@type = 'PIPENUMBER'"/>
			<xsl:when test="@type = 'BEERNUMBER'"/>
			<xsl:when test="@type = 'WINESPIRITNUMBER'"/>
			<xsl:when test="@type = 'RELATIVESNUMBER'"/>
			<!-- VITALSTATUS added by trac issue 94 -->
			<xsl:when test="@type = 'VITALSTATUS'"/>

			<!--+
		        | The following types are Onyx types 
		        | which we ignore for the moment...
		        +-->
			<xsl:when test="@type = 'DATA'"/>
			<xsl:when test="@type = 'LOCALE'"/>
			<xsl:when test="@type = 'BINARY'"/>
			
			<!--+
		        | Date-times for DataSubmissionQuestionnaire / Date_Table
		        | are accepted as booleans. The datetime here will be used
		        | as the observation_fact start date.
		        +-->
		    <xsl:when test="@type = 'DATETIME'">
		    	<xsl:choose>
		    		<xsl:when test="ancestor::*[@name = 'DataSubmissionQuestionnaire'] and ancestor::*[@name = 'Date_table']">
		    			<xsl:call-template name="insertTop" />
						<xsl:call-template name="insertValues" />
						<xsl:call-template name="insertTail" />
		    		</xsl:when>
		    	</xsl:choose>
		    </xsl:when>
			
			<!--+
		        | The following types are unwanted variables 
		        | for the moment...
		        +-->
			<xsl:when test="@name = 'tubeCode'"/>
			<xsl:when test="@name = 'barcode'"/>
			<xsl:when test="@name = 'prefixCode'"/>
			
			<!--+
			    | Process all other variables...
			    +-->	
			<xsl:otherwise>
				<xsl:call-template name="insertTop" />
				<xsl:call-template name="insertValues" />
				<xsl:call-template name="insertTail" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
<!--+=================================================================================-->
<!--|   Utility templates                                                            |-->
<!--+=================================================================================-->
	
	<!--+
	    | Puts out the top of a basic SQL insert command and columns
	    +-->
	<xsl:template name="insertTop" >
	   <xsl:text>INSERT INTO CONCEPT_DIMENSION( concept_path, concept_cd, name_char, update_date, download_date, import_date, sourcesystem_cd, UPLOAD_ID )</xsl:text>
	   <xsl:text>&#xA;</xsl:text>
       <xsl:text>VALUES( </xsl:text>
	</xsl:template>
	
	<!--+
	    | Puts out the tail of the SQL insert command 
	    +-->
	<xsl:template name="insertTail" >
	   <xsl:text> ) ;</xsl:text>
	   <xsl:text>&#xA;&#xA;</xsl:text>
	</xsl:template>
	
	<!--+
	    | Puts out the values of the SQL insert command 
	    +-->
	<xsl:template name="insertValues" >
		<!-- concept_path, -->
		<xsl:call-template name="getPath" /><xsl:text>, </xsl:text>
		<!-- concept_cd -->
		<xsl:call-template name="getBaseCode" /><xsl:text>, </xsl:text>
		<!-- name_char -->
		<xsl:call-template name="getDescriptiveName" /><xsl:text>, </xsl:text>
		<!-- Here is the administrative group -->
		<xsl:call-template name="insertAdminGroup" />
	</xsl:template>
	
	<!--+
	    | Puts out the full name for each node.
	    | The Participant-Entity folder is omitted.
	    | (For Brisskit, the Participants folder)...
	    +-->
	<xsl:template name="getPath">
		<xsl:text>'\</xsl:text>
		<xsl:for-each select="ancestor::*">	
			<xsl:choose>
				<!-- Participant-Entity and Admin are not included -->
				<xsl:when test="./@name = 'Participant-Entity'"/>
				<xsl:when test="./@name = 'Participants'"/>
				<xsl:when test="./@name = 'Admin'"/>
				<xsl:otherwise>
					<xsl:value-of select="@name" />
					<xsl:text>\</xsl:text>
				</xsl:otherwise>
			</xsl:choose>						
		</xsl:for-each>
		<xsl:value-of select="@name" />
		<xsl:text>\'</xsl:text>
	</xsl:template>
	
	<!--+
	    | Outputs the base code for a node.
	    +-->
	<xsl:template name="getBaseCode">
		<xsl:choose>
			<xsl:when test="string-length( @code ) > 0">
				<xsl:text>'</xsl:text>
				<xsl:value-of select="@code"/>
				<xsl:text>'</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>NULL</xsl:text>
			</xsl:otherwise>
		</xsl:choose>		
	</xsl:template>
	
	<!--+
	    | Uses description, if there is one.
	    +-->
	<xsl:template name="getDescriptiveName">
		<xsl:choose>
			<xsl:when test="@description">
				<xsl:text>'</xsl:text>
				<xsl:value-of select="translate( @description, &quot;'&quot;, ''  )" />
				<xsl:text>'</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>NULL</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--+
	    | Processing for the administrative group...
	    +-->
	<xsl:template name="insertAdminGroup" >

	   	<!-- UPDATE_DATE -->
	   	<xsl:call-template name="insertTimeStamp">
			<xsl:with-param name="value" select="$adminDate" />
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		
		<!-- DOWNLOAD_DATE -->
		<!-- xsl:call-template name="insertNullableTimeStamp">
			<xsl:with-param name="value" select="$adminDate" />
		</xsl:call-template -->
		<xsl:text>NULL, </xsl:text>
		
		<!-- IMPORT_DATE -->
		<!-- xsl:call-template name="insertNullableTimeStamp">
			<xsl:with-param name="value" select="$adminDate" />
		</xsl:call-template -->
		<xsl:text>NULL, </xsl:text>
		
		<!-- SOURCESYSTEM_CD -->
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="$sourceSystem" />
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		
		<!-- UPLOAD ID -->
		<xsl:text>NULL</xsl:text>

	</xsl:template>
	
	<!--+
	    | Template for nullable string
	    +-->
	<xsl:template name="insertNullableString" >
		<xsl:param name="value"/>
	   	<xsl:choose>
			<xsl:when test="$value">
				<xsl:call-template name="insertString">
					<xsl:with-param name="value" select="$value" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>NULL</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
				
	<!--+
	    | Template for a string value 
	    +-->
	<xsl:template name="insertString" >
		<xsl:param name="value"/>
	   	<xsl:text>'</xsl:text>
		<xsl:value-of select="$value" />
		<xsl:text>'</xsl:text>
	</xsl:template>
	
	<!--+
	    | Template for formatting nullable timestamp 
	    +-->
	<xsl:template name="insertNullableTimeStamp" >
		<xsl:param name="value"/>
		<xsl:choose>
			<xsl:when test="$value">
				<xsl:call-template name="insertTimeStamp">
					<xsl:with-param name="value" select="$value" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>NULL</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
			
	<!--+
	    | Template for formatting timestamp    
	    +-->
	<xsl:template name="insertTimeStamp" >
		<xsl:param name="value"/>		
		<text>CAST( '&lt;datetime&gt;</text>
		<xsl:value-of select="$value"/>
		<text>&lt;/datetime&gt;' as xml ).value('xs:dateTime(.[1])', 'datetime')</text>		
	</xsl:template>
		
	<xsl:template match="text()"/>
	
</xsl:stylesheet>

