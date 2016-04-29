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

package org.kaaproject.kaa.server.appenders.flume.appender.client;

import org.junit.After;
import org.junit.Before;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeConfig;

public abstract class FlumeClientManagerTest<T> {

    protected T flumeNodes = null;
    protected FlumeConfig configuration = null;
    
    protected byte[] testEventBody = new byte[] { 0, 1, 2, 3, 4, 5 };

    protected FlumeSourceRunner flumeSourceRunner = null;
    protected FlumeClientManager<T> clientManager = null;

    @Before
    public final void setUp() throws Exception {
        flumeSourceRunner = FlumeSourceRunner.getInstance();
        configuration = new FlumeConfig();
    }

    @After
    public final void tearDown() throws Exception {
        if (clientManager != null) {
            clientManager.cleanUp();
        }
        if (flumeSourceRunner != null && flumeSourceRunner.isRunning()) {
            flumeSourceRunner.stopFlumeSource();
        }
    }

}
