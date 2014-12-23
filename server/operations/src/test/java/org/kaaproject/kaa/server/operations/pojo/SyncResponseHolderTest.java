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
import org.kaaproject.kaa.server.operations.pojo.sync.ConfigurationServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachRequest;
import org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachResponse;
import org.kaaproject.kaa.server.operations.pojo.sync.EndpointDetachResponse;
import org.kaaproject.kaa.server.operations.pojo.sync.Event;
import org.kaaproject.kaa.server.operations.pojo.sync.EventListenersResponse;
import org.kaaproject.kaa.server.operations.pojo.sync.EventServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.LogServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.NotificationServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.ProfileServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.ServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.SyncResponseStatus;
import org.kaaproject.kaa.server.operations.pojo.sync.UserAttachResponse;
import org.kaaproject.kaa.server.operations.pojo.sync.UserServerSync;

public class SyncResponseHolderTest {


    @Test
    public void requireReplyTestForProfile(){
        ServerSync response = new ServerSync();
        response.setProfileSync(new ProfileServerSync());
        response.getProfileSync().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getProfileSync().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForConfig(){
        ServerSync response = new ServerSync();
        response.setConfigurationSync(new ConfigurationServerSync());
        response.getConfigurationSync().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getConfigurationSync().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForNotification(){
        ServerSync response = new ServerSync();
        response.setNotificationSync(new NotificationServerSync());
        response.getNotificationSync().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getNotificationSync().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEvents(){
        ServerSync response = new ServerSync();
        response.setEventSync(new EventServerSync());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSync().setEvents(new ArrayList<Event>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSync().getEvents().add(new Event());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }


    @Test
    public void requireReplyTestForEventListeners(){
        ServerSync response = new ServerSync();
        response.setEventSync(new EventServerSync());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSync().setEventListenersResponses(new ArrayList<EventListenersResponse>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getEventSync().getEventListenersResponses().add(new EventListenersResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForUserAttach(){
        ServerSync response = new ServerSync();
        response.setUserSync(new UserServerSync());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSync().setUserAttachResponse(new UserAttachResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEndpointAttach(){
        ServerSync response = new ServerSync();
        response.setUserSync(new UserServerSync());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSync().setEndpointAttachResponses(new ArrayList<EndpointAttachResponse>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSync().getEndpointAttachResponses().add(new EndpointAttachResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEndpointDetach(){
        ServerSync response = new ServerSync();
        response.setUserSync(new UserServerSync());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSync().setEndpointDetachResponses(new ArrayList<EndpointDetachResponse>());
        assertFalse(new SyncResponseHolder(response).requireImmediateReply());
        response.getUserSync().getEndpointDetachResponses().add(new EndpointDetachResponse());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForLogs(){
        ServerSync response = new ServerSync();
        response.setLogSync(new LogServerSync());
        assertTrue(new SyncResponseHolder(response).requireImmediateReply());
    }
}
