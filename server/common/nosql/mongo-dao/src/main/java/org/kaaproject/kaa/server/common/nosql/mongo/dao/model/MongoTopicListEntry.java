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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.TOPIC_LIST_ENTRY;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.TOPIC_LIST_SIMPLE_HASH;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.TOPIC_LIST_TOPIC_IDS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicListEntryDto;
import org.kaaproject.kaa.server.common.dao.model.TopicListEntry;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = TOPIC_LIST_ENTRY)
public final class MongoTopicListEntry implements TopicListEntry, Serializable {

    private static final long serialVersionUID = -5646769700581347085L;

    @Id
    private byte[] hash;

    @Field(TOPIC_LIST_SIMPLE_HASH)
    private int simpleHash;

    @Field(TOPIC_LIST_TOPIC_IDS)
    private List<String> topicIds;

    public MongoTopicListEntry() {
    }

    public MongoTopicListEntry(TopicListEntryDto dto) {
        this.hash = dto.getHash();
        this.simpleHash = dto.getSimpleHash();
        this.topicIds = new ArrayList<>();
        if (dto.getTopics() != null) {
            for (TopicDto topic : dto.getTopics()) {
                topicIds.add(topic.getId());
            }
        }
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
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
        if (!(o instanceof MongoTopicListEntry)) {
            return false;
        }

        MongoTopicListEntry that = (MongoTopicListEntry) o;

        if (simpleHash != that.simpleHash) {
            return false;
        }
        if (!Arrays.equals(hash, that.hash)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = hash != null ? Arrays.hashCode(hash) : 0;
        result = 31 * result + simpleHash;
        return result;
    }

    @Override
    public TopicListEntryDto toDto() {
        List<TopicDto> topicDtos = ModelUtils.getTopicDtos(topicIds);
        return new TopicListEntryDto(simpleHash, hash, topicDtos);
    }
}
