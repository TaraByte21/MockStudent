import edu.asu.bmi.cad.SerializationHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.fail;

@RunWith( Parameterized.class )
public class XMLDocValidationTest extends AbstractTest {

    String xml;
    Schema schema = SerializationHelper.getV3Schema( SerializationHelper.DEFAULT_SCHEMA_LOC );

    public XMLDocValidationTest( String file ) {
        xml = file;
    }

    @Parameterized.Parameters
    public static Collection filesToValidate() {
        return Arrays.asList( new Object[][] {
                { "/test.xml" },
        } );
    }

    @Test
    public void testValidation() {
        try {
            Validator validator = schema.newValidator();
            validator.validate( new StreamSource( XMLDocValidationTest.class.getResourceAsStream( xml ) ) );
        } catch ( IOException | SAXException e ) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
    }
}
