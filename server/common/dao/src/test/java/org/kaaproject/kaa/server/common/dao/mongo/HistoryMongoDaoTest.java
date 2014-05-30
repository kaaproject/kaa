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

package org.kaaproject.kaa.server.common.dao.mongo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.server.common.dao.mongo.model.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HistoryMongoDaoTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMongoDaoTest.class);

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.getDB().dropDatabase();
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() throws IOException {
        LOGGER.info("HistoryMongoDaoTest init before tests.");
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void findByAppIdTest() {
        History history = historyDao.findById(hists.get(1));
        Assert.assertNotNull(history);
        List<History> hists = historyDao.findByAppId(history.getApplicationId().toString());
        Assert.assertNotNull(hists);
        Assert.assertSame(hists.size(), 2);
    }

    @Test
    public void findBySeqNumberTest() {
        History history = historyDao.findById(hists.get(1));
        Assert.assertNotNull(history);
        History hist = historyDao.findBySeqNumber(history.getApplicationId().toString(), 0);
        Assert.assertNull(hist);
        hist = historyDao.findBySeqNumber(history.getApplicationId().toString(), 4);
        Assert.assertNotNull(hist);
    }

    @Test
    public void findBySeqNumberStartTest() {
        History history = historyDao.findById(hists.get(1));
        Assert.assertNotNull(history);
        List<History> hist = historyDao.findBySeqNumberStart(history.getApplicationId().toString(), 3);
        Assert.assertNotNull(hist);
        Assert.assertSame(hist.size(), 2);
    }

    @Test
    public void findBySeqNumberRangeTest() {
        History history = historyDao.findById(hists.get(1));
        Assert.assertNotNull(history);
        List<History> hist = historyDao.findBySeqNumberRange(history.getApplicationId().toString(), 0, 4);
        Assert.assertNotNull(hist);
        Assert.assertSame(hist.size(), 1);
    }

    @Test
    public void convertToDtoTest() {
        History history = historyDao.findById(hists.get(0));
        Assert.assertNotNull(history);
        HistoryDto dto = history.toDto();
        Assert.assertNotNull(dto);
        History converted = new History(dto);
        Assert.assertEquals(history, converted);
    }

    @Test
    public void removeByIdTest() {
        History history = historyDao.findById(hists.get(0));
        Assert.assertNotNull(history);
        historyDao.removeById(history.getId());
        history = historyDao.findById(hists.get(0));
        Assert.assertNull(history);
        historyDao.removeAll();
        List<History> hist = historyDao.find();
        Assert.assertTrue(hist.isEmpty());
    }

    @Test
    public void saveHistoryTest() {
        History history = historyDao.findById(hists.get(0));
        Assert.assertNotNull(history);
        history.setId(null);
        History saved = historyDao.save(history);
        Assert.assertNotNull(saved);
        Assert.assertNotNull(saved.getId());
    }

}
