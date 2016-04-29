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

import java.util.List;

/**
 * The interface History dao.
 * @param <T>  the type parameter
 */
public interface HistoryDao<T> extends SqlDao<T> {

    /**
     * Find history by application id.
     *
     * @param appId the application id
     * @return the list of history objects
     */
    List<T> findByAppId(String appId);

    /**
     * Find history by sequence number.
     *
     * @param appId the application id
     * @param seqNum the sequence number
     * @return the history object
     */
    T findBySeqNumber(String appId, int seqNum);

    /**
     * Find history by sequence number start.
     *
     * @param appId the application id
     * @param startSeqNum the start sequence number
     * @return the list of history objects
     */
    List<T> findBySeqNumberStart(String appId, int startSeqNum);

    /**
     * Find history by sequence number range.
     *
     * @param appId the application id
     * @param startSeqNum the start sequence number
     * @param endSeqNum the end sequence number
     * @return the list of history objects
     */
    List<T> findBySeqNumberRange(String appId, int startSeqNum, int endSeqNum);

}
