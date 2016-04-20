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

package org.kaaproject.kaa.server.common.dao;

import java.util.List;

import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;

/**
 * The interface Application Event Map service.
 */
public interface ApplicationEventMapService {

    /**
     * Find Application Event Family Maps by application id.
     *
     * @param applicationId the application id
     * @return the list of found application event family maps
     */
    List<ApplicationEventFamilyMapDto> findApplicationEventFamilyMapsByApplicationId(String applicationId);

    /**
     * Find Application Event Family Maps by ids.
     *
     * @param ids the Application Event Family Maps ids
     * @return the list of found application event family maps
     */
    List<ApplicationEventFamilyMapDto> findApplicationEventFamilyMapsByIds(List<String> ids);

    /**
     * Find Application Event Family Map by id.
     *
     * @param id the string id of Application Event Family Map
     * @return the application event family map dto object
     */
    ApplicationEventFamilyMapDto findApplicationEventFamilyMapById(String id);

    /**
     * Save Application Event Family Map
     *
     * @param applicationEventFamilyMap the application event family map dto
     * @return the application event family map dto
     */
    ApplicationEventFamilyMapDto saveApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap);

    /**
     * Find Vacant Event Class Families by application id.
     *
     * @param applicationId the application id
     * @return the list of found Event Class Families
     */
    List<EcfInfoDto> findVacantEventClassFamiliesByApplicationId(String applicationId);

    /**
     * Find Event Class Families by application id.
     *
     * @param applicationId the application id
     * @return the list of found Event Class Families
     */
    List<AefMapInfoDto> findEventClassFamiliesByApplicationId(String applicationId);

    /**
     * Find Application Event Family Maps by Event Class Family id and version.
     *
     * @param eventClassFamilyId the Event Class Family id
     * @param version the version
     * @return the list of found application event family maps
     */
    List<ApplicationEventFamilyMapDto> findByEcfIdAndVersion(String eventClassFamilyId, int version);

}
