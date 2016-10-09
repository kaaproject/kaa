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

import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;

/**
 * Callback interface for detached endpoint notifications.<br>
 * <br>
 * Use this interface to receive result of next operations:
 * Detach endpoint from user by {@link org.kaaproject.kaa.client.event.EndpointKeyHash}
 * <br>
 * Once result from Operations server is received, listener is notified with
 * string representation of operation name, result of the operation {@link SyncResponseResultType}
 * and additional data if available.
 *
 * @see EndpointRegistrationManager
 */
public interface OnDetachEndpointOperationCallback {

  /**
   * Callback on endpoint detach response<br>
   *
   * @param result The enum value [{@code SUCCESS, FAILURE}]
   * @see org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType
   */
  void onDetach(SyncResponseResultType result);
}
