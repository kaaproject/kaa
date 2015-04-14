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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.thrift.TException;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeConfigurationNotification;
import org.kaaproject.kaa.common.dto.ChangeNotificationDto;
import org.kaaproject.kaa.common.dto.ChangeProfileFilterNotification;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
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
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Version;
import org.kaaproject.kaa.server.common.core.schema.DataSchema;
import org.kaaproject.kaa.server.common.core.schema.ProtocolSchema;
import org.kaaproject.kaa.server.common.dao.ApplicationEventMapService;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.EventClassService;
import org.kaaproject.kaa.server.common.dao.LogAppendersService;
import org.kaaproject.kaa.server.common.dao.LogSchemaService;
import org.kaaproject.kaa.server.common.dao.NotificationService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.SdkKeyService;
import org.kaaproject.kaa.server.common.dao.TopicService;
import org.kaaproject.kaa.server.common.dao.UserConfigurationService;
import org.kaaproject.kaa.server.common.dao.UserService;
import org.kaaproject.kaa.server.common.dao.UserVerifierService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.model.sql.SdkKey;
import org.kaaproject.kaa.server.common.log.shared.RecordWrapperSchemaGenerator;
import org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftException;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.control.FileData;
import org.kaaproject.kaa.server.common.thrift.gen.control.Sdk;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Operation;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService.Iface;
import org.kaaproject.kaa.server.common.thrift.gen.operations.UserConfigurationUpdate;
import org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct;
import org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter;
import org.kaaproject.kaa.server.common.thrift.util.ThriftExecutor;
import org.kaaproject.kaa.server.common.zk.control.ControlNode;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.kaaproject.kaa.server.control.service.log.RecordLibraryGenerator;
import org.kaaproject.kaa.server.control.service.sdk.SdkGenerator;
import org.kaaproject.kaa.server.control.service.sdk.SdkGeneratorFactory;
import org.kaaproject.kaa.server.control.service.sdk.event.EventFamilyMetadata;
import org.kaaproject.kaa.server.control.service.zk.ControlZkService;
import org.kaaproject.kaa.server.hash.ConsistentHashResolver;
import org.kaaproject.kaa.server.resolve.OperationsServerResolver;
import org.kaaproject.kaa.server.thrift.NeighborTemplate;
import org.kaaproject.kaa.server.thrift.Neighbors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The Class ControlThriftServiceImpl.<br>
 * Implementation of Control Service Thrift Interface.
 */
@Service
public class ControlThriftServiceImpl extends BaseCliThriftService implements ControlThriftService.Iface {

    private static final int DEFAULT_NEIGHBOR_CONNECTIONS_SIZE = 10;
    private static final int DEFAULT_USER_HASH_PARTITIONS_SIZE = 10;

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ControlThriftServiceImpl.class);

    private static final String SCHEMA_NAME_PATTERN = "kaa-record-schema-l{}.avsc";

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

    /** The user configuration service. */
    @Autowired
    private UserConfigurationService userConfigurationService;

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

    /** The event class service. */
    @Autowired
    private EventClassService eventClassService;

    /** The application event map service. */
    @Autowired
    private ApplicationEventMapService applicationEventMapService;

    /** The control zookeeper service. */
    @Autowired
    private ControlZkService controlZKService;

    @Autowired
    private LogSchemaService logSchemaService;

    @Autowired
    private LogAppendersService logAppenderService;

    @Autowired
    private UserVerifierService userVerifierService;

    @Autowired
    private SdkKeyService sdkKeyService;

    @Value("#{properties[max_number_neighbor_connections]}")
    private int neighborConnectionsSize = DEFAULT_NEIGHBOR_CONNECTIONS_SIZE;

    @Value("#{properties[user_hash_partitions]}")
    private int userHashPartitions = DEFAULT_USER_HASH_PARTITIONS_SIZE;

    private volatile Neighbors<NeighborTemplate<UserConfigurationUpdate>, UserConfigurationUpdate> neighbors;

    private volatile OperationsServerResolver resolver;

    private Object zkLock = new Object();

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
     * . Iface#editTenant(org.kaaproject.kaa.server.common.thrift.gen.shared
     * .DataStruct)
     */
    /* CLI method */
    @Override
    public DataStruct editTenant(DataStruct tenant) throws TException {
        return toDataStruct(userService.saveTenant(ThriftDtoConverter.<TenantDto>toDto(tenant)));
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getTenantUsers(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getTenantUsers(String tenantId) throws TException {
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
     * .Iface#getUserByExternalUid(java.lang.String, java.lang.String)
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
        return toDataStruct(userService.saveUser(ThriftDtoConverter.<UserDto>toDto(user)));
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getTenantAdmins()
     */
    /* GUI method */
    @Override
    public List<DataStruct> getTenantAdmins() throws TException {
        return toDataStructList(userService.findAllTenantAdmins());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getTenantAdmin(java.lang.String)
     */
    /* GUI method */
    @Override
    public DataStruct getTenantAdmin(String tenantId) throws TException {
        return toDataStruct(userService.findTenantAdminById(tenantId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface
     * #editTenantAdmin(org.kaaproject.kaa.server.common.thrift.gen.shared.
     * DataStruct)
     */
    /* GUI method */
    @Override
    public DataStruct editTenantAdmin(DataStruct tenantAdmin) throws TException {
        return toDataStruct(userService.saveTenantAdmin(ThriftDtoConverter.<TenantAdminDto>toDto(tenantAdmin)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deleteTenantAdmin(java.lang.String)
     */
    /* GUI method */
    @Override
    public void deleteTenantAdmin(String tenantId) throws TException {
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
        return toDataStruct(applicationService.findAppById(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getApplicationByApplicationToken(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct getApplicationByApplicationToken(String applicationToken) throws TException {
        return toDataStruct(applicationService.findAppByApplicationToken(applicationToken));
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
    public List<DataStruct> getApplicationsByTenantId(String tenantId) throws TException {
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
        return toDataStruct(applicationService.saveApp(ThriftDtoConverter.<ApplicationDto>toDto(application)));
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
    public List<DataStruct> getConfigurationSchemasByApplicationId(String applicationId) throws TException {
        return toDataStructList(configurationService.findConfSchemasByAppId(applicationId));
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
    public DataStruct getConfigurationSchema(String configurationSchemaId) throws TException {
        return toDataStruct(configurationService.findConfSchemaById(configurationSchemaId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * . Iface#editConfigurationSchema(org.kaaproject.kaa.server.common.thrift
     * .gen.shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editConfigurationSchema(DataStruct configurationSchema) throws TException {
        ConfigurationSchemaDto confSchema = null;
        try {
            confSchema = configurationService.saveConfSchema(ThriftDtoConverter.<ConfigurationSchemaDto> toDto(configurationSchema));
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
    public List<DataStruct> getProfileSchemasByApplicationId(String applicationId) throws TException {
        return toDataStructList(profileService.findProfileSchemasByAppId(applicationId));
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
    public DataStruct getProfileSchema(String profileSchemaId) throws TException {
        return toDataStruct(profileService.findProfileSchemaById(profileSchemaId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * . Iface#editProfileSchema(org.kaaproject.kaa.server.common.thrift.gen.
     * shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editProfileSchema(DataStruct profileSchema) throws TException {
        return toDataStruct(profileService.saveProfileSchema(ThriftDtoConverter.<ProfileSchemaDto>toDto(profileSchema)));
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
    public List<DataStruct> getEndpointGroupsByApplicationId(String applicationId) throws TException {
        return toDataStructList(endpointService.findEndpointGroupsByAppId(applicationId));
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
    public DataStruct getEndpointGroup(String endpointGroupId) throws TException {
        return toDataStruct(endpointService.findEndpointGroupById(endpointGroupId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * . Iface#editEndpointGroup(org.kaaproject.kaa.server.common.thrift.gen.
     * shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editEndpointGroup(DataStruct endpointGroup) throws TException {
        return toDataStruct(endpointService.saveEndpointGroup(ThriftDtoConverter.<EndpointGroupDto>toDto(endpointGroup)));
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
     * @param endpointGroupId
     *            the endpoint group id
     * @param topicId
     *            the topic id
     * @return the data struct
     * @throws TException
     *             the t exception
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
     * @param endpointGroupId
     *            the endpoint group id
     * @param topicId
     *            the topic id
     * @return the data struct
     * @throws TException
     *             the t exception
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
    public DataStruct getProfileFilter(String profileFilterId) throws TException {
        return toDataStruct(profileService.findProfileFilterById(profileFilterId));
    }

    /* GUI method */
    @Override
    public List<DataStruct> getProfileFilterRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated) throws TException {
        return toGenericDataStructList(profileService.findAllProfileFilterRecordsByEndpointGroupId(endpointGroupId, includeDeprecated));
    }

    /* GUI method */
    @Override
    public DataStruct getProfileFilterRecord(String schemaId, String endpointGroupId) throws TException {
        return toGenericDataStruct(profileService.findProfileFilterRecordBySchemaIdAndEndpointGroupId(schemaId, endpointGroupId));
    }

    /* GUI method */
    @Override
    public List<DataStruct> getVacantProfileSchemasByEndpointGroupId(String endpointGroupId) throws TException {
        return toDataStructList(profileService.findVacantSchemasByEndpointGroupId(endpointGroupId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * . Iface#editProfileFilter(org.kaaproject.kaa.server.common.thrift.gen.
     * shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editProfileFilter(DataStruct profileFilter) throws TException {
        return toDataStruct(profileService.saveProfileFilter(ThriftDtoConverter.<ProfileFilterDto>toDto(profileFilter)));
    }

    /* GUI method */
    @Override
    public List<DataStruct> getConfigurationRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated) throws TException {
        return toGenericDataStructList(configurationService
                .findAllConfigurationRecordsByEndpointGroupId(endpointGroupId, includeDeprecated));
    }

    /* GUI method */
    @Override
    public DataStruct getConfigurationRecord(String schemaId, String endpointGroupId) throws TException {
        return toGenericDataStruct(configurationService.findConfigurationRecordBySchemaIdAndEndpointGroupId(schemaId, endpointGroupId));
    }

    /* GUI method */
    @Override
    public List<DataStruct> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId) throws TException {
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
    public DataStruct getConfiguration(String configurationId) throws TException {
        return toDataStruct(configurationService.findConfigurationById(configurationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * . Iface#editConfiguration(org.kaaproject.kaa.server.common.thrift.gen.
     * shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editConfiguration(DataStruct configuration) throws TException {
        ConfigurationDto cfg = configurationService.saveConfiguration(ThriftDtoConverter.<ConfigurationDto> toDto(configuration));
        return toDataStruct(cfg);
    }

    @Override
    public void editUserConfiguration(DataStruct configuration) throws ControlThriftException, TException {
        EndpointUserConfigurationDto ucfDto = ThriftDtoConverter.<EndpointUserConfigurationDto> toGenericDto(configuration);

        ApplicationDto appDto = applicationService.findAppByApplicationToken(ucfDto.getAppToken());

        EndpointUserDto userDto = endpointService.findEndpointUserByExternalIdAndTenantId(ucfDto.getUserId(), appDto.getTenantId());

        ucfDto.setUserId(userDto.getId());
        ucfDto = userConfigurationService.saveUserConfiguration(ucfDto);

        EndpointObjectHash hash = EndpointObjectHash.fromString(ucfDto.getBody());

        checkNeighbors();

        OperationsNodeInfo server = resolve(ucfDto.getUserId());

        if (server != null) {
            UserConfigurationUpdate msg = new UserConfigurationUpdate(appDto.getTenantId(), ucfDto.getUserId(), ucfDto.getAppToken(),
                    ucfDto.getSchemaVersion(), hash.getDataBuf());
            if (LOG.isTraceEnabled()) {
                LOG.trace("Sending message {} to [{}]", msg, Neighbors.getServerID(server.getConnectionInfo()));
            }
            neighbors.sendMessage(server.getConnectionInfo(), msg);
        } else {
            LOG.warn("Can't find server for user [{}]", ucfDto.getUserId());
        }
    }

    private void checkNeighbors() {
        if (neighbors == null) {
            synchronized (zkLock) {
                if (neighbors == null) {
                    neighbors = new Neighbors<NeighborTemplate<UserConfigurationUpdate>, UserConfigurationUpdate>(
                            new NeighborTemplate<UserConfigurationUpdate>() {
                                @Override
                                public void process(Iface client, List<UserConfigurationUpdate> messages) throws TException {
                                    client.sendUserConfigurationUpdates(messages);
                                }

                                @Override
                                public void onServerError(String serverId, Exception e) {
                                    LOG.error("Can't send configuration update to {}", serverId, e);
                                }
                            }, neighborConnectionsSize);
                    ControlNode zkNode = controlZKService.getControlZKNode();
                    neighbors.setZkNode(zkNode.getControlServerInfo().getConnectionInfo(), zkNode);
                }
            }
        }
    }

    private OperationsNodeInfo resolve(String userId) {
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
        return resolver.getNode(userId);
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
    public DataStruct activateConfiguration(String configurationId, String activatedUsername) throws TException {
        ChangeConfigurationNotification cfgNotification = configurationService.activateConfiguration(configurationId, activatedUsername);
        ChangeNotificationDto notification = cfgNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, null, cfgNotification.getConfigurationDto());
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
    public DataStruct deactivateConfiguration(String configurationId, String deactivatedUsername) throws TException {
        ChangeConfigurationNotification cfgNotification = configurationService
                .deactivateConfiguration(configurationId, deactivatedUsername);
        ChangeNotificationDto notification = cfgNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, null, cfgNotification.getConfigurationDto());
        }
        return toDataStruct(cfgNotification.getConfigurationDto());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deleteConfigurationRecord(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    /* GUI method */
    @Override
    public void deleteConfigurationRecord(String schemaId, String endpointGroupId, String deactivatedUsername) throws TException {
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
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#activateProfileFilter(java.lang.String, java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct activateProfileFilter(String profileFilterId, String activatedUsername) throws TException {
        ChangeProfileFilterNotification cpfNotification = profileService.activateProfileFilter(profileFilterId, activatedUsername);
        ChangeNotificationDto notification = cpfNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, cpfNotification.getProfileFilterDto(), null);
        }
        return toDataStruct(cpfNotification.getProfileFilterDto());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deactivateProfileFilter(java.lang.String, java.lang.String)
     */
    /* GUI method */
    @Override
    public DataStruct deactivateProfileFilter(String profileFilterId, String deactivatedUsername) throws TException {
        ChangeProfileFilterNotification cpfNotification = profileService.deactivateProfileFilter(profileFilterId, deactivatedUsername);
        ChangeNotificationDto notification = cpfNotification.getChangeNotificationDto();
        if (notification != null) {
            notifyEndpoints(notification, cpfNotification.getProfileFilterDto(), null);
        }
        return toDataStruct(cpfNotification.getProfileFilterDto());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deleteProfileFilterRecord(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    /* GUI method */
    @Override
    public void deleteProfileFilterRecord(String schemaId, String endpointGroupId, String deactivatedUsername) throws TException {
        ChangeProfileFilterNotification cpfNotification = profileService.deleteProfileFilterRecord(schemaId, endpointGroupId,
                deactivatedUsername);
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
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface
     * #generateSdk(org.kaaproject.kaa.server.common.thrift.gen.control.SdkPlatform
     * , java.lang.String, int, int, int, java.util.List)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public Sdk generateSdk(DataStruct sdkProperties) throws TException {
        /*
        SdkPlatform sdkPlatform, String applicationId, int profileSchemaVersion, int configurationSchemaVersion,
            int notificationSchemaVersion, List<String> aefMapIds, int logSchemaVersion, String defaultVerifierToken
         */
        SdkPropertiesDto sdkPropertiesDto = ThriftDtoConverter.toDto(sdkProperties);
        try {
            ApplicationDto application = applicationService.findAppById(sdkPropertiesDto.getApplicationId());
            if (application == null) {
                throw new TException("Application not found!");
            }
            ProfileSchemaDto profileSchema = profileService.findProfileSchemaByAppIdAndVersion(sdkPropertiesDto.getApplicationId(),
                    sdkPropertiesDto.getProfileSchemaVersion());
            if (profileSchema == null) {
                throw new TException("Profile schema not found!");
            }
            ConfigurationSchemaDto configurationSchema = configurationService.findConfSchemaByAppIdAndVersion(sdkPropertiesDto.getApplicationId(),
                    sdkPropertiesDto.getConfigurationSchemaVersion());
            if (configurationSchema == null) {
                throw new TException("Configuration schema not found!");
            }
            ConfigurationDto defaultConfiguration = configurationService.findDefaultConfigurationBySchemaId(configurationSchema.getId());
            if (defaultConfiguration == null) {
                throw new TException("Default configuration not found!");
            }
            NotificationSchemaDto notificationSchema = notificationService.findNotificationSchemaByAppIdAndTypeAndVersion(sdkPropertiesDto.getApplicationId(),
                    NotificationTypeDto.USER, sdkPropertiesDto.getNotificationSchemaVersion());
            if (notificationSchema == null) {
                throw new TException("Notification schema not found!");
            }

            LogSchemaDto logSchema = logSchemaService.findLogSchemaByAppIdAndVersion(sdkPropertiesDto.getApplicationId(),
                    sdkPropertiesDto.getLogSchemaVersion());
            if (logSchema == null) {
                throw new TException("Log schema not found!");
            }

            DataSchema profileDataSchema = new DataSchema(profileSchema.getSchema());
            DataSchema notificationDataSchema = new DataSchema(notificationSchema.getSchema());
            ProtocolSchema protocolSchema = new ProtocolSchema(configurationSchema.getProtocolSchema());
            DataSchema logDataSchema = new DataSchema(logSchema.getSchema());

            String appToken = application.getApplicationToken();
            String profileSchemaBody = profileDataSchema.getRawSchema();

            byte[] defaultConfigurationData = GenericAvroConverter.toRawData(defaultConfiguration.getBody(),
                    configurationSchema.getBaseSchema());

            List<EventFamilyMetadata> eventFamilies = new ArrayList<>();
            if (sdkPropertiesDto.getAefMapIds() != null) {
                List<ApplicationEventFamilyMapDto> aefMaps = applicationEventMapService.findApplicationEventFamilyMapsByIds(sdkPropertiesDto.getAefMapIds());
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

            sdkPropertiesDto.setApplicationToken(appToken);
            sdkPropertiesDto = sdkKeyService.saveSdkKey(sdkPropertiesDto);
            String sdkToken = new SdkKey(sdkPropertiesDto).getToken();
            LOG.debug("Sdk properties for sdk generation: {}", sdkPropertiesDto);

            SdkGenerator generator = SdkGeneratorFactory.createSdkGenerator(sdkPropertiesDto.getTargetPlatform());
            return generator.generateSdk(Version.PROJECT_VERSION, controlZKService.getCurrentBootstrapNodes(), sdkToken,
                    sdkPropertiesDto, profileSchemaBody, notificationDataSchema.getRawSchema(), protocolSchema.getRawSchema(),
                    configurationSchema.getBaseSchema(), defaultConfigurationData, eventFamilies, logDataSchema.getRawSchema());
        } catch (Exception e) {
            LOG.error("Unable to generate SDK", e);
            throw new TException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#generateRecordStructureLibrary(java.lang.String, int)
     */
    @Override
    public FileData generateRecordStructureLibrary(String applicationId, int logSchemaVersion) throws ControlThriftException, TException {
        try {
            ApplicationDto application = applicationService.findAppById(applicationId);
            if (application == null) {
                throw new TException("Application not found!");
            }
            LogSchemaDto logSchema = logSchemaService.findLogSchemaByAppIdAndVersion(applicationId, logSchemaVersion);
            if (logSchema == null) {
                throw new TException("Log schema not found!");
            }
            return RecordLibraryGenerator.generateRecordLibrary(logSchemaVersion, logSchema.getSchema());
        } catch (Exception e) {
            LOG.error("Unable to generate Record Structure Library", e);
            throw new TException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface
     * #editNotificationSchema(org.kaaproject.kaa.server.common.thrift.gen.
     * shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editNotificationSchema(DataStruct notificationSchema) throws TException {
        return toDataStruct(notificationService
                .saveNotificationSchema(ThriftDtoConverter.<NotificationSchemaDto> toDto(notificationSchema)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getNotificationSchema(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct getNotificationSchema(String notificationSchemaId) throws TException {
        return toDataStruct(notificationService.findNotificationSchemaById(notificationSchemaId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getNotificationSchemasByAppId(java.lang.String)
     */
    /* CLI method */
    @Override
    public List<DataStruct> getNotificationSchemasByAppId(String applicationId) throws TException {
        return toDataStructList(notificationService.findNotificationSchemasByAppId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getUserNotificationSchemasByAppId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getUserNotificationSchemasByAppId(String applicationId) throws TException {
        return toDataStructList(notificationService.findUserNotificationSchemasByAppId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#findNotificationSchemasByAppIdAndType(java.lang.String,
     * org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct)
     */
    /* GUI method */
    @Override
    public List<DataStruct> findNotificationSchemasByAppIdAndType(String applicationId, DataStruct type) throws TException {
        NotificationTypeDto notificationType = toGenericDto(type);
        return toDataStructList(notificationService.findNotificationSchemasByAppIdAndType(applicationId, notificationType));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#editLogSchema(java.lang.String)
     */
    /* GUI method */
    @Override
    public DataStruct editLogSchema(DataStruct logSchemaDto) throws ControlThriftException, TException {
        return toDataStruct(logSchemaService.saveLogSchema(ThriftDtoConverter.<LogSchemaDto> toDto(logSchemaDto)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getLogSchemasByApplicationId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getLogSchemasByApplicationId(String applicationId) throws ControlThriftException, TException {
        return toDataStructList(logSchemaService.findLogSchemasByAppId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getLogSchema(java.lang.String)
     */
    /* GUI method */
    @Override
    public DataStruct getLogSchema(String logSchemaId) throws TException {
        return toDataStruct(logSchemaService.findLogSchemaById(logSchemaId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getLogSchemaByApplicationIdAndVersion(java.lang.String,
     * java.lang.String)
     */
    /* GUI method */
    @Override
    public DataStruct getLogSchemaByApplicationIdAndVersion(String applicationId, int version) throws TException {
        return toDataStruct(logSchemaService.findLogSchemaByAppIdAndVersion(applicationId, version));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface
     * #editNotification(org.kaaproject.kaa.server.common.thrift.gen.shared
     * .DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editNotification(DataStruct notification) throws TException {
        return notifyAndGetPayload(notificationService.saveNotification(ThriftDtoConverter.<NotificationDto> toDto(notification)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getNotification(java.lang.String)
     */
    /* CLI method */
    @Override
    public DataStruct getNotification(String notificationId) throws TException {
        return toDataStruct(notificationService.findNotificationById(notificationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getNotificationsByTopicId(java.lang.String)
     */
    /* CLI method */
    @Override
    public List<DataStruct> getNotificationsByTopicId(String topicId) throws TException {
        return toDataStructList(notificationService.findNotificationsByTopicId(topicId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface
     * #editTopic(org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct editTopic(DataStruct topic) throws TException {
        return toDataStruct(topicService.saveTopic(ThriftDtoConverter.<TopicDto> toDto(topic)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getTopic(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public DataStruct getTopic(String topicId) throws TException {
        return toDataStruct(topicService.findTopicById(topicId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getTopicByAppId(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public List<DataStruct> getTopicByAppId(String appId) throws TException {
        return toDataStructList(topicService.findTopicsByAppId(appId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getTopicByEndpointGroupId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getTopicByEndpointGroupId(String endpointGroupId) throws TException {
        return toDataStructList(topicService.findTopicsByEndpointGroupId(endpointGroupId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getVacantTopicByEndpointGroupId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getVacantTopicByEndpointGroupId(String endpointGroupId) throws TException {
        return toDataStructList(topicService.findVacantTopicsByEndpointGroupId(endpointGroupId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deleteTopicById(java.lang.String)
     */
    /* GUI method */
    /* CLI method */
    @Override
    public void deleteTopicById(String topicId) throws TException {
        for (UpdateNotificationDto dto : topicService.removeTopicById(topicId)) {
            notifyAndGetPayload(dto);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getUnicastNotification(java.lang.String)
     */
    /* CLI method */
    @Override
    public DataStruct getUnicastNotification(String notificationId) throws TException {
        return toDataStruct(notificationService.findUnicastNotificationById(notificationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface
     * #editUnicastNotification(org.kaaproject.kaa.server.common.thrift.gen
     * .shared.DataStruct)
     */
    /* CLI method */
    @Override
    public DataStruct editUnicastNotification(DataStruct notification) throws TException {
        return notifyAndGetPayload(notificationService.saveUnicastNotification(ThriftDtoConverter
                .<EndpointNotificationDto> toDto(notification)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getUnicastNotificationsByKeyHash(java.nio.ByteBuffer)
     */
    @Override
    public List<DataStruct> getUnicastNotificationsByKeyHash(ByteBuffer keyhash) throws TException {
        List<DataStruct> structList = Collections.emptyList();
        if (keyhash != null) {
            structList = toDataStructList(notificationService.findUnicastNotificationsByKeyHash(keyhash.array()));
        }
        return structList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getConfigurationSchemaVersionsByApplicationId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getConfigurationSchemaVersionsByApplicationId(String applicationId) throws TException {
        return toDataStructList(configurationService.findConfigurationSchemaVersionsByAppId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getProfileSchemaVersionsByApplicationId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getProfileSchemaVersionsByApplicationId(String applicationId) throws TException {
        return toDataStructList(profileService.findProfileSchemaVersionsByAppId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getNotificationSchemaVersionsByApplicationId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getNotificationSchemaVersionsByApplicationId(String applicationId) throws TException {
        return toDataStructList(notificationService.findNotificationSchemaVersionsByAppId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getLogSchemaVersionsByApplicationId(java.lang.String)
     */
    /* GUI method */
    @Override
    public List<DataStruct> getLogSchemaVersionsByApplicationId(String applicationId) throws TException {
        return toDataStructList(logSchemaService.findLogSchemaVersionsByApplicationId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface
     * #editEventClassFamily(org.kaaproject.kaa.server.common.thrift.gen.shared
     * .DataStruct)
     */
    @Override
    public DataStruct editEventClassFamily(DataStruct eventClassFamily) throws ControlThriftException, TException {
        return toDataStruct(eventClassService.saveEventClassFamily(ThriftDtoConverter.<EventClassFamilyDto> toDto(eventClassFamily)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getEventClassFamiliesByTenantId(java.lang.String)
     */
    @Override
    public List<DataStruct> getEventClassFamiliesByTenantId(String tenantId) throws ControlThriftException, TException {
        return toDataStructList(eventClassService.findEventClassFamiliesByTenantId(tenantId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getEventClassFamily(java.lang.String)
     */
    @Override
    public DataStruct getEventClassFamily(String eventClassFamilyId) throws ControlThriftException, TException {
        return toDataStruct(eventClassService.findEventClassFamilyById(eventClassFamilyId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#addEventClassFamilySchema(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void addEventClassFamilySchema(String eventClassFamilyId, String eventClassFamilySchema, String createdUsername)
            throws ControlThriftException, TException {
        eventClassService.addEventClassFamilySchema(eventClassFamilyId, eventClassFamilySchema, createdUsername);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getEventClassesByFamilyIdAndVersion(java.lang.String, int,
     * org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct)
     */
    @Override
    public List<DataStruct> getEventClassesByFamilyIdVersionAndType(String ecfId, int version, DataStruct type)
            throws ControlThriftException, TException {
        EventClassType eventClassType = toGenericDto(type);
        return toDataStructList(eventClassService.findEventClassesByFamilyIdVersionAndType(ecfId, version, eventClassType));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface
     * #editApplicationEventFamilyMap(org.kaaproject.kaa.server.common.thrift
     * .gen.shared.DataStruct)
     */
    @Override
    public DataStruct editApplicationEventFamilyMap(DataStruct applicationEventFamilyMap) throws ControlThriftException, TException {
        return toDataStruct(applicationEventMapService.saveApplicationEventFamilyMap(ThriftDtoConverter
                .<ApplicationEventFamilyMapDto> toDto(applicationEventFamilyMap)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getApplicationEventFamilyMap(java.lang.String)
     */
    @Override
    public DataStruct getApplicationEventFamilyMap(String applicationEventFamilyMapId) throws ControlThriftException, TException {
        return toDataStruct(applicationEventMapService.findApplicationEventFamilyMapById(applicationEventFamilyMapId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getApplicationEventFamilyMapsByApplicationId(java.lang.String)
     */
    @Override
    public List<DataStruct> getApplicationEventFamilyMapsByApplicationId(String applicationId) throws ControlThriftException, TException {
        return toDataStructList(applicationEventMapService.findApplicationEventFamilyMapsByApplicationId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getVacantEventClassFamiliesByApplicationId(java.lang.String)
     */
    @Override
    public List<DataStruct> getVacantEventClassFamiliesByApplicationId(String applicationId) throws ControlThriftException, TException {
        return toGenericDataStructList(applicationEventMapService.findVacantEventClassFamiliesByApplicationId(applicationId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getEventClassFamiliesByApplicationId(java.lang.String)
     */
    @Override
    public List<DataStruct> getEventClassFamiliesByApplicationId(String applicationId) throws ControlThriftException, TException {
        return toGenericDataStructList(applicationEventMapService.findEventClassFamiliesByApplicationId(applicationId));
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
     * @see org.kaaproject.kaa.server.common.thrift.gen.cli.CliThriftService
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
     * @param notification
     *            the notification
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
                LOG.info("Unsupported change type. Check Operation and ChangeType enums.");
            }
        }
        return operation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getEndpointUsers()
     */
    /* CLI method */
    @Override
    public List<DataStruct> getEndpointUsers() throws ControlThriftException, TException {
        return toDataStructList(endpointService.findAllEndpointUsers());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#getEndpointUser(java.lang.String)
     */
    /* CLI method */
    @Override
    public DataStruct getEndpointUser(String endpointUserId) throws ControlThriftException, TException {
        return toDataStruct(endpointService.findEndpointUserById(endpointUserId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .
     * Iface#editEndpointUser(org.kaaproject.kaa.server.common.thrift.gen.shared
     * .DataStruct)
     */
    /* CLI method */
    @Override
    public DataStruct editEndpointUser(DataStruct endpointUser) throws ControlThriftException, TException {
        return toDataStruct(endpointService.saveEndpointUser(ThriftDtoConverter.<EndpointUserDto> toDto(endpointUser)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#deleteEndpointUser(java.lang.String)
     */
    /* CLI method */
    @Override
    public void deleteEndpointUser(String endpointUserId) throws ControlThriftException, TException {
        endpointService.removeEndpointUserById(endpointUserId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService
     * .Iface#generateEndpointUserAccessToken(java.lang.String)
     */
    /* CLI method */
    @Override
    public String generateEndpointUserAccessToken(String externalUid, String tenantId) throws ControlThriftException, TException {
        return endpointService.generateEndpointUserAccessToken(externalUid, tenantId);
    }

    @Override
    public List<DataStruct> getLogAppendersByApplicationId(String applicationId) throws ControlThriftException, TException {
        return toDataStructList(logAppenderService.findAllAppendersByAppId(applicationId));
    }

    @Override
    public DataStruct getLogAppender(String logAppenderId) throws ControlThriftException, TException {
        return toDataStruct(logAppenderService.findLogAppenderById(logAppenderId));
    }

    @Override
    public DataStruct editLogAppender(DataStruct logAppender) throws ControlThriftException, TException {
        LogAppenderDto appenderDto = ThriftDtoConverter.<LogAppenderDto> toDto(logAppender);
        DataStruct dataStruct = null;
        if (appenderDto != null) {
            LogAppenderDto saved = logAppenderService.saveLogAppender(appenderDto);
            if (saved != null) {
                Notification thriftNotification = new Notification();
                thriftNotification.setAppId(saved.getApplicationId());
                thriftNotification.setAppenderId(saved.getId());
                if (appenderDto.getId() == null) {
                    LOG.info("Add new log appender ...");
                    thriftNotification.setOp(Operation.ADD_LOG_APPENDER);
                    LOG.info("Send notification to operation servers about new appender.");
                } else {
                    thriftNotification.setOp(Operation.UPDATE_LOG_APPENDER);
                    LOG.info("Send notification to operation servers about update appender configuration.");
                }
                dataStruct = toDataStruct(saved);
                controlZKService.sendEndpointNotification(thriftNotification);
            }
        }
        return dataStruct;
    }

    @Override
    public void deleteLogAppender(String logAppenderId) throws ControlThriftException, TException {
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

    @Override
    public List<DataStruct> getUserVerifiersByApplicationId(String applicationId) throws ControlThriftException, TException {
        return toDataStructList(userVerifierService.findUserVerifiersByAppId(applicationId));
    }

    @Override
    public DataStruct getUserVerifier(String userVerifierId) throws ControlThriftException, TException {
        return toDataStruct(userVerifierService.findUserVerifierById(userVerifierId));
    }

    @Override
    public DataStruct editUserVerifier(DataStruct userVerifier) throws ControlThriftException, TException {
        UserVerifierDto userVerifierDto = ThriftDtoConverter.<UserVerifierDto> toDto(userVerifier);
        LOG.info("Adding new user verifier {}", userVerifierDto);
        DataStruct dataStruct = null;
        if (userVerifierDto != null) {
            UserVerifierDto saved = userVerifierService.saveUserVerifier(userVerifierDto);
            LOG.info("Saved user verifier {}", saved);
            if (saved != null) {
                Notification thriftNotification = new Notification();
                thriftNotification.setAppId(saved.getApplicationId());
                thriftNotification.setUserVerifierToken(saved.getVerifierToken());
                if (userVerifierDto.getId() == null) {
                    LOG.info("Add new user verifier ...");
                    thriftNotification.setOp(Operation.ADD_USER_VERIFIER);
                    LOG.info("Send notification to operation servers about new user verifier.");
                } else {
                    thriftNotification.setOp(Operation.UPDATE_USER_VERIFIER);
                    LOG.info("Send notification to operation servers about update user verifier configuration.");
                }
                dataStruct = toDataStruct(saved);
                controlZKService.sendEndpointNotification(thriftNotification);
            }
        }
        return dataStruct;
    }

    @Override
    public void deleteUserVerifier(String userVerifierId) throws ControlThriftException, TException {
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

    @Override
    public FileData getRecordStructureSchema(String applicationId, int logSchemaVersion) throws ControlThriftException, TException {

        try {
            ApplicationDto application = applicationService.findAppById(applicationId);
            if (application == null) {
                throw new TException("Application not found!");
            }
            LogSchemaDto logSchema = logSchemaService.findLogSchemaByAppIdAndVersion(applicationId, logSchemaVersion);
            if (logSchema == null) {
                throw new TException("Log schema not found!");
            }

            Schema recordWrapperSchema = RecordWrapperSchemaGenerator.generateRecordWrapperSchema(logSchema.getSchema());
            String libraryFileName = MessageFormatter.arrayFormat(SCHEMA_NAME_PATTERN, new Object[] { logSchemaVersion }).getMessage();
            String schemaInJson = recordWrapperSchema.toString(true);
            byte[] schemaData = schemaInJson.getBytes(StandardCharsets.UTF_8);

            FileData schema = new FileData();
            schema.setFileName(libraryFileName);
            schema.setData(schemaData);
            return schema;

        } catch (Exception e) {
            LOG.error("Unable to get Record Structure Schema", e);
            throw new TException(e);
        }
    }

}
