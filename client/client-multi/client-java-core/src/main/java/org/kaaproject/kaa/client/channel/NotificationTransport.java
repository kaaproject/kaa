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

import org.kaaproject.kaa.client.notification.NotificationProcessor;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;

import java.util.List;

/**
 * {@link KaaTransport} for the Notification service.
 * Updates the Notification manager state.
 *
 * @author Yaroslav Zeygerman
 */
public interface NotificationTransport extends KaaTransport {

  /**
   * Creates a new Notification request.
   *
   * @return new Notification request.
   * @see NotificationSyncRequest
   */
  NotificationSyncRequest createNotificationRequest();

  /**
   * Creates a new empty Notification request.
   *
   * @return new empty Notification request.
   * @see NotificationSyncRequest
   */
  NotificationSyncRequest createEmptyNotificationRequest();

  /**
   * Updates the state of the Notification manager according to the given response.
   *
   * @param response the response from the server.
   * @throws Exception the exception
   * @see NotificationSyncResponse
   */
  void onNotificationResponse(NotificationSyncResponse response) throws Exception;

  /**
   * Sets the given Notification processor.
   *
   * @param processor the Notification processor which to be set.
   * @see NotificationProcessor
   */
  void setNotificationProcessor(NotificationProcessor processor);

  /**
   * <p>Notify about new subscription info.</p>
   *
   * <p>Will be called when one either subscribes or unsubscribes
   * on\from some optional topic(s).</p>
   *
   * @param commands Info about subscription actions (subscribe/unsubscribe).
   */
  void onSubscriptionChanged(List<SubscriptionCommand> commands);

}
