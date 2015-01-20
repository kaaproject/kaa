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

import org.junit.Test;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.Event;
import org.kaaproject.kaa.common.endpoint.gen.EventListenersResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SyncResponseHolderTest {


    @Test
    public void requireReplyTestForProfile(){
        SyncResponse response = new SyncResponse();
        response.setProfileSyncResponse(new ProfileSyncResponse());
        response.getProfileSyncResponse().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getProfileSyncResponse().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForConfig(){
        SyncResponse response = new SyncResponse();
        response.setConfigurationSyncResponse(new ConfigurationSyncResponse());
        response.getConfigurationSyncResponse().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getConfigurationSyncResponse().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForNotification(){
        SyncResponse response = new SyncResponse();
        response.setNotificationSyncResponse(new NotificationSyncResponse());
        response.getNotificationSyncResponse().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getNotificationSyncResponse().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEvents(){
        SyncResponse response = new SyncResponse();
        response.setEventSyncResponse(new EventSyncResponse());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSyncResponse().setEvents(new ArrayList<Event>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSyncResponse().getEvents().add(new Event());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }


    @Test
    public void requireReplyTestForEventListeners(){
        SyncResponse response = new SyncResponse();
        response.setEventSyncResponse(new EventSyncResponse());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSyncResponse().setEventListenersResponses(new ArrayList<EventListenersResponse>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSyncResponse().getEventListenersResponses().add(new EventListenersResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForUserAttach(){
        SyncResponse response = new SyncResponse();
        response.setUserSyncResponse(new UserSyncResponse());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSyncResponse().setUserAttachResponse(new UserAttachResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEndpointAttach(){
        SyncResponse response = new SyncResponse();
        response.setUserSyncResponse(new UserSyncResponse());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSyncResponse().setEndpointAttachResponses(new ArrayList<EndpointAttachResponse>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSyncResponse().getEndpointAttachResponses().add(new EndpointAttachResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEndpointDetach(){
        SyncResponse response = new SyncResponse();
        response.setUserSyncResponse(new UserSyncResponse());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSyncResponse().setEndpointDetachResponses(new ArrayList<EndpointDetachResponse>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSyncResponse().getEndpointDetachResponses().add(new EndpointDetachResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForLogs(){
        SyncResponse response = new SyncResponse();
        response.setLogSyncResponse(new LogSyncResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }
}
