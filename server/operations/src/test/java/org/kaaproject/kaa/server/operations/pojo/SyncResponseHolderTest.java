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
package org.kaaproject.kaa.server.operations.pojo;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;

import org.junit.Test;
import org.kaaproject.kaa.common.endpoint.protocol.ConfigurationServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.EndpointAttachRequest;
import org.kaaproject.kaa.common.endpoint.protocol.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.protocol.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.protocol.Event;
import org.kaaproject.kaa.common.endpoint.protocol.EventListenersResponse;
import org.kaaproject.kaa.common.endpoint.protocol.EventServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.LogServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.NotificationServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.ProfileServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.ServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.protocol.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.protocol.UserServerSync;

public class SyncResponseHolderTest {


    @Test
    public void requireReplyTestForProfile(){
        ServerSync response = new ServerSync();
        response.setProfileSyncResponse(new ProfileServerSync());
        response.getProfileSyncResponse().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getProfileSyncResponse().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForConfig(){
        ServerSync response = new ServerSync();
        response.setConfigurationSyncResponse(new ConfigurationServerSync());
        response.getConfigurationSyncResponse().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getConfigurationSyncResponse().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForNotification(){
        ServerSync response = new ServerSync();
        response.setNotificationSyncResponse(new NotificationServerSync());
        response.getNotificationSyncResponse().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getNotificationSyncResponse().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEvents(){
        ServerSync response = new ServerSync();
        response.setEventSyncResponse(new EventServerSync());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSyncResponse().setEvents(new ArrayList<Event>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSyncResponse().getEvents().add(new Event());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }


    @Test
    public void requireReplyTestForEventListeners(){
        ServerSync response = new ServerSync();
        response.setEventSyncResponse(new EventServerSync());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSyncResponse().setEventListenersResponses(new ArrayList<EventListenersResponse>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSyncResponse().getEventListenersResponses().add(new EventListenersResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForUserAttach(){
        ServerSync response = new ServerSync();
        response.setUserSyncResponse(new UserServerSync());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSyncResponse().setUserAttachResponse(new UserAttachResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEndpointAttach(){
        ServerSync response = new ServerSync();
        response.setUserSyncResponse(new UserServerSync());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSyncResponse().setEndpointAttachResponses(new ArrayList<EndpointAttachResponse>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSyncResponse().getEndpointAttachResponses().add(new EndpointAttachResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEndpointDetach(){
        ServerSync response = new ServerSync();
        response.setUserSyncResponse(new UserServerSync());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSyncResponse().setEndpointDetachResponses(new ArrayList<EndpointDetachResponse>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSyncResponse().getEndpointDetachResponses().add(new EndpointDetachResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForLogs(){
        ServerSync response = new ServerSync();
        response.setLogSyncResponse(new LogServerSync());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }
}
