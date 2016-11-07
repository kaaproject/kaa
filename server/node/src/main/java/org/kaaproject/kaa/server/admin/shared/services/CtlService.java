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

package org.kaaproject.kaa.server.admin.shared.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
import org.kaaproject.kaa.common.dto.ctl.CtlSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;
import org.kaaproject.kaa.server.admin.shared.schema.EventClassViewDto;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/ctlService")
public interface CtlService extends RemoteService {

  CTLSchemaDto saveCtlSchema(CTLSchemaDto schema) throws KaaAdminServiceException;

  CTLSchemaDto saveCtlSchema(String body, String tenantId, String applicationId)
      throws KaaAdminServiceException;

  CTLSchemaDto saveCtlSchemaWithAppToken(String body, String tenantId, String applicationToken)
      throws KaaAdminServiceException;

  void deleteCtlSchemaByFqnVersionTenantIdAndApplicationToken(String fqn, Integer version,
                                                              String tenantId,
                                                              String applicationToken)
      throws KaaAdminServiceException;

  void deleteCtlSchemaByFqnVersionTenantIdAndApplicationId(String fqn,
                                                           Integer version,
                                                           String tenantId,
                                                           String applicationId)
      throws KaaAdminServiceException;

  CTLSchemaDto getCtlSchemaByFqnVersionTenantIdAndApplicationId(String fqn,
                                                                Integer version,
                                                                String tenantId,
                                                                String applicationId)
      throws KaaAdminServiceException;

  CTLSchemaDto getCtlSchemaByFqnVersionTenantIdAndApplicationToken(String fqn,
                                                                   Integer version,
                                                                   String tenantId,
                                                                   String applicationToken)
      throws KaaAdminServiceException;

  CTLSchemaDto getCtlSchemaById(String schemaId) throws KaaAdminServiceException;

  boolean checkFqnExists(String fqn, String tenantId, String applicationId)
      throws KaaAdminServiceException;

  boolean checkFqnExists(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

  boolean checkFqnExistsWithAppToken(String fqn, String tenantId, String applicationToken)
      throws KaaAdminServiceException;

  CtlSchemaMetaInfoDto promoteScopeToTenant(String applicationToken, String fqn)
      throws KaaAdminServiceException;

  List<CtlSchemaMetaInfoDto> getApplicationLevelCtlSchemas(String applicationId)
      throws KaaAdminServiceException;

  List<CtlSchemaMetaInfoDto> getSystemLevelCtlSchemas() throws KaaAdminServiceException;

  List<CtlSchemaMetaInfoDto> getTenantLevelCtlSchemas() throws KaaAdminServiceException;

  List<CtlSchemaReferenceDto> getTenantLevelCtlSchemaReferenceForEcf(
      String ecfId, List<EventClassViewDto> eventClassViewDtoList) throws KaaAdminServiceException;

  List<CtlSchemaMetaInfoDto> getApplicationLevelCtlSchemasByAppToken(String applicationToken)
      throws KaaAdminServiceException;

  FileData exportCtlSchemaByAppToken(String fqn,
                                     int version,
                                     String applicationToken,
                                     CTLSchemaExportMethod method) throws KaaAdminServiceException;

  FileData exportCtlSchema(String fqn,
                           int version,
                           String applicationId,
                           CTLSchemaExportMethod method) throws KaaAdminServiceException;

  CtlSchemaFormDto saveCtlSchemaForm(CtlSchemaFormDto ctlSchemaForm,
                                     ConverterType converterType) throws KaaAdminServiceException;

  List<CtlSchemaReferenceDto> getAvailableApplicationCtlSchemaReferences(String applicationId)
      throws KaaAdminServiceException;

  CtlSchemaFormDto getLatestCtlSchemaForm(String metaInfoId) throws KaaAdminServiceException;

  CtlSchemaFormDto getCtlSchemaFormByMetaInfoIdAndVer(String metaInfoId, int version)
      throws KaaAdminServiceException;

  CtlSchemaFormDto createNewCtlSchemaFormInstance(String metaInfoId,
                                                  Integer sourceVersion,
                                                  String applicationId,
                                                  ConverterType converterType)
      throws KaaAdminServiceException;

  RecordField generateCtlSchemaForm(String fileItemName, String applicationId)
      throws KaaAdminServiceException;

  String prepareCtlSchemaExport(String ctlSchemaId, CTLSchemaExportMethod method)
      throws KaaAdminServiceException;

  String getFlatSchemaByCtlSchemaId(String logSchemaId) throws KaaAdminServiceException;

  CtlSchemaReferenceDto getLastCtlSchemaReferenceDto(String ctlSchemaId)
      throws KaaAdminServiceException;

}
