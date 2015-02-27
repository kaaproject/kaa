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
package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserConfigurationDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointConfiguration;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointProfile;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointUserConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class AbstractMongoTest extends AbstractTest {

    @Autowired
    protected EndpointConfigurationDao<MongoEndpointConfiguration> endpointConfigurationDao;
    @Autowired
    protected EndpointProfileDao<MongoEndpointProfile> endpointProfileDao;

    @Autowired
    protected EndpointUserConfigurationDao<MongoEndpointUserConfiguration> userConfigurationMongoDao;

    protected EndpointProfileDto generateEndpointProfile(String appId, List<String> topicIds) {
        EndpointProfileDto profileDto = new EndpointProfileDto();
        profileDto.setApplicationId(appId);
        profileDto.setSubscriptions(topicIds);
        profileDto.setEndpointKeyHash("TEST_KEY_HASH".getBytes());
        return endpointProfileDao.save(new MongoEndpointProfile(profileDto)).toDto();
    }

    protected EndpointConfigurationDto generateEndpointConfiguration() {
        EndpointConfigurationDto configurationDto = new EndpointConfigurationDto();
        configurationDto.setConfigurationHash(UUID.randomUUID().toString().getBytes());
        configurationDto.setConfiguration(UUID.randomUUID().toString().getBytes());
        return endpointConfigurationDao.save(new MongoEndpointConfiguration(configurationDto)).toDto();
    }

    protected EndpointUserConfigurationDto generateEndpointUserConfiguration(EndpointUserDto endpointUser, ApplicationDto applicationDto, Integer schemaVersion) {
        EndpointUserConfigurationDto configurationDto = new EndpointUserConfigurationDto();
        if (endpointUser == null) {
            endpointUser = generateEndpointUser(null);
        }
        configurationDto.setUserId(endpointUser.getId());
        if (schemaVersion == null) {
            schemaVersion = 1;
        }
        configurationDto.setSchemaVersion(schemaVersion);
        configurationDto.setBody(UUID.randomUUID().toString().getBytes());
        if (applicationDto == null) {
            applicationDto = generateApplication();
        }
        configurationDto.setAppToken(applicationDto.getApplicationToken());
        userConfigurationMongoDao.save(new MongoEndpointUserConfiguration(configurationDto));
        return configurationDto;
    }
}
