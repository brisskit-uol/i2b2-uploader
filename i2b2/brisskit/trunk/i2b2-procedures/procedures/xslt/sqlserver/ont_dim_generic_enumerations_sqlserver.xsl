<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns="http://briccs.org.uk/xml/onyxmetadata-rev/v1.0/rev" 
                xmlns:rev="http://briccs.org.uk/xml/onyxmetadata-rev/v1.0/rev" >
                
	<!--+
	    | Style sheet that derives SQL inserts for the Ontology Dimension from xml files for enumerated variable.
	    |
	    | This is the SQLSERVER version of this style sheet.
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

	<!-- the 'enumeratedVariable' element -->
	<xsl:template match="rev:enumeratedVariable">
		<xsl:apply-templates select="rev:group" />
		<xsl:apply-templates select="rev:variable" />
	</xsl:template>

	<!-- the "group" element -->
	<xsl:template match="rev:group">
		<xsl:apply-templates select="rev:group" />
		<xsl:apply-templates select="rev:variable" />
	</xsl:template>
	
	<!-- the "variable" element -->
	<xsl:template match="rev:variable">
		<xsl:call-template name="insertTop" />
		<xsl:call-template name="insertValues" />
		<xsl:call-template name="insertTail" />
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
	    +-->
	<xsl:template name="getPath">
		<xsl:text>'</xsl:text>
		<xsl:for-each select="ancestor::*">	
			<xsl:choose>
				<xsl:when test="self::rev:enumeratedVariable">
					<xsl:value-of select="@path" />
					<xsl:text>\</xsl:text>
				</xsl:when>
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
	    | Puts out a descriptive name.
	    | Preference is given to the description, if there is one, 
	    | Otherwise it is the element's name or - if no name - then the textual content.
	    +-->
	<xsl:template name="getDescriptiveName">
		<xsl:text>'</xsl:text>
		<xsl:choose>
			<xsl:when test="@description">
				<xsl:value-of select="translate( @description, &quot;'&quot;, ''  )" />
			</xsl:when>
			<xsl:when test="@name">			
				<xsl:value-of select="@name" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="." />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>'</xsl:text>
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
		
		<!-- UPLOAD_ID -->
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

