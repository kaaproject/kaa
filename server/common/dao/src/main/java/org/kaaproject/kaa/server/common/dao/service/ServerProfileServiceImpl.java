/*
 * Copyright 2015 CyberVision, Inc.
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
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;

import java.nio.ByteBuffer;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.server.common.dao.ServerProfileService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.impl.ServerProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.kaaproject.kaa.server.common.dao.model.sql.ServerProfileSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServerProfileServiceImpl implements ServerProfileService {
    
    private static final Logger LOG = LoggerFactory.getLogger(ServerProfileServiceImpl.class);

    @Autowired
    private ServerProfileSchemaDao<ServerProfileSchema> serverProfileSchemaDao;

    private EndpointProfileDao<EndpointProfile> endpointProfileDao;

    @Override
    @Transactional
    public ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto dto) {
        Validator.validateObject(dto, "Incorrect server profile schema object.");
        String appId = dto.getApplicationId();
        if (isNotBlank(appId)) {
            String id = dto.getId();
            if (isBlank(id)) {
                ServerProfileSchema serverProfileSchema = serverProfileSchemaDao.findLatestByAppId(appId);
                int version = 0;
                if (serverProfileSchema != null) {
                    version = serverProfileSchema.getVersion();
                }
                dto.setVersion(++version);
                dto.setCreatedTime(System.currentTimeMillis());
            } else {
                ServerProfileSchemaDto oldServerProfileSchemaDto = getDto(serverProfileSchemaDao.findById(id));
                if (oldServerProfileSchemaDto != null) {
                    oldServerProfileSchemaDto.editFields(dto);
                    dto = oldServerProfileSchemaDto;
                } else {
                    LOG.error("Can't find server profile schema with given id [{}].", id);
                    throw new IncorrectParameterException("Invalid server profile schema id: " + id);
                }
            }
            return getDto(serverProfileSchemaDao.save(new ServerProfileSchema(dto)));
        } else {
            throw new IncorrectParameterException("Invalid server profile schema object. Application identifier is not specified!");
        }
    }

    @Override
    @Transactional
    public ServerProfileSchemaDto findLatestServerProfileSchema(String appId) {
        Validator.validateId(appId, "Incorrect application id.");
        return getDto(serverProfileSchemaDao.findLatestByAppId(appId));
    }

    @Override
    @Transactional
    public ServerProfileSchemaDto findServerProfileSchema(String schemaId) {
        Validator.validateId(schemaId, "Incorrect server profile schema  id.");
        return getDto(serverProfileSchemaDao.findById(schemaId));
    }

    @Override
    @Transactional
    public List<ServerProfileSchemaDto> findServerProfileSchemasByAppId(String appId) {
        Validator.validateId(appId, "Incorrect application id.");
        return convertDtoList(serverProfileSchemaDao.findByAppId(appId));
    }

    @Override
    @Transactional
    public void removeServerProfileSchemaById(String profileId) {
        Validator.validateId(profileId, "Incorrect server profile schema  id.");
        serverProfileSchemaDao.removeById(profileId);
    }

    @Override
    @Transactional
    public void removeServerProfileSchemaByAppId(String appId) {
        Validator.validateId(appId, "Incorrect application id.");
        serverProfileSchemaDao.removeByAppId(appId);
    }

    @Override
    public EndpointProfileDto saveServerProfile(byte[] keyHash, String serverProfile) {
        Validator.validateHash(keyHash, "Incorrect endpoint key hash.");
        EndpointProfile ep = endpointProfileDao.findById(ByteBuffer.wrap(keyHash));
        if (ep != null) {
            String schemaId = ep.getServerProfileSchemaId();
            ep = endpointProfileDao.updateServerProfile(keyHash, schemaId, serverProfile);
        } else {
            throw new DatabaseProcessingException("Can't find endpoint profile by key hash " + keyHash);
        }
        return ep != null ? ep.toDto() : null;
    }

    @Override
    public ServerProfileSchemaDto findServerProfileSchemaByKeyHash(byte[] keyHash) {
        Validator.validateHash(keyHash, "Incorrect endpoint key hash.");
        ServerProfileSchemaDto schemaDto = null;
        EndpointProfile ep = endpointProfileDao.findById(ByteBuffer.wrap(keyHash));
        if (ep != null) {
            String schemaId = ep.getServerProfileSchemaId();
            if (isNotBlank(schemaId)) {
                schemaDto = findServerProfileSchema(schemaId);
            }
        }
        return schemaDto;
    }

    public void setEndpointProfileDao(EndpointProfileDao<EndpointProfile> endpointProfileDao) {
        this.endpointProfileDao = endpointProfileDao;
    }
}
