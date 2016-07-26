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
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.server.admin.shared.properties.PropertiesDto;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/adminUIService")
public interface AdminUIService extends RemoteService {

    PropertiesDto getMailProperties() throws KaaAdminServiceException;

    PropertiesDto editMailProperties(PropertiesDto mailPropertiesDto) throws KaaAdminServiceException;

    PropertiesDto getGeneralProperties() throws KaaAdminServiceException;

    PropertiesDto editGeneralProperties(PropertiesDto generalPropertiesDto) throws KaaAdminServiceException;

    String getRecordDataByApplicationIdAndSchemaVersion(String applicationId, int schemaVersion, RecordKey.RecordFiles file) throws KaaAdminServiceException;

    String getRecordLibraryByApplicationIdAndSchemaVersion(String applicationId, int logSchemaVersion, RecordKey.RecordFiles file) throws KaaAdminServiceException;

    RecordField createSimpleEmptySchemaForm() throws KaaAdminServiceException;

    RecordField createCommonEmptySchemaForm() throws KaaAdminServiceException;

    RecordField generateSimpleSchemaForm(String fileItemName) throws KaaAdminServiceException;

    RecordField generateCommonSchemaForm(String fileItemName) throws KaaAdminServiceException;

    RecordField getRecordDataFromFile(String schema, String fileItemName) throws KaaAdminServiceException;

}
