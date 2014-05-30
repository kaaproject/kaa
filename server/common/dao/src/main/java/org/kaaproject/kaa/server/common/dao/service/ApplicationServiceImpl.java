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

package org.kaaproject.kaa.server.common.dao.service;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.getStringFromFile;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidObject;

import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.ProcessingStatus;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.server.common.dao.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ConfigurationDao;
import org.kaaproject.kaa.server.common.dao.ConfigurationSchemaDao;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.NotificationService;
import org.kaaproject.kaa.server.common.dao.ProfileFilterDao;
import org.kaaproject.kaa.server.common.dao.ProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.TopicService;
import org.kaaproject.kaa.server.common.dao.UserDao;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.mongo.model.Application;
import org.kaaproject.kaa.server.common.dao.mongo.model.Configuration;
import org.kaaproject.kaa.server.common.dao.mongo.model.ConfigurationSchema;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileFilter;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileSchema;
import org.kaaproject.kaa.server.common.dao.mongo.model.Update;
import org.kaaproject.kaa.server.common.dao.mongo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private static final String GROUP_ALL = "All";

    private static final String DEFAULT_PROFILE_SCHEMA_FILE = "/default_profile_schema.json";
    private static final String DEFAULT_CONFIGURATION_SCHEMA_FILE = "/default_configuration_schema.json";
    private static final String DEFAULT_NOTIFICATION_SCHEMA_FILE = "/default_notification_schema.json";
    private static final String DEFAULT_SCHEMA_NAME = "Generated";
    private static final int DEFAULT_TOKEN_SIZE = 15;

    @Autowired
    private ApplicationDao<Application> applicationDao;

    @Autowired
    private EndpointService endpointService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserDao<User> userDao;

    @Override
    public List<ApplicationDto> findAppsByTenantId(String tenantId) {
        List<ApplicationDto> applicationDto;
        if (isValidId(tenantId)) {
            LOG.debug("Find applications by tenant id [{}]", tenantId);
            applicationDto = convertDtoList(applicationDao.findByTenantId(tenantId));
        } else {
            throw new IncorrectParameterException("Incorrect tenant id: " + tenantId);
        }
        return applicationDto;
    }

    @Override
    public void removeAppsByTenantId(String tenantId) {
        if (isValidId(tenantId)) {
            List<ApplicationDto> applications = findAppsByTenantId(tenantId);
            for (ApplicationDto application : applications) {
                removeAppById(application.getId());
            }
        }
    }

    @Override
    public ApplicationDto findAppById(String id) {
        ApplicationDto applicationDto = null;
        if (isValidId(id)) {
            applicationDto = getDto(applicationDao.findById(id));
        }
        return applicationDto;
    }

    @Override
    public void removeAppById(String id) {
        if (isValidId(id)) {
            removeCascadeApplication(id);
        }
    }

    @Override
    public ApplicationDto findAppByApplicationToken(String applicationToken) {
        ApplicationDto applicationDto = null;
        if (isNotBlank(applicationToken)) {
            applicationDto = getDto(applicationDao.findByApplicationToken(applicationToken));
        }
        return applicationDto;
    }

    @Override
    public ApplicationDto saveApp(ApplicationDto applicationDto) {
        ApplicationDto appDto = null;
        if (isValidObject(applicationDto)) {
            if(isNotBlank(applicationDto.getId())) {
                LOG.debug("Update application with id [{}]", applicationDto.getId());
                appDto = getDto(applicationDao.save(new Application(applicationDto)));
                return appDto;
            }
            String appToken = RandomStringUtils.randomNumeric(DEFAULT_TOKEN_SIZE);
            applicationDto.setApplicationToken(appToken);

            Update update = new Update();
            update.setSequenceNumber(applicationDto.getSequenceNumber());
            update.setStatus(ProcessingStatus.IDLE);
            Application application = new Application(applicationDto);
            application.setUpdate(update);
            appDto = getDto(applicationDao.save(application));

            if(appDto != null) {
                String appId = appDto.getId();
                List<User> users = userDao.findByTenantIdAndAuthority(appDto.getTenantId(), KaaAuthorityDto.TENANT_ADMIN.name());
                String createdUsername = null;
                if (!users.isEmpty()) {
                    createdUsername = users.get(0).getUsername();
                }
                LOG.debug("Saved application with id [{}]", appId);
                EndpointGroupDto groupDto = createDefaultGroup(appId, createdUsername);

                if(groupDto != null) {
                    String groupId = groupDto.getId();
                    LOG.debug("Saved endpoint group with id [{}]", groupId);
                    ProfileFilterDto filter = createDefaultProfileWithSchema(appId, groupId, createdUsername);
                    ConfigurationDto configuration = createDefaultConfigurationWithSchema(appId, groupId, createdUsername);
                    if (filter == null || configuration == null) {
                        LOG.warn("Got error during creation application. Deleted application with id [{}]", appId);
                        removeCascadeApplication(appId);
                    }
                    LOG.debug("Creating default notification schema");
                    createDefaultNotificationSchema(appId, createdUsername);
                } else {
                    LOG.warn("Cant save default group for application with id [{}]", appId);
                    removeCascadeApplication(appId);
                }
            }
            LOG.debug("Inserted new application with");
        }
        return appDto;
    }

    private EndpointGroupDto createDefaultGroup(String appId, String createdUsername) {
        EndpointGroupDto endpointGroup = new EndpointGroupDto();
        endpointGroup.setName(GROUP_ALL);
        endpointGroup.setCreatedUsername(createdUsername);
        endpointGroup.setApplicationId(appId);
        return endpointService.saveEndpointGroup(endpointGroup);
    }

    private ProfileFilterDto createDefaultProfileWithSchema(String appId, String groupId, String createdUsername) {
        ProfileSchemaDto profileSchemaDto = new ProfileSchemaDto();
        profileSchemaDto.setApplicationId(appId);
        String schema = getStringFromFile(DEFAULT_PROFILE_SCHEMA_FILE, ApplicationServiceImpl.class);
        if (isNotBlank(schema)) {
            profileSchemaDto.setSchema(schema);
        } else {
            throw new RuntimeException("Can't read default profile schema.");
        }
        profileSchemaDto.setName(DEFAULT_SCHEMA_NAME);
        profileSchemaDto.setCreatedUsername(createdUsername);
        profileSchemaDto = profileService.saveProfileSchema(profileSchemaDto);

        if (profileSchemaDto != null) {
            return profileService.findLatestFilterBySchemaIdAndGroupId(profileSchemaDto.getId(), groupId);
        } else {
            throw new RuntimeException("Can't save default profile schema " + profileSchemaDto);
        }
    }

    private ConfigurationDto createDefaultConfigurationWithSchema(String appId, String groupId, String createdUsername) {
        ConfigurationSchemaDto schema = new ConfigurationSchemaDto();
        schema.setApplicationId(appId);
        String confSchema = getStringFromFile(DEFAULT_CONFIGURATION_SCHEMA_FILE, ApplicationServiceImpl.class);
        if (isNotBlank(confSchema)) {
            schema.setSchema(confSchema);
        } else {
            throw new RuntimeException("Can't read default configuration schema.");
        }
        schema.setName(DEFAULT_SCHEMA_NAME);
        schema.setCreatedUsername(createdUsername);
        ConfigurationSchemaDto savedSchema = configurationService.saveConfSchema(schema, groupId);
        ConfigurationDto config = configurationService.findConfigurationByAppIdAndVersion(savedSchema.getApplicationId(), savedSchema.getMajorVersion());
        if (config == null) {
            throw new RuntimeException("Can't find default configuration by schema id " + savedSchema.getId());
        } else {
            return config;
        }
    }

    private NotificationSchemaDto createDefaultNotificationSchema(String appId, String createdUsername) {
        NotificationSchemaDto schema = new NotificationSchemaDto();
        schema.setApplicationId(appId);
        String defSchema = getStringFromFile(DEFAULT_NOTIFICATION_SCHEMA_FILE, ApplicationServiceImpl.class);
        if (isNotBlank(defSchema)) {
            schema.setSchema(defSchema);
        } else {
            throw new RuntimeException("Can't read default notification schema.");
        }
        schema.setType(NotificationTypeDto.USER);
        schema.setName(DEFAULT_SCHEMA_NAME);
        schema.setCreatedUsername(createdUsername);

        return notificationService.saveNotificationSchema(schema);
    }

    private void removeCascadeApplication(String id) {
        configurationService.removeConfSchemasByAppId(id);
        endpointService.removeEndpointGroupByAppId(id);
        topicService.removeTopicsByAppId(id);
        notificationService.removeNotificationSchemasByAppId(id);
        endpointService.removeEndpointProfileByAppId(id);
        applicationDao.removeById(id);
    }

}
