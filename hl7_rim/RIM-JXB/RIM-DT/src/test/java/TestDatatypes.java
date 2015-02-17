import org.hl7.v3.ADXP;
import org.hl7.v3.CD;
import org.hl7.v3.CodingRationale;
import org.hl7.v3.NullFlavor;
import org.hl7.v3.TEL;
import org.hl7.v3.URL;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDatatypes {

    @Test
    public void testCD() {
        String code = "abc";
        String name = "name";
        CD cd = new CD().withCode( code ).withDisplayName( name ).withCodingRationales( CodingRationale.O, CodingRationale.P );

        assertEquals( code, cd.getCode() );
        assertEquals( name, cd.getDisplayName() );
        assertEquals( 2, cd.getCodingRationales().size() );
        assertTrue( cd.getCodingRationales().contains( CodingRationale.O ) );
    }

    @Test
    public void testADXP() {
        String code = "abc";
        String name = "name";
        ADXP adxp = new ADXP().withCode( "aaa" ).withLanguage( "IT" ).withNullFlavor( NullFlavor.ASKU );


    }




    @Test
    public void testEnums() {
        assertEquals( 15, NullFlavor.values().length );
    }
}
