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

package org.kaaproject.kaa.common.dto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public final class TopicListEntryDto implements Serializable {

    private static final long serialVersionUID = 2771583997490244417L;

    private int simpleHash;

    private byte[] hash;

    private List<TopicDto> topics;

    public TopicListEntryDto(int simpleHash, byte[] hash, List<TopicDto> topics) {
        this.simpleHash = simpleHash;
        this.hash = hash;
        this.topics = topics;
    }

    public int getSimpleHash() {
        return simpleHash;
    }

    public byte[] getHash() {
        return hash;
    }

    public List<TopicDto> getTopics() {
        return topics;
    }

    public void setSimpleHash(int simpleHash) {
        this.simpleHash = simpleHash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public void setTopics(List<TopicDto> topics) {
        this.topics = topics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TopicListEntryDto)) {
            return false;
        }

        TopicListEntryDto that = (TopicListEntryDto) o;

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
        int result = simpleHash;
        result = 31 * result + (hash != null ? Arrays.hashCode(hash) : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TopicListEntryDto{");
        sb.append("simpleHash=").append(simpleHash);
        sb.append(", hash=").append(Arrays.toString(hash));
        sb.append(", topics=").append(topics);
        sb.append('}');
        return sb.toString();
    }
}
