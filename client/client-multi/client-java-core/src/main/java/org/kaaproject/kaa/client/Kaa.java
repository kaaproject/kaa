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

package org.kaaproject.kaa.client;

import java.io.IOException;

import org.kaaproject.kaa.client.transport.TransportException;

/**
 * Entry point to the Kaa library.
 *
 * Responsible for the Kaa initialization and start/stop actions.
 *
 * @author Yaroslav Zeygerman
 *
 */
public abstract class Kaa {

    private static AbstractKaaClient client;

    public Kaa() {
    }

    /**
     * Initialize Kaa library
     *
     * @throws Exception
     *
     */
    protected void init() throws Exception { //NOSONAR
        if (client != null) {
            client.stop();
        }
        client = createClient();
        client.init();
    }

    protected abstract AbstractKaaClient createClient() throws Exception;

    /**
     * Starts Kaa's work flow.
     *
     */
    public void start() throws IOException, TransportException {
        if (client != null) {
            client.start();
        }
    }

    /**
     * Stops Kaa's work flow.
     *
     */
    public void stop() {
        if (client != null) {
            client.stop();
        }
    }

    /**
     * Retrieves the Kaa client.
     *
     * @return Kaa client.
     *
     */
    public KaaClient getClient() {
        return client;
    }

}
