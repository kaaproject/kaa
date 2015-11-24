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

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.server.common.dao.ServerProfileService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.impl.ServerProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.kaaproject.kaa.server.common.dao.model.sql.ServerProfileSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;

@Service
@Transactional
public class ServerProfileServiceImpl implements ServerProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(ServerProfileServiceImpl.class);

    @Autowired
    private ServerProfileSchemaDao<ServerProfileSchema> serverProfileSchemaDao;
    @Autowired
    private EndpointProfileDao<EndpointProfile> endpointProfileDao;

    @Override
    public ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto dto) {
        return getDto(serverProfileSchemaDao.save(new ServerProfileSchema(dto)));
    }

    @Override
    public ServerProfileSchemaDto findLatestServerProfileSchema(String appId) {
        return getDto(serverProfileSchemaDao.findLatestByAppId(appId));
    }

    @Override
    public ServerProfileSchemaDto findServerProfileSchema(String schemaId) {
        return getDto(serverProfileSchemaDao.findLatestByAppId(schemaId));
    }

    @Override
    public List<ServerProfileSchemaDto> findServerProfileSchemasByAppId(String appId) {
        return convertDtoList(serverProfileSchemaDao.findByAppId(appId));
    }

    @Override
    public void removeServerProfileSchemaById(String profileId) {
        serverProfileSchemaDao.removeById(profileId);
    }

    @Override
    public void removeServerProfileSchemaByAppId(String appId) {
        serverProfileSchemaDao.removeByAppId(appId);
    }

    @Override
    public EndpointProfileDto saveServerProfile(byte[] keyHash, String serverProfile) {
        EndpointProfile ep = endpointProfileDao.findById(ByteBuffer.wrap(keyHash));
        if (ep != null) {
            String schemaId = ep.getServerProfileSchemaId();
            ep = endpointProfileDao.updateProfileServer(keyHash, schemaId, serverProfile);
        } else {
            throw new DatabaseProcessingException("Can't find endpoint profile ");
        }
        return ep != null ? ep.toDto() : null;
    }

    @Override
    public ServerProfileSchemaDto findServerProfileSchemaByKeyHash(byte[] keyHash) {
        ServerProfileSchemaDto schemaDto = null;
        EndpointProfile ep = endpointProfileDao.findById(ByteBuffer.wrap(keyHash));
        if (ep != null) {
            String schemaId = ep.getServerProfileSchemaId();
            if (isNotBlank(schemaId)) {
                schemaDto = DaoUtil.getDto(serverProfileSchemaDao.findById(schemaId));
            }
        }
        return schemaDto;
    }
}
