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

package org.kaaproject.kaa.server.common.dao.impl;

import java.util.List;


/**
 * The interface Log Event Pack dao.
 * @param <T>  the type parameter
 */
public interface LogEventDao<T> {
    
    /**
     * Create Collection with specific name
     * 
     * @param collectionName the name of collection
     */
    void createCollection(String collectionName);
    
    /**
     * Save objects. Will be returned objects with id
     *
     * @param o the domain objects
     * @param collectionName the name of collection to which save object
     * @return the saved objects
     */
    List<T> save(List<T> o, String collectionName);

    /**
     * Remove all objects from collection/table.
     * 
     * @param collectionName the name of collection
     */
    void removeAll(String collectionName);
}