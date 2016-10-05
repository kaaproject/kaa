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

import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyVersionDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;

import java.util.List;
import java.util.Set;

/**
 * The interface EventClass service.
 */
public interface EventClassService {

  /**
   * Find event class families by tenant id.
   *
   * @param tenantId the tenant id
   * @return the list of found event class families
   */
  List<EventClassFamilyDto> findEventClassFamiliesByTenantId(String tenantId);

  /**
   * Find event class family by id.
   *
   * @param id the string id of event class family
   * @return the event class family dto object
   */
  EventClassFamilyDto findEventClassFamilyById(String id);

  /**
   * Find event class family by ECF version id.
   *
   * @param id the string id of event class family version
   * @return the event class family dto object
   */
  EventClassFamilyDto findEventClassFamilyByEcfvId(String id);

  /**
   * Find event class family versions by id.
   *
   * @param ecfId the string id of event class family
   * @return the event class family dto object
   */
  List<EventClassFamilyVersionDto> findEventClassFamilyVersionsByEcfId(String ecfId);

  /**
   * Save event class family.
   *
   * @param eventClassFamilyDto the event class family dto
   * @return the event class family dto
   */
  EventClassFamilyDto saveEventClassFamily(EventClassFamilyDto eventClassFamilyDto);

  /**
   * Add event class family schema.
   *
   * @param eventClassFamilyId      the event class family id
   * @param eventClassFamilyVersion the event class family version
   * @param createdUsername         the created username
   */
  void addEventClassFamilyVersion(
          String eventClassFamilyId,
          EventClassFamilyVersionDto eventClassFamilyVersion,
          String createdUsername
  );

  /**
   * Find event classes by event class family Id and version.
   *
   * @param ecfId   the string id of event class family
   * @param version the version
   * @param type    the type
   * @return the list of found event classes
   */
  List<EventClassDto> findEventClassesByFamilyIdVersionAndType(
          String ecfId,
          int version,
          EventClassType type
  );

  /**
   * Check passed FQNs if they are present in event class family.
   * FQNs in scope of event class family should be unique.
   *
   * @param ecfId the string id of event class family
   * @param fqns  list of fqns to check against family fqns
   * @return true is fqns are unique
   */
  boolean validateEventClassFamilyFqns(
          String ecfId,
          List<String> fqns
  );

  /**
   * Find event class family by tenant id and name.
   *
   * @param tenantId the string id of tenant
   * @param name     the event class family name
   * @return the event class family
   */
  EventClassFamilyDto findEventClassFamilyByTenantIdAndName(
          String tenantId,
          String name
  );

  /**
   * Find event class by tenant id and fqn.
   *
   * @param tenantId the string id of tenant
   * @param fqn      the event class fqn
   * @return the event class
   */
  List<EventClassDto> findEventClassByTenantIdAndFqn(String tenantId, String fqn);


  /**
   * Find event class by tenant id and full qualifier name(fqn) and version.
   *
   * @param tenantId the tenant id
   * @param fqn      the full qualifier name
   * @param version  the schema version
   * @return the event class dto
   */
  EventClassDto findEventClassByTenantIdAndFqnAndVersion(String tenantId, String fqn, int version);

  /**
   * Find event class by id.
   *
   * @param eventClassId the eventClass id
   * @return the event class dto
   */
  EventClassDto findEventClassById(String eventClassId);

  /**
   * Validate if there are intersections of same FQNs in event classes among list of chosen
   * families.
   *
   * @param ecfList the list of AefMapInfoDto
   * @return the event class dto
   */
  boolean isValidEcfListInSdkProfile(List<AefMapInfoDto> ecfList);

  /**
   * Get list of all events class FQNs in event class family.
   *
   * @param ecfId string of the event class family id
   * @return list of all FQNs
   */
  Set<String> getFqnSetForEcf(String ecfId);
}
