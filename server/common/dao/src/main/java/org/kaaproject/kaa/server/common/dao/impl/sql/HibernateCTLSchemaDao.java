package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateCTLSchemaDao extends HibernateAbstractDao<CTLSchema> implements CTLSchemaDao<CTLSchema> {

    LockOptions lockOptions = new LockOptions(LockMode.PESSIMISTIC_WRITE);

    @Override
    public CTLSchema save(CTLSchema o) {
        Session session = getSession();
        CTLSchema t = (CTLSchema) session.merge(o);
        session.flush();
        return t;
    }

    @Override
    protected Class<CTLSchema> getEntityClass() {
        return CTLSchema.class;
    }
}
