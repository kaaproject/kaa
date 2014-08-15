/*

 * Copyright 2014 CyberVision, Inc.
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

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;

public interface LogAppendersService {

    /**
     * @param logAppenderId
     * @return
     */
    List<LogSchemaDto> findVacantLogSchemasByLogAppenderId(String logAppenderId);

    /**
     * @param appId
     * @return
     */
    List<LogAppenderDto> findRegisteredLogAppendersByAppId(String appId);

    /**
     * @param appId
     * @param logAppenderId
     */
    LogAppenderDto registerLogAppenderById(String logAppenderId);

    /**
     * @param appId
     * @return
     */
    List<LogAppenderDto> findAllAppendersByAppId(String appId);

    /**
     * @param logAppenderId
     */
    LogAppenderDto unregisterLogAppenderById(String logAppenderId);

    /**
     * @param id
     */
    void removeLogAppenderById(String id);

    /**
     * @param id
     * @return
     */
    LogAppenderDto findLogAppenderById(String id);

    /**
     * @param logAppenderDto
     * @return
     */
    LogAppenderDto saveLogAppender(LogAppenderDto logAppenderDto);

}
