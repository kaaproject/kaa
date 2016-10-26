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

import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.channel.ProfileTransport;
import org.kaaproject.kaa.client.profile.ProfileManager;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DefaultProfileTransport extends AbstractKaaTransport implements
    ProfileTransport {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultProfileTransport.class);

  private ProfileManager manager;
  private KaaClientProperties properties;

  private boolean isProfileOutDated(EndpointObjectHash currentProfileHash) {
    EndpointObjectHash currentHash = clientState.getProfileHash();
    return currentHash == null || !currentHash.equals(currentProfileHash);
  }

  @Override
  public void sync() {
    syncAll(TransportType.PROFILE);
  }

  @Override
  public ProfileSyncRequest createProfileRequest() throws IOException {
    if (clientState != null && manager != null && properties != null) {
      byte[] serializedProfile = manager.getSerializedProfile();
      EndpointObjectHash currentProfileHash = EndpointObjectHash.fromSha1(serializedProfile);
      if (isProfileOutDated(currentProfileHash)
          || !clientState.isRegistered()
          || clientState.isNeedProfileResync()) {
        clientState.setProfileHash(currentProfileHash);
        ProfileSyncRequest request = new ProfileSyncRequest();
        request.setEndpointAccessToken(clientState.getEndpointAccessToken());
        if (!clientState.isRegistered()) {
          request.setEndpointPublicKey(ByteBuffer.wrap(clientState.getPublicKey().getEncoded()));
        }
        request.setProfileBody(ByteBuffer.wrap(serializedProfile));
        return request;
      } else {
        LOG.info("Profile is up to date");
      }
    } else {
      LOG.error("Failed to create ProfileSyncRequest clientState {}, manager {}, properties {}",
              clientState, manager, properties);
    }
    return null;
  }

  @Override
  public void onProfileResponse(ProfileSyncResponse response) throws Exception {
    if (response.getResponseStatus() == SyncResponseStatus.RESYNC) {
      clientState.setIfNeedProfileResync(true);
      sync();
    } else if (clientState != null && !clientState.isRegistered()) {
      clientState.setRegistered(true);
    }
    LOG.info("Processed profile response");
  }

  @Override
  public void setProfileManager(ProfileManager manager) {
    this.manager = manager;
  }

  @Override
  public void setClientProperties(KaaClientProperties properties) {
    this.properties = properties;
  }

  @Override
  protected TransportType getTransportType() {
    return TransportType.PROFILE;
  }

}
