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

package org.kaaproject.kaa.client.event;

import java.util.List;

/**
 * Listener interface for retrieving endpoints list
 * which supports requested event class FQNs.
 *
 * @author Taras Lemkin
 * @see EventListenersResolver
 */
public interface FindEventListenersCallback {

  /**
   * Called when resolve was successful.
   *
   * @param eventListeners List of endpoints
   */
  void onEventListenersReceived(List<String> eventListeners);

  // TODO: add some kind of error reason

  /**
   * Called when some error occured during resolving endpoints
   * via event class FQNs.
   */
  void onRequestFailed();
}
