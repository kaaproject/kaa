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
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicListEntryDto;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraTopicListEntry;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TopicListEntryCassandraDaoTest extends AbstractCassandraTest {

    @Test
    public void testFindByHash() throws Exception {
        List<TopicDto> topics = new ArrayList<>();
        TopicDto topicDto = new TopicDto();
        topicDto.setId("6123");
        topics.add(topicDto);
        byte[] hash = "hash".getBytes();
        int simpleHash = 123;
        TopicListEntryDto topicListEntryDto = new TopicListEntryDto(123, hash, topics);
        topicListEntryDao.save(topicListEntryDto);

        CassandraTopicListEntry topicListEntry = topicListEntryDao.findByHash(hash);
        Assert.assertEquals(simpleHash, topicListEntry.getSimpleHash());
    }
}
