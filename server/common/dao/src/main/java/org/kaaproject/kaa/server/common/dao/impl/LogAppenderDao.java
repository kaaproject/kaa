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

public interface LogAppenderDao<T> extends SqlDao<T> {

    /**
     *
     * @param   appId the app id
     * @return  the list of log appenders
     */
    List<T> findByAppId(String appId);

    /**
     * Find log appenders by application id and schema version
     *
     * @param appId the application id
     * @param schemaVersion the log schema version
     * @return the list of log appenders
     */
    List<T> findByAppIdAndSchemaVersion(String appId, int schemaVersion);

}
