import edu.asu.bmi.cad.SerializationHelper;
import org.hl7.v3.*;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.validation.Schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class XMLSerializationTest extends AbstractTest {

    private static SerializationHelper<InfrastructureRoot> helper;

    @BeforeClass
    public static void setUp() {
        Schema schema = null;
        schema = SerializationHelper.getV3Schema();
        helper = new SerializationHelper( schema );
    }


    @Test
    public void testSerializeGraph() {
        try {
            Procedure a = createSimple();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            helper.toXML( a, System.out );
            helper.toXML( a, baos );

            assertEquals( target.replaceAll("oid=\"[^\"]*\"", ""), baos.toString().replaceAll("oid=\"[^\"]*\"", "") );
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
    }

    @Test
    public void testSerializeGraphWithCycle() {
        try {
            Procedure a = create();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            helper.toXML( a, System.out );
            helper.toXML( a, baos );

            assertEquals( targetCycle.replaceAll("oid=\"[^\"]*\"", ""), baos.toString().replaceAll("oid=\"[^\"]*\"", "") );
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
    }

    @Test
    public void testDeserializeGraph() {
        try {
            Act a = createSimple();

            Procedure x = (Procedure) helper.fromXML( new ByteArrayInputStream( target.getBytes() ) );

            assertEquals( "132", ((PQ) ((Observation) x.getOutboundRelationships().get( 0 ).getTarget()).getValues().get( 0 )).getValue() );
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
    }

    @Test
    public void testDeserializeGraphCycle() {
        try {
            Act a = create();

            Procedure x = (Procedure) helper.fromXML( new ByteArrayInputStream( targetCycle.getBytes() ) );

            assertSame(x.getOutboundRelationships().get(0).getTarget(), x.getOutboundRelationships().get(1).getTarget());
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
    }

    @Test
    public void testCycle() {
        PatientEncounter pe = new PatientEncounter().withIds( new II().withRoot("1").withExtension("2")).withClassCode( ActClass.ENC );

        Role role = new Role().withIds( new II().withRoot("1.2.3.99.100.1").withExtension("4"));
        pe.withParticipations( new Participation().withAct(pe).withRole(role) );

        helper.toXML( pe, System.out );
    }

    @Test
    public void testCycleWithTwo() {
        PatientEncounter pe = new PatientEncounter().withIds( new II().withRoot("1").withExtension("1")).withClassCode( ActClass.ENC );

        Act target = new Act().withIds( new II().withExtension( "zzzz" ) );

        pe.withOutboundRelationships( new ActRelationship().withTarget( pe ).withSource( target ) );
        pe.withInboundRelationships( new ActRelationship().withTarget( target ).withSource( pe ) );

        target.withOutboundRelationships( new ActRelationship().withTarget( pe ).withSource( target ) );
        target.withInboundRelationships( new ActRelationship().withTarget( target ).withSource( pe ) );

        helper.toXML( pe, System.out );
        helper.toXML( target, System.out );
    }

        @Test
    public void testEncounterRoundtrip() {
        PatientEncounter pe = new PatientEncounter().withIds( new II().withRoot( "1.2.3.99.100.1" ).withExtension( "100_6955844_4098" ).withAssigningAuthorityName( "Mayo" ).withScope( IdentifierScope.OBJ ).withReliability( IdentifierReliability.VRF ) );
        pe.withEffectiveTime( new SXCM_TS().withValue( "12345" ) );
        pe.withParticipations(
                new Participation().withRole( new Role().withClassCode( RoleClass.PAT ).withCode( new CD().withCode( "123" ) ) ),
                new Participation().withTypeCode( ParticipationType.PRF ).withRole(
                        new Role().withClassCode( RoleClass.SAME ).withCode(
                                new CD().withCode( "X" ).withCodeSystem( "1.2.3.4" ) ).withNames( new EN().withContent( new ObjectFactory().createENFamily( new EnxpFamily().withValue( "DE MARTINO" ) ) ) )
                ),
                new Participation().withTypeCode( ParticipationType.LOC ).withTime( new IVL_TS().withValue( "132121" ) )
        );
        pe.withOutboundRelationships(
                new ActRelationship()
                        .withTypeCode( ActRelationshipType.COMP )
                        .withTarget(
                                new Act().withClassCode( ActClass.ENC ).withMoodCode( ActMood.EVN )
                                        .withCode( new CD().withCode( "R" ).withCodeSystem( "1.2.3" ) )
                                        .withEffectiveTime( new SXCM_TS().withValue( "12344543253" ) )
                        )
        );
        pe.withLengthOfStayQuantity( new PQ().withValue( "0" ) );
        pe.withDischargeDispositionCode( new CD().withCode( "A" ).withCodeSystem( "1.2.3" ) );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        helper.toXML( pe, baos );
        helper.toXML( pe, System.out );


        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        PatientEncounter x = (PatientEncounter) helper.fromXML( new ByteArrayInputStream( baos.toByteArray() ) );
        helper.toXML( x, baos2 );
        helper.toXML( x, System.out );

        assertEquals( new String( baos.toByteArray() ), new String( baos2.toByteArray() ) );

    }

    @Test
    public void testDeserializeEncounterbug() {
        PatientEncounter e = (PatientEncounter) helper.fromXML( this.getClass().getResourceAsStream("/encounter.xml") );

        int roles = 0;
        for( Participation p : e.getParticipations() ) {
            if( p.getRole() != null ) {
                roles++;
            }
        }

        assertEquals( 2, roles );
    }
}
