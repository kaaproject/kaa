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
package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.ActorTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventReceiveMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.ChannelAware;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.Request;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;
import org.mockito.Mockito;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Scheduler;

public class EndpointActorMessageProcessorTest {

    @Test
    public void noChannelsForEventsTest(){
        OperationsService osMock = Mockito.mock(OperationsService.class);
        ActorContext ctxMock = Mockito.mock(ActorContext.class);
        ActorRef appActorMock = Mockito.mock(ActorRef.class);
        Mockito.when(ctxMock.parent()).thenReturn(appActorMock);

        EndpointActorMessageProcessor processor = Mockito.spy(new EndpointActorMessageProcessor(osMock, "APP_TOKEN", EndpointObjectHash.fromSHA1("key"), "actorKey"));
        EndpointEventReceiveMessage msg = Mockito.mock(EndpointEventReceiveMessage.class);

        Mockito.doNothing().when(processor).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
        processor.processEndpointEventReceiveMessage(ctxMock, msg);
        Mockito.verify(processor).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
    }

    @Test
    public void actorTimeoutTest(){
        OperationsService osMock = Mockito.mock(OperationsService.class);
        ActorContext ctxMock = Mockito.mock(ActorContext.class);
        ActorRef appActorMock = Mockito.mock(ActorRef.class);
        Mockito.when(ctxMock.parent()).thenReturn(appActorMock);

        EndpointActorMessageProcessor processor = Mockito.spy(new EndpointActorMessageProcessor(osMock, "APP_TOKEN", EndpointObjectHash.fromSHA1("key"), "actorKey"));
        ActorTimeoutMessage msg = new ActorTimeoutMessage(System.currentTimeMillis());

        Mockito.doNothing().when(processor).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
        processor.processActorTimeoutMessage(ctxMock, msg);
        Mockito.verify(processor).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
    }

    @Test
    public void actorTimeoutNegativeTest(){
        OperationsService osMock = Mockito.mock(OperationsService.class);
        ActorContext ctxMock = Mockito.mock(ActorContext.class);
        ActorRef appActorMock = Mockito.mock(ActorRef.class);
        Mockito.when(ctxMock.parent()).thenReturn(appActorMock);

        EndpointActorMessageProcessor processor = Mockito.spy(new EndpointActorMessageProcessor(osMock, "APP_TOKEN", EndpointObjectHash.fromSHA1("key"), "actorKey"));
        ActorTimeoutMessage msg = new ActorTimeoutMessage(-1);

        Mockito.doNothing().when(processor).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
        processor.processActorTimeoutMessage(ctxMock, msg);
        Mockito.verify(processor, Mockito.never()).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
    }

    @Test
    public void processDisconnectMessageTest(){
        OperationsService osMock = Mockito.mock(OperationsService.class);
        ActorContext ctxMock = Mockito.mock(ActorContext.class);
        ActorRef appActorMock = Mockito.mock(ActorRef.class);
        Mockito.when(ctxMock.parent()).thenReturn(appActorMock);

        EndpointActorMessageProcessor processor = Mockito.spy(new EndpointActorMessageProcessor(osMock, "APP_TOKEN", EndpointObjectHash.fromSHA1("key"), "actorKey"));
        ChannelAware msg = Mockito.mock(ChannelAware.class);

        Assert.assertFalse(processor.processDisconnectMessage(ctxMock, msg));
    }

    @Test
    public void processDisconnectMessageExistingChannelTest() throws GetDeltaException{
        OperationsService osMock = Mockito.mock(OperationsService.class);
        ActorContext ctxMock = Mockito.mock(ActorContext.class);
        ActorSystem systemMock = Mockito.mock(ActorSystem.class);
        Scheduler schedulerMock = Mockito.mock(Scheduler.class);

        Mockito.when(ctxMock.system()).thenReturn(systemMock);
        Mockito.when(systemMock.scheduler()).thenReturn(schedulerMock);

        SyncResponseHolder responseHolder = Mockito.mock(SyncResponseHolder.class);
        Mockito.when(osMock.sync(Mockito.any(SyncRequest.class))).thenReturn(responseHolder);

        final UUID channelId = UUID.randomUUID();
        SyncRequestMessage message = Mockito.mock(SyncRequestMessage.class);
        ChannelHandlerContext channelCtx = Mockito.mock(ChannelHandlerContext.class);
        Mockito.when(message.getChannelType()).thenReturn(ChannelType.TCP);
        Mockito.when(message.getChannelUuid()).thenReturn(channelId);
        Mockito.when(message.getChannelContext()).thenReturn(channelCtx);
        Mockito.when(message.getSession()).thenReturn(new NettySessionInfo(channelId, channelCtx, ChannelType.TCP, null, EndpointObjectHash.fromSHA1("key"), "APP_TOKEN", 1000, true));
        Mockito.when(message.getCommand()).thenReturn(Mockito.mock(Request.class));
        Mockito.when(message.getOriginator()).thenReturn(Mockito.mock(ActorRef.class));

        EndpointActorMessageProcessor processor = Mockito.spy(new EndpointActorMessageProcessor(osMock, "APP_TOKEN", EndpointObjectHash.fromSHA1("key"), "actorKey"));

        Mockito.doNothing().when(processor).tellActor(Mockito.any(ActorContext.class), Mockito.any(ActorRef.class), Mockito.any(Object.class));

        processor.processEndpointSync(ctxMock, message);

        ChannelAware msg = new ChannelAware() {

            @Override
            public UUID getChannelUuid() {
                // TODO Auto-generated method stub
                return channelId;
            }

            @Override
            public ChannelType getChannelType() {
                return ChannelType.TCP;
            }

            @Override
            public ChannelHandlerContext getChannelContext() {
                return null;
            }
        };

        Assert.assertTrue(processor.processDisconnectMessage(ctxMock, msg));
    }

}
