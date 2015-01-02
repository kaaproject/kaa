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

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.UUID;

import org.kaaproject.kaa.server.common.server.http.AbstractCommand;
import org.kaaproject.kaa.server.common.server.http.DefaultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Handler Class.
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class TestHandler extends DefaultHandler {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(TestHandler.class);

    /** The akka service. */
    private final TestAkkaService akkaService;

    /** The uuid. */
    private final UUID uuid;

    public TestHandler(UUID uuid, TestAkkaService akkaService, EventExecutorGroup executorGroup) {
        super(executorGroup);
        this.akkaService = akkaService;
        this.uuid = uuid;
        LOG.trace("Test handler for session "+uuid.toString()+" created....");
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.DefaultHandler#channelRead0(io.netty.channel.ChannelHandlerContext, org.kaaproject.kaa.server.common.http.server.CommandProcessor)
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx,
            final AbstractCommand msg) throws Exception {
//        TODO: fix this
//        AbstractHttpSyncCommand<SpecificRecordBase, SpecificRecordBase> command = (AbstractHttpSyncCommand<SpecificRecordBase, SpecificRecordBase>) msg;
//        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage(uuidToStr(uuid), ctx, command, ChannelType.HTTP);
//        LOG.trace("Forwarding {} to akka", message);
//        akkaService.addCommand(message);
    }

    /**
     * Uuid to str.
     *
     * @param uuid the uuid
     * @return the string
     */
    private static String uuidToStr(UUID uuid) {
        return uuid.toString().replace('-', '_');
    };
}
