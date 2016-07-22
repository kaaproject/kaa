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

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;
import org.kaaproject.kaa.server.admin.shared.schema.LogSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.services.CtlService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.LoggingService;
import org.kaaproject.kaa.server.common.plugin.PluginType;
import org.kaaproject.kaa.server.control.service.exception.ControlServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

@Service("loggingService")
public class LoggingServiceImpl extends AbstractAdminService implements LoggingService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LoggingServiceImpl.class);

    @Autowired
    CtlService ctlService;

    @Override
    public List<LogSchemaDto> getLogSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getLogSchemasByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<LogSchemaDto> getLogSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getLogSchemasByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogSchemaDto getLogSchema(String logSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            LogSchemaDto logSchema = controlService.getLogSchema(logSchemaId);
            Utils.checkNotNull(logSchema);
            checkApplicationId(logSchema.getApplicationId());
            return logSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogSchemaDto getLogSchemaByApplicationTokenAndVersion(String applicationToken, int version) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ApplicationDto storedApplication = controlService.getApplicationByApplicationToken(applicationToken);
            checkApplication(storedApplication);
            LogSchemaDto logSchema = controlService.getLogSchemaByApplicationIdAndVersion(storedApplication.getId(), version);
            Utils.checkNotNull(logSchema);
            return logSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogSchemaDto saveLogSchema(LogSchemaDto logSchema) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(logSchema.getId())) {
                logSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(logSchema.getApplicationId());
            } else {
                LogSchemaDto storedLogSchema = controlService.getLogSchema(logSchema.getId());
                Utils.checkNotNull(storedLogSchema);
                checkApplicationId(storedLogSchema.getApplicationId());
            }
            return controlService.saveLogSchema(logSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogSchemaViewDto getLogSchemaView(String logSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            LogSchemaDto logSchema = getLogSchema(logSchemaId);
            CTLSchemaDto ctlSchemaDto = controlService.getCTLSchemaById(logSchema.getCtlSchemaId());
            LogSchemaViewDto logSchemaViewDto = new LogSchemaViewDto(logSchema, toCtlSchemaForm(ctlSchemaDto, ConverterType.FORM_AVRO_CONVERTER));
            return logSchemaViewDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogSchemaViewDto saveLogSchemaView(LogSchemaViewDto logSchemaView) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            LogSchemaDto logSchemaDto = logSchemaView.getSchema();
            String applicationId = logSchemaDto.getApplicationId();
            checkApplicationId(applicationId);
            String ctlSchemaId = logSchemaDto.getCtlSchemaId();
            if (isEmpty(ctlSchemaId)) {
                if (logSchemaView.useExistingCtlSchema()) {
                    CtlSchemaReferenceDto metaInfo = logSchemaView.getExistingMetaInfo();
                    CTLSchemaDto schema = ctlService.getCTLSchemaByFqnVersionTenantIdAndApplicationId(metaInfo.getMetaInfo().getFqn(),
                            metaInfo.getVersion(),
                            metaInfo.getMetaInfo().getTenantId(),
                            metaInfo.getMetaInfo().getApplicationId());
                    logSchemaDto.setCtlSchemaId(schema.getId());
                } else {
                    CtlSchemaFormDto ctlSchemaForm = ctlService.saveCTLSchemaForm(logSchemaView.getCtlSchemaForm(), ConverterType.FORM_AVRO_CONVERTER);
                    logSchemaDto.setCtlSchemaId(ctlSchemaForm.getId());
                }
            }
            LogSchemaDto savedLogSchema = saveLogSchema(logSchemaDto);
            return getLogSchemaView(savedLogSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogSchemaViewDto createLogSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException {
        LOG.error("createLogSchemaFormCtlSchema [{}]", ctlSchemaForm.getSchema().getDisplayString());
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            LogSchemaDto logSchema = new LogSchemaDto();
            logSchema.setApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            logSchema.setName(ctlSchemaForm.getSchema().getDisplayNameFieldValue());
            logSchema.setDescription(ctlSchemaForm.getSchema().getDescriptionFieldValue());
            CtlSchemaFormDto savedCtlSchemaForm = ctlService.saveCTLSchemaForm(ctlSchemaForm, ConverterType.FORM_AVRO_CONVERTER);
            logSchema.setCtlSchemaId(savedCtlSchemaForm.getId());
            LogSchemaDto savedLogSchema = saveLogSchema(logSchema);
            return getLogSchemaView(savedLogSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<LogAppenderDto> getRestLogAppendersByApplicationToken(String appToken) throws KaaAdminServiceException {
        return getRestLogAppendersByApplicationId(checkApplicationToken(appToken));
    }

    @Override
    public List<LogAppenderDto> getRestLogAppendersByApplicationId(String appId) throws KaaAdminServiceException {
        List<LogAppenderDto> logAppenders = getLogAppendersByApplicationId(appId);
        for (LogAppenderDto logAppender : logAppenders) {
            setPluginJsonConfigurationFromRaw(logAppender, PluginType.LOG_APPENDER);
        }
        return logAppenders;
    }

    @Override
    public List<LogAppenderDto> getLogAppendersByApplicationId(String appId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(appId);
            return controlService.getLogAppendersByApplicationId(appId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogAppenderDto getRestLogAppender(String appenderId) throws KaaAdminServiceException {
        LogAppenderDto logAppender = getLogAppender(appenderId);
        setPluginJsonConfigurationFromRaw(logAppender, PluginType.LOG_APPENDER);
        return logAppender;
    }

    @Override
    public LogAppenderDto getLogAppender(String appenderId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            LogAppenderDto logAppender = controlService.getLogAppender(appenderId);
            Utils.checkNotNull(logAppender);
            checkApplicationId(logAppender.getApplicationId());
            return logAppender;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogAppenderDto editRestLogAppender(LogAppenderDto logAppender) throws KaaAdminServiceException {
        setPluginRawConfigurationFromJson(logAppender, PluginType.LOG_APPENDER);
        LogAppenderDto savedLogAppender = editLogAppender(logAppender);
        setPluginJsonConfigurationFromRaw(savedLogAppender, PluginType.LOG_APPENDER);
        return savedLogAppender;
    }

    @Override
    public LogAppenderDto editLogAppender(LogAppenderDto appender) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(appender.getId())) {
                appender.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(appender.getApplicationId());
            } else {
                LogAppenderDto storedlLogAppender = controlService.getLogAppender(appender.getId());
                Utils.checkNotNull(storedlLogAppender);
                checkApplicationId(storedlLogAppender.getApplicationId());
            }
            return controlService.editLogAppender(appender);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteLogAppender(String appenderId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(appenderId)) {
                throw new IllegalArgumentException("The appenderId parameter is empty.");
            }
            LogAppenderDto logAppender = controlService.getLogAppender(appenderId);
            Utils.checkNotNull(logAppender);
            checkApplicationId(logAppender.getApplicationId());
            controlService.deleteLogAppender(appenderId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<VersionDto> getLogSchemasVersions(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        List<VersionDto> logSchemaVersions = Collections.emptyList();
        try {
            checkApplicationId(applicationId);
            logSchemaVersions = controlService.getLogSchemaVersionsByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
        return logSchemaVersions;
    }

    @Override
    public LogAppenderDto getLogAppenderForm(String appenderId) throws KaaAdminServiceException {
        LogAppenderDto logAppender = getLogAppender(appenderId);
        try {
            setPluginFormConfigurationFromRaw(logAppender, PluginType.LOG_APPENDER);
            return logAppender;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogAppenderDto editLogAppenderForm(LogAppenderDto logAppender) throws KaaAdminServiceException {
        try {
            setPluginRawConfigurationFromForm(logAppender);
            LogAppenderDto saved = editLogAppender(logAppender);
            return saved;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<PluginInfoDto> getLogAppenderPluginInfos() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        return new ArrayList<PluginInfoDto>(pluginsInfo.get(PluginType.LOG_APPENDER).values());
    }

}
