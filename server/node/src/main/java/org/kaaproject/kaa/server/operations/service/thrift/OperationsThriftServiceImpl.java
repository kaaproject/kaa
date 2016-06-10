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

package org.kaaproject.kaa.server.operations.service.thrift;

import java.security.PublicKey;
import java.util.List;

import org.apache.thrift.TException;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.ServerProfileService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Message;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Operation;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEndpointDeregistrationMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEntityRouteMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftServerProfileUpdateMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftUnicastNotificationMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.UserConfigurationUpdate;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.cache.AppProfileVersionsKey;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cluster.ClusterService;
import org.kaaproject.kaa.server.operations.service.event.EventService;
import org.kaaproject.kaa.server.operations.service.initialization.OperationsInitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The implementation of {#link
 * org.kaaproject.kaa.server.common.thrift.gen.operations
 * .OperationsThriftService.Iface OperationsThriftService}. The only one
 * specific method to Operations Service is {#link #onNotification(Notification
 * notification) onNotification}
 *
 * @author Andrew Shvayka
 */
@Service
public class OperationsThriftServiceImpl implements OperationsThriftService.Iface {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(OperationsThriftServiceImpl.class);

    /** The operations bootstrap service. */
    @Autowired
    OperationsInitializationService operationsBootstrapService;

    /** The cache service. */
    @Autowired
    CacheService cacheService;

    /** The akka service. */
    @Autowired
    AkkaService akkaService;

    /** The application service. */
    @Autowired
    ApplicationService applicationService;

    /** The event service */
    @Autowired
    EventService eventService;
    
    @Autowired
    ClusterService clusterService;

    @Autowired
    ProfileService profileService;

    @Autowired
    ServerProfileService serverProfileService;

    @Override
    public void onNotification(Notification notification) throws TException {
        LOG.debug("Received Notification from control server {}", notification);
        LOG.debug("Going to notify cache service..");
        processCacheNotification(notification);
        if (notification.getOp() != Operation.APP_UPDATE) {
            LOG.debug("Going to notify akka service..");
            akkaService.onNotification(notification);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.common.thrift.gen.operations.
     * OperationsThriftService
     * .Iface#setRedirectionRule(org.kaaproject.kaa.server
     * .common.thrift.gen.operations.RedirectionRule)
     */
    @Override
    public void setRedirectionRule(RedirectionRule redirectionRule) throws TException {
        LOG.debug("Received setRedirectionRule from control Dynamic Load Mgmt service {}", redirectionRule);
        LOG.debug("Notify akka service..");
        akkaService.onRedirectionRule(redirectionRule);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.common.thrift.gen.operations.
     * OperationsThriftService.Iface#sendEventMessage(java.util.List)
     */
    @Override
    public void sendMessages(List<Message> messages) throws TException {
        eventService.sendEventMessage(messages);
    }

    @Override
    public void sendUserConfigurationUpdates(List<UserConfigurationUpdate> updates) throws TException {
        for (UserConfigurationUpdate update : updates) {
            akkaService
                    .onUserConfigurationUpdate(org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserConfigurationUpdate
                            .fromThrift(update));
        }
    }

    /**
     * Process cache notification.
     *
     * @param notification
     *            the notification
     */
    private void processCacheNotification(Notification notification) {
        ApplicationDto appDto = applicationService.findAppById(notification.getAppId());
        LOG.debug("Processing cache notification {} for app {}", notification, appDto);
        if (appDto != null) {
            if (notification.getOp() == Operation.APP_UPDATE) {
                LOG.debug("Reseting application info {}", appDto.getId());
                cacheService.resetAppById(appDto.getId());
                return;
            }
            if (notification.getProfileFilterId() != null) {
                ProfileFilterDto filterDto = cacheService.getFilter(notification.getProfileFilterId());
                LOG.debug("Processing filter  {}", filterDto); 
                if (filterDto.getEndpointProfileSchemaId() != null && filterDto.getServerProfileSchemaId() != null) {
                    cacheService.resetFilters(new AppProfileVersionsKey(appDto.getApplicationToken(), filterDto
                            .getEndpointProfileSchemaVersion(), filterDto.getServerProfileSchemaVersion()));
                } else if (filterDto.getServerProfileSchemaVersion() == null) {
                    for (VersionDto version : profileService.findProfileSchemaVersionsByAppId(appDto.getId())) {
                        LOG.debug("Processing version {}", version);
                        cacheService.resetFilters(new AppProfileVersionsKey(appDto.getApplicationToken(), version.getVersion(), null));
                    }
                } else {
                    for (ServerProfileSchemaDto version : serverProfileService.findServerProfileSchemasByAppId(appDto.getId())) {
                        LOG.debug("Processing version {}", version);
                        cacheService.resetFilters(new AppProfileVersionsKey(appDto.getApplicationToken(), null, version.getVersion()));
                    }
                }
            }
            if (notification.getGroupId() != null) {
                cacheService.resetGroup(notification.getGroupId());
            }
            if (notification.getAppSeqNumber() != 0) {
                LOG.debug("Going to update application {} with seqNumber {} in thread {}", appDto.getApplicationToken(),
                        notification.getAppSeqNumber(), Thread.currentThread().getId());
                synchronized (cacheService) {
                    int currentSeqNumber = cacheService.getAppSeqNumber(appDto.getApplicationToken()).getSeqNumber();
                    if (currentSeqNumber < notification.getAppSeqNumber()) {
                        cacheService.putAppSeqNumber(appDto.getApplicationToken(), new AppSeqNumber(appDto.getTenantId(), appDto.getId(),
                                appDto.getApplicationToken(), notification.getAppSeqNumber()));
                        LOG.debug("Update application {} with seqNumber {} in thread {}", appDto.getApplicationToken(),
                                notification.getAppSeqNumber(), Thread.currentThread().getId());
                    } else {
                        LOG.debug("Update ignored. application {} already has seqNumber {}", appDto.getApplicationToken(),
                                notification.getAppSeqNumber());
                    }

                }
            }
        } else {
            LOG.warn("Application with following id is not found ", notification.getAppId());
        }
    }

    @Override
    public void onEntityRouteMessages(List<ThriftEntityRouteMessage> msgs) throws TException {
        clusterService.onEntityRouteMessages(msgs);
    }

    @Override
    public void onUnicastNotification(ThriftUnicastNotificationMessage message) throws TException {
        clusterService.onUnicastNotificationMessage(message);
    }

    @Override
    public void onServerProfileUpdate(ThriftServerProfileUpdateMessage message) throws TException {
        clusterService.onServerProfileUpdateMessage(message);
    }

    @Override
    public void onEndpointDeregistration(ThriftEndpointDeregistrationMessage message) throws TException {
        LOG.debug("Received Event about endpoint deregistration {}", message);
        byte[] address = message.getAddress().getEntityId();
        EndpointObjectHash hash = EndpointObjectHash.fromBytes(address);
        clusterService.onEndpointDeregistrationMessage(message);
        PublicKey endpointPublickKey = cacheService.getEndpointKey(hash);
        if(endpointPublickKey != null){
            cacheService.resetEndpointKey(hash, endpointPublickKey);
        }
    }
}
