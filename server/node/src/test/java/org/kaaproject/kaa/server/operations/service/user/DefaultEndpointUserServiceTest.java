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

package org.kaaproject.kaa.server.operations.service.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFqnKey;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;
import org.kaaproject.kaa.server.operations.service.event.EventClassFqnVersion;
import org.kaaproject.kaa.server.operations.service.event.RouteTableKey;
import org.kaaproject.kaa.server.sync.EndpointAttachRequest;
import org.kaaproject.kaa.server.sync.EndpointAttachResponse;
import org.kaaproject.kaa.server.sync.EndpointDetachRequest;
import org.kaaproject.kaa.server.sync.EndpointDetachResponse;
import org.kaaproject.kaa.server.sync.EventListenersRequest;
import org.kaaproject.kaa.server.sync.EventListenersResponse;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultEndpointUserServiceTest {
    private static final int ECF1_VERSION = 43;
    private static final String ECF1_ID = "EF1_ID";
    private static final String ENDPOINT_ACCESS_TOKEN = "endpointAccessToken";
    private static final String EXTERNAL_USER_ID = "userExternalId";
    private static final String USER_ID = "userId";

    private static final String TEST_TENANT_ID = "testTenantId";

    private static final String EXTERNAL_USER_ID_OTHER = "userExternalIdOther";
    private static final int REQUEST_ID = 42;
    private static final byte[] ENDPOINT_KEY_HASH = new byte[]{1,2,3};
    private static final byte[] ENDPOINT_KEY_HASH_OTHER = new byte[]{4,5,6};
    private static final byte[] ENDPOINT_KEY_HASH1 = new byte[]{1,2,0,1};
    private static final byte[] ENDPOINT_KEY_HASH2 = new byte[]{1,2,0,2};
    private static final byte[] ENDPOINT_KEY_HASH3 = new byte[]{1,2,0,3};


    private static final String TEST_APP_ID = "testAppId";
    private static final String TEST_APP_ID2 = "testAppId2";
    private static final String TEST_APP_ID3 = "testAppId3";
    private static final String TEST_APP_TOKEN = "testAppToken";
    private static final String TEST_APP_TOKEN2 = "testAppToken2";
    private static final String TEST_APP_TOKEN3 = "testAppToken3";
    private static final int TEST_APP_SEQ_NUM = 42;

    private EndpointUserService endpointUserService;
    private EndpointService endpointService;
    private CacheService cacheService;

    @Before
    public void before() {
        endpointUserService = new DefaultEndpointUserService();
        endpointService = mock(EndpointService.class);
        cacheService = mock(CacheService.class);
        ReflectionTestUtils.setField(endpointUserService, "endpointService", endpointService);
        ReflectionTestUtils.setField(endpointUserService, "cacheService", cacheService);
    }

//    TODO: move to appropriate place
//    @Test
//    public void attachUserSuccessTest(){
//        UserAttachRequest request = new UserAttachRequest(EXTERNAL_USER_ID, USER_ACCESS_TOKEN);
//        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);
//
//        when(profileMock.getApplicationId()).thenReturn(APPLICATION_ID);
//
//        ApplicationDto appDto = new ApplicationDto();
//        appDto.setTenantId(TEST_TENANT_ID);
//
//        when(applicationService.findAppById(APPLICATION_ID)).thenReturn(appDto);
//
//        when(endpointService.checkAccessToken(appDto, EXTERNAL_USER_ID, USER_ACCESS_TOKEN)).thenReturn(Boolean.TRUE);
//
//        UserAttachResponse response = endpointUserService.attachUser(profileMock, request);
//        assertNotNull(response);
//        assertEquals(SyncStatus.SUCCESS, response.getResult());
//    }
//
//    @Test
//    public void attachUserFailureTest(){
//        UserAttachRequest request = new UserAttachRequest(EXTERNAL_USER_ID, USER_ACCESS_TOKEN);
//        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);
//
//        when(profileMock.getApplicationId()).thenReturn(APPLICATION_ID);
//
//        ApplicationDto appDto = new ApplicationDto();
//        appDto.setTenantId(TEST_TENANT_ID);
//
//        when(applicationService.findAppById(APPLICATION_ID)).thenReturn(appDto);
//
//        when(endpointService.checkAccessToken(appDto, EXTERNAL_USER_ID, USER_ACCESS_TOKEN)).thenReturn(Boolean.FALSE);
//
//        UserAttachResponse response = endpointUserService.attachUser(profileMock, request);
//        assertNotNull(response);
//        assertEquals(SyncStatus.FAILURE, response.getResult());
//    }

    @Test
    public void attachEndpointSuccessTest(){
        EndpointAttachRequest request = new EndpointAttachRequest(REQUEST_ID, ENDPOINT_ACCESS_TOKEN);
        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);
        EndpointProfileDto attachedEndpointMock = mock(EndpointProfileDto.class);

        when(profileMock.getEndpointUserId()).thenReturn(EXTERNAL_USER_ID);
        when(endpointService.attachEndpointToUser(EXTERNAL_USER_ID, ENDPOINT_ACCESS_TOKEN)).thenReturn(attachedEndpointMock);
        when(attachedEndpointMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH);

        EndpointAttachResponse response = endpointUserService.attachEndpoint(profileMock, request);
        assertNotNull(response);
        assertEquals(SyncStatus.SUCCESS, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
    }

    @Test
    public void attachEndpointFailureTest(){
        EndpointAttachRequest request = new EndpointAttachRequest(REQUEST_ID, ENDPOINT_ACCESS_TOKEN);
        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);

        when(profileMock.getEndpointUserId()).thenReturn(null);

        EndpointAttachResponse response = endpointUserService.attachEndpoint(profileMock, request);
        assertNotNull(response);
        assertEquals(SyncStatus.FAILURE, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
    }

    @Test
    public void attachEndpointDBFailureTest(){
        EndpointAttachRequest request = new EndpointAttachRequest(REQUEST_ID, ENDPOINT_ACCESS_TOKEN);
        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);

        when(profileMock.getEndpointUserId()).thenReturn(EXTERNAL_USER_ID);
        when(endpointService.attachEndpointToUser(EXTERNAL_USER_ID, ENDPOINT_ACCESS_TOKEN)).thenThrow(DatabaseProcessingException.class);

        EndpointAttachResponse response = endpointUserService.attachEndpoint(profileMock, request);
        assertNotNull(response);
        assertEquals(SyncStatus.FAILURE, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
    }

    @Test
    public void detachSelfEndpointSuccessTest(){
        EndpointDetachRequest request = new EndpointDetachRequest(REQUEST_ID, Base64Util.encode(ENDPOINT_KEY_HASH));
        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);

        when(profileMock.getEndpointUserId()).thenReturn(EXTERNAL_USER_ID);
        when(profileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH);

        EndpointDetachResponse response = endpointUserService.detachEndpoint(profileMock, request);

        Mockito.verify(endpointService, Mockito.never()).findEndpointProfileByKeyHash(Mockito.any(byte[].class));
        Mockito.verify(endpointService).detachEndpointFromUser(profileMock);

        assertNotNull(response);
        assertEquals(SyncStatus.SUCCESS, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
    }

    @Test
    public void detachEndpointSuccessTest(){
        EndpointDetachRequest request = new EndpointDetachRequest(REQUEST_ID, Base64Util.encode(ENDPOINT_KEY_HASH_OTHER));
        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);
        EndpointProfileDto detachMock = mock(EndpointProfileDto.class);

        when(profileMock.getEndpointUserId()).thenReturn(EXTERNAL_USER_ID);
        when(profileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH);
        when(endpointService.findEndpointProfileByKeyHash(ENDPOINT_KEY_HASH_OTHER)).thenReturn(detachMock);
        when(detachMock.getEndpointUserId()).thenReturn(EXTERNAL_USER_ID);

        EndpointDetachResponse response = endpointUserService.detachEndpoint(profileMock, request);

        Mockito.verify(endpointService).detachEndpointFromUser(detachMock);

        assertNotNull(response);
        assertEquals(SyncStatus.SUCCESS, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
    }

    @Test
    public void detachEndpointInvalidRequestFailureTest(){
        EndpointDetachRequest request = new EndpointDetachRequest(REQUEST_ID, null);
        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);


        EndpointDetachResponse response = endpointUserService.detachEndpoint(profileMock, request);

        assertNotNull(response);
        assertEquals(SyncStatus.FAILURE, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
    }

    @Test
    public void detachEndpointInvalidProfileFailureTest(){
        EndpointDetachRequest request = new EndpointDetachRequest(REQUEST_ID, Base64Util.encode(ENDPOINT_KEY_HASH_OTHER));
        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);


        EndpointDetachResponse response = endpointUserService.detachEndpoint(profileMock, request);

        assertNotNull(response);
        assertEquals(SyncStatus.FAILURE, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
    }

    @Test
    public void detachEndpointInvalidUserIdFailureTest(){
        EndpointDetachRequest request = new EndpointDetachRequest(REQUEST_ID, Base64Util.encode(ENDPOINT_KEY_HASH_OTHER));
        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);
        EndpointProfileDto detachMock = mock(EndpointProfileDto.class);

        when(profileMock.getEndpointUserId()).thenReturn(EXTERNAL_USER_ID);
        when(profileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH);
        when(endpointService.findEndpointProfileByKeyHash(ENDPOINT_KEY_HASH_OTHER)).thenReturn(detachMock);
        when(detachMock.getEndpointUserId()).thenReturn(EXTERNAL_USER_ID_OTHER);

        EndpointDetachResponse response = endpointUserService.detachEndpoint(profileMock, request);

        Mockito.verify(endpointService, Mockito.never()).detachEndpointFromUser(detachMock);

        assertNotNull(response);
        assertEquals(SyncStatus.FAILURE, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
    }

    @Test
    public void detachEndpointDBExceptionFailureTest(){
        EndpointDetachRequest request = new EndpointDetachRequest(REQUEST_ID, Base64Util.encode(ENDPOINT_KEY_HASH_OTHER));
        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);
        EndpointProfileDto detachMock = mock(EndpointProfileDto.class);

        when(profileMock.getEndpointUserId()).thenReturn(EXTERNAL_USER_ID);
        when(profileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH);
        when(endpointService.findEndpointProfileByKeyHash(ENDPOINT_KEY_HASH_OTHER)).thenReturn(detachMock);
        when(detachMock.getEndpointUserId()).thenReturn(EXTERNAL_USER_ID);
        Mockito.doThrow(new DatabaseProcessingException("")).when(endpointService).detachEndpointFromUser(detachMock);

        EndpointDetachResponse response = endpointUserService.detachEndpoint(profileMock, request);

        assertNotNull(response);
        assertEquals(SyncStatus.FAILURE, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
    }

    @Test
    public void getEventListenersTest(){
        EventListenersRequest request = new EventListenersRequest();
        request.setEventClassFQNs(Arrays.asList("fqn2", "fqn3"));
        request.setRequestId(REQUEST_ID);

        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);
        EndpointProfileDto listener1Mock = mock(EndpointProfileDto.class);
        EndpointProfileDto listener2Mock = mock(EndpointProfileDto.class);
        EndpointProfileDto listener3Mock = mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        when(profileMock.getId()).thenReturn("A");
        when(profileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(profileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH);
        when(profileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(listener1Mock.getId()).thenReturn("B");
        when(listener1Mock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH1);
        when(listener1Mock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));
        when(listener1Mock.getApplicationId()).thenReturn(TEST_APP_ID);

        when(listener2Mock.getId()).thenReturn("C");
        when(listener2Mock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH2);
        when(listener2Mock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));
        when(listener2Mock.getApplicationId()).thenReturn(TEST_APP_ID2);

        when(listener3Mock.getId()).thenReturn("D");
        when(listener3Mock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH3);
        when(listener3Mock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));
        when(listener3Mock.getApplicationId()).thenReturn(TEST_APP_ID3);

        when(endpointService.findEndpointProfilesByUserId(USER_ID)).thenReturn(Arrays.asList(profileMock, listener1Mock, listener2Mock, listener3Mock));
        when(cacheService.getTenantIdByAppToken(TEST_APP_TOKEN)).thenReturn(TEST_TENANT_ID);
        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TEST_TENANT_ID, "fqn2"))).thenReturn(ECF1_ID);
        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TEST_TENANT_ID, "fqn3"))).thenReturn(ECF1_ID);

        when(cacheService.getAppSeqNumber(TEST_APP_TOKEN)).thenReturn(new AppSeqNumber(TEST_TENANT_ID, TEST_APP_ID, TEST_APP_TOKEN, TEST_APP_SEQ_NUM));
        when(cacheService.getAppSeqNumber(TEST_APP_TOKEN2)).thenReturn(new AppSeqNumber(TEST_TENANT_ID, TEST_APP_ID2, TEST_APP_TOKEN2, TEST_APP_SEQ_NUM));
        when(cacheService.getAppSeqNumber(TEST_APP_TOKEN3)).thenReturn(new AppSeqNumber(TEST_TENANT_ID, TEST_APP_ID3, TEST_APP_TOKEN3, TEST_APP_SEQ_NUM));

        RouteTableKey key1 = new RouteTableKey(TEST_APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        RouteTableKey key2 = new RouteTableKey(TEST_APP_TOKEN2, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        RouteTableKey key3 = new RouteTableKey(TEST_APP_TOKEN3, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));

        HashSet<RouteTableKey> fqn2Keys = new HashSet<>();
        fqn2Keys.add(key1);
        fqn2Keys.add(key2);
        fqn2Keys.add(key3);
        HashSet<RouteTableKey> fqn3Keys = new HashSet<>();
        fqn3Keys.add(key1);
        fqn3Keys.add(key2);
        //No Key3 for FQN 3
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TEST_TENANT_ID, "fqn2", ECF1_VERSION))).thenReturn(fqn2Keys);
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TEST_TENANT_ID, "fqn3", ECF1_VERSION))).thenReturn(fqn3Keys);

        EventListenersResponse response = endpointUserService.findListeners(profileMock, TEST_APP_TOKEN, request);

        assertNotNull(response);
        assertEquals(SyncStatus.SUCCESS, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
        assertEquals(2, response.getListeners().size());
        assertTrue(response.getListeners().contains(Base64Util.encode(ENDPOINT_KEY_HASH1)));
        assertTrue(response.getListeners().contains(Base64Util.encode(ENDPOINT_KEY_HASH2)));
        assertFalse(response.getListeners().contains(Base64Util.encode(ENDPOINT_KEY_HASH3)));
        assertFalse(response.getListeners().contains(Base64Util.encode(ENDPOINT_KEY_HASH)));
    }

    @Test
    public void getEventListenersFailure1Test(){
        EventListenersRequest request = new EventListenersRequest();
        request.setEventClassFQNs(Arrays.asList("fqn2", "fqn3"));
        request.setRequestId(REQUEST_ID);

        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);

        when(profileMock.getId()).thenReturn("A");
        when(profileMock.getEndpointUserId()).thenReturn(null);

        EventListenersResponse response = endpointUserService.findListeners(profileMock, TEST_APP_TOKEN, request);

        assertNotNull(response);
        assertEquals(SyncStatus.FAILURE, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
    }


    @Test
    public void getEventListenersEmptyTest(){
        EventListenersRequest request = new EventListenersRequest();
        request.setEventClassFQNs(Arrays.asList("fqn2", "fqn3"));
        request.setRequestId(REQUEST_ID);

        EndpointProfileDto profileMock = mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        when(profileMock.getId()).thenReturn("A");
        when(profileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(profileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH);
        when(profileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(endpointService.findEndpointProfilesByUserId(USER_ID)).thenReturn(Arrays.asList(profileMock));

        EventListenersResponse response = endpointUserService.findListeners(profileMock, TEST_APP_TOKEN, request);

        assertNotNull(response);
        assertEquals(SyncStatus.SUCCESS, response.getResult());
        assertEquals(REQUEST_ID, response.getRequestId());
        assertEquals(0, response.getListeners().size());
    }

}
