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

package org.kaaproject.kaa.server.control.service;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PreDestroy;

import org.apache.avro.Schema;
import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.avro.ui.shared.Fqn;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.RecordKey.RecordFiles;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.Version;
import org.kaaproject.kaa.server.common.core.algorithms.AvroUtils;
import org.kaaproject.kaa.server.common.core.schema.DataSchema;
import org.kaaproject.kaa.server.common.core.schema.ProtocolSchema;
import org.kaaproject.kaa.server.common.dao.ApplicationEventMapService;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointRegistrationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.EventClassService;
import org.kaaproject.kaa.server.common.dao.LogAppendersService;
import org.kaaproject.kaa.server.common.dao.LogSchemaService;
import org.kaaproject.kaa.server.common.dao.NotificationService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.SdkProfileService;
import org.kaaproject.kaa.server.common.dao.ServerProfileService;
import org.kaaproject.kaa.server.common.dao.TopicService;
import org.kaaproject.kaa.server.common.dao.UserConfigurationService;
import org.kaaproject.kaa.server.common.dao.UserService;
import org.kaaproject.kaa.server.common.dao.UserVerifierService;
import org.kaaproject.kaa.server.common.dao.exception.CredentialsServiceException;
import org.kaaproject.kaa.server.common.dao.exception.EndpointRegistrationServiceException;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.exception.NotFoundException;
import org.kaaproject.kaa.server.common.dao.model.sql.NotificationSchema;
import org.kaaproject.kaa.server.common.log.shared.RecordWrapperSchemaGenerator;
import org.kaaproject.kaa.server.common.thrift.KaaThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Operation;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService.Iface;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftActorClassifier;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftClusterEntityType;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEndpointDeregistrationMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEntityAddress;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftServerProfileUpdateMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftUnicastNotificationMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.UserConfigurationUpdate;
import org.kaaproject.kaa.server.common.zk.control.ControlNode;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.kaaproject.kaa.server.control.service.exception.ControlServiceException;
import org.kaaproject.kaa.server.control.service.schema.SchemaLibraryGenerator;
import org.kaaproject.kaa.server.control.service.sdk.SdkGenerator;
import org.kaaproject.kaa.server.control.service.sdk.SdkGeneratorFactory;
import org.kaaproject.kaa.server.control.service.sdk.event.EventFamilyMetadata;
import org.kaaproject.kaa.server.control.service.zk.ControlZkService;
import org.kaaproject.kaa.server.hash.ConsistentHashResolver;
import org.kaaproject.kaa.server.node.service.credentials.CredentialsServiceLocator;
import org.kaaproject.kaa.server.node.service.credentials.CredentialsServiceRegistry;
import org.kaaproject.kaa.server.node.service.thrift.OperationsServiceMsg;
import org.kaaproject.kaa.server.resolve.OperationsServerResolver;
import org.kaaproject.kaa.server.thrift.NeighborTemplate;
import org.kaaproject.kaa.server.thrift.Neighbors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

/**
 * The Class DefaultControlService.
 */
@Service
public class DefaultControlService implements ControlService {

    /** The Constant DEFAULT_NEIGHBOR_CONNECTIONS_SIZE. */
    private static final int DEFAULT_NEIGHBOR_CONNECTIONS_SIZE = 10;

    /** The Constant DEFAULT_USER_HASH_PARTITIONS_SIZE. */
    private static final int DEFAULT_USER_HASH_PARTITIONS_SIZE = 10;

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultControlService.class);

    /** The Constant SCHEMA_NAME_PATTERN. */
    private static final String SCHEMA_NAME_PATTERN = "kaa-record-schema-l{}.avsc";

    /** The Constant DATA_NAME_PATTERN. */
    private static final String DATA_NAME_PATTERN = "kaa-{}-schema-v{}.avsc";

    /** The Constant LOG_SCHEMA_LIBRARY_NAME_PATTERN. */
    private static final String LOG_SCHEMA_LIBRARY_NAME_PATTERN = "kaa-record-lib-l{}";

    /**
     * A template for naming exported CTL library schemas.
     * 
     * @see #exportCTLSchemaFlatAsLibrary(CTLSchemaDto)
     */
    private static final String CTL_LIBRARY_EXPORT_TEMPLATE = "{0}.v{1}";

    /** The user service. */
    @Autowired
    private UserService userService;

    /** The application service. */
    @Autowired
    private ApplicationService applicationService;

    /** The configuration service. */
    @Autowired
    private ConfigurationService configurationService;

    /** The user configuration service. */
    @Autowired
    private UserConfigurationService userConfigurationService;

    /** The profile service. */
    @Autowired
    private ProfileService profileService;

    /** The server profile service. */
    @Autowired
    private ServerProfileService serverProfileService;

    /** The endpoint service. */
    @Autowired
    private EndpointService endpointService;

    /** The notification service. */
    @Autowired
    private NotificationService notificationService;

    /** The topic service. */
    @Autowired
    private TopicService topicService;

    /** The event class service. */
    @Autowired
    private EventClassService eventClassService;

    /** The application event map service. */
    @Autowired
    private ApplicationEventMapService applicationEventMapService;

    /** The control zk service. */
    @Autowired
    private ControlZkService controlZKService;

    /** The log schema service. */
    @Autowired
    private LogSchemaService logSchemaService;

    /** The log appender service. */
    @Autowired
    private LogAppendersService logAppenderService;

    /** The user verifier service. */
    @Autowired
    private UserVerifierService userVerifierService;

    /** The sdk key service. */
    @Autowired
    private SdkProfileService sdkProfileService;

    @Autowired
    private CTLService ctlService;

    @Autowired
    @Qualifier("rootCredentialsServiceLocator")
    private CredentialsServiceLocator credentialsServiceLocator;
    
    @Autowired
    private CredentialsServiceRegistry credentialsServiceRegistry;
    
    @Autowired
    private EndpointRegistrationService endpointRegistrationService;

    /** The neighbor connections size. */
    @Value("#{properties[max_number_neighbor_connections]}")
    private int neighborConnectionsSize = DEFAULT_NEIGHBOR_CONNECTIONS_SIZE;

    /** The user hash partitions. */
    @Value("#{properties[user_hash_partitions]}")
    private int userHashPartitions = DEFAULT_USER_HASH_PARTITIONS_SIZE;

    /** The neighbors. */
    private volatile Neighbors<NeighborTemplate<OperationsServiceMsg>, OperationsServiceMsg> neighbors;

    /** The resolver. */
    private volatile OperationsServerResolver resolver;

    /** The zk lock. */
    private Object zkLock = new Object();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getTenants()
     */
    @Override
    public List<TenantDto> getTenants() throws ControlServiceException {
        return userService.findAllTenants();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getTenant(java
     * .lang.String)
     */
    @Override
    public TenantDto getTenant(String tenantId) throws ControlServiceException {
        return userService.findTenantById(tenantId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#editTenant(org
     * .kaaproject.kaa.common.dto.TenantDto)
     */
    @Override
    public TenantDto editTenant(TenantDto tenant) throws ControlServiceException {
        return userService.saveTenant(tenant);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#deleteTenant
     * (java.lang.String)
     */
    @Override
    public void deleteTenant(String tenantId) throws ControlServiceException {
        userService.removeTenantById(tenantId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#getUsers()
     */
    @Override
    public List<UserDto> getUsers() throws ControlServiceException {
        return userService.findAllUsers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getTenantUsers
     * (java.lang.String)
     */
    @Override
    public List<UserDto> getTenantUsers(String tenantId) throws ControlServiceException {
        return userService.findAllTenantUsers(tenantId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getUser(java
     * .lang.String)
     */
    @Override
    public UserDto getUser(String userId) throws ControlServiceException {
        return userService.findUserById(userId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getUserByExternalUid (java.lang.String)
     */
    @Override
    public UserDto getUserByExternalUid(String uid) throws ControlServiceException {
        return userService.findUserByExternalUid(uid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#editUser(org
     * .kaaproject.kaa.common.dto.UserDto)
     */
    @Override
    public UserDto editUser(UserDto user) throws ControlServiceException {
        return userService.saveUser(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#deleteUser(java
     * .lang.String)
     */
    @Override
    public void deleteUser(String userId) throws ControlServiceException {
        userService.removeUserById(userId);
    }



    @Override
    public List<UserDto> findAllTenantAdminsByTenantId(String tenantId) throws ControlServiceException {
        return userService.findAllTenantAdminsByTenantId(tenantId);
    }




    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getApplication
     * (java.lang.String)
     */
    @Override
    public ApplicationDto getApplication(String applicationId) throws ControlServiceException {
        return applicationService.findAppById(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getApplicationByApplicationToken(java.lang.String)
     */
    @Override
    public ApplicationDto getApplicationByApplicationToken(String applicationToken) throws ControlServiceException {
        return applicationService.findAppByApplicationToken(applicationToken);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getApplicationsByTenantId(java.lang.String)
     */
    @Override
    public List<ApplicationDto> getApplicationsByTenantId(String tenantId) throws ControlServiceException {
        return applicationService.findAppsByTenantId(tenantId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#editApplication
     * (org.kaaproject.kaa.common.dto.ApplicationDto)
     */
    @Override
    public ApplicationDto editApplication(ApplicationDto application) throws ControlServiceException {
        ApplicationDto storedApplication = applicationService.findAppByApplicationToken(application.getApplicationToken());
        if (storedApplication != null) {
            application.setId(storedApplication.getId());
        }

        boolean update = !isEmpty(application.getId());
        ApplicationDto appDto = applicationService.saveApp(application);
        if (update) {
            LOG.info("[{}] Broadcasting notification about application {} update.", application.getId(), application.getApplicationToken());
            Notification thriftNotification = new Notification();
            thriftNotification.setAppId(appDto.getId());
            thriftNotification.setAppSeqNumber(appDto.getSequenceNumber());
            thriftNotification.setOp(Operation.APP_UPDATE);
            controlZKService.sendEndpointNotification(thriftNotification);
        }
        return appDto;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * deleteApplication (java.lang.String)
     */
    @Override
    public void deleteApplication(String applicationId) throws ControlServiceException {
        applicationService.removeAppById(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getConfigurationSchemasByApplicationId(java.lang.String)
     */
    @Override
    public List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(String applicationId) throws ControlServiceException {
        return configurationService.findConfSchemasByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getConfigurationSchema(java.lang.String)
     */
    @Override
    public ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId) throws ControlServiceException {
        return configurationService.findConfSchemaById(configurationSchemaId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * editConfigurationSchema
     * (org.kaaproject.kaa.common.dto.ConfigurationSchemaDto)
     */
    @Override
    public ConfigurationSchemaDto editConfigurationSchema(ConfigurationSchemaDto configurationSchema) throws ControlServiceException {
        ConfigurationSchemaDto confSchema = null;
        try {
            confSchema = configurationService.saveConfSchema(configurationSchema);
        } catch (IncorrectParameterException e) {
            LOG.error("Can't generate protocol schema. Can't save configuration schema.");
            throw new ControlServiceException(e);
        }
        return confSchema;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getProfileSchemasByApplicationId(java.lang.String)
     */
    @Override
    public List<EndpointProfileSchemaDto> getProfileSchemasByApplicationId(String applicationId) throws ControlServiceException {
        return profileService.findProfileSchemasByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getProfileSchema
     * (java.lang.String)
     */
    @Override
    public EndpointProfileSchemaDto getProfileSchema(String profileSchemaId) throws ControlServiceException {
        return profileService.findProfileSchemaById(profileSchemaId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getProfileSchemaByApplicationIdAndVersion (java.lang.String, int)
     */
    @Override
    public EndpointProfileSchemaDto getProfileSchemaByApplicationIdAndVersion(String applicationId, int version)
            throws ControlServiceException {
        return profileService.findProfileSchemaByAppIdAndVersion(applicationId, version);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * editProfileSchema (org.kaaproject.kaa.common.dto.ProfileSchemaDto)
     */
    @Override
    public EndpointProfileSchemaDto editProfileSchema(EndpointProfileSchemaDto profileSchema) throws ControlServiceException {
        return profileService.saveProfileSchema(profileSchema);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getServerProfileSchemasByApplicationId(java.lang.String)
     */
    @Override
    public List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationId(String applicationId) throws ControlServiceException {
        return serverProfileService.findServerProfileSchemasByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getServerProfileSchema(java.lang.String)
     */
    @Override
    public ServerProfileSchemaDto getServerProfileSchema(String serverProfileSchemaId) throws ControlServiceException {
        return serverProfileService.findServerProfileSchema(serverProfileSchemaId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getServerProfileSchemaByApplicationIdAndVersion(java.lang.String, int)
     */
    @Override
    public ServerProfileSchemaDto getServerProfileSchemaByApplicationIdAndVersion(String applicationId, int version)
            throws ControlServiceException {
        return serverProfileService.findServerProfileSchemaByAppIdAndVersion(applicationId, version);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * editServerProfileSchema
     * (org.kaaproject.kaa.common.dto.ServerProfileSchemaDto)
     */
    @Override
    public ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto serverProfileSchema) throws ControlServiceException {
        if (isNotBlank(serverProfileSchema.getCtlSchemaId())) {
            return serverProfileService.saveServerProfileSchema(serverProfileSchema);
        } else {
            LOG.error("Server profile schema has no CTL schema ID");
            throw new ControlServiceException("Server profile schema has no CTL schema ID");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * findLatestServerProfileSchema(java.lang.String)
     */
    @Override
    public ServerProfileSchemaDto findLatestServerProfileSchema(String applicationId) throws ControlServiceException {
        return serverProfileService.findLatestServerProfileSchema(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getEndpointGroupsByApplicationId(java.lang.String)
     */
    @Override
    public List<EndpointGroupDto> getEndpointGroupsByApplicationId(String applicationId) throws ControlServiceException {
        return endpointService.findEndpointGroupsByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getEndpointGroup
     * (java.lang.String)
     */
    @Override
    public EndpointGroupDto getEndpointGroup(String endpointGroupId) throws ControlServiceException {
        return endpointService.findEndpointGroupById(endpointGroupId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * editEndpointGroup (org.kaaproject.kaa.common.dto.EndpointGroupDto)
     */
    @Override
    public EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws ControlServiceException {
        return endpointService.saveEndpointGroup(endpointGroup);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * deleteEndpointGroup (java.lang.String)
     */
    @Override
    public void deleteEndpointGroup(String endpointGroupId) throws ControlServiceException {
        ChangeNotificationDto notification = endpointService.removeEndpointGroupById(endpointGroupId);
        if (notification != null) {
            notifyEndpoints(notification, null, null);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * removeTopicsFromEndpointGroup(java.lang.String, java.lang.String)
     */
    @Override
    public EndpointGroupDto removeTopicsFromEndpointGroup(String endpointGroupId, String topicId) throws ControlServiceException {
        return notifyAndGetPayload(endpointService.removeTopicFromEndpointGroup(endpointGroupId, topicId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * addTopicsToEndpointGroup(java.lang.String, java.lang.String)
     */
    @Override
    public EndpointGroupDto addTopicsToEndpointGroup(String endpointGroupId, String topicId) throws ControlServiceException {
        return notifyAndGetPayload(endpointService.addTopicToEndpointGroup(endpointGroupId, topicId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getProfileFilter
     * (java.lang.String)
     */
    @Override
    public ProfileFilterDto getProfileFilter(String profileFilterId) throws ControlServiceException {
        return profileService.findProfileFilterById(profileFilterId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getProfileFilterRecordsByEndpointGroupId(java.lang.String, boolean)
     */
    @Override
    public List<ProfileFilterRecordDto> getProfileFilterRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated)
            throws ControlServiceException {
        return new ArrayList<>(profileService.findAllProfileFilterRecordsByEndpointGroupId(endpointGroupId, includeDeprecated));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getProfileFilterRecord(java.lang.String, java.lang.String)
     */
    @Override
    public ProfileFilterRecordDto getProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId,
            String endpointGroupId) throws ControlServiceException {
        return profileService.findProfileFilterRecordBySchemaIdAndEndpointGroupId(endpointProfileSchemaId, serverProfileSchemaId,
                endpointGroupId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getVacantProfileSchemasByEndpointGroupId(java.lang.String)
     */
    @Override
    public List<ProfileVersionPairDto> getVacantProfileSchemasByEndpointGroupId(String endpointGroupId) throws ControlServiceException {
        return profileService.findVacantSchemasByEndpointGroupId(endpointGroupId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * editProfileFilter (org.kaaproject.kaa.common.dto.ProfileFilterDto)
     */
    @Override
    public ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws ControlServiceException {
        return profileService.saveProfileFilter(profileFilter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getConfigurationRecordsByEndpointGroupId(java.lang.String, boolean)
     */
    @Override
    public List<ConfigurationRecordDto> getConfigurationRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated)
            throws ControlServiceException {
        return new ArrayList<>(configurationService.findAllConfigurationRecordsByEndpointGroupId(endpointGroupId, includeDeprecated));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getConfigurationRecord(java.lang.String, java.lang.String)
     */
    @Override
    public ConfigurationRecordDto getConfigurationRecord(String schemaId, String endpointGroupId) throws ControlServiceException {
        return configurationService.findConfigurationRecordBySchemaIdAndEndpointGroupId(schemaId, endpointGroupId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getVacantConfigurationSchemasByEndpointGroupId(java.lang.String)
     */
    @Override
    public List<VersionDto> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId) throws ControlServiceException {
        return configurationService.findVacantSchemasByEndpointGroupId(endpointGroupId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getConfiguration
     * (java.lang.String)
     */
    @Override
    public ConfigurationDto getConfiguration(String configurationId) throws ControlServiceException {
        return configurationService.findConfigurationById(configurationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * editConfiguration (org.kaaproject.kaa.common.dto.ConfigurationDto)
     */
    @Override
    public ConfigurationDto editConfiguration(ConfigurationDto configuration) throws ControlServiceException {
        return configurationService.saveConfiguration(configuration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * editUserConfiguration
     * (org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto)
     */
    @Override
    public void editUserConfiguration(EndpointUserConfigurationDto configuration) throws ControlServiceException {
        ApplicationDto appDto = applicationService.findAppByApplicationToken(configuration.getAppToken());

        EndpointUserDto userDto = endpointService.findEndpointUserByExternalIdAndTenantId(configuration.getUserId(), appDto.getTenantId());
        
        if(userDto == null){
            throw new NotFoundException("Specified user not found!");
        }

        configuration.setUserId(userDto.getId());
        configuration = userConfigurationService.saveUserConfiguration(configuration);

        EndpointObjectHash hash = EndpointObjectHash.fromString(configuration.getBody());

        checkNeighbors();

        OperationsNodeInfo server = resolve(configuration.getUserId());

        if (server != null) {
            UserConfigurationUpdate msg = new UserConfigurationUpdate(appDto.getTenantId(), configuration.getUserId(),
                    configuration.getAppToken(), configuration.getSchemaVersion(), hash.getDataBuf());
            if (LOG.isTraceEnabled()) {
                LOG.trace("Sending message {} to [{}]", msg, Neighbors.getServerID(server.getConnectionInfo()));
            }
            neighbors.sendMessage(server.getConnectionInfo(), OperationsServiceMsg.fromUpdate(msg));
        } else {
            LOG.warn("Can't find server for user [{}]", configuration.getUserId());
        }
    }

    /**
     * Check neighbors.
     */
    private void checkNeighbors() {
        if (neighbors == null) {
            synchronized (zkLock) {
                if (neighbors == null) {
                    neighbors = new Neighbors<NeighborTemplate<OperationsServiceMsg>, OperationsServiceMsg>(
                            KaaThriftService.OPERATIONS_SERVICE, new NeighborTemplate<OperationsServiceMsg>() {
                                @Override
                                public void process(Iface client, List<OperationsServiceMsg> messages) throws TException {
                                    OperationsServiceMsg.dispatch(client, messages);
                                }

                                @Override
                                public void onServerError(String serverId, Exception e) {
                                    LOG.error("Can't send configuration update to {}", serverId, e);
                                }
                            }, neighborConnectionsSize);
                    ControlNode zkNode = controlZKService.getControlZKNode();
                    neighbors.setZkNode(KaaThriftService.KAA_NODE_SERVICE, zkNode.getControlServerInfo().getConnectionInfo(), zkNode);
                }
            }
        }
    }

    /**
     * Resolve.
     *
     * @param entityId
     *            the entity id
     * @return the operations node info
     */
    private OperationsNodeInfo resolve(String entityId) {
        if (resolver == null) {
            synchronized (zkLock) {
                if (resolver == null) {
                    ControlNode zkNode = controlZKService.getControlZKNode();
                    resolver = new ConsistentHashResolver(zkNode.getCurrentOperationServerNodes(), userHashPartitions);
                    zkNode.addListener(new OperationsNodeListener() {
                        @Override
                        public void onNodeUpdated(OperationsNodeInfo node) {
                            LOG.info("Update of node {} is pushed to resolver {}", node, resolver);
                            resolver.onNodeUpdated(node);
                        }

                        @Override
                        public void onNodeRemoved(OperationsNodeInfo node) {
                            LOG.info("Remove of node {} is pushed to resolver {}", node, resolver);
                            resolver.onNodeRemoved(node);
                        }

                        @Override
                        public void onNodeAdded(OperationsNodeInfo node) {
                            LOG.info("Add of node {} is pushed to resolver {}", node, resolver);
                            resolver.onNodeAdded(node);
                        }
                    });
                }
            }
        }
        return resolver.getNode(entityId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * activateConfiguration(java.lang.String, java.lang.String)
     */
    @Override
    public ConfigurationDto activateConfiguration(String configurationId, String activatedUsername) throws ControlServiceException {
        ChangeConfigurationNotification cfgNotification = configurationService.activateConfiguration(configurationId, activatedUsername);
        ChangeNotificationDto notification = cfgNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, null, cfgNotification.getConfigurationDto());
        }
        return cfgNotification.getConfigurationDto();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * deactivateConfiguration(java.lang.String, java.lang.String)
     */
    @Override
    public ConfigurationDto deactivateConfiguration(String configurationId, String deactivatedUsername) throws ControlServiceException {
        ChangeConfigurationNotification cfgNotification = configurationService.deactivateConfiguration(configurationId,
                deactivatedUsername);
        ChangeNotificationDto notification = cfgNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, null, cfgNotification.getConfigurationDto());
        }
        return cfgNotification.getConfigurationDto();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * deleteConfigurationRecord(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void deleteConfigurationRecord(String schemaId, String endpointGroupId, String deactivatedUsername)
            throws ControlServiceException {
        ChangeConfigurationNotification cfgNotification = configurationService.deleteConfigurationRecord(schemaId, endpointGroupId,
                deactivatedUsername);
        if (cfgNotification != null) {
            ChangeNotificationDto notification = cfgNotification.getChangeNotificationDto();
            if (notification != null) {
                notifyEndpoints(notification, null, cfgNotification.getConfigurationDto());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * activateProfileFilter(java.lang.String, java.lang.String)
     */
    @Override
    public ProfileFilterDto activateProfileFilter(String profileFilterId, String activatedUsername) throws ControlServiceException {
        ChangeProfileFilterNotification cpfNotification = profileService.activateProfileFilter(profileFilterId, activatedUsername);
        ChangeNotificationDto notification = cpfNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, cpfNotification.getProfileFilterDto(), null);
        }
        return cpfNotification.getProfileFilterDto();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * deactivateProfileFilter(java.lang.String, java.lang.String)
     */
    @Override
    public ProfileFilterDto deactivateProfileFilter(String profileFilterId, String deactivatedUsername) throws ControlServiceException {
        ChangeProfileFilterNotification cpfNotification = profileService.deactivateProfileFilter(profileFilterId, deactivatedUsername);
        ChangeNotificationDto notification = cpfNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, cpfNotification.getProfileFilterDto(), null);
        }
        return cpfNotification.getProfileFilterDto();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * deleteProfileFilterRecord(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId,
            String deactivatedUsername) throws ControlServiceException {
        ChangeProfileFilterNotification cpfNotification = profileService.deleteProfileFilterRecord(endpointProfileSchemaId,
                serverProfileSchemaId, endpointGroupId, deactivatedUsername);
        if (cpfNotification != null) {
            ChangeNotificationDto notification = cpfNotification.getChangeNotificationDto();
            if (notification != null) {
                notifyEndpoints(notification, cpfNotification.getProfileFilterDto(), null);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#generateSdk(
     * org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto)
     */
    @Override
    public FileData generateSdk(SdkProfileDto sdkProfile, SdkPlatform platform) throws ControlServiceException {
        EndpointProfileSchemaDto profileSchema = profileService.findProfileSchemaByAppIdAndVersion(sdkProfile.getApplicationId(),
                sdkProfile.getProfileSchemaVersion());
        if (profileSchema == null) {
            throw new NotFoundException("Profile schema not found!");
        }
        ConfigurationSchemaDto configurationSchema = configurationService.findConfSchemaByAppIdAndVersion(sdkProfile.getApplicationId(),
                sdkProfile.getConfigurationSchemaVersion());
        if (configurationSchema == null) {
            throw new NotFoundException("Configuration schema not found!");
        }
        ConfigurationDto defaultConfiguration = configurationService.findDefaultConfigurationBySchemaId(configurationSchema.getId());
        if (defaultConfiguration == null) {
            throw new NotFoundException("Default configuration not found!");
        }
        NotificationSchemaDto notificationSchema = notificationService.findNotificationSchemaByAppIdAndTypeAndVersion(
                sdkProfile.getApplicationId(), NotificationTypeDto.USER, sdkProfile.getNotificationSchemaVersion());
        if (notificationSchema == null) {
            throw new NotFoundException("Notification schema not found!");
        }

        LogSchemaDto logSchema = logSchemaService.findLogSchemaByAppIdAndVersion(sdkProfile.getApplicationId(),
                sdkProfile.getLogSchemaVersion());
        if (logSchema == null) {
            throw new NotFoundException("Log schema not found!");
        }

        CTLSchemaDto logCtlSchema = getCTLSchemaById(logSchema.getCtlSchemaId());

        String logSchemaBodyString = ctlService.flatExportAsString(logCtlSchema);

        CTLSchemaDto profileCtlSchema = getCTLSchemaById(profileSchema.getCtlSchemaId());

        CTLSchemaDto notificationCtlSchema = getCTLSchemaById(notificationSchema.getCtlSchemaId());

        CTLSchemaDto confCtlSchema = getCTLSchemaById(configurationSchema.getCtlSchemaId());

        String notificationSchemaBodyString = ctlService.flatExportAsString(notificationCtlSchema);
        String profileSchemaBodyString = ctlService.flatExportAsString(profileCtlSchema);
        String confSchemaBodyString = ctlService.flatExportAsString(confCtlSchema);

        DataSchema profileDataSchema = new DataSchema(profileSchemaBodyString);
        DataSchema confDataSchema = new DataSchema(confSchemaBodyString);
        DataSchema notificationDataSchema = new DataSchema(notificationSchemaBodyString);
        ProtocolSchema protocolSchema = new ProtocolSchema(configurationSchema.getProtocolSchema());
        DataSchema logDataSchema = new DataSchema(logSchemaBodyString);

        String profileSchemaBody = profileDataSchema.getRawSchema();
        String confSchemaBody = confDataSchema.getRawSchema();

        JsonNode json;
        try {
            json = new ObjectMapper().readTree(defaultConfiguration.getBody());
        } catch (IOException e) {
            LOG.error("Unable to convert default configuration data to json", e);
            throw new ControlServiceException(e);
        }
        AvroUtils.removeUuids(json);

        byte[] defaultConfigurationData = GenericAvroConverter.toRawData(json.toString(), confSchemaBody);


        List<EventFamilyMetadata> eventFamilies = new ArrayList<>();
        if (sdkProfile.getAefMapIds() != null) {
            List<ApplicationEventFamilyMapDto> aefMaps = applicationEventMapService
                    .findApplicationEventFamilyMapsByIds(sdkProfile.getAefMapIds());
            for (ApplicationEventFamilyMapDto aefMap : aefMaps) {
                EventFamilyMetadata efm = new EventFamilyMetadata();
                efm.setVersion(aefMap.getVersion());
                efm.setEventMaps(aefMap.getEventMaps());
                EventClassFamilyDto ecf = eventClassService.findEventClassFamilyById(aefMap.getEcfId());
                efm.setEcfName(ecf.getName());
                efm.setEcfNamespace(ecf.getNamespace());
                efm.setEcfClassName(ecf.getClassName());
                List<EventSchemaVersionDto> ecfSchemas = ecf.getSchemas();
                for (EventSchemaVersionDto ecfSchema : ecfSchemas) {
                    if (ecfSchema.getVersion() == efm.getVersion()) {
                        efm.setEcfSchema(ecfSchema.getSchema());
                        break;
                    }
                }
                eventFamilies.add(efm);
            }
        }

        LOG.debug("Sdk profile for sdk generation: {}", sdkProfile);

        SdkGenerator generator = SdkGeneratorFactory.createSdkGenerator(platform);
        FileData sdkFile = null;
        try {
            sdkFile = generator.generateSdk(Version.PROJECT_VERSION, controlZKService.getCurrentBootstrapNodes(), sdkProfile,
                    profileSchemaBody, notificationDataSchema.getRawSchema(), protocolSchema.getRawSchema(),
                    confSchemaBody, defaultConfigurationData, eventFamilies, logDataSchema.getRawSchema());
        } catch (Exception e) {
            LOG.error("Unable to generate SDK", e);
            throw new ControlServiceException(e);
        }
        sdkFile.setContentType(platform.getContentType());
        return sdkFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * generateRecordStructureLibrary(java.lang.String, int)
     */
    @Override
    public FileData generateRecordStructureLibrary(String applicationId, int logSchemaVersion) throws ControlServiceException {
        ApplicationDto application = applicationService.findAppById(applicationId);
        if (application == null) {
            throw new NotFoundException("Application not found!");
        }
        LogSchemaDto logSchema = logSchemaService.findLogSchemaByAppIdAndVersion(applicationId, logSchemaVersion);
        if (logSchema == null) {
            throw new NotFoundException("Log schema not found!");
        }
        try {
            CTLSchemaDto logCtlSchema = getCTLSchemaById(logSchema.getCtlSchemaId());
            Schema recordWrapperSchema = RecordWrapperSchemaGenerator.generateRecordWrapperSchema(logCtlSchema.getBody());
            String fileName = MessageFormatter.arrayFormat(LOG_SCHEMA_LIBRARY_NAME_PATTERN, new Object[] { logSchemaVersion }).getMessage();
            return SchemaLibraryGenerator.generateSchemaLibrary(recordWrapperSchema, fileName);
        } catch (Exception e) {
            LOG.error("Unable to generate Record Structure Library", e);
            throw new ControlServiceException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * editNotificationSchema
     * (org.kaaproject.kaa.common.dto.NotificationSchemaDto)
     */
    @Override
    public NotificationSchemaDto saveNotificationSchema(NotificationSchemaDto notificationSchema) throws ControlServiceException {
        return notificationService.saveNotificationSchema(notificationSchema);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getNotificationSchema(java.lang.String)
     */
    @Override
    public NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws ControlServiceException {
        return notificationService.findNotificationSchemaById(notificationSchemaId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getNotificationSchemasByAppId(java.lang.String)
     */
    @Override
    public List<NotificationSchemaDto> getNotificationSchemasByAppId(String applicationId) throws ControlServiceException {
        return notificationService.findNotificationSchemasByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getUserNotificationSchemasByAppId(java.lang.String)
     */
    @Override
    public List<VersionDto> getUserNotificationSchemasByAppId(String applicationId) throws ControlServiceException {
        return notificationService.findUserNotificationSchemasByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * findNotificationSchemasByAppIdAndType(java.lang.String,
     * org.kaaproject.kaa.common.dto.NotificationTypeDto)
     */
    @Override
    public List<NotificationSchemaDto> findNotificationSchemasByAppIdAndType(String applicationId, NotificationTypeDto type)
            throws ControlServiceException {
        return notificationService.findNotificationSchemasByAppIdAndType(applicationId, type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#editLogSchema
     * (org.kaaproject.kaa.common.dto.logs.LogSchemaDto)
     */
    @Override
    public LogSchemaDto saveLogSchema(LogSchemaDto logSchemaDto) throws ControlServiceException {
        return logSchemaService.saveLogSchema(logSchemaDto);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#editLogSchema
     * (org.kaaproject.kaa.common.dto.logs.LogSchemaDto)
     */
    @Override
    public String getFlatSchemaByCtlSchemaId(String schemaId) throws ControlServiceException{
        CTLSchemaDto ctlSchemaDto = getCTLSchemaById(schemaId);
        return ctlService.flatExportAsString(ctlSchemaDto);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getLogSchemasByApplicationId(java.lang.String)
     */
    @Override
    public List<LogSchemaDto> getLogSchemasByApplicationId(String applicationId) throws ControlServiceException {
        return logSchemaService.findLogSchemasByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getLogSchema
     * (java.lang.String)
     */
    @Override
    public LogSchemaDto getLogSchema(String logSchemaId) throws ControlServiceException {
        return logSchemaService.findLogSchemaById(logSchemaId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getLogSchemaByApplicationIdAndVersion(java.lang.String, int)
     */
    @Override
    public LogSchemaDto getLogSchemaByApplicationIdAndVersion(String applicationId, int version) throws ControlServiceException {
        return logSchemaService.findLogSchemaByAppIdAndVersion(applicationId, version);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#editNotification
     * (org.kaaproject.kaa.common.dto.NotificationDto)
     */
    @Override
    public NotificationDto editNotification(NotificationDto notification) throws ControlServiceException {
        return notifyAndGetPayload(notificationService.saveNotification(notification));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getNotification
     * (java.lang.String)
     */
    @Override
    public NotificationDto getNotification(String notificationId) throws ControlServiceException {
        return notificationService.findNotificationById(notificationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getNotificationsByTopicId(java.lang.String)
     */
    @Override
    public List<NotificationDto> getNotificationsByTopicId(String topicId) throws ControlServiceException {
        return notificationService.findNotificationsByTopicId(topicId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#editTopic(org
     * .kaaproject.kaa.common.dto.TopicDto)
     */
    @Override
    public TopicDto editTopic(TopicDto topic) throws ControlServiceException {
        return topicService.saveTopic(topic);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getTopic(java
     * .lang.String)
     */
    @Override
    public TopicDto getTopic(String topicId) throws ControlServiceException {
        return topicService.findTopicById(topicId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getTopicByAppId
     * (java.lang.String)
     */
    @Override
    public List<TopicDto> getTopicByAppId(String appId) throws ControlServiceException {
        return topicService.findTopicsByAppId(appId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getTopicByEndpointGroupId(java.lang.String)
     */
    @Override
    public List<TopicDto> getTopicByEndpointGroupId(String endpointGroupId) throws ControlServiceException {
        return topicService.findTopicsByEndpointGroupId(endpointGroupId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getVacantTopicByEndpointGroupId(java.lang.String)
     */
    @Override
    public List<TopicDto> getVacantTopicByEndpointGroupId(String endpointGroupId) throws ControlServiceException {
        return topicService.findVacantTopicsByEndpointGroupId(endpointGroupId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#deleteTopicById
     * (java.lang.String)
     */
    @Override
    public void deleteTopicById(String topicId) throws ControlServiceException {
        for (UpdateNotificationDto<EndpointGroupDto> dto : topicService.removeTopicById(topicId)) {
            notifyAndGetPayload(dto);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getUnicastNotification(java.lang.String)
     */
    @Override
    public EndpointNotificationDto getUnicastNotification(String notificationId) throws ControlServiceException {
        return notificationService.findUnicastNotificationById(notificationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * editUnicastNotification
     * (org.kaaproject.kaa.common.dto.EndpointNotificationDto)
     */
    @Override
    public EndpointNotificationDto editUnicastNotification(EndpointNotificationDto notification) throws ControlServiceException {
        UpdateNotificationDto<EndpointNotificationDto> updateNotification = notificationService.saveUnicastNotification(notification);
        EndpointNotificationDto notificationDto = updateNotification.getPayload();

        checkNeighbors();

        String endpointId = Base64Util.encode(notificationDto.getEndpointKeyHash());
        OperationsNodeInfo server = resolve(endpointId);

        if (server != null) {
            ApplicationDto appDto = getApplication(updateNotification.getAppId());
            ThriftUnicastNotificationMessage nf = new ThriftUnicastNotificationMessage();
            nf.setAddress(new ThriftEntityAddress(appDto.getTenantId(), appDto.getApplicationToken(), ThriftClusterEntityType.ENDPOINT,
                    ByteBuffer.wrap(notificationDto.getEndpointKeyHash())));
            nf.setActorClassifier(ThriftActorClassifier.GLOBAL);
            nf.setNotificationId(notificationDto.getId());
            if (LOG.isTraceEnabled()) {
                LOG.trace("Sending message {} to [{}]", nf, Neighbors.getServerID(server.getConnectionInfo()));
            }
            neighbors.sendMessage(server.getConnectionInfo(), OperationsServiceMsg.fromNotification(nf));
        } else {
            LOG.warn("Can't find server for endpoint [{}]", endpointId);
        }
        return updateNotification.getPayload();
    }

    @Override
    public EndpointProfileDto updateServerProfile(String endpointKeyHash, int version, String serverProfile)
            throws ControlServiceException {
        EndpointProfileDto endpointProfileDto = serverProfileService.saveServerProfile(Base64.decodeBase64(endpointKeyHash), version,
                serverProfile);
        checkNeighbors();

        OperationsNodeInfo server = resolve(endpointKeyHash);

        if (server != null) {
            ApplicationDto appDto = getApplication(endpointProfileDto.getApplicationId());
            ThriftServerProfileUpdateMessage nf = new ThriftServerProfileUpdateMessage();
            nf.setAddress(new ThriftEntityAddress(appDto.getTenantId(), appDto.getApplicationToken(), ThriftClusterEntityType.ENDPOINT,
                    ByteBuffer.wrap(endpointProfileDto.getEndpointKeyHash())));
            nf.setActorClassifier(ThriftActorClassifier.GLOBAL);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Sending message {} to [{}]", nf, Neighbors.getServerID(server.getConnectionInfo()));
            }
            neighbors.sendMessage(server.getConnectionInfo(), OperationsServiceMsg.fromServerProfileUpdateMessage(nf));

        } else {
            LOG.warn("Can't find server for endpoint [{}]", endpointKeyHash);
        }
        return endpointProfileDto;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getUnicastNotificationsByKeyHash(byte[])
     */
    @Override
    public List<EndpointNotificationDto> getUnicastNotificationsByKeyHash(byte[] keyhash) throws ControlServiceException {
        List<EndpointNotificationDto> structList = Collections.emptyList();
        if (keyhash != null) {
            structList = notificationService.findUnicastNotificationsByKeyHash(keyhash);
        }
        return structList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getConfigurationSchemaVersionsByApplicationId(java.lang.String)
     */
    @Override
    public List<VersionDto> getConfigurationSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException {
        return configurationService.findConfigurationSchemaVersionsByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getProfileSchemaVersionsByApplicationId(java.lang.String)
     */
    @Override
    public List<VersionDto> getProfileSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException {
        return profileService.findProfileSchemaVersionsByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getNotificationSchemaVersionsByApplicationId(java.lang.String)
     */
    @Override
    public List<VersionDto> getNotificationSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException {
        return notificationService.findNotificationSchemaVersionsByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getLogSchemaVersionsByApplicationId(java.lang.String)
     */
    @Override
    public List<VersionDto> getLogSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException {
        return logSchemaService.findLogSchemaVersionsByApplicationId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * editEventClassFamily
     * (org.kaaproject.kaa.common.dto.event.EventClassFamilyDto)
     */
    @Override
    public EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws ControlServiceException {
        return eventClassService.saveEventClassFamily(eventClassFamily);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getEventClassFamiliesByTenantId(java.lang.String)
     */
    @Override
    public List<EventClassFamilyDto> getEventClassFamiliesByTenantId(String tenantId) throws ControlServiceException {
        return eventClassService.findEventClassFamiliesByTenantId(tenantId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getEventClassFamily (java.lang.String)
     */
    @Override
    public EventClassFamilyDto getEventClassFamily(String eventClassFamilyId) throws ControlServiceException {
        return eventClassService.findEventClassFamilyById(eventClassFamilyId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * addEventClassFamilySchema(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void addEventClassFamilySchema(String eventClassFamilyId, String eventClassFamilySchema, String createdUsername)
            throws ControlServiceException {
        eventClassService.addEventClassFamilySchema(eventClassFamilyId, eventClassFamilySchema, createdUsername);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getEventClassesByFamilyIdVersionAndType(java.lang.String, int,
     * org.kaaproject.kaa.common.dto.event.EventClassType)
     */
    @Override
    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String ecfId, int version, EventClassType type)
            throws ControlServiceException {
        return eventClassService.findEventClassesByFamilyIdVersionAndType(ecfId, version, type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * editApplicationEventFamilyMap
     * (org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto)
     */
    @Override
    public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap)
            throws ControlServiceException {
        return applicationEventMapService.saveApplicationEventFamilyMap(applicationEventFamilyMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getApplicationEventFamilyMap(java.lang.String)
     */
    @Override
    public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String applicationEventFamilyMapId) throws ControlServiceException {
        return applicationEventMapService.findApplicationEventFamilyMapById(applicationEventFamilyMapId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getApplicationEventFamilyMapsByApplicationId(java.lang.String)
     */
    @Override
    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(String applicationId)
            throws ControlServiceException {
        return applicationEventMapService.findApplicationEventFamilyMapsByApplicationId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getVacantEventClassFamiliesByApplicationId(java.lang.String)
     */
    @Override
    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(String applicationId) throws ControlServiceException {
        return applicationEventMapService.findVacantEventClassFamiliesByApplicationId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getEventClassFamiliesByApplicationId(java.lang.String)
     */
    @Override
    public List<AefMapInfoDto> getEventClassFamiliesByApplicationId(String applicationId) throws ControlServiceException {
        return applicationEventMapService.findEventClassFamiliesByApplicationId(applicationId);
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
    private void notifyEndpoints(ChangeNotificationDto notification, ProfileFilterDto profileFilter, ConfigurationDto configuration) {
        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(notification.getAppId());
        thriftNotification.setAppSeqNumber(notification.getAppSeqNumber());
        thriftNotification.setGroupId(notification.getGroupId());
        thriftNotification.setGroupSeqNumber(notification.getGroupSeqNumber());
        if (profileFilter != null) {
            thriftNotification.setProfileFilterId(profileFilter.getId());
            thriftNotification.setProfileFilterSeqNumber(profileFilter.getSequenceNumber());
        }
        if (configuration != null) {
            thriftNotification.setConfigurationId(configuration.getId());
            thriftNotification.setConfigurationSeqNumber(configuration.getSequenceNumber());
        }
        controlZKService.sendEndpointNotification(thriftNotification);
    }

    /**
     * Notify endpoints.
     *
     * @param notification
     *            the notification
     */
    private <T> void notifyEndpoints(UpdateNotificationDto<T> notification) {
        controlZKService.sendEndpointNotification(toNotification(notification));
    }

    private <T> Notification toNotification(UpdateNotificationDto<T> notification) {
        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(notification.getAppId());
        thriftNotification.setAppSeqNumber(notification.getAppSeqNumber());
        thriftNotification.setGroupId(notification.getGroupId());
        thriftNotification.setGroupSeqNumber(notification.getGroupSeqNumber());
        thriftNotification.setTopicId(notification.getTopicId());
        thriftNotification.setOp(getOperation(notification.getChangeType()));
        T payload = notification.getPayload();
        if (payload != null) {
            if (payload instanceof NotificationDto) {
                NotificationDto dto = (NotificationDto) payload;
                thriftNotification.setNotificationId(dto.getId());
            }
        }
        return thriftNotification;
    }

    /**
     * Notify and get payload.
     *
     * @param notification
     *            the notification
     * @return the checks for id
     */
    private <T> T notifyAndGetPayload(UpdateNotificationDto<T> notification) {
        T result = null;
        if (notification != null) {
            notifyEndpoints(notification);
            T payload = notification.getPayload();
            if (payload instanceof HasId && payload != null) {
                result = payload;
            }
        }
        return result;
    }

    /**
     * Gets the operation.
     *
     * @param type
     *            the type
     * @return the operation
     */
    private Operation getOperation(ChangeType type) {
        Operation operation = null;
        if (type != null) {
            try {
                operation = Operation.valueOf(type.name());
            } catch (IllegalArgumentException ex) {
                LOG.info("Unsupported change type. Check Operation and ChangeType enums.", ex);
            }
        }
        return operation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getEndpointUsers
     * ()
     */
    @Override
    public List<EndpointUserDto> getEndpointUsers() throws ControlServiceException {
        return endpointService.findAllEndpointUsers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getEndpointUser
     * (java.lang.String)
     */
    @Override
    public EndpointUserDto getEndpointUser(String endpointUserId) throws ControlServiceException {
        return endpointService.findEndpointUserById(endpointUserId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * generateEndpointUserAccessToken(java.lang.String, java.lang.String)
     */
    @Override
    public String generateEndpointUserAccessToken(String externalUid, String tenantId) throws ControlServiceException {
        return endpointService.generateEndpointUserAccessToken(externalUid, tenantId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getLogAppendersByApplicationId(java.lang.String)
     */
    @Override
    public List<LogAppenderDto> getLogAppendersByApplicationId(String applicationId) throws ControlServiceException {
        return logAppenderService.findAllAppendersByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getLogAppender
     * (java.lang.String)
     */
    @Override
    public LogAppenderDto getLogAppender(String logAppenderId) throws ControlServiceException {
        return logAppenderService.findLogAppenderById(logAppenderId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#editLogAppender
     * (org.kaaproject.kaa.common.dto.logs.LogAppenderDto)
     */
    @Override
    public LogAppenderDto editLogAppender(LogAppenderDto logAppender) throws ControlServiceException {
        LogAppenderDto saved = null;
        if (logAppender != null) {
            saved = logAppenderService.saveLogAppender(logAppender);
            if (saved != null) {
                Notification thriftNotification = new Notification();
                thriftNotification.setAppId(saved.getApplicationId());
                thriftNotification.setAppenderId(saved.getId());
                if (logAppender.getId() == null) {
                    LOG.info("Add new log appender ...");
                    thriftNotification.setOp(Operation.ADD_LOG_APPENDER);
                    LOG.info("Send notification to operation servers about new appender.");
                } else {
                    thriftNotification.setOp(Operation.UPDATE_LOG_APPENDER);
                    LOG.info("Send notification to operation servers about update appender configuration.");
                }
                controlZKService.sendEndpointNotification(thriftNotification);
            }
        }
        return saved;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * deleteLogAppender (java.lang.String)
     */
    @Override
    public void deleteLogAppender(String logAppenderId) throws ControlServiceException {
        LogAppenderDto logAppenderDto = logAppenderService.findLogAppenderById(logAppenderId);
        LOG.info("Remove log appender ...");
        logAppenderService.removeLogAppenderById(logAppenderId);
        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(logAppenderDto.getApplicationId());
        thriftNotification.setAppenderId(logAppenderDto.getId());
        thriftNotification.setOp(Operation.REMOVE_LOG_APPENDER);
        LOG.info("Send notification to operation servers about removing appender.");
        controlZKService.sendEndpointNotification(thriftNotification);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getUserVerifiersByApplicationId(java.lang.String)
     */
    @Override
    public List<UserVerifierDto> getUserVerifiersByApplicationId(String applicationId) throws ControlServiceException {
        return userVerifierService.findUserVerifiersByAppId(applicationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#getUserVerifier
     * (java.lang.String)
     */
    @Override
    public UserVerifierDto getUserVerifier(String userVerifierId) throws ControlServiceException {
        return userVerifierService.findUserVerifierById(userVerifierId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.ControlService#editUserVerifier
     * (org.kaaproject.kaa.common.dto.user.UserVerifierDto)
     */
    @Override
    public UserVerifierDto editUserVerifier(UserVerifierDto userVerifier) throws ControlServiceException {
        LOG.info("Adding new user verifier {}", userVerifier);
        UserVerifierDto saved = null;
        if (userVerifier != null) {
            saved = userVerifierService.saveUserVerifier(userVerifier);
            LOG.info("Saved user verifier {}", saved);
            if (saved != null) {
                Notification thriftNotification = new Notification();
                thriftNotification.setAppId(saved.getApplicationId());
                thriftNotification.setUserVerifierToken(saved.getVerifierToken());
                if (userVerifier.getId() == null) {
                    LOG.info("Add new user verifier ...");
                    thriftNotification.setOp(Operation.ADD_USER_VERIFIER);
                    LOG.info("Send notification to operation servers about new user verifier.");
                } else {
                    thriftNotification.setOp(Operation.UPDATE_USER_VERIFIER);
                    LOG.info("Send notification to operation servers about update user verifier configuration.");
                }
                controlZKService.sendEndpointNotification(thriftNotification);
            }
        }
        return saved;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * deleteUserVerifier (java.lang.String)
     */
    @Override
    public void deleteUserVerifier(String userVerifierId) throws ControlServiceException {
        UserVerifierDto userVerifierDto = userVerifierService.findUserVerifierById(userVerifierId);
        LOG.info("Remove user verifier ...");
        userVerifierService.removeUserVerifierById(userVerifierId);
        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(userVerifierDto.getApplicationId());
        thriftNotification.setUserVerifierToken(userVerifierDto.getVerifierToken());
        thriftNotification.setOp(Operation.REMOVE_USER_VERIFIER);
        LOG.info("Send notification to operation servers about removing user verifier.");
        controlZKService.sendEndpointNotification(thriftNotification);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getRecordStructureSchema(java.lang.String, int)
     */
    @Override
    public FileData getRecordStructureSchema(String applicationId, int logSchemaVersion) throws ControlServiceException {
        ApplicationDto application = applicationService.findAppById(applicationId);
        if (application == null) {
            throw new NotFoundException("Application not found!");
        }
        LogSchemaDto logSchema = logSchemaService.findLogSchemaByAppIdAndVersion(applicationId, logSchemaVersion);
        if (logSchema == null) {
            throw new NotFoundException("Log schema not found!");
        }

        Schema recordWrapperSchema = null;
        try {
            CTLSchemaDto logCtlSchema = getCTLSchemaById(logSchema.getCtlSchemaId());
            recordWrapperSchema = RecordWrapperSchemaGenerator.generateRecordWrapperSchema(logCtlSchema.getBody());
        } catch (IOException e) {
            LOG.error("Unable to get Record Structure Schema", e);
            throw new ControlServiceException(e);
        }
        String libraryFileName = MessageFormatter.arrayFormat(SCHEMA_NAME_PATTERN, new Object[] { logSchemaVersion }).getMessage();
        String schemaInJson = recordWrapperSchema.toString(true);
        byte[] schemaData = schemaInJson.getBytes(StandardCharsets.UTF_8);

        FileData schema = new FileData();
        schema.setFileName(libraryFileName);
        schema.setFileData(schemaData);
        return schema;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.control.service.ControlService#
     * getRecordStructureData(org.kaaproject.kaa.common.dto.admin.RecordKey)
     */
    @Override
    public FileData getRecordStructureData(RecordKey key) throws ControlServiceException {
        ApplicationDto application = applicationService.findAppById(key.getApplicationId());
        if (application == null) {
            throw new NotFoundException("Application not found!");
        }

        FileData data = new FileData();
        if (RecordFiles.LOG_LIBRARY.equals(key.getRecordFiles())) {
            data = generateRecordStructureLibrary(key.getApplicationId(), key.getSchemaVersion());
        } else {
            AbstractSchemaDto schemaDto = null;
            ConfigurationSchemaDto confSchemaDto = null;
            String fileName = null;
            String schema = null;
            switch (key.getRecordFiles()) {
            case LOG_SCHEMA:
                throw new RuntimeException("Not implemented!");
            case CONFIGURATION_SCHEMA:
                throw new RuntimeException("Not implemented!");
            case CONFIGURATION_BASE_SCHEMA:
                confSchemaDto = configurationService.findConfSchemaByAppIdAndVersion(key.getApplicationId(), key.getSchemaVersion());
                checkSchema(confSchemaDto, RecordFiles.CONFIGURATION_BASE_SCHEMA);
                schema = confSchemaDto.getBaseSchema();
                fileName = MessageFormatter.arrayFormat(DATA_NAME_PATTERN, new Object[] { "configuration-base", key.getSchemaVersion() })
                        .getMessage();
                break;
            case CONFIGURATION_OVERRIDE_SCHEMA:
                confSchemaDto = configurationService.findConfSchemaByAppIdAndVersion(key.getApplicationId(), key.getSchemaVersion());
                checkSchema(confSchemaDto, RecordFiles.CONFIGURATION_OVERRIDE_SCHEMA);
                schema = confSchemaDto.getOverrideSchema();
                fileName = MessageFormatter.arrayFormat(DATA_NAME_PATTERN, new Object[] { "configuration-override", key.getSchemaVersion() })
                        .getMessage();
                break;
            case NOTIFICATION_SCHEMA:
                throw new RuntimeException("Not implemented!");
            case PROFILE_SCHEMA:
                throw new RuntimeException("Not implemented!");
            case SERVER_PROFILE_SCHEMA:
                throw new RuntimeException("Not implemented!");
            default:
                break;
            }

            byte[] schemaData = schema.getBytes(StandardCharsets.UTF_8);
            data.setFileName(fileName);
            data.setFileData(schemaData);
        }
        return data;
    }

    @Override
    public EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(PageLinkDto pageLinkDto) throws ControlServiceException {
        return endpointService.findEndpointProfileBodyByEndpointGroupId(pageLinkDto);
    }

    @Override
    public EndpointProfileDto getEndpointProfileByKeyHash(String endpointProfileKeyHash) throws ControlServiceException {
        return endpointService.findEndpointProfileByKeyHash(Base64.decodeBase64(endpointProfileKeyHash));
    }

    @Override
    public EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(String endpointProfileKeyHash) throws ControlServiceException {
        return endpointService.findEndpointProfileBodyByKeyHash(Base64.decodeBase64(endpointProfileKeyHash));
    }

    @Override
    public EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(PageLinkDto pageLinkDto) throws ControlServiceException {
        return endpointService.findEndpointProfileByEndpointGroupId(pageLinkDto);
    }

    @Override
    public SdkProfileDto getSdkProfile(String sdkProfileId) throws ControlServiceException {
        return sdkProfileService.findSdkProfileById(sdkProfileId);
    }

    @Override
    public List<SdkProfileDto> getSdkProfilesByApplicationId(String applicationId) {
        return sdkProfileService.findSdkProfilesByApplicationId(applicationId);
    }

    @Override
    public void deleteSdkProfile(String sdkProfileId) throws ControlServiceException {
        sdkProfileService.removeSdkProfileById(sdkProfileId);
    }

    @Override
    public boolean isSdkProfileUsed(String token) throws ControlServiceException {
        return sdkProfileService.isSdkProfileUsed(token);
    }

    @Override
    public SdkProfileDto saveSdkProfile(SdkProfileDto sdkProfile) throws ControlServiceException {
        return sdkProfileService.saveSdkProfile(sdkProfile);
    }

    /**
     * Check schema.
     *
     * @param schemaDto the schema dto
     * @param file      the file
     * @throws NotFoundException the control service exception
     */
    private void checkSchema(VersionDto schemaDto, RecordFiles file) throws NotFoundException {
        if (schemaDto == null) {
            throw new NotFoundException("Schema " + file + " not found!");
        }
    }

    @Override
    public CTLSchemaDto saveCTLSchema(CTLSchemaDto schema) throws ControlServiceException {
        return ctlService.saveCTLSchema(schema);
    }

    @Override
    public void deleteCTLSchemaByFqnAndVersionTenantIdAndApplicationId(String fqn, int version, String tenantId, String applicationId) throws ControlServiceException {
        ctlService.removeCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(fqn, version, tenantId, applicationId);
    }

    @Override
    public CTLSchemaDto getCTLSchemaById(String schemaId) throws ControlServiceException {
        CTLSchemaDto ctlSchemaDto = ctlService.findCTLSchemaById(schemaId);
        if (ctlSchemaDto == null) {
            LOG.error("CTL schema with Id [{}] not found!", schemaId);
            throw new NotFoundException("CTL schema not found!");
        }
        return ctlSchemaDto;
    }

    @Override
    public CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, int version, String tenantId, String applicationId) throws ControlServiceException {
        return ctlService.findCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(fqn, version, tenantId, applicationId);
    }
    
    @Override
    public CTLSchemaDto getCTLSchemaByMetaInfoIdAndVer(String metaInfoId, Integer version) {
        return ctlService.findByMetaInfoIdAndVer(metaInfoId, version);
    }

    @Override
    public CTLSchemaDto getAnyCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, int version, String tenantId,
            String applicationId) throws ControlServiceException {
        return ctlService.findAnyCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(fqn, version, tenantId, applicationId);
    }

    @Override
    public List<CTLSchemaMetaInfoDto> getSiblingsByFqnTenantIdAndApplicationId(String fqn, String tenantId, String applicationId) {
        return ctlService.findSiblingsByFqnTenantIdAndApplicationId(fqn, tenantId, applicationId);
    }

    @Override
    public CTLSchemaMetaInfoDto updateCTLSchemaMetaInfoScope(CTLSchemaMetaInfoDto ctlSchemaMetaInfo) {
        return ctlService.updateCTLSchemaMetaInfoScope(ctlSchemaMetaInfo);
    }

    @Override
    public List<CTLSchemaMetaInfoDto> getSystemCTLSchemasMetaInfo() throws ControlServiceException {
        return ctlService.findSystemCTLSchemasMetaInfo();
    }
    
    @Override
    public Map<Fqn, List<Integer>> getAvailableCTLSchemaVersionsForSystem() throws ControlServiceException {
        return extractCtlSchemaVersionsInfo(ctlService.findSystemCTLSchemasMetaInfo());
    }

    @Override
    public List<CTLSchemaMetaInfoDto> getAvailableCTLSchemasMetaInfoForTenant(String tenantId) throws ControlServiceException {
        return ctlService.findAvailableCTLSchemasMetaInfoForTenant(tenantId);
    }
    
    @Override
    public Map<Fqn, List<Integer>> getAvailableCTLSchemaVersionsForTenant(String tenantId) throws ControlServiceException {
        return extractCtlSchemaVersionsInfo(ctlService.findAvailableCTLSchemasMetaInfoForTenant(tenantId));
    }
    
    @Override
    public List<CTLSchemaMetaInfoDto> getAvailableCTLSchemasMetaInfoForApplication(String tenantId, String appId) throws ControlServiceException {
        return ctlService.findAvailableCTLSchemasMetaInfoForApplication(tenantId, appId);
    }
    
    @Override
    public Map<Fqn, List<Integer>> getAvailableCTLSchemaVersionsForApplication(String tenantId, String appId) throws ControlServiceException {
        return extractCtlSchemaVersionsInfo(ctlService.findAvailableCTLSchemasMetaInfoForApplication(tenantId, appId));
    }
    
    private Map<Fqn, List<Integer>> extractCtlSchemaVersionsInfo(List<CTLSchemaMetaInfoDto> ctlSchemaInfos) {
        Map<Fqn, List<Integer>> ctlSchemaVersions = new HashMap<>(); 
        for (CTLSchemaMetaInfoDto ctlSchemaInfo : ctlSchemaInfos) {
            ctlSchemaVersions.put(new Fqn(ctlSchemaInfo.getFqn()), ctlSchemaInfo.getVersions());
        }
        return ctlSchemaVersions;
    }

    @Override
    public List<CTLSchemaDto> getCTLSchemaDependents(String schemaId) throws ControlServiceException {
        return ctlService.findCTLSchemaDependents(schemaId);
    }

    @Override
    public List<CTLSchemaDto> getCTLSchemaDependents(String fqn, int version, String tenantId, String applicationId) throws ControlServiceException {
        return ctlService.findCTLSchemaDependents(fqn, version, tenantId, applicationId);
    }

    @Override
    public CTLSchemaDto getLatestCTLSchemaByFqnTenantIdAndApplicationId(String fqn, String tenantId, String applicationId) throws ControlServiceException {
        return ctlService.findLatestCTLSchemaByFqnAndTenantIdAndApplicationId(fqn, tenantId, applicationId);
    }
    
    @Override
    public CTLSchemaDto getLatestCTLSchemaByMetaInfoId(String metaInfoId) {
        return ctlService.findLatestByMetaInfoId(metaInfoId);
    }
    
    @Override
    public List<Integer> getAllCTLSchemaVersionsByFqnTenantIdAndApplicationId(String fqn, String tenantId, String applicationId) throws ControlServiceException {
        List<CTLSchemaDto> schemas = ctlService.findAllCTLSchemasByFqnAndTenantIdAndApplicationId(fqn, tenantId, applicationId);
        List<Integer> versions = new ArrayList<>(schemas.size());
        for (CTLSchemaDto schema : schemas) {
            versions.add(schema.getVersion());
        }
        return versions;
    }

    public FileData exportCTLSchemaShallow(CTLSchemaDto schema) throws ControlServiceException {
        return ctlService.shallowExport(schema);
    }

    @Override
    public FileData exportCTLSchemaFlat(CTLSchemaDto schema) throws ControlServiceException {
        return ctlService.flatExport(schema);
    }

    @Override
    public FileData exportCTLSchemaFlatAsLibrary(CTLSchemaDto schema) throws ControlServiceException {
        try {
            Schema avroSchema = ctlService.flatExportAsSchema(schema);
            String fileName = MessageFormat.format(CTL_LIBRARY_EXPORT_TEMPLATE, 
                    schema.getMetaInfo().getFqn(), schema.getVersion());
            return SchemaLibraryGenerator.generateSchemaLibrary(avroSchema, fileName);
        } catch (Exception e) {
            LOG.error("Unable to export flat CTL schema as library", e);
            throw new ControlServiceException(e);
        }
    }

    @Override
    public String exportCTLSchemaFlatAsString(CTLSchemaDto schema) throws ControlServiceException {
        return ctlService.flatExportAsString(schema);
    }

    @Override
    public Schema exportCTLSchemaFlatAsSchema(CTLSchemaDto schema) throws ControlServiceException {
        return ctlService.flatExportAsSchema(schema);
    }

    @Override
    public FileData exportCTLSchemaDeep(CTLSchemaDto schema) throws ControlServiceException {
        return ctlService.deepExport(schema);
    }

    @Override
    public SdkProfileDto findSdkProfileByToken(String sdkToken) throws ControlServiceException {
        SdkProfileDto result = sdkProfileService.findSdkProfileByToken(sdkToken);
        if (result != null) {
            return result;
        } else {
            throw new ControlServiceException("Can't find sdk profile by sdk token: " + sdkToken + "!");
        }
    }

    @Override
    public List<EndpointProfileDto> getEndpointProfilesByUserExternalIdAndTenantId(String endpointUserExternalId, String tenantId) throws ControlServiceException {
        return this.endpointService.findEndpointProfilesByExternalIdAndTenantId(endpointUserExternalId, tenantId);
    }

    @PreDestroy
    public void onStop() {
        if (neighbors != null) {
            LOG.info("Shutdown of control service neighbors started!");
            neighbors.shutdown();
            LOG.info("Shutdown of control service neighbors complete!");
        }
    }

    @Override
    public void removeEndpointProfile(EndpointProfileDto endpointProfile) throws ControlServiceException {
        checkNeighbors();
        byte[] endpointKeyHash = endpointProfile.getEndpointKeyHash();
        this.endpointService.removeEndpointProfileByKeyHash(endpointKeyHash);
        ApplicationDto appDto = getApplication(endpointProfile.getApplicationId());
        ThriftEndpointDeregistrationMessage nf = new ThriftEndpointDeregistrationMessage();
        nf.setAddress(new ThriftEntityAddress(appDto.getTenantId(), appDto.getApplicationToken(), ThriftClusterEntityType.ENDPOINT,
                ByteBuffer.wrap(endpointKeyHash)));
        nf.setActorClassifier(ThriftActorClassifier.APPLICATION);
        neighbors.brodcastMessage(OperationsServiceMsg.fromDeregistration(nf));
    }

    @Override
    public CredentialsDto provisionCredentials(String applicationId, String credentialsBody) throws ControlServiceException {
        CredentialsDto credentials = new CredentialsDto(Base64Utils.decodeFromString(credentialsBody), CredentialsStatus.AVAILABLE);
        try {
            return this.credentialsServiceLocator.getCredentialsService(applicationId).provideCredentials(credentials);
        } catch (CredentialsServiceException cause) {
            String message = MessageFormat.format("An unexpected exception occured while saving credentials [{0}]", credentials);
            LOG.error(message, cause);
            throw new ControlServiceException(cause);
        }
    }

    @Override
    public Optional<CredentialsDto> getCredentials(String applicationId, String credentialsId) throws ControlServiceException {
        try {
            return this.credentialsServiceLocator.getCredentialsService(applicationId).lookupCredentials(credentialsId);
        } catch (CredentialsServiceException cause) {
            String message = MessageFormat.format("An unexpected exception occured while searching for credentials by ID [{0}]", credentialsId);
            LOG.error(message, cause);
            throw new ControlServiceException(cause);
        }
    }

    @Override
    public void revokeCredentials(String applicationId, String credentialsId) throws ControlServiceException {
        try {
            this.credentialsServiceLocator.getCredentialsService(applicationId).markCredentialsRevoked(credentialsId);
            onCredentailsRevoked(applicationId, credentialsId);
        } catch (CredentialsServiceException cause) {
            String message = MessageFormat.format("An unexpected exception occured while revoking credentials by ID [{0}]", credentialsId);
            LOG.error(message, cause);
            throw new ControlServiceException(cause);
        }
    }

    @Override
    public void onCredentailsRevoked(String applicationId, String credentialsId) throws ControlServiceException {
        LOG.debug("[{}] Lookup registration information based on credentials ID [{}]", applicationId, credentialsId);
        try {
            Optional<EndpointRegistrationDto> endpointRegistrationOptional = endpointRegistrationService
                    .findEndpointRegistrationByCredentialsId(credentialsId);
            if (endpointRegistrationOptional.isPresent()) {
                EndpointRegistrationDto endpointRegistration = endpointRegistrationOptional.get();
                LOG.debug("[{}] Found endpoint registration information [{}]", applicationId, endpointRegistration);
                if (endpointRegistration.getEndpointId() != null) {
                    checkNeighbors();
                    ApplicationDto appDto = getApplication(endpointRegistration.getApplicationId());
                    ThriftEndpointDeregistrationMessage nf = new ThriftEndpointDeregistrationMessage();
                    nf.setAddress(new ThriftEntityAddress(appDto.getTenantId(), appDto.getApplicationToken(),
                            ThriftClusterEntityType.ENDPOINT, ByteBuffer.wrap(Base64Util.decode(endpointRegistration.getEndpointId()))));
                    nf.setActorClassifier(ThriftActorClassifier.APPLICATION);
                    neighbors.brodcastMessage(OperationsServiceMsg.fromDeregistration(nf));
                }
                endpointRegistrationService.removeEndpointRegistrationById(endpointRegistration.getId());
                LOG.debug("[{}] endpoint registration information [{}] removed", applicationId, endpointRegistration);
            } else {
                LOG.debug("[{}] No endpoint registration information provisioned for credentials ID [{}]", applicationId, credentialsId);
            }
        } catch (EndpointRegistrationServiceException cause) {
            String message = MessageFormat.format(
                    "An unexpected exception occured while lookup registration information based on credentails ID [{0}]", credentialsId);
            LOG.error(message, cause);
            throw new ControlServiceException(cause);
        }
    }

    @Override
    public void provisionRegistration(
            String applicationId,
            String credentialsId,
            Integer serverProfileVersion,
            String serverProfileBody)
                    throws ControlServiceException {
        EndpointRegistrationDto endpointRegistration = new EndpointRegistrationDto(applicationId, null, credentialsId, serverProfileVersion, serverProfileBody);
        try {
            this.endpointRegistrationService.saveEndpointRegistration(endpointRegistration);
        } catch (EndpointRegistrationServiceException cause) {
            String message = MessageFormat.format("An unexpected exception occured while saving endpoint registration [{0}]", endpointRegistration);
            LOG.error(message, cause);
            throw new ControlServiceException(cause);
        }
    }

    @Override
    public List<String> getCredentialsServiceNames() throws ControlServiceException {
        return this.credentialsServiceRegistry.getCredentialsServiceNames();
    }
}
