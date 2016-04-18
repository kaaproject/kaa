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

public interface LogAppendersService {

    /**
     * @param   appId the app id
     * @return  the list log appender dto
     */
    List<LogAppenderDto> findAllAppendersByAppId(String appId);

    /**
     * @param   appId           the app id
     * @param   schemaVersion   the schema version
     * @return  the list log appender dto
     */
    List<LogAppenderDto> findLogAppendersByAppIdAndSchemaVersion(String appId, int schemaVersion);

    /**
     * @param id the id
     */
    void removeLogAppenderById(String id);

    /**
     * @param   id the id
     * @return  the log appender dto
     */
    LogAppenderDto findLogAppenderById(String id);

    /**
     * @param   logAppenderDto the log appender dto
     * @return  the log appender dto
     */
    LogAppenderDto saveLogAppender(LogAppenderDto logAppenderDto);

}
