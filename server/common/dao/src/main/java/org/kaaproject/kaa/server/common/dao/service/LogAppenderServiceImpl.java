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
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;

import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.common.dao.LogAppendersService;
import org.kaaproject.kaa.server.common.dao.impl.LogAppenderDao;
import org.kaaproject.kaa.server.common.dao.impl.LogSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.LogAppender;
import org.kaaproject.kaa.server.common.dao.model.sql.LogSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LogAppenderServiceImpl implements LogAppendersService {

    private static final Logger LOG = LoggerFactory.getLogger(LogAppenderServiceImpl.class);

    @Autowired
    private LogAppenderDao<LogAppender> logAppenderDao;
    @Autowired
    private LogSchemaDao<LogSchema> logSchemaDao;

    @Override
    public List<LogAppenderDto> findLogAppendersByAppIdAndSchemaVersion(
            String appId, int schemaVersion) {
        LOG.debug("Find registered log appenders by application id [{}] and schema version [{}]", appId, schemaVersion);
        return convertDtoList(logAppenderDao.findByAppIdAndSchemaVersion(appId, schemaVersion));
    }

    @Override
    public List<LogAppenderDto> findAllAppendersByAppId(String appId) {
        LOG.debug("Find vacant log appenders by application id [{}]", appId);
        return convertDtoList(logAppenderDao.findByAppId(appId));
    }

    @Override
    public LogAppenderDto saveLogAppender(LogAppenderDto logAppenderDto) {
        LOG.debug("Save log appender [{}]", logAppenderDto);
        LogAppenderDto saved = null;
        if (logAppenderDto != null) {
            if(isBlank(logAppenderDto.getId())) {
                logAppenderDto.setCreatedTime(System.currentTimeMillis());
            }
            saved = getDto(logAppenderDao.save(new LogAppender(logAppenderDto)));
        }
        return saved;
    }

    @Override
    public LogAppenderDto findLogAppenderById(String id) {
        LOG.debug("Find log appender by id  [{}]", id);
        return getDto(logAppenderDao.findById(id));
    }

    @Override
    public void removeLogAppenderById(String id) {
        LOG.debug("Remove log appender by id  [{}]", id);
        logAppenderDao.removeById(id);
    }

}
