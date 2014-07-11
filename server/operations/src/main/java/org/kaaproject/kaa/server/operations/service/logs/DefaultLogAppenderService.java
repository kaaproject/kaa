/*
 * Copyright 2014 CyberVision, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.LogSchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultLogAppenderService implements LogAppenderService {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogAppenderService.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private LogSchemaService logSchemaService;

    @Autowired
    private LogAppenderResolver logAppenderResolver;

    @Override
    public List<LogAppender> getApplicationAppenders(String applicationId) {
        ApplicationDto applicationDto = applicationService.findAppById(applicationId);
        String applicationToken = applicationDto.getApplicationToken();
        LOG.debug("Finding all appenders for application with ApplicationToken: [{}] ", applicationToken);

        String tenantId = applicationDto.getTenantId();
        String appendersNames = applicationDto.getLogAppendersNames();
        LOG.debug("All appenders names: [{}], for application", appendersNames);

        if (appendersNames == null) {
            LOG.debug("Appenders list for application is empty");
            return Collections.emptyList();
        }else{
            String[] logAppendersNames = appendersNames.split(",");
            List<LogAppender> logAppenders = new ArrayList<>(logAppendersNames.length);
            LOG.debug("Starting initialize appenders..");
            for (String logAppenderName : logAppendersNames) {
                LogAppender logAppender = logAppenderResolver.resolve(logAppenderName);
                logAppender.init(logAppenderName, tenantId, applicationId);
                logAppenders.add(logAppender);
            }
            return logAppenders;
        }
    }

    @Override
    public LogSchema getLogSchema(String applicationId, int logSchemaVersion) {
        LOG.debug("Fetching log schema for application {} and version {}", applicationId, logSchemaVersion);
        LogSchemaDto logSchemaDto = logSchemaService.findLogSchemaByAppIdAndVersion(applicationId, logSchemaVersion);
        return new LogSchema(logSchemaDto);
    }

}
