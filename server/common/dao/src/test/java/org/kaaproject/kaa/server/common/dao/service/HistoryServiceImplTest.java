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

package org.kaaproject.kaa.server.common.dao.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.ProcessingStatus;
import org.kaaproject.kaa.server.common.dao.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.HistoryDao;
import org.kaaproject.kaa.server.common.dao.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.mongo.model.Application;
import org.kaaproject.kaa.server.common.dao.mongo.model.History;
import org.kaaproject.kaa.server.common.dao.mongo.model.Update;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.mongodb.MongoException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SuppressWarnings("unchecked")
public class HistoryServiceImplTest extends AbstractTest {

    private ApplicationDto application;

    private HistoryDao<History> historyDaoMock = mock(HistoryDao.class);
    private HistoryDao<History> historyDao = null;
    private ApplicationDao<Application> applicationDaoMock = mock(ApplicationDao.class);;
    private ApplicationDao<Application> applicationDao = null;

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() {
        application = generateApplication();
        generateConfSchema(application.getId(), 1);
        generateProfSchema(application.getId(), 1);
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void findHistoriesByAppIdTest() {
        List<HistoryDto> historyRows = historyService.findHistoriesByAppId(application.getId());
        Assert.assertEquals(4, historyRows.size());
        Assert.assertEquals(ChangeType.ADD_PROF, historyRows.get(0).getChange().getType());
        Assert.assertEquals(ChangeType.ADD_CONF, historyRows.get(1).getChange().getType());
    }

    @Test
    public void findHistoryBySeqNumberTest() {
        HistoryDto history = historyService.findHistoryBySeqNumber(application.getId(), 3);
        Assert.assertEquals(ChangeType.ADD_CONF, history.getChange().getType());
    }

    @Test
    public void findHistoriesBySeqNumberStartTest() {
        List<HistoryDto> historyRows = historyService.findHistoriesBySeqNumberStart(application.getId(), 2);
        Assert.assertEquals(2, historyRows.size());
        Assert.assertEquals(ChangeType.ADD_PROF, historyRows.get(1).getChange().getType());
        Assert.assertEquals(ChangeType.ADD_CONF, historyRows.get(0).getChange().getType());
    }

    @Test
    public void findHistoriesBySeqNumberRangeTest() {
        List<HistoryDto> historyRows = historyService.findHistoriesBySeqNumberRange(application.getId(), 2, 3);
        Assert.assertEquals(1, historyRows.size());
        Assert.assertEquals(ChangeType.ADD_CONF, historyRows.get(0).getChange().getType());
    }

    @Test
    public void checkIncrementingSequenceNumberTest() {
        mockDao();
        String appId = new ObjectId().toString();

        Update update = new Update();
        update.setSequenceNumber(56);
        update.setStatus(ProcessingStatus.PENDING);

        Application application = new Application();
        application.setId(appId);
        application.setName("Test Application");
        application.setSequenceNumber(55);
        application.setUpdate(update);

        ChangeDto change = new ChangeDto();
        change.setCfMajorVersion(3);
        change.setConfigurationId(new ObjectId().toString());

        HistoryDto historyDto = new HistoryDto();
        historyDto.setApplicationId(appId);
        historyDto.setChange(change);

        when(historyDaoMock.save(any(History.class))).thenThrow(MongoException.DuplicateKey.class)
        .thenThrow(MongoException.DuplicateKey.class).thenReturn(null);

        when(applicationDaoMock.forceNextSeqNumber(any(String.class))).thenReturn(application);

        historyService.saveHistory(historyDto);

        unmockDao();
    }

    private void mockDao() {
        historyDao = (HistoryDao<History>) ReflectionTestUtils.getField(historyService, "historyDao");
        ReflectionTestUtils.setField(historyService, "historyDao", historyDaoMock);

        ReflectionTestUtils.setField(historyService, "waitSeconds", 1);

        applicationDao = (ApplicationDao<Application>) ReflectionTestUtils.getField(historyService, "applicationDao");
        ReflectionTestUtils.setField(historyService, "applicationDao", applicationDaoMock);
    }

    private void unmockDao() {
        ReflectionTestUtils.setField(historyService, "historyDao", historyDao);
        ReflectionTestUtils.setField(historyService, "applicationDao", applicationDao);
        ReflectionTestUtils.setField(historyService, "waitSeconds", 3);
    }

}
