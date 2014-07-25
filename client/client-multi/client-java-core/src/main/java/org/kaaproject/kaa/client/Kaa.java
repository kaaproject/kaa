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
 * <br><br>
 * Responsible for the Kaa initialization and start/stop actions.
 * Contains abstract method {@link Kaa#createClient()} for {@link AbstractKaaClient} concrete object creation.
 * All manipulations using Kaa library should be accessed through {@link KaaClient} interface.<br>
 * <br>
 * <b>WARNING:</b> Calling {@link Kaa#getClient()} without previously initialized Kaa library
 * ({@link Kaa#init()}) will return null and will cause NullPointerException
 * when attempting to use Kaa client functionality.
 * <br><br>
 * Available implementations can be found in Maven projects
 * <i>client-java-android</i> and <i>client-java-desktop</i>.
 * <br><br>
 * Start Kaa:
 * <pre>
 * {@code
 * Kaa kaa = new SomeKaaImpl();
 * kaa.start();}
 * </pre>
 * Stop Kaa:
 * <pre>
 * {@code
 * kaa.stop();
 * }
 * </pre>
 * Accessing KaaClient:
 * <pre>
 * {@code
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
     * Initialize Kaa library
     * <br><br>
     * Each call forces {@link KaaClient} to be reinitialized. All current processes
     * will be stopped and new client will be created using {@link Kaa#createClient()}
     * and {@link Kaa#start()} should be called as client will not start automatically.
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
     * Starts Kaa's work flow.
     * <br><br>
     * Should be called after each call to {@link Kaa#init()}.
     *
     * @see AbstractKaaClient#start()
     */
    public void start() throws IOException, TransportException {
        if (client != null) {
            client.start();
        }
    }

    /**
     * Stops Kaa's work flow.
     *
     * @see AbstractKaaClient#stop()
     */
    public void stop() {
        if (client != null) {
            client.stop();
        }
    }
    
    /**
     * Pauses Kaa's work flow.
     */
    public void pause() {
        if (client != null) {
            client.pause();
        }
    }
    
    /**
     * Resumes Kaa's work flow.
     */
    public void resume() {
        if (client != null) {
            client.resume();
        }
    }

    /**
     * Retrieves the Kaa client.
     * <br><br>
     * Use this to access Kaa library functionality.
     *
     * @return Kaa client.
     * @see KaaClient
     */
    public KaaClient getClient() {
        return client;
    }

}
