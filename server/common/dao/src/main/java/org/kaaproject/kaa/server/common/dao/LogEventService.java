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

import org.kaaproject.kaa.common.dto.logs.LogEventDto;

/**
 * The interface LogEventService service.
 */
public interface LogEventService {
    
    /**
     * Create Collection with specific name
     * 
     * @param collectionName the name of collection
     */
    void createCollection(String collectionName);
    
    /**
     * Create Role with specific name
     * 
     * @param name the name of role
     * @param collectionName the name of collection to which role will be created
     */
    void createRole(String name, String collectionName);
    
    /**
     * Create User with specific name, password and role
     * 
     * @param name the name of user
     * @param password the password of user
     * @param roleName the role of user
     */
    void createUser(String name, String password, String roleName);
    
    /**
     * Save LogEventPacks. Will be returned LogEventPacks with id
     *
     * @param logEventPackDto the domain objects
     * @param collectionName the name of collection to which save LogEventPack
     * @return the saved LogEventPacks
     */
    List<LogEventDto> save(List<LogEventDto> logEventPackDtos, String collectionName);

    /**
     * Remove all objects from collection/table.
     * 
     * @param collectionName the name of collection
     */
    void removeAll(String collectionName);
}
