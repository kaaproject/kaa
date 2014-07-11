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

package org.kaaproject.kaa.client.configuration.delta.manager;

import org.kaaproject.kaa.client.configuration.delta.DeltaHandlerId;

/**
 * Interface for the delta manager
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface DeltaManager {

    /**
     * Registers root receiver to receive first and full resync deltas
     *
     * @param receiver the root receiver object
     * @see DeltaReceiver
     */
    void registerRootReceiver(DeltaReceiver receiver);

    /**
     * Subscribes receiver for delta updates by the given handler id
     *
     * @param handlerId id of the delta handler
     * @param receiver the object which is going to receive updates
     * 
     * @see DeltaHandlerId
     * @see DeltaReceiver
     */
    void subscribeForDeltaUpdates(DeltaHandlerId handlerId, DeltaReceiver receiver);

    /**
     * Unsubscribes receiver from delta updates
     *
     * @param handlerId id of the handler which is going to be unsubscribed
     * @see DeltaHandlerId
     */
    void unsubscribeFromDeltaUpdates(DeltaHandlerId handlerId);

}
