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

package org.kaaproject.kaa.client.channel.impl;

import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.ConfigurationTransport;
import org.kaaproject.kaa.client.channel.EventTransport;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.client.channel.MetaDataTransport;
import org.kaaproject.kaa.client.channel.NotificationTransport;
import org.kaaproject.kaa.client.channel.ProfileTransport;
import org.kaaproject.kaa.client.channel.RedirectionTransport;
import org.kaaproject.kaa.client.channel.UserTransport;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultOperationDataProcessor implements KaaDataMultiplexer, KaaDataDemultiplexer {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultOperationDataProcessor.class);
  private final AtomicInteger requestsCounter = new AtomicInteger(0);
  private final AvroByteArrayConverter<SyncRequest> requestConverter =
          new AvroByteArrayConverter<>(SyncRequest.class);
  private final AvroByteArrayConverter<SyncResponse> responseConverter =
          new AvroByteArrayConverter<>(SyncResponse.class);
  private final KaaClientState state;
  private MetaDataTransport metaDataTransport;
  private ConfigurationTransport configurationTransport;
  private EventTransport eventTransport;
  private NotificationTransport notificationTransport;
  private ProfileTransport profileTransport;
  private UserTransport userTransport;
  private RedirectionTransport redirectionTransport;
  private LogTransport logTransport;

  public DefaultOperationDataProcessor(KaaClientState state) {
    super();
    this.state = state;
  }

  public synchronized void setRedirectionTransport(RedirectionTransport redirectionTransport) {
    this.redirectionTransport = redirectionTransport;
  }

  public synchronized void setMetaDataTransport(MetaDataTransport metaDataTransport) {
    this.metaDataTransport = metaDataTransport;
  }

  public synchronized void setConfigurationTransport(
          ConfigurationTransport configurationTransport) {
    this.configurationTransport = configurationTransport;
  }

  public synchronized void setEventTransport(EventTransport eventTransport) {
    this.eventTransport = eventTransport;
  }

  public synchronized void setNotificationTransport(NotificationTransport notificationTransport) {
    this.notificationTransport = notificationTransport;
  }

  public synchronized void setProfileTransport(ProfileTransport profileTransport) {
    this.profileTransport = profileTransport;
  }

  public synchronized void setUserTransport(UserTransport userTransport) {
    this.userTransport = userTransport;
  }

  public synchronized void setLogTransport(LogTransport logTransport) {
    this.logTransport = logTransport;
  }

  @Override
  public synchronized void processResponse(byte[] response) throws Exception {
    if (response != null) {
      try {
        SyncResponse syncResponse = responseConverter.fromByteArray(response);

        LOG.info("Received Sync response: {}", syncResponse);
        if (syncResponse.getConfigurationSyncResponse() != null && configurationTransport != null) {
          configurationTransport.onConfigurationResponse(
                  syncResponse.getConfigurationSyncResponse());
        }
        if (eventTransport != null) {
          eventTransport.onSyncResposeIdReceived(syncResponse.getRequestId());
          if (syncResponse.getEventSyncResponse() != null) {
            eventTransport.onEventResponse(syncResponse.getEventSyncResponse());
          }
        }
        if (syncResponse.getNotificationSyncResponse() != null && notificationTransport != null) {
          notificationTransport.onNotificationResponse(syncResponse.getNotificationSyncResponse());
        }
        if (syncResponse.getUserSyncResponse() != null && userTransport != null) {
          userTransport.onUserResponse(syncResponse.getUserSyncResponse());
        }
        if (syncResponse.getRedirectSyncResponse() != null && redirectionTransport != null) {
          redirectionTransport.onRedirectionResponse(syncResponse.getRedirectSyncResponse());
        }
        if (syncResponse.getProfileSyncResponse() != null && profileTransport != null) {
          profileTransport.onProfileResponse(syncResponse.getProfileSyncResponse());
        }
        if (syncResponse.getLogSyncResponse() != null && logTransport != null) {
          logTransport.onLogResponse(syncResponse.getLogSyncResponse());
        }

        boolean needProfileResync = syncResponse.getStatus() == SyncResponseResultType
                .PROFILE_RESYNC;
        state.setIfNeedProfileResync(needProfileResync);
        if (needProfileResync) {
          LOG.info("Going to resync profile...");
          profileTransport.sync();
        }
      } finally {
        state.persist();
      }
    }
  }

  @Override
  public synchronized byte[] compileRequest(Map<TransportType, ChannelDirection> types)
          throws Exception {
    if (types != null) {
      SyncRequest request = new SyncRequest();
      request.setRequestId(requestsCounter.incrementAndGet());

      if (metaDataTransport != null) {
        request.setSyncRequestMetaData(metaDataTransport.createMetaDataRequest());
      }
      for (Map.Entry<TransportType, ChannelDirection> type : types.entrySet()) {
        boolean isDownDirection = type.getValue().equals(ChannelDirection.DOWN);
        switch (type.getKey()) {
          case CONFIGURATION:
            if (configurationTransport != null) {
              request.setConfigurationSyncRequest(
                      configurationTransport.createConfigurationRequest());
            }
            break;
          case EVENT:
            if (isDownDirection) {
              request.setEventSyncRequest(new EventSyncRequest());
            } else if (eventTransport != null) {
              request.setEventSyncRequest(
                      eventTransport.createEventRequest(request.getRequestId()));
            }
            break;
          case NOTIFICATION:
            if (notificationTransport != null) {
              if (isDownDirection) {
                request.setNotificationSyncRequest(
                        notificationTransport.createEmptyNotificationRequest());
              } else {
                request.setNotificationSyncRequest(
                        notificationTransport.createNotificationRequest());
              }
            }
            break;
          case PROFILE:
            if (!isDownDirection && profileTransport != null) {
              request.setProfileSyncRequest(profileTransport.createProfileRequest());
            }
            break;
          case USER:
            if (isDownDirection) {
              request.setUserSyncRequest(new UserSyncRequest());
            } else if (userTransport != null) {
              request.setUserSyncRequest(userTransport.createUserRequest());
            }
            break;
          case LOGGING:
            if (isDownDirection) {
              request.setLogSyncRequest(new LogSyncRequest());
            } else if (logTransport != null) {
              request.setLogSyncRequest(logTransport.createLogRequest());
            }
            break;
          default:
            LOG.error("Invalid transport type {}", type.getKey());
            return null; //NOSONAR
        }
      }
      LOG.info("Created Sync request: {}", request);
      return requestConverter.toByteArray(request);
    }
    return null; //NOSONAR
  }

  @Override
  public void preProcess() {
    if (eventTransport != null) {
      eventTransport.blockEventManager();
    }

  }

  @Override
  public void postProcess() {
    if (eventTransport != null) {
      eventTransport.releaseEventManager();
    }
  }
}
