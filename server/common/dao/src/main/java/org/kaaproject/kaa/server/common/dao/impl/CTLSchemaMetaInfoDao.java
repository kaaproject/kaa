/*
 * Copyright 2015 CyberVision, Inc.
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

import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;

import java.util.List;

/**
 * The interface CTL schema meta information dao.
 *
 * @param <T> the model type parameter.
 */
public interface CTLSchemaMetaInfoDao<T> extends SqlDao<T> {

    /**
     * Increment count of the given CTL schema meta information.
     * This is number of CTL schemas used the same meta information.
     *
     * @param metaInfo the CTL schema meta information object
     * @return the CTL schema meta information with incremented count.
     */
    T incrementCount(T metaInfo);

    /**
     * Find a CTL schema of the given fully qualified name and version number.
     *
     * @param fqn     the fully qualified.
     * @param version the schema version.
     * @return the CTL schema meta information object with given .
     */
    T findByFqnAndVersion(String fqn, Integer version);

    /**
     * Find a CTL schema meta information
     * by the given fully qualified name and version number.
     *
     * @return the list of system CTL schema meta information.
     */
    List<T> findSystemSchemaMetaInfo();

    /**
     * Update scope of the given CTL schema meta information.
     *
     * @param ctlSchema the CTL schema meta information object.
     * @return the saved the CTL schema meta information object.
     */
    T updateScope(CTLSchemaMetaInfo ctlSchema);
}
