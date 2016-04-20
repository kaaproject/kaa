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

package org.kaaproject.kaa.server.operations.service.cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicListEntryDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.ApplicationEventMapService;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.EventClassService;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.SdkProfileService;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.cache.concurrent.ConcurrentCacheService;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;
import org.kaaproject.kaa.server.operations.service.event.EventClassFqnVersion;
import org.kaaproject.kaa.server.operations.service.event.RouteTableKey;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/operations/cache-test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ConcurrentCacheServiceTest extends AbstractTest {
    private static final int EVENT_CLASS_FAMILY_VERSION = 42;

    private static final String EVENT_CLASS_ID = "EVENT_CLASS_ID";

    private static final String EVENT_CLASS_FAMILY_ID = "EVENT_CLASS_FAMILY_ID";
    private static final String ECF_NAME = "ECF_NAME";
    private static final String EC_FQN = "EC_FQN";
    private static final String TENANT_ID = "TENANT_ID";
    private static final int CONF1_SCHEMA_VERSION = 1;
    private static final int PROFILE1_SCHEMA_VERSION = 1;
    private static final int PROFILE1_SERVER_SCHEMA_VERSION = 1;
    private static final int PROFILE2_SCHEMA_VERSION = 2;
    private static final int PROFILE2_SERVER_SCHEMA_VERSION = 2;

    private static final String CF1_ID = "cf1";
    private static final String CF2_ID = "cf2";
    private static final String CF3_ID = "cf3";

    private static final String PF1_ID = "pf1";
    private static final String PF2_ID = "pf2";
    private static final String PF3_ID = "pf3";

    private static final String ENDPOINT_GROUP1_ID = "eg1";

    private static final int STRESS_TEST_N_THREADS = 10;

    private static final int STRESS_TEST_INVOCATIONS = 50;

    private static final int ESTIMATED_METHOD_EXECUTION_TIME = 5;

    private static final String TEST_APP_ID = "testAppId";
    private static final String TEST_APP_TOKEN = "testApp";
    private static final String TEST_SDK_TOKEN = "testSdkToken";
    private static final String APP_ID = "testAppId";
    private static final String DEFAULT_VERIFIER_TOKEN = "defaultVerifierToken";
    private static final int TEST_APP_SEQ_NUMBER = 42;
    private static final int TEST_APP_SEQ_NUMBER_NEW = 46;

    private static final EndpointConfigurationDto CF1 = new EndpointConfigurationDto();
    private static final ConfigurationSchemaDto CF1_SCHEMA = new ConfigurationSchemaDto();
    private static final EndpointProfileSchemaDto PF1_SCHEMA = new EndpointProfileSchemaDto();
    private static final SdkProfileDto SDK_PROFILE = new SdkProfileDto();
    private static final ProfileFilterDto TEST_PROFILE_FILTER = new ProfileFilterDto();
    private static final List<ProfileFilterDto> TEST_PROFILE_FILTER_LIST = Collections.singletonList(TEST_PROFILE_FILTER);

    private static final EndpointObjectHash CF1_HASH = EndpointObjectHash.fromSHA1(CF1_ID);

    private static final EndpointObjectHash TLCE1_HASH = CF1_HASH;
    private static final TopicListEntryDto TLCE1 = new TopicListEntryDto(100, TLCE1_HASH.getData(), null);

    private static final ConfigurationIdKey TEST_CONF_ID_KEY = new ConfigurationIdKey(APP_ID, TEST_APP_SEQ_NUMBER, CONF1_SCHEMA_VERSION,
            ENDPOINT_GROUP1_ID);

    private static final HistoryKey TEST_HISTORY_KEY = new HistoryKey(TEST_APP_TOKEN, TEST_APP_SEQ_NUMBER,
            TEST_APP_SEQ_NUMBER_NEW, CONF1_SCHEMA_VERSION, PROFILE1_SCHEMA_VERSION, PROFILE1_SERVER_SCHEMA_VERSION);

    private static final AppProfileVersionsKey TEST_GET_PROFILES_KEY = new AppProfileVersionsKey(TEST_APP_TOKEN, PROFILE1_SCHEMA_VERSION,
            PROFILE1_SERVER_SCHEMA_VERSION);

    private static final AppVersionKey CF_SCHEMA_KEY = new AppVersionKey(TEST_APP_TOKEN, CONF1_SCHEMA_VERSION);
    private static final AppVersionKey PF_SCHEMA_KEY = new AppVersionKey(TEST_APP_TOKEN, PROFILE1_SCHEMA_VERSION);

    private static final List<String> AEFMAP_IDS = Arrays.asList("id1");
    private static final ApplicationEventFamilyMapDto APPLICATION_EVENT_FAMILY_MAP_DTO = new ApplicationEventFamilyMapDto();
    private static final List<ApplicationEventFamilyMapDto> AEFM_LIST = Arrays.asList(APPLICATION_EVENT_FAMILY_MAP_DTO);

    private PublicKey publicKey;
    private PublicKey publicKey2;
    private EndpointObjectHash publicKeyHash;
    private EndpointObjectHash publicKeyHash2;

    @Autowired
    private CacheService cacheService;
    private ApplicationService appService;
    private ConfigurationService configurationService;
    private HistoryService historyService;
    private ProfileService profileService;
    private EndpointService endpointService;
    private EventClassService eventClassService;
    private ApplicationEventMapService applicationEventMapService;
    private SdkProfileService sdkProfileService;

    @Before
    public void prepare() throws GeneralSecurityException {
        registerMocks();

        final ApplicationDto appDto = new ApplicationDto();
        appDto.setSequenceNumber(TEST_APP_SEQ_NUMBER);
        appDto.setId(APP_ID);
        appDto.setApplicationToken(TEST_APP_TOKEN);
        when(appService.findAppByApplicationToken(TEST_APP_TOKEN)).then(new Answer<ApplicationDto>() {
            @Override
            public ApplicationDto answer(InvocationOnMock invocation) {
                sleepABit();
                return appDto;
            }
        });

        SDK_PROFILE.setApplicationToken(TEST_APP_TOKEN);
        SDK_PROFILE.setApplicationId(APP_ID);
        SDK_PROFILE.setDefaultVerifierToken(DEFAULT_VERIFIER_TOKEN);
        when(sdkProfileService.findSdkProfileByToken(TEST_SDK_TOKEN)).then(new Answer<SdkProfileDto>() {
            @Override
            public SdkProfileDto answer(InvocationOnMock invocationOnMock) throws Throwable {
                sleepABit();
                return SDK_PROFILE;
            }
        });

        final List<ConfigurationDto> configurations = new ArrayList<ConfigurationDto>();
        ConfigurationDto theConf = new ConfigurationDto();
        theConf.setId(CF1_ID);
        theConf.setSchemaVersion(CONF1_SCHEMA_VERSION);
        configurations.add(theConf);
        when(configurationService.findConfigurationsByEndpointGroupId(ENDPOINT_GROUP1_ID)).then(new Answer<List<ConfigurationDto>>() {
            @Override
            public List<ConfigurationDto> answer(InvocationOnMock invocation) throws Throwable {
                sleepABit();
                return configurations;
            }
        });

        final List<HistoryDto> historyList = new ArrayList<HistoryDto>();

        historyList.add(buildMatchingHistoryDto(ChangeType.ADD_CONF));
        historyList.add(buildNotMatchingHistoryDto(ChangeType.ADD_CONF));
        historyList.add(buildMatchingHistoryDto(ChangeType.REMOVE_CONF));
        historyList.add(buildNotMatchingHistoryDto(ChangeType.REMOVE_CONF));
        historyList.add(buildMatchingHistoryDto(ChangeType.ADD_PROF));
        historyList.add(buildNotMatchingHistoryDto(ChangeType.ADD_PROF));
        historyList.add(buildMatchingHistoryDto(ChangeType.REMOVE_PROF));
        historyList.add(buildNotMatchingHistoryDto(ChangeType.REMOVE_PROF));
        historyList.add(buildMatchingHistoryDto(ChangeType.ADD_TOPIC));
        historyList.add(buildMatchingHistoryDto(ChangeType.REMOVE_TOPIC));
        historyList.add(buildMatchingHistoryDto(ChangeType.REMOVE_GROUP));

        when(historyService.findHistoriesBySeqNumberRange(APP_ID, TEST_APP_SEQ_NUMBER, TEST_APP_SEQ_NUMBER_NEW)).then(
                new Answer<List<HistoryDto>>() {
                    @Override
                    public List<HistoryDto> answer(InvocationOnMock invocation) throws Throwable {
                        sleepABit();
                        return historyList;
                    }
                });

        when(
                profileService.findProfileFiltersByAppIdAndVersionsCombination(APP_ID, TEST_GET_PROFILES_KEY.getEndpointProfileSchemaVersion(),
                        TEST_GET_PROFILES_KEY.getServerProfileSchemaVersion())).then(new Answer<List<ProfileFilterDto>>() {
            @Override
            public List<ProfileFilterDto> answer(InvocationOnMock invocation) throws Throwable {
                sleepABit();
                return TEST_PROFILE_FILTER_LIST;
            }
        });

        when(profileService.findProfileFilterById(PF1_ID)).then(new Answer<ProfileFilterDto>() {
            @Override
            public ProfileFilterDto answer(InvocationOnMock invocation) throws Throwable {
                sleepABit();
                return TEST_PROFILE_FILTER;
            }
        });
        when(profileService.findProfileFilterById(PF2_ID)).then(new Answer<ProfileFilterDto>() {
            @Override
            public ProfileFilterDto answer(InvocationOnMock invocation) throws Throwable {
                sleepABit();
                return TEST_PROFILE_FILTER;
            }
        });
        when(profileService.findProfileFilterById(PF3_ID)).then(new Answer<ProfileFilterDto>() {
            @Override
            public ProfileFilterDto answer(InvocationOnMock invocation) throws Throwable {
                sleepABit();
                ProfileFilterDto dto = new ProfileFilterDto();
                dto.setEndpointProfileSchemaId(PROFILE2_SCHEMA_VERSION+"");
                dto.setEndpointProfileSchemaVersion(PROFILE2_SCHEMA_VERSION);
                dto.setServerProfileSchemaId(PROFILE2_SERVER_SCHEMA_VERSION+"");
                dto.setServerProfileSchemaVersion(PROFILE2_SERVER_SCHEMA_VERSION);
                return dto;
            }
        });

        when(endpointService.findEndpointConfigurationByHash(CF1_HASH.getData())).then(new Answer<EndpointConfigurationDto>() {
            @Override
            public EndpointConfigurationDto answer(InvocationOnMock invocation) throws Throwable {
                sleepABit();
                return CF1;
            }
        });

        when(endpointService.findTopicListEntryByHash(TLCE1_HASH.getData())).then(new Answer<TopicListEntryDto>() {
            @Override
            public TopicListEntryDto answer(InvocationOnMock invocation) throws Throwable {
                sleepABit();
                return TLCE1;
            }
        });

        when(configurationService.findConfSchemaByAppIdAndVersion(APP_ID, CF_SCHEMA_KEY.getVersion())).then(
                new Answer<ConfigurationSchemaDto>() {
                    @Override
                    public ConfigurationSchemaDto answer(InvocationOnMock invocation) throws Throwable {
                        sleepABit();
                        return CF1_SCHEMA;
                    }
                });

        when(profileService.findProfileSchemaByAppIdAndVersion(APP_ID, PF_SCHEMA_KEY.getVersion())).then(
                new Answer<EndpointProfileSchemaDto>() {
                    @Override
                    public EndpointProfileSchemaDto answer(InvocationOnMock invocation) throws Throwable {
                        sleepABit();
                        return PF1_SCHEMA;
                    }
                });

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(512, random);

        publicKey = keyGen.genKeyPair().getPublic();
        byte[] key = publicKey.getEncoded();
        publicKeyHash = EndpointObjectHash.fromSHA1(key);

        publicKey2 = keyGen.genKeyPair().getPublic();
        byte[] key2 = publicKey2.getEncoded();
        publicKeyHash2 = EndpointObjectHash.fromSHA1(key2);

        final EndpointProfileDto ep = new EndpointProfileDto();
        ep.setEndpointKey(key);

        when(endpointService.findEndpointProfileByKeyHash(publicKeyHash.getData())).then(new Answer<EndpointProfileDto>() {
            @Override
            public EndpointProfileDto answer(InvocationOnMock invocation) throws Throwable {
                sleepABit();
                return ep;
            }
        });

        final EventClassFamilyDto ecfDto = new EventClassFamilyDto();
        ecfDto.setId(EVENT_CLASS_FAMILY_ID);

        when(eventClassService.findEventClassFamilyByTenantIdAndName(TENANT_ID, ECF_NAME)).then(new Answer<EventClassFamilyDto>() {
            @Override
            public EventClassFamilyDto answer(InvocationOnMock invocation) throws Throwable {
                sleepABit();
                return ecfDto;
            }
        });

        final EventClassDto ecDto = new EventClassDto();
        ecDto.setId(EVENT_CLASS_ID);
        ecDto.setEcfId(EVENT_CLASS_FAMILY_ID);

        final List<EventClassDto> eventClassDtos = new ArrayList<>();
        eventClassDtos.add(ecDto);

        when(eventClassService.findEventClassByTenantIdAndFQN(TENANT_ID, EC_FQN)).then(new Answer<List<EventClassDto>>() {
            @Override
            public List<EventClassDto> answer(InvocationOnMock invocation) throws Throwable {
                sleepABit();
                return eventClassDtos;
            }
        });

        final EventClassDto evcDto = new EventClassDto();
        evcDto.setId(EVENT_CLASS_ID);
        evcDto.setEcfId(EVENT_CLASS_FAMILY_ID);
        evcDto.setVersion(EVENT_CLASS_FAMILY_VERSION);

        when(eventClassService.findEventClassByTenantIdAndFQNAndVersion(TENANT_ID, EC_FQN, EVENT_CLASS_FAMILY_VERSION)).then(
                new Answer<EventClassDto>() {
                    @Override
                    public EventClassDto answer(InvocationOnMock invocation) throws Throwable {
                        sleepABit();
                        return evcDto;
                    }
                });

        when(applicationEventMapService.findByEcfIdAndVersion(EVENT_CLASS_FAMILY_ID, EVENT_CLASS_FAMILY_VERSION)).then(
                new Answer<List<ApplicationEventFamilyMapDto>>() {

                    @Override
                    public List<ApplicationEventFamilyMapDto> answer(InvocationOnMock invocation) throws Throwable {
                        ApplicationEventMapDto matchButSource = new ApplicationEventMapDto();
                        matchButSource.setAction(ApplicationEventAction.SOURCE);
                        matchButSource.setEventClassId(EVENT_CLASS_ID);

                        ApplicationEventMapDto match = new ApplicationEventMapDto();
                        match.setAction(ApplicationEventAction.BOTH);
                        match.setEventClassId(EVENT_CLASS_ID);

                        ApplicationEventFamilyMapDto mapping = new ApplicationEventFamilyMapDto();
                        mapping.setApplicationId(APP_ID);
                        mapping.setEcfId(EVENT_CLASS_FAMILY_ID);
                        mapping.setVersion(EVENT_CLASS_FAMILY_VERSION);
                        mapping.setEventMaps(Arrays.asList(matchButSource, match));
                        return Arrays.asList(mapping);
                    }
                });

        APPLICATION_EVENT_FAMILY_MAP_DTO.setEcfName("someName");
        when(applicationEventMapService.findApplicationEventFamilyMapsByIds(AEFMAP_IDS)).thenReturn(AEFM_LIST);

        when(appService.findAppById(APP_ID)).thenReturn(appDto);
    }

    @Test
    public void testGetAppSeqNumber() throws GetDeltaException {
        assertEquals(new AppSeqNumber(TENANT_ID, TEST_APP_ID, TEST_APP_TOKEN, TEST_APP_SEQ_NUMBER),
                cacheService.getAppSeqNumber(TEST_APP_TOKEN));
        verify(appService, times(1)).findAppByApplicationToken(TEST_APP_TOKEN);
        reset(appService);

        assertEquals(new AppSeqNumber(TENANT_ID, TEST_APP_ID, TEST_APP_TOKEN, TEST_APP_SEQ_NUMBER),
                cacheService.getAppSeqNumber(TEST_APP_TOKEN));
        verify(appService, times(0)).findAppByApplicationToken(TEST_APP_TOKEN);
        reset(appService);
    }

    @Test
    public void testConcurrentGetAppSeqNumberMultipleTimes() throws GetDeltaException {
        for (int i = 0; i < STRESS_TEST_INVOCATIONS; i++) {
            launchCodeInParallelThreads(STRESS_TEST_N_THREADS, new Runnable() {
                @Override
                public void run() {
                    assertEquals(new AppSeqNumber(TENANT_ID, TEST_APP_ID, TEST_APP_TOKEN, TEST_APP_SEQ_NUMBER),
                            cacheService.getAppSeqNumber(TEST_APP_TOKEN));
                }
            });

            verify(appService, atMost(2)).findAppByApplicationToken(TEST_APP_TOKEN);
            reset(appService);
        }
    }

    @Test
    public void testGetConfIdNumber() throws GetDeltaException {
        assertEquals(CF1_ID, cacheService.getConfIdByKey(TEST_CONF_ID_KEY));
        verify(configurationService, times(1)).findConfigurationsByEndpointGroupId(ENDPOINT_GROUP1_ID);
        reset(configurationService);

        assertEquals(CF1_ID, cacheService.getConfIdByKey(TEST_CONF_ID_KEY));
        verify(configurationService, times(0)).findConfigurationsByEndpointGroupId(ENDPOINT_GROUP1_ID);
        reset(configurationService);
    }

    @Test
    public void testGetAppTokenBySdkToken() {
        assertEquals(TEST_APP_TOKEN, cacheService.getAppTokenBySdkToken(TEST_SDK_TOKEN));
        verify(sdkProfileService, times(1)).findSdkProfileByToken(TEST_SDK_TOKEN);
        reset(sdkProfileService);

        assertEquals(TEST_APP_TOKEN, cacheService.getAppTokenBySdkToken(TEST_SDK_TOKEN));
        verify(sdkProfileService, never()).findSdkProfileByToken(TEST_SDK_TOKEN);
        reset(sdkProfileService);
    }

    @Test
    public void testConcurrentGetConfIdMultipleTimes() throws GetDeltaException {
        for (int i = 0; i < STRESS_TEST_INVOCATIONS; i++) {
            launchCodeInParallelThreads(STRESS_TEST_N_THREADS, new Runnable() {
                @Override
                public void run() {
                    assertEquals(CF1_ID, cacheService.getConfIdByKey(TEST_CONF_ID_KEY));
                }
            });

            verify(configurationService, atMost(2)).findConfigurationsByEndpointGroupId(ENDPOINT_GROUP1_ID);
            reset(configurationService);
        }
    }

    @Test
    public void testGetHistory() throws GetDeltaException {
        List<HistoryDto> expectedList = getResultHistoryList();
        assertEquals(expectedList, cacheService.getHistory(TEST_HISTORY_KEY));
        verify(historyService, times(1)).findHistoriesBySeqNumberRange(APP_ID, TEST_APP_SEQ_NUMBER, TEST_APP_SEQ_NUMBER_NEW);
        reset(historyService);

        assertEquals(expectedList, cacheService.getHistory(TEST_HISTORY_KEY));
        verify(historyService, times(0)).findHistoriesBySeqNumberRange(APP_ID, TEST_APP_SEQ_NUMBER, TEST_APP_SEQ_NUMBER_NEW);
        reset(historyService);
    }

    @Test
    public void testConcurrentGetHistoryMultipleTimes() throws GetDeltaException {
        final List<HistoryDto> expectedList = getResultHistoryList();
        for (int i = 0; i < STRESS_TEST_INVOCATIONS; i++) {
            launchCodeInParallelThreads(STRESS_TEST_N_THREADS, new Runnable() {
                @Override
                public void run() {
                    assertEquals(expectedList, cacheService.getHistory(TEST_HISTORY_KEY));
                }
            });

            verify(historyService, atMost(2)).findHistoriesBySeqNumberRange(APP_ID, TEST_APP_SEQ_NUMBER, TEST_APP_SEQ_NUMBER_NEW);
            reset(historyService);
        }
    }

    @Test
    public void testGetFilters() throws GetDeltaException {
        assertEquals(TEST_PROFILE_FILTER_LIST, cacheService.getFilters(TEST_GET_PROFILES_KEY));
        verify(profileService, times(1)).findProfileFiltersByAppIdAndVersionsCombination(APP_ID, TEST_GET_PROFILES_KEY.getEndpointProfileSchemaVersion(),
                TEST_GET_PROFILES_KEY.getServerProfileSchemaVersion());
        reset(profileService);

        assertEquals(TEST_PROFILE_FILTER_LIST, cacheService.getFilters(TEST_GET_PROFILES_KEY));
        verify(profileService, times(0)).findProfileFiltersByAppIdAndVersionsCombination(APP_ID, TEST_GET_PROFILES_KEY.getEndpointProfileSchemaVersion(),
                TEST_GET_PROFILES_KEY.getServerProfileSchemaVersion());
        reset(profileService);
    }

    @Test
    public void testConcurrentGetFiltersMultipleTimes() throws GetDeltaException {
        for (int i = 0; i < STRESS_TEST_INVOCATIONS; i++) {
            launchCodeInParallelThreads(STRESS_TEST_N_THREADS, new Runnable() {
                @Override
                public void run() {
                    assertEquals(TEST_PROFILE_FILTER_LIST, cacheService.getFilters(TEST_GET_PROFILES_KEY));
                }
            });

            verify(profileService, atMost(2)).findProfileFiltersByAppIdAndVersionsCombination(APP_ID, TEST_GET_PROFILES_KEY.getEndpointProfileSchemaVersion(),
                    TEST_GET_PROFILES_KEY.getServerProfileSchemaVersion());
            reset(profileService);
        }
    }

    @Test
    public void testGetFilter() throws GetDeltaException {
        assertEquals(TEST_PROFILE_FILTER, cacheService.getFilter(PF1_ID));
        verify(profileService, times(1)).findProfileFilterById(PF1_ID);
        reset(profileService);

        assertEquals(TEST_PROFILE_FILTER, cacheService.getFilter(PF1_ID));
        verify(profileService, times(0)).findProfileFilterById(PF1_ID);
        reset(profileService);
    }

    @Test
    public void testConcurrentGetFilterMultipleTimes() throws GetDeltaException {
        for (int i = 0; i < STRESS_TEST_INVOCATIONS; i++) {
            launchCodeInParallelThreads(STRESS_TEST_N_THREADS, new Runnable() {
                @Override
                public void run() {
                    assertEquals(TEST_PROFILE_FILTER, cacheService.getFilter(PF1_ID));
                }
            });

            verify(profileService, atMost(2)).findProfileFilterById(PF1_ID);
            reset(profileService);
        }
    }

    @Test
    public void testGetConf() throws GetDeltaException {
        assertEquals(CF1, cacheService.getConfByHash(CF1_HASH));
        verify(endpointService, times(1)).findEndpointConfigurationByHash(CF1_HASH.getData());
        reset(endpointService);

        assertEquals(CF1, cacheService.getConfByHash(CF1_HASH));
        verify(endpointService, times(0)).findEndpointConfigurationByHash(CF1_HASH.getData());
        reset(endpointService);
    }

    @Test
    public void testConcurrentGetConfMultipleTimes() throws GetDeltaException {
        for (int i = 0; i < STRESS_TEST_INVOCATIONS; i++) {
            launchCodeInParallelThreads(STRESS_TEST_N_THREADS, new Runnable() {
                @Override
                public void run() {
                    assertEquals(CF1, cacheService.getConfByHash(CF1_HASH));
                }
            });

            verify(endpointService, atMost(2)).findEndpointConfigurationByHash(CF1_HASH.getData());
            reset(endpointService);
        }
    }

    @Test
    public void testGetConfSchema() throws GetDeltaException {
        assertEquals(CF1_SCHEMA, cacheService.getConfSchemaByAppAndVersion(CF_SCHEMA_KEY));
        verify(configurationService, times(1)).findConfSchemaByAppIdAndVersion(APP_ID, CF_SCHEMA_KEY.getVersion());
        reset(configurationService);

        assertEquals(CF1_SCHEMA, cacheService.getConfSchemaByAppAndVersion(CF_SCHEMA_KEY));
        verify(configurationService, times(0)).findConfSchemaByAppIdAndVersion(APP_ID, CF_SCHEMA_KEY.getVersion());
        reset(configurationService);
    }

    @Test
    public void testConcurrentGetConfSchemaMultipleTimes() throws GetDeltaException {
        for (int i = 0; i < STRESS_TEST_INVOCATIONS; i++) {
            launchCodeInParallelThreads(STRESS_TEST_N_THREADS, new Runnable() {
                @Override
                public void run() {
                    assertEquals(CF1_SCHEMA, cacheService.getConfSchemaByAppAndVersion(CF_SCHEMA_KEY));
                }
            });

            verify(configurationService, atMost(2)).findConfigurationByAppIdAndVersion(APP_ID, CF_SCHEMA_KEY.getVersion());
            reset(configurationService);
        }
    }

    @Test
    public void testGetProfileSchema() throws GetDeltaException {
        assertEquals(PF1_SCHEMA, cacheService.getProfileSchemaByAppAndVersion(PF_SCHEMA_KEY));
        verify(profileService, times(1)).findProfileSchemaByAppIdAndVersion(APP_ID, PF_SCHEMA_KEY.getVersion());
        reset(profileService);

        assertEquals(PF1_SCHEMA, cacheService.getProfileSchemaByAppAndVersion(PF_SCHEMA_KEY));
        verify(profileService, times(0)).findProfileSchemaByAppIdAndVersion(APP_ID, PF_SCHEMA_KEY.getVersion());
        reset(profileService);
    }

    @Test
    public void testGetSdkProperties() {
        assertEquals(SDK_PROFILE, cacheService.getSdkProfileBySdkToken(TEST_SDK_TOKEN));
        verify(sdkProfileService, times(1)).findSdkProfileByToken(TEST_SDK_TOKEN);
        reset(sdkProfileService);

        assertEquals(SDK_PROFILE, cacheService.getSdkProfileBySdkToken(TEST_SDK_TOKEN));
        verify(sdkProfileService, times(0)).findSdkProfileByToken(TEST_SDK_TOKEN);
        reset(sdkProfileService);
    }

    @Test
    public void testGetApplicationEventFamilyMapsByIds() {
        assertEquals(AEFM_LIST, cacheService.getApplicationEventFamilyMapsByIds(AEFMAP_IDS));
        verify(applicationEventMapService, times(1)).findApplicationEventFamilyMapsByIds(AEFMAP_IDS);
        reset(applicationEventMapService);

        assertEquals(AEFM_LIST, cacheService.getApplicationEventFamilyMapsByIds(AEFMAP_IDS));
        verify(applicationEventMapService, times(0)).findApplicationEventFamilyMapsByIds(AEFMAP_IDS);
        reset(applicationEventMapService);
    }

    @Test
    public void testConcurrentGetProfileSchemaMultipleTimes() throws GetDeltaException {
        for (int i = 0; i < STRESS_TEST_INVOCATIONS; i++) {
            launchCodeInParallelThreads(STRESS_TEST_N_THREADS, new Runnable() {
                @Override
                public void run() {
                    assertEquals(PF1_SCHEMA, cacheService.getProfileSchemaByAppAndVersion(PF_SCHEMA_KEY));
                }
            });

            verify(profileService, atMost(2)).findProfileSchemaByAppIdAndVersion(APP_ID, PF_SCHEMA_KEY.getVersion());
            reset(profileService);
        }
    }

    @Test
    public void testGetEndpointKey() throws GetDeltaException {
        assertEquals(publicKey, cacheService.getEndpointKey(publicKeyHash));
        verify(endpointService, times(1)).findEndpointProfileByKeyHash(publicKeyHash.getData());
        reset(endpointService);

        assertEquals(publicKey, cacheService.getEndpointKey(publicKeyHash));
        verify(endpointService, times(0)).findEndpointProfileByKeyHash(publicKeyHash.getData());
        reset(endpointService);

        assertNull(cacheService.getEndpointKey(publicKeyHash2));
        verify(endpointService, times(1)).findEndpointProfileByKeyHash(publicKeyHash2.getData());
        reset(endpointService);

        final EndpointProfileDto ep2 = new EndpointProfileDto();
        ep2.setEndpointKey(publicKey2.getEncoded());

        when(endpointService.findEndpointProfileByKeyHash(publicKeyHash2.getData())).then(new Answer<EndpointProfileDto>() {
            @Override
            public EndpointProfileDto answer(InvocationOnMock invocation) throws Throwable {
                sleepABit();
                return ep2;
            }
        });

        cacheService.putEndpointKey(publicKeyHash2, publicKey2);
        assertEquals(publicKey2, cacheService.getEndpointKey(publicKeyHash2));
        verify(endpointService, times(0)).findEndpointProfileByKeyHash(publicKeyHash2.getData());
        reset(endpointService);
    }

    @Test
    public void testConcurrentGetEndpointMultipleTimes() throws GetDeltaException {
        for (int i = 0; i < STRESS_TEST_INVOCATIONS; i++) {
            launchCodeInParallelThreads(STRESS_TEST_N_THREADS, new Runnable() {
                @Override
                public void run() {
                    assertEquals(publicKey, cacheService.getEndpointKey(publicKeyHash));
                }
            });

            verify(endpointService, atMost(2)).findEndpointProfileByKeyHash(publicKeyHash.getData());
            reset(endpointService);
        }
    }

    @Test
    public void testGetEcfIdByName() throws GetDeltaException {
        assertEquals(EVENT_CLASS_FAMILY_ID, cacheService.getEventClassFamilyIdByName(new EventClassFamilyIdKey(TENANT_ID, ECF_NAME)));
        verify(eventClassService, times(1)).findEventClassFamilyByTenantIdAndName(TENANT_ID, ECF_NAME);
        reset(eventClassService);

        assertEquals(EVENT_CLASS_FAMILY_ID, cacheService.getEventClassFamilyIdByName(new EventClassFamilyIdKey(TENANT_ID, ECF_NAME)));
        verify(eventClassService, times(0)).findEventClassFamilyByTenantIdAndName(TENANT_ID, ECF_NAME);
        reset(eventClassService);
    }

    @Test
    public void testGetEcfIdByFqn() throws GetDeltaException {
        assertEquals(EVENT_CLASS_FAMILY_ID, cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, EC_FQN)));
        verify(eventClassService, times(1)).findEventClassByTenantIdAndFQN(TENANT_ID, EC_FQN);
        reset(eventClassService);

        assertEquals(EVENT_CLASS_FAMILY_ID, cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, EC_FQN)));
        verify(eventClassService, times(0)).findEventClassByTenantIdAndFQN(TENANT_ID, EC_FQN);
        reset(eventClassService);
    }

    @Test
    public void testGetRouteKeys() throws GetDeltaException {
        assertEquals(Collections.singleton(new RouteTableKey(TEST_APP_TOKEN, new EventClassFamilyVersion(EVENT_CLASS_FAMILY_ID,
                EVENT_CLASS_FAMILY_VERSION))), cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, EC_FQN,
                EVENT_CLASS_FAMILY_VERSION)));
        verify(eventClassService, times(1)).findEventClassByTenantIdAndFQNAndVersion(TENANT_ID, EC_FQN, EVENT_CLASS_FAMILY_VERSION);
        reset(eventClassService);

        assertEquals(Collections.singleton(new RouteTableKey(TEST_APP_TOKEN, new EventClassFamilyVersion(EVENT_CLASS_FAMILY_ID,
                EVENT_CLASS_FAMILY_VERSION))), cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, EC_FQN,
                EVENT_CLASS_FAMILY_VERSION)));
        verify(eventClassService, times(0)).findEventClassByTenantIdAndFQNAndVersion(TENANT_ID, EC_FQN, EVENT_CLASS_FAMILY_VERSION);
        reset(eventClassService);
    }

    @Test
    public void testIsSupported() {
        for (ChangeType change : ChangeType.values()) {
            switch (change) {
            case ADD_CONF:
            case ADD_PROF:
            case ADD_TOPIC:
            case REMOVE_CONF:
            case REMOVE_PROF:
            case REMOVE_TOPIC:
            case REMOVE_GROUP:
                Assert.assertTrue(ConcurrentCacheService.isSupported(change));
                break;
            default:
                Assert.assertFalse(ConcurrentCacheService.isSupported(change));
                break;
            }
        }
    }

    @Test
    public void testGetTopicListByHash() throws GetDeltaException {
        TopicListCacheEntry entry = cacheService.getTopicListByHash(CF1_HASH);
        assertNotNull(entry);
        verify(endpointService, times(1)).findTopicListEntryByHash(CF1_HASH.getData());
        reset(endpointService);

        entry = cacheService.getTopicListByHash(CF1_HASH);
        assertNotNull(entry);
        verify(endpointService, times(0)).findTopicListEntryByHash(CF1_HASH.getData());
        reset(endpointService);
    }

    private void registerMocks() {
        appService = mock(ApplicationService.class);
        configurationService = mock(ConfigurationService.class);
        historyService = mock(HistoryService.class);
        profileService = mock(ProfileService.class);
        endpointService = mock(EndpointService.class);
        eventClassService = mock(EventClassService.class);
        applicationEventMapService = mock(ApplicationEventMapService.class);
        sdkProfileService = mock(SdkProfileService.class);

        ReflectionTestUtils.invokeMethod(cacheService, "setApplicationService", appService);
        ReflectionTestUtils.invokeMethod(cacheService, "setConfigurationService", configurationService);
        ReflectionTestUtils.invokeMethod(cacheService, "setHistoryService", historyService);
        ReflectionTestUtils.invokeMethod(cacheService, "setProfileService", profileService);
        ReflectionTestUtils.invokeMethod(cacheService, "setEndpointService", endpointService);
        ReflectionTestUtils.invokeMethod(cacheService, "setEventClassService", eventClassService);
        ReflectionTestUtils.invokeMethod(cacheService, "setApplicationEventMapService", applicationEventMapService);
        ReflectionTestUtils.invokeMethod(cacheService, "setSdkProfileService", sdkProfileService);
    }

    private HistoryDto buildNotMatchingHistoryDto(ChangeType changeType) {
        HistoryDto notMatchingHistory = new HistoryDto();
        ChangeDto notMatchingConfChange;
        notMatchingConfChange = new ChangeDto();
        notMatchingHistory.setApplicationId(APP_ID);
        notMatchingHistory.setSequenceNumber(TEST_APP_SEQ_NUMBER + 2);
        notMatchingConfChange.setType(changeType);
        notMatchingConfChange.setEndpointGroupId(ENDPOINT_GROUP1_ID);
        notMatchingConfChange.setConfigurationId(CF3_ID);
        notMatchingConfChange.setCfVersion(CONF1_SCHEMA_VERSION + 1);
        notMatchingConfChange.setProfileFilterId(PF3_ID);
        notMatchingHistory.setChange(notMatchingConfChange);
        return notMatchingHistory;
    }

    private HistoryDto buildMatchingHistoryDto(ChangeType changeType) {
        HistoryDto matchingHistory;
        ChangeDto matchingConfChange;
        matchingHistory = new HistoryDto();
        matchingConfChange = new ChangeDto();
        matchingHistory.setApplicationId(APP_ID);
        matchingHistory.setSequenceNumber(TEST_APP_SEQ_NUMBER + 1);
        matchingConfChange.setType(changeType);
        matchingConfChange.setEndpointGroupId(ENDPOINT_GROUP1_ID);
        matchingConfChange.setConfigurationId(CF2_ID);
        matchingConfChange.setCfVersion(CONF1_SCHEMA_VERSION);
        matchingConfChange.setProfileFilterId(PF2_ID);
        matchingHistory.setChange(matchingConfChange);
        return matchingHistory;
    }

    private List<HistoryDto> getResultHistoryList() {
        List<HistoryDto> expectedList = new ArrayList<>();
        expectedList.add(buildMatchingHistoryDto(ChangeType.ADD_CONF));
        expectedList.add(buildMatchingHistoryDto(ChangeType.REMOVE_CONF));
        expectedList.add(buildMatchingHistoryDto(ChangeType.ADD_PROF));
        expectedList.add(buildMatchingHistoryDto(ChangeType.REMOVE_PROF));
        expectedList.add(buildMatchingHistoryDto(ChangeType.ADD_TOPIC));
        expectedList.add(buildMatchingHistoryDto(ChangeType.REMOVE_TOPIC));
        expectedList.add(buildMatchingHistoryDto(ChangeType.REMOVE_GROUP));
        return expectedList;
    }

    private void sleepABit() {
        try {
            Thread.sleep(ESTIMATED_METHOD_EXECUTION_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void launchCodeInParallelThreads(final int nThreads, final Runnable task) {
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch endGate = new CountDownLatch(nThreads);

        for (int i = 0; i < nThreads; i++) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        startGate.await();
                        try {
                            task.run();
                        } finally {
                            endGate.countDown();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }

        startGate.countDown();
        try {
            endGate.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
