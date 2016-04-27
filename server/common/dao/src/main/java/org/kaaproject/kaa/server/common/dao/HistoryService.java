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

package org.kaaproject.kaa.server.common.dao;


import java.util.List;

import org.kaaproject.kaa.common.dto.HistoryDto;

/**
 * The interface History service. This service manage history information about
 * every changes in kaa system related to configuration, profile filters and endpoint groups.
 * Its helps to define what was changed during some period of time.
 */
public interface HistoryService {

    /**
     * Find histories by application id.
     *
     * @param appId the string id of application
     * @return the list of found histories
     */
    List<HistoryDto> findHistoriesByAppId(String appId);

    /**
     * Find history by application id and sequence number.
     *
     * @param appId  the string id of application
     * @param seqNum the sequence number
     * @return the found history dto
     */
    HistoryDto findHistoryBySeqNumber(String appId, int seqNum);

    /**
     * Find histories by application id with sequence numbers which equal or more than start sequence number.
     *
     * @param appId       the string id of application
     * @param startSeqNum the start sequence number
     * @return the list of found histories
     */
    List<HistoryDto> findHistoriesBySeqNumberStart(String appId, int startSeqNum);

    /**
     * Find histories by application id with sequence number between start and end sequence numbers.
     *
     * @param appId       the string id of application
     * @param startSeqNum the start sequence number
     * @param endSeqNum   the end sequence number
     * @return the list of found histories
     */
    List<HistoryDto> findHistoriesBySeqNumberRange(String appId, int startSeqNum, int endSeqNum);

    /**
     * Save history object.
     *
     * @param historyDto the history dto
     * @return the history dto
     */
    HistoryDto saveHistory(HistoryDto historyDto);
}
