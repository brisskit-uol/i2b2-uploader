<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns='http://briccs.org.uk/xml/onyxvariables/v1.0/ov'
		xmlns:ov='http://briccs.org.uk/xml/onyxvariables/v1.0/ov'>

	<!--+===============================================================================
	    | Designed by Will Lusted - will_lusted@hotmail.co.uk - in December 2011. 
	    | Last modified 02/05/12. 
	    |
	    | The stylesheet orders elements in the initial metadata from an Onyx questionnaire
	    | alphabetically, therefore making a semantic comparison of two metadata files 
	    | possible.  
	    +===============================================================================-->

	<xsl:output indent="yes" />
	<xsl:strip-space elements="*" />

<xsl:template match="/">
	<xsl:apply-templates select="*" />
</xsl:template>

<xsl:template match="ov:variables">
	<variables xmlns='http://briccs.org.uk/xml/onyxvariables/v1.0/ov' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
	<xsl:for-each select="ov:variable">
		<xsl:sort select="@name" />
		<xsl:apply-templates select="." />	
	</xsl:for-each>
	</variables>
</xsl:template>

<xsl:template match="ov:variable">
	<variable>
	<xsl:attribute name="name" select="@name" />
	<xsl:attribute name="valueType" select="@valueType" />
	<xsl:attribute name="entityType" select="@entityType" />
	<xsl:if test="@repeatable">
		<xsl:attribute name="repeatable" select="@repeatable" />
	</xsl:if>
	<xsl:if test="@occurrenceGroup">
		<xsl:attribute name="occurrenceGroup" select="@occurrenceGroup" />
	</xsl:if>
	<xsl:apply-templates select="./ov:categories" />	
	<xsl:apply-templates select="./ov:attributes" />	
	</variable>
</xsl:template>

<xsl:template match="ov:categories">
	<categories>
	<xsl:for-each select="ov:category">
		<xsl:sort select="@name" />
		<xsl:copy-of select="." />	
	</xsl:for-each>
	</categories>
</xsl:template>

<xsl:template match="ov:attributes">
	<attributes>
	<xsl:for-each select="ov:attribute">
		<xsl:sort select="@name" />
		<xsl:copy-of select="." />	
	</xsl:for-each>
	</attributes>
</xsl:template>

</xsl:stylesheet>