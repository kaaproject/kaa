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
package org.kaaproject.kaa.server.operations.service.akka.messages.io.request;

import java.util.UUID;

import io.netty.channel.ChannelHandlerContext;

import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.ChannelAware;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;

import akka.actor.ActorRef;

public class NettyTcpDisconnectMessage extends EndpointAwareMessage implements SessionAware, ChannelAware{

    private final NettySessionInfo session;

    public NettyTcpDisconnectMessage(NettySessionInfo session) {
        super(session.getApplicationToken(), session.getKey(), ActorRef.noSender());
        this.session = session;
    }

    @Override
    public UUID getChannelUuid() {
        return session.getUuid();
    }

    @Override
    public NettySessionInfo getSessionInfo() {
        return session;
    }

    @Override
    public ChannelType getChannelType() {
        return session.getChannelType();
    }

    @Override
    public ChannelHandlerContext getChannelContext() {
        return session.getCtx();
    }

}
