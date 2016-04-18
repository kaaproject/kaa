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

package org.kaaproject.kaa.server.operations.service.cache;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class TopicListCacheEntry implements Serializable {

    private static final long serialVersionUID = -3744774001481169888L;
    
    private final int simpleHash;
    
    private final EndpointObjectHash hash;

    private final List<TopicDto> topics;

    public TopicListCacheEntry(int simpleHash, EndpointObjectHash hash, List<TopicDto> topics) {
        this.simpleHash = simpleHash;
        this.hash = hash;
        this.topics = topics;
    }

    public int getSimpleHash() {
        return simpleHash;
    }

    public EndpointObjectHash getHash() {
        return hash;
    }

    public List<TopicDto> getTopics() {
        return topics;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hash == null) ? 0 : hash.hashCode());
        result = prime * result + simpleHash;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TopicListCacheEntry other = (TopicListCacheEntry) obj;
        if (hash == null) {
            if (other.hash != null)
                return false;
        } else if (!hash.equals(other.hash))
            return false;
        if (simpleHash != other.simpleHash)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TopicListCacheEntry [simpleHash=" + simpleHash + ", hash=" + hash + ", topics=" + topics + "]";
    }

}
