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

package org.kaaproject.kaa.server.common.dao.service;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeNotificationDto;
import org.kaaproject.kaa.common.dto.ChangeProfileFilterNotification;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.exception.UpdateStatusConflictException;
import org.kaaproject.kaa.server.common.dao.impl.EndpointGroupDao;
import org.kaaproject.kaa.server.common.dao.impl.ProfileFilterDao;
import org.kaaproject.kaa.server.common.dao.impl.ProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointProfileSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.ProfileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.common.dto.UpdateStatus.ACTIVE;
import static org.kaaproject.kaa.common.dto.UpdateStatus.INACTIVE;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateSqlId;

@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileServiceImpl.class);

    @Autowired
    private ProfileSchemaDao<EndpointProfileSchema> profileSchemaDao;
    @Autowired
    private ProfileFilterDao<ProfileFilter> profileFilterDao;
    @Autowired
    private EndpointGroupDao<EndpointGroup> endpointGroupDao;
    @Autowired
    private HistoryService historyService;

    @Override
    public List<EndpointProfileSchemaDto> findProfileSchemasByAppId(String applicationId) {
        validateId(applicationId, "Can't find profile schema. Invalid application id: " + applicationId);
        return convertDtoList(profileSchemaDao.findByApplicationId(applicationId));
    }

    @Override
    public List<VersionDto> findProfileSchemaVersionsByAppId(String applicationId) {
        validateId(applicationId, "Can't find profile schemas. Invalid application id: " + applicationId);
        List<EndpointProfileSchema> endpointProfileSchemas = profileSchemaDao.findByApplicationId(applicationId);
        List<VersionDto> schemas = new ArrayList<>();
        for (EndpointProfileSchema endpointProfileSchema : endpointProfileSchemas) {
            schemas.add(endpointProfileSchema.toVersionDto());
        }
        return schemas;
    }

    @Override
    public EndpointProfileSchemaDto findProfileSchemaById(String id) {
        validateId(id, "Can't find profile schema. Invalid profile schema id: " + id);
        return getDto(profileSchemaDao.findById(id));
    }


    @Override
    public EndpointProfileSchemaDto saveProfileSchema(EndpointProfileSchemaDto profileSchemaDto) {
        if (profileSchemaDto == null) {
            throw new IncorrectParameterException("Can't save profile schema. Invalid profile schema object.");
        }
        String appId = profileSchemaDto.getApplicationId();
        if (isNotBlank(appId)) {
            String id = profileSchemaDto.getId();
            if (StringUtils.isBlank(id)) {
                EndpointProfileSchema endpointProfileSchema = profileSchemaDao.findLatestByAppId(appId);
                int version = -1;
                if (endpointProfileSchema != null) {
                    version = endpointProfileSchema.getVersion();
                }
                profileSchemaDto.setId(null);
                profileSchemaDto.setVersion(++version);
                profileSchemaDto.setCreatedTime(System.currentTimeMillis());
            } else {
                EndpointProfileSchemaDto oldProfileSchemaDto = getDto(profileSchemaDao.findById(id));
                if (oldProfileSchemaDto != null) {
                    oldProfileSchemaDto.editFields(profileSchemaDto);
                    profileSchemaDto = oldProfileSchemaDto;
                    return getDto(profileSchemaDao.save(new EndpointProfileSchema(profileSchemaDto)));
                } else {
                    LOG.error("Can't find profile schema with given id [{}].", id);
                    throw new IncorrectParameterException("Invalid profile schema id: " + id);
                }
            }
            return getDto(profileSchemaDao.save(new EndpointProfileSchema(profileSchemaDto)));
        } else {
            throw new IncorrectParameterException("Invalid profile schema object. Incorrect application id" + appId);
        }
    }

    @Override
    public void removeProfileSchemasByAppId(String applicationId) {
        validateId(applicationId, "Can't remove profile schema. Invalid application id: " + applicationId);
        List<EndpointProfileSchema> schemas = profileSchemaDao.findByApplicationId(applicationId);
        if (schemas != null && !schemas.isEmpty()) {
            LOG.debug("Remove profile shemas by application id {}", applicationId);
            for (EndpointProfileSchema schema : schemas) {
                removeProfileSchemaById(schema.getId().toString());
            }
        }
    }

    @Override
    public void removeProfileSchemaById(String id) {
        validateSqlId(id, "Can't remove profile schema. Invalid profile schema id: " + id);
//        removeProfileFiltersByProfileSchemaId(id);
        profileSchemaDao.removeById(id);
        LOG.debug("Removed profile schema [{}] with filters.", id);
    }

    @Override
    public Collection<StructureRecordDto<ProfileFilterDto>> findAllProfileFilterRecordsByEndpointGroupId(
            String endpointGroupId, boolean includeDeprecated) {
        Collection<ProfileFilterDto> profileFilters = convertDtoList(profileFilterDao.findActualByEndpointGroupId(endpointGroupId));
        List<StructureRecordDto<ProfileFilterDto>> records = StructureRecordDto.convertToRecords(profileFilters);
        if (includeDeprecated) {
            List<ProfileVersionPairDto> schemas = findVacantSchemasByEndpointGroupId(endpointGroupId);
            for (ProfileVersionPairDto schema : schemas) {
                ProfileFilterDto deprecatedProfileFilter = getDto(profileFilterDao.findLatestDeprecated(schema.getId(), endpointGroupId));
                if (deprecatedProfileFilter != null) {
                    StructureRecordDto<ProfileFilterDto> record = new StructureRecordDto<>();
                    record.setActiveStructureDto(deprecatedProfileFilter);
                    records.add(record);
                }
            }
        }
        Collections.sort(records);
        return records;
    }

    @Override
    public StructureRecordDto<ProfileFilterDto> findProfileFilterRecordBySchemaIdAndEndpointGroupId(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) {
        StructureRecordDto<ProfileFilterDto> record = new StructureRecordDto<>();
        Collection<ProfileFilterDto> profileFilters = convertDtoList(profileFilterDao.findActualBySchemaIdAndGroupId(schemaId, endpointGroupId));
        if (profileFilters != null) {
            for (ProfileFilterDto profileFilter : profileFilters) {
                if (profileFilter.getStatus() == UpdateStatus.ACTIVE) {
                    record.setActiveStructureDto(profileFilter);
                } else if (profileFilter.getStatus() == UpdateStatus.INACTIVE) {
                    record.setInactiveStructureDto(profileFilter);
                }
            }
        }
        if (!record.hasActive()) {
            ProfileFilterDto deprecatedProfileFilter = getDto(profileFilterDao.findLatestDeprecated(schemaId, endpointGroupId));
            if (deprecatedProfileFilter != null) {
                record.setActiveStructureDto(deprecatedProfileFilter);
            }
        }
        if (record.isEmpty()) {
            LOG.debug("Can't find related profile filter record for schema {} and group {}.", schemaId, endpointGroupId);
            throw new IncorrectParameterException("Profile filter record not found, schemaId: " + schemaId + ", endpointGroupId: " + endpointGroupId);
        }
        return record;
    }

    @Override
    public List<ProfileVersionPairDto> findVacantSchemasByEndpointGroupId(String endpointGroupId) {
        validateId(endpointGroupId, "Can't find vacant schemas. Invalid endpoint group id: " + endpointGroupId);
        EndpointGroup group = endpointGroupDao.findById(endpointGroupId);
        List<ProfileFilter> profileFilters = profileFilterDao.findActualByEndpointGroupId(endpointGroupId);
        List<String> usedSchemaIds = new ArrayList<>();
        for (ProfileFilter filter : profileFilters) {
            usedSchemaIds.add(filter.getSchemaId());
        }
        List<EndpointProfileSchema> schemas = profileSchemaDao.findVacantSchemas(group.getApplicationId(), usedSchemaIds);
        List<ProfileVersionPairDto> schemaDtoList = new ArrayList<>();
        for (EndpointProfileSchema schema : schemas) {
            schemaDtoList.add(schema.toVersionDto());
        }
        return schemaDtoList;
    }


    @Override
    public ProfileFilterDto findProfileFilterById(String id) {
        validateId(id, "Can't find profile filter. Invalid profile filter id: " + id);
        return getDto(profileFilterDao.findById(id));
    }

    @Override
    public ProfileFilterDto saveProfileFilter(ProfileFilterDto profileFilterDto) {
        validateFilter(profileFilterDto);
        String id = profileFilterDto.getId();
        if (isNotBlank(id)) {
            ProfileFilterDto oldProfileFilter = findProfileFilterById(id);
            if (oldProfileFilter != null && oldProfileFilter.getStatus() != INACTIVE) {
                throw new UpdateStatusConflictException("Can't update profile filter, invalid old profile filter with id " + id);
            }
        } else {
            String groupId = profileFilterDto.getEndpointGroupId();
            EndpointGroup group = endpointGroupDao.findById(groupId);
            if (group.getWeight() == 0) {
                long count = profileFilterDao.findActiveFilterCount(schemaId, groupId);
                if (count > 0) {
                    throw new UpdateStatusConflictException("Can't create more than one profile filter, for default endpoint group");
                }
            }
            EndpointProfileSchemaDto profileSchemaDto = findProfileSchemaById(schemaId);
            if (profileSchemaDto != null) {
                ProfileFilter inactiveFilter = profileFilterDao.findInactiveFilter(schemaId, groupId);
                ProfileFilter latestFilter = profileFilterDao.findLatestFilter(schemaId, groupId);
                if (inactiveFilter != null) {
                    profileFilterDto.setId(inactiveFilter.getId().toString());
                    profileFilterDto.setSequenceNumber(inactiveFilter.getSequenceNumber());
                } else if (latestFilter != null) {
                    profileFilterDto.setSequenceNumber(latestFilter.getSequenceNumber());
                }
                profileFilterDto.setApplicationId(profileSchemaDto.getApplicationId());
                profileFilterDto.setCreatedTime(System.currentTimeMillis());
            } else {
                throw new IncorrectParameterException("Can't update profile filter, invalid profile schema id " + id);
            }
        }
        profileFilterDto.setStatus(UpdateStatus.INACTIVE);
        profileFilterDto.setLastModifyTime(System.currentTimeMillis());
        return getDto(profileFilterDao.save(new ProfileFilter(profileFilterDto)));
    }

    @Override
    public ChangeProfileFilterNotification activateProfileFilter(String id, String activatedUsername) {
        ChangeProfileFilterNotification profileNotification;
        ProfileFilterDto profileFilter;
        validateId(id, "Can't activate profile filter. Invalid profile filter id: " + id);
        ProfileFilter oldProfileFilter = profileFilterDao.findById(id);
        if (oldProfileFilter != null) {
            UpdateStatus status = oldProfileFilter.getStatus();
            if (status != null && status == INACTIVE) {
                String schemaId = oldProfileFilter.getSchemaId();
                String groupId = oldProfileFilter.getGroupId();
                if (schemaId != null && groupId != null) {
                    profileFilterDao.deactivateOldFilter(schemaId, groupId, activatedUsername);
                } else {
                    throw new DatabaseProcessingException("Incorrect old profile filters. Profile schema id is empty.");
                }
                profileFilter = getDto(profileFilterDao.activate(id, activatedUsername));
                if (profileFilter != null) {
                    HistoryDto history = addHistory(profileFilter, ChangeType.ADD_PROF);
                    ChangeNotificationDto changeNotificationDto = createNotification(profileFilter, history);
                    profileNotification = new ChangeProfileFilterNotification();
                    profileNotification.setProfileFilterDto(profileFilter);
                    profileNotification.setChangeNotificationDto(changeNotificationDto);
                } else {
                    throw new DatabaseProcessingException("Can't activate profile filter.");
                }
            } else {
                throw new UpdateStatusConflictException("Incorrect status for activating profile filter " + status);
            }
        } else {
            throw new IncorrectParameterException("Can't find profile filter with id " + id);
        }
        return profileNotification;
    }

    @Override
    public ChangeProfileFilterNotification deactivateProfileFilter(String id, String deactivatedUsername) {
        ChangeProfileFilterNotification profileNotification;
        validateId(id, "Incorrect profile filter id. Can't deactivate profile filter with id " + id);
        ProfileFilter oldProfileFilter = profileFilterDao.findById(id);
        if (oldProfileFilter != null) {
            UpdateStatus status = oldProfileFilter.getStatus();
            String oid = oldProfileFilter.getGroupId();
            if (oid != null) {
                EndpointGroup group = endpointGroupDao.findById(oid);
                if (group != null && group.getWeight() == 0) {
                    throw new IncorrectParameterException("Can't deactivate default profile filter");
                }
            }
            if (status != null && status == ACTIVE) {
                ProfileFilterDto profileFilterDto = getDto(profileFilterDao.deactivate(id, deactivatedUsername));
                HistoryDto historyDto = addHistory(profileFilterDto, ChangeType.REMOVE_PROF);
                ChangeNotificationDto changeNotificationDto = createNotification(profileFilterDto, historyDto);
                profileNotification = new ChangeProfileFilterNotification();
                profileNotification.setProfileFilterDto(profileFilterDto);
                profileNotification.setChangeNotificationDto(changeNotificationDto);
            } else {
                throw new UpdateStatusConflictException("Incorrect status for activating profile filter " + status);
            }
        } else {
            throw new IncorrectParameterException("Can't find profile filter with id " + id);
        }
        return profileNotification;
    }

    @Override
    public ChangeProfileFilterNotification deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String groupId, String deactivatedUsername) {
        ChangeProfileFilterNotification profileNotification = null;
        validateId(schemaId, "Incorrect profile schema id " + schemaId + ".");
        validateId(groupId, "Incorrect group id " + groupId + ".");
        ProfileFilterDto profileFilterDto = getDto(profileFilterDao.deactivateOldFilter(schemaId, groupId, deactivatedUsername));
        if (profileFilterDto != null) {
            HistoryDto historyDto = addHistory(profileFilterDto, ChangeType.REMOVE_PROF);
            ChangeNotificationDto changeNotificationDto = createNotification(profileFilterDto, historyDto);
            profileNotification = new ChangeProfileFilterNotification();
            profileNotification.setProfileFilterDto(profileFilterDto);
            profileNotification.setChangeNotificationDto(changeNotificationDto);
        }
        ProfileFilter profileFilter = profileFilterDao.findInactiveFilter(schemaId, groupId);
        if (profileFilter != null) {
            profileFilterDao.removeById(profileFilter.getId().toString());
        }
        return profileNotification;
    }

    private ChangeNotificationDto createNotification(ProfileFilterDto profileFilter, HistoryDto historyDto) {
        LOG.debug("Create notification after profile filter update.");
        ChangeNotificationDto changeNotificationDto = null;
        if (historyDto != null) {
            changeNotificationDto = new ChangeNotificationDto();
            changeNotificationDto.setAppId(profileFilter.getApplicationId());
            changeNotificationDto.setAppSeqNumber(historyDto.getSequenceNumber());
            String endpointGroupId = profileFilter.getEndpointGroupId();
            if (isValidId(endpointGroupId)) {
                EndpointGroup group = endpointGroupDao.findById(endpointGroupId);
                if (group != null) {
                    changeNotificationDto.setGroupId(group.getId().toString());
                    changeNotificationDto.setGroupSeqNumber(group.getSequenceNumber());
                } else {
                    LOG.debug("Can't find endpoint group by id [{}].", endpointGroupId);
                }
            } else {
                LOG.debug("Incorrect endpoint group id [{}].", endpointGroupId);
            }
        } else {
            LOG.debug("Can't save history information.");
        }
        return changeNotificationDto;
    }

    private void removeProfileFilter(String id, boolean forceRemove) {
        ProfileFilter pf = profileFilterDao.findById(id);
        if (pf != null && pf.getGroupId() != null) {
            EndpointGroup group = endpointGroupDao.findById(pf.getGroupId());
            if (group != null) {
                if (group.getWeight() != 0 || forceRemove) {
                    profileFilterDao.removeById(id);
                    LOG.trace("Removed profile filter by id [{}] ", id);
                    addHistory(pf.toDto(), ChangeType.REMOVE_PROF);
                } else {
                    LOG.warn("Can't delete default profile filter by id [{}]", id);
                }
            }
        }
    }

    @Override
    public List<ProfileFilterDto> findProfileFiltersByAppIdAndVersions(String appId, int endpointSchemaVersion, int serverSchemaVersion) {
        validateId(appId, "Can't find profile filter. Invalid application id: " + appId);
        return convertDtoList(profileFilterDao.findByAppIdAndSchemaVersions(appId, endpointSchemaVersion, serverSchemaVersion));
    }

    @Override
    public EndpointProfileSchemaDto findProfileSchemaByAppIdAndVersion(String appId, int schemaVersion) {
        validateId(appId, "Can't find profile schema. Invalid application id: " + appId);
        return getDto(profileSchemaDao.findByAppIdAndVersion(appId, schemaVersion));
    }

    @Override
    public ProfileFilterDto findLatestFilterBySchemaIdsAndGroupId(String endpointProfileSchemaId, String serverProfileSchemaId, String groupId) {
        validateId(endpointProfileSchemaId, "Can't find profile filter. Invalid endpoint profile schema id: " + endpointProfileSchemaId);
        validateId(serverProfileSchemaId, "Can't find profile filter. Invalid server profile schema id: " + serverProfileSchemaId);
        validateId(groupId, "Can't find profile filter. Invalid group id: " + groupId);
        return getDto(profileFilterDao.findLatestFilter(endpointProfileSchemaId, groupId));
    }

    private HistoryDto addHistory(ProfileFilterDto dto, ChangeType type) {
        LOG.debug("Add history information about profile filter update with change type {} ", type);
        HistoryDto history = new HistoryDto();
        history.setApplicationId(dto.getApplicationId());
        ChangeDto change = new ChangeDto();
        change.setProfileFilterId(dto.getId());
        change.setProfileFilterId(dto.getId());
        change.setEndpointGroupId(dto.getEndpointGroupId());
        change.setType(type);
        history.setChange(change);
        return historyService.saveHistory(history);
    }

    private void validateFilter(ProfileFilterDto dto) {
        if (dto == null) {
            throw new IncorrectParameterException("Can't save profile filter. Incorrect object.");
        }
        if (dto.getEndpointProfileSchemaId() == null && dto.getServerProfileSchemaId() == null) {
            throw new IncorrectParameterException("Profile Filter object invalid. Both schemas are empty.");
        }
        if (StringUtils.isBlank(dto.getEndpointGroupId())) {
            throw new IncorrectParameterException("Profile Filter object invalid. Endpoint Group id invalid:"
                    + dto.getEndpointGroupId());
        }
    }
}
