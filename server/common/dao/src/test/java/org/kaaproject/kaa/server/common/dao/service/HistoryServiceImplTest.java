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

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.server.common.dao.impl.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HistoryServiceImplTest extends AbstractTest {

    private ApplicationDto application;

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

}
