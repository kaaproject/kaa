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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.server.common.dao.exception.KaaOptimisticLockingFailureException;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointProfileCassandraDaoTest extends AbstractCassandraTest {

    private static final String TEST_APPID = "1";
    private static final String TEST_LIMIT = "3";
    private static final String TEST_OFFSET = "0";
    private static final int GENERATED_PROFILES_COUNT = 5;

    private static final Logger LOG = LoggerFactory.getLogger(EndpointProfileCassandraDaoTest.class);

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);

    @Test
    public void testFindByEndpointGroupId() throws Exception {
        PageLinkDto pageLink = getPageLinkDto();
        EndpointProfilesPageDto found = endpointProfileDao.findByEndpointGroupId(pageLink);
        Assert.assertFalse(found.getEndpointProfiles().isEmpty());
        int lim = Integer.valueOf(TEST_LIMIT);
        Assert.assertEquals(lim, found.getEndpointProfiles().size());
        pageLink.setApplicationId(TEST_APPID);
        EndpointProfilesPageDto foundbyAppId = endpointProfileDao.findByEndpointGroupId(pageLink);
        Assert.assertFalse(foundbyAppId.getEndpointProfiles().isEmpty());
        Assert.assertEquals(lim, foundbyAppId.getEndpointProfiles().size());
    }

    @Test
    public void testFindByEndpointGroupIdWithNfGroupState() throws Exception {
        PageLinkDto pageLink = getPageLinkDto();
        EndpointProfilesPageDto found = endpointProfileDao.findByEndpointGroupId(pageLink);
        Assert.assertFalse(found.getEndpointProfiles().isEmpty());
        int lim = Integer.valueOf(TEST_LIMIT);
        Assert.assertEquals(lim, found.getEndpointProfiles().size());
        pageLink.setApplicationId(TEST_APPID);
        EndpointProfilesPageDto foundbyAppId = endpointProfileDao.findByEndpointGroupId(pageLink);
        Assert.assertFalse(foundbyAppId.getEndpointProfiles().isEmpty());
        Assert.assertEquals(lim, foundbyAppId.getEndpointProfiles().size());
    }

    @Test
    public void testFindBodyByEndpointGroupId() throws Exception {
        PageLinkDto pageLink = getPageLinkDto();
        EndpointProfilesBodyDto found = endpointProfileDao.findBodyByEndpointGroupId(pageLink);
        Assert.assertFalse(found.getEndpointProfilesBody().isEmpty());
        int lim = Integer.valueOf(TEST_LIMIT);
        Assert.assertEquals(lim, found.getEndpointProfilesBody().size());
        pageLink.setApplicationId(TEST_APPID);
        EndpointProfilesBodyDto foundbyAppId = endpointProfileDao.findBodyByEndpointGroupId(pageLink);
        Assert.assertFalse(foundbyAppId.getEndpointProfilesBody().isEmpty());
        Assert.assertEquals(lim, foundbyAppId.getEndpointProfilesBody().size());
    }

    @Test
    public void testFindBodyByEndpointGroupIdWithNfGroupState() throws Exception {
        PageLinkDto pageLink = getPageLinkDto();
        EndpointProfilesBodyDto found = endpointProfileDao.findBodyByEndpointGroupId(pageLink);
        Assert.assertFalse(found.getEndpointProfilesBody().isEmpty());
        int lim = Integer.valueOf(TEST_LIMIT);
        Assert.assertEquals(lim, found.getEndpointProfilesBody().size());
        pageLink.setApplicationId(TEST_APPID);
        EndpointProfilesBodyDto foundbyAppId = endpointProfileDao.findBodyByEndpointGroupId(pageLink);
        Assert.assertFalse(foundbyAppId.getEndpointProfilesBody().isEmpty());
        Assert.assertEquals(lim, foundbyAppId.getEndpointProfilesBody().size());
    }

    private PageLinkDto getPageLinkDto() {
        List<EndpointProfileDto> endpointProfileList = new ArrayList<>();
        for (int i = 0; i < GENERATED_PROFILES_COUNT; i++) {
            endpointProfileList.add(generateEndpointProfileWithEndpointGroupId(TEST_APPID));
        }
        String id = endpointProfileList.get(0).getGroupState().get(0).getEndpointGroupId();
        return new PageLinkDto(id, TEST_LIMIT, TEST_OFFSET);
    }

    @Test
    public void testFindBodyByKeyHash() throws Exception {
        EndpointProfileDto expected = generateEndpointProfileWithEndpointGroupId(null);
        EndpointProfileBodyDto found = endpointProfileDao.findBodyByKeyHash(expected.getEndpointKeyHash());
        Assert.assertFalse(found.getClientSideProfile().isEmpty());
        Assert.assertFalse(found.getServerSideProfile().isEmpty());
        Assert.assertEquals(expected.getClientProfileBody(), found.getClientSideProfile());
        Assert.assertEquals(expected.getServerProfileBody(), found.getServerSideProfile());
    }

    @Test
    public void testUpdate() throws Exception {
        List<EndpointGroupStateDto> cfGroupStateSave = new ArrayList<>();
        List<EndpointGroupStateDto> cfGroupStateUpdate = new ArrayList<>();
        PageLinkDto pageLink;
        EndpointProfilesPageDto found;
        String endpointProfileId = "11";
        EndpointGroupDto endpointGroupDto = new EndpointGroupDto();
        endpointGroupDto.setWeight(1);
        cfGroupStateSave.add(new EndpointGroupStateDto("111", null, null));
        cfGroupStateSave.add(new EndpointGroupStateDto("222", null, null));
        cfGroupStateSave.add(new EndpointGroupStateDto("333", null, null));
        byte[] keyHash = generateBytes();
        EndpointProfileDto endpointProfileSave = generateEndpointProfileForTestUpdate(null, keyHash, cfGroupStateSave);
        EndpointProfile saved = endpointProfileDao.save(endpointProfileSave);
        cfGroupStateUpdate.add(new EndpointGroupStateDto("111", null, null));
        cfGroupStateUpdate.add(new EndpointGroupStateDto("444", null, null));
        EndpointProfileDto endpointProfileUpdate = generateEndpointProfileForTestUpdate(endpointProfileId, keyHash, cfGroupStateUpdate);
        endpointProfileUpdate.setVersion(saved.getVersion());
        saved = endpointProfileDao.save(endpointProfileUpdate);
        String limit = "10";
        String offset = "0";
        String[] endpointGroupId = {"111", "444", "222", "333"};
        for (int i = 0; i < 2; i++) {
            pageLink = new PageLinkDto(endpointGroupId[i], limit, offset);
            found = endpointProfileDao.findByEndpointGroupId(pageLink);
            Assert.assertFalse(found.getEndpointProfiles().isEmpty());
        }
        for (int i = 2; i < 4; i++) {
            pageLink = new PageLinkDto(endpointGroupId[i], limit, offset);
            found = endpointProfileDao.findByEndpointGroupId(pageLink);
            Assert.assertTrue(found.getEndpointProfiles().isEmpty());
        }
    }

    @Test
    public void testSave() throws Exception {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null, null, null);
        EndpointProfile found = endpointProfileDao.findByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertEquals(endpointProfile, found.toDto());
    }

    @Test
    public void testFindByKeyHash() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null, null);
        EndpointProfile found = endpointProfileDao.findByKeyHash(expected.getEndpointKeyHash());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testFindEndpointIdByKeyHash() throws Exception {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null, null, null);
        EndpointProfile ep = endpointProfileDao.findEndpointIdByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertEquals(endpointProfile.getId(), ep.getId());
        Assert.assertNull(endpointProfile.getEndpointKey());
        Assert.assertNull(ep.getEndpointKey());
        Assert.assertNull(ep.getEndpointUserId());
        Assert.assertNull(ep.getSubscriptions());
    }

    @Test(expected = KaaOptimisticLockingFailureException.class)
    public void testOptimisticLockWithConcurrency() throws Throwable {
        final EndpointProfileDto endpointProfile = generateEndpointProfile(null, null, null, null);
        List<Future<?>> tasks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int id = i;
            tasks.add(EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        CassandraEndpointProfile ep = new CassandraEndpointProfile(endpointProfile);
                        ep.setEndpointUserId("Ololo " + id);
                        endpointProfileDao.save(ep.toDto());
                    } catch (KaaOptimisticLockingFailureException ex) {
                        LOG.error("Catch optimistic exception.");
                        throw ex;
                    }
                }
            }));
        }
        for (Future future : tasks) {
            try {
                future.get();
            } catch (ExecutionException ex) {
                throw ex.getCause();
            }
        }
    }

    @Test
    public void testRemoveByKeyHash() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null, null);
        endpointProfileDao.removeByKeyHash(expected.getEndpointKeyHash());
        EndpointProfile found = endpointProfileDao.findByKeyHash(expected.getEndpointKeyHash());
        Assert.assertNull(found);
    }

    @Test
    public void testRemoveById() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null, null);
        endpointProfileDao.removeById(ByteBuffer.wrap(expected.getEndpointKeyHash()));
        EndpointProfile found = endpointProfileDao.findByKeyHash(expected.getEndpointKeyHash());
        Assert.assertNull(found);
    }

    @Test
    public void testRemoveByIdNullKey() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null, null);
        EndpointProfile found = endpointProfileDao.findByKeyHash(expected.getEndpointKeyHash());
        Assert.assertNotNull(found);
    }

    @Test
    public void testFindById() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null, null);
        EndpointProfile found = endpointProfileDao.findById(ByteBuffer.wrap(expected.getEndpointKeyHash()));
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testFindByIdNullKey() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null, null);
        EndpointProfile found = endpointProfileDao.findById(null);
        Assert.assertNull(found);
    }

    @Test
    public void testFindByAccessToken() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null, null);
        EndpointProfile found = endpointProfileDao.findByAccessToken(expected.getAccessToken());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testFindByEndpointUserId() throws Exception {
        EndpointProfileDto endpointProfileDto = generateEndpointProfile(null, null, null, null);
        EndpointUserDto endpointUserDto = generateEndpointUser(Arrays.asList(endpointProfileDto.getId()));
        List<CassandraEndpointProfile> found = endpointProfileDao.findByEndpointUserId(endpointUserDto.getId());
        Assert.assertFalse(found.isEmpty());
        Assert.assertEquals(endpointProfileDto, found.get(0).toDto());
    }

    @Test
    public void testCheckSdkToken() throws Exception {
        generateEndpointProfile(null, "alpha", null, null);
        Assert.assertTrue(endpointProfileDao.checkSdkToken("alpha"));
        Assert.assertFalse(endpointProfileDao.checkSdkToken("beta"));
    }
 
}
