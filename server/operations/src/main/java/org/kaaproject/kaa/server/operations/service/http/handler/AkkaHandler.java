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

package org.kaaproject.kaa.server.operations.service.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.UUID;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.server.common.http.server.CommandProcessor;
import org.kaaproject.kaa.server.common.http.server.DefaultHandler;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyEncodedRequestMessage;
import org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AkkaHandler.
 */
public class AkkaHandler extends DefaultHandler {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AkkaHandler.class);

    /** The akka service. */
    private AkkaService akkaService;

    /** The uuid. */
    private UUID uuid;

    /**
     * Instantiates a new akka handler.
     * 
     * @param UUID
     *            - session uuid
     * @param akkaService
     *            the akka service
     * @param executorGroup
     *            the executor group
     */
    public AkkaHandler(UUID uuid, AkkaService akkaService, EventExecutorGroup executorGroup) {
        super(executorGroup);
        this.akkaService = akkaService;
        this.uuid = uuid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.http.server.DefaultHandler#channelRead0
     * (io.netty.channel.ChannelHandlerContext,
     * org.kaaproject.kaa.server.common.http.server.CommandProcessor)
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final CommandProcessor msg) throws Exception {
        AbstractOperationsCommand<SpecificRecordBase, SpecificRecordBase> command = (AbstractOperationsCommand<SpecificRecordBase, SpecificRecordBase>) msg;
        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage(uuidToStr(uuid), ctx, command);
        LOG.trace("Forwarding {} to akka", message);
        akkaService.process(message);
    }

    /**
     * Uuid to str.
     * 
     * @param uuid
     *            the uuid
     * @return the string
     */
    private static String uuidToStr(UUID uuid) {
        return uuid.toString().replace('-', '_');
    }
}
