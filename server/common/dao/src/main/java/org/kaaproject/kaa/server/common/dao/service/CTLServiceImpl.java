package org.kaaproject.kaa.server.common.dao.service;


import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaMetaInfoDao;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto.APPLICATION;
import static org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto.SYSTEM;
import static org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto.TENANT;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateObject;

@Service
public class CTLServiceImpl implements CTLService {

    private static final Logger LOG = LoggerFactory.getLogger(CTLServiceImpl.class);

    private final LockOptions lockOptions = new LockOptions(LockMode.PESSIMISTIC_WRITE);

    @Autowired
    private CTLSchemaDao<CTLSchema> ctlSchemaDao;
    @Autowired
    private CTLSchemaMetaInfoDao<CTLSchemaMetaInfo> schemaMetaInfoDao;

    @Override
    @Transactional
    public CTLSchemaDto saveCTLSchema(CTLSchemaDto unSavedSchema) {
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
        metaInfo.setScope(currentScope);

        CTLSchemaDto dto;
        synchronized (this) {
            CTLSchemaMetaInfo uniqueMetaInfo;
            try {
                uniqueMetaInfo = schemaMetaInfoDao.save(new CTLSchemaMetaInfo(metaInfo));
            } catch (Exception e) {
                uniqueMetaInfo = schemaMetaInfoDao.findByFqnAndVersion(metaInfo.getFqn(), metaInfo.getVersion());
            }
            schemaMetaInfoDao.lockRequest(lockOptions).setScope(true).lock(uniqueMetaInfo);

            switch (uniqueMetaInfo.getScope()) {
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
            schemaMetaInfoDao.incrementCount(uniqueMetaInfo);
            dto = getDto(ctlSchemaDao.save(ctlSchema, true));
        }
        return dto;
    }

    @Override
    public CTLSchemaDto updateCTLSchemaScope(CTLSchemaDto ctlSchema) {
        validateCTLSchemaObject(ctlSchema);
        LOG.debug("Set new scope {} for ctl schema with id [{}]", ctlSchema.getId(), ctlSchema.getMetaInfo().getScope());
        CTLSchemaMetaInfoDto newMetaInfo = ctlSchema.getMetaInfo();
        CTLSchemaScopeDto newScope = newMetaInfo.getScope();
        if (!CTLSchemaScopeDto.SYSTEM.equals(newScope)) {
            CTLSchema schema = ctlSchemaDao.findById(ctlSchema.getId());
            if (schema != null) {
                CTLSchemaMetaInfo metaInfo = schema.getMetaInfo();
                if (metaInfo.getScope().getLevel() < newScope.getLevel()) {
                    CTLSchemaMetaInfo updated = schemaMetaInfoDao.updateScope(new CTLSchemaMetaInfo(newMetaInfo));
                    if (updated != null) {
                        schema.setMetaInfo(updated);
                        return DaoUtil.getDto(schema);
                    } else {
                        throw new DatabaseProcessingException("Can't update scope for ctl meta info.");
                    }
                } else {
                    throw new DatabaseProcessingException("Forbidden to update scope to lower level.");
                }
            } else {
                throw new DatabaseProcessingException("Can't find ctl schema by id.");
            }
        } else {
            throw new DatabaseProcessingException("Forbidden to update existing scope to system.");
        }
    }

    // TODO: remove meta info if no used.
    @Override
    public void removeCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId) {
        LOG.debug("Remove ctl schema by fqn {} version {} and tenant id {}", fqn, version, tenantId);
        ctlSchemaDao.removeByFqnAndVerAndTenantId(fqn, version, tenantId);
    }

    @Override
    public CTLSchemaDto findCTLSchemaById(String schemaId) {
        LOG.debug("Find ctl schema by id [{}]", schemaId);
        return DaoUtil.getDto(ctlSchemaDao.findById(schemaId));
    }

    // TODO: remove meta info if no used.
    @Override
    public void removeCTLSchemaById(String schemaId) {
        LOG.debug("Remove ctl schema by id [{}]", schemaId);
        ctlSchemaDao.removeById(schemaId);
    }

    @Override
    public CTLSchemaDto findCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId) {
        LOG.debug("Find ctl schema by fqn {} version {} and tenant id {}", fqn, version, tenantId);
        return DaoUtil.getDto(ctlSchemaDao.findByFqnAndVerAndTenantId(fqn, version, tenantId));
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemasByApplicationId(String appId) {
        LOG.debug("Find ctl schemas by application id {}", appId);
        return convertDtoList(ctlSchemaDao.findByApplicationId(appId));
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemasByTenantId(String tenantId) {
        LOG.debug("Find ctl schemas by tenant id {}", tenantId);
        return convertDtoList(ctlSchemaDao.findByTenantId(tenantId));
    }

    @Override
    public List<CTLSchemaDto> findSystemCTLSchemas() {
        LOG.debug("Find system ctl schemas");
        return convertDtoList(ctlSchemaDao.findSystemSchemas());
    }

    @Override
    public List<CTLSchemaMetaInfoDto> findSystemCTLSchemasMetaInfo() {
        LOG.debug("Find meta info for system ctl schemas");
        return convertDtoList(schemaMetaInfoDao.findSystemSchemaMetaInfo());
    }

    @Override
    public CTLSchemaDto findLatestCTLSchemaByFqn(String fqn) {
        LOG.debug("Find latest ctl schema by fqn {}", fqn);
        return DaoUtil.getDto(ctlSchemaDao.findLatestByFqn(fqn));
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemas() {
        LOG.debug("Find all ctl schemas");
        return convertDtoList(ctlSchemaDao.find());
    }

    @Override
    public List<CTLSchemaMetaInfoDto> findCTLSchemasMetaInfoByApplicationId(String appId) {
        LOG.debug("Find meta info for ctl schemas by application id {}", appId);
        return getMetaInfoFromCTLSchema(ctlSchemaDao.findByApplicationId(appId));
    }

    @Override
    public List<CTLSchemaMetaInfoDto> findCTLSchemasMetaInfoByTenantId(String tenantId) {
        LOG.debug("Find meta info for ctl schemas by tenant id {}", tenantId);
        return getMetaInfoFromCTLSchema(ctlSchemaDao.findByTenantId(tenantId));
    }

    @Override
    public List<CTLSchemaDto> findAvailableCTLSchemas(String tenantId) {
        LOG.debug("Find system and tenant scopes ctl schemas by tenant id {}", tenantId);
        return convertDtoList(ctlSchemaDao.findAvailableSchemas(tenantId));
    }

    @Override
    public List<CTLSchemaMetaInfoDto> findAvailableCTLSchemasMetaInfo(String tenantId) {
        LOG.debug("Find system and tenant scopes ctl schemas by tenant id {}", tenantId);
        return getMetaInfoFromCTLSchema(ctlSchemaDao.findAvailableSchemas(tenantId));
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemaDependents(String schemaId) {
        LOG.debug("Find dependents schemas for schema with id [{}]", schemaId);
        List<CTLSchemaDto> list = Collections.emptyList();
        CTLSchema schemaDto = ctlSchemaDao.findById(schemaId);
        if (schemaDto != null) {
            list = convertDtoList(ctlSchemaDao.findDependentsSchemas(schemaDto.getId()));
        }
        return list;
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemaDependents(String fqn, int version, String tenantId) {
        LOG.debug("Find dependents schemas for schema with fqn {} version {} and tenantId {}", fqn, version, tenantId);
        List<CTLSchemaDto> schemas = Collections.emptyList();
        CTLSchema schema = ctlSchemaDao.findByFqnAndVerAndTenantId(fqn, version, tenantId);
        if (schema != null) {
            schemas = convertDtoList(ctlSchemaDao.findDependentsSchemas(schema.getId()));
        }
        return schemas;
    }

    private void validateCTLSchemaObject(CTLSchemaDto ctlSchema) {
        validateObject(ctlSchema, "Invalid ctl schema object");
        CTLSchemaMetaInfoDto metaInfo = ctlSchema.getMetaInfo();
        if (metaInfo == null) {
            throw new RuntimeException("Invalid ctl schema object. CTLSchemaMetaInfoDto is mandatory information.");
        }
    }

    private List<CTLSchemaMetaInfoDto> getMetaInfoFromCTLSchema(List<CTLSchema> schemas) {
        List<CTLSchemaMetaInfoDto> metaInfoDtoList = Collections.emptyList();
        if (!schemas.isEmpty()) {
            metaInfoDtoList = new ArrayList<>(schemas.size());
            for (CTLSchema schema : schemas) {
                metaInfoDtoList.add(getDto(schema.getMetaInfo()));
            }
        }
        return metaInfoDtoList;
    }
}
