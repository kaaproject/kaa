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

package org.kaaproject.kaa.server.operations.service.profile;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFamilyIdKey;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class ProfileServiceTest {

    /** The endpoint service. */
    private EndpointService endpointService;

    /** The cache service. */
    private CacheService cacheService;

    private DefaultProfileService testService;

    @Before
    public void before() {
        testService = new DefaultProfileService();
        endpointService = mock(EndpointService.class);
        cacheService = mock(CacheService.class);

        ReflectionTestUtils.setField(testService, "endpointService", endpointService);
        ReflectionTestUtils.setField(testService, "cacheService", cacheService);

    }

    @Test
    public void testPopulateVersionStates() {
        EndpointProfileDto dtoMock = Mockito.mock(EndpointProfileDto.class);

        SdkProfileDto sdkProperties = new SdkProfileDto(null, 1, 2, 3, 4, Collections.EMPTY_LIST, null, null,
                null, null, null);

        ApplicationEventFamilyMapDto applicationEventFamilyMap = new ApplicationEventFamilyMapDto();
        applicationEventFamilyMap.setVersion(7);
        applicationEventFamilyMap.setEcfName("ecf1");
        Mockito.when(cacheService.getApplicationEventFamilyMapsByIds(sdkProperties.getAefMapIds())).
                thenReturn(Arrays.asList(applicationEventFamilyMap));

        EventClassFamilyIdKey key = new EventClassFamilyIdKey("tenantId", "ecf1");
        Mockito.when(cacheService.getEventClassFamilyIdByName(key)).thenReturn("ecf1Id");

        testService.populateVersionStates("tenantId", dtoMock, sdkProperties);

        EventClassFamilyVersionStateDto ecfVersionStateDto = new EventClassFamilyVersionStateDto();
        ecfVersionStateDto.setEcfId("ecf1Id");
        ecfVersionStateDto.setVersion(7);
        Mockito.verify(dtoMock).setEcfVersionStates(Collections.singletonList(ecfVersionStateDto));
    }
}
