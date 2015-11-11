package org.kaaproject.kaa.server.common.dao.service;


import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CTLSchemaServiceImpl {

    @Autowired
    private CTLSchemaDao<CTLSchema> ctlSchemaDao;

    public CTLSchema save(CTLSchema ctlSchema) {
        return ctlSchemaDao.save(ctlSchema);
    }
}
