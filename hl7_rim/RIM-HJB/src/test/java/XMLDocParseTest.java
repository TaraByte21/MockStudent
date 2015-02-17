import org.hl7.v3.Act;
import org.hl7.v3.INT;
import org.hl7.v3.Observation;
import edu.asu.bmi.cad.SerializationHelper;
import org.junit.Test;

import java.io.InputStream;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class XMLDocParseTest extends AbstractTest {

    String xml = "/test.xml";
    SerializationHelper<Act> helper = new SerializationHelper<Act>( SerializationHelper.getV3Schema() );

    public Act loadModel( InputStream source ) {
        return helper.fromXML( source );
    }


    @Test
    public void testParse() {
        try {
            Act a = loadModel( XMLDocParseTest.class.getResourceAsStream( xml ) );

            Act tgt = a.getOutboundRelationships().get( 0 ).getTarget();
            assertTrue( tgt instanceof Observation );

            Observation obs = (Observation) tgt;
            assertEquals( BigInteger.valueOf( 12 ), ( (INT) obs.getValues().get( 0 ) ).getValue() );
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
    }
}
