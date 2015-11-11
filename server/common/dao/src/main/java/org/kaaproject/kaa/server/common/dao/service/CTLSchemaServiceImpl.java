package org.kaaproject.kaa.server.common.dao.service;


import org.hibernate.LockOptions;
import org.hibernate.exception.ConstraintViolationException;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaMetaInfoDao;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto.APPLICATION;
import static org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto.SYSTEM;
import static org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto.TENANT;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateObject;

@Service
@Transactional
public class CTLSchemaServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(CTLSchemaServiceImpl.class);

    @Autowired
    private CTLSchemaDao<CTLSchema> ctlSchemaDao;
    @Autowired
    private CTLSchemaMetaInfoDao<CTLSchemaMetaInfo> schemaMetaInfoDao;

    public CTLSchemaDto save(CTLSchemaDto unSavedSchema) {
        CTLSchemaDto savedCtlSchemaDto = null;
        validateCTLSchemaObject(unSavedSchema);
        CTLSchemaMetaInfoDto metaInfo = unSavedSchema.getMetaInfo();

        CTLSchemaScopeDto currentScope = null;
        if (isBlank(unSavedSchema.getTenantId())) {
            currentScope = SYSTEM;
        } else if (!isBlank(unSavedSchema.getTenantId())) {
            currentScope = TENANT;
        } else if (!isBlank(unSavedSchema.getAppId())) {
            currentScope = APPLICATION;
        }
        metaInfo.setSchemaScopeDto(currentScope);

        CTLSchemaMetaInfo uniqueMetaInfo = null;
        try {
            uniqueMetaInfo = schemaMetaInfoDao.findByFqnAndVersion(metaInfo.getFqn(), metaInfo.getVersion());
            if (uniqueMetaInfo == null) {
                synchronized (schemaMetaInfoDao) {
                    uniqueMetaInfo = schemaMetaInfoDao.save(new CTLSchemaMetaInfo(metaInfo));
                }
            }
        } catch (Exception e) {
            LOG.warn("---> Got exception", e);
            uniqueMetaInfo = schemaMetaInfoDao.findByFqnAndVersion(metaInfo.getFqn(), metaInfo.getVersion());
        }
        schemaMetaInfoDao.lock(uniqueMetaInfo, LockOptions.READ);

        switch (uniqueMetaInfo.getSchemaScopeDto()) {
            case SYSTEM:
                throw new RuntimeException("Disable to store system ctl schema with same fqn and version.");
            case TENANT:
                if (currentScope == SYSTEM) {
                    throw new RuntimeException("Disable to store system ctl schema. Tenant's scope schema already exists with the same fqn and version.");
                }
                break;
            case APPLICATION:
                break;
            default:
                break;

        }
        CTLSchema ctlSchema = new CTLSchema(unSavedSchema);
        ctlSchema.setMetaInfo(uniqueMetaInfo);
        savedCtlSchemaDto = getDto(ctlSchemaDao.save(ctlSchema));
        uniqueMetaInfo.incrementCount();
        return savedCtlSchemaDto;
    }

    private void validateCTLSchemaObject(CTLSchemaDto ctlSchema) {
        validateObject(ctlSchema, "Error");
        CTLSchemaMetaInfoDto metaInfo = ctlSchema.getMetaInfo();
        if (metaInfo == null) {
            throw new RuntimeException("Invalid object ");
        }
    }
}
