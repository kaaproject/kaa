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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.user;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.user.LocalUserActorMessageProcessor;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.EndpointEventTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventReceiveMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventSendMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RemoteEndpointEventMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserRouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFqnKey;
import org.kaaproject.kaa.server.operations.service.event.EndpointEvent;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;
import org.kaaproject.kaa.server.operations.service.event.EventClassFqnVersion;
import org.kaaproject.kaa.server.operations.service.event.EventService;
import org.kaaproject.kaa.server.operations.service.event.EventStorage;
import org.kaaproject.kaa.server.operations.service.event.RemoteEndpointEvent;
import org.kaaproject.kaa.server.operations.service.event.RouteInfo;
import org.kaaproject.kaa.server.operations.service.event.RouteTableAddress;
import org.kaaproject.kaa.server.operations.service.event.RouteTableKey;
import org.kaaproject.kaa.server.operations.service.event.UserRouteInfo;
import org.kaaproject.kaa.server.sync.Event;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class UserActorMessageProcessorTest {

    private static final int ECF_ID2_VERSION = 3;

    private static final int ECF_ID1_VERSION = 1;

    private static final String ECF_ID2 = "ECF_ID2";

    private static final String ECF_ID1 = "ECF_ID1";

    private static final String SERVER2 = "SERVER2";
    private static final String SERVER3 = "SERVER3";

    private static final String APP_TOKEN = "APP_TOKEN";

    private static final String TENANT_ID = "TENANT_ID";

    private static final String USER_ID = "USER_ID";

    private LocalUserActorMessageProcessor messageProcessor;

    private final EndpointObjectHash endpoint1Key = EndpointObjectHash.fromSHA1("endpoint1");
    private final EndpointObjectHash endpoint2Key = EndpointObjectHash.fromSHA1("endpoint2");
    private final EndpointObjectHash endpoint3Key = EndpointObjectHash.fromSHA1("endpoint3");
    private List<EventClassFamilyVersion> ecfVersions;
    private EventClassFamilyVersion ecfVersion1;
    private EventClassFamilyVersion ecfVersion2;

    private RouteTableAddress address1;
    private RouteTableAddress address2;
    private RouteTableAddress address3;

    private AkkaContext akkaContextMock;
    private CacheService cacheServiceMock;
    private EventService eventServiceMock;
    private ActorContext actorContextMock;
    private ActorRef originatorRefMock;

    @Before
    public void before() {
        cacheServiceMock = mock(CacheService.class);
        eventServiceMock = mock(EventService.class);
        originatorRefMock = mock(ActorRef.class);
        actorContextMock = mock(ActorContext.class);
        
        akkaContextMock = mock(AkkaContext.class);
        when(akkaContextMock.getCacheService()).thenReturn(cacheServiceMock);
        when(akkaContextMock.getEventService()).thenReturn(eventServiceMock);
        when(akkaContextMock.getEventTimeout()).thenReturn(60 * 1000L);
        
        messageProcessor = spy(new LocalUserActorMessageProcessor(akkaContextMock, USER_ID, TENANT_ID));
        doReturn("dummyPathName").when(messageProcessor).getActorPathName(any(ActorRef.class));
        Mockito.doNothing().when(messageProcessor).scheduleTimeoutMessage(Mockito.any(ActorContext.class), Mockito.any(EndpointEvent.class));
        Mockito.doNothing().when(messageProcessor).sendEventToLocal(Mockito.any(ActorContext.class), Mockito.any(EndpointEventReceiveMessage.class));
        ecfVersions = new ArrayList<>();
        ecfVersion1 = new EventClassFamilyVersion(ECF_ID1, ECF_ID1_VERSION);
        ecfVersion2 = new EventClassFamilyVersion(ECF_ID2, ECF_ID2_VERSION);
        ecfVersions.add(ecfVersion1);
        ecfVersions.add(ecfVersion2);

        address1 = new RouteTableAddress(endpoint1Key, APP_TOKEN);
        address2 = new RouteTableAddress(endpoint2Key, APP_TOKEN, SERVER2);
        address3 = new RouteTableAddress(endpoint3Key, APP_TOKEN);
    }

    @Test
    public void testEndpointConnectFlow(){
        EndpointUserConnectMessage message1 = new EndpointUserConnectMessage(USER_ID, endpoint1Key, ecfVersions, 1, null, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointConnectMessage(actorContextMock, message1);

        verify(eventServiceMock).sendUserRouteInfo( new UserRouteInfo(TENANT_ID, USER_ID));
        verify(eventServiceMock, Mockito.times(0)).sendRouteInfo(any(RouteInfo.class), any(String.class));

        RouteInfo routeInfo = new RouteInfo(TENANT_ID, USER_ID, address2, ecfVersions);
        RouteInfoMessage message2 = new RouteInfoMessage(routeInfo);
        messageProcessor.processRouteInfoMessage(actorContextMock, message2);

        RouteInfo localRouteInfo =new RouteInfo(TENANT_ID, USER_ID, address1, ecfVersions);
        verify(eventServiceMock).sendRouteInfo(Collections.singletonList(localRouteInfo), SERVER2);

        EndpointUserConnectMessage message3 = new EndpointUserConnectMessage(USER_ID, endpoint3Key, ecfVersions, 1, null, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointConnectMessage(actorContextMock, message3);

        verify(eventServiceMock).sendRouteInfo(new RouteInfo(TENANT_ID, USER_ID, address3, ecfVersions), SERVER2);

    }

    @Test
    public void testEndpointLocalEvent(){
        EndpointUserConnectMessage message1 = new EndpointUserConnectMessage(USER_ID, endpoint1Key, ecfVersions, 1, null, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointConnectMessage(actorContextMock, message1);

        EndpointUserConnectMessage message2 = new EndpointUserConnectMessage(USER_ID, endpoint2Key, ecfVersions, 1, null, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointConnectMessage(actorContextMock, message2);

        verify(eventServiceMock).sendUserRouteInfo( new UserRouteInfo(TENANT_ID, USER_ID));
        verify(eventServiceMock, Mockito.times(0)).sendRouteInfo(any(RouteInfo.class), any(String.class));

        when(cacheServiceMock.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, "testClassFqn"))).thenReturn(ECF_ID1);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, ecfVersion1);
        when(cacheServiceMock.getRouteKeys(new EventClassFqnVersion(TENANT_ID, "testClassFqn", ECF_ID1_VERSION))).thenReturn(Collections.singleton(routeKey));

        Event event = new Event(0, "testClassFqn", ByteBuffer.wrap(new byte[0]), null, Base64Util.encode(endpoint1Key.getData()));
        EndpointEventSendMessage eventMessage = new EndpointEventSendMessage(USER_ID, Collections.singletonList(event), endpoint2Key, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointEventSendMessage(actorContextMock, eventMessage);

        verify(messageProcessor).sendEventToLocal(Mockito.any(ActorContext.class), Mockito.any(EndpointEventReceiveMessage.class));
    }

    @Test
    public void testEndpointRemoteReceiveEvent(){
        EndpointUserConnectMessage message1 = new EndpointUserConnectMessage(USER_ID, endpoint1Key, ecfVersions, 1, null, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointConnectMessage(actorContextMock, message1);

        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, ecfVersion1);
        when(cacheServiceMock.getRouteKeys(new EventClassFqnVersion(TENANT_ID, "testClassFqn", ECF_ID1_VERSION))).thenReturn(Collections.singleton(routeKey));

        Event event = new Event(0, "testClassFqn", ByteBuffer.wrap(new byte[0]), null, Base64Util.encode(endpoint1Key.getData()));
        EndpointEvent endpointEvent = new EndpointEvent(endpoint2Key, event, UUID.randomUUID(), System.currentTimeMillis(), ECF_ID1_VERSION);
        RemoteEndpointEvent remoteEvent = new RemoteEndpointEvent(TENANT_ID, USER_ID, endpointEvent, new RouteTableAddress(endpoint1Key, APP_TOKEN, "SERVER1"));
        RemoteEndpointEventMessage message2 = new RemoteEndpointEventMessage(remoteEvent);
        messageProcessor.processRemoteEndpointEventMessage(actorContextMock, message2);

        verify(cacheServiceMock, Mockito.never()).getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, "testClassFqn"));

        verify(messageProcessor).sendEventToLocal(Mockito.any(ActorContext.class), Mockito.any(EndpointEventReceiveMessage.class));
    }

    @Test
    public void testEndpointRemoteSendEvent(){
        EndpointUserConnectMessage message1 = new EndpointUserConnectMessage(USER_ID, endpoint1Key, ecfVersions, 1, null, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointConnectMessage(actorContextMock, message1);

        verify(eventServiceMock).sendUserRouteInfo( new UserRouteInfo(TENANT_ID, USER_ID));
        verify(eventServiceMock, Mockito.times(0)).sendRouteInfo(any(RouteInfo.class), any(String.class));

        RouteInfo routeInfo = new RouteInfo(TENANT_ID, USER_ID, address2, ecfVersions);
        RouteInfoMessage message2 = new RouteInfoMessage(routeInfo);
        messageProcessor.processRouteInfoMessage(actorContextMock, message2);

        when(cacheServiceMock.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, "testClassFqn"))).thenReturn(ECF_ID1);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, ecfVersion1);
        when(cacheServiceMock.getRouteKeys(new EventClassFqnVersion(TENANT_ID, "testClassFqn", ECF_ID1_VERSION))).thenReturn(Collections.singleton(routeKey));

        Event event = new Event(0, "testClassFqn", ByteBuffer.wrap(new byte[0]), null, Base64Util.encode(endpoint2Key.getData()));
        EndpointEventSendMessage eventMessage = new EndpointEventSendMessage(USER_ID, Collections.singletonList(event), endpoint1Key, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointEventSendMessage(actorContextMock, eventMessage);

        verify(messageProcessor, Mockito.never()).sendEventToLocal(Mockito.any(ActorContext.class), Mockito.any(EndpointEventReceiveMessage.class));
        verify(eventServiceMock).sendEvent(any(RemoteEndpointEvent.class));
    }

    @Test
    public void testEndpointTimeoutMessage() throws NoSuchFieldException, SecurityException{
        EndpointUserConnectMessage message1 = new EndpointUserConnectMessage(USER_ID, endpoint1Key, ecfVersions, 1, null, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointConnectMessage(actorContextMock, message1);

        when(cacheServiceMock.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, "testClassFqn"))).thenReturn(ECF_ID1);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, ecfVersion1);
        when(cacheServiceMock.getRouteKeys(new EventClassFqnVersion(TENANT_ID, "testClassFqn", ECF_ID1_VERSION))).thenReturn(Collections.singleton(routeKey));

        Event event = new Event(0, "testClassFqn", ByteBuffer.wrap(new byte[0]), null, null);
        EndpointEventSendMessage eventMessage = new EndpointEventSendMessage(USER_ID, Collections.singletonList(event), endpoint1Key, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointEventSendMessage(actorContextMock, eventMessage);

        EndpointUserConnectMessage message2 = new EndpointUserConnectMessage(USER_ID, endpoint2Key, ecfVersions, 1, null, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointConnectMessage(actorContextMock, message2);

        //1 - means it is called once
        verify(messageProcessor, Mockito.times(1)).sendEventToLocal(Mockito.any(ActorContext.class), Mockito.any(EndpointEventReceiveMessage.class));

        Field field = LocalUserActorMessageProcessor.class.getDeclaredField("eventStorage");
        field.setAccessible(true);
        EventStorage storage = (EventStorage)ReflectionUtils.getField(field, messageProcessor);
        EndpointEventTimeoutMessage message = new EndpointEventTimeoutMessage(storage.getEvents(routeKey).iterator().next());
        messageProcessor.processEndpointEventTimeoutMessage(actorContextMock, message);

        EndpointUserConnectMessage message3 = new EndpointUserConnectMessage(USER_ID, endpoint3Key, ecfVersions, 1, null, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointConnectMessage(actorContextMock, message3);

        //still 1 - means it was not called after first call
        verify(messageProcessor, Mockito.times(1)).sendEventToLocal(Mockito.any(ActorContext.class), Mockito.any(EndpointEventReceiveMessage.class));
    }

    @Test
    public void testUserRouteInfoAddFlow(){
        EndpointUserConnectMessage message1 = new EndpointUserConnectMessage(USER_ID, endpoint1Key, ecfVersions, 1, null, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointConnectMessage(actorContextMock, message1);

        verify(eventServiceMock).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID));
        verify(eventServiceMock, Mockito.times(0)).sendRouteInfo(any(RouteInfo.class), any(String.class));

        UserRouteInfoMessage message2 = new UserRouteInfoMessage(new UserRouteInfo(TENANT_ID, USER_ID, SERVER2, RouteOperation.ADD));
        messageProcessor.processUserRouteInfoMessage(actorContextMock, message2);
        RouteInfo localRouteInfo =new RouteInfo(TENANT_ID, USER_ID, address1, ecfVersions);
        verify(eventServiceMock, Mockito.times(1)).sendRouteInfo(Collections.singletonList(localRouteInfo), SERVER2);

        UserRouteInfoMessage message3 = new UserRouteInfoMessage(new UserRouteInfo(TENANT_ID, USER_ID, SERVER3, RouteOperation.ADD));
        messageProcessor.processUserRouteInfoMessage(actorContextMock, message3);
        verify(eventServiceMock, Mockito.times(1)).sendRouteInfo(Collections.singletonList(localRouteInfo), SERVER3);
        verify(eventServiceMock, Mockito.times(1)).sendRouteInfo(Collections.singletonList(localRouteInfo), SERVER2);
    }

    @Test
    public void testUserRouteInfoRemoveFlow(){
        EndpointUserConnectMessage message1 = new EndpointUserConnectMessage(USER_ID, endpoint1Key, ecfVersions, 1, null, APP_TOKEN, originatorRefMock);
        messageProcessor.processEndpointConnectMessage(actorContextMock, message1);

        verify(eventServiceMock).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID));
        verify(eventServiceMock, Mockito.times(0)).sendRouteInfo(any(RouteInfo.class), any(String.class));

        UserRouteInfoMessage message2 = new UserRouteInfoMessage(new UserRouteInfo(TENANT_ID, USER_ID, SERVER2, RouteOperation.DELETE));
        messageProcessor.processUserRouteInfoMessage(actorContextMock, message2);
        RouteInfo localRouteInfo =new RouteInfo(TENANT_ID, USER_ID, address1, ecfVersions);
        verify(eventServiceMock, Mockito.times(0)).sendRouteInfo(Collections.singletonList(localRouteInfo), SERVER2);

        UserRouteInfoMessage message3 = new UserRouteInfoMessage(new UserRouteInfo(TENANT_ID, USER_ID, SERVER3, RouteOperation.DELETE));
        messageProcessor.processUserRouteInfoMessage(actorContextMock, message3);
        verify(eventServiceMock, Mockito.times(0)).sendRouteInfo(Collections.singletonList(localRouteInfo), SERVER3);
        verify(eventServiceMock, Mockito.times(0)).sendRouteInfo(Collections.singletonList(localRouteInfo), SERVER2);
    }
}
