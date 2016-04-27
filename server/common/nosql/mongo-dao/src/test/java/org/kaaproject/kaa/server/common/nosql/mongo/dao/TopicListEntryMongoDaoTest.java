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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicListEntryDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoTopicListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TopicListEntryMongoDaoTest extends AbstractMongoTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopicListEntryMongoDaoTest.class);

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @After
    public void afterTest() throws IOException {
        MongoDataLoader.clearDBData();
    }

    @Test
    public void findEndpointUserConfigurationDtoTest() throws IOException {
        List<TopicDto> topics = new ArrayList<>();
        topics.add(generateTopicDto(null, TopicTypeDto.OPTIONAL));
        byte[] hash = "hash".getBytes();
        int simpleHash = 123;
        TopicListEntryDto topicListEntryDto = new TopicListEntryDto(123, hash, topics);
        topicListEntryDao.save(topicListEntryDto);

        MongoTopicListEntry topicListEntry = topicListEntryDao.findByHash(hash);
        Assert.assertEquals(simpleHash, topicListEntry.getSimpleHash());
    }
}
