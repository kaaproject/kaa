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

package org.kaaproject.kaa.server.operations.service.http;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.util.UUID;

import org.kaaproject.kaa.server.common.http.server.CommandProcessor;
import org.kaaproject.kaa.server.operations.service.http.OperationsServerInitializer;
import org.kaaproject.kaa.server.operations.service.http.OperationsServerConfig;

/**
 * Test Initializator Class.
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class TestEndPointServerInitializer extends OperationsServerInitializer {
    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.DefaultServerInitializer#getMainHandler(java.util.UUID)
     */
    @Override
    protected SimpleChannelInboundHandler<CommandProcessor> getMainHandler(UUID uuid){
        return new TestHandler(uuid, (TestAkkaService) ((OperationsServerConfig) getConf()).getAkkaService(), new DefaultEventExecutorGroup(1));
    }
}
