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
import java.util.Properties;

/**
 * Entry point to the Kaa library.
 *
 * Responsible for the Kaa initialization and start/stop actions.
 *
 * @author Yaroslav Zeygerman
 *
 */
public class Kaa {
    private static DefaultKaaClient client;

    /**
     * Initialize Kaa library using given properties
     *
     * @param properties properties for the initialization
     * @throws Exception
     *
     */
    public static void init(Properties properties) throws Exception {
        if (client != null) {
            client.stop();
        }
        client = new DefaultKaaClient(properties);
        client.init();
    }

    /**
     * Starts Kaa's work flow.
     *
     */
    public static void start() throws IOException {
        if (client != null) {
            client.start();
        }
    }

    /**
     * Stops Kaa's work flow.
     *
     */
    public static void stop() {
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
    public static KaaClient getClient() {
        return client;
    }
}
