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

public interface UserVerifierDao<T> extends Dao<T, String> {

    /**
     * Find user verifiers by application id
     *
     * @param appId the application id
     * @return the list of user verifiers
     */
    List<T> findByAppId(String appId);

    /**
     * Find user verifier by application id and verifier token
     *
     * @param appId         the application id
     * @param verifierToken the verifier token
     * @return the found user verifier
     */
    T findByAppIdAndVerifierToken(String appId, String verifierToken);

}
