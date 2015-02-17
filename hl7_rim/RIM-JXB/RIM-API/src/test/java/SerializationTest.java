import org.hl7.v3.api.Act;
import org.hl7.v3.ActRelationshipType;
import org.hl7.v3.II;
import org.hl7.v3.api.ActRelationship;
import org.hl7.v3.api.Observation;
import org.hl7.v3.PQ;
import org.hl7.v3.api.Procedure;
import org.hl7.v3.SXCM_TS;
import org.hl7.v3.api.impl.ActRelationshipImpl;
import org.hl7.v3.api.impl.ObservationImpl;
import org.hl7.v3.api.impl.ProcedureImpl;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import static org.junit.Assert.fail;

public class SerializationTest {

    String xml = "test.xml";

    public Act create() {
        Procedure p = new ProcedureImpl();
        p.getIds().add( new II().withRoot( "a.b.c" ).withExtension( "d" ) );
        p.setActivityTime( new SXCM_TS() );

        Observation o = new ObservationImpl();
        o.getValues().add( new PQ().withValue( "132" ).withUnit( "mg/l" ) );

        ActRelationship a1 = new ActRelationshipImpl();
        a1.setTypeCode( ActRelationshipType.DEBIT );
        a1.setTarget( o );
        ActRelationship a2 = new ActRelationshipImpl();
        a2.setTypeCode( ActRelationshipType.PART );
        a2.setTarget( o );

        p.getInboundRelationships().add( a1 );
        p.getInboundRelationships().add( a2 );

        return p;
    }


    @Test
    public void testSerializeGraph() {
        try {
            Act a = create();

            JAXBContext ctx = JAXBContext.newInstance( Act.class.getPackage().getName() );

            Marshaller m = ctx.createMarshaller();
            m.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
            m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

            m.marshal( a, System.out );
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
    }
}
