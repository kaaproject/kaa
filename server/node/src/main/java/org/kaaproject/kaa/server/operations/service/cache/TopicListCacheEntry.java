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
        super();
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
