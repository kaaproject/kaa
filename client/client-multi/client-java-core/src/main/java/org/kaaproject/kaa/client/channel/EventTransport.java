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

import org.kaaproject.kaa.client.event.EventManager;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;

/**
 * {@link KaaTransport} for the Event service.
 * Updates the Event manager state.
 *
 * @author Yaroslav Zeygerman
 */
public interface EventTransport extends KaaTransport {

  /**
   * Creates the Event request.
   *
   * @param requestId new request id of the SyncRequest.
   * @return new Event request.
   * @see EventSyncRequest
   */
  EventSyncRequest createEventRequest(Integer requestId);

  /**
   * Updates the state of the Event manager according to the given response.
   *
   * @param response the response from the server.
   * @see EventSyncResponse
   */
  void onEventResponse(EventSyncResponse response);

  /**
   * Notifies event transport about response from server for specific request.
   *
   * @param requestId request id of the corresponding SyncRequest
   */
  void onSyncResposeIdReceived(Integer requestId);

  /**
   * Sets the given Event manager.
   *
   * @param manager the Event manager which is going to be set.
   * @see EventManager
   */
  void setEventManager(EventManager manager);

  /**
   * Block Event manager.
   */
  void blockEventManager();

  /**
   * Release Event manager.
   */
  void releaseEventManager();

}
