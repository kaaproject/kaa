/*
 * Copyright 2014 CyberVision, Inc.
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
 * <p>Provides implementation to manage partial configuration updates.</p>
 *
 * <p>Configuration delta is a difference between current full configuration on
 * a client and up-to-date version on a server side.</p>
 *
 * <h5>Root delta receiver</h5>
 *
 * <p>To subscribe on delta updates for a root configuration record, do following:</p>
 * <pre>
 * {@code
 * public class BasicDeltaReceiver implements DeltaReceiver {
 *     \@Override
 *     public void loadDelta(ConfigurationDelta rootDelta) {
 *        System.out.println("Received delta for " + rootDelta.getHandlerId());
 *     }
 * }
 *
 * DeltaManager deltaManager = kaaClient.getDeltaManager();
 * deltaManager.registerRootReceiver(new BasicDeltaReceiver());
 * }
 * </pre>
 *
 * <h5>Specific delta receiver</h5>
 *
 * <p>Kaa SDK provides an ability to work with deltas both for a root
 * configuration record and particular configuration subtrees as well.
 * Each configuration delta has its own unique id -
 * {@link org.kaaproject.kaa.client.configuration.delta.DeltaHandlerId}.
 * Knowing it you can subscribe listener to receive delta updates specific for
 * this configuration subtree.</p>
 *
 * <p>Assume we have received delta for a root record using code from the previous
 * example. Next we want to subscribe specific receiver for a some subrecord with
 * field name <i>nestedRecordField</i>. Do following:</p>
 * <pre>
 * {@code
 * String fieldName = "nestedRecordField";
 * DeltaType nestedRecordDelta = rootDelta.getDeltaType(fieldName);
 * if (nestedRecordDelta != null && nestedRecordDelta.getNewValue() != null) {
 *     ConfigurationDelta subdelta = (ConfigurationDelta)nestedRecordDelta.getNewValue();
 *     deltaManager.subscribeForDeltaUpdates(subdelta.getHandlerId(), new BasicDeltaReceiver());
 *
 *     // Some useful stuff
 *
 *     // Remove receiver if it is no longer needed
 *     deltaManager.unsubscribeFromDeltaUpdates(subdelta.getHandlerId());
 * }
 * }
 * </pre>
 *
 * @see org.kaaproject.kaa.client.configuration.delta.manager.DeltaManager
 * @see org.kaaproject.kaa.client.configuration.delta.manager.DeltaReceiver
 * @see org.kaaproject.kaa.client.configuration.delta.ConfigurationDelta
 */
package org.kaaproject.kaa.client.configuration.delta.manager;


