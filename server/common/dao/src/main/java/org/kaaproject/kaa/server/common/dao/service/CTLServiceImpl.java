/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.dao.service;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto.SYSTEM;
import static org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto.TENANT;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getStringFromFile;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateObject;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateSqlId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.common.core.algorithms.generation.ConfigurationGenerationException;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
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

@Service
@Transactional
public class CTLServiceImpl implements CTLService {

    private static final String JSON = "application/json";
    private static final String ZIP = "application/zip";
    private static final String VERSION = "version";
    private static final String FQN = "fqn";
    private static final Logger LOG = LoggerFactory.getLogger(CTLServiceImpl.class);
    private static final String DEPENDENCIES = "dependencies";
    private static final String GENERATED = "Generated";
    private static final String DEFAULT_SYSTEM_EMPTY_SCHEMA_FILE = "/default_system_empty_schema.avsc";

    private final LockOptions lockOptions = new LockOptions(LockMode.PESSIMISTIC_WRITE);

    /**
     * A template for naming exported CTL schemas.
     * 
     * @see #shallowExport(CTLSchemaDto)
     * @see #flatExport(CTLSchemaDto)
     */
    private static final String CTL_EXPORT_TEMPLATE = "{0}.v{1}.avsc";

    /**
     * The name of the archive to put exported CTL schemas into.
     * 
     * @see #deepExport(CTLSchemaDto)
     */
    private static final String CTL_EXPORT_ZIP_NAME = "schemas.zip";

    /**
     * Used to format CTL schema body.
     */
    private static final ObjectMapper FORMATTER = new ObjectMapper();

    @Autowired
    private CTLSchemaDao<CTLSchema> ctlSchemaDao;
    @Autowired
    private CTLSchemaMetaInfoDao<CTLSchemaMetaInfo> schemaMetaInfoDao;

    @Override
    public CTLSchemaDto getOrCreateEmptySystemSchema(String createdUsername) {
        CTLSchemaDto ctlSchema = findCTLSchemaByFqnAndVerAndTenantId(DEFAULT_SYSTEM_EMPTY_SCHEMA_FQN, DEFAULT_SYSTEM_EMPTY_SCHEMA_VERSION,
                null);
        if (ctlSchema == null) {
            CTLSchemaInfoDto schema = new CTLSchemaInfoDto();
            schema.setName(GENERATED);
            schema.setCreatedUsername(createdUsername);
            schema.setScope(CTLSchemaScopeDto.SYSTEM);
            schema.setFqn(DEFAULT_SYSTEM_EMPTY_SCHEMA_FQN);
            schema.setVersion(DEFAULT_SYSTEM_EMPTY_SCHEMA_VERSION);
            schema.setDependencies(new HashSet<CTLSchemaMetaInfoDto>());
            String body = getStringFromFile(DEFAULT_SYSTEM_EMPTY_SCHEMA_FILE, CTLServiceImpl.class);
            if (!body.isEmpty()) {
                schema.setBody(body);
            } else {
                throw new RuntimeException("Can't read default system schema."); // NOSONAR
            }
            ctlSchema = new CTLSchemaDto(schema, new HashSet<CTLSchemaDto>());
            ctlSchema = saveCTLSchema(ctlSchema);
        }
        return ctlSchema;
    }

    @Override
    public CTLSchemaDto saveCTLSchema(CTLSchemaDto unSavedSchema) {
        validateCTLSchemaObject(unSavedSchema);
        if (unSavedSchema.getDefaultRecord() == null) {
            unSavedSchema = generateDefaultRecord(unSavedSchema);
        } else {
            validateDefaultRecord(unSavedSchema);
        }
        if (isBlank(unSavedSchema.getId())) {
            CTLSchemaMetaInfoDto metaInfo = unSavedSchema.getMetaInfo();
            CTLSchemaScopeDto currentScope = metaInfo.getScope();
            CTLSchemaDto dto;
            synchronized (this) {
                boolean existing = false;
                CTLSchemaMetaInfo uniqueMetaInfo;
                try {
                    uniqueMetaInfo = schemaMetaInfoDao.save(new CTLSchemaMetaInfo(metaInfo));
                } catch (Exception e) {
                    existing = true;
                    uniqueMetaInfo = schemaMetaInfoDao.findByFqnAndVersion(metaInfo.getFqn(), metaInfo.getVersion());
                }
                if (uniqueMetaInfo == null) {
                    throw new DatabaseProcessingException("Can't save or find ctl meta information. Please check database state.");
                }
                schemaMetaInfoDao.lockRequest(lockOptions).setScope(true).lock(uniqueMetaInfo);

                switch (uniqueMetaInfo.getScope()) {
                case SYSTEM:
                    if (existing) {
                        throw new DatabaseProcessingException("Disable to store system ctl schema with same fqn and version.");
                    }
                case TENANT:
                    if (currentScope == SYSTEM && existing) {
                        throw new DatabaseProcessingException(
                                "Disable to store system ctl schema. Tenant's scope schema already exists with the same fqn and version.");
                    }
                    break;
                case APPLICATION:
                    break;
                default:
                    break;

                }
                CTLSchema ctlSchema = new CTLSchema(unSavedSchema);
                ctlSchema.setMetaInfo(uniqueMetaInfo);
                ctlSchema.setCreatedTime(System.currentTimeMillis());
                if (isBlank(unSavedSchema.getDescription())) {
                    ctlSchema.setDescription(GENERATED);
                }
                schemaMetaInfoDao.refresh(uniqueMetaInfo);
                schemaMetaInfoDao.incrementCount(uniqueMetaInfo);
                try {
                    dto = getDto(ctlSchemaDao.save(ctlSchema, true));
                } catch (DataIntegrityViolationException ex) {
                    throw new DatabaseProcessingException("Can't save cql schema with same fqn, version and tenant id.");
                } catch (Exception ex) {
                    throw new DatabaseProcessingException(ex);
                }
            }
            return dto;
        } else {
            return updateCTLSchema(unSavedSchema);
        }
    }

    private void validateDefaultRecord(CTLSchemaDto unSavedSchema) {
        try {
            String schemaBody = flatExportAsString(unSavedSchema);
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<GenericRecord>(schemaBody);
            converter.decodeJson(unSavedSchema.getDefaultRecord());
        } catch (IOException | RuntimeException e) {
            LOG.error("Invalid default record for CTL schema with body: {}", unSavedSchema.getBody(), e);
            throw new RuntimeException("An unexpected exception occured: " + e.toString());
        }
    }

    private CTLSchemaDto generateDefaultRecord(CTLSchemaDto unSavedSchema) {
        try {
            String schemaBody = flatExportAsString(unSavedSchema);
            LOG.debug("Generating default record for flat schema: {}", schemaBody);
            RawSchema dataSchema = new RawSchema(schemaBody);
            DefaultRecordGenerationAlgorithm<RawData> dataProcessor = new DefaultRecordGenerationAlgorithmImpl<RawSchema, RawData>(
                    dataSchema, new RawDataFactory());
            unSavedSchema.setDefaultRecord(dataProcessor.getRootData().getRawData());
            return unSavedSchema;
        } catch (ConfigurationGenerationException | IOException | RuntimeException e) {
            LOG.error("Failed to generate default record for CTL schema with body: {}", unSavedSchema.getBody(), e);
            throw new RuntimeException("An unexpected exception occured: " + e.toString());
        }
    }

    @Override
    public CTLSchemaDto updateCTLSchema(CTLSchemaDto ctlSchema) {
        validateCTLSchemaObject(ctlSchema);
        LOG.debug("Set new scope {} for ctl schema with id [{}]", ctlSchema.getId(), ctlSchema.getMetaInfo().getScope());
        CTLSchemaMetaInfoDto newMetaInfo = ctlSchema.getMetaInfo();
        CTLSchemaScopeDto newScope = newMetaInfo.getScope();
        if (!SYSTEM.equals(newScope)) {
            CTLSchema schema = ctlSchemaDao.findById(ctlSchema.getId());
            if (schema != null) {
                CTLSchemaMetaInfo metaInfo = schema.getMetaInfo();
                if (metaInfo.getScope().getLevel() <= newScope.getLevel()) {
                    CTLSchemaMetaInfo updated = schemaMetaInfoDao.updateScope(new CTLSchemaMetaInfo(newMetaInfo));
                    if (updated != null) {
                        schema.setMetaInfo(updated);
                    } else {
                        throw new DatabaseProcessingException("Can't update scope for ctl meta info.");
                    }
                } else if (metaInfo.getScope().getLevel() > newScope.getLevel()) {
                    throw new DatabaseProcessingException("Forbidden to update scope to lower level.");
                }
                updateEditableFields(ctlSchema, schema);
                return DaoUtil.getDto(ctlSchemaDao.save(schema));
            } else {
                throw new DatabaseProcessingException("Can't find ctl schema by id.");
            }
        } else {
            throw new DatabaseProcessingException("Forbidden to update existing scope to system.");
        }
    }

    @Override
    public void removeCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId) {
        if (isBlank(fqn) || version == null) {
            throw new IncorrectParameterException("Incorrect parameters for ctl schema request.");
        }
        LOG.debug("Remove ctl schema by fqn {} version {} and tenant id {}", fqn, version, tenantId);
        CTLSchema ctlSchema = ctlSchemaDao.findByFqnAndVerAndTenantId(fqn, version, tenantId);
        if (ctlSchema != null) {
            List<CTLSchema> dependsList = ctlSchemaDao.findDependentSchemas(ctlSchema.getStringId());
            if (dependsList.isEmpty()) {
                ctlSchemaDao.removeById(ctlSchema.getStringId());
            } else {
                throw new DatabaseProcessingException("Forbidden to delete ctl schema with relations.");
            }
        }
    }

    @Override
    public CTLSchemaDto findCTLSchemaById(String schemaId) {
        validateSqlId(schemaId, "Incorrect schema id for ctl request " + schemaId);
        LOG.debug("Find ctl schema by id [{}]", schemaId);
        return DaoUtil.getDto(ctlSchemaDao.findById(schemaId));
    }

    @Override
    public void removeCTLSchemaById(String schemaId) {
        validateSqlId(schemaId, "Incorrect schema id for ctl request  " + schemaId);
        LOG.debug("Remove ctl schema by id [{}]", schemaId);
        ctlSchemaDao.removeById(schemaId);
    }

    @Override
    public CTLSchemaDto findCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId) {
        if (isBlank(fqn) || version == null) {
            throw new IncorrectParameterException("Incorrect parameters for ctl schema request.");
        }
        LOG.debug("Find ctl schema by fqn {} version {} and tenant id {}", fqn, version, tenantId);
        return DaoUtil.getDto(ctlSchemaDao.findByFqnAndVerAndTenantId(fqn, version, tenantId));
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
        validateString(fqn, "Incorrect fqn for ctl schema request.");
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
        validateSqlId(appId, "Incorrect application id for ctl schema request.");
        LOG.debug("Find meta info for ctl schemas by application id {}", appId);
        return getMetaInfoFromCTLSchema(ctlSchemaDao.findByApplicationId(appId));
    }

    @Override
    public List<CTLSchemaMetaInfoDto> findTenantCTLSchemasMetaInfoByTenantId(String tenantId) {
        validateSqlId(tenantId, "Incorrect tenant id for ctl schema request.");
        LOG.debug("Find meta info for ctl schemas by tenant id {}", tenantId);
        return getMetaInfoFromCTLSchema(ctlSchemaDao.findTenantSchemasByTenantId(tenantId));
    }

    @Override
    public List<CTLSchemaMetaInfoDto> findAvailableCTLSchemasMetaInfo(String tenantId) {
        LOG.debug("Find system and tenant scopes ctl schemas by tenant id {}", tenantId);
        return getMetaInfoFromCTLSchema(ctlSchemaDao.findAvailableSchemas(tenantId));
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemaDependents(String schemaId) {
        validateSqlId(schemaId, "Incorrect schema id for ctl schema request.");
        LOG.debug("Find dependents schemas for schema with id [{}]", schemaId);
        List<CTLSchemaDto> list = Collections.emptyList();
        CTLSchema schemaDto = ctlSchemaDao.findById(schemaId);
        if (schemaDto != null) {
            list = convertDtoList(ctlSchemaDao.findDependentSchemas(schemaDto.getStringId()));
        }
        return list;
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemaDependents(String fqn, Integer version, String tenantId) {
        if (isBlank(fqn) || version == null) {
            throw new IncorrectParameterException("Incorrect parameters for ctl schema request.");
        }
        LOG.debug("Find dependents schemas for schema with fqn {} version {} and tenantId {}", fqn, version, tenantId);
        List<CTLSchemaDto> schemas = Collections.emptyList();
        CTLSchema schema = ctlSchemaDao.findByFqnAndVerAndTenantId(fqn, version, tenantId);
        if (schema != null) {
            schemas = convertDtoList(ctlSchemaDao.findDependentSchemas(schema.getStringId()));
        }
        return schemas;
    }

    private void validateCTLSchemaObject(CTLSchemaDto ctlSchema) {
        validateObject(ctlSchema, "Incorrect ctl schema object");
        CTLSchemaMetaInfoDto metaInfo = ctlSchema.getMetaInfo();
        if (metaInfo == null) {
            throw new RuntimeException("Incorrect ctl schema object. CTLSchemaMetaInfoDto is mandatory information.");
        } else {
            if (isBlank(metaInfo.getFqn()) || metaInfo.getVersion() == null) {
                throw new RuntimeException("Incorrect CTL meta information, please add correct version and fqn.");
            }
        }
    }

    private List<CTLSchemaMetaInfoDto> getMetaInfoFromCTLSchema(List<CTLSchema> schemas) {
        List<CTLSchemaMetaInfoDto> metaInfoDtoList = Collections.emptyList();
        if (!schemas.isEmpty()) {
            metaInfoDtoList = new ArrayList<>(schemas.size());
            for (CTLSchema schema : schemas) {
                CTLSchemaMetaInfoDto metaInfoDto = getDto(schema.getMetaInfo());
                metaInfoDto.setName(schema.getName());
                metaInfoDtoList.add(metaInfoDto);
            }
        }
        return metaInfoDtoList;
    }

    private void updateEditableFields(CTLSchemaDto src, CTLSchema dst) {
        CTLSchemaScopeDto scope = src.getMetaInfo().getScope();
        dst.setDescription(src.getDescription());
        dst.setName(src.getName());
        dst.setCreatedUsername(src.getCreatedUsername());
        String tenantId = src.getTenantId();
        if (isBlank(tenantId)) {
            if (SYSTEM.equals(scope)) {
                dst.setTenant(null);
            }
        }
        String appId = src.getApplicationId();
        if (isBlank(appId)) {
            if (SYSTEM.equals(scope) || TENANT.equals(scope)) {
                dst.setApplication(null);
            }
        }
    }

    @Override
    public FileData shallowExport(CTLSchemaDto schema) {
        try {
            FileData result = new FileData();
            result.setContentType(JSON);
            result.setFileName(MessageFormat.format(CTL_EXPORT_TEMPLATE, schema.getMetaInfo().getFqn(), schema.getMetaInfo().getVersion()));

            // Format schema body
            Object json = FORMATTER.readValue(schema.getBody(), Object.class);
            result.setFileData(FORMATTER.writerWithDefaultPrettyPrinter().writeValueAsString(json).getBytes());

            return result;
        } catch (Exception cause) {
            throw new RuntimeException("An unexpected exception occured: " + cause.toString());
        }
    }

    @Override
    public Schema flatExportAsSchema(CTLSchemaDto schema) {
        return this.parseDependencies(schema, new Schema.Parser());
    }

    @Override
    public String flatExportAsString(CTLSchemaDto schema) {
        return flatExportAsSchema(schema).toString();
    }

    @Override
    public FileData flatExport(CTLSchemaDto schema) {
        try {
            FileData result = new FileData();
            result.setContentType(JSON);
            result.setFileName(MessageFormat.format(CTL_EXPORT_TEMPLATE, schema.getMetaInfo().getFqn(), schema.getMetaInfo().getVersion()));

            // Get schema body
            String body = flatExportAsString(schema);

            // Format schema body
            Object json = FORMATTER.readValue(body, Object.class);
            result.setFileData(FORMATTER.writerWithDefaultPrettyPrinter().writeValueAsString(json).getBytes());

            return result;
        } catch (Exception cause) {
            throw new RuntimeException("An unexpected exception occured: " + cause.toString());
        }
    }

    @Override
    public FileData deepExport(CTLSchemaDto schema) {
        try {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            ZipOutputStream out = new ZipOutputStream(content);
            List<FileData> files = this.recursiveShallowExport(new ArrayList<FileData>(), schema);
            for (FileData file : files) {
                out.putNextEntry(new ZipEntry(file.getFileName()));
                out.write(file.getFileData());
                out.closeEntry();
            }
            out.close();

            FileData result = new FileData();
            result.setContentType(ZIP);
            result.setFileName(CTL_EXPORT_ZIP_NAME);
            result.setFileData(content.toByteArray());
            return result;
        } catch (Exception cause) {
            throw new RuntimeException("An unexpected exception occured: " + cause.toString());
        }
    }

    private Schema parseDependencies(CTLSchemaDto schema, final Schema.Parser parser) {
        if (schema.getDependencySet() != null) {
            for (CTLSchemaDto dependency : schema.getDependencySet()) {
                this.parseDependencies(dependency, parser);
            }
        }
        return parser.parse(schema.getBody());
    }

    private List<FileData> recursiveShallowExport(List<FileData> files, CTLSchemaDto parent) throws Exception {
        files.add(this.shallowExport(parent));
        ObjectNode object = new ObjectMapper().readValue(parent.getBody(), ObjectNode.class);
        ArrayNode dependencies = (ArrayNode) object.get(DEPENDENCIES);
        if (dependencies != null) {
            for (JsonNode node : dependencies) {
                ObjectNode dependency = (ObjectNode) node;
                String fqn = dependency.get(FQN).getTextValue();
                Integer version = dependency.get(VERSION).getIntValue();
                CTLSchemaDto child = this.findCTLSchemaByFqnAndVerAndTenantId(fqn, version, parent.getTenantId());
                this.recursiveShallowExport(files, child);
            }
        }
        return files;
    }

}
