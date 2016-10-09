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

package org.kaaproject.kaa.client.notification;

/**
 * <p>Wrapper class for a topic subscription stuff.</p>
 *
 * <p><b>This class is deprecated</b>. Use instead:
 * {@link NotificationManager#subscribeToTopic(Long, boolean)},
 * {@link NotificationManager#addNotificationListener(Long, NotificationListener)},
 * {@link NotificationManager#subscribeToTopics(java.util.List, boolean)},
 * {@link NotificationManager#addTopicListListener(NotificationTopicListListener)}.</p>
 */
@Deprecated
public class NotificationListenerInfo {
  private final NotificationListener listener;
  private final Action action;

  public NotificationListenerInfo(NotificationListener listener, Action action) {
    this.listener = listener;
    this.action = action;
  }

  public NotificationListener getListener() {
    return listener;
  }

  public Action getAction() {
    return action;
  }

  @Deprecated
  public enum Action {
    ADD,
    REMOVE
  }
}
