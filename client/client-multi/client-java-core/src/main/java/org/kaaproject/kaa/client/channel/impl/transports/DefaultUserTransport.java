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

package org.kaaproject.kaa.client.channel.impl.transports;

import org.kaaproject.kaa.client.channel.UserTransport;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.EndpointRegistrationProcessor;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultUserTransport extends AbstractKaaTransport implements
    UserTransport {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultUserTransport.class);

  private EndpointRegistrationProcessor processor;
  private Map<EndpointAccessToken, EndpointKeyHash> attachedEndpoints = new HashMap<>();

  @Override
  public UserSyncRequest createUserRequest() {
    if (processor != null) {
      UserSyncRequest request = new UserSyncRequest();

      Map<Integer, EndpointAccessToken> attachEndpointRequests =
              processor.getAttachEndpointRequests();
      List<EndpointAttachRequest> attachEpRequestList = new LinkedList<EndpointAttachRequest>();
      for (Map.Entry<Integer, EndpointAccessToken> attachEpRequest : attachEndpointRequests
              .entrySet()) {
        attachEpRequestList.add(new EndpointAttachRequest(attachEpRequest.getKey(),
                attachEpRequest.getValue().getToken()));
      }

      Map<Integer, EndpointKeyHash> detachEndpointRequests = processor.getDetachEndpointRequests();
      List<EndpointDetachRequest> detachEpRequestList = new LinkedList<>();
      for (Map.Entry<Integer, EndpointKeyHash> detachEpRequest : detachEndpointRequests
              .entrySet()) {
        detachEpRequestList.add(new EndpointDetachRequest(detachEpRequest.getKey(),
                detachEpRequest.getValue().getKeyHash()));
      }

      request.setEndpointAttachRequests(attachEpRequestList);
      request.setEndpointDetachRequests(detachEpRequestList);
      request.setUserAttachRequest(processor.getUserAttachRequest());
      return request;
    }
    return null;
  }

  @Override
  public void onUserResponse(UserSyncResponse response) throws IOException {
    if (processor != null) {
      boolean hasChanges = false;
      if (clientState != null) {
        attachedEndpoints = clientState.getAttachedEndpointsList();
      }
      Map<Integer, EndpointAccessToken> attachEndpointRequests =
              processor.getAttachEndpointRequests();
      if (response.getEndpointAttachResponses() != null
              && !response.getEndpointAttachResponses().isEmpty()) {
        for (EndpointAttachResponse attached : response.getEndpointAttachResponses()) {
          EndpointAccessToken attachedToken = attachEndpointRequests.remove(
                  attached.getRequestId());
          if (attached.getResult() == SyncResponseResultType.SUCCESS) {
            if (attachedToken != null) {
              LOG.info("Token {}", attachedToken);
              attachedEndpoints.put(attachedToken,
                      new EndpointKeyHash(attached.getEndpointKeyHash()));
              hasChanges = true;
            } else {
              LOG.warn("Endpoint {} is already attached!", attached.getEndpointKeyHash());
            }
          } else {
            LOG.error("Failed to attach endpoint {}. Attach endpoint request id: {}",
                    attached.getEndpointKeyHash(), attached.getRequestId());
          }
        }
      }
      Map<Integer, EndpointKeyHash> detachEndpointRequests = processor.getDetachEndpointRequests();
      if (response.getEndpointDetachResponses() != null
              && !response.getEndpointDetachResponses().isEmpty()) {
        for (EndpointDetachResponse detached : response.getEndpointDetachResponses()) {
          EndpointKeyHash detachedEndpointKeyHash = detachEndpointRequests.remove(
                  detached.getRequestId());
          if (detached.getResult() == SyncResponseResultType.SUCCESS) {
            if (detachedEndpointKeyHash != null) {
              for (Map.Entry<EndpointAccessToken, EndpointKeyHash> entry : attachedEndpoints
                      .entrySet()) {
                if (detachedEndpointKeyHash.equals(entry.getValue())) {
                  EndpointKeyHash removed = attachedEndpoints.remove(entry.getKey());
                  if (!hasChanges) {
                    hasChanges = (removed != null);
                  }
                  break;
                }
              }
            }
          } else {
            LOG.error("Failed to detach endpoint. Detach endpoint request id: {}",
                    detached.getRequestId());
          }
        }
      }

      if (hasChanges && clientState != null) {
        clientState.setAttachedEndpointsList(attachedEndpoints);
      }
      processor.onUpdate(response.getEndpointAttachResponses(),
              response.getEndpointDetachResponses(),
              response.getUserAttachResponse(),
              response.getUserAttachNotification(),
              response.getUserDetachNotification());
      LOG.info("Processed user response");
    }
  }

  @Override
  public void setEndpointRegistrationProcessor(EndpointRegistrationProcessor manager) {
    this.processor = manager;
  }

  @Override
  protected TransportType getTransportType() {
    return TransportType.USER;
  }

}
