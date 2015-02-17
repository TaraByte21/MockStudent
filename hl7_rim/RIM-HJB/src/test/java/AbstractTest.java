import org.hl7.v3.ActClass;
import org.hl7.v3.ActMood;
import org.hl7.v3.ActRelationship;
import org.hl7.v3.ActRelationshipType;
import org.hl7.v3.II;
import org.hl7.v3.Observation;
import org.hl7.v3.PQ;
import org.hl7.v3.Procedure;
import org.hl7.v3.SXCM_TS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class AbstractTest {

    String target =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<procedure xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" classCode=\"PROC\" moodCode=\"EVN\" oid=\"odi_oid\">\n" +
            "    <id root=\"abc\" extension=\"d\"/>\n" +
            "    <activityTime/>\n" +
            "    <outboundRelationship typeCode=\"DEBIT\" >\n" +
            "        <target xsi:type=\"Observation\" classCode=\"OBS\" moodCode=\"EVN\" oid=\"xz\">\n" +
            "            <id root=\"x\" extension=\"z\"/>\n" +
            "            <value xsi:type=\"PQ\" value=\"132\" unit=\"mg/l\"/>\n" +
            "        </target>\n" +
            "    </outboundRelationship>\n" +
            "</procedure>\n";

    String targetCycle =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<procedure xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" classCode=\"PROC\" moodCode=\"EVN\" oid=\"aaa\">\n" +
            "    <id root=\"abc\" extension=\"d\"/>\n" +
            "    <activityTime/>\n" +
            "    <outboundRelationship typeCode=\"DEBIT\" >\n" +
            "        <target xsi:type=\"Observation\" classCode=\"OBS\" moodCode=\"EVN\" oid=\"xz\">\n" +
            "            <id root=\"x\" extension=\"z\"/>\n" +
            "            <value xsi:type=\"PQ\" value=\"132\" unit=\"mg/l\"/>\n" +
            "        </target>\n" +
            "    </outboundRelationship>\n" +
            "    <outboundRelationship typeCode=\"PART\" >\n" +
            "        <target xsi:type=\"Observation\" oref=\"xz\"/>\n" +
            "    </outboundRelationship>\n" +
            "</procedure>\n";

    public Procedure create() {
        Procedure p = new Procedure()
                .withClassCode( ActClass.PROC )
                .withMoodCode( ActMood.EVN )
                .withIds( new II().withRoot( "abc" ).withExtension( "d" ) )
                .withActivityTime( new SXCM_TS() )
                .withOid( "aaa" );

        Observation o = new Observation()
                .withClassCode( ActClass.OBS )
                .withMoodCode( ActMood.EVN )
                .withIds( new II().withRoot( "x" ).withExtension( "z" ) )
                .withValues( new PQ().withValue( "132" ).withUnit( "mg/l" ) )
                .withOid( "xz" );

        p.withOutboundRelationships(
                new ActRelationship()
                        .withTypeCode( ActRelationshipType.DEBIT )
                        .withTarget( o ),
                new ActRelationship()
                        .withTypeCode( ActRelationshipType.PART )
                        .withTarget( o )
        );

        return p;
    }

    public Procedure createSimple() {
        Procedure p = new Procedure()
                .withClassCode( ActClass.PROC )
                .withMoodCode( ActMood.EVN )
                .withIds( new II().withRoot( "abc" ).withExtension( "d" ) )
                .withActivityTime( new SXCM_TS() )
                .withOid( "odi_oid" );

        Observation o = new Observation()
                .withClassCode( ActClass.OBS )
                .withMoodCode( ActMood.EVN )
                .withIds( new II().withRoot( "x" ).withExtension( "z" ) )
                .withValues( new PQ().withValue( "132" ).withUnit( "mg/l" ) )
                .withOid( "xz" );

        p.withOutboundRelationships(
                new ActRelationship()
                        .withTypeCode( ActRelationshipType.DEBIT )
                        .withTarget( o )
        );

        return p;
    }

    protected void compareRoundTrip( Procedure p, Procedure z ) {
        assertNotSame( p, z );
        assertEquals( 2, z.getOutboundRelationships().size() );
        assertNotSame( p.getOutboundRelationships().get( 0 ), z.getOutboundRelationships().get( 0 ) );

        Observation o1 = (Observation) p.getOutboundRelationships().get( 0 ).getTarget();
        Observation o2 = (Observation) z.getOutboundRelationships().get( 0 ).getTarget();

        PQ pq1 = (PQ) o1.getValues().get( 0 );
        PQ pq2 = (PQ) o2.getValues().get( 0 );

        assertEquals( pq1.getValue(), pq2.getValue() );
    }


}