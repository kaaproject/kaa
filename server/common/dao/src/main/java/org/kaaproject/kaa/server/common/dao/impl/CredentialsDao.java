/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.common.dao.impl;

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.model.Credentials;

import java.nio.ByteBuffer;

/**
 * The interface Credentials dao
 * @param <T> the type parameter
 */
public interface CredentialsDao<T extends Credentials> extends Dao<T, ByteBuffer> {

    /**
     * Saves given dto
     * @param dto
     * @return
     */
    T save(CredentialsDto dto);


    /**
     * Find credential by id
     * @param id the credential id
     * @return credential object
     */
    T findById(String id);


    /**
     * Updates credential's status by id
     * @param id credential's id to be updated
     * @param status status to update
     * @return updated credential object
     */
    T updateStatusById(String id, CredentialsStatus status);


    /**
     * Removes credential by id
     * @param id credential id to be deleted
     */
    void removeById(String id);
}
