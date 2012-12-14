<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns="http://briccs.org.uk/xml/onyxmetadata-refined/v1.0/omr" 
                xmlns:omr="http://briccs.org.uk/xml/onyxmetadata-refined/v1.0/omr" >
                
	<!--+
	    | Style sheet that derives SQL inserts for the Ontology Cell from the refined version of the ontology xml file.
	    | The latter is produced from the intermediate ontology file by program MetadataRefiner.
	    |
	    | This is the SQLSERVER version of this style sheet.
	    |
	    | An SQL insert is required for every ontological node in the intermediate ontology file.
	    |
	    | Currently the data type of nodes is fixed as T for text.
	    | The i2b2 demo projects are similarly "configured".
	    | I'm hoping this is bland enough to work, but I anticipate changing it to produce a more interesting system
	    | (when I understand more).
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
		<xsl:call-template name="insertTop" />
		<xsl:call-template name="insertValues" />
		<xsl:call-template name="insertTail" />
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
			    +-->			
			<xsl:when test="@name = 'Participant-Entity'">
				<xsl:apply-templates select="omr:folder" />
			</xsl:when>
			<xsl:when test="@name = 'Interview'" />
			<xsl:when test="@name = 'ApplicationConfiguration'" />
			<xsl:when test="@name = 'Action'" />
			<xsl:when test="@name = 'StageInstance'" />			
			<!--+
			    | For the Participant-Entity/Admin folder, we only
			    | test for child folders (one of which is Participant!)...
			    +-->
			<xsl:when test="@name = 'Admin'">
				<xsl:apply-templates select="omr:folder" />
			</xsl:when>			
			<!--+
			    | For the Participant, we only output the folder details and
			    | sub-folders...
			    +-->
			<xsl:when test="@name = 'Participant'">
				<xsl:call-template name="insertTop" />
				<xsl:call-template name="insertValues" />
				<xsl:call-template name="insertTail" />
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
				<xsl:call-template name="insertTop" />
				<xsl:call-template name="insertValues" />
				<xsl:call-template name="insertTail" />
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
	
	<!--+=================================================================================
	    |   Utility templates
	    +=================================================================================-->
	
	<!--+
	    | Puts out the top of a basic SQL insert command and columns
	    +-->
	<xsl:template name="insertTop" >
	   <xsl:text>INSERT INTO CATISSUE( C_HLEVEL, C_FULLNAME, C_NAME, C_SYNONYM_CD, C_VISUALATTRIBUTES, C_TOTALNUM, C_BASECODE, C_METADATAXML, C_FACTTABLECOLUMN, C_TABLENAME, C_COLUMNNAME, C_COLUMNDATATYPE, C_OPERATOR, C_DIMCODE, C_COMMENT, C_TOOLTIP, UPDATE_DATE, DOWNLOAD_DATE, IMPORT_DATE, SOURCESYSTEM_CD, VALUETYPE_CD )</xsl:text>
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
	   	<!-- C_HLEVEL, -->
		<xsl:call-template name="getHLevel" /><xsl:text>, </xsl:text>
		<!-- C_FULLNAME, -->
		<xsl:call-template name="getPath" /><xsl:text>, </xsl:text>
		<!-- C_NAME -->
		<xsl:call-template name="getName" /><xsl:text>, </xsl:text>
		<!-- C_SYNONYM_CD -->
		<xsl:text>'N', </xsl:text>
		<!-- C_VISUALATTRIBUTES -->
		<xsl:call-template name="getVisualAttributes" /><xsl:text>, </xsl:text>
		<!-- C_TOTALNUM -->
		<xsl:text>NULL, </xsl:text>
		<!-- C_BASECODE -->
		<xsl:call-template name="getBaseCode" /><xsl:text>, </xsl:text>
		<!-- C_METADATAXML -->
		<xsl:call-template name="getMetaDataXML" /><xsl:text>, </xsl:text>
		<!-- C_FACTTABLECOLUMN -->
		<xsl:text>'concept_cd', </xsl:text>
		<!-- C_TABLENAME -->
		<xsl:text>'concept_dimension', </xsl:text>
		<!-- C_COLUMNNAME -->
		<xsl:text>'concept_path', </xsl:text>
		<!-- C_COLUMNDATATYPE -->
		<xsl:call-template name="getColType" /><xsl:text>, </xsl:text>
		<!-- C_OPERATOR -->
		<xsl:text>'LIKE', </xsl:text>
		<!-- C_DIMCODE -->
		<xsl:call-template name="getPath" /><xsl:text>, </xsl:text>
		<!-- C_COMMENT -->
		<xsl:text>NULL, </xsl:text>
		<!-- C_TOOLTIP -->
		<xsl:call-template name="getToolTip" /><xsl:text>, </xsl:text>
		<!-- Here is the administrative group -->
		<xsl:call-template name="insertAdminGroup" />
	</xsl:template>
	
	<!--+
	    | Puts out the level number, which is 0 for the root, incrementing by one for each parent element.
	    | Some special processing is needed for Participant (and children), as two of its ancestors are not included.
	    +-->
	<xsl:template name="getHLevel">
		<xsl:choose> 
			<xsl:when test="ancestor-or-self::*[@name = 'Participant']">
				<xsl:value-of select="count(ancestor::*)-2" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="count(ancestor::*)" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--+
	    | Puts out the short name for each node.
	    +-->
	<xsl:template name="getName">
		<xsl:text>'</xsl:text>
		<xsl:value-of select="@name"/>
		<xsl:text>'</xsl:text>
	</xsl:template>
	
	<!--+
	    | Puts out the full name for each node.
	    | The Participant-Entity folder is omitted.
	    +-->
	<xsl:template name="getPath">
		<xsl:text>'\</xsl:text>
		<xsl:for-each select="ancestor::*">	
			<xsl:choose>
				<!-- Participant-Entity and Admin are not included -->
				<xsl:when test="./@name = 'Participant-Entity'"/>
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
	    | Puts out the tool tip.
	    | Preference is given to the description, if there is one, 
	    | Otherwise it is the element's name or - if no name - then the textual content.
	    +-->
	<xsl:template name="getToolTip">
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
	    | Puts out the visual attributes.
	    | Must be:
	    |   container + active for the root element.
	    |   folder + active for a non-leaf element.
	    |   leaf + active for leaf elements.
	    +-->
	<xsl:template name="getVisualAttributes">
		<xsl:text>'</xsl:text>
		<xsl:choose>
			<xsl:when test="count(ancestor::*) = 0"><xsl:text>CA</xsl:text></xsl:when>  
			<xsl:when test="self::omr:folder"><xsl:text>FA</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:text>LA</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>'</xsl:text>
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

        <xsl:template name="getColType"> 		
                <xsl:choose>
			<xsl:when test="@type = 'NUMBER'">
				<xsl:text>'</xsl:text>
				<xsl:text>N</xsl:text>
				<xsl:text>'</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>'</xsl:text>
				<xsl:text>T</xsl:text>
				<xsl:text>'</xsl:text>
			</xsl:otherwise>
		</xsl:choose>		
       </xsl:template>


        <xsl:template name="getMetaDataXML"> 		
                <xsl:choose>
			<xsl:when test="@name = 'At260'">
				<xsl:text>'</xsl:text>
				<xsl:text disable-output-escaping="yes">&lt;?xml version="1.0"?&gt;&lt;ValueMetadata&gt;&lt;Version&gt;3.02&lt;/Version&gt;&lt;CreationDateTime&gt;04/15/2007 01:22:04&lt;/CreationDateTime&gt;&lt;TestID&gt;At260&lt;/TestID&gt;&lt;TestName&gt;At260&lt;/TestName&gt;&lt;DataType&gt;PosFloat&lt;/DataType&gt;&lt;CodeType&gt;GRP&lt;/CodeType&gt;&lt;Loinc&gt;2000-8&lt;/Loinc&gt;&lt;Flagstouse&gt;HL&lt;/Flagstouse&gt;&lt;Oktousevalues&gt;Y&lt;/Oktousevalues&gt;&lt;MaxStringLength/&gt;&lt;LowofLowValue/&gt;&lt;HighofLowValue&gt;9&lt;/HighofLowValue&gt;&lt;LowofHighValue&gt;10&lt;/LowofHighValue&gt;&lt;HighofHighValue/&gt;&lt;LowofToxicValue/&gt;&lt;HighofToxicValue/&gt;&lt;EnumValues/&gt;&lt;CommentsDeterminingExclusion&gt;&lt;Com/&gt;&lt;/CommentsDeterminingExclusion&gt;&lt;UnitValues&gt;&lt;NormalUnits&gt;a&lt;/NormalUnits&gt;&lt;NormalUnits&gt;a%&lt;/NormalUnits&gt;&lt;EqualUnits&gt;a&lt;/EqualUnits&gt;&lt;EqualUnits&gt;a%&lt;/EqualUnits&gt;&lt;ExcludingUnits/&gt;&lt;ConvertingUnits&gt;&lt;Units/&gt;&lt;MultiplyingFactor/&gt;&lt;/ConvertingUnits&gt;&lt;/UnitValues&gt;&lt;Analysis&gt;&lt;Enums/&gt;&lt;Counts/&gt;&lt;New/&gt;&lt;/Analysis&gt;&lt;/ValueMetadata&gt;</xsl:text>
				<xsl:text>'</xsl:text>
			</xsl:when>
			<xsl:when test="@name = 'At280'">
				<xsl:text>'</xsl:text>
				<xsl:text disable-output-escaping="yes">&lt;?xml version="1.0"?&gt;&lt;ValueMetadata&gt;&lt;Version&gt;3.02&lt;/Version&gt;&lt;CreationDateTime&gt;04/15/2007 01:22:04&lt;/CreationDateTime&gt;&lt;TestID&gt;At280&lt;/TestID&gt;&lt;TestName&gt;At280&lt;/TestName&gt;&lt;DataType&gt;PosFloat&lt;/DataType&gt;&lt;CodeType&gt;GRP&lt;/CodeType&gt;&lt;Loinc&gt;2000-8&lt;/Loinc&gt;&lt;Flagstouse&gt;HL&lt;/Flagstouse&gt;&lt;Oktousevalues&gt;Y&lt;/Oktousevalues&gt;&lt;MaxStringLength/&gt;&lt;LowofLowValue/&gt;&lt;HighofLowValue&gt;9&lt;/HighofLowValue&gt;&lt;LowofHighValue&gt;10&lt;/LowofHighValue&gt;&lt;HighofHighValue/&gt;&lt;LowofToxicValue/&gt;&lt;HighofToxicValue/&gt;&lt;EnumValues/&gt;&lt;CommentsDeterminingExclusion&gt;&lt;Com/&gt;&lt;/CommentsDeterminingExclusion&gt;&lt;UnitValues&gt;&lt;NormalUnits&gt;a&lt;/NormalUnits&gt;&lt;NormalUnits&gt;a%&lt;/NormalUnits&gt;&lt;EqualUnits&gt;a&lt;/EqualUnits&gt;&lt;EqualUnits&gt;a%&lt;/EqualUnits&gt;&lt;ExcludingUnits/&gt;&lt;ConvertingUnits&gt;&lt;Units/&gt;&lt;MultiplyingFactor/&gt;&lt;/ConvertingUnits&gt;&lt;/UnitValues&gt;&lt;Analysis&gt;&lt;Enums/&gt;&lt;Counts/&gt;&lt;New/&gt;&lt;/Analysis&gt;&lt;/ValueMetadata&gt;</xsl:text>
                                <xsl:text>'</xsl:text>
			</xsl:when>
			<xsl:when test="@name = 'Ratio'">
				<xsl:text>'</xsl:text>
				<xsl:text disable-output-escaping="yes">&lt;?xml version="1.0"?&gt;&lt;ValueMetadata&gt;&lt;Version&gt;3.02&lt;/Version&gt;&lt;CreationDateTime&gt;04/15/2007 01:22:04&lt;/CreationDateTime&gt;&lt;TestID&gt;Ratio&lt;/TestID&gt;&lt;TestName&gt;Ratio&lt;/TestName&gt;&lt;DataType&gt;PosFloat&lt;/DataType&gt;&lt;CodeType&gt;GRP&lt;/CodeType&gt;&lt;Loinc&gt;2000-8&lt;/Loinc&gt;&lt;Flagstouse&gt;HL&lt;/Flagstouse&gt;&lt;Oktousevalues&gt;Y&lt;/Oktousevalues&gt;&lt;MaxStringLength/&gt;&lt;LowofLowValue/&gt;&lt;HighofLowValue&gt;9&lt;/HighofLowValue&gt;&lt;LowofHighValue&gt;10&lt;/LowofHighValue&gt;&lt;HighofHighValue/&gt;&lt;LowofToxicValue/&gt;&lt;HighofToxicValue/&gt;&lt;EnumValues/&gt;&lt;CommentsDeterminingExclusion&gt;&lt;Com/&gt;&lt;/CommentsDeterminingExclusion&gt;&lt;UnitValues&gt;&lt;NormalUnits&gt;dn&lt;/NormalUnits&gt;&lt;NormalUnits&gt;dn%&lt;/NormalUnits&gt;&lt;EqualUnits&gt;dn&lt;/EqualUnits&gt;&lt;EqualUnits&gt;dn%&lt;/EqualUnits&gt;&lt;ExcludingUnits/&gt;&lt;ConvertingUnits&gt;&lt;Units/&gt;&lt;MultiplyingFactor/&gt;&lt;/ConvertingUnits&gt;&lt;/UnitValues&gt;&lt;Analysis&gt;&lt;Enums/&gt;&lt;Counts/&gt;&lt;New/&gt;&lt;/Analysis&gt;&lt;/ValueMetadata&gt;</xsl:text>
				<xsl:text>'</xsl:text>
			</xsl:when>
			<xsl:when test="@code = 'CAT:S.FixedTissue.volume_aliquots'
				    or @code = 'CAT:S.FixedTissueBlock.volume_aliquots'
				    or @code = 'CAT:S.FixedTissueSlide.volume_aliquots'
				    or @code = 'CAT:S.FreshTissue.volume_aliquots'
				    or @code = 'CAT:S.FrozenTissue.volume_aliquots'
				    or @code = 'CAT:S.FrozenTissueBlock.volume_aliquots'
				    or @code = 'CAT:S.FrozenTissueSlide.volume_aliquots'
				    or @code = 'CAT:S.Microdissected.volume_aliquots'
				    or @code = 'CAT:S.NotSpecified.volume_aliquots'
                                    ">				    
				<xsl:text>'</xsl:text>
				<xsl:text disable-output-escaping="yes">&lt;?xml version="1.0"?&gt;&lt;ValueMetadata&gt;&lt;Version&gt;3.02&lt;/Version&gt;&lt;CreationDateTime&gt;04/15/2007 01:22:04&lt;/CreationDateTime&gt;&lt;TestID&gt;Tissue&lt;/TestID&gt;&lt;TestName&gt;Tissue&lt;/TestName&gt;&lt;DataType&gt;PosFloat&lt;/DataType&gt;&lt;CodeType&gt;GRP&lt;/CodeType&gt;&lt;Loinc&gt;2000-8&lt;/Loinc&gt;&lt;Flagstouse&gt;HL&lt;/Flagstouse&gt;&lt;Oktousevalues&gt;Y&lt;/Oktousevalues&gt;&lt;MaxStringLength/&gt;&lt;LowofLowValue/&gt;&lt;HighofLowValue&gt;9&lt;/HighofLowValue&gt;&lt;LowofHighValue&gt;10&lt;/LowofHighValue&gt;&lt;HighofHighValue/&gt;&lt;LowofToxicValue/&gt;&lt;HighofToxicValue/&gt;&lt;EnumValues/&gt;&lt;CommentsDeterminingExclusion&gt;&lt;Com/&gt;&lt;/CommentsDeterminingExclusion&gt;&lt;UnitValues&gt;&lt;NormalUnits&gt;count&lt;/NormalUnits&gt;&lt;NormalUnits&gt;count%&lt;/NormalUnits&gt;&lt;EqualUnits&gt;count&lt;/EqualUnits&gt;&lt;EqualUnits&gt;count%&lt;/EqualUnits&gt;&lt;ExcludingUnits/&gt;&lt;ConvertingUnits&gt;&lt;Units/&gt;&lt;MultiplyingFactor/&gt;&lt;/ConvertingUnits&gt;&lt;/UnitValues&gt;&lt;Analysis&gt;&lt;Enums/&gt;&lt;Counts/&gt;&lt;New/&gt;&lt;/Analysis&gt;&lt;/ValueMetadata&gt;</xsl:text>
				<xsl:text>'</xsl:text>
			</xsl:when>
			<xsl:when test="@code = 'CAT:S.AmnioticFluid.volume_aliquots'
				    or @code = 'CAT:S.Bile.volume_aliquots'
				    or @code = 'CAT:S.BodyCavityFluid.volume_aliquots'
				    or @code = 'CAT:S.BoneMarrowPlasma.volume_aliquots'
				    or @code = 'CAT:S.BuffyCoat.volume_aliquots'
				    or @code = 'CAT:S.CerebrospinalFluid.volume_aliquots'
				    or @code = 'CAT:S.Feces.volume_aliquots'
				    or @code = 'CAT:S.GastricFluid.volume_aliquots'
				    or @code = 'CAT:S.Lavage.volume_aliquots'
				    or @code = 'CAT:S.Milk.volume_aliquots'
				    or @code = 'CAT:S.NotSpecified.volume_aliquots'
				    or @code = 'CAT:S.PericardialFluid.volume_aliquots'
				    or @code = 'CAT:S.Plasma.volume_aliquots'
				    or @code = 'CAT:S.Saliva.volume_aliquots'
				    or @code = 'CAT:S.Serum.volume_aliquots'
				    or @code = 'CAT:S.Sputum.volume_aliquots'
				    or @code = 'CAT:S.Sweat.volume_aliquots'
				    or @code = 'CAT:S.SynovialFluid.volume_aliquots'
				    or @code = 'CAT:S.Urine.volume_aliquots'
				    or @code = 'CAT:S.VitreousFluid.volume_aliquots'
				    or @code = 'CAT:S.WholeBlood.volume_aliquots'
				    or @code = 'CAT:S.WholeBoneMarrow.volume_aliquots'
                                    ">				    
				<xsl:text>'</xsl:text>
				<xsl:text disable-output-escaping="yes">&lt;?xml version="1.0"?&gt;&lt;ValueMetadata&gt;&lt;Version&gt;3.02&lt;/Version&gt;&lt;CreationDateTime&gt;04/15/2007 01:22:04&lt;/CreationDateTime&gt;&lt;TestID&gt;Fluid&lt;/TestID&gt;&lt;TestName&gt;Fluid&lt;/TestName&gt;&lt;DataType&gt;PosFloat&lt;/DataType&gt;&lt;CodeType&gt;GRP&lt;/CodeType&gt;&lt;Loinc&gt;2000-8&lt;/Loinc&gt;&lt;Flagstouse&gt;HL&lt;/Flagstouse&gt;&lt;Oktousevalues&gt;Y&lt;/Oktousevalues&gt;&lt;MaxStringLength/&gt;&lt;LowofLowValue/&gt;&lt;HighofLowValue&gt;9&lt;/HighofLowValue&gt;&lt;LowofHighValue&gt;10&lt;/LowofHighValue&gt;&lt;HighofHighValue/&gt;&lt;LowofToxicValue/&gt;&lt;HighofToxicValue/&gt;&lt;EnumValues/&gt;&lt;CommentsDeterminingExclusion&gt;&lt;Com/&gt;&lt;/CommentsDeterminingExclusion&gt;&lt;UnitValues&gt;&lt;NormalUnits&gt;ml&lt;/NormalUnits&gt;&lt;NormalUnits&gt;ml%&lt;/NormalUnits&gt;&lt;EqualUnits&gt;ml&lt;/EqualUnits&gt;&lt;EqualUnits&gt;ml%&lt;/EqualUnits&gt;&lt;ExcludingUnits/&gt;&lt;ConvertingUnits&gt;&lt;Units/&gt;&lt;MultiplyingFactor/&gt;&lt;/ConvertingUnits&gt;&lt;/UnitValues&gt;&lt;Analysis&gt;&lt;Enums/&gt;&lt;Counts/&gt;&lt;New/&gt;&lt;/Analysis&gt;&lt;/ValueMetadata&gt;</xsl:text>
				<xsl:text>'</xsl:text>
                        </xsl:when>
			<xsl:when test="@code = 'CAT:S.cDNA.volume_aliquots'
				    or @code = 'CAT:S.DNA.volume_aliquots'
				    or @code = 'CAT:S.NotSpecified.volume_aliquots'
				    or @code = 'CAT:S.Protein.volume_aliquots'
				    or @code = 'CAT:S.RNA.volume_aliquots'
				    or @code = 'CAT:S.RNA,cytoplasmic.volume_aliquots'
				    or @code = 'CAT:S.RNA,nuclear.volume_aliquots'
				    or @code = 'CAT:S.RNA,poly-Aenriched.volume_aliquots'
				    or @code = 'CAT:S.TotalNucleicAcid.volume_aliquots'
				    or @code = 'CAT:S.WholeGenomeAmplifiedDNA.volume_aliquots'
                                    ">				    
				<xsl:text>'</xsl:text>
				<xsl:text disable-output-escaping="yes">&lt;?xml version="1.0"?&gt;&lt;ValueMetadata&gt;&lt;Version&gt;3.02&lt;/Version&gt;&lt;CreationDateTime&gt;04/15/2007 01:22:04&lt;/CreationDateTime&gt;&lt;TestID&gt;Molecular&lt;/TestID&gt;&lt;TestName&gt;Molecular&lt;/TestName&gt;&lt;DataType&gt;PosFloat&lt;/DataType&gt;&lt;CodeType&gt;GRP&lt;/CodeType&gt;&lt;Loinc&gt;2000-8&lt;/Loinc&gt;&lt;Flagstouse&gt;HL&lt;/Flagstouse&gt;&lt;Oktousevalues&gt;Y&lt;/Oktousevalues&gt;&lt;MaxStringLength/&gt;&lt;LowofLowValue/&gt;&lt;HighofLowValue&gt;9&lt;/HighofLowValue&gt;&lt;LowofHighValue&gt;10&lt;/LowofHighValue&gt;&lt;HighofHighValue/&gt;&lt;LowofToxicValue/&gt;&lt;HighofToxicValue/&gt;&lt;EnumValues/&gt;&lt;CommentsDeterminingExclusion&gt;&lt;Com/&gt;&lt;/CommentsDeterminingExclusion&gt;&lt;UnitValues&gt;&lt;NormalUnits&gt;µg&lt;/NormalUnits&gt;&lt;NormalUnits&gt;µg%&lt;/NormalUnits&gt;&lt;EqualUnits&gt;µg&lt;/EqualUnits&gt;&lt;EqualUnits&gt;µg%&lt;/EqualUnits&gt;&lt;ExcludingUnits/&gt;&lt;ConvertingUnits&gt;&lt;Units/&gt;&lt;MultiplyingFactor/&gt;&lt;/ConvertingUnits&gt;&lt;/UnitValues&gt;&lt;Analysis&gt;&lt;Enums/&gt;&lt;Counts/&gt;&lt;New/&gt;&lt;/Analysis&gt;&lt;/ValueMetadata&gt;</xsl:text>
				<xsl:text>'</xsl:text>
                        </xsl:when>
			<xsl:when test="@code = 'CAT:S.CryoCells.volume_aliquots'
				    or @code = 'CAT:S.FixedCellBlock.volume_aliquots'
				    or @code = 'CAT:S.FrozenCellBlock.volume_aliquots'
				    or @code = 'CAT:S.NotSpecified.volume_aliquots'
				    or @code = 'CAT:S.Slide.volume_aliquots'
                                    ">				    
				<xsl:text>'</xsl:text>
				<xsl:text disable-output-escaping="yes">&lt;?xml version="1.0"?&gt;&lt;ValueMetadata&gt;&lt;Version&gt;3.02&lt;/Version&gt;&lt;CreationDateTime&gt;04/15/2007 01:22:04&lt;/CreationDateTime&gt;&lt;TestID&gt;Cell&lt;/TestID&gt;&lt;TestName&gt;Cell&lt;/TestName&gt;&lt;DataType&gt;PosFloat&lt;/DataType&gt;&lt;CodeType&gt;GRP&lt;/CodeType&gt;&lt;Loinc&gt;2000-8&lt;/Loinc&gt;&lt;Flagstouse&gt;HL&lt;/Flagstouse&gt;&lt;Oktousevalues&gt;Y&lt;/Oktousevalues&gt;&lt;MaxStringLength/&gt;&lt;LowofLowValue/&gt;&lt;HighofLowValue&gt;9&lt;/HighofLowValue&gt;&lt;LowofHighValue&gt;10&lt;/LowofHighValue&gt;&lt;HighofHighValue/&gt;&lt;LowofToxicValue/&gt;&lt;HighofToxicValue/&gt;&lt;EnumValues/&gt;&lt;CommentsDeterminingExclusion&gt;&lt;Com/&gt;&lt;/CommentsDeterminingExclusion&gt;&lt;UnitValues&gt;&lt;NormalUnits&gt;cellcount&lt;/NormalUnits&gt;&lt;NormalUnits&gt;cellcount%&lt;/NormalUnits&gt;&lt;EqualUnits&gt;cellcount&lt;/EqualUnits&gt;&lt;EqualUnits&gt;cellcount%&lt;/EqualUnits&gt;&lt;ExcludingUnits/&gt;&lt;ConvertingUnits&gt;&lt;Units/&gt;&lt;MultiplyingFactor/&gt;&lt;/ConvertingUnits&gt;&lt;/UnitValues&gt;&lt;Analysis&gt;&lt;Enums/&gt;&lt;Counts/&gt;&lt;New/&gt;&lt;/Analysis&gt;&lt;/ValueMetadata&gt;</xsl:text>
				<xsl:text>'</xsl:text>
                        </xsl:when>
			<xsl:when test="@code = 'XXXXX'
				">
				<xsl:text>'</xsl:text>
				<xsl:text disable-output-escaping="yes">cell</xsl:text>
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
		
		<!-- VALUETYPE_CD -->
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

