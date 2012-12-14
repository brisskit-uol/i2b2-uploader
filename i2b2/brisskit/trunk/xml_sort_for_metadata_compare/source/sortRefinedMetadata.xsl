<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns='http://briccs.org.uk/xml/onyxmetadata-refined/v1.0/omr'
		xmlns:omr="http://briccs.org.uk/xml/onyxmetadata-refined/v1.0/omr">

	<!--+===============================================================================
	    | Designed by Will Lusted - will_lusted@hotmail.co.uk - in May 2012. 
	    | Last modified 02/05/12. 
	    |
	    | The stylesheet orders elements in a refined metadata file (either nominal or real) 
	    | alphabetically, therefore making a semantic comparison of two refined ontology
	    | files possible.  
	    +===============================================================================-->

	<xsl:output indent="yes" />
	<xsl:strip-space elements="*" />

<xsl:template match="/">
	<xsl:apply-templates select="*" />
</xsl:template>


<xsl:template match="omr:container">
	<container xmlns:omr="http://briccs.org.uk/xml/onyxmetadata-refined/v1.0/omr" name="onyx">
		<xsl:for-each select="omr:folder">
			<xsl:sort select="@name" />
			<xsl:apply-templates select="." />	
		</xsl:for-each>
	</container>
</xsl:template>

<xsl:template match="omr:folder">
	<folder>
	<xsl:attribute name="name" select="@name"/>
	<xsl:if test="@description">
		<xsl:attribute name="description" select="@description" />
	</xsl:if>
	<xsl:for-each select="omr:folder">
		<xsl:sort select="@name" />
		<xsl:apply-templates select="." />	
	</xsl:for-each>
	<xsl:for-each select="omr:variable">
		<xsl:sort select="@name" />
		<xsl:apply-templates select="." />	
	</xsl:for-each>
	</folder>
</xsl:template>

<xsl:template match="omr:variable">
	<variable>
	<xsl:attribute name="name" select="@name" />
	<xsl:attribute name="type" select="@type" />
	<xsl:attribute name="code" select="@code" />
	<xsl:attribute name="description" select="@description" />
	</variable>
</xsl:template>

</xsl:stylesheet>