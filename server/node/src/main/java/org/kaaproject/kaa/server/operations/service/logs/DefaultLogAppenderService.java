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

package org.kaaproject.kaa.server.operations.service.logs;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.LogAppendersService;
import org.kaaproject.kaa.server.common.dao.LogSchemaService;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultLogAppenderService implements LogAppenderService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogAppenderService.class);

    @Autowired
    private LogSchemaService logSchemaService;

    @Autowired
    private LogAppenderBuilder logAppenderResolver;

    @Autowired
    private CTLService ctlService;

    @Autowired
    private LogAppendersService logAppendersService;

    @Override
    public List<LogAppender> getApplicationAppenders(String applicationId) {
        LOG.debug("Get log appenders by application id [{}]", applicationId);
        List<LogAppenderDto> appenders = logAppendersService.findAllAppendersByAppId(applicationId);
        LOG.debug("Found all appenders [{}] for application.", appenders);
        List<LogAppender> logAppenders = new ArrayList<>(appenders.size());

        for (LogAppenderDto appender : appenders) {
            try {
                LogAppender logAppender = logAppenderResolver.getAppender(appender);
                logAppenders.add(logAppender);
            } catch (Exception e) {
                LOG.warn("Can't initialize log appender [{}]", appender, e);
                continue;
            }
        }
        return logAppenders;
    }
    
    @Override
    public List<LogAppender> getApplicationAppendersByLogSchemaVersion(
            String applicationId, int schemaVersion) {
        LOG.debug("Get log appenders by application id [{}] and schema version [{}]", applicationId, schemaVersion);
        List<LogAppenderDto> appenders = logAppendersService.findLogAppendersByAppIdAndSchemaVersion(applicationId, schemaVersion);
        LOG.debug("Found all appenders [{}] for application and schema version.", appenders);
        List<LogAppender> logAppenders = new ArrayList<>(appenders.size());

        for (LogAppenderDto appender : appenders) {
            try {
                LogAppender logAppender = logAppenderResolver.getAppender(appender);
                logAppenders.add(logAppender);
            } catch (Exception e) {
                LOG.warn("Can't initialize log appender [{}], exception catched: {}", appender, e);
                continue;
            }
        }
        return logAppenders;
    }

    @Override
    public LogAppender getApplicationAppender(String appenderId) {
        LOG.debug("Get log appender by id [{}]", appenderId);
        LogAppenderDto appender = logAppendersService.findLogAppenderById(appenderId);
        LOG.debug("Found appender [{}] by appender id [{}] ", appender, appenderId);
        LogAppender logAppender = null;
        try {
            logAppender = logAppenderResolver.getAppender(appender);
        } catch (Exception e) {
            LOG.warn("Can't initialize log appender [{}]", appender, e);
        }
        return logAppender;
    }

    @Override
    public LogSchema getLogSchema(String applicationId, int logSchemaVersion) {
        LOG.debug("Fetching log schema for application {} and version {}", applicationId, logSchemaVersion);
        LogSchema logSchema = null;
        LogSchemaDto logSchemaDto = logSchemaService.findLogSchemaByAppIdAndVersion(applicationId, logSchemaVersion);
        CTLSchemaDto ctlSchema = ctlService.findCTLSchemaById(logSchemaDto.getCtlSchemaId());
        String logFlatSchema = ctlService.flatExportAsString(ctlSchema);
        if (logSchemaDto != null) {
            logSchema = new LogSchema(logSchemaDto, logFlatSchema);
        }
        return logSchema;
    }

}
