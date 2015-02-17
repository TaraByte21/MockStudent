package org.hl7.v3;

import java.io.Serializable;
import java.util.List;

import com.sun.xml.bind.CycleRecoverable;
import edu.asu.bmi.cad.AbstractSerializationHelper;
import edu.asu.bmi.cad.SerializationHelper;


@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractRoot
        implements Serializable, CycleRecoverable
{

    private static final long serialVersionUID = -540118664766022861L;


    /**
     * This should be overridden.
     */
    public List<II> getIds() {
        return null;
    }

    private II getPrimaryId() {
        List<II> ids = this.getIds();
        if( ids == null || ids.isEmpty() ) {
            return null;
        } else {
            return ids.get( 0 );
        }
    }

    @Override
    public int hashCode() {
        II primaryId = this.getPrimaryId();
        if(primaryId == null) {
            return super.hashCode();
        } else {
            return primaryId.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if(! (obj instanceof AbstractRoot)) {
            return false;
        }

        AbstractRoot other = (AbstractRoot) obj;
        II thisPrimaryId = this.getPrimaryId();
        II otherPrimaryId = other.getPrimaryId();

        if(thisPrimaryId == null || otherPrimaryId == null) {
            return false;
        }

        return thisPrimaryId.equals(otherPrimaryId);
    }


    @Override
    public Object onCycleDetected(Context context) {
        AbstractSerializationHelper helper = context.getMarshaller().getAdapter( SerializationHelper.class );
        try {
            return helper.marshal( this );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}