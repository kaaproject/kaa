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

package org.kaaproject.kaa.server.common.dao.service;

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidSqlId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidSqlObject;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateSqlId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;
import org.kaaproject.kaa.server.common.dao.ApplicationEventMapService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.impl.ApplicationEventFamilyMapDao;
import org.kaaproject.kaa.server.common.dao.impl.EventClassFamilyDao;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.ApplicationEventFamilyMap;
import org.kaaproject.kaa.server.common.dao.model.sql.EventClassFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationEventMapServiceImpl implements ApplicationEventMapService {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationEventMapServiceImpl.class);

    @Autowired
    private ApplicationEventFamilyMapDao<ApplicationEventFamilyMap> applicationEventFamilyMapDao;

    @Autowired
    private ApplicationDao<Application> applicationDao;

    @Autowired
    private EventClassFamilyDao<EventClassFamily> eventClassFamilyDao;

    @Override
    public List<ApplicationEventFamilyMapDto> findApplicationEventFamilyMapsByApplicationId(
            String applicationId) {
        List<ApplicationEventFamilyMapDto> eventFamilyMaps;
        if (isValidSqlId(applicationId)) {
            LOG.debug("Find application event family maps by applicationId id [{}]", applicationId);
            eventFamilyMaps = convertDtoList(applicationEventFamilyMapDao.findByApplicationId(applicationId));
        } else {
            throw new IncorrectParameterException("Incorrect applicationId id: " + applicationId);
        }
        return eventFamilyMaps;
    }

    @Override
    public List<ApplicationEventFamilyMapDto> findApplicationEventFamilyMapsByIds(List<String> ids) {
        LOG.debug("Find application event family maps by ids [{}]", ids);
        List<ApplicationEventFamilyMapDto> eventFamilies = Collections.emptyList();
        if (ids != null && !ids.isEmpty()) {
            eventFamilies = convertDtoList(applicationEventFamilyMapDao.findByIds(ids));
        }
        return eventFamilies;
    }

    @Override
    public List<ApplicationEventFamilyMapDto> findByEcfIdAndVersion(String eventClassFamilyId, int version){
        LOG.debug("Find application event family maps by ecf id [{}] and version", eventClassFamilyId, version);
        return convertDtoList(applicationEventFamilyMapDao.findByEcfIdAndVersion(eventClassFamilyId, version));
    }

    @Override
    public ApplicationEventFamilyMapDto findApplicationEventFamilyMapById(String id) {
        validateSqlId(id, "Application event family map id is incorrect. Can't find application event family map by id " + id);
        return getDto(applicationEventFamilyMapDao.findById(id));
    }

    @Override
    public ApplicationEventFamilyMapDto saveApplicationEventFamilyMap(
            ApplicationEventFamilyMapDto applicationEventFamilyMapDto) {
        ApplicationEventFamilyMapDto savedApplicationEventFamilyMap = null;
        if (isValidSqlObject(applicationEventFamilyMapDto)) {
            if (isValidSqlId(applicationEventFamilyMapDto.getId())) {
                ApplicationEventFamilyMapDto previousApplicationEventFamilyMapDto = findApplicationEventFamilyMapById(applicationEventFamilyMapDto.getId());
                if (previousApplicationEventFamilyMapDto != null) {
                    LOG.debug("Can't save application event family map. Update is forbidden.");
                    throw new IncorrectParameterException("Can't save application event family map. Update is forbidden.");
                }
            }
            if (applicationEventFamilyMapDao.validateApplicationEventFamilyMap(applicationEventFamilyMapDto.getApplicationId(), applicationEventFamilyMapDto.getEcfId(), applicationEventFamilyMapDto.getVersion())) {
                applicationEventFamilyMapDto.setCreatedTime(System.currentTimeMillis());
                savedApplicationEventFamilyMap = getDto(applicationEventFamilyMapDao.save(new ApplicationEventFamilyMap(applicationEventFamilyMapDto)));
            } else {
                LOG.debug("Can't save application event family map. Uniqueness violation.");
                throw new IncorrectParameterException("Incorrect application event family map. Uniqueness violation within the application.");
            }
        }
        return savedApplicationEventFamilyMap;
    }

    @Override
    public List<EcfInfoDto> findVacantEventClassFamiliesByApplicationId(
            String applicationId) {
        List<EcfInfoDto> vacantEcfs = new ArrayList<>();
        if (isValidSqlId(applicationId)) {
            ApplicationDto application = getDto(applicationDao.findById(applicationId));
            if (application != null) {
                String tenantId = application.getTenantId();
                List<EventClassFamilyDto> eventClassFamilies = convertDtoList(eventClassFamilyDao.findByTenantId(tenantId));
                List<AefMapInfoDto> aefMaps = findEventClassFamiliesByApplicationId(applicationId);
                List<EcfInfoDto> occupiedEcfs = new ArrayList<>();
                for (AefMapInfoDto aefMap : aefMaps) {
                    EcfInfoDto ecf = new EcfInfoDto();
                    ecf.setEcfId(aefMap.getEcfId());
                    ecf.setEcfName(aefMap.getEcfName());
                    ecf.setVersion(aefMap.getVersion());
                    occupiedEcfs.add(ecf);
                }
                if (eventClassFamilies != null) {
                    for (EventClassFamilyDto eventClassFamily : eventClassFamilies) {
                        if (eventClassFamily.getSchemas() != null) {
                            for (EventSchemaVersionDto eventSchemaVersion : eventClassFamily.getSchemas()) {
                                EcfInfoDto ecf = new EcfInfoDto();
                                ecf.setEcfId(eventClassFamily.getId());
                                ecf.setEcfName(eventClassFamily.getName());
                                ecf.setVersion(eventSchemaVersion.getVersion());
                                if (occupiedEcfs != null && !occupiedEcfs.contains(ecf)) {
                                    vacantEcfs.add(ecf);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw new IncorrectParameterException("Incorrect applicationId id: " + applicationId);
        }
        return vacantEcfs;
    }

    @Override
    public List<AefMapInfoDto> findEventClassFamiliesByApplicationId(
            String applicationId) {
        List<ApplicationEventFamilyMapDto> eventFamilyMaps = findApplicationEventFamilyMapsByApplicationId(applicationId);
        List<AefMapInfoDto> aefMaps = new ArrayList<>();
        if (eventFamilyMaps != null) {
            for (ApplicationEventFamilyMapDto eventFamilyMap : eventFamilyMaps) {
                AefMapInfoDto aefMap = new AefMapInfoDto();
                aefMap.setAefMapId(eventFamilyMap.getId());
                aefMap.setEcfId(eventFamilyMap.getEcfId());
                aefMap.setEcfName(eventFamilyMap.getEcfName());
                aefMap.setVersion(eventFamilyMap.getVersion());
                aefMaps.add(aefMap);
            }
        }
        return aefMaps;
    }



}
