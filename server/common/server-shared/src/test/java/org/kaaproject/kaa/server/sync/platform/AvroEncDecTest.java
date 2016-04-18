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

package org.kaaproject.kaa.server.sync.platform;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.common.endpoint.gen.BootstrapSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationType;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachErrorCode;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncRequest;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ConfigurationServerSync;
import org.kaaproject.kaa.server.sync.Event;
import org.kaaproject.kaa.server.sync.EventServerSync;
import org.kaaproject.kaa.server.sync.LogDeliveryStatus;
import org.kaaproject.kaa.server.sync.LogServerSync;
import org.kaaproject.kaa.server.sync.NotificationServerSync;
import org.kaaproject.kaa.server.sync.ProfileServerSync;
import org.kaaproject.kaa.server.sync.RedirectServerSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.kaaproject.kaa.server.sync.UserServerSync;
import org.kaaproject.kaa.server.sync.UserVerifierErrorCode;
import org.kaaproject.kaa.server.sync.bootstrap.BootstrapServerSync;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AvroEncDecTest {

    private static final String CONVER_METHOD = "convert";

    @Test
    public void encodeTest() throws PlatformEncDecException {
        AvroEncDec encDec = new AvroEncDec();
        Assert.assertNull(encDec.encode(null));
    }

    @Test
    public void getIdTest() {
        Assert.assertEquals(Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, new AvroEncDec().getId());
    }

    @Test
    public void convertSyncRequestTest() {
        ClientSync clientSync = new ClientSync();
        Assert.assertEquals(clientSync, AvroEncDec.convert(new SyncRequest()));
    }

    @Test
    public void convertServerSyncTest() {
        SyncResponse syncResponse = new SyncResponse();
        Assert.assertEquals(syncResponse, AvroEncDec.convert(new ServerSync()));
    }

    @Test
    public void convertNullTest() {
        Event event = null;
        Assert.assertNull(AvroEncDec.convert(event));
    }

    @Test
    public void convertEventTest() {
        Event event = new Event();
        org.kaaproject.kaa.common.endpoint.gen.Event genEvent = new org.kaaproject.kaa.common.endpoint.gen.Event(event.getSeqNum(), event.getEventClassFQN(), event.getEventData(), event.getSource(), event.getTarget());
        org.kaaproject.kaa.common.endpoint.gen.Event nullGenEvent = null;
        Assert.assertEquals(event, AvroEncDec.convert(genEvent));
        Assert.assertNull(AvroEncDec.convert(nullGenEvent));
    }

    @Test(expected = PlatformEncDecException.class)
    public void decodeNullSourceTest() throws PlatformEncDecException {
        AvroEncDec encDec = new AvroEncDec();
        byte[] bytes = new byte[0];
        Assert.assertNull(encDec.decode(bytes));
    }

    @Test
    public void convertSyncStatusTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, SyncStatus.class);
        method.setAccessible(true);
        SyncStatus syncStatus = null;
        Assert.assertNull(method.invoke(avroEncDec, syncStatus));
        Assert.assertEquals(SyncResponseResultType.SUCCESS, method.invoke(avroEncDec, SyncStatus.SUCCESS));
        Assert.assertEquals(SyncResponseResultType.FAILURE, method.invoke(avroEncDec, SyncStatus.FAILURE));
        Assert.assertEquals(SyncResponseResultType.PROFILE_RESYNC, method.invoke(avroEncDec, SyncStatus.PROFILE_RESYNC));
        Assert.assertEquals(SyncResponseResultType.REDIRECT, method.invoke(avroEncDec, SyncStatus.REDIRECT));
    }


    @Test
    public void convertBootstrapServerSyncTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, BootstrapServerSync.class);
        method.setAccessible(true);
        BootstrapServerSync serverSync = null;
        Assert.assertNull(method.invoke(avroEncDec, serverSync));
        BootstrapServerSync bootstrapServerSync = new BootstrapServerSync(1, Collections.EMPTY_SET);
        method.invoke(avroEncDec, bootstrapServerSync);
    }


    @Test
    public void convertProtocolConnectionDataSetTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, Set.class);
        method.setAccessible(true);
        Set set = null;
        Assert.assertEquals(Collections.emptyList(), method.invoke(avroEncDec, set));
        method.invoke(avroEncDec, new HashSet<>());
    }

    @Test
    public void convertRedirectServerSyncTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, RedirectServerSync.class);
        method.setAccessible(true);
        RedirectServerSync serverSync = null;
        Assert.assertNull(method.invoke(avroEncDec, serverSync));
        method.invoke(avroEncDec, new RedirectServerSync());
    }

    @Test
    public void convertProfileServerSyncTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, ProfileServerSync.class);
        method.setAccessible(true);
        ProfileServerSync serverSync = null;
        Assert.assertNull(method.invoke(avroEncDec, serverSync));
        method.invoke(avroEncDec, new ProfileServerSync());
    }

    @Test
    public void convertSyncResponseStatusTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, org.kaaproject.kaa.server.sync.SyncResponseStatus.class);
        method.setAccessible(true);
        org.kaaproject.kaa.server.sync.SyncResponseStatus responseStatus = null;
        Assert.assertNull(method.invoke(avroEncDec, responseStatus));
        Assert.assertEquals(SyncResponseStatus.DELTA, method.invoke(avroEncDec, org.kaaproject.kaa.server.sync.SyncResponseStatus.DELTA));
        Assert.assertEquals(SyncResponseStatus.NO_DELTA, method.invoke(avroEncDec, org.kaaproject.kaa.server.sync.SyncResponseStatus.NO_DELTA));
        Assert.assertEquals(SyncResponseStatus.RESYNC, method.invoke(avroEncDec, org.kaaproject.kaa.server.sync.SyncResponseStatus.RESYNC));
    }

    @Test
    public void convertConfigurationServerSyncTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, ConfigurationServerSync.class);
        method.setAccessible(true);
        ConfigurationServerSync serverSync = null;
        Assert.assertNull(method.invoke(avroEncDec, serverSync));
        method.invoke(avroEncDec, new ConfigurationServerSync());
    }

    @Test
    public void convertNotificationServerSyncTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, NotificationServerSync.class);
        method.setAccessible(true);
        NotificationServerSync serverSync = null;
        Assert.assertNull(method.invoke(avroEncDec, serverSync));
        method.invoke(avroEncDec, new NotificationServerSync());
    }

    @Test
    public void convertEventServerSyncTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, EventServerSync.class);
        method.setAccessible(true);
        EventServerSync serverSync = null;
        Assert.assertNull(method.invoke(avroEncDec, serverSync));
        method.invoke(avroEncDec, new EventServerSync());
    }

    @Test
    public void convertUserServerSyncTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, UserServerSync.class);
        method.setAccessible(true);
        UserServerSync serverSync = null;
        Assert.assertNull(method.invoke(avroEncDec, serverSync));
        method.invoke(avroEncDec, new UserServerSync());
    }

    @Test
    public void convertUserVerifierErrorCodeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, UserVerifierErrorCode.class);
        method.setAccessible(true);
        UserVerifierErrorCode errorCode = null;
        Assert.assertNull(method.invoke(avroEncDec, errorCode));
        Assert.assertEquals(UserAttachErrorCode.CONNECTION_ERROR, method.invoke(avroEncDec, UserVerifierErrorCode.CONNECTION_ERROR));
        Assert.assertEquals(UserAttachErrorCode.INTERNAL_ERROR, method.invoke(avroEncDec, UserVerifierErrorCode.INTERNAL_ERROR));
        Assert.assertEquals(UserAttachErrorCode.NO_VERIFIER_CONFIGURED, method.invoke(avroEncDec, UserVerifierErrorCode.NO_VERIFIER_CONFIGURED));
        Assert.assertEquals(UserAttachErrorCode.REMOTE_ERROR, method.invoke(avroEncDec, UserVerifierErrorCode.REMOTE_ERROR));
        Assert.assertEquals(UserAttachErrorCode.TOKEN_EXPIRED, method.invoke(avroEncDec, UserVerifierErrorCode.TOKEN_EXPIRED));
        Assert.assertEquals(UserAttachErrorCode.TOKEN_INVALID, method.invoke(avroEncDec, UserVerifierErrorCode.TOKEN_INVALID));
    }


    @Test
    public void convertNotificationTypeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, org.kaaproject.kaa.server.sync.NotificationType.class);
        method.setAccessible(true);
        org.kaaproject.kaa.server.sync.NotificationType notificationType = null;
        Assert.assertNull(method.invoke(avroEncDec, notificationType));
        Assert.assertEquals(NotificationType.CUSTOM, method.invoke(avroEncDec, org.kaaproject.kaa.server.sync.NotificationType.CUSTOM));
        Assert.assertEquals(NotificationType.SYSTEM, method.invoke(avroEncDec, org.kaaproject.kaa.server.sync.NotificationType.SYSTEM));
    }

    @Test
    public void convertUserAttachResponseTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, org.kaaproject.kaa.server.sync.UserAttachResponse.class);
        method.setAccessible(true);
        org.kaaproject.kaa.server.sync.UserAttachResponse failure = new org.kaaproject.kaa.server.sync.UserAttachResponse(SyncStatus.FAILURE, null, null);
        Assert.assertEquals(new UserAttachResponse(SyncResponseResultType.FAILURE, null, null), method.invoke(avroEncDec, failure));
        org.kaaproject.kaa.server.sync.UserAttachResponse success = new org.kaaproject.kaa.server.sync.UserAttachResponse(SyncStatus.SUCCESS, null, null);
        Assert.assertEquals(new UserAttachResponse(SyncResponseResultType.SUCCESS, null, null), method.invoke(avroEncDec, success));

    }

    @Test
    public void convertLogServerSyncTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, LogServerSync.class);
        method.setAccessible(true);
        LogServerSync serverSync = null;
        Assert.assertNull(method.invoke(avroEncDec, serverSync));
        method.invoke(avroEncDec, new LogServerSync());
    }

    @Test
    public void convertLogDeliveryStatusTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, LogDeliveryStatus.class);
        method.setAccessible(true);
        LogDeliveryStatus deliveryStatus = null;
        Assert.assertNull(method.invoke(avroEncDec, deliveryStatus));
        method.invoke(avroEncDec, new LogDeliveryStatus(1, SyncStatus.SUCCESS, null));
    }

    @Test
    public void convertLogDeliveryErrorCodeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, org.kaaproject.kaa.server.sync.LogDeliveryErrorCode.class);
        method.setAccessible(true);
        org.kaaproject.kaa.server.sync.LogDeliveryErrorCode deliveryErrorCode = null;
        Assert.assertNull(method.invoke(avroEncDec, deliveryErrorCode));
        Assert.assertEquals(LogDeliveryErrorCode.NO_APPENDERS_CONFIGURED, method.invoke(avroEncDec, org.kaaproject.kaa.server.sync.LogDeliveryErrorCode.NO_APPENDERS_CONFIGURED));
        Assert.assertEquals(LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR, method.invoke(avroEncDec, org.kaaproject.kaa.server.sync.LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR));
        Assert.assertEquals(LogDeliveryErrorCode.REMOTE_CONNECTION_ERROR, method.invoke(avroEncDec, org.kaaproject.kaa.server.sync.LogDeliveryErrorCode.REMOTE_CONNECTION_ERROR));
        Assert.assertEquals(LogDeliveryErrorCode.REMOTE_INTERNAL_ERROR, method.invoke(avroEncDec, org.kaaproject.kaa.server.sync.LogDeliveryErrorCode.REMOTE_INTERNAL_ERROR));
    }

    @Test
    public void convertSyncRequestMetaDataTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, SyncRequestMetaData.class);
        method.setAccessible(true);
        SyncRequestMetaData requestMetaData = null;
        Assert.assertNull(method.invoke(avroEncDec, requestMetaData));
        method.invoke(avroEncDec, new SyncRequestMetaData());
    }

    @Test
    public void convertBootstrapSyncRequestTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, BootstrapSyncRequest.class);
        method.setAccessible(true);
        BootstrapSyncRequest syncRequest = null;
        Assert.assertNull(method.invoke(avroEncDec, syncRequest));
        method.invoke(avroEncDec, new BootstrapSyncRequest());
    }

    @Test
    public void convertProtocolVersionPairListTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, List.class);
        method.setAccessible(true);
        List list = null;
        Assert.assertEquals(Collections.emptyList(), method.invoke(avroEncDec, list));
        method.invoke(avroEncDec, new LinkedList<>());
    }

    @Test
    public void convertProfileSyncRequestTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, ProfileSyncRequest.class);
        method.setAccessible(true);
        ProfileSyncRequest syncRequest = null;
        Assert.assertNull(method.invoke(avroEncDec, syncRequest));
        method.invoke(avroEncDec, new ProfileSyncRequest());
    }

    @Test
    public void convertConfigurationSyncRequestTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, ConfigurationSyncRequest.class);
        method.setAccessible(true);
        ConfigurationSyncRequest syncRequest = null;
        Assert.assertNull(method.invoke(avroEncDec, syncRequest));
        method.invoke(avroEncDec, new ConfigurationSyncRequest());
    }


    @Test
    public void convertNotificationSyncRequestTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, NotificationSyncRequest.class);
        method.setAccessible(true);
        NotificationSyncRequest syncRequest = null;
        Assert.assertNull(method.invoke(avroEncDec, syncRequest));
        method.invoke(avroEncDec, new NotificationSyncRequest());
    }

    @Test
    public void convertEventSyncRequestTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, EventSyncRequest.class);
        method.setAccessible(true);
        EventSyncRequest syncRequest = null;
        Assert.assertNull(method.invoke(avroEncDec, syncRequest));
        method.invoke(avroEncDec, new EventSyncRequest());
    }

    @Test
    public void convertLogSyncRequestTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, LogSyncRequest.class);
        method.setAccessible(true);
        LogSyncRequest syncRequest = null;
        Assert.assertNull(method.invoke(avroEncDec, syncRequest));
        method.invoke(avroEncDec, new LogSyncRequest());
    }

    @Test
    public void convertUserSyncRequestTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AvroEncDec avroEncDec = new AvroEncDec();
        Method method = AvroEncDec.class.getDeclaredMethod(CONVER_METHOD, UserSyncRequest.class);
        method.setAccessible(true);
        UserSyncRequest syncRequest = null;
        Assert.assertNull(method.invoke(avroEncDec, syncRequest));
        method.invoke(avroEncDec, new UserSyncRequest());
    }


}
