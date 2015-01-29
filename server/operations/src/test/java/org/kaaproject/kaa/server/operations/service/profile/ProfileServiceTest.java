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
package org.kaaproject.kaa.server.operations.service.profile;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFamilyIdKey;
import org.kaaproject.kaa.server.sync.EndpointVersionInfo;
import org.kaaproject.kaa.server.sync.EventClassFamilyVersionInfo;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

public class ProfileServiceTest {

    /** The application service. */
    @Autowired
    private ApplicationService applicationService;

    /** The endpoint service. */
    @Autowired
    private EndpointService endpointService;

    /** The profile service. */
    @Autowired
    private org.kaaproject.kaa.server.common.dao.ProfileService profileService;

    /** The endpoint service. */
    @Autowired
    private CacheService cacheService;

    private DefaultProfileService testService;

    @Before
    public void before() {
        testService = new DefaultProfileService();
        applicationService = mock(ApplicationService.class);
        endpointService = mock(EndpointService.class);
        profileService = mock(org.kaaproject.kaa.server.common.dao.ProfileService.class);
        cacheService = mock(CacheService.class);

        ReflectionTestUtils.setField(testService, "applicationService", applicationService);
        ReflectionTestUtils.setField(testService, "endpointService", endpointService);
        ReflectionTestUtils.setField(testService, "profileService", profileService);
        ReflectionTestUtils.setField(testService, "cacheService", cacheService);

    }

    @Test
    public void testPopulateVersionStates() {
        EventClassFamilyVersionInfo ecf1 = new EventClassFamilyVersionInfo("ecf1", 7);
        EventClassFamilyVersionInfo ecf2 = new EventClassFamilyVersionInfo("ecf2", 8);
        EndpointVersionInfo evInfo = new EndpointVersionInfo(1, 2, 3, 4, Arrays.asList(ecf1, ecf2), 5);
        EndpointProfileDto dtoMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyIdKey key = new EventClassFamilyIdKey("tenantId", "ecf1");
        Mockito.when(cacheService.getEventClassFamilyIdByName(key)).thenReturn("ecf1Id");

        testService.populateVersionStates("tenantId", dtoMock, evInfo);

        EventClassFamilyVersionStateDto ecfVersionStateDto = new EventClassFamilyVersionStateDto();
        ecfVersionStateDto.setEcfId("ecf1Id");
        ecfVersionStateDto.setVersion(7);
        Mockito.verify(dtoMock).setEcfVersionStates(Collections.singletonList(ecfVersionStateDto));
    }
}
