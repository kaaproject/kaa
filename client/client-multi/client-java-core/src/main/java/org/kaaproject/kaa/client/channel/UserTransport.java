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

import org.kaaproject.kaa.client.event.registration.EndpointRegistrationProcessor;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;

/**
 * {@link KaaTransport} for the Endpoint service.
 * Updates the Endpoint manager state.
 *
 * @author Yaroslav Zeygerman
 */
public interface UserTransport extends KaaTransport {

  /**
   * Creates new User update request.
   *
   * @return new User update request.
   * @see UserSyncRequest
   */
  UserSyncRequest createUserRequest();

  /**
   * Updates the state of the Endpoint manager according to the given response.
   *
   * @param response the response from the server.
   * @throws Exception the exception
   * @see UserSyncResponse
   */
  void onUserResponse(UserSyncResponse response) throws Exception;

  /**
   * Sets the given Endpoint processor.
   *
   * @param processor the Endpoint processor to be set.
   * @see EndpointRegistrationProcessor
   */
  void setEndpointRegistrationProcessor(EndpointRegistrationProcessor processor);

}
