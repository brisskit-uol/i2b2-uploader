<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns="http://www.i2b2.org/xsd/hive/pdo/1.1/pdo" 
                xmlns:pdo="http://www.i2b2.org/xsd/hive/pdo/1.1/pdo" >
                
	<!--+
	    | Style sheet that derives SQL inserts for the CRC Cell from PDO object files.
	    | Covers tables: patient_dimension, patient_mapping, visit_dimension, observation_fact
	    |
	    | This is the SQLSERVER version of the style sheet.
	    | (Essentially, only the method of dealing with a timestamp is different from the Oracle version)
	    |
	    | Author Jeff Lusted.
	    | email: jl99@leicester.ac.uk
	    +-->

	<xsl:output method="text" indent="no" />
	<xsl:strip-space elements="*"/>

	<!-- the root template -->
	<xsl:template match="/">
		<xsl:apply-templates select="*" />
	</xsl:template>
	
	
	<!--+=================================================================================
	    |   Visit Dimension
	    +=================================================================================-->
	<xsl:template match="event">
		<!--+
		    | Here is the top of the SQL insert statement 
		    +-->
		<xsl:text>INSERT INTO VISIT_DIMENSION( </xsl:text> 
	   	<xsl:text>ENCOUNTER_NUM, PATIENT_NUM, </xsl:text>
	   	<xsl:text>ACTIVE_STATUS_CD, INOUT_CD, LOCATION_CD, LOCATION_PATH, </xsl:text>
	   	<xsl:text>START_DATE, END_DATE, </xsl:text> 
	   	<xsl:text>VISIT_BLOB, </xsl:text>
	   	<xsl:text>UPDATE_DATE, DOWNLOAD_DATE, IMPORT_DATE, SOURCESYSTEM_CD, UPLOAD_ID )</xsl:text>
	   	<xsl:text>&#xA;</xsl:text>
       	<xsl:text>VALUES( </xsl:text>
		<!--+
		    | Here are the values
		    +-->
		<!-- ENCOUNTER_NUM -->
		<xsl:value-of select="event_id" />
		<xsl:text>, </xsl:text>
		<!-- PATIENT_NUM -->
		<xsl:value-of select="patient_id" />
		<xsl:text>, </xsl:text>
		
		<!-- optional param types go here -->
		<!-- (Why is ACTIVE_STATUS_CD an optional column? It is not nullable.) -->
		<xsl:apply-templates select="param[ @column = 'ACTIVE_STATUS_CD' ]" />
		<xsl:text>, </xsl:text>
		<xsl:apply-templates select="param[ @column = 'INOUT_CD' ]" />
		<xsl:text>, </xsl:text>
		<xsl:apply-templates select="param[ @column = 'LOCATION_CD' ]" />
		<xsl:text>, </xsl:text>
		<xsl:apply-templates select="param[ @column = 'LOCATION_PATH' ]" />
		<xsl:text>, </xsl:text>
		
		<!-- START_DATE eg: 1982-08-08T00:00:00.000+01:00 -->
		<xsl:call-template name="insertTimeStamp" >
			<xsl:with-param name="value" select="start_date"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		
		<!-- END_DATE eg: 1982-08-08T00:00:00.000+0100 -->
		<xsl:call-template name="insertNullableTimeStamp" >
			<xsl:with-param name="value" select="end_date"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		
		<!-- VISIT_BLOB -->
		<!-- How would we include a VISIT_BLOB when one becomes available? -->
		<xsl:text>NULL</xsl:text>
		<xsl:text>, </xsl:text>
		
		<!-- Here is the administrative group -->
		<xsl:call-template name="insertAdminGroup" />
		
		<!-- And finally add the tail to the SQL insert command -->
		<xsl:call-template name="insertTail" />
	</xsl:template>
	
	
	<!--+=================================================================================
	    |   Concept Dimension
	    +=================================================================================-->
	<xsl:template match="concept">
		<!--+
		    |  Empty at present because the concept dimension is maintained in synch with the ontology cell;
		    |  ie: at ontology build time. I believe the concept set is here for querying, which has an
		    |  intriguing prospect of data being self-defining. Think about it!
		    +-->
	</xsl:template>
	
	
	<!--+=================================================================================
	    |   Provider Dimension
	    +=================================================================================-->
	<xsl:template match="observer">
		<!--+
		    |  Empty at present. I don't know what to do with this!
		    +-->
	</xsl:template>
	
	
	<!--+=================================================================================
	    |   Patient Mapping
	    |   This models a new insert of a participant; 
	    |   ie: we assume this is an upload for someone who is not already in the hive.
	    |   As such there is a self mapping for the hive's patient number.
	    +=================================================================================-->
	<xsl:template match="pid">
		<!-- Save the hive's patient id. We need it for subsequent mappings -->
		<xsl:variable name="patient_id" select="./patient_id"/>
		
		<!--+.............................................................................
		    | Do a self (hive) mapping first 
		    +.............................................................................-->
		
		<xsl:text>INSERT INTO PATIENT_MAPPING( </xsl:text>
	   	<xsl:text>PATIENT_IDE, PATIENT_IDE_SOURCE, PATIENT_NUM, PATIENT_IDE_STATUS, </xsl:text> 
	   	<xsl:text>UPLOAD_DATE, UPDATE_DATE, DOWNLOAD_DATE, IMPORT_DATE, SOURCESYSTEM_CD, UPLOAD_ID )</xsl:text>
	   	<xsl:text>&#xA;</xsl:text>
       	<xsl:text>VALUES( </xsl:text>
       	
       	<!-- PATIENT_IDE, -->
		<xsl:text>'</xsl:text>
		<xsl:value-of select="$patient_id" />
		<xsl:text>', </xsl:text>
		<!-- PATIENT_IDE_SOURCE -->
		<xsl:text>'</xsl:text>
		<xsl:value-of select="./patient_id/@source" />
		<xsl:text>', </xsl:text>
		<!-- PATIENT_NUM -->
		<xsl:value-of select="$patient_id" />
		<xsl:text>, </xsl:text>
		<!-- PATIENT_IDE_STATUS -->
		<xsl:text>'</xsl:text>
		<xsl:value-of select="./patient_id/@status"/>
		<xsl:text>', </xsl:text>
       	
       	<!-- UPLOAD_DATE -->
       	<!-- What is this? Why is it not part of the admin group? -->
       	<xsl:text>NULL</xsl:text>
       	<xsl:text>, </xsl:text>
       	
       	<!-- Here is the administrative group -->
		<xsl:call-template name="insertAdminGroup" />
		
		<!-- And finally add the tail to the SQL insert command -->
		<xsl:call-template name="insertTail" />
		
		<!--+.............................................................................
		    | Do a source mapping for each patient_map_id 
		    +.............................................................................-->
		<xsl:for-each select="./patient_map_id">
			<xsl:text>INSERT INTO PATIENT_MAPPING( </xsl:text>
		   	<xsl:text>PATIENT_IDE, PATIENT_IDE_SOURCE, PATIENT_NUM, PATIENT_IDE_STATUS, </xsl:text> 
		   	<xsl:text>UPLOAD_DATE, UPDATE_DATE, DOWNLOAD_DATE, IMPORT_DATE, SOURCESYSTEM_CD, UPLOAD_ID )</xsl:text>
		   	<xsl:text>&#xA;</xsl:text>
	       	<xsl:text>VALUES( </xsl:text>
	       	
	       	<!-- PATIENT_IDE, -->
			<xsl:text>'</xsl:text>
			<xsl:value-of select="." />
			<xsl:text>', </xsl:text>
			<!-- PATIENT_IDE_SOURCE -->
			<xsl:text>'</xsl:text>
			<xsl:value-of select="./@source" />
			<xsl:text>', </xsl:text>
			<!-- PATIENT_NUM -->
			<xsl:value-of select="$patient_id" />
			<xsl:text>, </xsl:text>
			<!-- PATIENT_IDE_STATUS -->
			<xsl:text>'</xsl:text>
			<xsl:value-of select="./@status"/>
			<xsl:text>', </xsl:text>
	       	
	       	<!-- UPLOAD_DATE -->
	       	<!-- What is this? Why is it not part of the admin group? -->
       		<xsl:text>NULL</xsl:text>
       		<xsl:text>, </xsl:text>
	       	
	       	<!-- Here is the administrative group -->
			<xsl:call-template name="insertAdminGroup" />
			
			<!-- And finally add the tail to the SQL insert command -->
			<xsl:call-template name="insertTail" />

        </xsl:for-each>
	</xsl:template>
	
	
	<!--+=================================================================================
	    |   Visit or Encounter Mapping
	    +=================================================================================-->
	<xsl:template match="eid">
		<!--+
		    |  Empty at present. I don't know what to do with this!
		    +-->
	</xsl:template>
	
	
	<!--+=================================================================================
	    |   Patient Dimension
	    +=================================================================================-->	
	<xsl:template match="patient">
		<!--+
		    | Here is the top of the SQL insert statement 
		    +-->
		<xsl:text>INSERT INTO PATIENT_DIMENSION( </xsl:text>
	   	<xsl:text>PATIENT_NUM, </xsl:text> 
	   	<xsl:text>VITAL_STATUS_CD, </xsl:text>
	   	<xsl:text>BIRTH_DATE, </xsl:text>
	   	<xsl:text>DEATH_DATE, </xsl:text>
	   	<xsl:text>SEX_CD, </xsl:text>
	   	<xsl:text>AGE_IN_YEARS_NUM, </xsl:text>
	   	<xsl:text>RACE_CD, </xsl:text>
	   	<xsl:text>MARITAL_STATUS_CD, </xsl:text>
	   	<xsl:text>RELIGION_CD, </xsl:text>
	   	<xsl:text>ZIP_CD, </xsl:text>
	   	<xsl:text>STATECITYZIP_PATH, </xsl:text>
	   	<xsl:text>PATIENT_BLOB, </xsl:text>
	   	<xsl:text>UPDATE_DATE, DOWNLOAD_DATE, IMPORT_DATE, SOURCESYSTEM_CD, UPLOAD_ID )</xsl:text>
	   	<xsl:text>&#xA;</xsl:text>
       	<xsl:text>VALUES( </xsl:text>
		
		<!--+
		    | Here are the values
		    +-->
		<!-- PATIENT_NUM -->
		<xsl:value-of select="patient_id" />
		<xsl:text>, </xsl:text>
		<!-- VITAL_STATUS_CD -->
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="param[ @column = 'vital_status_cd' ]" />
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!--  BIRTH_DATE -->
        <xsl:call-template name="insertNullableTimeStamp">
			<xsl:with-param name="value" select="param[ @column = 'birth_date' ]" />
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!-- DEATH_DATE -->
		<!-- Can this be anything other than null from Onyx? -->
       	<xsl:text>NULL</xsl:text>
       	<xsl:text>, </xsl:text>
       	<!-- SEX_CD -->
       	<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="param[ @column = 'sex_cd' ]" />
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!--  AGE_IN_YEARS_NUM -->
		<xsl:call-template name="insertNullableNumeric">
			<xsl:with-param name="value" select="param[ @column = 'age_in_years_num' ]" />
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!-- RACE_CD -->
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="param[ @column = 'race_cd' ]" />
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		
		<!-- recruitment_cd, enrollment_id and participant_id removed by trac issue 94 -->
		
		<!-- MARITAL_STATUS_CD -->
		<xsl:text>NULL</xsl:text>
		<xsl:text>, </xsl:text>
		<!-- RELIGION_CD -->
		<xsl:text>NULL</xsl:text>
		<xsl:text>, </xsl:text>
		<!-- ZIP_CD -->
		<xsl:text>NULL</xsl:text>
		<xsl:text>, </xsl:text>
		<!-- STATECITYZIP_PATH -->
		<xsl:text>NULL</xsl:text>
		<xsl:text>, </xsl:text>
		
		<!-- patient_blob -->
		<!-- How would we include a patient_blob when one becomes available? -->
		<xsl:text>NULL</xsl:text>
		<xsl:text>, </xsl:text>
		
		<!-- Here is the administrative group -->
		<xsl:call-template name="insertAdminGroup" />
		
		<!-- And finally add the tail to the SQL insert command -->
		<xsl:call-template name="insertTail" />
	
	</xsl:template>
	
	
	<!--+=================================================================================
	    |   Observations
	    +=================================================================================-->
	<xsl:template match="observation">
		<!--+
		    | Here is the top of the SQL insert statement 
		    +-->
		<xsl:text>INSERT INTO OBSERVATION_FACT( </xsl:text>
	   	<xsl:text>ENCOUNTER_NUM, PATIENT_NUM, CONCEPT_CD, PROVIDER_ID, START_DATE, </xsl:text>
	   	<xsl:text>MODIFIER_CD, VALTYPE_CD, TVAL_CHAR, NVAL_NUM, VALUEFLAG_CD, QUANTITY_NUM, UNITS_CD, </xsl:text>
	   	<xsl:text>END_DATE, LOCATION_CD, OBSERVATION_BLOB, CONFIDENCE_NUM, </xsl:text>
	   	<xsl:text>UPDATE_DATE, DOWNLOAD_DATE, IMPORT_DATE, SOURCESYSTEM_CD, UPLOAD_ID )</xsl:text>
	   	<xsl:text>&#xA;</xsl:text>
       	<xsl:text>VALUES( </xsl:text>
	
		<!--+
		    | Here are the values
		    +-->
		<!-- ENCOUNTER_NUM -->
		<xsl:value-of select="event_id" />
		<xsl:text>, </xsl:text>
		<!-- PATIENT_NUM -->
		<xsl:value-of select="patient_id" />
		<xsl:text>, </xsl:text>
		<!-- CONCEPT_CD -->
		<xsl:call-template name="insertString" >
			<xsl:with-param name="value" select="concept_cd"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!-- PROVIDER_ID -->
		<xsl:call-template name="insertString" >
			<xsl:with-param name="value" select="observer_cd"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!-- START_DATE -->
		<xsl:call-template name="insertTimeStamp" >
			<xsl:with-param name="value" select="start_date"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!-- MODIFIER_CD -->
		<xsl:call-template name="insertString" >
			<xsl:with-param name="value" select="modifier_cd"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!-- VALTYPE_CD -->
		<xsl:call-template name="insertString" >
			<xsl:with-param name="value" select="valuetype_cd"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!-- TVAL_CHAR -->
		<xsl:call-template name="insertNullableString" >
			<xsl:with-param name="value" select="tval_char"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!-- NVAL_NUM -->
		<xsl:call-template name="insertNullableNumeric" >
			<xsl:with-param name="value" select="nval_num"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!-- VALUEFLAG_CD -->
		<xsl:call-template name="insertNullableString" >
			<xsl:with-param name="value" select="valueflag_cd"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>		
		<!-- QUANTITY_NUM -->
		<xsl:call-template name="insertNullableNumeric" >
			<xsl:with-param name="value" select="quantity_num"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>		
		<!-- UNITS_CD -->
		<xsl:call-template name="insertString" >
			<xsl:with-param name="value" select="units_cd"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>		
		<!-- END_DATE eg: 1982-08-08T00:00:00.000+0100-->
		<xsl:call-template name="insertNullableTimeStamp" >
			<xsl:with-param name="value" select="end_date"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!-- LOCATION_CD -->
		<xsl:call-template name="insertString" >
			<xsl:with-param name="value" select="location_cd"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		<!-- OBSERVATION_BLOB -->
		<xsl:call-template name="insertNullableString" >
			<xsl:with-param name="value" select="observation_blob"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>		
		<!-- CONFIDENCE_NUM: NULL for the moment -->
		<xsl:call-template name="insertNullableString" >
			<xsl:with-param name="value" select="confidence_num"/>
		</xsl:call-template>
		<xsl:text>, </xsl:text>		
				
		<!-- Here is the administrative group -->
		<xsl:call-template name="insertAdminGroup" />
		
		<!-- And finally add the tail to the SQL insert command -->
		<xsl:call-template name="insertTail" />
	</xsl:template>
	
	
	<!--+=================================================================================
	    |   Utility templates
	    +=================================================================================-->
		
	<!--+
	    | Puts out the tail of the SQL insert command 
	    +-->
	<xsl:template name="insertTail" >
	   <xsl:text> ) ;</xsl:text>
	   <xsl:text>&#xA;&#xA;</xsl:text>
	</xsl:template>
	
	<!--+
	    | Section for putting out optional parameter columns
	    +-->
	<xsl:template match="param[ @column = 'ACTIVE_STATUS_CD' ]">
		<xsl:call-template name="insertString">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="param[ @column = 'INOUT_CD' ]">
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="param[ @column = 'LOCATION_CD' ]">
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="param[ @column = 'LOCATION_PATH' ]">
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="param[ @column = 'vital_status_cd' ]">
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="param[ @column = 'birth_date' ]">
		<xsl:call-template name="insertNullableTimeStamp">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>	
	
	<xsl:template match="param[ @column = 'age_in_years_num' ]">
		<xsl:call-template name="insertNullableNumeric">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>	
	
	<xsl:template match="param[ @column = 'race_cd' ]">
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="param[ @column = 'sex_cd' ]">
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="param[ @column = 'recruitment_cd' ]">
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="param[ @column = 'enrollment_id' ]">
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="param[ @column = 'participant_id' ]">
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="." />
		</xsl:call-template>
	</xsl:template>
		
	<!--+
	    | Processing for the administrative group...
	    +-->
	<xsl:template name="insertAdminGroup" >
	   	<!-- UPDATE_DATE -->
	   	<xsl:call-template name="insertNullableTimeStamp">
			<xsl:with-param name="value" select="@update_date" />
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		
		<!-- DOWNLOAD_DATE -->
		<!-- xsl:call-template name="insertNullableTimeStamp">
			<xsl:with-param name="value" select="@download_date" />
		</xsl:call-template -->
		<xsl:text>NULL, </xsl:text>
		
		<!-- IMPORT_DATE -->
		<!-- xsl:call-template name="insertNullableTimeStamp">
			<xsl:with-param name="value" select="@import_date" />
		</xsl:call-template -->
		<xsl:text>NULL, </xsl:text>
		
		<!-- SOURCESYSTEM_CD -->
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="@sourcesystem_cd" />
		</xsl:call-template>
		<xsl:text>, </xsl:text>
		
		<!-- UPLOAD_ID -->
		<xsl:call-template name="insertNullableString">
			<xsl:with-param name="value" select="@upload_id" />
		</xsl:call-template>

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

	<!--+
	    | Template for nullable numeric
	    +-->
	<xsl:template name="insertNullableNumeric" >
		<xsl:param name="value"/>
	   	<xsl:choose>
			<xsl:when test="$value">
				<xsl:value-of select="$value"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>NULL</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
		
	<xsl:template match="text()"/>
	
</xsl:stylesheet>

