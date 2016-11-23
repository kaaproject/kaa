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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.UserConfigurationService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointUser;
import org.kaaproject.kaa.server.common.dao.model.EndpointUserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserConfigurationServiceImpl implements UserConfigurationService {

  private static final Logger LOG = LoggerFactory.getLogger(UserConfigurationServiceImpl.class);

  @Autowired
  private ApplicationService applicationService;

  @Autowired
  private ConfigurationService configurationService;

  private EndpointUserConfigurationDao<EndpointUserConfiguration> endpointUserConfigurationDao;

  private EndpointUserDao<EndpointUser> endpointUserDao;

  @Override
  public EndpointUserConfigurationDto saveUserConfiguration(EndpointUserConfigurationDto userConfig) {
    EndpointUserConfigurationDto userConfigurationDto = null;
    if (userConfig != null) {
      String userConfigBody = userConfig.getBody();
      if (isNotBlank(userConfigBody)) {
        String appToken = userConfig.getAppToken();
        ApplicationDto applicationDto = applicationService.findAppByApplicationToken(appToken);
        if (applicationDto != null) {
          int schemaVersion = userConfig.getSchemaVersion();
          userConfig.setBody(configurationService.normalizeAccordingToOverrideConfigurationSchema(applicationDto.getId(), schemaVersion, userConfigBody));
          userConfigurationDto = getDto(endpointUserConfigurationDao.save(userConfig));
        } else {
          LOG.warn("Can't find application with token {} for endpoint user configuration.", appToken);
          throw new IncorrectParameterException("Can't find application for specified token.");
        }
      } else {
        LOG.warn("Invalid endpoint user configuration. Configuration body is empty");
        throw new IncorrectParameterException("Configuration body is empty.");
      }
    }
    return userConfigurationDto;
  }

  @Override
  public EndpointUserConfigurationDto findUserConfigurationByUserIdAndAppTokenAndSchemaVersion(
          String userId, String appToken, Integer schemaVersion) {
    return getDto(endpointUserConfigurationDao.findByUserIdAndAppTokenAndSchemaVersion(
            userId, appToken, schemaVersion));
  }

  @Override
  public List<EndpointUserConfigurationDto> findUserConfigurationByUserId(String userId) {
    return convertDtoList(endpointUserConfigurationDao.findByUserId(userId));
  }

  @Override
  public void removeByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken,
                                                        Integer schemaVersion) {
    endpointUserConfigurationDao.removeByUserIdAndAppTokenAndSchemaVersion(userId, appToken,
            schemaVersion);
  }

  @Override
  public EndpointUserConfigurationDto findUserConfigurationByExternalUIdAndAppTokenAndSchemaVersion(
      String externalUid,
      String appToken,
      Integer schemaVersion,
      String tenantId) {
    if (isNotBlank(externalUid)) {
      EndpointUser endpointUser = endpointUserDao.findByExternalIdAndTenantId(
              externalUid, tenantId);
      if (endpointUser != null) {
        return getDto(endpointUserConfigurationDao.findByUserIdAndAppTokenAndSchemaVersion(
            endpointUser.getId(),
            appToken,
            schemaVersion));
      } else {
        LOG.warn("Could not find endpoint user by externalUid:", externalUid);
        throw new IncorrectParameterException("Could not find endpoint user by externalUid");
      }
    } else {
      LOG.warn("external user id could not be null!");
      throw new IncorrectParameterException("externalUid could not be null!");
    }
  }


  public void setEndpointUserConfigurationDao(
          EndpointUserConfigurationDao<EndpointUserConfiguration> endpointUserConfigurationDao) {
    this.endpointUserConfigurationDao = endpointUserConfigurationDao;
  }

  public void setEndpointUserDao(EndpointUserDao<EndpointUser> endpointUserDao) {
    this.endpointUserDao = endpointUserDao;
  }
}
