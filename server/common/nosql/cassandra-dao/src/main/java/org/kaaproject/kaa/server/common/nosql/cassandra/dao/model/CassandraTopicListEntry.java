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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.TOPIC_LIST_ENTRY_COLUMN_FAMILY_NAME;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicListEntryDto;
import org.kaaproject.kaa.server.common.dao.model.TopicListEntry;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;


@Table(name = TOPIC_LIST_ENTRY_COLUMN_FAMILY_NAME)
public final class CassandraTopicListEntry implements TopicListEntry, Serializable {

    @Transient
    private static final long serialVersionUID = 1812154867980435128L;

    @PartitionKey
    @Column(name = CassandraModelConstants.TOPIC_LIST_ENTRY_HASH_PROPERTY)
    private ByteBuffer hash;
    @Column(name = CassandraModelConstants.TOPIC_LIST_ENTRY_SIMPLE_HASH_PROPERTY)
    private int simpleHash;
    @Column(name = CassandraModelConstants.TOPIC_LIST_ENTRY_TOPIC_IDS_PROPERTY)
    private List<String> topicIds;

    public CassandraTopicListEntry() {
    }

    public CassandraTopicListEntry(TopicListEntryDto dto) {
        this.hash = getByteBuffer(dto.getHash());
        this.simpleHash = dto.getSimpleHash();
        this.topicIds = new ArrayList<>();
        if (dto.getTopics() != null) {
            for (TopicDto topic : dto.getTopics()) {
                topicIds.add(topic.getId());
            }
        }
    }

    public ByteBuffer getHash() {
        return hash;
    }

    public void setHash(ByteBuffer hash) {
        this.hash = hash;
    }

    public int getSimpleHash() {
        return simpleHash;
    }

    public void setSimpleHash(int simpleHash) {
        this.simpleHash = simpleHash;
    }

    public List<String> getTopicIds() {
        return topicIds;
    }

    public void setTopicIds(List<String> topicIds) {
        this.topicIds = topicIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CassandraTopicListEntry)) {
            return false;
        }

        CassandraTopicListEntry that = (CassandraTopicListEntry) o;

        if (simpleHash != that.simpleHash) {
            return false;
        }
        if (hash != null ? !hash.equals(that.hash) : that.hash != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = hash != null ? hash.hashCode() : 0;
        result = 31 * result + simpleHash;
        return result;
    }

    @Override
    public TopicListEntryDto toDto() {
        List<TopicDto> topicDtos = ModelUtils.getTopicDtos(topicIds);
        return new TopicListEntryDto(simpleHash, getBytes(hash), topicDtos);
    }
}
