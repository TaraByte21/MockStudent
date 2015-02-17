import org.hl7.v3.Procedure;
import edu.asu.bmi.cad.SerializationHelper;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class JSONTest extends AbstractTest {

    @Test
    public void testCyclic() {
        Procedure p = create();

        SerializationHelper<Procedure> helper = new SerializationHelper<>( SerializationHelper.getV3Schema() );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        helper.toJSON( p, baos );
;
        System.out.println( new String( baos.toByteArray() ) );

        Procedure q = helper.fromJson( new ByteArrayInputStream( baos.toByteArray() ) );

        compareRoundTrip( p, q );
    }
}
