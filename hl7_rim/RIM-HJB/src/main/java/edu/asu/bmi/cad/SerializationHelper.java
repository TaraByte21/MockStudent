package edu.asu.bmi.cad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.validation.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.hl7.v3.InfrastructureRoot;

public class SerializationHelper<T extends InfrastructureRoot> extends AbstractSerializationHelper<T> {

    public static final String DEFAULT_SCHEMA_LOC = "/xsd/type/";

    private static final String ns = InfrastructureRoot.class.getPackage().getName();

    public SerializationHelper() {
        super( ns, null );
    }

    public SerializationHelper( Schema schema ) {
        super( ns, schema );
    }
    
    public SerializationHelper( Schema schema, String encoding ) {
        super( ns, schema, encoding );
    }

    @Override
    protected String getId( InfrastructureRoot x ) {
        String id;
        if ( x.getOid() == null ) {
            id = "id_" + System.identityHashCode( x );
            x.setOid( id );
        }
        return x.getOid();
    }

    protected T getReference( T reborn ) {
        return (T) reborn.getOref();
    }

    protected void setReference( InfrastructureRoot alter, InfrastructureRoot orig ) {
        alter.setOref( orig );
    }

    protected void setId( InfrastructureRoot alter, Object id ) {
        alter.setOid( id != null ? id.toString() : null );
    }


    public T fromXML( InputStream source ) {
        return fromXML( source, ns );
    }

    public void toXML( T o, OutputStream out ) {
        try {
            Marshaller m = this.createMarshaller( ns, getSchema() );
            setId( o, getId( o ) );
            m.marshal( o, out );
        } catch ( JAXBException e ) {
            e.printStackTrace();
        } finally {
            getCache().clear();
        }
    }

    public void toJSON( T o, OutputStream out ) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion( JsonInclude.Include.NON_EMPTY );
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT);
        try {
            mapper.writeValue( out, o );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public T fromJson( InputStream src ) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion( JsonInclude.Include.NON_EMPTY );
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT);
        try {
            return (T) mapper.readValue( src, InfrastructureRoot.class );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }

    public static Schema getV3Schema() {
        return getV3Schema( DEFAULT_SCHEMA_LOC );
    }

    @Override
    public Marshaller createMarshaller( String pack, Schema schema ) {
        Marshaller marshaller = super.createMarshaller( pack, schema );

        try {
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
                @Override
                public String[] getPreDeclaredNamespaceUris() {
                    return new String[] { XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI };
                }

                @Override
                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                    if (namespaceUri.equals( XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI ))
                        return "xsi";
                    if (namespaceUri.equals( XMLConstants.W3C_XML_SCHEMA_NS_URI))
                        return "xs";
                    return suggestion;

                }
            });
        } catch ( PropertyException e ) {
            // Probably using a different implementation
        }

        return marshaller;
    }

    @Override
    protected void setAdapter(Marshaller marshaller) {
        marshaller.setAdapter( SerializationHelper.class, this );
    }
}
