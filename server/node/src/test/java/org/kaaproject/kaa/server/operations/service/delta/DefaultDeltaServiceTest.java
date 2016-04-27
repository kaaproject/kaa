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

package org.kaaproject.kaa.server.operations.service.delta;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.algorithms.delta.DeltaCalculationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.delta.DeltaCalculatorFactory;
import org.kaaproject.kaa.server.common.core.algorithms.delta.RawBinaryDelta;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.filter.FilterService;
import org.kaaproject.kaa.server.operations.service.profile.ProfileService;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultDeltaServiceTest {

    private static final String DELTA_CALCULATOR_FACTORY = "deltaCalculatorFactory";
    private static final String PROFILE_BODY = "dummy profile body";
    private static final int PROFILE_VERSION = 1;
    private static final int CONF_VERSION = 1;
    private static final String FILTER_SERVICE = "filterService";
    private static final RawBinaryDelta DELTA = new TestRawBinaryDelta("delta");
    private static final String PROFILE_SERVICE = "profileService";
    private static final String CACHE_SERVICE = "cacheService";
    private static final EndpointObjectHash PROFILE_HASH = EndpointObjectHash.fromSHA1("profileHash");
    private static final EndpointObjectHash ENDPOINT_KEY_HASH = EndpointObjectHash.fromSHA1("endpointKey");
    private static final String TEST_APP = "testApp";
    private static final EndpointObjectHash CONFIGURATION_HASH = EndpointObjectHash.fromSHA1("configurationHash");
    private static final String NEW_CONF = "{ \"type\": \"newConf\" }";
    private static final RawBinaryDelta NEW_CONF_DELTA = new TestRawBinaryDelta("{ \"type\": \"newConf\" }");

    private DeltaService deltaService;
    private CacheService cacheService;
    private ProfileService profileService;
    private FilterService filterService;
    private DeltaCalculatorFactory deltaCalculatorFactory;
    private DeltaCalculationAlgorithm deltaCalculator;

    @Before
    public void before() {
        deltaService = new DefaultDeltaService();
        cacheService = mock(CacheService.class);
        profileService = mock(ProfileService.class);
        filterService = mock(FilterService.class);
        deltaCalculatorFactory = mock(DeltaCalculatorFactory.class);
        deltaCalculator = mock(DeltaCalculationAlgorithm.class);

        ReflectionTestUtils.setField(deltaService, CACHE_SERVICE, cacheService);
        ReflectionTestUtils.setField(deltaService, PROFILE_SERVICE, profileService);
        ReflectionTestUtils.setField(deltaService, FILTER_SERVICE, filterService);
        ReflectionTestUtils.setField(deltaService, DELTA_CALCULATOR_FACTORY, deltaCalculatorFactory);
    }

    private List<EndpointGroupStateDto> createGroupList(EndpointGroupStateDto... groups) {
        List<EndpointGroupStateDto> result = new ArrayList<EndpointGroupStateDto>();
        for (EndpointGroupStateDto group : groups) {
            result.add(group);
        }
        return result;
    }

    private List<HistoryDto> createHistoryList(HistoryDto... history) {
        List<HistoryDto> result = new ArrayList<HistoryDto>();
        for (HistoryDto group : history) {
            result.add(group);
        }
        return result;
    }

    private HistoryDto createHistory(ChangeDto changeDto) {
        HistoryDto historyDto = new HistoryDto();
        historyDto.setChange(changeDto);
        return historyDto;
    }

    private ChangeDto createChange(ChangeType changeType, String endpointGroupId, String pfId, int pfVersion,
            String cfId, int cfVersion) {
        ChangeDto change = new ChangeDto();
        change.setType(changeType);
        change.setEndpointGroupId(endpointGroupId);
        change.setProfileFilterId(pfId);
        change.setConfigurationId(cfId);
        change.setCfVersion(cfVersion);
        return change;
    }

    private EndpointProfileDto createDefaultTestProfile(List<EndpointGroupStateDto> oldGroups) {
        EndpointProfileDto profile = new EndpointProfileDto();
        profile.setEndpointKey(ENDPOINT_KEY_HASH.getData());
        profile.setSequenceNumber(1);
        profile.setProfileHash(PROFILE_HASH.getData());
        profile.setConfigurationHash(CONFIGURATION_HASH.getData());
        profile.setClientProfileVersion(0);
        profile.setConfigurationVersion(1);
        profile.setClientProfileBody(PROFILE_BODY);
        profile.setGroupState(oldGroups);
        return profile;
    }

//    @Test
//    public void testAppSeqNumberCache() throws GetDeltaException {
//        when(cacheService.getAppSeqNumber(TEST_APP)).thenReturn(42);
//
//        GetDeltaRequest request = new GetDeltaRequest(TEST_APP, null, null, null, 42);
//        GetDeltaResponse response = deltaService.getDelta(request);
//
//        assertNotNull(response);
//        verify(profileService, times(0)).getClientProfileBody(ENDPOINT_KEY_HASH);
//        assertEquals(GetDeltaResponseType.NO_DELTA, response.getResponseType());
//        assertEquals(null, response.getDelta());
//        assertEquals(request.getSequenceNumber(), response.getSequenceNumber());
//    }
//
//    @Test
//    public void testProfileResync() throws GetDeltaException {
//        when(cacheService.getAppSeqNumber(TEST_APP)).thenReturn(42);
//        EndpointProfileDto profile = new EndpointProfileDto();
//        profile.setEndpointKey(ENDPOINT_KEY_HASH.getData());
//        profile.setSequenceNumber(1);
//        profile.setProfileHash(PROFILE_HASH.getData());
//        profile.setConfigurationHash(CONFIGURATION_HASH.getData());
//        profile.setProfileVersion(1);
//        profile.setConfigurationVersion(1);
//        when(profileService.getClientProfileBody(ENDPOINT_KEY_HASH)).thenReturn(profile);
//
//        GetDeltaRequest request = new GetDeltaRequest(TEST_APP, ENDPOINT_KEY_HASH,
//                EndpointObjectHash.fromSHA1("invalidProfileHash"), null, 41);
//        GetDeltaResponse response = deltaService.getDelta(request);
//
//        assertNotNull(response);
//        verify(profileService, times(1)).getClientProfileBody(ENDPOINT_KEY_HASH);
//        assertEquals(GetDeltaResponseType.GET_PROFILE, response.getResponseType());
//        assertEquals(null, response.getDelta());
//        assertEquals(0, response.getSequenceNumber());
//    }
//
//    @Test
//    public void testConfigurationDelta() throws GetDeltaException {
//        EndpointGroupStateDto oldGroup = new EndpointGroupStateDto("eg1", "pf1", "cf1");
//        EndpointGroupStateDto newGroup = new EndpointGroupStateDto("eg2", "pf2", "cf2");
//        List<EndpointGroupStateDto> oldGroups = createGroupList(oldGroup);
//        List<EndpointGroupStateDto> newGroups = createGroupList(oldGroup, newGroup);
//
//        HistoryKey historyKey = new HistoryKey(TEST_APP, 41, 42, CONF_VERSION, PROFILE_VERSION);
//
//        List<HistoryDto> historyList = createHistoryList(createHistory(createChange(ChangeType.INSERT, "eg2", "pf2",
//                PROFILE_VERSION, "cf2", CONF_VERSION)));
//
//        EndpointProfileDto profile = createDefaultTestProfile(oldGroups);
//
//        when(cacheService.getAppSeqNumber(TEST_APP)).thenReturn(42);
//        when(cacheService.getHistory(historyKey)).thenReturn(historyList);
//        when(filterService.matches(TEST_APP, "pf2", PROFILE_BODY)).thenReturn(true);
//        when(profileService.getClientProfileBody(ENDPOINT_KEY_HASH)).thenReturn(profile);
//
//        when(cacheService.getDelta(Mockito.any(DeltaCacheKey.class), Mockito.any(Computable.class)))
//        .thenReturn(new DeltaCacheEntry(NEW_CONF, DELTA, EndpointObjectHash.fromSHA1("hash")));
//
//
//        GetDeltaRequest request = new GetDeltaRequest(TEST_APP, ENDPOINT_KEY_HASH, PROFILE_HASH, CONFIGURATION_HASH, 41);
//        GetDeltaResponse response = deltaService.getDelta(request);
//
//        assertNotNull(response);
//        verify(profileService, times(1)).getClientProfileBody(ENDPOINT_KEY_HASH);
//        verify(profileService, times(1)).updateProfile(profile, 42, EndpointObjectHash.fromSHA1("hash"));
//
//        assertEquals(GetDeltaResponseType.DELTA, response.getResponseType());
//        assertEquals(DELTA, response.getDelta());
//        assertEquals(42, response.getSequenceNumber());
//    }
//
//    @Test
//    public void testConfigurationDeltaNoCache() throws GetDeltaException, IOException, DeltaCalculatorException {
//        EndpointGroupStateDto oldGroup = new EndpointGroupStateDto("eg1", "pf1", "cf1");
//        EndpointGroupStateDto newGroup = new EndpointGroupStateDto("eg1", "pf1", "cf2");
//        List<EndpointGroupStateDto> oldGroups = createGroupList(oldGroup);
//        List<EndpointGroupStateDto> newGroups = createGroupList(newGroup);
//
//        HistoryKey historyKey = new HistoryKey(TEST_APP, 41, 42, CONF_VERSION, PROFILE_VERSION);
//
//        List<HistoryDto> historyList = createHistoryList(createHistory(createChange(ChangeType.INSERT, "eg1", "pf1",
//                PROFILE_VERSION, "cf2", CONF_VERSION)));
//
//        EndpointProfileDto profile = createDefaultTestProfile(oldGroups);
//
//        when(cacheService.getAppSeqNumber(TEST_APP)).thenReturn(42);
//        when(cacheService.getHistory(historyKey)).thenReturn(historyList);
//        when(profileService.getClientProfileBody(ENDPOINT_KEY_HASH)).thenReturn(profile);
//
//        when(cacheService.getDelta(Mockito.any(DeltaCacheKey.class), Mockito.any(Computable.class)))
//                .thenReturn(new DeltaCacheEntry(NEW_CONF, NEW_CONF_DELTA, EndpointObjectHash.fromSHA1(NEW_CONF)));
//
//        GetDeltaRequest request = new GetDeltaRequest(TEST_APP, ENDPOINT_KEY_HASH, PROFILE_HASH, CONFIGURATION_HASH, 41);
//        GetDeltaResponse response = deltaService.getDelta(request);
//
//        assertNotNull(response);
//        verify(profileService, times(1)).getClientProfileBody(ENDPOINT_KEY_HASH);
//        verify(profileService, times(1)).updateProfile(profile, 42, EndpointObjectHash.fromSHA1(NEW_CONF));
//
//        assertEquals(GetDeltaResponseType.DELTA, response.getResponseType());
//        assertEquals(NEW_CONF_DELTA, response.getDelta());
//        assertEquals(42, response.getSequenceNumber());
//    }
//
//    @Test
//    public void testConfigurationResync() throws GetDeltaException {
//        EndpointGroupStateDto oldGroup = new EndpointGroupStateDto("eg1", "pf1", "cf1");
//        EndpointGroupStateDto newGroup = new EndpointGroupStateDto("eg2", "pf2", "cf2");
//        List<EndpointGroupStateDto> oldGroups = createGroupList(oldGroup);
//        List<EndpointGroupStateDto> newGroups = createGroupList(oldGroup, newGroup);
//
//        HistoryKey historyKey = new HistoryKey(TEST_APP, 41, 42, CONF_VERSION, PROFILE_VERSION);
//
//        List<HistoryDto> historyList = createHistoryList(createHistory(createChange(ChangeType.INSERT, "eg2", "pf2",
//                PROFILE_VERSION, "cf2", CONF_VERSION)));
//
//        EndpointProfileDto profile = createDefaultTestProfile(oldGroups);
//
//        when(cacheService.getAppSeqNumber(TEST_APP)).thenReturn(42);
//        when(cacheService.getHistory(historyKey)).thenReturn(historyList);
//        when(filterService.matches(TEST_APP, "pf2", PROFILE_BODY)).thenReturn(true);
//        when(profileService.getClientProfileBody(ENDPOINT_KEY_HASH)).thenReturn(profile);
//
//        when(cacheService.getDelta(Mockito.any(DeltaCacheKey.class), Mockito.any(Computable.class))).thenReturn(
//                new DeltaCacheEntry(NEW_CONF, NEW_CONF_DELTA, EndpointObjectHash.fromSHA1("hash")));
//
//        GetDeltaRequest request = new GetDeltaRequest(TEST_APP, ENDPOINT_KEY_HASH, PROFILE_HASH,
//                EndpointObjectHash.fromSHA1("invalidConfigurationHash"), 41);
//        GetDeltaResponse response = deltaService.getDelta(request);
//
//        assertNotNull(response);
//        verify(profileService, times(1)).getClientProfileBody(ENDPOINT_KEY_HASH);
//        verify(profileService, times(1)).updateProfile(profile, 42, EndpointObjectHash.fromSHA1("hash"));
//
//        assertEquals(GetDeltaResponseType.CONF_RESYNC, response.getResponseType());
//        assertEquals(NEW_CONF_DELTA, response.getDelta());
//        assertEquals(42, response.getSequenceNumber());
//    }
//
//    @Test
//    public void testConfigurationResyncNoCache() throws GetDeltaException, IOException, DeltaCalculatorException {
//        EndpointGroupStateDto oldGroup = new EndpointGroupStateDto("eg1", "pf1", "cf1");
//        EndpointGroupStateDto newGroup = new EndpointGroupStateDto("eg1", "pf1", "cf2");
//        List<EndpointGroupStateDto> oldGroups = createGroupList(oldGroup);
//        List<EndpointGroupStateDto> newGroups = createGroupList(newGroup);
//
//        HistoryKey historyKey = new HistoryKey(TEST_APP, 41, 42, CONF_VERSION, PROFILE_VERSION);
//
//        List<HistoryDto> historyList = createHistoryList(createHistory(createChange(ChangeType.INSERT, "eg1", "pf1",
//                PROFILE_VERSION, "cf2", CONF_VERSION)));
//
//        EndpointProfileDto profile = createDefaultTestProfile(oldGroups);
//
//        when(cacheService.getAppSeqNumber(TEST_APP)).thenReturn(42);
//        when(cacheService.getHistory(historyKey)).thenReturn(historyList);
//        when(profileService.getClientProfileBody(ENDPOINT_KEY_HASH)).thenReturn(profile);
//
//        when(cacheService.getDelta(Mockito.any(DeltaCacheKey.class), Mockito.any(Computable.class)))
//        .thenReturn(new DeltaCacheEntry(NEW_CONF, NEW_CONF_DELTA, EndpointObjectHash.fromSHA1(NEW_CONF)));
//
//        GetDeltaRequest request = new GetDeltaRequest(TEST_APP, ENDPOINT_KEY_HASH, PROFILE_HASH,
//                EndpointObjectHash.fromSHA1("invalidConfigurationHash"), 41);
//        GetDeltaResponse response = deltaService.getDelta(request);
//
//        assertNotNull(response);
//        verify(profileService, times(1)).getClientProfileBody(ENDPOINT_KEY_HASH);
//        verify(profileService, times(1)).updateProfile(profile, 42, EndpointObjectHash.fromSHA1(NEW_CONF));
//
//        assertEquals(GetDeltaResponseType.CONF_RESYNC, response.getResponseType());
//        assertEquals(NEW_CONF_DELTA, response.getDelta());
//        assertEquals(42, response.getSequenceNumber());
//    }
}
