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

package org.kaaproject.kaa.server.transport;

/**
 * Responsible for lookup, initialization and life-cycle management of the
 * available transport implementations
 * 
 * @author Andrew Shvayka
 *
 */
public interface TransportService {

    /**
     * Look up the available transport implementations and initialize them
     */
    void lookupAndInit();

    /**
     * Start all the initialized transports.
     */
    void start();

    /**
     * Stop all the initialized transports.
     */
    void stop();

    /**
     * Adds a listener for the {@link Transport} state updates
     */
    boolean addListener(TransportUpdateListener listener);

    /**
     * Removes the {@link Transport} state updates listener
     */
    boolean removeListener(TransportUpdateListener listener);

}
