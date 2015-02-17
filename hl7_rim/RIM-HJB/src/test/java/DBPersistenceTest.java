import org.hl7.v3.Act;
import org.hl7.v3.Observation;
import org.hl7.v3.PQ;
import org.hl7.v3.Procedure;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class DBPersistenceTest extends AbstractTest {


    private void persist( Object o, EntityManager em ) {
        em.getTransaction().begin();
        em.merge( o );
        em.getTransaction().commit();

        em.clear();
    }

    private Object refreshOnJPA( Class k, Object key, EntityManager em ) {
        Object x = null;

        em.getTransaction().begin();
        x = em.find( k, key );
        em.getTransaction().commit();

        return x;
    }


    @Test
    public void testRoundtripFromDB() {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(
                Act.class.getPackage().getName()
        );
        EntityManager em = emf.createEntityManager();

        Procedure p = create();
        persist( p, em );

        Procedure z = (Procedure) refreshOnJPA( p.getClass(), p.getOid(), em );

        compareRoundTrip( p, z );
        em.close();
    }

}
