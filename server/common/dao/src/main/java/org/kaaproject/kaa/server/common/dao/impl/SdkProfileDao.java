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

public interface SdkProfileDao<T> extends Dao<T, String> {

    /**
     * Returns an SDK profile with the given token.
     *
     * @param token An SDK profile token
     *
     * @return An SDK profile with the given token
     */
    public T findSdkProfileByToken(String token);

    /**
     * Returns a list of SDK profiles for an application with the given identifier.
     *
     * @param applicationId An application identifier
     *
     * @return A list of SDK profiles for an application with the given identifier
     */
    public List<T> findSdkProfileByApplicationId(String applicationId);
}
