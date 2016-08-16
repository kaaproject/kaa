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

package org.kaaproject.kaa.server.admin.services;

import org.apache.avro.Schema;
import org.kaaproject.avro.ui.converter.SchemaFormAvroConverter;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.EventService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.control.service.sdk.SchemaUtil;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

@Service("eventService")
public class EventServiceImpl extends AbstractAdminService implements EventService {

    @Override
    public List<EventClassFamilyDto> getEventClassFamilies() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            return controlService.getEventClassFamiliesByTenantId(getTenantId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EventClassFamilyDto getEventClassFamily(String eventClassFamilyId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            EventClassFamilyDto eventClassFamily = controlService.getEventClassFamily(eventClassFamilyId);
            Utils.checkNotNull(eventClassFamily);
            checkTenantId(eventClassFamily.getTenantId());
            for (EventSchemaVersionDto eventSchemaVersion : eventClassFamily.getSchemas()) {
                Schema schema = new Schema.Parser().parse(eventSchemaVersion.getSchema());
                RecordField schemaForm = ecfSchemaFormAvroConverter.createSchemaFormFromSchema(schema);
                eventSchemaVersion.setSchemaForm(schemaForm);
            }
            return eventClassFamily;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            if (!isEmpty(eventClassFamily.getId())) {
                EventClassFamilyDto storedEventClassFamily = controlService.getEventClassFamily(eventClassFamily.getId());
                Utils.checkNotNull(storedEventClassFamily);
                checkTenantId(storedEventClassFamily.getTenantId());
            } else {
                String username = getCurrentUser().getUsername();
                eventClassFamily.setCreatedUsername(username);
            }
            eventClassFamily.setTenantId(getTenantId());
            return controlService.editEventClassFamily(eventClassFamily);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void addEventClassFamilySchema(String eventClassFamilyId, byte[] data) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            checkEventClassFamilyId(eventClassFamilyId);
            String schema = new String(data);
            SchemaUtil.compileAvroSchema(validateSchema(schema, false));

            EventClassFamilyDto storedEventClassFamily = controlService.getEventClassFamily(eventClassFamilyId);
            Utils.checkNotNull(storedEventClassFamily);
            checkTenantId(storedEventClassFamily.getTenantId());

            String username = getCurrentUser().getUsername();
            controlService.addEventClassFamilySchema(eventClassFamilyId, schema, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String eventClassFamilyId, int version, EventClassType type)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEventClassFamilyId(eventClassFamilyId);
            EventClassFamilyDto storedEventClassFamily = controlService.getEventClassFamily(eventClassFamilyId);
            Utils.checkNotNull(storedEventClassFamily);
            checkTenantId(storedEventClassFamily.getTenantId());

            return controlService.getEventClassesByFamilyIdVersionAndType(eventClassFamilyId, version, type);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationToken(String applicationToken)
            throws KaaAdminServiceException {
        return getApplicationEventFamilyMapsByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(String applicationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getApplicationEventFamilyMapsByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String applicationEventFamilyMapId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ApplicationEventFamilyMapDto aefMap = controlService.getApplicationEventFamilyMap(applicationEventFamilyMapId);
            Utils.checkNotNull(aefMap);
            checkApplicationId(aefMap.getApplicationId());
            return aefMap;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(applicationEventFamilyMap.getId())) {
                String username = getCurrentUser().getUsername();
                applicationEventFamilyMap.setCreatedUsername(username);
                checkApplicationId(applicationEventFamilyMap.getApplicationId());
            } else {
                ApplicationEventFamilyMapDto storedApplicationEventFamilyMap = controlService
                        .getApplicationEventFamilyMap(applicationEventFamilyMap.getId());
                Utils.checkNotNull(storedApplicationEventFamilyMap);
                checkApplicationId(storedApplicationEventFamilyMap.getApplicationId());
            }
            return controlService.editApplicationEventFamilyMap(applicationEventFamilyMap);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getVacantEventClassFamiliesByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getVacantEventClassFamiliesByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<AefMapInfoDto> getEventClassFamiliesByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getEventClassFamiliesByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<AefMapInfoDto> getEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getEventClassFamiliesByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void checkEventClassFamilyId(String eventClassFamilyId) throws IllegalArgumentException {
        if (isEmpty(eventClassFamilyId)) {
            throw new IllegalArgumentException("The eventClassFamilyId parameter is empty.");
        }
    }

    @Override
    public RecordField createEcfEmptySchemaForm() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return ecfSchemaFormAvroConverter.getEmptySchemaFormInstance();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField generateEcfSchemaForm(String fileItemName) throws KaaAdminServiceException {
        try {
            byte[] data = getFileContent(fileItemName);
            String avroSchema = new String(data);
            Schema schema = new Schema.Parser().parse(avroSchema);
            return ecfSchemaFormAvroConverter.createSchemaFormFromSchema(schema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void addEventClassFamilySchemaForm(String eventClassFamilyId, RecordField schemaForm) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            Schema schema = ecfSchemaFormAvroConverter.createSchemaFromSchemaForm(schemaForm);
            String schemaString = SchemaFormAvroConverter.createSchemaString(schema, true);

            EventClassFamilyDto storedEventClassFamily = controlService.getEventClassFamily(eventClassFamilyId);
            Utils.checkNotNull(storedEventClassFamily);
            checkTenantId(storedEventClassFamily.getTenantId());

            String username = getCurrentUser().getUsername();
            controlService.addEventClassFamilySchema(eventClassFamilyId, schemaString, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
}
