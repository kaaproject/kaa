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
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.LogSchemaViewDto;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/loggingService")
public interface LoggingService extends RemoteService {

    List<LogSchemaDto> getLogSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<LogSchemaDto> getLogSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    LogSchemaDto getLogSchema(String logSchemaId) throws KaaAdminServiceException;

    LogSchemaDto getLogSchemaByApplicationTokenAndVersion(String applicationToken, int version) throws KaaAdminServiceException;

    LogSchemaDto saveLogSchema(LogSchemaDto profileSchema) throws KaaAdminServiceException;

    List<LogAppenderDto> getRestLogAppendersByApplicationToken(String appToken) throws KaaAdminServiceException;

    LogAppenderDto getRestLogAppender(String appenderId) throws KaaAdminServiceException;

    LogAppenderDto editRestLogAppender(LogAppenderDto appender) throws KaaAdminServiceException;

    void deleteLogAppender(String appenderId) throws KaaAdminServiceException;

    List<LogAppenderDto> getRestLogAppendersByApplicationId(String appId) throws KaaAdminServiceException;

    List<LogAppenderDto> getLogAppendersByApplicationId(String appId) throws KaaAdminServiceException;

    LogAppenderDto getLogAppender(String appenderId) throws KaaAdminServiceException;

    LogAppenderDto editLogAppender(LogAppenderDto appender) throws KaaAdminServiceException;

    List<VersionDto> getLogSchemasVersions(String applicationId) throws KaaAdminServiceException;

    LogSchemaViewDto getLogSchemaView(String logSchemaId) throws KaaAdminServiceException;

    LogAppenderDto getLogAppenderForm(String appenderId) throws KaaAdminServiceException;

    LogAppenderDto editLogAppenderForm(LogAppenderDto appender) throws KaaAdminServiceException;

    List<PluginInfoDto> getLogAppenderPluginInfos() throws KaaAdminServiceException;

    LogSchemaViewDto saveLogSchemaView(LogSchemaViewDto logSchema) throws KaaAdminServiceException;

    LogSchemaViewDto createLogSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

}
