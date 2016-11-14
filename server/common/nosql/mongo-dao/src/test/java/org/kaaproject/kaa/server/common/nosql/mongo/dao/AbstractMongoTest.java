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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.CredentialsDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointRegistrationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointSpecificConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.TopicListEntryDao;
import org.kaaproject.kaa.server.common.dao.model.Credentials;
import org.kaaproject.kaa.server.common.dao.model.EndpointSpecificConfiguration;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointConfiguration;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointProfile;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointRegistration;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointUserConfiguration;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoTopicListEntry;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class AbstractMongoTest extends AbstractTest {

  @Autowired
  protected EndpointConfigurationDao<MongoEndpointConfiguration> endpointConfigurationDao;
  @Autowired
  protected EndpointUserConfigurationDao<MongoEndpointUserConfiguration> endpointUserConfigurationDao;
  @Autowired
  protected EndpointProfileDao<MongoEndpointProfile> endpointProfileDao;
  @Autowired
  protected TopicListEntryDao<MongoTopicListEntry> topicListEntryDao;
  @Autowired
  protected CredentialsDao<Credentials> credentialsDao;
  @Autowired
  protected EndpointRegistrationDao<MongoEndpointRegistration> endpointRegistrationDao;
  @Autowired
  protected EndpointSpecificConfigurationDao<EndpointSpecificConfiguration> endpointSpecificConfigurationDao;

  protected EndpointConfigurationDto generateEndpointConfiguration() {
    EndpointConfigurationDto configurationDto = new EndpointConfigurationDto();
    configurationDto.setConfigurationHash(UUID.randomUUID().toString().getBytes());
    configurationDto.setConfiguration(UUID.randomUUID().toString().getBytes());
    return endpointConfigurationDao.save(new MongoEndpointConfiguration(configurationDto)).toDto();
  }

  /**
   * Constructs security credentials with the information provided and saves it
   * to the database.
   *
   * @param applicationId   The application ID
   * @param credentialsBody The actual security credentials
   * @param status          The security credentials status
   * @return The security credentials saved
   */
  protected CredentialsDto generateCredentials(String applicationId, byte[] credentialsBody, CredentialsStatus status) {
    CredentialsDto credentials = new CredentialsDto(credentialsBody, status);
    return this.credentialsDao.save(applicationId, credentials).toDto();
  }

  /**
   * Constructs an endpoint registration with the information provided and
   * saves it to the database.
   *
   * @param applicationId The application ID
   * @param endpointId    The endpoint ID
   * @param credentialsId The credentials ID
   * @return The endpoint registration saved
   */
  protected EndpointRegistrationDto generateEndpointRegistration(String applicationId, String endpointId, String credentialsId) {
    EndpointRegistrationDto endpointRegistration = new EndpointRegistrationDto(applicationId, endpointId, credentialsId, null, null);
    return this.endpointRegistrationDao.save(endpointRegistration).toDto();
  }

  /**
   * Constructs an endpoint specific configuration with the information provided and
   * saves it to the database.
   *
   * @param endpointKeyHash      The endpoint key hash
   * @param configurationVersion The endpoint configuration version
   * @param configuration        The configuration body
   * @param version              The endpoint specific configuration version
   * @return saved endpoint specific configuration
   */
  protected EndpointSpecificConfigurationDto generateEndpointSpecificConfigurationDto(byte[] endpointKeyHash, Integer configurationVersion, String configuration, Long version) {
    EndpointSpecificConfigurationDto dto = new EndpointSpecificConfigurationDto(endpointKeyHash, configurationVersion, configuration, version);
    return endpointSpecificConfigurationDao.save(dto).toDto();
  }
}
