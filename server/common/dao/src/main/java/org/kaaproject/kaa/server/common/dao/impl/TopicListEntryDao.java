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

package org.kaaproject.kaa.server.common.dao.impl;

import org.kaaproject.kaa.common.dto.TopicListEntryDto;
import org.kaaproject.kaa.server.common.dao.model.TopicListEntry;

import java.nio.ByteBuffer;

/**
 * The interface Topic list entry dao.
 *
 * @param <T> the type parameter
 */
public interface TopicListEntryDao<T extends TopicListEntry> extends Dao<T, ByteBuffer> {

    /**
     * Save topic list entry
     *
     * @param dto topic list entry
     * @return save topic list entry
     */
    T save(TopicListEntryDto dto);

    /**
     * Find topic list entry by hash
     *
     * @param hash the hash of a topic list
     * @return found topic list entry
     */
    T findByHash(byte[] hash);

    /**
     * Remove topic list entry by hash
     *
     * @param hash the hash of a topic list
     */
    void removeByHash(byte[] hash);
}
