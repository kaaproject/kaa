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
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/ctlService")
public interface CtlService extends RemoteService {

    CTLSchemaDto saveCTLSchema(CTLSchemaDto schema) throws KaaAdminServiceException;

    CTLSchemaDto saveCTLSchema(String body, String tenantId, String applicationId) throws KaaAdminServiceException;

    CTLSchemaDto saveCTLSchemaWithAppToken(String body, String tenantId, String applicationToken) throws KaaAdminServiceException;

    void deleteCTLSchemaByFqnVersionTenantIdAndApplicationToken(String fqn, Integer version, String tenantId, String applicationToken) throws KaaAdminServiceException;

    void deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) throws KaaAdminServiceException;

    CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) throws KaaAdminServiceException;

    CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationToken(String fqn, Integer version, String tenantId, String applicationToken) throws KaaAdminServiceException;

    CTLSchemaDto getCTLSchemaById(String schemaId) throws KaaAdminServiceException;

    boolean checkFqnExists(String fqn, String tenantId, String applicationId) throws KaaAdminServiceException;

    boolean checkFqnExists(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    boolean checkFqnExistsWithAppToken(String fqn, String tenantId, String applicationToken) throws KaaAdminServiceException;

    CTLSchemaMetaInfoDto promoteScopeToTenant(String applicationToken, String fqn) throws KaaAdminServiceException;

    List<CTLSchemaMetaInfoDto> getApplicationLevelCTLSchemas(String applicationId) throws KaaAdminServiceException;

    List<CTLSchemaMetaInfoDto> getSystemLevelCTLSchemas() throws KaaAdminServiceException;

    List<CTLSchemaMetaInfoDto> getTenantLevelCTLSchemas() throws KaaAdminServiceException;

    List<CTLSchemaMetaInfoDto> getApplicationLevelCTLSchemasByAppToken(String applicationToken) throws KaaAdminServiceException;

    FileData exportCTLSchemaByAppToken(String fqn, int version, String applicationToken, CTLSchemaExportMethod method) throws KaaAdminServiceException;

    FileData exportCTLSchema(String fqn, int version, String applicationId, CTLSchemaExportMethod method) throws KaaAdminServiceException;

    CtlSchemaFormDto saveCTLSchemaForm(CtlSchemaFormDto ctlSchemaForm, ConverterType converterType) throws KaaAdminServiceException;

    List<CtlSchemaReferenceDto> getAvailableApplicationCTLSchemaReferences(String applicationId) throws KaaAdminServiceException;

    CtlSchemaFormDto getLatestCTLSchemaForm(String metaInfoId) throws KaaAdminServiceException;

    CtlSchemaFormDto getCTLSchemaFormByMetaInfoIdAndVer(String metaInfoId, int version) throws KaaAdminServiceException;

    CtlSchemaFormDto createNewCTLSchemaFormInstance(String metaInfoId, Integer sourceVersion, String applicationId, ConverterType converterType) throws KaaAdminServiceException;

    RecordField generateCtlSchemaForm(String fileItemName, String applicationId) throws KaaAdminServiceException;

    String prepareCTLSchemaExport(String ctlSchemaId, CTLSchemaExportMethod method) throws KaaAdminServiceException;

    String getFlatSchemaByCtlSchemaId(String logSchemaId) throws KaaAdminServiceException;

}
