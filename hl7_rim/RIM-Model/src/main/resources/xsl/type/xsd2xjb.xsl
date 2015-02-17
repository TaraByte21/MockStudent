<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (c) 2014, Pragmatic Data LLC. All rights reserved. -->
<xsl:transform version="2.0" 
							 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
							 xmlns:mif="urn:hl7-org:v3/mif"
							 xmlns:xs="http://www.w3.org/2001/XMLSchema"
							 xmlns:xsi="http://www.w3.org/2000/10/XMLSchema-instance"
							 xmlns:annox="http://annox.dev.java.net"
							 xmlns:inheritance="http://jaxb2-commons.dev.java.net/basic/inheritance"
							 xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
							 xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc"
                             xmlns:equals="http://jaxb2-commons.dev.java.net/basic/equals"
                             xmlns:hashcode="http://jaxb2-commons.dev.java.net/basic/hashCode"
							 xpath-default-namespace="urn:hl7-org:v3/mif"
							 exclude-result-prefixes="xsl mif equals hashcode">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	<xsl:strip-space elements="*"/>

    <xsl:param name="target">InfrastructureRoot</xsl:param>

	<!-- deep null transform -->
	<xsl:template match="/">
		<jaxb:bindings xmlns="http://java.sun.com/xml/ns/jaxb"
		          xmlns:xsi="http://www.w3.org/2000/10/XMLSchema-instance"
		          xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc"
		          xmlns:xs="http://www.w3.org/2001/XMLSchema"
		          xmlns:inheritance="http://jaxb2-commons.dev.java.net/basic/inheritance"
		          xmlns:annox="http://annox.dev.java.net"
		          version="2.1">

			<jaxb:bindings node="/xs:schema" schemaLocation="{ substring-after(base-uri(),'core/') }">
				<xsl:apply-templates select="//xs:complexType"/>
			</jaxb:bindings>
		</jaxb:bindings>

	</xsl:template>

	<xsl:template name="annotate" match="//xs:complexType[ not( contains( document-uri(.), 'datatypes' ) ) ]" >
		<jaxb:bindings node="//xs:complexType[@name='{@name}']" >

			<xsl:if test="not( contains( base-uri(), 'datatypes' ) ) and //xs:extension[ @base=current()/@name ]" >
				<annox:annotate>
					<annox:annotate annox:class="javax.xml.bind.annotation.XmlRootElement" name="{ concat( lower-case( substring( @name, 1, 1 ) ), substring( @name, 2 ) )}"/>
				</annox:annotate>
			</xsl:if>
			<xsl:if test="not( contains( base-uri(), 'datatypes' ) )" >
				<annox:annotate>
					<annox:annotate annox:class="org.drools.core.factmodel.traits.Traitable" logical="true" />
					<annox:annotate annox:class="org.kie.api.definition.type.PropertyReactive" />
				</annox:annotate>
			</xsl:if>
			<xsl:if test=".[@name='InfrastructureRoot']">
				<jaxb:bindings node="//xs:complexType[ @name='InfrastructureRoot' ]" >
					<inheritance:extends>org.hl7.v3.TraitableInfrastructureRoot</inheritance:extends>
				</jaxb:bindings>
			</xsl:if>
            <xsl:if test=".[@name='Act']">
                <jaxb:bindings node="//xs:complexType[ @name='Act' ]//xs:element[ @name='outboundRelationship' ]" >
                    <jaxb:property collectionType="org.hl7.v3.IndexedActRelationshipList" />
                </jaxb:bindings>
                <jaxb:bindings node="//xs:complexType[ @name='Act' ]//xs:element[ @name='inboundRelationship' ]" >
                    <jaxb:property collectionType="org.hl7.v3.IndexedActRelationshipList" />
                </jaxb:bindings>
            </xsl:if>

            <xsl:variable name="parent">
                <xsl:call-template name="parent"/>
            </xsl:variable>
            <xsl:if test="$parent=$target">
                    <equals:ignored/>
                    <hashcode:ignored/>
            </xsl:if>

			<xsl:apply-templates select="for $el in .//xs:element return $el[ //xs:complexType[ @name=$el/@type and .//xs:element[ @name='id'] ] ]"/>

		</jaxb:bindings>
	</xsl:template>

	<xsl:template name="adapter" match="//xs:element" >
		<jaxb:bindings node=".//xs:element[@name='{@name}']">
			<annox:annotate target="field">
				<annox:annotate annox:class="javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter" value="org.hl7.v3.SerializationHelper"/>
			</annox:annotate>
		</jaxb:bindings>
	</xsl:template>

    <xsl:template name="parent">
        <xsl:choose>
            <xsl:when test="exists(xs:complexContent/xs:extension)">
                <xsl:for-each select="//xs:complexType[@name=current()/xs:complexContent/xs:extension/@base]">
                    <xsl:call-template name="parent"/>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@name"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
	
</xsl:transform>
