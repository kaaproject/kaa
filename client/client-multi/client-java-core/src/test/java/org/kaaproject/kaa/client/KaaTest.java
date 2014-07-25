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

import static org.mockito.Mockito.*;

import org.junit.Test;

public class KaaTest extends Kaa {

    @Test
    public void testStart() throws Exception {
        start();
        init();

        AbstractKaaClient client = (AbstractKaaClient) getClient();
        verify(client, times(1)).init();

        init();
        verify(client, times(1)).stop();

        client = (AbstractKaaClient) getClient();
        verify(client, times(1)).init();
        start();
        verify(client, times(1)).start();
    }

    @Test
    public void testStop() throws Exception {
        stop();
        init();
        AbstractKaaClient client = (AbstractKaaClient) getClient();
        stop();
        verify(client, times(1)).stop();
    }

    @Test
    public void testPause() throws Exception {
        pause();
        init();
        AbstractKaaClient client = (AbstractKaaClient) getClient();
        pause();
        verify(client, times(1)).pause();
    }

    @Test
    public void testResume() throws Exception {
        resume();
        init();
        AbstractKaaClient client = (AbstractKaaClient) getClient();
        resume();
        verify(client, times(1)).resume();
    }

    @Override
    protected AbstractKaaClient createClient() throws Exception {
        return mock(AbstractKaaClient.class);
    }

}
