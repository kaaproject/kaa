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

import org.hibernate.FlushMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;

public interface SqlDao<T> extends Dao<T, String> {

    /**
     * Re-read object from database.
     *
     * @param object The object to refresh
     */
    void refresh(Object object);

    /**
     * Get hibernate session with the given flush mode option.
     *
     * @param flushMode the {@link org.hibernate.FlushMode} object.
     * @return the {@link org.hibernate.Session} object.
     */
    Session getSession(FlushMode flushMode);

    /**
     * Get hibernate session.
     *
     * @return the {@link org.hibernate.Session} object.
     */
    Session getSession();

    /**
     * Save object. Will be returned object with id.
     *
     * @param o     the domain object
     * @param flush specify if session flush needed.
     * @return the saved object
     */
    T save(T o, boolean flush);

    /**
     * Build lock request with the given {@link org.hibernate.LockOptions} object
     *
     * @param lockOptions The lock options to use
     * @return the {@link org.hibernate.Session.LockRequest} object.
     */
    Session.LockRequest lockRequest(LockOptions lockOptions);

    /**
     * Find object by id.
     *
     * @param id   the id
     * @param lazy specifies whether return initialized object (if false is set)
     *             or proxy (if true is set)
     * @return the found object or null if object not found
     */
    T findById(String id, boolean lazy);

    /**
     * Persist model object
     *
     * @param o the model object
     * @return the persisted object
     */
    T persist(T o);
    
    /**
     * @param   o       the o
     * @param   clazz   the clazz
     * @param   <V>     the V
     * @return  the saved object
     *
     */
    <V> V save(V o, Class<?> clazz);
}
