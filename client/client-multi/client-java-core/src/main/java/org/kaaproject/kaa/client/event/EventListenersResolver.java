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
 * Public access interface for events listener resolution request.<br>
 * <br>
 * Use this module to find endpoints which are able to receive events by list of
 * events' fully-qualified names.<br>
 * <b>NOTE:</b> Operations server will respond with list of endpoints which
 * can receive <b>ALL</b> listed event types (FQNs).
 * For example:
 * <pre>
 * {@code
 * List<String> fqnsToBeSupported = new ArrayList<String>();
 * fqnsToBeSupported.add("org.kaaproject.test.FooEvent");
 * fqnsToBeSupported.add("org.kaaproject.test.BarEvent");
 *
 * EventListenersResolver resolver = kaaClient.getEventListenerResolver();
 * resolver.findEventListeners(fqnsToBeSupported, new FetchEventListeners() {
 *     public void onEventListenersReceived(List<String> eventListeners) {
 *         System.out.println("Found " + eventListeners.size() + " event targets");
 *     }
 *     public void onRequestFailed() {
 *         System.out.println("Request failed for some reason.");
 *     }
 * });
 * }
 * </pre>
 * This code will receive list of endpoints which support receiving event types
 * {@code "org.kaaproject.test.FooEvent"} <b>AND</b> {@code "org.kaaproject.test.BarEvent"}.
 *
 * @author Taras Lemkin
 * @see FindEventListenersCallback
 */
public interface EventListenersResolver {

  /**
   * Submits an event listeners resolution request
   *
   * @param eventFqns List of event class FQNs which have to be supported by endpoint.
   * @param listener  Result listener {@link FindEventListenersCallback}}
   * @return Request ID of submitted request
   */
  int findEventListeners(List<String> eventFqns, FindEventListenersCallback listener);
}
