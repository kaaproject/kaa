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

/**
 * Callback interface for attached to user notifications.<br>
 * <br>
 * Provide listener implementation to {@link EndpointRegistrationManager} to
 * retrieve notification when current endpoint is attached to user by another
 * endpoint.
 *
 * @author Taras Lemkin
 * @see EndpointRegistrationManager
 * @see EndpointRegistrationManager#setAttachedCallback(AttachEndpointToUserCallback)
 */
public interface AttachEndpointToUserCallback {

  /**
   * Callback on current endpoint is attached to user.
   *
   * @param userExternalId      the user external id
   * @param endpointAccessToken the endpoint access token
   */
  void onAttachedToUser(String userExternalId, String endpointAccessToken);
}
