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

package org.kaaproject.kaa.client.channel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.impl.DefaultOperationDataProcessor;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryStatus;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.RedirectSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;
import org.mockito.Mockito;

public class DefaultOperationDataProcessorTest {

    private static final int REQUEST_ID = 42;

    @Test
    public void testUpRequestCreationWithNullTypes() throws Exception {
        KaaClientState state = Mockito.mock(KaaClientState.class);
        DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor(state);
        assertNull(operationsDataProcessor.compileRequest(null));
    }

    @Test
    public void testUpRequestCreationWithUnknownType() throws Exception {
        KaaClientState state = Mockito.mock(KaaClientState.class);
        DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor(state);
        Map<TransportType, ChannelDirection> types = new HashMap<>();
        types.put(TransportType.BOOTSTRAP, ChannelDirection.BIDIRECTIONAL);
        assertNull(operationsDataProcessor.compileRequest(types));
    }

    @Test
    public void testUpRequestCreationWithNullTransports() throws Exception {
        KaaClientState state = Mockito.mock(KaaClientState.class);
        DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor(state);

        Map<TransportType, ChannelDirection> transportTypes = new HashMap<TransportType, ChannelDirection>();
        transportTypes.put(TransportType.PROFILE, ChannelDirection.BIDIRECTIONAL);
        transportTypes.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        transportTypes.put(TransportType.NOTIFICATION, ChannelDirection.BIDIRECTIONAL);
        transportTypes.put(TransportType.USER, ChannelDirection.BIDIRECTIONAL);
        transportTypes.put(TransportType.EVENT, ChannelDirection.BIDIRECTIONAL);
        transportTypes.put(TransportType.LOGGING, ChannelDirection.BIDIRECTIONAL);

        assertNotNull(operationsDataProcessor.compileRequest(transportTypes));
    }

    @Test
    public void testUpRequestCreation() throws Exception {
        KaaClientState state = Mockito.mock(KaaClientState.class);
        DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor(state);

        ProfileTransport profileTransport = Mockito.mock(ProfileTransport.class);
        EventTransport eventTransport = Mockito.mock(EventTransport.class);
        NotificationTransport notificationTransport = Mockito.mock(NotificationTransport.class);
        ConfigurationTransport configurationTransport = Mockito.mock(ConfigurationTransport.class);
        UserTransport userTransport = Mockito.mock(UserTransport.class);
        MetaDataTransport metaDataTransport = Mockito.mock(MetaDataTransport.class);
        LogTransport logTransport = Mockito.mock(LogTransport.class);

        operationsDataProcessor.setConfigurationTransport(configurationTransport);
        operationsDataProcessor.setEventTransport(eventTransport);
        operationsDataProcessor.setMetaDataTransport(metaDataTransport);
        operationsDataProcessor.setNotificationTransport(notificationTransport);
        operationsDataProcessor.setProfileTransport(profileTransport);
        operationsDataProcessor.setUserTransport(userTransport);
        operationsDataProcessor.setLogTransport(logTransport);

        Map<TransportType, ChannelDirection> transportTypes = new HashMap<TransportType, ChannelDirection>();
        transportTypes.put(TransportType.PROFILE, ChannelDirection.BIDIRECTIONAL);
        transportTypes.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        transportTypes.put(TransportType.NOTIFICATION, ChannelDirection.BIDIRECTIONAL);
        transportTypes.put(TransportType.USER, ChannelDirection.BIDIRECTIONAL);
        transportTypes.put(TransportType.EVENT, ChannelDirection.BIDIRECTIONAL);
        transportTypes.put(TransportType.LOGGING, ChannelDirection.BIDIRECTIONAL);

        assertNotNull(operationsDataProcessor.compileRequest(transportTypes));
        Mockito.verify(profileTransport, Mockito.times(1)).createProfileRequest();
        Mockito.verify(eventTransport, Mockito.times(1)).createEventRequest(Mockito.anyInt());
        Mockito.verify(notificationTransport, Mockito.times(1)).createNotificationRequest();
        Mockito.verify(configurationTransport, Mockito.times(1)).createConfigurationRequest();
        Mockito.verify(userTransport, Mockito.times(1)).createUserRequest();
        Mockito.verify(metaDataTransport, Mockito.times(1)).createMetaDataRequest();
        Mockito.verify(logTransport, Mockito.times(1)).createLogRequest();
    }

    @Test
    public void testDownRequestCreation() throws Exception {
        KaaClientState state = Mockito.mock(KaaClientState.class);
        DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor(state);

        ProfileTransport profileTransport = Mockito.mock(ProfileTransport.class);
        EventTransport eventTransport = Mockito.mock(EventTransport.class);
        NotificationTransport notificationTransport = Mockito.mock(NotificationTransport.class);
        ConfigurationTransport configurationTransport = Mockito.mock(ConfigurationTransport.class);
        UserTransport userTransport = Mockito.mock(UserTransport.class);
        MetaDataTransport metaDataTransport = Mockito.mock(MetaDataTransport.class);
        LogTransport logTransport = Mockito.mock(LogTransport.class);

        operationsDataProcessor.setConfigurationTransport(configurationTransport);
        operationsDataProcessor.setEventTransport(eventTransport);
        operationsDataProcessor.setMetaDataTransport(metaDataTransport);
        operationsDataProcessor.setNotificationTransport(notificationTransport);
        operationsDataProcessor.setProfileTransport(profileTransport);
        operationsDataProcessor.setUserTransport(userTransport);
        operationsDataProcessor.setLogTransport(logTransport);

        Map<TransportType, ChannelDirection> transportTypes = new HashMap<TransportType, ChannelDirection>();
        transportTypes.put(TransportType.PROFILE, ChannelDirection.DOWN);
        transportTypes.put(TransportType.CONFIGURATION, ChannelDirection.DOWN);
        transportTypes.put(TransportType.NOTIFICATION, ChannelDirection.DOWN);
        transportTypes.put(TransportType.USER, ChannelDirection.DOWN);
        transportTypes.put(TransportType.EVENT, ChannelDirection.DOWN);
        transportTypes.put(TransportType.LOGGING, ChannelDirection.DOWN);

        assertNotNull(operationsDataProcessor.compileRequest(transportTypes));
        Mockito.verify(profileTransport, Mockito.times(0)).createProfileRequest();
        Mockito.verify(eventTransport, Mockito.times(0)).createEventRequest(Mockito.anyInt());
        Mockito.verify(notificationTransport, Mockito.times(0)).createNotificationRequest();
        Mockito.verify(configurationTransport, Mockito.times(1)).createConfigurationRequest();
        Mockito.verify(userTransport, Mockito.times(0)).createUserRequest();
        Mockito.verify(logTransport, Mockito.times(0)).createLogRequest();
        Mockito.verify(metaDataTransport, Mockito.times(1)).createMetaDataRequest();
    }

    @Test
    public void testResponse() throws Exception {
        KaaClientState state = Mockito.mock(KaaClientState.class);
        DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor(state);

        ProfileTransport profileTransport = Mockito.mock(ProfileTransport.class);
        EventTransport eventTransport = Mockito.mock(EventTransport.class);
        NotificationTransport notificationTransport = Mockito.mock(NotificationTransport.class);
        ConfigurationTransport configurationTransport = Mockito.mock(ConfigurationTransport.class);
        UserTransport userTransport = Mockito.mock(UserTransport.class);
        RedirectionTransport redirectionTransport = Mockito.mock(RedirectionTransport.class);
        LogTransport logTransport = Mockito.mock(LogTransport.class);

        operationsDataProcessor.setConfigurationTransport(configurationTransport);
        operationsDataProcessor.setEventTransport(eventTransport);
        operationsDataProcessor.setNotificationTransport(notificationTransport);
        operationsDataProcessor.setProfileTransport(profileTransport);
        operationsDataProcessor.setRedirectionTransport(redirectionTransport);
        operationsDataProcessor.setUserTransport(userTransport);
        operationsDataProcessor.setLogTransport(logTransport);

        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.SUCCESS);
        response.setConfigurationSyncResponse(new ConfigurationSyncResponse(SyncResponseStatus.DELTA, null, null));
        response.setEventSyncResponse(new EventSyncResponse());
        response.setNotificationSyncResponse(new NotificationSyncResponse(SyncResponseStatus.DELTA, null, null));
        response.setProfileSyncResponse(new ProfileSyncResponse(SyncResponseStatus.DELTA));
        response.setRedirectSyncResponse(new RedirectSyncResponse(1));
        response.setUserSyncResponse(new UserSyncResponse());

        LogDeliveryStatus status = new LogDeliveryStatus(REQUEST_ID, SyncResponseResultType.SUCCESS, null);
        response.setLogSyncResponse(new LogSyncResponse(Collections.singletonList(status)));

        AvroByteArrayConverter<SyncResponse> converter = new AvroByteArrayConverter<>(SyncResponse.class);
        operationsDataProcessor.processResponse(converter.toByteArray(response));

        Mockito.verify(profileTransport, Mockito.times(1)).onProfileResponse(Mockito.any(ProfileSyncResponse.class));
        Mockito.verify(eventTransport, Mockito.times(1)).onEventResponse(Mockito.any(EventSyncResponse.class));
        Mockito.verify(notificationTransport, Mockito.times(1)).onNotificationResponse(Mockito.any(NotificationSyncResponse.class));
        Mockito.verify(configurationTransport, Mockito.times(1)).onConfigurationResponse(Mockito.any(ConfigurationSyncResponse.class));
        Mockito.verify(userTransport, Mockito.times(1)).onUserResponse(Mockito.any(UserSyncResponse.class));
        Mockito.verify(redirectionTransport, Mockito.times(1)).onRedirectionResponse(Mockito.any(RedirectSyncResponse.class));
        Mockito.verify(logTransport, Mockito.times(1)).onLogResponse(Mockito.any(LogSyncResponse.class));
    }

    @Test
    public void testResponseWithNullTransports() throws Exception {
        KaaClientState state = Mockito.mock(KaaClientState.class);
        DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor(state);

        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.SUCCESS);
        response.setConfigurationSyncResponse(new ConfigurationSyncResponse(SyncResponseStatus.DELTA, null, null));
        response.setEventSyncResponse(new EventSyncResponse());
        response.setNotificationSyncResponse(new NotificationSyncResponse(SyncResponseStatus.DELTA, null, null));
        response.setProfileSyncResponse(new ProfileSyncResponse(SyncResponseStatus.DELTA));
        response.setRedirectSyncResponse(new RedirectSyncResponse(1));
        response.setUserSyncResponse(new UserSyncResponse());

        LogDeliveryStatus status = new LogDeliveryStatus(REQUEST_ID, SyncResponseResultType.SUCCESS, null);
        response.setLogSyncResponse(new LogSyncResponse(Collections.singletonList(status)));

        AvroByteArrayConverter<SyncResponse> converter = new AvroByteArrayConverter<>(SyncResponse.class);
        operationsDataProcessor.processResponse(converter.toByteArray(response));
    }

    @Test
    public void testResponseWithNullTransportsAndResponses() throws Exception {
        KaaClientState state = Mockito.mock(KaaClientState.class);
        DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor(state);

        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.SUCCESS);

        AvroByteArrayConverter<SyncResponse> converter = new AvroByteArrayConverter<>(SyncResponse.class);
        operationsDataProcessor.processResponse(converter.toByteArray(response));
    }

    @Test
    public void testResponseWithNullResponses() throws Exception {
        KaaClientState state = Mockito.mock(KaaClientState.class);
        DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor(state);

        ProfileTransport profileTransport = Mockito.mock(ProfileTransport.class);
        EventTransport eventTransport = Mockito.mock(EventTransport.class);
        NotificationTransport notificationTransport = Mockito.mock(NotificationTransport.class);
        ConfigurationTransport configurationTransport = Mockito.mock(ConfigurationTransport.class);
        UserTransport userTransport = Mockito.mock(UserTransport.class);
        RedirectionTransport redirectionTransport = Mockito.mock(RedirectionTransport.class);
        LogTransport logTransport = Mockito.mock(LogTransport.class);

        operationsDataProcessor.setConfigurationTransport(configurationTransport);
        operationsDataProcessor.setEventTransport(eventTransport);
        operationsDataProcessor.setNotificationTransport(notificationTransport);
        operationsDataProcessor.setProfileTransport(profileTransport);
        operationsDataProcessor.setRedirectionTransport(redirectionTransport);
        operationsDataProcessor.setUserTransport(userTransport);
        operationsDataProcessor.setLogTransport(logTransport);

        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.SUCCESS);

        AvroByteArrayConverter<SyncResponse> converter = new AvroByteArrayConverter<>(SyncResponse.class);
        operationsDataProcessor.processResponse(converter.toByteArray(response));

        Mockito.verify(profileTransport, Mockito.times(0)).onProfileResponse(Mockito.any(ProfileSyncResponse.class));
        Mockito.verify(eventTransport, Mockito.times(0)).onEventResponse(Mockito.any(EventSyncResponse.class));
        Mockito.verify(notificationTransport, Mockito.times(0)).onNotificationResponse(Mockito.any(NotificationSyncResponse.class));
        Mockito.verify(configurationTransport, Mockito.times(0)).onConfigurationResponse(Mockito.any(ConfigurationSyncResponse.class));
        Mockito.verify(userTransport, Mockito.times(0)).onUserResponse(Mockito.any(UserSyncResponse.class));
        Mockito.verify(redirectionTransport, Mockito.times(0)).onRedirectionResponse(Mockito.any(RedirectSyncResponse.class));
        Mockito.verify(logTransport, Mockito.times(0)).onLogResponse(Mockito.any(LogSyncResponse.class));
    }

    @Test
    public void testProfileResync() throws Exception {
        KaaClientState state = Mockito.mock(KaaClientState.class);
        DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor(state);

        ProfileTransport profileTransport = Mockito.mock(ProfileTransport.class);
        operationsDataProcessor.setProfileTransport(profileTransport);

        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.PROFILE_RESYNC);

        AvroByteArrayConverter<SyncResponse> converter = new AvroByteArrayConverter<>(SyncResponse.class);

        byte[] responseData = converter.toByteArray(response);

        operationsDataProcessor.processResponse(responseData);
        Mockito.verify(profileTransport, Mockito.times(1)).sync();

        operationsDataProcessor.processResponse(responseData);
        Mockito.verify(profileTransport, Mockito.times(2)).sync();

        response.setStatus(SyncResponseResultType.SUCCESS);
        responseData = converter.toByteArray(response);

        operationsDataProcessor.processResponse(responseData);
        //invocation count still equals 2 because no resync response received
        Mockito.verify(profileTransport, Mockito.times(2)).sync();
    }

}
