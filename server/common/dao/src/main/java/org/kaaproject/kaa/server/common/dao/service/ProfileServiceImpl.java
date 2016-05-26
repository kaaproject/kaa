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

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeNotificationDto;
import org.kaaproject.kaa.common.dto.ChangeProfileFilterNotification;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.ServerProfileService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.exception.NotFoundException;
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
import java.util.HashSet;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
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
    @Autowired
    private ServerProfileService serverProfileService;

    @Override
    public List<EndpointProfileSchemaDto> findProfileSchemasByAppId(String applicationId) {
        validateSqlId(applicationId, "Can't find profile schema. Invalid application id: " + applicationId);
        return convertDtoList(profileSchemaDao.findByApplicationId(applicationId));
    }

    @Override
    public List<VersionDto> findProfileSchemaVersionsByAppId(String applicationId) {
        validateSqlId(applicationId, "Can't find profile schemas. Invalid application id: " + applicationId);
        List<EndpointProfileSchema> endpointProfileSchemas = profileSchemaDao.findByApplicationId(applicationId);
        List<VersionDto> schemas = new ArrayList<>();
        for (EndpointProfileSchema endpointProfileSchema : endpointProfileSchemas) {
            schemas.add(endpointProfileSchema.toVersionDto());
        }
        return schemas;
    }

    @Override
    public EndpointProfileSchemaDto findProfileSchemaById(String id) {
        validateSqlId(id, "Can't find profile schema. Invalid profile schema id: " + id);
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
        validateSqlId(applicationId, "Can't remove profile schema. Invalid application id: " + applicationId);
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
        profileSchemaDao.removeById(id);
        LOG.debug("Removed profile schema [{}] with filters.", id);
    }

    @Override
    public Collection<ProfileFilterRecordDto> findAllProfileFilterRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated) {
        Collection<ProfileFilterDto> profileFilters = convertDtoList(profileFilterDao.findActualByEndpointGroupId(endpointGroupId));
        List<ProfileFilterRecordDto> records = ProfileFilterRecordDto.convertToProfileFilterRecords(profileFilters);
        if (includeDeprecated) {
            List<ProfileVersionPairDto> versions = findVacantSchemasByEndpointGroupId(endpointGroupId);
            for (ProfileVersionPairDto version : versions) {
                ProfileFilterDto deprecatedProfileFilter = getDto(profileFilterDao.findLatestDeprecated(
                        version.getEndpointProfileSchemaid(), version.getServerProfileSchemaid(), endpointGroupId));
                if (deprecatedProfileFilter != null) {
                    ProfileFilterRecordDto record = new ProfileFilterRecordDto();
                    record.setActiveStructureDto(deprecatedProfileFilter);
                    records.add(record);
                }
            }
        }
        Collections.sort(records);
        return records;
    }

    @Override
    public ProfileFilterRecordDto findProfileFilterRecordBySchemaIdAndEndpointGroupId(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) {
        validateFilterSchemaIds(endpointProfileSchemaId, serverProfileSchemaId);
        ProfileFilterRecordDto record = new ProfileFilterRecordDto();
        Collection<ProfileFilterDto> profileFilters = convertDtoList(profileFilterDao.findActualBySchemaIdAndGroupId(endpointProfileSchemaId,
                serverProfileSchemaId, endpointGroupId));
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
            ProfileFilterDto deprecatedProfileFilter = getDto(profileFilterDao.findLatestDeprecated(endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId));
            if (deprecatedProfileFilter != null) {
                record.setActiveStructureDto(deprecatedProfileFilter);
            }
        }
        if (record.isEmpty()) {
            LOG.debug("Can't find related profile filter record for endpoint schema {}, server schema {} and group {}.", endpointProfileSchemaId,
                    serverProfileSchemaId, endpointGroupId);
            throw new NotFoundException("Profile filter record not found, endpointProfileSchemaId: " + endpointProfileSchemaId
                    + ", serverProfileSchemaId: " + serverProfileSchemaId + " endpointGroupId: " + endpointGroupId);
        }
        return record;
    }

    @Override
    public List<ProfileVersionPairDto> findVacantSchemasByEndpointGroupId(String endpointGroupId) {
        validateSqlId(endpointGroupId, "Can't find vacant schemas. Invalid endpoint group id: " + endpointGroupId);
        EndpointGroup group = endpointGroupDao.findById(endpointGroupId);
        List<ProfileFilter> profileFilters = profileFilterDao.findActualByEndpointGroupId(endpointGroupId);
        String appId = group.getApplicationId();
        List<ServerProfileSchemaDto> serverSchemas = serverProfileService.findServerProfileSchemasByAppId(appId);
        Collections.sort(serverSchemas);
        List<EndpointProfileSchemaDto> endpointProfileSchemas = findProfileSchemasByAppId(appId);
        Collections.sort(endpointProfileSchemas);

        Collection<ProfileVersionPairDto> pairVersionSet = new HashSet<>();
        for (int i = 0; i < endpointProfileSchemas.size(); i++) {
            EndpointProfileSchemaDto endSchema = endpointProfileSchemas.get(i);
            for (ServerProfileSchemaDto serverSchema : serverSchemas) {
                if (i == 0) {
                    pairVersionSet.add(new ProfileVersionPairDto(serverSchema.getId(), serverSchema.getVersion()));
                }
                pairVersionSet.add(new ProfileVersionPairDto(endSchema.getId(), endSchema.getVersion(), serverSchema.getId(), serverSchema.getVersion()));
            }
            pairVersionSet.add(new ProfileVersionPairDto(endSchema.getVersion(), endSchema.getId()));
        }
        for (ProfileFilter pf : profileFilters) {
            pairVersionSet.remove(new ProfileVersionPairDto(pf.getEndpointProfileSchemaId(), pf.getEndpointProfileSchemaVersion(),
                    pf.getServerProfileSchemaId(), pf.getServerProfileSchemaVersion()));
        }
        return new ArrayList<>(pairVersionSet);
    }


    @Override
    public ProfileFilterDto findProfileFilterById(String id) {
        validateSqlId(id, "Can't find profile filter. Invalid profile filter id: " + id);
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
            String endProfSchemaId = profileFilterDto.getEndpointProfileSchemaId();
            String srvProfSchemaId = profileFilterDto.getServerProfileSchemaId();
            String groupId = profileFilterDto.getEndpointGroupId();
            EndpointGroup group = endpointGroupDao.findById(groupId);
            if (group.getWeight() == 0) {
                throw new UpdateStatusConflictException("Add profile filter to default group is forbidden!");
            }
            EndpointProfileSchemaDto endpointProfileSchemaDto = null;
            if (endProfSchemaId != null) {
                endpointProfileSchemaDto = findProfileSchemaById(endProfSchemaId);
                if (endpointProfileSchemaDto == null) {
                    throw new IncorrectParameterException("Can't update profile filter, endpoint profile schema not found!");
                }
            }
            ServerProfileSchemaDto serverProfileSchemaDto = null;
            if (srvProfSchemaId != null) {
                serverProfileSchemaDto = serverProfileService.findServerProfileSchema(srvProfSchemaId);
                if (serverProfileSchemaDto == null) {
                    throw new IncorrectParameterException("Can't update profile filter, server profile schema not found!");
                }
            }
            if (endpointProfileSchemaDto != null || serverProfileSchemaDto != null) {
                ProfileFilter inactiveFilter = profileFilterDao.findInactiveFilter(endProfSchemaId, srvProfSchemaId, groupId);
                ProfileFilter latestFilter = profileFilterDao.findLatestFilter(endProfSchemaId, srvProfSchemaId, groupId);
                if (inactiveFilter != null) {
                    profileFilterDto.setId(inactiveFilter.getId().toString());
                    profileFilterDto.setSequenceNumber(inactiveFilter.getSequenceNumber());
                } else if (latestFilter != null) {
                    profileFilterDto.setSequenceNumber(latestFilter.getSequenceNumber());
                }
                if (endpointProfileSchemaDto != null) {
                    profileFilterDto.setApplicationId(endpointProfileSchemaDto.getApplicationId());
                } else {
                    profileFilterDto.setApplicationId(serverProfileSchemaDto.getApplicationId());
                }
                profileFilterDto.setCreatedTime(System.currentTimeMillis());
            } else {
                throw new IncorrectParameterException("Can't update profile filter, profile schemas not set!");
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
        validateSqlId(id, "Can't activate profile filter. Invalid profile filter id: " + id);
        ProfileFilter oldProfileFilter = profileFilterDao.findById(id);
        if (oldProfileFilter != null) {
            UpdateStatus status = oldProfileFilter.getStatus();
            if (status != null && status == INACTIVE) {
                String endProfSchemaId = oldProfileFilter.getEndpointProfileSchemaId();
                String srvProfSchemaId = oldProfileFilter.getServerProfileSchemaId();
                String groupId = oldProfileFilter.getGroupId();
                if (groupId != null) {
                    profileFilterDao.deactivateOldFilter(endProfSchemaId, srvProfSchemaId, groupId, activatedUsername);
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
        validateSqlId(id, "Incorrect profile filter id. Can't deactivate profile filter with id " + id);
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
    public ChangeProfileFilterNotification deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId,
                                                                     String groupId, String deactivatedUsername) {
        validateFilterSchemaIds(endpointProfileSchemaId, serverProfileSchemaId);
        validateSqlId(groupId, "Incorrect group id " + groupId + ".");
        ChangeProfileFilterNotification profileNotification = null;
        ProfileFilterDto profileFilterDto = getDto(profileFilterDao.deactivateOldFilter(endpointProfileSchemaId, serverProfileSchemaId,
                groupId, deactivatedUsername));
        if (profileFilterDto != null) {
            HistoryDto historyDto = addHistory(profileFilterDto, ChangeType.REMOVE_PROF);
            ChangeNotificationDto changeNotificationDto = createNotification(profileFilterDto, historyDto);
            profileNotification = new ChangeProfileFilterNotification();
            profileNotification.setProfileFilterDto(profileFilterDto);
            profileNotification.setChangeNotificationDto(changeNotificationDto);
        }
        ProfileFilter profileFilter = profileFilterDao.findInactiveFilter(endpointProfileSchemaId, serverProfileSchemaId, groupId);
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

    @Override
    public List<ProfileFilterDto> findProfileFiltersByAppIdAndVersionsCombination(String appId, int endpointSchemaVersion, int serverSchemaVersion) {
        validateId(appId, "Can't find profile filter. Invalid application id: " + appId);
        return convertDtoList(profileFilterDao.findByAppIdAndSchemaVersionsCombination(appId, endpointSchemaVersion, serverSchemaVersion));
    }

    @Override
    public EndpointProfileSchemaDto findProfileSchemaByAppIdAndVersion(String appId, int schemaVersion) {
        validateId(appId, "Can't find profile schema. Invalid application id: " + appId);
        return getDto(profileSchemaDao.findByAppIdAndVersion(appId, schemaVersion));
    }

    @Override
    public ProfileFilterDto findLatestFilterBySchemaIdsAndGroupId(String endpointProfileSchemaId, String serverProfileSchemaId, String groupId) {
        validateFilterSchemaIds(endpointProfileSchemaId, serverProfileSchemaId);
        validateId(groupId, "Can't find profile filter. Invalid group id: " + groupId);
        return getDto(profileFilterDao.findLatestFilter(endpointProfileSchemaId, serverProfileSchemaId, groupId));
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

    private void validateFilterSchemaIds(String endpointProfileSchemaId, String serverProfileSchemaId) {
        if (isBlank(endpointProfileSchemaId) && isBlank(serverProfileSchemaId)) {
            throw new IncorrectParameterException("Both profile schema ids can't be empty");
        }
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
