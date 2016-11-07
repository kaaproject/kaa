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

package org.kaaproject.kaa.client.event.registration;

import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;

import java.util.Map;

/**
 * Callback interface for attached endpoint list change notifications.
 *
 * @author Taras Lemkin
 */
public interface ChangedAttachedEndpointListCallback {

  /**
   * Callback on attached endpoints list changed.
   *
   * @param attachedEndpoints the attached endpoints
   * @see EndpointAccessToken
   * @see EndpointKeyHash
   */
  void onAttachedEndpointListChanged(Map<EndpointAccessToken, EndpointKeyHash> attachedEndpoints);
}
