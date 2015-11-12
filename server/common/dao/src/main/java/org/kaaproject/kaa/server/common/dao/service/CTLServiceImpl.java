package org.kaaproject.kaa.server.common.dao.service;


import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaMetaInfoDao;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto.APPLICATION;
import static org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto.SYSTEM;
import static org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto.TENANT;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateObject;

@Service
@Transactional
public class CTLServiceImpl implements CTLService {

    private static final Logger LOG = LoggerFactory.getLogger(CTLServiceImpl.class);

    @Autowired
    private CTLSchemaDao<CTLSchema> ctlSchemaDao;
    @Autowired
    private CTLSchemaMetaInfoDao<CTLSchemaMetaInfo> schemaMetaInfoDao;

    Object huita = new Object();

    public CTLSchemaDto saveCTLSchema(CTLSchemaDto unSavedSchema) {
        CTLSchemaDto savedCtlSchemaDto;
        synchronized (huita) {
            schemaMetaInfoDao.refreshSession();
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

            CTLSchemaMetaInfo uniqueMetaInfo;
            try {

                uniqueMetaInfo = schemaMetaInfoDao.findByFqnAndVersion(metaInfo.getFqn(), metaInfo.getVersion());
                if (uniqueMetaInfo == null) {
                    uniqueMetaInfo = schemaMetaInfoDao.save(new CTLSchemaMetaInfo(metaInfo), true);
                }
            } catch (DataIntegrityViolationException ex) {
                LOG.warn("---> Got DataIntegrityViolationException. Its ok for multy thread");
//            schemaMetaInfoDao.refreshSession();
                uniqueMetaInfo = schemaMetaInfoDao.findByFqnAndVersion(metaInfo.getFqn(), metaInfo.getVersion());
            } catch (Exception e) {
                throw new RuntimeException("Database processing exception.");
            }
            LockOptions lockOptions = new LockOptions(LockMode.PESSIMISTIC_WRITE);
            lockOptions.setTimeOut(1000);

            schemaMetaInfoDao.lock(uniqueMetaInfo, lockOptions);

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
        }
        return savedCtlSchemaDto;
    }

    @Override
    public CTLSchemaDto updateCTLSchemaScope(CTLSchemaDto ctlSchema) {
        return DaoUtil.getDto(ctlSchemaDao.updateScope(ctlSchema));
    }

    @Override
    public void removeCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId) {
        ctlSchemaDao.removeByFqnAndVerAndTenantId(fqn, version, tenantId);
    }

    @Override
    public CTLSchemaDto findCTLSchemaById(String schemaId) {
        return DaoUtil.getDto(ctlSchemaDao.findById(schemaId));
    }

    @Override
    public void removeCTLSchemaById(String schemaId) {
        ctlSchemaDao.removeById(schemaId);
    }

    @Override
    public CTLSchemaDto findCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId) {
        return DaoUtil.getDto(ctlSchemaDao.findByFqnAndVerAndTenantId(fqn, version, tenantId));
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemasByApplicationId(String appId) {
        return DaoUtil.convertDtoList(ctlSchemaDao.findByApplicationId(appId));
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemasByTenantId(String tenantId) {
        return DaoUtil.convertDtoList(ctlSchemaDao.findByTenantId(tenantId));
    }

    @Override
    public List<CTLSchemaDto> findSystemCTLSchemas() {
        return DaoUtil.convertDtoList(ctlSchemaDao.findSystemSchemas());
    }

    @Override
    public CTLSchemaDto findLatestCTLSchemaByFqn(String fqn) {
        return DaoUtil.getDto(ctlSchemaDao.findLatestByFqn(fqn));
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemas() {
        return DaoUtil.convertDtoList(ctlSchemaDao.find());
    }

    private void validateCTLSchemaObject(CTLSchemaDto ctlSchema) {
        validateObject(ctlSchema, "Error");
        CTLSchemaMetaInfoDto metaInfo = ctlSchema.getMetaInfo();
        if (metaInfo == null) {
            throw new RuntimeException("Invalid object ");
        }
    }
}
