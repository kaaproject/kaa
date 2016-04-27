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
 * The generic Dao interface.
 * @param <T>  the type parameter
 */
public interface Dao<T, K> {

    /**
     * Save object. Will be returned object with id
     *
     * @param o the domain object
     * @return the saved object
     */
    T save(T o);

    /**
     * Find all objects.
     *
     * @return the list of objects
     */
    List<T> find();

    /**
     * Find object by id.
     *
     * @param   id the id
     * @return  the found object
     */
    T findById(K id);

    /**
     * Remove all objects from collection/table.
     */
    void removeAll();

    /**
     * Remove object by id.
     *
     * @param id the object id
     */
    void removeById(K id);

}
