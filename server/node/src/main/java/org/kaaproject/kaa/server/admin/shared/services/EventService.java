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

package org.kaaproject.kaa.server.admin.shared.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyVersionDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.EventClassViewDto;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/eventService")
public interface EventService extends RemoteService {

  List<EventClassFamilyDto> getEventClassFamilies() throws KaaAdminServiceException;

  EventClassFamilyDto getEventClassFamily(String eventClassFamilyId)
      throws KaaAdminServiceException;

  EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily)
      throws KaaAdminServiceException;

  List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String eventClassFamilyId,
                                                              int version,
                                                              EventClassType type)
      throws KaaAdminServiceException;

  List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationToken(
      String applicationToken) throws KaaAdminServiceException;

  ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String applicationEventFamilyMapId)
      throws KaaAdminServiceException;

  ApplicationEventFamilyMapDto editApplicationEventFamilyMap(
      ApplicationEventFamilyMapDto applicationEventFamilyMap) throws KaaAdminServiceException;

  List<EcfInfoDto> getVacantEventClassFamiliesByApplicationToken(String applicationToken)
      throws KaaAdminServiceException;

  List<AefMapInfoDto> getEventClassFamiliesByApplicationToken(String applicationToken)
      throws KaaAdminServiceException;

  List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(
      String applicationId) throws KaaAdminServiceException;

  List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(String applicationId)
      throws KaaAdminServiceException;

  List<AefMapInfoDto> getEventClassFamiliesByApplicationId(String applicationId)
      throws KaaAdminServiceException;

  RecordField createEcfEmptySchemaForm() throws KaaAdminServiceException;

  RecordField generateEcfSchemaForm(String fileItemName) throws KaaAdminServiceException;

  List<EventClassFamilyVersionDto> getEventClassFamilyVersions(String eventClassFamilyId)
      throws KaaAdminServiceException;

  void addEventClassFamilyVersion(String eventClassFamilyId,
                                  EventClassFamilyVersionDto eventClassFamilyVersion)
      throws KaaAdminServiceException;

  EventClassViewDto getEventClassView(String eventClassId) throws KaaAdminServiceException;

  EventClassViewDto getEventClassViewByCtlSchemaId(EventClassDto eventClassDto)
      throws KaaAdminServiceException;

  EventClassDto getEventClass(String eventClassId) throws KaaAdminServiceException;

  EventClassViewDto saveEventClassView(EventClassViewDto eventClassViewDto)
      throws KaaAdminServiceException;

  EventClassViewDto createEventClassFormCtlSchema(CtlSchemaFormDto ctlSchemaForm)
      throws KaaAdminServiceException;

  void addEventClassFamilyVersionFromView(String eventClassFamilyId,
                                          List<EventClassViewDto> eventClassViewDto)
      throws KaaAdminServiceException;

  void validateEcfListInSdkProfile(List<AefMapInfoDto> ecfList) throws KaaAdminServiceException;

}
