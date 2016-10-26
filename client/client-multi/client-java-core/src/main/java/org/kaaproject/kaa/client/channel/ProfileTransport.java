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

package org.kaaproject.kaa.client.channel;

import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.profile.ProfileManager;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncResponse;

import java.io.IOException;

/**
 * {@link KaaTransport} for the Profile service.
 * Updates the Profile manager state.
 *
 * @author Yaroslav Zeygerman
 */
public interface ProfileTransport extends KaaTransport {

  /**
   * Creates a new Profile update request.
   *
   * @return new Profile update request.
   * @throws IOException the io exception
   * @see ProfileSyncRequest
   */
  ProfileSyncRequest createProfileRequest() throws IOException;

  /**
   * Updates the state of the Profile manager from the given response.
   *
   * @param response the response from the server.
   * @throws Exception the exception
   * @see ProfileSyncResponse
   */
  void onProfileResponse(ProfileSyncResponse response) throws Exception;

  /**
   * Sets the given Profile manager.
   *
   * @param manager the Profile manager to be set.
   * @see ProfileManager
   */
  void setProfileManager(ProfileManager manager);

  /**
   * Sets the given client's properties.
   *
   * @param properties the client's properties to be set.
   * @see KaaClientProperties
   */
  void setClientProperties(KaaClientProperties properties);
}
