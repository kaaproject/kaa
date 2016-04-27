/*
 * Copyright 2014-2016 CyberVision, Inc.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang.Validate;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
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
    private CTLSchemaMetaInfoDao<CTLSchemaMetaInfo> ctlSchemaMetaInfoDao;

    @Override
    public CTLSchemaDto getOrCreateEmptySystemSchema(String createdUsername) {
        CTLSchemaDto ctlSchema = findCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(DEFAULT_SYSTEM_EMPTY_SCHEMA_FQN, DEFAULT_SYSTEM_EMPTY_SCHEMA_VERSION,
                null, null);
        if (ctlSchema == null) {
            ctlSchema = new CTLSchemaDto();
            CTLSchemaMetaInfoDto metaInfo = new CTLSchemaMetaInfoDto(DEFAULT_SYSTEM_EMPTY_SCHEMA_FQN);
            ctlSchema.setMetaInfo(metaInfo);
            ctlSchema.setVersion(DEFAULT_SYSTEM_EMPTY_SCHEMA_VERSION);
            ctlSchema.setCreatedUsername(createdUsername);
            ctlSchema.setDependencySet(new HashSet<CTLSchemaDto>());
            String body = getStringFromFile(DEFAULT_SYSTEM_EMPTY_SCHEMA_FILE, CTLServiceImpl.class);
            if (!body.isEmpty()) {
                ctlSchema.setBody(body);
            } else {
                throw new RuntimeException("Can't read default system schema."); // NOSONAR
            }
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
            CTLSchemaDto dto;
            synchronized (this) {
                List<CTLSchemaMetaInfo> existingFqns = ctlSchemaMetaInfoDao.findExistingFqns(metaInfo.getFqn(), metaInfo.getTenantId(), metaInfo.getApplicationId());
                if (existingFqns != null && !existingFqns.isEmpty()) {
                    throw new DatabaseProcessingException("Can't save common type due to an FQN conflict.");
                }
                metaInfo.setId(null);
                CTLSchemaMetaInfo uniqueMetaInfo = ctlSchemaMetaInfoDao.save(new CTLSchemaMetaInfo(metaInfo));
                ctlSchemaMetaInfoDao.lockRequest(lockOptions).setScope(true).lock(uniqueMetaInfo);
                CTLSchema ctlSchema = new CTLSchema(unSavedSchema);
                ctlSchema.setMetaInfo(uniqueMetaInfo);
                ctlSchema.setCreatedTime(System.currentTimeMillis());
                ctlSchemaMetaInfoDao.refresh(uniqueMetaInfo);
                try {
                    dto = getDto(ctlSchemaDao.save(ctlSchema, true));
                } catch (DataIntegrityViolationException ex) {
                    throw new DatabaseProcessingException("Can't save common type: such FQN and version already exist.");
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
        } catch (StackOverflowError e) {
            LOG.error("Failed to generate default record. An endless recursion is detected. CTL schema body: {}", unSavedSchema.getBody(), e);
            throw new RuntimeException("Unable to generate default record. An endless recursion is detected! "
                    + "Please check non-optional references to nested types.");
        } catch (ConfigurationGenerationException | IOException | RuntimeException e) {
            LOG.error("Failed to generate default record for CTL schema with body: {}", unSavedSchema.getBody(), e);
            throw new RuntimeException("An unexpected exception occured: " + e.toString());
        }
    }

    @Override
    public CTLSchemaDto updateCTLSchema(CTLSchemaDto ctlSchema) {
        validateCTLSchemaObject(ctlSchema);
        LOG.debug("Update ctl schema with id [{}]", ctlSchema.getId());
        CTLSchema schema = ctlSchemaDao.findById(ctlSchema.getId());
        if (schema != null) {
            synchronized (this) {
                if (ctlSchema.getVersion() != schema.getVersion()) {
                    throw new DatabaseProcessingException("Can't change version of existing common type version.");
                }
                CTLSchemaMetaInfo metaInfo = schema.getMetaInfo();
                if (!ctlSchema.getMetaInfo().equals(metaInfo.toDto())) {
                    throw new DatabaseProcessingException("Can't update scope of existing common type version within update procedure.");
                }
                ctlSchemaMetaInfoDao.lockRequest(lockOptions).setScope(true).lock(metaInfo);
                schema.update(ctlSchema);
                return DaoUtil.getDto(ctlSchemaDao.save(schema, true));
            }
        } else {
            throw new DatabaseProcessingException("Can't find common type version by id.");
        }
    }

    @Override
    public CTLSchemaMetaInfoDto updateCTLSchemaMetaInfoScope(CTLSchemaMetaInfoDto ctlSchemaMetaInfo) {
        validateObject(ctlSchemaMetaInfo, "Incorrect ctl schema meta info object");
        LOG.debug("Update ctl schema meta info scope with id [{}]", ctlSchemaMetaInfo.getId());
        CTLSchemaMetaInfo schemaMetaInfo = ctlSchemaMetaInfoDao.findById(ctlSchemaMetaInfo.getId());
        if (schemaMetaInfo != null) {
            synchronized (this) {
                ctlSchemaMetaInfoDao.lockRequest(lockOptions).setScope(true).lock(schemaMetaInfo);
                if (checkScopeUpdate(ctlSchemaMetaInfo, schemaMetaInfo.toDto())) {
                    List<CTLSchemaMetaInfo> others = ctlSchemaMetaInfoDao.findOthersByFqnAndTenantId(ctlSchemaMetaInfo.getFqn(), ctlSchemaMetaInfo.getTenantId(), ctlSchemaMetaInfo.getId());
                    if (others != null && !others.isEmpty()) {
                        throw new DatabaseProcessingException("Can't update scope of the common type due to an FQN conflict.");
                    }
                    schemaMetaInfo = ctlSchemaMetaInfoDao.updateScope(new CTLSchemaMetaInfo(ctlSchemaMetaInfo));
                }
                return DaoUtil.getDto(schemaMetaInfo);
            }            
        } else {
            throw new DatabaseProcessingException("Can't find common type by id.");
        }
    }

    @Override
    public List<CTLSchemaMetaInfoDto> findSiblingsByFqnTenantIdAndApplicationId(String fqn, String tenantId, String applicationId) {
        if (isBlank(fqn)) {
            throw new IncorrectParameterException("Incorrect parameters for ctl schema request.");
        }
        LOG.debug("Find sibling ctl schemas by fqn {}, tenant id {} and application id {}", fqn, tenantId, applicationId);
        return convertDtoList(ctlSchemaMetaInfoDao.findSiblingsByFqnTenantIdAndApplicationId(fqn, tenantId, applicationId));
    }

    private boolean checkScopeUpdate(CTLSchemaMetaInfoDto newSchemaMetaInfo, CTLSchemaMetaInfoDto prevSchemaMetaInfo) {
        if (!newSchemaMetaInfo.equals(prevSchemaMetaInfo)) {
            if (isBlank(newSchemaMetaInfo.getFqn())) {
                throw new DatabaseProcessingException("FQN can't be empty.");
            }
            if (!newSchemaMetaInfo.getFqn().equals(prevSchemaMetaInfo.getFqn())) {
                throw new DatabaseProcessingException("Can't change FQN of the existing common type.");
            }
            CTLSchemaScopeDto newScope = newSchemaMetaInfo.getScope();
            CTLSchemaScopeDto prevScope = prevSchemaMetaInfo.getScope();
            if (newScope != prevScope) {
                if (SYSTEM.equals(newScope)) {
                    throw new DatabaseProcessingException("Can't update scope to system.");
                }
                if (newScope.getLevel() > prevScope.getLevel()) {
                    throw new DatabaseProcessingException("Can't update scope to lower.");
                }
            }
            if (!isBlank(newSchemaMetaInfo.getTenantId()) && 
                    !newSchemaMetaInfo.getTenantId().equals(prevSchemaMetaInfo.getTenantId())) {
                throw new DatabaseProcessingException("Can't change tenant reference for the existing common type.");
            }
            if (!isBlank(newSchemaMetaInfo.getApplicationId()) && 
                    !newSchemaMetaInfo.getApplicationId().equals(prevSchemaMetaInfo.getApplicationId())) {
                throw new DatabaseProcessingException("Can't change application reference for the existing common type.");
            }
            return true;
        }
        return false;
    }

    @Override
    public void removeCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) {
        if (isBlank(fqn) || version == null) {
            throw new IncorrectParameterException("Incorrect parameters for ctl schema request.");
        }
        LOG.debug("Remove ctl schema by fqn {} version {}, tenant id {} and application id {}", fqn, version, tenantId, applicationId);
        CTLSchema ctlSchema = ctlSchemaDao.findByFqnAndVerAndTenantIdAndApplicationId(fqn, version, tenantId, applicationId);
        if (ctlSchema != null) {
            List<CTLSchema> dependsList = ctlSchemaDao.findDependentSchemas(ctlSchema.getStringId());
            if (dependsList.isEmpty()) {
                synchronized (this) {
                    CTLSchemaMetaInfo metaInfo = ctlSchema.getMetaInfo();
                    ctlSchemaMetaInfoDao.lockRequest(lockOptions).setScope(true).lock(metaInfo);
                    try {
                        ctlSchemaDao.removeById(ctlSchema.getStringId());
                        List<CTLSchema> schemas = ctlSchemaDao.findAllByMetaInfoId(metaInfo.getStringId());
                        if (schemas == null || schemas.isEmpty()) {
                            ctlSchemaMetaInfoDao.removeById(metaInfo.getStringId());
                        }
                    } catch (DataIntegrityViolationException ex) {
                        throw new DatabaseProcessingException("Common type version can't be deleted because it is referenced by system modules.");
                    }
                }
            } else {
                throw new DatabaseProcessingException("Common type version can't be deleted because it is referenced by other types.");
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
    public CTLSchemaDto findCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) {
        if (isBlank(fqn) || version == null) {
            throw new IncorrectParameterException("Incorrect parameters for ctl schema request.");
        }
        LOG.debug("Find ctl schema by fqn {} version {}, tenant id {} and application id {}", fqn, version, tenantId, applicationId);
        return DaoUtil.getDto(ctlSchemaDao.findByFqnAndVerAndTenantIdAndApplicationId(fqn, version, tenantId, applicationId));
    }

    @Override
    public CTLSchemaDto findByMetaInfoIdAndVer(String metaInfoId, Integer version) {
        if (isBlank(metaInfoId) || version == null) {
            throw new IncorrectParameterException("Incorrect parameters for ctl schema request.");
        }
        LOG.debug("Find ctl schema by meta info id {} and version {}", metaInfoId, version);
        return DaoUtil.getDto(ctlSchemaDao.findByMetaInfoIdAndVer(metaInfoId, version));
    }

    @Override
    public CTLSchemaDto findAnyCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(String fqn, Integer version,
            String tenantId, String applicationId) {
        if (isBlank(fqn) || version == null) {
            throw new IncorrectParameterException("Incorrect parameters for ctl schema request.");
        }
        LOG.debug("Find any ctl schema by fqn {} version {}, tenant id {} and application id {}", fqn, version, tenantId, applicationId);
        return DaoUtil.getDto(ctlSchemaDao.findAnyByFqnAndVerAndTenantIdAndApplicationId(fqn, version, tenantId, applicationId));
    }

    @Override
    public List<CTLSchemaDto> findSystemCTLSchemas() {
        LOG.debug("Find system ctl schemas");
        return convertDtoList(ctlSchemaDao.findSystemSchemas());
    }

    @Override
    public List<CTLSchemaMetaInfoDto> findSystemCTLSchemasMetaInfo() {
        LOG.debug("Find system ctl schemas");
        return getMetaInfoFromCTLSchema(ctlSchemaDao.findSystemSchemas());
    }

    @Override
    public List<CTLSchemaMetaInfoDto> findAvailableCTLSchemasMetaInfoForTenant(String tenantId) {
        LOG.debug("Find system and tenant scopes ctl schemas by tenant id {}", tenantId);
        return getMetaInfoFromCTLSchema(ctlSchemaDao.findAvailableSchemasForTenant(tenantId));
    }

    @Override
    public List<CTLSchemaMetaInfoDto> findAvailableCTLSchemasMetaInfoForApplication(String tenantId, String applicationId) {
        LOG.debug("Find system, tenant and application scopes ctl schemas by application id {}", applicationId);
        return getMetaInfoFromCTLSchema(ctlSchemaDao.findAvailableSchemasForApplication(tenantId, applicationId));
    }

    @Override
    public CTLSchemaDto findLatestCTLSchemaByFqnAndTenantIdAndApplicationId(String fqn, String tenantId, String applicationId) {
        validateString(fqn, "Incorrect fqn for ctl schema request.");
        LOG.debug("Find latest ctl schema by fqn {}, tenantId {} and applicationId {}", fqn, tenantId, applicationId);
        return DaoUtil.getDto(ctlSchemaDao.findLatestByFqnAndTenantIdAndApplicationId(fqn, tenantId, applicationId));
    }

    @Override
    public CTLSchemaDto findLatestByMetaInfoId(String metaInfoId) {
        validateString(metaInfoId, "Incorrect meta info id for ctl schema request.");
        LOG.debug("Find latest ctl schema by meta info id {}", metaInfoId);
        return DaoUtil.getDto(ctlSchemaDao.findLatestByMetaInfoId(metaInfoId));
    }

    @Override
    public List<CTLSchemaDto> findAllCTLSchemasByFqnAndTenantIdAndApplicationId(String fqn, String tenantId, String applicationId) {
        validateString(fqn, "Incorrect fqn for ctl schema request.");
        LOG.debug("Find all ctl schemas by fqn {}, tenantId {} and applicationId {}", fqn, tenantId, applicationId);
        return convertDtoList(ctlSchemaDao.findAllByFqnAndTenantIdAndApplicationId(fqn, tenantId, applicationId));
    }

    @Override
    public List<CTLSchemaDto> findCTLSchemas() {
        LOG.debug("Find all ctl schemas");
        return convertDtoList(ctlSchemaDao.find());
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
    public List<CTLSchemaDto> findCTLSchemaDependents(String fqn, Integer version, String tenantId, String applicationId) {
        if (isBlank(fqn) || version == null) {
            throw new IncorrectParameterException("Incorrect parameters for ctl schema request.");
        }
        LOG.debug("Find dependents schemas for schema with fqn {} version {}, tenantId {} and applicationId ()", fqn, version, tenantId, applicationId);
        List<CTLSchemaDto> schemas = Collections.emptyList();
        CTLSchema schema = ctlSchemaDao.findByFqnAndVerAndTenantIdAndApplicationId(fqn, version, tenantId, applicationId);
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
            if (isBlank(metaInfo.getFqn()) || ctlSchema.getVersion() == null) {
                throw new RuntimeException("Incorrect CTL schema, please add correct version and fqn.");
            }
        }
    }

    private List<CTLSchemaMetaInfoDto> getMetaInfoFromCTLSchema(List<CTLSchema> schemas) {
        Map<String, CTLSchemaMetaInfoDto> metaInfoMap = new HashMap<>();
        if (!schemas.isEmpty()) {
            for (CTLSchema schema : schemas) {
                String metaInfoId = schema.getMetaInfo().getStringId();
                CTLSchemaMetaInfoDto metaInfoDto = metaInfoMap.get(metaInfoId);
                if (metaInfoDto == null) {
                    metaInfoDto = getDto(schema.getMetaInfo());
                    metaInfoMap.put(metaInfoId, metaInfoDto);
                }
                List<Integer> versions = metaInfoDto.getVersions();
                if (versions == null) {
                    versions = new ArrayList<>();
                    metaInfoDto.setVersions(versions);
                }
                versions.add(schema.getVersion());
            }
        }
        List<CTLSchemaMetaInfoDto> result = new ArrayList<>(metaInfoMap.values());
        return result;
    }

    @Override
    public FileData shallowExport(CTLSchemaDto schema) {
        try {
            FileData result = new FileData();
            result.setContentType(JSON);
            result.setFileName(MessageFormat.format(CTL_EXPORT_TEMPLATE, schema.getMetaInfo().getFqn(), schema.getVersion()));

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
        try {
            return this.parseDependencies(schema, new Schema.Parser());
        } catch (Exception cause) {
            LOG.error("Unable to export CTL schema as flat: {}", schema, cause);
            throw new RuntimeException("An unexpected exception occured: " + cause.toString());
        }
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
            result.setFileName(MessageFormat.format(CTL_EXPORT_TEMPLATE, schema.getMetaInfo().getFqn(), schema.getVersion()));

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

    private Schema parseDependencies(CTLSchemaDto schema, final Schema.Parser parser) throws Exception {
        if (schema.getDependencySet() != null) {
            for (CTLSchemaDto dependency : schema.getDependencySet()) {
                this.parseDependencies(dependency, parser);
            }
        }
        ObjectNode object = new ObjectMapper().readValue(schema.getBody(), ObjectNode.class);
        object.remove(DEPENDENCIES);
        String body = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        return parser.parse(body);
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
                CTLSchemaDto child = this.findAnyCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(
                        fqn, version, parent.getMetaInfo().getTenantId(), parent.getMetaInfo().getApplicationId());
                Validate.notNull(child, MessageFormat.format("The dependency [{0}] was not found!", fqn));
                this.recursiveShallowExport(files, child);
            }
        }
        return files;
    }
}
