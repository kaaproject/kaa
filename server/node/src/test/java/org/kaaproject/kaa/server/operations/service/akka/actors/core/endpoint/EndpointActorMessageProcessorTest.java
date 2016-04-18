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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.local.LocalEndpointActorMessageProcessor;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.ActorTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventReceiveMessage;
import org.kaaproject.kaa.server.transport.channel.ChannelAware;
import org.mockito.Mockito;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class EndpointActorMessageProcessorTest {

    @Test
    public void noChannelsForEventsTest() {
        OperationsService osMock = Mockito.mock(OperationsService.class);
        ActorContext ctxMock = Mockito.mock(ActorContext.class);
        ActorRef appActorMock = Mockito.mock(ActorRef.class);
        Mockito.when(ctxMock.parent()).thenReturn(appActorMock);
        
        AkkaContext context = Mockito.mock(AkkaContext.class);
        Mockito.when(context.getOperationsService()).thenReturn(osMock);

        LocalEndpointActorMessageProcessor processor = Mockito.spy(new LocalEndpointActorMessageProcessor(context, "APP_TOKEN", EndpointObjectHash
                .fromSHA1("key"), "actorKey"));
        EndpointEventReceiveMessage msg = Mockito.mock(EndpointEventReceiveMessage.class);

        Mockito.doNothing().when(processor).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
        processor.processEndpointEventReceiveMessage(ctxMock, msg);
        Mockito.verify(processor).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
    }

    @Test
    public void actorTimeoutTest() {
        OperationsService osMock = Mockito.mock(OperationsService.class);
        ActorContext ctxMock = Mockito.mock(ActorContext.class);
        ActorRef appActorMock = Mockito.mock(ActorRef.class);
        Mockito.when(ctxMock.parent()).thenReturn(appActorMock);

        AkkaContext context = Mockito.mock(AkkaContext.class);
        Mockito.when(context.getOperationsService()).thenReturn(osMock);
        
        LocalEndpointActorMessageProcessor processor = Mockito.spy(new LocalEndpointActorMessageProcessor(context, "APP_TOKEN", EndpointObjectHash
                .fromSHA1("key"), "actorKey"));
        ActorTimeoutMessage msg = new ActorTimeoutMessage(System.currentTimeMillis());

        Mockito.doNothing().when(processor).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
        processor.processActorTimeoutMessage(ctxMock, msg);
        Mockito.verify(processor).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
    }

    @Test
    public void actorTimeoutNegativeTest() {
        OperationsService osMock = Mockito.mock(OperationsService.class);
        ActorContext ctxMock = Mockito.mock(ActorContext.class);
        ActorRef appActorMock = Mockito.mock(ActorRef.class);
        Mockito.when(ctxMock.parent()).thenReturn(appActorMock);

        AkkaContext context = Mockito.mock(AkkaContext.class);
        Mockito.when(context.getOperationsService()).thenReturn(osMock);
        
        LocalEndpointActorMessageProcessor processor = Mockito.spy(new LocalEndpointActorMessageProcessor(context, "APP_TOKEN", EndpointObjectHash
                .fromSHA1("key"), "actorKey"));
        ActorTimeoutMessage msg = new ActorTimeoutMessage(-1);

        Mockito.doNothing().when(processor).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
        processor.processActorTimeoutMessage(ctxMock, msg);
        Mockito.verify(processor, Mockito.never()).tellParent(Mockito.any(ActorContext.class), Mockito.any(Object.class));
    }

    @Test
    public void processDisconnectMessageTest() {
        OperationsService osMock = Mockito.mock(OperationsService.class);
        ActorContext ctxMock = Mockito.mock(ActorContext.class);
        ActorRef appActorMock = Mockito.mock(ActorRef.class);
        Mockito.when(ctxMock.parent()).thenReturn(appActorMock);

        AkkaContext context = Mockito.mock(AkkaContext.class);
        Mockito.when(context.getOperationsService()).thenReturn(osMock);
        
        LocalEndpointActorMessageProcessor processor = Mockito.spy(new LocalEndpointActorMessageProcessor(context, "APP_TOKEN", EndpointObjectHash
                .fromSHA1("key"), "actorKey"));
        ChannelAware msg = Mockito.mock(ChannelAware.class);

        Assert.assertFalse(processor.processDisconnectMessage(ctxMock, msg));
    }

}
