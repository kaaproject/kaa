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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.common.dto.UpdateStatus.ACTIVE;
import static org.kaaproject.kaa.common.dto.UpdateStatus.INACTIVE;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToString;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeNotificationDto;
import org.kaaproject.kaa.common.dto.ChangeProfileFilterNotification;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.EndpointGroupDao;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.ProfileFilterDao;
import org.kaaproject.kaa.server.common.dao.ProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.exception.UpdateStatusConflictException;
import org.kaaproject.kaa.server.common.dao.mongo.model.Application;
import org.kaaproject.kaa.server.common.dao.mongo.model.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileFilter;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileServiceImpl.class);
    private static final String DEFAULT_FILTER_BODY = "true";

    @Autowired
    private ProfileSchemaDao<ProfileSchema> profileSchemaDao;
    @Autowired
    private ProfileFilterDao<ProfileFilter> profileFilterDao;
    @Autowired
    private EndpointGroupDao<EndpointGroup> endpointGroupDao;
    @Autowired
    private HistoryService historyService;

    @Override
    public List<ProfileSchemaDto> findProfileSchemasByAppId(String applicationId) {
        validateId(applicationId, "Can't find profile schema. Invalid application id: " + applicationId);
        return convertDtoList(profileSchemaDao.findByApplicationId(applicationId));
    }

    @Override
    public List<SchemaDto> findProfileSchemaVersionsByAppId(String applicationId) {
        validateId(applicationId, "Can't find profile schemas. Invalid application id: " + applicationId);
        List<ProfileSchema> profileSchemas = profileSchemaDao.findByApplicationId(applicationId);
        List<SchemaDto> schemas = new ArrayList<>();
        for (ProfileSchema profileSchema : profileSchemas) {
            schemas.add(profileSchema.toSchemaDto());
        }
        return schemas;
    }

    @Override
    public ProfileSchemaDto findProfileSchemaById(String id) {
        validateId(id, "Can't find profile schema. Invalid profile schema id: " + id);
        return getDto(profileSchemaDao.findById(id));
    }


    @Override
    public ProfileSchemaDto saveProfileSchema(ProfileSchemaDto profileSchemaDto) {
        validateObject(profileSchemaDto, "Can't save profile schema. Invalid profile schema object.");
        String appId = profileSchemaDto.getApplicationId();
        if (ObjectId.isValid(appId)) {
            String id = profileSchemaDto.getId();
            if (StringUtils.isBlank(id)) {
                ProfileSchema profileSchema = profileSchemaDao.findLatestByAppId(appId);
                int majorVersion = 0;
                if (profileSchema != null) {
                    majorVersion = profileSchema.getMajorVersion();
                }
                profileSchemaDto.setMinorVersion(0);
                profileSchemaDto.setId(null);
                profileSchemaDto.setMajorVersion(++majorVersion);
                profileSchemaDto.setCreatedTime(System.currentTimeMillis());
            } else {
                ProfileSchemaDto oldProfileSchemaDto = getDto(profileSchemaDao.findById(id));
                if (oldProfileSchemaDto != null) {
                    oldProfileSchemaDto.editFields(profileSchemaDto);
                    profileSchemaDto = oldProfileSchemaDto;
                    return getDto(profileSchemaDao.save(new ProfileSchema(profileSchemaDto)));
                } else {
                    LOG.error("Can't find profile schema with given id [{}].", id);
                    throw new IncorrectParameterException("Invalid profile schema id: " + id);
                }
            }
            ProfileSchemaDto profileSchema = getDto(profileSchemaDao.save(new ProfileSchema(profileSchemaDto)));
            if (profileSchema != null) {
                EndpointGroup group = endpointGroupDao.findByAppIdAndWeight(profileSchemaDto.getApplicationId(), 0);
                ProfileFilterDto filter = new ProfileFilterDto();
                filter.setBody(DEFAULT_FILTER_BODY);
                filter.setEndpointGroupId(group.getId());
                filter.setSchemaId(profileSchema.getId());
                filter = saveProfileFilter(filter);
                if (filter != null) {
                    activateProfileFilter(filter.getId(), profileSchema.getCreatedUsername());
                } else {
                    LOG.warn("Can't activate profile filter {}", filter);
                    throw new IncorrectParameterException("Can't save profile filter.");
                }
            } else {
                LOG.warn("Can't save profile schema {}", profileSchemaDto);
                throw new IncorrectParameterException("Can't save profile schema.");
            }
            return profileSchema;
        } else {
            throw new IncorrectParameterException("Invalid profile schema object. Incorrect application id" + appId);
        }
    }

    @Override
    public void removeProfileSchemasByAppId(String applicationId) {
        validateId(applicationId, "Can't remove profile schema. Invalid application id: " + applicationId);
        List<ProfileSchema> schemas = profileSchemaDao.findByApplicationId(applicationId);
        if (schemas != null && !schemas.isEmpty()) {
            LOG.debug("Remove profile shemas by application id {}", applicationId);
            for (ProfileSchema schema : schemas) {
                removeProfileSchemaById(schema.getId());
            }
        }
    }

    @Override
    public void removeProfileSchemaById(String id) {
        validateId(id, "Can't remove profile schema. Invalid profile schema id: " + id);
        removeProfileFiltersByProfileSchemaId(id);
        profileSchemaDao.removeById(id);
        LOG.debug("Removed profile schema [{}] with filters.", id);
    }

    @Override
    public Collection<StructureRecordDto<ProfileFilterDto>> findAllProfileFilterRecordsByEndpointGroupId(
            String endpointGroupId, boolean includeDeprecated) {
        Collection<ProfileFilterDto> profileFilters = convertDtoList(profileFilterDao.findActualByEndpointGroupId(endpointGroupId));
        List<StructureRecordDto<ProfileFilterDto>> records = StructureRecordDto.convertToRecords(profileFilters);
        if (includeDeprecated) {
            List<SchemaDto> schemas = findVacantSchemasByEndpointGroupId(endpointGroupId);
            for (SchemaDto schema : schemas) {
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
    public StructureRecordDto<ProfileFilterDto> findProfileFilterRecordBySchemaIdAndEndpointGroupId(
            String schemaId, String endpointGroupId) {
        StructureRecordDto<ProfileFilterDto> record = new StructureRecordDto<>();
        Collection<ProfileFilterDto> profileFilters = convertDtoList(profileFilterDao.findActualBySchemaIdAndGroupId(schemaId, endpointGroupId));
        if (profileFilters != null) {
            for (ProfileFilterDto profileFilter : profileFilters) {
                if (profileFilter.getStatus()==UpdateStatus.ACTIVE) {
                    record.setActiveStructureDto(profileFilter);
                } else if (profileFilter.getStatus()==UpdateStatus.INACTIVE) {
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
    public List<SchemaDto> findVacantSchemasByEndpointGroupId(String endpointGroupId) {
        validateId(endpointGroupId, "Can't find vacant schemas. Invalid endpoint group id: " + endpointGroupId);
        EndpointGroup group = endpointGroupDao.findById(endpointGroupId);
        List<ProfileFilter> profileFilters = profileFilterDao.findActualByEndpointGroupId(endpointGroupId);
        List<String> usedSchemaIds = new ArrayList<>();
        for (ProfileFilter filter : profileFilters) {
            usedSchemaIds.add(idToString(filter.getSchemaId()));
        }
        List<ProfileSchema> schemas = profileSchemaDao.findVacantSchemas(idToString(group.getApplicationId()), usedSchemaIds);
        List<SchemaDto> schemaDtoList = new ArrayList<>();
        for (ProfileSchema schema : schemas) {
            schemaDtoList.add(schema.toSchemaDto());
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
            String schemaId = profileFilterDto.getSchemaId();
            String groupId = profileFilterDto.getEndpointGroupId();
            EndpointGroup group = endpointGroupDao.findById(groupId);
            if (group.getWeight() == 0) {
                long count = profileFilterDao.findActiveFilterCount(schemaId, groupId);
                if (count > 0) {
                    throw new UpdateStatusConflictException("Can't create more than one profile filter, for default endpoint group");
                }
            }
            ProfileSchemaDto profileSchemaDto = findProfileSchemaById(schemaId);
            if (profileSchemaDto != null) {
                ProfileFilter inactiveFilter = profileFilterDao.findInactiveFilter(schemaId, groupId);
                ProfileFilter latestFilter = profileFilterDao.findLatestFilter(schemaId, groupId);
                if (inactiveFilter != null) {
                    profileFilterDto.setId(inactiveFilter.getId());
                    profileFilterDto.setSequenceNumber(inactiveFilter.getSequenceNumber());
                } else if (latestFilter != null) {
                    profileFilterDto.setSequenceNumber(latestFilter.getSequenceNumber());
                }
                profileFilterDto.setApplicationId(profileSchemaDto.getApplicationId());
                profileFilterDto.setMajorVersion(profileSchemaDto.getMajorVersion());
                profileFilterDto.setMinorVersion(profileSchemaDto.getMinorVersion());
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
                ObjectId schemaId = oldProfileFilter.getSchemaId();
                ObjectId groupId = oldProfileFilter.getEndpointGroupId();
                if (schemaId != null && groupId != null) {
                    profileFilterDao.deactivateOldFilter(schemaId.toString(), groupId.toString(), activatedUsername);
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
            ObjectId oid = oldProfileFilter.getEndpointGroupId();
            if (oid != null) {
                EndpointGroup group = endpointGroupDao.findById(oid.toString());
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
    public ChangeProfileFilterNotification deleteProfileFilterRecord(String schemaId, String groupId, String deactivatedUsername) {
        ChangeProfileFilterNotification profileNotification = null;
        validateId(schemaId, "Incorrect profile schema id " + schemaId + ".");
        validateId(groupId, "Incorrect group id " + groupId + ".");
        ProfileFilterDto profileFilterDto = getDto(profileFilterDao.deactivateOldFilter(schemaId.toString(), groupId.toString(), deactivatedUsername));
        if (profileFilterDto != null) {
            HistoryDto historyDto = addHistory(profileFilterDto, ChangeType.REMOVE_PROF);
            ChangeNotificationDto changeNotificationDto = createNotification(profileFilterDto, historyDto);
            profileNotification = new ChangeProfileFilterNotification();
            profileNotification.setProfileFilterDto(profileFilterDto);
            profileNotification.setChangeNotificationDto(changeNotificationDto);
        }
        ProfileFilter profileFilter = profileFilterDao.findInactiveFilter(schemaId, groupId);
        if (profileFilter != null) {
            profileFilterDao.removeById(profileFilter.getId());
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
                    changeNotificationDto.setGroupId(group.getId());
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

    @Override
    public void removeProfileFiltersByProfileSchemaId(String profileSchemaId) {
        validateId(profileSchemaId, "Can't remove profile filter. Invalid profile schema id: " + profileSchemaId);
        List<ProfileFilter> filters = profileFilterDao.findAllByProfileSchemaId(profileSchemaId);
        if (filters != null && !filters.isEmpty()) {
            LOG.debug("Removing profile schema by id [{}] and corresponding filters", profileSchemaId);
            for (ProfileFilter filter : filters) {
                removeProfileFilter(filter.getId(), true);
            }
        }
    }

    private void removeProfileFilter(String id, boolean forceRemove) {
        ProfileFilter pf = profileFilterDao.findById(id);
        if (pf != null && pf.getEndpointGroupId() != null) {
            EndpointGroup group = endpointGroupDao.findById(pf.getEndpointGroupId().toString());
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
    public List<ProfileFilterDto> findProfileFilterByAppIdAndVersion(String appId, int schemaVersion) {
        validateId(appId, "Can't find profile filter. Invalid application id: " + appId);
        return convertDtoList(profileFilterDao.findByAppIdAndSchemaVersion(appId, schemaVersion));
    }

    @Override
    public ProfileSchemaDto findProfileSchemaByAppIdAndVersion(String appId, int schemaVersion) {
        validateId(appId, "Can't find profile schema. Invalid application id: " + appId);
        return getDto(profileSchemaDao.findByAppIdAndVersion(appId, schemaVersion));
    }

    @Override
    public ProfileFilterDto findLatestFilterBySchemaIdAndGroupId(String schemaId, String groupId) {
        validateId(schemaId, "Can't find profile filter. Invalid profile schema id: " + schemaId);
        validateId(groupId, "Can't find profile filter. Invalid group id: " + groupId);
        return getDto(profileFilterDao.findLatestFilter(schemaId, groupId));
    }

    private HistoryDto addHistory(ProfileFilterDto dto, ChangeType type) {
        LOG.debug("Add history information about profile filter update with change type {} ", type);
        HistoryDto history = new HistoryDto();
        history.setApplicationId(dto.getApplicationId());
        ChangeDto change = new ChangeDto();
        change.setProfileFilterId(dto.getId());
        change.setPfMajorVersion(dto.getMajorVersion());
        change.setEndpointGroupId(dto.getEndpointGroupId());
        change.setType(type);
        history.setChange(change);
        return historyService.saveHistory(history);
    }

    private void validateFilter(ProfileFilterDto dto) {
        validateObject(dto, "Can't save profile filter. Incorrect object.");
        if (StringUtils.isBlank(dto.getSchemaId())) {
            throw new IncorrectParameterException("Profile Filter object invalid. Profile Schema id invalid:"
                    + dto.getSchemaId());
        }
        if (StringUtils.isBlank(dto.getEndpointGroupId())) {
            throw new IncorrectParameterException("Profile Filter object invalid. Endpoint Group id invalid:"
                    + dto.getEndpointGroupId());
        }
    }
}
