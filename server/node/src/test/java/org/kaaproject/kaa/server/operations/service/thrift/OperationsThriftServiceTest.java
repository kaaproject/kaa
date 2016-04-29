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

package org.kaaproject.kaa.server.operations.service.thrift;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.cache.AppProfileVersionsKey;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class OperationsThriftServiceTest {

    private static final int PF_VERSION = 3;
    OperationsThriftService.Iface operationsThriftService;
    //mocks
    private AkkaService akkaService;
    private CacheService cacheService;
    private ApplicationService applicationService;

    private static final String TEST_TENANT_ID = "testTenantId";
    private static final String TEST_APP_ID = "testAppId";
    private static final String TEST_APP_TOKEN = "testApp";
    private static final String TEST_PF_ID = "pfID";
    private static final String TEST_PF_ENDPOINT_SCHEMA_ID = "epPfSchemaId";
    private static final String TEST_PF_SERVER_SCHEMA_ID = "serverPfSchemaId";
    private static final Integer TEST_PF_ENDPOINT_SCHEMA_VERSION = 42;
    private static final Integer TEST_PF_SERVER_SCHEMA_VERSION = 73;
    private static final int TEST_APP_SEQ_NUMBER = 42;


    @Before
    public void before(){
        operationsThriftService = new OperationsThriftServiceImpl();
        cacheService = mock(CacheService.class);
        akkaService = mock(AkkaService.class);
        applicationService = mock(ApplicationService.class);

        ReflectionTestUtils.setField(operationsThriftService, "cacheService", cacheService);
        ReflectionTestUtils.setField(operationsThriftService, "akkaService", akkaService);
        ReflectionTestUtils.setField(operationsThriftService, "applicationService", applicationService);
    }

    @Test
    public void testSimpleFlowWithZeroAppSeqNumber() throws TException{
        Notification notification = new Notification();
        notification.setAppId(TEST_APP_ID);
        notification.setProfileFilterId(TEST_PF_ID);
        notification.setAppSeqNumber(0);

        ApplicationDto appDto = new ApplicationDto();
        appDto.setId(TEST_APP_ID);
        appDto.setApplicationToken(TEST_APP_TOKEN);

        ProfileFilterDto pfDto = new ProfileFilterDto();
        pfDto.setEndpointProfileSchemaId(TEST_PF_ENDPOINT_SCHEMA_ID);
        pfDto.setEndpointProfileSchemaVersion(TEST_PF_ENDPOINT_SCHEMA_VERSION);
        pfDto.setServerProfileSchemaId(TEST_PF_SERVER_SCHEMA_ID);
        pfDto.setServerProfileSchemaVersion(TEST_PF_SERVER_SCHEMA_VERSION);

        Mockito.when(applicationService.findAppById(TEST_APP_ID)).thenReturn(appDto);
        Mockito.when(cacheService.getFilter(TEST_PF_ID)).thenReturn(pfDto);
        operationsThriftService.onNotification(notification);
        Mockito.verify(applicationService).findAppById(TEST_APP_ID);
        Mockito.verify(cacheService).getFilter(TEST_PF_ID);
        Mockito.verify(cacheService).resetFilters(new AppProfileVersionsKey(TEST_APP_TOKEN, TEST_PF_ENDPOINT_SCHEMA_VERSION, TEST_PF_SERVER_SCHEMA_VERSION));
        //Due to notification.setAppSeqNumber(0);
        Mockito.verify(cacheService, Mockito.times(0)).putAppSeqNumber(Mockito.anyString(), Mockito.any(AppSeqNumber.class));

        Mockito.verify(akkaService).onNotification(notification);
    }

    @Test
    public void testSimpleFlowWithNotZeroAppSeqNumber() throws TException{
        Notification notification = new Notification();
        notification.setAppId(TEST_APP_ID);
        notification.setProfileFilterId(TEST_PF_ID);
        notification.setAppSeqNumber(TEST_APP_SEQ_NUMBER);

        ApplicationDto appDto = new ApplicationDto();
        appDto.setId(TEST_APP_ID);
        appDto.setApplicationToken(TEST_APP_TOKEN);

        ProfileFilterDto pfDto = new ProfileFilterDto();
        pfDto.setEndpointProfileSchemaId(TEST_PF_ENDPOINT_SCHEMA_ID);
        pfDto.setEndpointProfileSchemaVersion(TEST_PF_ENDPOINT_SCHEMA_VERSION);
        pfDto.setServerProfileSchemaId(TEST_PF_SERVER_SCHEMA_ID);
        pfDto.setServerProfileSchemaVersion(TEST_PF_SERVER_SCHEMA_VERSION);

        Mockito.when(applicationService.findAppById(TEST_APP_ID)).thenReturn(appDto);
        Mockito.when(cacheService.getAppSeqNumber(TEST_APP_TOKEN)).thenReturn(new AppSeqNumber(TEST_TENANT_ID, TEST_APP_ID, TEST_APP_TOKEN, 0));
        Mockito.when(cacheService.getFilter(TEST_PF_ID)).thenReturn(pfDto);
        operationsThriftService.onNotification(notification);
        Mockito.verify(applicationService).findAppById(TEST_APP_ID);
        Mockito.verify(cacheService).getFilter(TEST_PF_ID);
        Mockito.verify(cacheService).resetFilters(new AppProfileVersionsKey(TEST_APP_TOKEN, TEST_PF_ENDPOINT_SCHEMA_VERSION, TEST_PF_SERVER_SCHEMA_VERSION));
        //Due to notification.setAppSeqNumber(TEST_APP_SEQ_NUMBER);
        Mockito.verify(cacheService, Mockito.times(1)).putAppSeqNumber(TEST_APP_TOKEN, new AppSeqNumber(TEST_TENANT_ID, TEST_APP_ID, TEST_APP_TOKEN, TEST_APP_SEQ_NUMBER));
        Mockito.verify(akkaService).onNotification(notification);
    }

    @Test
    public void testAppNotFound() throws TException{
        Notification notification = new Notification();
        notification.setAppId(TEST_APP_ID);
        notification.setProfileFilterId(TEST_PF_ID);
        notification.setAppSeqNumber(TEST_APP_SEQ_NUMBER);

        ProfileFilterDto pfDto = new ProfileFilterDto();
        pfDto.setEndpointProfileSchemaId(TEST_PF_ENDPOINT_SCHEMA_ID);
        pfDto.setEndpointProfileSchemaVersion(TEST_PF_ENDPOINT_SCHEMA_VERSION);
        pfDto.setServerProfileSchemaId(TEST_PF_SERVER_SCHEMA_ID);
        pfDto.setServerProfileSchemaVersion(TEST_PF_SERVER_SCHEMA_VERSION);

        Mockito.when(applicationService.findAppById(TEST_APP_ID)).thenReturn(null);
        operationsThriftService.onNotification(notification);
        Mockito.verify(applicationService).findAppById(TEST_APP_ID);
        Mockito.verify(cacheService, Mockito.times(0)).getFilter(Mockito.anyString());
        Mockito.verify(cacheService, Mockito.times(0)).resetFilters(Mockito.any(AppProfileVersionsKey.class));
        Mockito.verify(cacheService, Mockito.times(0)).putAppSeqNumber(Mockito.anyString(), Mockito.any(AppSeqNumber.class));
        Mockito.verify(akkaService).onNotification(notification);
    }

    @Test
    public void testSetRedirectionRule() throws TException{
        RedirectionRule redirectionRule = new RedirectionRule();
        operationsThriftService.setRedirectionRule(redirectionRule);
        Mockito.verify(akkaService, atLeastOnce()).onRedirectionRule(redirectionRule);
    }
    
}
