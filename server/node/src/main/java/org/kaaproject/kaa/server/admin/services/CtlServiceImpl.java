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

package org.kaaproject.kaa.server.admin.services;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

import net.iharder.Base64;

import org.apache.avro.Schema;
import org.kaaproject.avro.ui.converter.SchemaFormAvroConverter;
import org.kaaproject.avro.ui.shared.Fqn;
import org.kaaproject.avro.ui.shared.FqnVersion;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.common.dto.ctl.CtlSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.admin.services.entity.AuthUserDto;
import org.kaaproject.kaa.server.admin.services.schema.CtlSchemaParser;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaExportKey;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;
import org.kaaproject.kaa.server.admin.shared.schema.EventClassViewDto;
import org.kaaproject.kaa.server.admin.shared.services.CtlService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.kaaproject.kaa.server.control.service.exception.ControlServiceException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("ctlService")
public class CtlServiceImpl extends AbstractAdminService implements CtlService {

  @Override
  public CTLSchemaDto saveCtlSchema(CTLSchemaDto schema) throws KaaAdminServiceException {
    this.checkAuthority(KaaAuthorityDto.values());
    try {
      Utils.checkNotNull(schema);
      checkCtlSchemaVersion(schema.getVersion());
      checkCtlSchemaEditScope(
          schema.getMetaInfo().getTenantId(), schema.getMetaInfo().getApplicationId());

      // Check if the schema dependencies are present in the database
      List<FqnVersion> missingDependencies = new ArrayList<>();
      Set<CTLSchemaDto> dependencies = new HashSet<>();
      if (schema.getDependencySet() != null) {
        for (CTLSchemaDto dependency : schema.getDependencySet()) {
          CTLSchemaDto schemaFound =
              controlService.getAnyCtlSchemaByFqnVersionTenantIdAndApplicationId(
                  dependency.getMetaInfo().getFqn(), dependency.getVersion(),
                  schema.getMetaInfo().getTenantId(), schema.getMetaInfo().getApplicationId());
          if (schemaFound == null) {
            missingDependencies.add(
                new FqnVersion(dependency.getMetaInfo().getFqn(), dependency.getVersion()));
          } else {
            dependencies.add(schemaFound);
          }
        }
      }
      if (!missingDependencies.isEmpty()) {
        String message = "The following dependencies are missing from the database: "
            + Arrays.toString(missingDependencies.toArray());
        throw new IllegalArgumentException(message);
      }

      // Check if the schema body is valid
      CtlSchemaParser parser = new CtlSchemaParser(
          controlService, schema.getMetaInfo().getTenantId());
      parser.validate(schema);

      CTLSchemaDto result = controlService.saveCtlSchema(schema);
      return result;
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public CTLSchemaDto saveCtlSchema(String body, String tenantId, String applicationId)
      throws KaaAdminServiceException {
    this.checkAuthority(KaaAuthorityDto.values());
    try {
      checkCtlSchemaEditScope(tenantId, applicationId);
      CtlSchemaParser parser = new CtlSchemaParser(controlService, tenantId);
      CTLSchemaDto schema = parser.parse(body, applicationId);
      checkCtlSchemaVersion(schema.getVersion());
      // Check if the schema body is valid
      parser.validate(schema);
      CTLSchemaDto result = controlService.saveCtlSchema(schema);
      return result;
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public CTLSchemaDto saveCtlSchemaWithAppToken(String body,
                                                String tenantId,
                                                String applicationToken)
      throws KaaAdminServiceException {
    String applicationId = null;
    if (!isEmpty(applicationToken)) {
      applicationId = checkApplicationToken(applicationToken);
    }
    return saveCtlSchema(body, tenantId, applicationId);
  }

  @Override
  public void deleteCtlSchemaByFqnVersionTenantIdAndApplicationToken(String fqn,
                                                                     Integer version,
                                                                     String tenantId,
                                                                     String applicationToken)
      throws KaaAdminServiceException {
    String applicationId = null;
    if (!isEmpty(applicationToken)) {
      applicationId = checkApplicationToken(applicationToken);
    }
    deleteCtlSchemaByFqnVersionTenantIdAndApplicationId(fqn, version, tenantId, applicationId);
  }

  @Override
  public void deleteCtlSchemaByFqnVersionTenantIdAndApplicationId(String fqn,
                                                                  Integer version,
                                                                  String tenantId,
                                                                  String applicationId)
      throws KaaAdminServiceException {
    this.checkAuthority(KaaAuthorityDto.values());
    try {
      this.checkCtlSchemaFqn(fqn);
      this.checkCtlSchemaVersion(version);
      if (!isEmpty(applicationId)) {
        this.checkApplicationId(applicationId);
      }
      CTLSchemaDto schemaFound = controlService.getCtlSchemaByFqnVersionTenantIdAndApplicationId(
          fqn, version, tenantId, applicationId);
      Utils.checkNotNull(schemaFound);
      checkCtlSchemaEditScope(
          schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
      List<CTLSchemaDto> schemaDependents = controlService.getCtlSchemaDependents(
          fqn, version, tenantId, applicationId);
      if (schemaDependents != null && !schemaDependents.isEmpty()) {
        String message = "Can't delete the common type version as it is referenced"
            + " by the following common type(s): "
            + this.asText(schemaDependents);
        throw new IllegalArgumentException(message);
      }

      controlService.deleteCtlSchemaByFqnAndVersionTenantIdAndApplicationId(
          fqn, version, tenantId, applicationId);
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public CTLSchemaDto getCtlSchemaByFqnVersionTenantIdAndApplicationToken(String fqn,
                                                                          Integer version,
                                                                          String tenantId,
                                                                          String applicationToken)
      throws KaaAdminServiceException {
    String applicationId = null;
    if (!isEmpty(applicationToken)) {
      applicationId = checkApplicationToken(applicationToken);
    }
    return getCtlSchemaByFqnVersionTenantIdAndApplicationId(fqn, version, tenantId, applicationId);
  }

  @Override
  public CTLSchemaDto getCtlSchemaByFqnVersionTenantIdAndApplicationId(String fqn,
                                                                       Integer version,
                                                                       String tenantId,
                                                                       String applicationId)
      throws KaaAdminServiceException {
    this.checkAuthority(KaaAuthorityDto.values());
    try {
      this.checkCtlSchemaFqn(fqn);
      this.checkCtlSchemaVersion(version);
      if (!isEmpty(applicationId)) {
        this.checkApplicationId(applicationId);
      }
      CTLSchemaDto schemaFound = controlService.getCtlSchemaByFqnVersionTenantIdAndApplicationId(
          fqn, version, tenantId, applicationId);
      Utils.checkNotNull(schemaFound);
      checkCtlSchemaReadScope(
          schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
      return schemaFound;
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public CTLSchemaDto getCtlSchemaById(String schemaId) throws KaaAdminServiceException {
    this.checkAuthority(KaaAuthorityDto.values());
    try {
      this.checkCtlSchemaId(schemaId);
      CTLSchemaDto schemaFound = controlService.getCtlSchemaById(schemaId);
      Utils.checkNotNull(schemaFound);
      checkCtlSchemaReadScope(
          schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
      return schemaFound;
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public boolean checkFqnExistsWithAppToken(String fqn, String tenantId, String applicationToken)
      throws KaaAdminServiceException {
    String applicationId = null;
    if (!isEmpty(applicationToken)) {
      applicationId = checkApplicationToken(applicationToken);
    }
    return checkFqnExists(fqn, tenantId, applicationId);
  }

  @Override
  public boolean checkFqnExists(String fqn, String tenantId, String applicationId)
      throws KaaAdminServiceException {
    this.checkAuthority(KaaAuthorityDto.values());
    try {
      this.checkCtlSchemaFqn(fqn);
      List<CtlSchemaMetaInfoDto> result = controlService.getSiblingsByFqnTenantIdAndApplicationId(
          fqn, tenantId, applicationId);
      return result != null && !result.isEmpty();
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public boolean checkFqnExists(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.values());
    try {
      if (isEmpty(ctlSchemaForm.getId())) {
        AuthUserDto currentUser = getCurrentUser();
        RecordField schemaForm = ctlSchemaForm.getSchema();
        String fqn = schemaForm.getDeclaredFqn().getFqnString();
        String tenantId = currentUser.getTenantId();
        return checkFqnExists(fqn, tenantId, ctlSchemaForm.getMetaInfo().getApplicationId());
      }
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
    return false;
  }

  @Override
  public CtlSchemaMetaInfoDto promoteScopeToTenant(String applicationId, String fqn)
      throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.values());
    final String tenantId = getTenantId();
    checkApplicationId(applicationId);
    checkCtlSchemaEditScope(tenantId, applicationId);

    try {
      Set<CTLSchemaDto> dependencies = new HashSet<>();
      List<Integer> versions = controlService.getAllCtlSchemaVersionsByFqnTenantIdAndApplicationId(
          fqn, tenantId, applicationId);

      if (versions.isEmpty()) {
        throw new KaaAdminServiceException(
            "The requested item was not found!", ServiceErrorCode.ITEM_NOT_FOUND);
      }

      // meta info same for all versions
      CtlSchemaMetaInfoDto ctlSchemaMetaInfo =
          controlService.getCtlSchemaByFqnVersionTenantIdAndApplicationId(
              fqn, versions.get(0), tenantId, applicationId).getMetaInfo();
      ctlSchemaMetaInfo.setApplicationId(null); //promote to tenant

      // get dep of all versions
      for (Integer version : versions) {
        CTLSchemaDto schema = controlService.getCtlSchemaByFqnVersionTenantIdAndApplicationId(
            fqn, version, tenantId, applicationId);
        Set<CTLSchemaDto> schemaDependents = schema.getDependencySet();
        dependencies.addAll(schemaDependents.stream()
            .filter(dep -> dep.getMetaInfo().getScope() == CTLSchemaScopeDto.APPLICATION)
            .collect(Collectors.toList()));
      }

      // check if CT has dependencies with application scope
      if (!dependencies.isEmpty()) {
        String message = "Can't promote the common type version as it has references"
            + " on following common type(s) with application scope: "
            + asText(dependencies);
        throw new KaaAdminServiceException(message, ServiceErrorCode.CONFLICT);
      }

      return controlService.updateCtlSchemaMetaInfoScope(ctlSchemaMetaInfo);
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public List<CtlSchemaMetaInfoDto> getSystemLevelCtlSchemas() throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.values());
    try {
      return controlService.getSystemCtlSchemasMetaInfo();
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public List<CtlSchemaMetaInfoDto> getTenantLevelCtlSchemas() throws KaaAdminServiceException {
    checkAuthority(
        KaaAuthorityDto.TENANT_ADMIN,
        KaaAuthorityDto.TENANT_DEVELOPER,
        KaaAuthorityDto.TENANT_USER);
    try {
      AuthUserDto currentUser = getCurrentUser();
      return controlService.getAvailableCtlSchemasMetaInfoForTenant(currentUser.getTenantId());
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public List<CtlSchemaReferenceDto> getTenantLevelCtlSchemaReferenceForEcf(
      String ecfId, List<EventClassViewDto> eventClassViewDtoList)
      throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
    try {
      AuthUserDto currentUser = getCurrentUser();
      List<CtlSchemaMetaInfoDto> ctlSchemaReferenceDtoListForTenant =
          controlService.getAvailableCtlSchemasMetaInfoForTenant(currentUser.getTenantId());
      Set<String> fqnListOfCurrentEcf = controlService.getFqnSetForEcf(ecfId);
      if (eventClassViewDtoList != null) {
        for (EventClassViewDto eventClassViewDto : eventClassViewDtoList) {
          String fqn = eventClassViewDto.getCtlSchemaForm().getMetaInfo().getFqn();
          fqnListOfCurrentEcf.add(fqn);
        }
      }
      List<CtlSchemaReferenceDto> availableCtlSchemaReferenceForEcf = new ArrayList<>();
      for (CtlSchemaMetaInfoDto metaInfo : ctlSchemaReferenceDtoListForTenant) {
        if (!fqnListOfCurrentEcf.contains(metaInfo.getFqn())) {
          for (int version : metaInfo.getVersions()) {
            availableCtlSchemaReferenceForEcf.add(new CtlSchemaReferenceDto(metaInfo, version));
          }
        }
      }
      return availableCtlSchemaReferenceForEcf;
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public List<CtlSchemaMetaInfoDto> getApplicationLevelCtlSchemasByAppToken(
      String applicationToken) throws KaaAdminServiceException {
    return getApplicationLevelCtlSchemas(checkApplicationToken(applicationToken));
  }

  @Override
  public List<CtlSchemaMetaInfoDto> getApplicationLevelCtlSchemas(String applicationId)
      throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
    try {
      this.checkApplicationId(applicationId);
      AuthUserDto currentUser = getCurrentUser();
      return controlService.getAvailableCtlSchemasMetaInfoForApplication(
          currentUser.getTenantId(),applicationId);
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public FileData exportCtlSchemaByAppToken(String fqn, int version,
                                            String applicationToken,
                                            CTLSchemaExportMethod method)
      throws KaaAdminServiceException {
    String applicationId = null;
    if (!isEmpty(applicationToken)) {
      applicationId = checkApplicationToken(applicationToken);
    }
    return exportCtlSchema(fqn, version, applicationId, method);
  }

  @Override
  public FileData exportCtlSchema(String fqn, int version,
                                  String applicationId,
                                  CTLSchemaExportMethod method) throws KaaAdminServiceException {
    try {
      this.checkCtlSchemaFqn(fqn);
      this.checkCtlSchemaVersion(version);
      String tenantId = getCurrentUser().getTenantId();
      CTLSchemaDto schemaFound = controlService.getCtlSchemaByFqnVersionTenantIdAndApplicationId(
          fqn, version, tenantId, applicationId);
      Utils.checkNotNull(schemaFound);
      checkCtlSchemaReadScope(
          schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
      switch (method) {
        case SHALLOW:
          return controlService.exportCtlSchemaShallow(schemaFound);
        case FLAT:
          return controlService.exportCtlSchemaFlat(schemaFound);
        case DEEP:
          return controlService.exportCtlSchemaDeep(schemaFound);
        default:
          throw new IllegalArgumentException(
              "The export method " + method.name() + " is not currently supported!");
      }
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public CtlSchemaFormDto saveCtlSchemaForm(CtlSchemaFormDto ctlSchemaForm,
                                            ConverterType converterType)
      throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.values());
    try {
      AuthUserDto currentUser = getCurrentUser();
      CTLSchemaDto ctlSchema = null;
      if (!isEmpty(ctlSchemaForm.getId())) {
        ctlSchema = getCtlSchemaById(ctlSchemaForm.getId());
        if (ctlSchema == null) {
          throw new KaaAdminServiceException(
              "Requested item was not found!",
              ServiceErrorCode.ITEM_NOT_FOUND);
        }
      } else {
        ctlSchema = new CTLSchemaDto();
      }
      if (isEmpty(ctlSchema.getId())) {
        ctlSchema.setCreatedUsername(currentUser.getUsername());
        RecordField schemaForm = ctlSchemaForm.getSchema();
        ctlSchema.setMetaInfo(ctlSchemaForm.getMetaInfo());
        ctlSchema.getMetaInfo().setFqn(schemaForm.getDeclaredFqn().getFqnString());
        ctlSchema.getMetaInfo().setTenantId(currentUser.getTenantId());
        ctlSchema.setVersion(schemaForm.getVersion());
        List<FqnVersion> dependenciesList = schemaForm.getContext().getCtlDependenciesList();
        Set<CTLSchemaDto> dependencies = new HashSet<>();
        List<FqnVersion> missingDependencies = new ArrayList<>();
        for (FqnVersion fqnVersion : dependenciesList) {
          CTLSchemaDto dependency =
              controlService.getAnyCtlSchemaByFqnVersionTenantIdAndApplicationId(
              fqnVersion.getFqnString(), fqnVersion.getVersion(),
              ctlSchema.getMetaInfo().getTenantId(), ctlSchema.getMetaInfo().getApplicationId());
          if (dependency != null) {
            dependencies.add(dependency);
          } else {
            missingDependencies.add(fqnVersion);
          }
        }
        if (!missingDependencies.isEmpty()) {
          String message = "The following dependencies are missing from the database: "
              + Arrays.toString(missingDependencies.toArray());
          throw new IllegalArgumentException(message);
        }
        ctlSchema.setDependencySet(dependencies);
        SchemaFormAvroConverter converter = getCtlSchemaConverterForScope(
            ctlSchema.getMetaInfo().getTenantId(),
            ctlSchema.getMetaInfo().getApplicationId(), converterType);
        Schema avroSchema = converter.createSchemaFromSchemaForm(schemaForm);
        String schemaBody = SchemaFormAvroConverter.createSchemaString(avroSchema, true);
        ctlSchema.setBody(schemaBody);
      }

      CTLSchemaDto savedCtlSchema = saveCtlSchema(ctlSchema);
      if (savedCtlSchema != null) {
        return toCtlSchemaForm(savedCtlSchema, converterType);
      }
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
    return null;
  }

  @Override
  public List<CtlSchemaReferenceDto> getAvailableApplicationCtlSchemaReferences(
      String applicationId)
      throws KaaAdminServiceException {
    checkAuthority(
        KaaAuthorityDto.TENANT_DEVELOPER,
        KaaAuthorityDto.TENANT_USER,
        KaaAuthorityDto.TENANT_ADMIN);
    try {
      AuthUserDto currentUser = getCurrentUser();
      List<CtlSchemaReferenceDto> result = new ArrayList<>();
      List<CtlSchemaMetaInfoDto> availableMetaInfo =
          controlService.getAvailableCtlSchemasMetaInfoForApplication(
              currentUser.getTenantId(), applicationId);
      for (CtlSchemaMetaInfoDto metaInfo : availableMetaInfo) {
        for (int version : metaInfo.getVersions()) {
          result.add(new CtlSchemaReferenceDto(metaInfo, version));
        }
      }
      return result;
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }


  @Override
  public CtlSchemaFormDto getLatestCtlSchemaForm(String metaInfoId)
      throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.values());
    try {
      this.checkCtlSchemaMetaInfoId(metaInfoId);
      CTLSchemaDto ctlSchema = controlService.getLatestCtlSchemaByMetaInfoId(metaInfoId);
      Utils.checkNotNull(ctlSchema);
      checkCtlSchemaReadScope(
          ctlSchema.getMetaInfo().getTenantId(), ctlSchema.getMetaInfo().getApplicationId());
      return toCtlSchemaForm(ctlSchema, ConverterType.FORM_AVRO_CONVERTER);
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public CtlSchemaFormDto getCtlSchemaFormByMetaInfoIdAndVer(String metaInfoId, int version)
      throws KaaAdminServiceException {
    this.checkAuthority(KaaAuthorityDto.values());
    try {
      this.checkCtlSchemaMetaInfoId(metaInfoId);
      this.checkCtlSchemaVersion(version);
      CTLSchemaDto schemaFound = controlService.getCtlSchemaByMetaInfoIdAndVer(
          metaInfoId, version);
      Utils.checkNotNull(schemaFound);
      checkCtlSchemaReadScope(
          schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
      return toCtlSchemaForm(schemaFound, ConverterType.FORM_AVRO_CONVERTER);
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public CtlSchemaFormDto createNewCtlSchemaFormInstance(String metaInfoId,
                                                         Integer sourceVersion,
                                                         String applicationId,
                                                         ConverterType converterType)
      throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.values());
    try {
      SchemaFormAvroConverter converter = getCtlSchemaConverterForScope(
          getCurrentUser().getTenantId(), applicationId, converterType);
      CtlSchemaFormDto sourceCtlSchema = null;
      if (!isEmpty(metaInfoId) && sourceVersion != null) {
        sourceCtlSchema = getCtlSchemaFormByMetaInfoIdAndVer(metaInfoId, sourceVersion);
        Utils.checkNotNull(sourceCtlSchema);
      }
      CtlSchemaFormDto ctlSchemaForm = null;
      if (sourceCtlSchema != null) {
        checkCtlSchemaEditScope(
            sourceCtlSchema.getMetaInfo().getTenantId(),
            sourceCtlSchema.getMetaInfo().getApplicationId());
        ctlSchemaForm = new CtlSchemaFormDto();
        ctlSchemaForm.setMetaInfo(sourceCtlSchema.getMetaInfo());
        RecordField form = sourceCtlSchema.getSchema();
        form.updateVersion(form.getContext().getMaxVersion(
            new Fqn(sourceCtlSchema.getMetaInfo().getFqn())) + 1);
        ctlSchemaForm.setSchema(form);
      } else {
        checkCtlSchemaEditScope(getCurrentUser().getTenantId(), applicationId);
        ctlSchemaForm = new CtlSchemaFormDto();
        RecordField form = converter.getEmptySchemaFormInstance();
        form.updateVersion(1);
        ctlSchemaForm.setSchema(form);
        CtlSchemaMetaInfoDto metaInfo = new CtlSchemaMetaInfoDto(null,
            getCurrentUser().getTenantId(), applicationId);
        ctlSchemaForm.setMetaInfo(metaInfo);
      }
      return ctlSchemaForm;
    } catch (Exception cause) {
      throw Utils.handleException(cause);
    }
  }

  @Override
  public RecordField generateCtlSchemaForm(String fileItemName, String applicationId)
      throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.values());
    try {
      checkCtlSchemaReadScope(getCurrentUser().getTenantId(), applicationId);
      byte[] data = getFileContent(fileItemName);
      String avroSchema = new String(data);
      validateRecordSchema(avroSchema, true);
      SchemaFormAvroConverter converter = getCtlSchemaConverterForScope(
          getCurrentUser().getTenantId(), applicationId, ConverterType.FORM_AVRO_CONVERTER);
      RecordField form = converter.createSchemaFormFromSchema(avroSchema);
      if (form.getVersion() == null) {
        form.updateVersion(1);
      }
      return form;
    } catch (Exception ex) {
      throw Utils.handleException(ex);
    }
  }

  @Override
  public String prepareCtlSchemaExport(String ctlSchemaId,
                                       CTLSchemaExportMethod method)
      throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.values());
    try {
      CTLSchemaDto schemaFound = controlService.getCtlSchemaById(ctlSchemaId);
      Utils.checkNotNull(schemaFound);
      checkCtlSchemaReadScope(
          schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
      CtlSchemaExportKey key = new CtlSchemaExportKey(ctlSchemaId, method);
      return Base64.encodeObject(key, Base64.URL_SAFE);
    } catch (Exception ex) {
      throw Utils.handleException(ex);
    }
  }

  @Override
  public String getFlatSchemaByCtlSchemaId(String schemaId) throws KaaAdminServiceException {
    this.checkAuthority(KaaAuthorityDto.values());
    try {
      return controlService.getFlatSchemaByCtlSchemaId(schemaId);
    } catch (ControlServiceException ex) {
      throw Utils.handleException(ex);
    }
  }

  private void checkCtlSchemaEditScope(String tenantId, String applicationId)
      throws KaaAdminServiceException {
    AuthUserDto currentUser = getCurrentUser();
    CTLSchemaScopeDto scope = detectScope(tenantId, applicationId);
    boolean allowed = false;
    switch (currentUser.getAuthority()) {
      case KAA_ADMIN:
        allowed = scope == CTLSchemaScopeDto.SYSTEM;
        break;
      case TENANT_ADMIN:
        checkTenantId(tenantId);
        allowed = scope == CTLSchemaScopeDto.TENANT;
        break;
      case TENANT_DEVELOPER:
      case TENANT_USER:
        checkTenantId(tenantId);
        if (scope.getLevel() >= CTLSchemaScopeDto.APPLICATION.getLevel()) {
          checkApplicationId(applicationId);
        }
        allowed = scope.getLevel() >= CTLSchemaScopeDto.TENANT.getLevel();
        break;
      default:
        break;
    }
    if (!allowed) {
      throw new KaaAdminServiceException(ServiceErrorCode.PERMISSION_DENIED);
    }
  }

  private void checkCtlSchemaId(String schemaId) throws KaaAdminServiceException {
    if (schemaId == null || schemaId.isEmpty()) {
      throw new IllegalArgumentException("Missing CTL schema ID!");
    }
  }

  private void checkCtlSchemaMetaInfoId(String metaInfoId) throws KaaAdminServiceException {
    if (metaInfoId == null || metaInfoId.isEmpty()) {
      throw new IllegalArgumentException("Missing CTL schema meta info ID!");
    }
  }

  private void checkCtlSchemaFqn(String fqn) throws KaaAdminServiceException {
    if (fqn == null || fqn.isEmpty()) {
      throw new IllegalArgumentException("Missing fully qualified CTL schema name!");
    }
  }

  private void checkCtlSchemaVersion(Integer version) throws KaaAdminServiceException {
    if (version == null) {
      throw new IllegalArgumentException("Missing CTL schema version number!");
    } else if (version <= 0) {
      throw new IllegalArgumentException("The CTL schema version is not a positive number!");
    }
  }

  private CTLSchemaScopeDto detectScope(String tenantId, String applicationId) {
    CTLSchemaScopeDto scope = CTLSchemaScopeDto.SYSTEM;
    if (tenantId != null && !tenantId.isEmpty()) {
      if (applicationId != null && !applicationId.isEmpty()) {
        scope = CTLSchemaScopeDto.APPLICATION;
      } else {
        scope = CTLSchemaScopeDto.TENANT;
      }
    }
    return scope;
  }

  private void checkCtlSchemaReadScope(String tenantId, String applicationId)
      throws KaaAdminServiceException {
    AuthUserDto currentUser = getCurrentUser();
    CTLSchemaScopeDto scope = detectScope(tenantId, applicationId);
    boolean allowed = false;
    switch (currentUser.getAuthority()) {
      case KAA_ADMIN:
        allowed = scope == CTLSchemaScopeDto.SYSTEM;
        break;
      case TENANT_ADMIN:
        if (scope == CTLSchemaScopeDto.TENANT) {
          checkTenantId(tenantId);
        }
        allowed = scope.getLevel() <= CTLSchemaScopeDto.TENANT.getLevel();
        break;
      case TENANT_DEVELOPER:
      case TENANT_USER:
        if (scope == CTLSchemaScopeDto.TENANT) {
          checkTenantId(tenantId);
        }
        if (scope.getLevel() >= CTLSchemaScopeDto.APPLICATION.getLevel()) {
          checkApplicationId(applicationId);
        }
        allowed = scope.getLevel() >= CTLSchemaScopeDto.SYSTEM.getLevel();
        break;
      default:
        break;
    }
    if (!allowed) {
      throw new KaaAdminServiceException(ServiceErrorCode.PERMISSION_DENIED);
    }
  }

  /**
   * Returns a string that contains fully qualified names and version numbers
   * of the given CTL schemas.
   *
   * @param types A collection of CTL schemas
   * @return A string that contains fully qualified names and version numbers of the given CTL
   *         schemas
   */
  private String asText(Collection<CTLSchemaDto> types) {
    StringBuilder message = new StringBuilder();
    if (types != null) {
      for (CTLSchemaDto type : types) {
        CtlSchemaMetaInfoDto details = type.getMetaInfo();
        message.append("\n").append("FQN: ")
            .append(details.getFqn())
            .append(", version: ")
            .append(type.getVersion());
      }
    }
    return message.toString();
  }

  @Override
  public CtlSchemaReferenceDto getLastCtlSchemaReferenceDto(String ctlSchemaId)
      throws KaaAdminServiceException {
    try {
      if (!isEmpty(ctlSchemaId)) {
        CTLSchemaDto ctlSchemaDto = controlService.getCtlSchemaById(ctlSchemaId);
        CtlSchemaReferenceDto ctlSchemaReference =
            getAvailableApplicationCtlSchemaReferences(null).stream()
                .filter(ctlSchemaReferenceDto -> ctlSchemaReferenceDto.getMetaInfo()
                .getId()
                .equals(ctlSchemaDto.getMetaInfo().getId()))
                .findFirst()
                .get();
        return ctlSchemaReference;
      }
    } catch (Exception ex) {
      throw Utils.handleException(ex);
    }
    return null;
  }

}
