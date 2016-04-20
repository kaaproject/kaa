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

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;

import java.nio.ByteBuffer;

import org.kaaproject.kaa.common.dto.TopicListEntryDto;
import org.kaaproject.kaa.server.common.dao.impl.TopicListEntryDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraTopicListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository(value = "topicListEntryDao")
public class TopicListEntryCassandraDao extends AbstractCassandraDao<CassandraTopicListEntry, ByteBuffer> implements TopicListEntryDao<CassandraTopicListEntry> {

    private static final Logger LOG = LoggerFactory.getLogger(TopicListEntryCassandraDao.class);

    @Override
    protected Class<CassandraTopicListEntry> getColumnFamilyClass() {
        return CassandraTopicListEntry.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.TOPIC_LIST_ENTRY_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraTopicListEntry save(TopicListEntryDto dto) {
        LOG.debug("Save topic list entry [{}] ", dto);
        return save(new CassandraTopicListEntry(dto));
    }

    @Override
    public CassandraTopicListEntry findByHash(byte[] hash) {
        LOG.debug("Try to find topic list entry by hash [{}] ", hash);
        return (CassandraTopicListEntry) getMapper().get(getByteBuffer(hash));
    }

    @Override
    public void removeByHash(byte[] hash) {
        LOG.debug("Remove topic list entry by hash [{}] ", hash);
        getMapper().delete(getByteBuffer(hash));
    }
}
