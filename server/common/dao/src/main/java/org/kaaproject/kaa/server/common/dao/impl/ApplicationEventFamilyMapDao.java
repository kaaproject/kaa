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
 * The interface for Application Event Family Map Dao.
 * @param <T>  the type parameter
 */
public interface ApplicationEventFamilyMapDao<T> extends SqlDao<T> {

    /**
     * Find map elements for applicationId.
     *
     * @param id the application id
     * @return the list of map elements
     */
    List<T> findByApplicationId(String id);

    /**
     * Find map elements for ids.
     *
     * @param ids the application event maps ids
     * @return the list of map elements
     */
    List<T> findByIds(List<String> ids);

    /**
     * Remove all Map Elements by Application id.
     *
     * @param applicationId the application id
     */
    void removeByApplicationId(String applicationId);

    /**
     * Validate application event family map for uniqueness within the application.
     *
     * @param applicationId the application id
     * @param ecfId the event class family id
     * @param version the event class family version
     * @return true if application event family map is unique otherwise false
     */
    boolean validateApplicationEventFamilyMap(String applicationId, String ecfId, int version);

    /**
     * Find map elements for Event Class Family id and version.
     *
     * @param eventClassFamilyId the Event Class Family id
     * @param version the version of Event Class Family
     * @return the list of map elements
     */
    List<T> findByEcfIdAndVersion(String eventClassFamilyId, int version);
}
