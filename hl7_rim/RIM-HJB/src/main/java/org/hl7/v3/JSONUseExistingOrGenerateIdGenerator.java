package org.hl7.v3;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;

import java.util.UUID;


public class JSONUseExistingOrGenerateIdGenerator extends ObjectIdGenerator<String> {

    @Override
    public Class<?> getScope() {
        return Object.class;
    }

    @Override
    public boolean canUseFor( ObjectIdGenerator<?> objectIdGenerator ) {
        return (objectIdGenerator.getClass() == getClass());
    }

    @Override
    public ObjectIdGenerator<String> forScope( Class<?> aClass ) {
        return this;
    }

    @Override
    public ObjectIdGenerator<String> newForSerialization( Object o ) {
        return this;
    }

    @Override
    public IdKey key( Object o ) {
        return new IdKey( getClass(), null, o );
    }

    @Override
    public String generateId( Object o ) {
        if ( o instanceof InfrastructureRoot ) {
            InfrastructureRoot ir = (InfrastructureRoot) o;
            if ( ir.getOid() != null ) {
                return ir.getOid();
            }
        }
        return UUID.randomUUID().toString();
    }
}
