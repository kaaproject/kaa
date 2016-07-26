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

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateId;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.dao.LogSchemaService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.LogSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.LogSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LogSchemaServiceImpl implements LogSchemaService {

    private static final Logger LOG = LoggerFactory.getLogger(LogSchemaServiceImpl.class);

    @Autowired
    private LogSchemaDao<LogSchema> logSchemaDao;

    @Override
    public List<LogSchemaDto> findLogSchemasByAppId(String applicationId) {
        validateId(applicationId, "Can't find log schemas. Invalid application id: " + applicationId);
        return convertDtoList(logSchemaDao.findByApplicationId(applicationId));
    }

    @Override
    public List<VersionDto> findLogSchemaVersionsByApplicationId(String applicationId) {
        validateId(applicationId, "Can't find log schemas. Invalid application id: " + applicationId);
        List<LogSchema> logSchemas = logSchemaDao.findByApplicationId(applicationId);
        List<VersionDto> schemas = new ArrayList<>();
        for (LogSchema logSchema : logSchemas) {
            schemas.add(logSchema.toVersionDto());
        }
        return schemas;
    }

    @Override
    public LogSchemaDto findLogSchemaById(String id) {
        validateId(id, "Can't find log schema. Invalid log schema id: " + id);
        return getDto(logSchemaDao.findById(id));
    }

    @Override
    public LogSchemaDto findLogSchemaByAppIdAndVersion(String appId,
                                                       int schemaVersion) {
        validateId(appId, "Can't find log schema. Invalid application id: " + appId);
        return getDto(logSchemaDao.findByApplicationIdAndVersion(appId, schemaVersion));
    }


    @Override
    public LogSchemaDto saveLogSchema(LogSchemaDto logSchemaDto) {
        String id = logSchemaDto.getId();
        if (StringUtils.isBlank(id)) {
            LogSchema logSchema = logSchemaDao.findLatestLogSchemaByAppId(logSchemaDto.getApplicationId());
            int version = 0;
            if (logSchema != null) {
                version = logSchema.getVersion();
            }
            logSchemaDto.setId(null);
            logSchemaDto.setVersion(++version);
            logSchemaDto.setCreatedTime(System.currentTimeMillis());
        } else {
            LogSchemaDto oldLogSchemaDto = getDto(logSchemaDao.findById(id));
            if (oldLogSchemaDto != null) {
                oldLogSchemaDto.editFields(logSchemaDto);
                logSchemaDto = oldLogSchemaDto;
            } else {
                LOG.error("Can't find log schema with given id [{}].", id);
                throw new IncorrectParameterException("Invalid log schema id: " + id);
            }
        }
        LogSchemaDto savedSchema = getDto(logSchemaDao.save(new LogSchema(logSchemaDto)));
        if (savedSchema == null) {
            throw new RuntimeException("Can't save log schema");
        }
        return savedSchema;
    }

    @Override
    public void removeLogSchemasByAppId(String applicationId) {
        validateId(applicationId, "Can't remove log schema. Invalid application id: " + applicationId);
        List<LogSchema> schemas = logSchemaDao.findByApplicationId(applicationId);
        if (schemas != null && !schemas.isEmpty()) {
            LOG.debug("Remove log shemas by application id {}", applicationId);
            for (LogSchema schema : schemas) {
                removeLogSchemaById(schema.getStringId());
            }
        }
    }

    @Override
    public void removeLogSchemaById(String id) {
        validateId(id, "Can't remove log schema. Invalid log schema id: " + id);
        logSchemaDao.removeById(id);
        LOG.debug("Removed log schema [{}]", id);
    }

}
