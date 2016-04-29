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

package org.kaaproject.kaa.server.operations.pojo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;
import org.kaaproject.kaa.server.sync.ConfigurationServerSync;
import org.kaaproject.kaa.server.sync.EndpointAttachResponse;
import org.kaaproject.kaa.server.sync.EndpointDetachResponse;
import org.kaaproject.kaa.server.sync.Event;
import org.kaaproject.kaa.server.sync.EventListenersResponse;
import org.kaaproject.kaa.server.sync.EventServerSync;
import org.kaaproject.kaa.server.sync.LogServerSync;
import org.kaaproject.kaa.server.sync.NotificationServerSync;
import org.kaaproject.kaa.server.sync.ProfileServerSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.SyncResponseStatus;
import org.kaaproject.kaa.server.sync.UserAttachResponse;
import org.kaaproject.kaa.server.sync.UserServerSync;

public class SyncResponseHolderTest {


    @Test
    public void requireReplyTestForProfile(){
        ServerSync response = new ServerSync();
        response.setProfileSync(new ProfileServerSync());
        response.getProfileSync().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getProfileSync().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncContext(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForConfig(){
        ServerSync response = new ServerSync();
        response.setConfigurationSync(new ConfigurationServerSync());
        response.getConfigurationSync().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getConfigurationSync().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncContext(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForNotification(){
        ServerSync response = new ServerSync();
        response.setNotificationSync(new NotificationServerSync());
        response.getNotificationSync().setResponseStatus(SyncResponseStatus.NO_DELTA);
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getNotificationSync().setResponseStatus(SyncResponseStatus.DELTA);
        assertTrue(new SyncContext(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEvents(){
        ServerSync response = new ServerSync();
        response.setEventSync(new EventServerSync());
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getEventSync().setEvents(new ArrayList<Event>());
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getEventSync().getEvents().add(new Event());
        assertTrue(new SyncContext(response).requireImmediateReply());
    }


    @Test
    public void requireReplyTestForEventListeners(){
        ServerSync response = new ServerSync();
        response.setEventSync(new EventServerSync());
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getEventSync().setEventListenersResponses(new ArrayList<EventListenersResponse>());
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getEventSync().getEventListenersResponses().add(new EventListenersResponse());
        assertTrue(new SyncContext(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForUserAttach(){
        ServerSync response = new ServerSync();
        response.setUserSync(new UserServerSync());
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getUserSync().setUserAttachResponse(new UserAttachResponse());
        assertTrue(new SyncContext(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEndpointAttach(){
        ServerSync response = new ServerSync();
        response.setUserSync(new UserServerSync());
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getUserSync().setEndpointAttachResponses(new ArrayList<EndpointAttachResponse>());
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getUserSync().getEndpointAttachResponses().add(new EndpointAttachResponse());
        assertTrue(new SyncContext(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForEndpointDetach(){
        ServerSync response = new ServerSync();
        response.setUserSync(new UserServerSync());
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getUserSync().setEndpointDetachResponses(new ArrayList<EndpointDetachResponse>());
        assertFalse(new SyncContext(response).requireImmediateReply());
        response.getUserSync().getEndpointDetachResponses().add(new EndpointDetachResponse());
        assertTrue(new SyncContext(response).requireImmediateReply());
    }

    @Test
    public void requireReplyTestForLogs(){
        ServerSync response = new ServerSync();
        response.setLogSync(new LogServerSync());
        assertTrue(new SyncContext(response).requireImmediateReply());
    }
}
