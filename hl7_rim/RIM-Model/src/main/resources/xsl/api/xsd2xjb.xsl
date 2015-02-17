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
							 xpath-default-namespace="urn:hl7-org:v3/mif"
							 exclude-result-prefixes="xsl mif">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	<xsl:strip-space elements="*"/> 

	<!-- deep null transform -->
	<xsl:template match="/">
		<jaxb:bindings xmlns="http://java.sun.com/xml/ns/jaxb"
		          xmlns:xsi="http://www.w3.org/2000/10/XMLSchema-instance"
		          xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc"
		          xmlns:xs="http://www.w3.org/2001/XMLSchema"
		          xmlns:inheritance="http://jaxb2-commons.dev.java.net/basic/inheritance"
		          xmlns:annox="http://annox.dev.java.net"
		          version="2.1">

			<jaxb:bindings node="/xs:schema" schemaLocation="{substring-after(base-uri(),'core/')}">
				<xsl:apply-templates select="node()"/>
			</jaxb:bindings>
		</jaxb:bindings>

	</xsl:template>

	<xsl:template name="reuse-complex" match="//xs:complexType[ contains( base-uri(), 'datatypes' ) ]">
		<jaxb:bindings node="//xs:complexType[@name='{@name}']" >
			<jaxb:class ref="{concat( 'org.hl7.v3.', @name )}" />
 		</jaxb:bindings>
	</xsl:template>
	
	<xsl:template name="reuse-simple" match="//xs:simpleType[not(starts-with( @name, 'set_' ) ) and count(current()/xs:restriction/xs:enumeration) > 0]">
		<jaxb:bindings node="//xs:simpleType[@name='{@name}']" >
			<jaxb:typesafeEnumClass ref="{concat( 'org.hl7.v3.', replace( @name, '-', '' ) )}" />
 		</jaxb:bindings>
	</xsl:template>

	<xsl:template name="annotate" match="//xs:complexType[ not( contains( base-uri(), 'datatypes' ) ) ]">
		<jaxb:bindings node="//xs:complexType[@name='{@name}']" >
			<annox:annotate>
				<annox:annotate annox:class="javax.xml.bind.annotation.XmlRootElement" name="{@name}"/>
			</annox:annotate>
		</jaxb:bindings>
	</xsl:template>


</xsl:transform>
