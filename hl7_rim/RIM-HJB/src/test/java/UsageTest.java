import org.hl7.v3.ActClass;
import org.hl7.v3.ActRelationship;
import org.hl7.v3.ActRelationshipType;
import org.hl7.v3.CD;
import org.hl7.v3.Observation;
import org.hl7.v3.PQ;
import org.hl7.v3.PatientEncounter;
import org.hl7.v3.Procedure;
import org.hl7.v3.ST;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UsageTest extends AbstractTest {

    @Test
    public void testCreate() {
        PatientEncounter p = new PatientEncounter()
                .withClassCode( ActClass.ENC )
                .withCode( new CD().withCode( "a.b.c" ).withValueSet( "mayo" ) )
                .withInboundRelationships( new ActRelationship()
                                                   .withTypeCode( ActRelationshipType.APND )
                                                   .withLocalVariableName( new ST().withValue( "pod1Weight" ) )
                                                   .withTarget( new Observation()
                                                                        .withCode( new CD().withCode( "pod1Weight" ) )
                                                                        .withValues( new PQ().withValue( "75" ).withUnit( "kg" ) ) ) );

        assertNotNull( p );
    }

}
