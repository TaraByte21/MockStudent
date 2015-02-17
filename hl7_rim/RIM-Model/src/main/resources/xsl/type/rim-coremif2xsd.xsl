<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (c) 2014, Pragmatic Data LLC. All rights reserved. -->
<xsl:transform version="2.0" 
							 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
							 xmlns:mif="urn:hl7-org:v3/mif"
							 xmlns:xs="http://www.w3.org/2001/XMLSchema"
							 xmlns:f="f"
							 xpath-default-namespace="urn:hl7-org:v3/mif" 
							 exclude-result-prefixes="xsl mif xs f">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	<xsl:strip-space elements="*"/> 

	<!-- deep null transform -->
	<xsl:template match="/|@*|node()">
		<xsl:apply-templates select="@*|node()"/>
	</xsl:template>
	
	<xsl:template match="mif:staticModel">
		<xs:schema xmlns="urn:hl7-org:v3" xmlns:xs="http://www.w3.org/2001/XMLSchema"
							 targetNamespace="urn:hl7-org:v3"
							 elementFormDefault="qualified">
			<xs:include schemaLocation="infrastructureRoot-r2b.xsd"/>
			<xsl:apply-templates select="node()"/>
		</xs:schema>
	</xsl:template>

	<xsl:template match="mif:class[@name='Transmission']" priority="2"/>
	<xsl:template match="mif:class[@name='TransmissionRelationship']" priority="2"/>
	<xsl:template match="mif:class[@name='Acknowledgement']" priority="2"/>
	<xsl:template match="mif:class[@name='AcknowledgementDetail']" priority="2"/>
	<xsl:template match="mif:class[@name='Attachment']" priority="2"/>
	<xsl:template match="mif:class[@name='AttentionLine']" priority="2"/>
	<xsl:template match="mif:class[@name='Batch']" priority="2"/>
	<xsl:template match="mif:class[@name='Message']" priority="2"/>
	<xsl:template match="mif:class[@name='CommunicationFunction']" priority="2"/>
	<xsl:template match="mif:class[@name='ControlAct']" priority="2"/>

	<xsl:template match="mif:class[//mif:specializationChild[@childClassName = current()/@name][not(parent::mif:class/@name = 'InfrastructureRoot')]]" priority="1">
		<xs:element name="{concat(lower-case(substring(@name,1,1)),substring(@name,2))}" type="{@name}" />
		<xs:complexType name="{@name}">
			<xs:complexContent>
				<xs:extension base="{//mif:specializationChild[@childClassName = current()/@name]/parent::mif:class/@name}">
					<xsl:call-template name="class-content"/>
				</xs:extension>
			</xs:complexContent>
		</xs:complexType>
	</xsl:template>
	<xsl:template match="mif:class">
		<xs:element name="{concat(lower-case(substring(@name,1,1)),substring(@name,2))}" type="{@name}" />
		<xs:complexType name="{@name}">
			<xsl:if test="@name='InfrastructureRoot'" >
				<xsl:call-template name="class-content"/>
			</xsl:if>
			<xsl:if test="not( @name='InfrastructureRoot' )">
				<xs:complexContent>
					<xs:extension base="InfrastructureRoot">
						<xsl:call-template name="class-content"/>
					</xs:extension>
				</xs:complexContent>
			</xsl:if>
		</xs:complexType>
	</xsl:template>
	<xsl:template name="class-content">
		<xs:sequence>
			<!-- xs:group ref="InfrastructureRootElements"/ -->
					<xsl:apply-templates select="node()"/>
					<xsl:apply-templates select="for $a in //mif:connections/mif:traversableConnection[@participantClassName=current()/@name] return $a/preceding-sibling::mif:traversableConnection|$a/following-sibling::mif:traversableConnection">
					<xsl:with-param name="className" select="@name"/>
					<xsl:sort select="@sortKey" data-type="number"/>
					<xsl:sort select="@sortKey" data-type="text"/>
					<xsl:sort select="@name" data-type="text"/>
				</xsl:apply-templates>
		</xs:sequence>
		<!--xs:attributeGroup ref="InfrastructureRootAttributes"/-->
		<xsl:if test="@name='InfrastructureRoot'" >
			<!-- DS Add internal attributes for refs TODO settle the internal ID strategy-->
			<xs:attribute name="oid" type="xs:ID" />
			<xs:attribute name="oref" type="xs:IDREF" />
		</xsl:if>
		<xsl:apply-templates mode="attributes" select="node()"/>
	</xsl:template>
	
	<xsl:template match="/mif:specializationChild[@childClassName and not(preceding-sibling::mif:specializationChild[@childClassName eq current()/@childClassName])]">
		<xsl:param name="visiting" select="/.." tunnel="yes"/>
		<!-- xsl:message><TEST visited="{string-join($visited,' ')}"/></xsl:message -->
		<!-- xsl:if test="not(some $i in $visited satisfies $i lt @childClassName)" -->
		<mif:specializationChild sortKey="{count(preceding-sibling::mif:specializationChild)}">
			<mif:specializedClass>
				<xsl:apply-templates select="//mif:class[@name=current()/@childClassName]">
					<xsl:with-param name="visiting" select="../mif:specializationChild/@childClassName" tunnel="yes"/>
					<xsl:sort select="@sortKey" data-type="number"/>
					<xsl:sort select="@sortKey" data-type="text"/>
				</xsl:apply-templates>
			</mif:specializedClass>
		</mif:specializationChild>
		<!-- /xsl:if -->
	</xsl:template>

	<xsl:template match="mif:ownedAssociation"/>

	<xsl:template match="mif:traversableConnection[@participantClassName='CommunicationFunction']" priority="1"/> 
	<xsl:template match="mif:traversableConnection[@participantClassName='ControlAct']" priority="1"/> 
	<xsl:template match="mif:traversableConnection">
		<xs:element name="{@name}" type="{@participantClassName}" minOccurs="0" maxOccurs="{f:occurs(@maximumMultiplicity)}"/>
	</xsl:template>

	<xsl:template match="mif:attribute"/>
	<xsl:template match="mif:attribute[not(@isStructural='true')]" priority="1">
		<xs:element name="{@name}" minOccurs="{@minimumMultiplicity}"  maxOccurs="{f:occurs(@maximumMultiplicity)}">
			<xsl:apply-templates select="@*|node()"/>
		</xs:element>
	</xsl:template>
	<xsl:template match="mif:annotations"/>
	<xsl:template match="mif:type">
		<xsl:attribute name="type">
			<!--xsl:text>dt:</xsl:text-->
			<xsl:value-of select="f:type-noflavor(@name)"/>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:attribute>
	</xsl:template>
	<xsl:template match="mif:supplierBindingArgumentDatatype">
		<xsl:text>_</xsl:text>
		<xsl:value-of select="f:type-noflavor(@name)"/>
	</xsl:template>
	<xsl:template match="mif:type[@name = ('DSET','LIST','BAG','COLL')][mif:supplierBindingArgumentDatatype/@name]" priority="1">
		<xsl:attribute name="type">
			<!--xsl:text>dt:</xsl:text-->
			<xsl:value-of select="f:type-noflavor(mif:supplierBindingArgumentDatatype/@name)"/>
		</xsl:attribute>
	</xsl:template>
	<xsl:template match="mif:type[@name = 'QSET'][mif:supplierBindingArgumentDatatype/@name]" priority="1">
		<xsl:attribute name="type">
			<!--xsl:text>dt:</xsl:text-->
			<xsl:text>SXCM_</xsl:text>
			<xsl:value-of select="f:type-noflavor(mif:supplierBindingArgumentDatatype/@name)"/>
		</xsl:attribute>
	</xsl:template>


	<xsl:template mode="attributes" match="mif:attribute[not(@isStructural='true')]" priority="1"/>
	<xsl:template mode="attributes" match="mif:attribute[@name='blockedContextActRelationshipType']" priority="2"/>
	<xsl:template mode="attributes" match="mif:attribute[@name='blockedContextParticipationType']" priority="2"/>
	<xsl:template mode="attributes" match="mif:attribute">
		<xs:attribute name="{@name}">
			<xsl:apply-templates mode="attributes" select="@*|node()"/>
		</xs:attribute>
	</xsl:template>
	<xsl:template mode="attributes" match="mif:supplierStructuralDomain">
		<xsl:comment>
			<xs:attribute name="classCode" type="{@codeSystemName}" use="optional" default="{@mnemonic}"/>
		</xsl:comment>
	</xsl:template>

	<xsl:template mode="attributes" match="mif:annotations"/>
	<xsl:template mode="attributes" match="mif:type">
		<xsl:attribute name="type">
			<!--xsl:text>dt:</xsl:text-->
			<xsl:value-of select="lower-case(f:type-noflavor(@name))"/>
			<xsl:apply-templates mode="attributes" select="@*|node()"/>
		</xsl:attribute>
	</xsl:template>
	<xsl:template mode="attributes" match="mif:supplierBindingArgumentDatatype">
		<xsl:text>_</xsl:text>
		<xsl:value-of select="lower-case(f:type-noflavor(@name))"/>
	</xsl:template>
	<xsl:template mode="attributes" match="mif:type[../mif:supplierDomainSpecification[@codingStrength='CNE']]">
		<xsl:attribute name="type">
			<!--xsl:text>dt:</xsl:text-->
			<xsl:value-of select="../mif:supplierDomainSpecification/@domainName"/>
		</xsl:attribute>
	</xsl:template>

	<xsl:function name="f:occurs">
		<xsl:param name="s"/>
		<xsl:value-of select="if($s = '*') then 'unbounded' else $s"/>
	</xsl:function>
	<xsl:function name="f:type-noflavor">
		<xsl:param name="s"/>
		<xsl:value-of select="if(contains($s,'.')) then substring-before($s, '.') else $s"/>
	</xsl:function>

    <!-- Disable default attribute values
	<xsl:template mode="attributes" match="@defaultValue">
		<xsl:attribute name="default">
			<xsl:value-of select="."/>
		</xsl:attribute>
	</xsl:template>
	-->

	<xsl:template mode="attributes" match="@isMandatory">
		<xsl:attribute name="use">
			<xsl:choose>
				<!-- DS Make attributes always optional to support refs -->
				<xsl:when test=". = 'true'">optional</xsl:when>
				<xsl:when test=". = 'false'">optional</xsl:when>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>

	<!-- MODE: attributes - deep null transform -->
	<xsl:template mode="attributes" match="/|@*|node()">
		<xsl:apply-templates mode="attributes" select="@*|node()"/>
	</xsl:template>
</xsl:transform>
