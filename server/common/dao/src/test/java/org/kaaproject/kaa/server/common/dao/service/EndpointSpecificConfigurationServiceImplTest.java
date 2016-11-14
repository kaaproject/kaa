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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.impl.EndpointSpecificConfigurationDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointSpecificConfiguration;
import org.springframework.util.Base64Utils;


public class EndpointSpecificConfigurationServiceImplTest {

  private static final byte[] KEY = Base64Utils.decodeFromString("keyHasg=");
  private static final String APP_ID = "app";
  private static final String CONFIG_BODY = "body";
  private static final int CONF_VERSION = 7;
  private static final EndpointSpecificConfigurationServiceImpl SERVICE = new EndpointSpecificConfigurationServiceImpl();
  private EndpointSpecificConfigurationDao daoMock = mock(EndpointSpecificConfigurationDao.class);
  private EndpointService endpointServiceMock = mock(EndpointService.class);
  private ConfigurationService configurationServiceMock = mock(ConfigurationService.class);
  private EndpointSpecificConfiguration configuration = mock(EndpointSpecificConfiguration.class);
  private EndpointSpecificConfigurationDto configurationDto = mock(EndpointSpecificConfigurationDto.class);

  @Before
  public void setUp() throws Exception {
    SERVICE.setEndpointService(endpointServiceMock);
    SERVICE.setConfigurationService(configurationServiceMock);
    SERVICE.setEndpointSpecificConfigurationDao(daoMock);
  }

  @Test
  public void testShouldSaveWithActiveConfigSchemaVersion() {
    EndpointSpecificConfigurationDto dto = new EndpointSpecificConfigurationDto();
    dto.setConfigurationSchemaVersion(null);
    dto.setEndpointKeyHash(KEY);
    dto.setConfiguration(CONFIG_BODY);
    when(endpointServiceMock.findEndpointProfileByKeyHash(KEY)).thenReturn(generateProfile());
    when(configurationServiceMock.normalizeAccordingToOverrideConfigurationSchema(APP_ID, CONF_VERSION, CONFIG_BODY)).thenReturn("valid body");
    when(daoMock.save(dto)).thenReturn(configuration);
    when(configuration.toDto()).thenReturn(new EndpointSpecificConfigurationDto());
    Assert.assertTrue(SERVICE.save(dto) != null);
    verify(configurationServiceMock).normalizeAccordingToOverrideConfigurationSchema(APP_ID, CONF_VERSION, CONFIG_BODY);
    verify(endpointServiceMock).findEndpointProfileByKeyHash(KEY);
    verify(daoMock).save(dto);
  }

  @Test
  public void testShouldSaveWithProvidedConfigSchemaVersion() {
    EndpointSpecificConfigurationDto dto = new EndpointSpecificConfigurationDto();
    dto.setConfigurationSchemaVersion(7);
    dto.setEndpointKeyHash(KEY);
    dto.setConfiguration(CONFIG_BODY);
    when(endpointServiceMock.findEndpointProfileByKeyHash(KEY)).thenReturn(generateProfile());
    when(configurationServiceMock.normalizeAccordingToOverrideConfigurationSchema(APP_ID, 7, CONFIG_BODY)).thenReturn("valid body");
    when(daoMock.save(dto)).thenReturn(configuration);
    when(configuration.toDto()).thenReturn(new EndpointSpecificConfigurationDto());
    Assert.assertTrue(SERVICE.save(dto) != null);
    verify(configurationServiceMock).normalizeAccordingToOverrideConfigurationSchema(APP_ID, 7, CONFIG_BODY);
    verify(endpointServiceMock).findEndpointProfileByKeyHash(KEY);
    verify(daoMock).save(dto);
  }

  @Test
  public void testShouldActiveConfigurationByEndpointProfile() {
    when(daoMock.findByEndpointKeyHashAndConfigurationVersion(KEY, CONF_VERSION)).thenReturn(configuration);
    when(configuration.toDto()).thenReturn(new EndpointSpecificConfigurationDto());
    Assert.assertTrue(SERVICE.findActiveConfigurationByEndpointProfile(generateProfile()).isPresent());
    verify(daoMock).findByEndpointKeyHashAndConfigurationVersion(KEY, CONF_VERSION);
  }

  @Test
  public void testShouldFindActiveConfigurationByEndpointKeyHash() {
    EndpointSpecificConfigurationDto dto = new EndpointSpecificConfigurationDto();
    dto.setConfigurationSchemaVersion(CONF_VERSION);
    when(endpointServiceMock.findEndpointProfileByKeyHash(KEY)).thenReturn(generateProfile());
    when(daoMock.findByEndpointKeyHashAndConfigurationVersion(KEY, CONF_VERSION)).thenReturn(configuration);
    when(configuration.toDto()).thenReturn(dto);
    Assert.assertTrue(SERVICE.findActiveConfigurationByEndpointKeyHash(KEY).isPresent());
    verify(endpointServiceMock).findEndpointProfileByKeyHash(KEY);
    verify(daoMock).findByEndpointKeyHashAndConfigurationVersion(KEY, CONF_VERSION);
  }

  @Test
  public void testShouldNotFindActiveConfigurationByEndpointKeyHashWhenProfileNotFound() {
    when(endpointServiceMock.findEndpointProfileByKeyHash(KEY)).thenReturn(null);
    Assert.assertFalse(SERVICE.findActiveConfigurationByEndpointKeyHash(KEY).isPresent());
    verify(endpointServiceMock).findEndpointProfileByKeyHash(KEY);
  }

  @Test
  public void testShouldDeleteActiveConfigurationByEndpointKeyHash() {
    when(endpointServiceMock.findEndpointProfileByKeyHash(KEY)).thenReturn(generateProfile());
    when(daoMock.findByEndpointKeyHashAndConfigurationVersion(KEY, CONF_VERSION)).thenReturn(configuration);
    when(configuration.toDto()).thenReturn(configurationDto);
    when(configurationDto.getConfigurationSchemaVersion()).thenReturn(CONF_VERSION);
    Assert.assertTrue(SERVICE.deleteActiveConfigurationByEndpointKeyHash(KEY).isPresent());
    verify(endpointServiceMock).findEndpointProfileByKeyHash(KEY);
    verify(daoMock).findByEndpointKeyHashAndConfigurationVersion(KEY, CONF_VERSION);
    verify(daoMock).removeByEndpointKeyHashAndConfigurationVersion(KEY, CONF_VERSION);
  }

  @Test
  public void testShouldDeleteByEndpointKeyHashAndConfSchemaVersion() {
    EndpointProfileDto profile = generateProfile();
    profile.setConfigurationVersion(0);
    when(endpointServiceMock.findEndpointProfileByKeyHash(KEY)).thenReturn(profile);
    when(daoMock.findByEndpointKeyHashAndConfigurationVersion(KEY, CONF_VERSION)).thenReturn(configuration);
    when(configuration.toDto()).thenReturn(configurationDto);
    when(configurationServiceMock.findConfSchemaByAppIdAndVersion(APP_ID, CONF_VERSION)).thenReturn(new ConfigurationSchemaDto());
    when(configurationDto.getConfigurationSchemaVersion()).thenReturn(CONF_VERSION);
    Assert.assertTrue(SERVICE.deleteByEndpointKeyHashAndConfSchemaVersion(KEY, CONF_VERSION).isPresent());
    verify(endpointServiceMock).findEndpointProfileByKeyHash(KEY);
    verify(daoMock).findByEndpointKeyHashAndConfigurationVersion(KEY, CONF_VERSION);
    verify(daoMock).removeByEndpointKeyHashAndConfigurationVersion(KEY, CONF_VERSION);
    verify(configurationServiceMock).findConfSchemaByAppIdAndVersion(APP_ID, CONF_VERSION);
  }

  @Test
  public void testShouldNotDeleteActiveConfigurationByEndpointKeyHashWhenProfileNotFound() {
    when(endpointServiceMock.findEndpointProfileByKeyHash(KEY)).thenReturn(null);
    Assert.assertFalse(SERVICE.deleteActiveConfigurationByEndpointKeyHash(KEY).isPresent());
    verify(endpointServiceMock).findEndpointProfileByKeyHash(KEY);
  }

  private EndpointProfileDto generateProfile() {
    EndpointProfileDto profile = new EndpointProfileDto();
    profile.setEndpointKeyHash(KEY);
    profile.setConfigurationVersion(CONF_VERSION);
    profile.setApplicationId(APP_ID);
    return profile;
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(daoMock, configurationServiceMock, endpointServiceMock);
  }
}