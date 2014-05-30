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

package org.kaaproject.kaa.server.control.service.thrift;

import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDataStruct;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDataStructList;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toGenericDataStruct;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toGenericDataStructList;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toGenericDto;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.thrift.TException;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeConfigurationNotification;
import org.kaaproject.kaa.common.dto.ChangeNotificationDto;
import org.kaaproject.kaa.common.dto.ChangeProfileFilterNotification;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TenantAdminDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.UpdateNotificationDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.NotificationService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.TopicService;
import org.kaaproject.kaa.server.common.dao.UserService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.control.Sdk;
import org.kaaproject.kaa.server.common.thrift.gen.control.SdkPlatform;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Operation;
import org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct;
import org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter;
import org.kaaproject.kaa.server.common.thrift.util.ThriftExecutor;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.kaaproject.kaa.server.control.service.sdk.SdkGenerator;
import org.kaaproject.kaa.server.control.service.sdk.SdkGeneratorFactory;
import org.kaaproject.kaa.server.control.service.zk.ControlZkService;
import org.kaaproject.kaa.server.operations.service.delta.DefaultDeltaCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The Class ControlThriftServiceImpl.<br>
 * Implementation of Control Service Thrift Interface.
 */
@Service
public class ControlThriftServiceImpl extends BaseCliThriftService implements
        ControlThriftService.Iface {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(ControlThriftServiceImpl.class);

    /** The control service. */
    @Autowired
    private ControlService controlService;
    
    /** The user service. */
    @Autowired
    private UserService userService;

    /** The application service. */
    @Autowired
    private ApplicationService applicationService;

    /** The configuration service. */
    @Autowired
    private ConfigurationService configurationService;

    /** The profile service. */
    @Autowired
    private ProfileService profileService;

    /** The endpoint service. */
    @Autowired
    private EndpointService endpointService;

    /** The notification service. */
    @Autowired
    private NotificationService notificationService;

    /** The topic service. */
    @Autowired
    private TopicService topicService;

    /** The control zookeeper service. */
    @Autowired
    private ControlZkService controlZKService;
    
    /** The thrift host. */
    @Value("#{properties[build_version]}")
    private String buildVersion;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getTenants()
     */
    /* CLI method */
    @Override
    public List<DataStruct> getTenants() throws TException {
        return toDataStructList(userService.findAllTenants());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getTenant(java.lang.String)
     */
    /* CLI method */
    @Override
    public DataStruct getTenant(String tenantId) throws TException {
        return toDataStruct(userService.findTenantById(tenantId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .
     * Iface#editTenant(org.kaaproject.kaa.server.common.thrift.gen.shared
     * .DataStruct)
     */
    /* CLI method */
    @Override
    public DataStruct editTenant(DataStruct tenant) throws TException {
        return toDataStruct(userService.saveTenant(ThriftDtoConverter
                .<TenantDto>toDto(tenant)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deleteTenant(java.lang.String)
     */
    /* CLI method */
    @Override
    public void deleteTenant(String tenantId) throws TException {
        userService.removeTenantById(tenantId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getUsers()
     */
    /* CLI method */
    @Override
    public List<DataStruct> getUsers() throws TException {
        return toDataStructList(userService.findAllUsers());
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getTenantUsers(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getTenantUsers(String tenantId)
            throws TException {
        return toDataStructList(userService.findAllTenantUsers(tenantId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getUser(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct getUser(String userId) throws TException {
        return toDataStruct(userService.findUserById(userId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getUserByExternalUid(java.lang.String)
     */
    /* GUI method */
    @Override
    public DataStruct getUserByExternalUid(String uid) throws TException {
        return toDataStruct(userService.findUserByExternalUid(uid));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#editUser(org.kaaproject.kaa.server.common.thrift.gen.shared.
     * DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editUser(DataStruct user) throws TException {
        return toDataStruct(userService.saveUser(ThriftDtoConverter
                .<UserDto>toDto(user)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deleteUser(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public void deleteUser(String userId) throws TException {
        userService.removeUserById(userId);
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getTenantAdmins()
     */
    /* GUI method */
    @Override
    public List<DataStruct> getTenantAdmins() throws TException {
        return toDataStructList(userService.findAllTenantAdmins());
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getTenantAdmin(java.lang.String)
     */
    /* GUI method */
    @Override
    public DataStruct getTenantAdmin(String tenantId)
            throws TException {
        return toDataStruct(userService.findTenantAdminById(tenantId));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#editTenantAdmin(org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct)
     */
    /* GUI method */
    @Override
    public DataStruct editTenantAdmin(DataStruct tenantAdmin)
            throws TException {
        return toDataStruct(userService.saveTenantAdmin(ThriftDtoConverter
                .<TenantAdminDto>toDto(tenantAdmin)));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#deleteTenantAdmin(java.lang.String)
     */
    /* GUI method */
    @Override
    public void deleteTenantAdmin(String tenantId)
            throws TException {
        userService.removeTenantAdminById(tenantId);
    }    

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getApplication(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct getApplication(String applicationId) throws TException {
        return toDataStruct(applicationService
                .findAppById(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getApplicationsByTenantId(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public List<DataStruct> getApplicationsByTenantId(String tenantId)
            throws TException {
        return toDataStructList(applicationService.findAppsByTenantId(tenantId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .
     * Iface#editApplication(org.kaaproject.kaa.server.common.thrift.gen.shared
     * .DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editApplication(DataStruct application) throws TException {
        return toDataStruct(applicationService
                .saveApp(ThriftDtoConverter
                        .<ApplicationDto>toDto(application)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deleteApplication(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public void deleteApplication(String applicationId) throws TException {
        applicationService.removeAppById(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getConfigurationSchemasByApplicationId(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public List<DataStruct> getConfigurationSchemasByApplicationId(
            String applicationId) throws TException {
        return toDataStructList(configurationService
                .findConfSchemasByAppId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getConfigurationSchema(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct getConfigurationSchema(String configurationSchemaId)
            throws TException {
        return toDataStruct(configurationService
                .findConfSchemaById(configurationSchemaId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .
     * Iface#editConfigurationSchema(org.kaaproject.kaa.server.common.thrift
     * .gen.shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editConfigurationSchema(DataStruct configurationSchema)
            throws TException {
        ConfigurationSchemaDto confSchema = null;
        try {
            confSchema = configurationService.saveConfSchema(ThriftDtoConverter.<ConfigurationSchemaDto>toDto(configurationSchema));
        } catch (IncorrectParameterException e) {
            LOG.error("Can't generate protocol schema. Can't save configuration schema.");
            throw new TException(e);
        }
        return toDataStruct(confSchema);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getProfileSchemasByApplicationId(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public List<DataStruct> getProfileSchemasByApplicationId(
            String applicationId) throws TException {
        return toDataStructList(profileService
                .findProfileSchemasByAppId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getProfileSchema(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct getProfileSchema(String profileSchemaId)
            throws TException {
        return toDataStruct(profileService
                .findProfileSchemaById(profileSchemaId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .
     * Iface#editProfileSchema(org.kaaproject.kaa.server.common.thrift.gen.
     * shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editProfileSchema(DataStruct profileSchema)
            throws TException {
        return toDataStruct(profileService.saveProfileSchema(ThriftDtoConverter
                .<ProfileSchemaDto>toDto(profileSchema)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getEndpointGroupsByApplicationId(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public List<DataStruct> getEndpointGroupsByApplicationId(
            String applicationId) throws TException {
        return toDataStructList(endpointService
                .findEndpointGroupsByAppId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getEndpointGroup(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct getEndpointGroup(String endpointGroupId)
            throws TException {
        return toDataStruct(endpointService
                .findEndpointGroupById(endpointGroupId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .
     * Iface#editEndpointGroup(org.kaaproject.kaa.server.common.thrift.gen.
     * shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editEndpointGroup(DataStruct endpointGroup)
            throws TException {
        return toDataStruct(endpointService
                .saveEndpointGroup(ThriftDtoConverter
                        .<EndpointGroupDto>toDto(endpointGroup)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deleteEndpointGroup(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public void deleteEndpointGroup(String endpointGroupId) throws TException {
        endpointService.removeEndpointGroupById(endpointGroupId);
    }

    /**
     * Removes the topics from endpoint group.
     *
     * @param endpointGroupId the endpoint group id
     * @param topicId the topic id
     * @return the data struct
     * @throws TException the t exception
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct removeTopicsFromEndpointGroup(String endpointGroupId, String topicId) throws TException {
        return notifyAndGetPayload(endpointService.removeTopicFromEndpointGroup(endpointGroupId, topicId));
    }

    /**
     * Adds the topics to endpoint group.
     *
     * @param endpointGroupId the endpoint group id
     * @param topicId the topic id
     * @return the data struct
     * @throws TException the t exception
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct addTopicsToEndpointGroup(String endpointGroupId, String topicId) throws TException {
        return notifyAndGetPayload(endpointService.addTopicToEndpointGroup(endpointGroupId, topicId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getProfileFilter(java.lang.String)
     */
    /* CLI method */
    @Override
    public DataStruct getProfileFilter(String profileFilterId)
            throws TException {
        return toDataStruct(profileService
                .findProfileFilterById(profileFilterId));
    }
    
    /* GUI method */
    @Override
    public List<DataStruct> getProfileFilterRecordsByEndpointGroupId(
            String endpointGroupId, boolean includeDeprecated) throws TException {
        return toGenericDataStructList(profileService.findAllProfileFilterRecordsByEndpointGroupId(endpointGroupId, includeDeprecated));
    }

    /* GUI method */
    @Override
    public DataStruct getProfileFilterRecord(String schemaId,
            String endpointGroupId) throws TException {
        return toGenericDataStruct(profileService.findProfileFilterRecordBySchemaIdAndEndpointGroupId(schemaId, endpointGroupId));
    }
    
    /* GUI method */
    @Override
    public List<DataStruct> getVacantProfileSchemasByEndpointGroupId(
            String endpointGroupId) throws TException {
        return toDataStructList(profileService.findVacantSchemasByEndpointGroupId(endpointGroupId));
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .
     * Iface#editProfileFilter(org.kaaproject.kaa.server.common.thrift.gen.
     * shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editProfileFilter(DataStruct profileFilter)
            throws TException {
        return toDataStruct(profileService.saveProfileFilter(ThriftDtoConverter
                .<ProfileFilterDto> toDto(profileFilter)));
    }

    /* GUI method */
    @Override
    public List<DataStruct> getConfigurationRecordsByEndpointGroupId(
            String endpointGroupId, boolean includeDeprecated) throws TException {
        return toGenericDataStructList(configurationService.findAllConfigurationRecordsByEndpointGroupId(endpointGroupId, includeDeprecated));
    }

    /* GUI method */
    @Override
    public DataStruct getConfigurationRecord(String schemaId,
            String endpointGroupId) throws TException {
        return toGenericDataStruct(configurationService.findConfigurationRecordBySchemaIdAndEndpointGroupId(schemaId, endpointGroupId));
    }

    /* GUI method */
    @Override
    public List<DataStruct> getVacantConfigurationSchemasByEndpointGroupId(
            String endpointGroupId) throws TException {
        return toDataStructList(configurationService.findVacantSchemasByEndpointGroupId(endpointGroupId));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getConfiguration(java.lang.String)
     */
    /* CLI method */
    @Override
    public DataStruct getConfiguration(String configurationId)
            throws TException {
        return toDataStruct(configurationService
                .findConfigurationById(configurationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .
     * Iface#editConfiguration(org.kaaproject.kaa.server.common.thrift.gen.
     * shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editConfiguration(DataStruct configuration)
            throws TException {
        ConfigurationDto cfg = configurationService.saveConfiguration(ThriftDtoConverter
                    .<ConfigurationDto> toDto(configuration));
        return toDataStruct(cfg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#activateConfiguration(java.lang.String, java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct activateConfiguration(String configurationId, String activatedUsername)
            throws TException {
        ChangeConfigurationNotification cfgNotification = configurationService
                .activateConfiguration(configurationId, activatedUsername);
        ChangeNotificationDto notification = cfgNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, null,
                    cfgNotification.getConfigurationDto());
        }
        return toDataStruct(cfgNotification.getConfigurationDto());
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deactivateConfiguration(java.lang.String, java.lang.String)
     */
    /* GUI method */
    @Override
    public DataStruct deactivateConfiguration(String configurationId,
            String deactivatedUsername) throws TException {
        ChangeConfigurationNotification cfgNotification = configurationService
                .deactivateConfiguration(configurationId, deactivatedUsername);
        ChangeNotificationDto notification = cfgNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, null,
                    cfgNotification.getConfigurationDto());
        }
        return toDataStruct(cfgNotification.getConfigurationDto());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deleteConfigurationRecord(java.lang.String, java.lang.String, java.lang.String)
     */
    /* GUI method */
    @Override
    public void deleteConfigurationRecord(String schemaId, String endpointGroupId,
            String deactivatedUsername) throws TException {
        ChangeConfigurationNotification cfgNotification = configurationService
                .deleteConfigurationRecord(schemaId, endpointGroupId, deactivatedUsername);
        if (cfgNotification != null) {
            ChangeNotificationDto notification = cfgNotification.getChangeNotificationDto();
            if (notification != null) {
                notifyEndpoints(notification, null,
                        cfgNotification.getConfigurationDto());
            }
        }
    }


    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#activateProfileFilter(java.lang.String, java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct activateProfileFilter(String profileFilterId, String activatedUsername) throws TException {
        ChangeProfileFilterNotification cpfNotification = profileService
                .activateProfileFilter(profileFilterId, activatedUsername);
        ChangeNotificationDto notification = cpfNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, cpfNotification.getProfileFilterDto(), null);
        }
        return toDataStruct(cpfNotification.getProfileFilterDto());
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#deactivateProfileFilter(java.lang.String, java.lang.String)
     */
    /* GUI method */
    @Override
    public DataStruct deactivateProfileFilter(String profileFilterId,
            String deactivatedUsername) throws TException {
        ChangeProfileFilterNotification cpfNotification = profileService
                .deactivateProfileFilter(profileFilterId, deactivatedUsername);
        ChangeNotificationDto notification = cpfNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, cpfNotification.getProfileFilterDto(), null);
        }
        return toDataStruct(cpfNotification.getProfileFilterDto());
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#deleteProfileFilterRecord(java.lang.String, java.lang.String, java.lang.String)
     */
    /* GUI method */
    @Override
    public void deleteProfileFilterRecord(String schemaId,
            String endpointGroupId, String deactivatedUsername)
            throws TException {
        ChangeProfileFilterNotification cpfNotification = profileService
                .deleteProfileFilterRecord(schemaId, endpointGroupId, deactivatedUsername);
        if (cpfNotification != null) {
            ChangeNotificationDto notification = cpfNotification.getChangeNotificationDto();
            if (notification != null) {
                notifyEndpoints(notification, cpfNotification.getProfileFilterDto(), null);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#generateSdk(org.kaaproject.kaa.server.common.thrift.gen.control.SdkPlatform, java.lang.String, int, int, int)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public Sdk generateSdk(SdkPlatform sdkPlatform, String applicationId,
            int profileSchemaVersion, int configurationSchemaVersion,
            int notificationSchemaVersion) throws TException {

        try {
            ApplicationDto application = applicationService.findAppById(applicationId);
            if (application == null) {
                throw new TException("Application not found!");
            }
            ProfileSchemaDto profileSchema = profileService.findProfileSchemaByAppIdAndVersion(applicationId, profileSchemaVersion);
            if (profileSchema == null) {
                throw new TException("Profile schema not found!");
            }            
            ConfigurationSchemaDto configurationShema = configurationService.findConfSchemaByAppIdAndVersion(applicationId, configurationSchemaVersion);
            if (configurationShema == null) {
                throw new TException("Configuration schema not found!");
            }
            ConfigurationDto defaultConfiguration = configurationService.findDefaultConfigurationBySchemaId(configurationShema.getId());
            if (defaultConfiguration == null) {
                throw new TException("Default configuration not found!");
            }
            NotificationSchemaDto notificationSchema = notificationService.findNotificationSchemaByAppIdAndTypeAndVersion(applicationId, NotificationTypeDto.USER, notificationSchemaVersion);
            if (notificationSchema == null) {
                throw new TException("Notification schema not found!");
            }
            String appToken = application.getApplicationToken();
            String profileSchemaBody = profileSchema.getSchema();
            String notificationSchemaBody = notificationSchema.getSchema();
            String configurationProtocolSchemaBody = configurationShema.getProtocolSchema();
            
            Schema deltaSchema = new Schema.Parser().parse(configurationProtocolSchemaBody);
            Schema baseSchema = new Schema.Parser().parse(configurationShema.getBaseSchema());
            DefaultDeltaCalculator calculator = new DefaultDeltaCalculator(deltaSchema, baseSchema);
            
            byte[] defaultConfigurationData = calculator.calculate(defaultConfiguration.getBody()).getData();
            
            SdkGenerator generator = SdkGeneratorFactory.createSdkGenerator(sdkPlatform);
            return generator.generateSdk(buildVersion, 
                    controlZKService.getCurrentBootstrapNodes(),
                    appToken, 
                    profileSchemaVersion, 
                    configurationSchemaVersion, 
                    notificationSchemaVersion, 
                    profileSchemaBody,
                    notificationSchemaBody,
                    configurationProtocolSchemaBody,
                    defaultConfigurationData);
        } catch (Exception e) {
            LOG.error("Unable to generate SDK", e);
            throw new TException(e);
        }
        
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#editNotificationSchema(org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editNotificationSchema(DataStruct notificationSchema) throws TException {
        return toDataStruct(notificationService.saveNotificationSchema(
                ThriftDtoConverter.<NotificationSchemaDto>toDto(notificationSchema)));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getNotificationSchema(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct getNotificationSchema(String notificationSchemaId) throws TException {
        return toDataStruct(notificationService.findNotificationSchemaById(notificationSchemaId));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getNotificationSchemasByAppId(java.lang.String)
     */
    /* CLI method */
    @Override
    public List<DataStruct> getNotificationSchemasByAppId(String applicationId) throws TException {
        return toDataStructList(notificationService.findNotificationSchemasByAppId(applicationId));
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getUserNotificationSchemasByAppId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getUserNotificationSchemasByAppId(String applicationId) throws TException {
        return toDataStructList(notificationService.findUserNotificationSchemasByAppId(applicationId));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#findNotificationSchemasByAppIdAndType(java.lang.String, org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct)
     */
    /* GUI method */
    @Override
    public List<DataStruct> findNotificationSchemasByAppIdAndType(String applicationId, DataStruct type) throws TException {
        NotificationTypeDto notificationType = toGenericDto(type);
        return toDataStructList(notificationService.findNotificationSchemasByAppIdAndType(applicationId,
                notificationType));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#editNotification(org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct)
     */    
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editNotification(DataStruct notification) throws TException {
        return notifyAndGetPayload(notificationService.saveNotification(
                ThriftDtoConverter.<NotificationDto>toDto(notification)));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getNotification(java.lang.String)
     */
    /* CLI method */
    @Override
    public DataStruct getNotification(String notificationId) throws TException {
        return toDataStruct(notificationService.findNotificationById(notificationId));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getNotificationsByTopicId(java.lang.String)
     */
    /* CLI method */
    @Override
    public List<DataStruct> getNotificationsByTopicId(String topicId) throws TException {
        return toDataStructList(notificationService.findNotificationsByTopicId(topicId));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#editTopic(org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editTopic(DataStruct topic) throws TException {
        return toDataStruct(topicService.saveTopic(ThriftDtoConverter.<TopicDto>toDto(topic)));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getTopic(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct getTopic(String topicId) throws TException {
        return toDataStruct(topicService.findTopicById(topicId));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getTopicByAppId(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public List<DataStruct> getTopicByAppId(String appId) throws TException {
        return toDataStructList(topicService.findTopicsByAppId(appId));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getTopicByEndpointGroupId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getTopicByEndpointGroupId(String endpointGroupId)
            throws TException {
        return toDataStructList(topicService.findTopicsByEndpointGroupId(endpointGroupId));
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getVacantTopicByEndpointGroupId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getVacantTopicByEndpointGroupId(
            String endpointGroupId) throws TException {
        return toDataStructList(topicService.findVacantTopicsByEndpointGroupId(endpointGroupId));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#deleteTopicById(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public void deleteTopicById(String topicId) throws TException {
        topicService.removeTopicById(topicId);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getUnicastNotification(java.lang.String)
     */
    /* CLI method */
    @Override
    public DataStruct getUnicastNotification(String notificationId) throws TException {
        return toDataStruct(notificationService.findUnicastNotificationById(notificationId));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#editUnicastNotification(org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct)
     */
    /* CLI method */
    @Override
    public DataStruct editUnicastNotification(DataStruct notification) throws TException {
        return notifyAndGetPayload(notificationService.saveUnicastNotification(
                ThriftDtoConverter.<EndpointNotificationDto>toDto(notification)));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getUnicastNotificationsByKeyHash(java.nio.ByteBuffer)
     */
    @Override
    public List<DataStruct> getUnicastNotificationsByKeyHash(ByteBuffer keyhash) throws TException {
        List<DataStruct> structList = Collections.emptyList();
        if (keyhash != null) {
            structList = toDataStructList(notificationService.findUnicastNotificationsByKeyHash(keyhash.array()));
        }
        return structList;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getConfigurationSchemaVersionsByApplicationId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getConfigurationSchemaVersionsByApplicationId(
            String applicationId) throws TException {
        return toDataStructList(configurationService.findConfigurationSchemaVersionsByAppId(applicationId));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getProfileSchemaVersionsByApplicationId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getProfileSchemaVersionsByApplicationId(
            String applicationId) throws TException {
        return toDataStructList(profileService.findProfileSchemaVersionsByAppId(applicationId));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService.Iface#getNotificationSchemaVersionsByApplicationId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getNotificationSchemaVersionsByApplicationId(
            String applicationId) throws TException {
        return toDataStructList(notificationService.findNotificationSchemaVersionsByAppId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService
     * #getServerShortName()
     */
    @Override
    protected String getServerShortName() {
        return "control-server";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService
     * #initServiceCommands()
     */
    @Override
    protected void initServiceCommands() {
        
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.cli.CliThriftService
     * .Iface#shutdown()
     */
    @Override
    public void shutdown() throws TException {
        LOG.info("Received shutdown command.");
        
        Runnable shutdownCommmand = new Runnable() {
            @Override
            public void run() {
                LOG.info("Stopping Control Server Application...");
                controlService.stop();
                ThriftExecutor.shutdown();
            }
        };
        
        Thread shutdownThread = new Thread(shutdownCommmand);
        shutdownThread.setName("Control Server Shutdown Thread");
        shutdownThread.start();
    }


    /**
     * Notify endpoints.
     *
     * @param notification
     *            the notification
     * @param profileFilter
     *            the profile filter
     * @param configuration
     *            the configuration
     */
    private void notifyEndpoints(ChangeNotificationDto notification,
                                 ProfileFilterDto profileFilter, ConfigurationDto configuration) {

        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(notification.getAppId());
        thriftNotification.setAppSeqNumber(notification.getAppSeqNumber());
        thriftNotification.setGroupId(notification.getGroupId());
        thriftNotification.setGroupSeqNumber(notification.getGroupSeqNumber());
        if (profileFilter != null) {
            thriftNotification.setProfileFilterId(profileFilter.getId());
            thriftNotification.setProfileFilterSeqNumber(profileFilter
                    .getSequenceNumber());
        }
        if (configuration != null) {
            thriftNotification.setConfigurationId(configuration.getId());
            thriftNotification.setConfigurationSeqNumber(configuration
                    .getSequenceNumber());
        }
        controlZKService.sendEndpointNotification(thriftNotification);

    }

    /**
     * Notify endpoints.
     *
     * @param notification the notification
     */
    private void notifyEndpoints(UpdateNotificationDto notification) {
        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(notification.getAppId());
        thriftNotification.setAppSeqNumber(notification.getAppSeqNumber());
        thriftNotification.setGroupId(notification.getGroupId());
        thriftNotification.setGroupSeqNumber(notification.getGroupSeqNumber());
        thriftNotification.setTopicId(notification.getTopicId());
        thriftNotification.setOp(getOperation(notification.getChangeType()));
        Object payload = notification.getPayload();
        if (payload != null) {
            if (payload instanceof NotificationDto) {
                NotificationDto dto = (NotificationDto) payload;
                thriftNotification.setNotificationId(dto.getId());
            } else if (payload instanceof EndpointNotificationDto) {
                EndpointNotificationDto unicastDto = (EndpointNotificationDto) payload;
                thriftNotification.setKeyHash(unicastDto.getEndpointKeyHash());
                thriftNotification.setUnicastNotificationId(unicastDto.getId());
            }
        }
        controlZKService.sendEndpointNotification(thriftNotification);
    }

    /**
     * Notify and get payload.
     *
     * @param notification the notification
     * @return the data struct
     */
    private DataStruct notifyAndGetPayload(UpdateNotificationDto notification) {
        DataStruct dataStruct = null;
        if (notification != null) {
            notifyEndpoints(notification);
            Object payload = notification.getPayload();
            if (payload instanceof HasId && payload != null) {
                dataStruct = toDataStruct((HasId) payload);
            }
        }
        return dataStruct;
    }

    /**
     * Gets the operation.
     *
     * @param type the type
     * @return the operation
     */
    private Operation getOperation(ChangeType type) {
        Operation operation = null;
        if (type != null) {
            try {
                operation = Operation.valueOf(type.name());
            } catch (IllegalArgumentException ex) {
                LOG.info("Unsupported change type. Check Operation and ChangeType enums.");
            }
        }
        return operation;
    }

}
