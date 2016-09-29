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

import java.util.Set;

/**
 * Interface for Event Family.
 * Each EventFamily should be accessed through {@link EventFamilyFactory}
 *
 * @author Taras Lemkin
 */
public interface EventFamily {

  /**
   * Returns set of supported incoming events in event family.
   *
   * @return set of supported events presented as set event fully qualified names
   */
  Set<String> getSupportedEventFqns();

  /**
   * Generic handler of event received from server.
   *
   * @param eventFqn Fully qualified name of an event
   * @param data     Event data
   * @param source   Event source
   */
  void onGenericEvent(String eventFqn, byte[] data, String source);
}
