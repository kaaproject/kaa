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
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachRequest;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserDetachNotification;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This processor that applies the endpoint registration
 * updates received from the remote server.
 *
 * @author Taras Lemkin
 */
public interface EndpointRegistrationProcessor {

  /**
   * Retrieves current attach requests.
   *
   * @return the map of access tokens.
   * @see EndpointAccessToken
   */
  Map<Integer, EndpointAccessToken> getAttachEndpointRequests(); // TODO

  /**
   * Retrieves current detach requests.
   *
   * @return the map of endpoint key hashes.
   * @see EndpointKeyHash
   */
  Map<Integer, EndpointKeyHash> getDetachEndpointRequests();

  /**
   * Retrieves the user attach request.
   *
   * @return the user attach request.
   * @see UserAttachRequest
   */
  UserAttachRequest getUserAttachRequest();

  /**
   * Updates the manager's state.
   *
   * @param attachResponses        the list of attach responses.
   * @param detachResponses        the list of detach responses.
   * @param userResponse           the user attach response.
   * @param userAttachNotification the user attach notification
   * @param userDetachNotification the user detach notification
   * @throws IOException the io exception
   * @see EndpointAttachResponse
   * @see EndpointDetachResponse
   * @see UserAttachResponse
   */
  void onUpdate(List<EndpointAttachResponse> attachResponses,
                List<EndpointDetachResponse> detachResponses,
                UserAttachResponse userResponse,
                UserAttachNotification userAttachNotification,
                UserDetachNotification userDetachNotification) throws IOException;

}
