package edu.asu.bmi.cad;

import org.hl7.v3.InfrastructureRoot;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractSerializationHelper<IRoot> extends XmlAdapter<IRoot, IRoot> {

    private String namespace;
    private Schema schema;
    private String encoding;

    private Map cache;

    public AbstractSerializationHelper( String namespace, Schema schema ) {
    	this( namespace, schema, "UTF-8");
    }
    
    public AbstractSerializationHelper( String namespace, Schema schema, String encoding ) {
        this.namespace = namespace;
        this.schema = schema;
        this.encoding = encoding;
    }

    public Collection getObjects() {
        return getCache().values();
    }

    public Object getObject( String key ) {
        return getCache().get( key );
    }

    public Marshaller createMarshaller( String pack ) {
        return createMarshaller( pack, null );
    }

    public Marshaller createMarshaller( String pack, Schema schema ) {
        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance( pack );
            final Marshaller marsh = jaxbContext.createMarshaller();


            if ( schema != null ) {
                marsh.setSchema( schema );
                marsh.setEventHandler( new DefaultValidationEventHandler() );
            } else {
                throw new IllegalStateException( "Validate!" );
            }

            marsh.setProperty( Marshaller.JAXB_ENCODING, this.encoding );
            marsh.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

            this.setAdapter(marsh);

            marsh.setListener( new Marshaller.Listener() {
                @Override
                public void beforeMarshal( Object target ) {
                    if ( ( target instanceof InfrastructureRoot )
                         && ((InfrastructureRoot) target).getOref() == null ) {
                        // ensure the object has an ID or references may be broken
                        getId((IRoot) target);
                    }
                }
            } );

            return marsh;
        } catch ( JAXBException e ) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract void setAdapter(Marshaller marshaller);

    protected Unmarshaller createUnmarshaller( String pack ) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance( pack );
            final Unmarshaller unmarsh = jaxbContext.createUnmarshaller();
            final AbstractSerializationHelper self = this;

            if ( schema != null ) {
                unmarsh.setSchema( schema );
                unmarsh.setEventHandler( new DefaultValidationEventHandler() );
            } else {
                throw new IllegalStateException( "Validate!" );
            }

            unmarsh.setListener( new Unmarshaller.Listener() {
                @Override
                public void beforeUnmarshal( Object target, Object parent ) {
                    unmarsh.setAdapter( AbstractSerializationHelper.class, self );
                }
            } );

            return unmarsh;
        } catch ( JAXBException e ) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IRoot unmarshal( IRoot obj ) throws Exception {
        IRoot reborn = (IRoot) obj;
        if ( getReference( reborn ) != null ) {
            return getReference( reborn );
        } else {
            getCache().put( getId( reborn ), (IRoot) reborn );
            return (IRoot) reborn;
        }
    }

    protected abstract IRoot getReference( IRoot reborn );

    protected abstract String getId( IRoot x );

    protected abstract void setReference( IRoot alter, IRoot orig );

    protected abstract void setId( IRoot alter, Object id );

    @Override
    public IRoot marshal( IRoot x ) throws Exception {
        if ( x == null ) {
            return null;
        }
        if ( !getCache().containsKey( getId( x ) ) ) {
            String oid = getId( x );
            getCache().put( oid, x );
            return x;
        } else {
            Class k = x.getClass();
            IRoot alter = (IRoot) k.newInstance();
            setReference( alter, x );
            return alter;
        }
    }



    protected IRoot fromXML( InputStream source, String namespace ) {
        try {
            Unmarshaller unmarshal = createUnmarshaller( namespace );
            Object o = unmarshal.unmarshal( source );
            if ( o instanceof JAXBElement ) {
                return (IRoot) ((JAXBElement) o).getValue();
            } else {
                return (IRoot) o;
            }
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        } finally {
            getCache().clear();
        }
    }

    protected Schema getSchema() {
        return schema;
    }


    public static Schema getV3Schema( final String xsdPath ) {
        SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );

        try {
            factory.setFeature( "http://apache.org/xml/features/honour-all-schemaLocations", true );
        } catch ( SAXNotRecognizedException e ) {
            e.printStackTrace();
        } catch ( SAXNotSupportedException e ) {
            e.printStackTrace();
        }

        factory.setResourceResolver( new LSResourceResolver() {
            @Override
            public LSInput resolveResource( String type, String namespaceURI, String publicId, String systemId, String baseURI ) {
                LSInput input = new LSInputImpl( namespaceURI, publicId, systemId, xsdPath );
                return input;
            }
        } );
        try {
            Schema schema = factory.newSchema( new StreamSource[] {
                    new StreamSource( AbstractSerializationHelper.class.getResourceAsStream( xsdPath + "rim.xsd" ) ),
            } );
            return schema;
        } catch ( SAXException e ) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, IRoot> getCache() {
        if ( cache == null ) {
            cache = new LinkedHashMap<String, IRoot>();
        }
        return cache;
    }
}
