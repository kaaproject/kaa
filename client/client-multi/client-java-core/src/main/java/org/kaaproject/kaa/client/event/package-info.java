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

/**
 * Provides implementation of event subsystem
 *
 *
 * Endpoint is able to send or receive events when it is attached to user. See {@link
 * org.kaaproject.kaa.client.event.registration.EndpointRegistrationManager}<br>
 *
 * <h2>Usage</h2> For the example, we have one event class family named "ExampleClassFamily":
 * <pre>
 *  {@code
 *  [
 *      {
 *          "namespace": "org.kaa.example.events",
 *          "name": "TestEvent",
 *          "type": "record",
 *          "fields": []
 *      }
 *  ]
 *  }
 *  </pre>
 * <h3>Getting ExampleClassFamily instance</h3>
 * <pre>
 *  {@code
 *      EventFamilyFactory factory = Kaa.getClient().getEventFamilyFactory();
 *      ExampleClassFamily classFamily = factory.getExampleClassFamily();
 *  }
 *  </pre>
 * <h3>Sending an event</h3> <h4>Sending event to all available recipients</h4>
 * <pre>
 *  {@code
 *      classFamily.sendEventToAll(new TestEvent());
 *  }
 *  </pre>
 * <h4>Sending event to a concrete target</h4>
 * <pre>
 *  {@code
 *      String target = "lZjEzq4E/D5aWjXYuG1N2sKYt/U="; // Target's public key hash.
 *      classFamily.sendEvent(new TestEvent(), target);
 *  }
 *  </pre>
 *
 * <h4>Sending bulk of events</h4> Event blocks in sdk are identified by TransactionId key.<br>
 *
 * Create new empty events block:
 * <pre>
 *  {@code
 *       TransactionId blockId = Kaa.getClient().getEventFamilyFactory().startEventsBlock();
 *  }
 *  </pre>
 *
 * Events block can contain events from different event class families.<br> Add events to a block:
 * <pre>
 *  {@code
 *       TestEvent event1 = new TestEvent();
 *       TestEvent event2 = new TestEvent();
 *       // Sending event1 to a concrete target and broadcasting event2 to all endpoints.
 *       String target = "lZjEzq4E/D5aWjXYuG1N2sKYt/U="; // Target's public key hash.
 *       classFamily.addEventToBlock(blockId, event1, target);
 *       classFamily.addEventToBlock(blockId, event2);
 *  }
 *  </pre>
 *
 * If events block is completed use next call to send events:
 * <pre>
 *  {@code
 *       Kaa.getClient().getEventFamilyFactory().submitEventsBlock(blockId);
 *  }
 *  </pre>
 *
 * In order to remove events block (events will not be sent) use:
 * <pre>
 *  {@code
 *       Kaa.getClient().getEventFamilyFactory().removeEventsBlock(blockId);
 *  }
 *  </pre>
 * <h3>Receiving an event</h3> <h4>Register event listener</h4>
 * <pre>
 *  {@code
 *      classFamily.addEventFamilyListener(new ExampleClassFamily.ExampleClassFamilyListener {
 *          \@Override
 *          public void onEvent(TestEvent event, String source) {
 *              System.out.println("Received event TestEvent!");
 *          }
 *      });
 *  }
 *  </pre>
 * <h3>Searching for event recipients</h3>
 * <pre>
 *  {@code
 *      Kaa.getClient().getEventListenersResolver().findEventListeners(Arrays.asList(
 *      "org.kaa.example.events.TestEvent"),new FetchEventListeners() {
 *          \@Override
 *          public void onEventListenersReceived(List<String> eventListeners) {
 *              // process response
 *          }
 *
 *          \@Override
 *          public void onRequestFailed() {
 *          }
 *
 *      });
 *  }
 *  </pre>
 * <b>NOTE:</b> Passing multiple events fqns means that recipient MUST support receiving ALL
 * mentioned events.
 */
package org.kaaproject.kaa.client.event;
