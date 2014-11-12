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
 * <p>Entry point to the Kaa library.</p>
 *
 * <p>Responsible for the Kaa initialization and start/stop actions.
 * Contains abstract method {@link Kaa#createClient()} for {@link AbstractKaaClient}
 * concrete object creation. All manipulations using Kaa library should be
 * accessed through {@link KaaClient} interface.</p>
 *
 * <p><b>WARNING:</b> Calling {@link Kaa#getClient()} without previously
 * initialized Kaa library ({@link Kaa#init()}) will return null and will
 * cause NullPointerException when attempting to use Kaa client functionality.</p>
 *
 * <p>Available implementations can be found in Maven projects
 * <i>client-java-android</i> and <i>client-java-desktop</i></p>.
 *
 * <h5>Example</h5>
 * <pre>
 * {@code
 * // Start Kaa
 * Kaa kaa = new SomeKaaImpl();
 * kaa.start();}
 *
 * // Stop Kaa:
 * kaa.stop();
 *
 * // Accessing KaaClient:
 * KaaClient kaaClient = kaa.getClient();
 * ...
 * }
 * </pre>
 *
 * @author Yaroslav Zeygerman
 *
 * @see KaaClient
 * @see AbstractKaaClient
 */
public abstract class Kaa {

    private static AbstractKaaClient client;

    public Kaa() {
    }

    /**
     * <p>Initialize Kaa library.</p>
     *
     * <p>Each call forces {@link KaaClient} to be reinitialized. All current
     * processes will be stopped and new client will be created using
     * {@link Kaa#createClient()} and {@link Kaa#start()} should be called as
     * client will not start automatically.</p>
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

    /**
     * Creates platform-specific {@link AbstractKaaClient} object.
     *
     * @return Instance of {@link AbstractKaaClient}
     * @throws Exception
     */
    protected abstract AbstractKaaClient createClient() throws Exception;

    /**
     * <p>Starts Kaa's workflow.</p>
     *
     * <p>Should be called after each call to {@link Kaa#init()}.</p>
     *
     * @see AbstractKaaClient#start()
     */
    public void start() throws IOException, TransportException {
        if (client != null) {
            client.start();
        }
    }

    /**
     * Stops Kaa's workflow.
     *
     * @see AbstractKaaClient#stop()
     */
    public void stop() {
        if (client != null) {
            client.stop();
        }
    }

    /**
     * Pauses Kaa's workflow.
     */
    public void pause() {
        if (client != null) {
            client.pause();
        }
    }

    /**
     * Resumes Kaa's workflow.
     */
    public void resume() {
        if (client != null) {
            client.resume();
        }
    }

    /**
     * <p>Retrieves the Kaa client.</p>
     *
     * <p>Use this to access Kaa library functionality.</p>
     *
     * @return Kaa client.
     * @see KaaClient
     *
     */
    public KaaClient getClient() {
        return client;
    }

}
