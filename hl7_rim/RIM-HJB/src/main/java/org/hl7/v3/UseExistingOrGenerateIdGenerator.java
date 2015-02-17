package org.hl7.v3;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.SequenceGenerator;

import java.io.Serializable;
import java.util.UUID;

public class UseExistingOrGenerateIdGenerator extends SequenceGenerator {
    @Override
    public Serializable generate(SessionImplementor session, Object object)
            throws HibernateException {
        Serializable id = session.getEntityPersister(null, object)
                .getClassMetadata().getIdentifier(object, session);
        id = (id != null) ? id : UUID.randomUUID().toString();
        return id;
    }
}