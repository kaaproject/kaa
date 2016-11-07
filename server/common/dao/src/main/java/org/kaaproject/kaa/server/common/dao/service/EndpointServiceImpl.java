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

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidObject;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateHash;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateObject;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateSqlId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateSqlObject;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeNotificationDto;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicListEntryDto;
import org.kaaproject.kaa.common.dto.UpdateNotificationDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.hash.SHA1HashUtils;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.ServerProfileService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.exception.KaaOptimisticLockingFailureException;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointGroupDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserDao;
import org.kaaproject.kaa.server.common.dao.impl.TopicDao;
import org.kaaproject.kaa.server.common.dao.impl.TopicListEntryDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointConfiguration;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.kaaproject.kaa.server.common.dao.model.EndpointUser;
import org.kaaproject.kaa.server.common.dao.model.TopicListEntry;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;
import org.kaaproject.kaa.server.common.dao.model.sql.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EndpointServiceImpl implements EndpointService {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointServiceImpl.class);
    private static final int DEFAULT_GROUP_WEIGHT = 0;

    @Autowired
    private EndpointGroupDao<EndpointGroup> endpointGroupDao;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ServerProfileService serverProfileService;

    @Autowired
    private CTLService ctlService;

    @Autowired
    private TopicDao<Topic> topicDao;

    private EndpointProfileDao<EndpointProfile> endpointProfileDao;
    private EndpointConfigurationDao<EndpointConfiguration> endpointConfigurationDao;
    private EndpointUserDao<EndpointUser> endpointUserDao;
    private TopicListEntryDao<TopicListEntry> topicListEntryDao;

    @Override
    @Transactional
    public List<EndpointGroupDto> findEndpointGroupsByAppId(String applicationId) {
        validateSqlId(applicationId, "Can't find endpoint groups by application id. Incorrect application id " + applicationId);
        return convertDtoList(endpointGroupDao.findByApplicationId(applicationId));
    }

    @Override
    @Transactional
    public EndpointGroupDto findEndpointGroupById(String id) {
        validateSqlId(id, "Can't find endpoint group by id. Incorrect id " + id);
        return getDto(endpointGroupDao.findById(id));
    }

    @Override
    @Transactional
    public void removeEndpointGroupByAppId(String applicationId) {
        validateSqlId(applicationId, "Can't remove endpoint groups by application id. Incorrect application id " + applicationId);
        List<EndpointGroup> groups = endpointGroupDao.findByApplicationId(applicationId);
        if (groups != null && !groups.isEmpty()) {
            for (EndpointGroup group : groups) {
                removeEndpointGroup(group.getId().toString(), true);
            }
        }
    }

    @Override
    @Transactional
    public ChangeNotificationDto removeEndpointGroupById(String id) {
        validateSqlId(id, "Can't remove endpoint group by id. Incorrect id " + id);
        return removeEndpointGroup(id, false);
    }

    @Override
    @Transactional
    public EndpointGroupDto saveEndpointGroup(EndpointGroupDto endpointGroupDto) {
        validateSqlObject(endpointGroupDto, "Can't save endpoint group object. Incorrect endpoint group object." + endpointGroupDto);
        EndpointGroupDto savedGroup = null;
        String appId = endpointGroupDto.getApplicationId();
        if (isValidId(appId)) {
            String id = endpointGroupDto.getId();
            EndpointGroup groupWithSameWeight = endpointGroupDao.findByAppIdAndWeight(appId, endpointGroupDto.getWeight());
            EndpointGroup groupWithSameName = endpointGroupDao.findByAppIdAndName(appId, endpointGroupDto.getName());
            if (StringUtils.isBlank(id)) {
                if (groupWithSameWeight != null) {
                    throw new IncorrectParameterException("Can't save endpoint group with same weight and application id");
                } else if (groupWithSameName != null) {
                    throw new IncorrectParameterException("Can't save endpoint group with same name and application id");
                } else {
                    endpointGroupDto.setCreatedTime(System.currentTimeMillis());
                    savedGroup = getDto(endpointGroupDao.save(new EndpointGroup(endpointGroupDto)));
                } 
            } else {
                EndpointGroup previousGroup = endpointGroupDao.findById(id);
                if (previousGroup != null) {
                    if (groupWithSameWeight != null && !groupWithSameWeight.getId().equals(previousGroup.getId())) {
                        throw new IncorrectParameterException("Can't save endpoint group with same weight and application id");
                    } else if (previousGroup.getWeight() == DEFAULT_GROUP_WEIGHT) {
                        throw new IncorrectParameterException("Can't update weight for default endpoint group");
                    } else if (groupWithSameName != null && !groupWithSameName.getId().equals(previousGroup.getId())) {
                        throw new IncorrectParameterException("Can't save endpoint group with same name and application id");
                    } else {
                        savedGroup = getDto(endpointGroupDao.save(new EndpointGroup(endpointGroupDto)));
                    }
                    if (previousGroup.getWeight() != savedGroup.getWeight()) {
                        addHistory(savedGroup, ChangeType.UPDATE_WEIGHT);
                    }
                } else {
                    LOG.debug("Can't update endpoint group, id: [{}]", id);
                }
            }
        } else {
            LOG.debug("Can't save endpoint group. Invalid application id [{}]", appId);
        }
        return savedGroup;
    }

    @Override
    @Transactional
    public UpdateNotificationDto<EndpointGroupDto> removeTopicFromEndpointGroup(String id, String topicId) {
        validateSqlId(id, "Can't remove endpoint group topics " + topicId + ". Incorrect endpoint group id." + id);
        UpdateNotificationDto<EndpointGroupDto> dto = null;
        EndpointGroup endpointGroup = endpointGroupDao.removeTopicFromEndpointGroup(id, topicId);
        if (endpointGroup != null) {
            dto = new UpdateNotificationDto<>();
            HistoryDto history = addHistory(endpointGroup.toDto(), ChangeType.REMOVE_TOPIC, topicId);
            if (history != null) {
                dto.setAppId(history.getApplicationId());
                dto.setAppSeqNumber(history.getSequenceNumber());
            }
            dto.setChangeType(ChangeType.REMOVE_TOPIC);
            dto.setTopicId(topicId);
            dto.setGroupId(endpointGroup.getId().toString());
            dto.setPayload(endpointGroup.toDto());
        } else {
            LOG.debug("Can't remove topic [{}] from endpoint group [{}]", topicId, id);
        }
        return dto;
    }

    @Override
    @Transactional
    public UpdateNotificationDto<EndpointGroupDto> addTopicToEndpointGroup(String id, String topicId) {
        validateSqlId(id, "Can't add topics " + topicId + " to endpoint group . Incorrect endpoint group id." + id);
        UpdateNotificationDto<EndpointGroupDto> dto = null;
        EndpointGroup endpointGroup = endpointGroupDao.addTopicToEndpointGroup(id, topicId);
        if (endpointGroup != null) {
            dto = new UpdateNotificationDto<>();
            HistoryDto history = addHistory(endpointGroup.toDto(), ChangeType.ADD_TOPIC, topicId);
            if (history != null) {
                dto.setAppId(history.getApplicationId());
                dto.setAppSeqNumber(history.getSequenceNumber());
            }
            dto.setChangeType(ChangeType.ADD_TOPIC);
            dto.setTopicId(topicId);
            dto.setGroupId(endpointGroup.getId().toString());
            dto.setPayload(endpointGroup.toDto());
        } else {
            LOG.debug("Can't add topic [{}] to endpoint group [{}]", topicId, id);
        }
        return dto;
    }

    @Override
    @Transactional
    public EndpointProfilesPageDto findEndpointProfileByEndpointGroupId(PageLinkDto pageLink) {
        validateSqlId(pageLink.getLimit(), "Can't find endpoint group by id. Incorrect limit parameter " + pageLink.getLimit());
        validateString(pageLink.getOffset(), "Can't find endpoint group by id. Incorrect offset parameter " + pageLink.getOffset());
        return endpointProfileDao.findByEndpointGroupId(pageLink);
    }

    @Override
    @Transactional
    public EndpointProfilesBodyDto findEndpointProfileBodyByEndpointGroupId(PageLinkDto pageLink) {
        validateSqlId(pageLink.getLimit(), "Can't find endpoint group by id. Incorrect limit parameter " + pageLink.getLimit());
        validateString(pageLink.getOffset(), "Can't find endpoint group by id. Incorrect offset parameter " + pageLink.getOffset());
        return endpointProfileDao.findBodyByEndpointGroupId(pageLink);
    }

    @Override
    public EndpointConfigurationDto findEndpointConfigurationByHash(byte[] hash) {
        validateHash(hash, "Can't find endpoint configuration by hash. Invalid configuration hash " + hash);
        return getDto(endpointConfigurationDao.findByHash(hash));
    }

    @Override
    public EndpointConfigurationDto saveEndpointConfiguration(EndpointConfigurationDto endpointConfigurationDto) {
        return getDto(endpointConfigurationDao.save(endpointConfigurationDto));
    }

    @Override
    public EndpointProfileDto findEndpointProfileByKeyHash(byte[] endpointProfileKeyHash) {
        validateHash(endpointProfileKeyHash, "Can't find endpoint profile by key hash. Invalid key hash " + endpointProfileKeyHash);
        return getDto(endpointProfileDao.findByKeyHash(endpointProfileKeyHash));
    }

    @Override
    public EndpointProfileBodyDto findEndpointProfileBodyByKeyHash(byte[] endpointProfileKeyHash) {
        validateHash(endpointProfileKeyHash, "Can't find endpoint profile by key hash. Invalid key hash " + endpointProfileKeyHash);
        return endpointProfileDao.findBodyByKeyHash(endpointProfileKeyHash);
    }

    @Override
    public void removeEndpointProfileByKeyHash(byte[] endpointProfileKeyHash) {
        validateHash(endpointProfileKeyHash, "Can't remove endpoint profile by key hash. Invalid key hash " + endpointProfileKeyHash);
        EndpointProfile endpointProfile = endpointProfileDao.findByKeyHash(endpointProfileKeyHash);
        if(endpointProfile != null){
            if (isValidId(endpointProfile.getEndpointUserId())) {
                detachEndpointFromUser(getDto(endpointProfile));
            }
            endpointProfileDao.removeByKeyHash(endpointProfileKeyHash);
        } else {
            throw new DatabaseProcessingException("Endpoint profile is not present in db.");
        }
    }

    @Override
    public void removeEndpointProfileByAppId(String appId) {
        validateSqlId(appId, "Can't remove endpoint profile by application id. Invalid application id " + appId);
        endpointProfileDao.removeByAppId(appId);
    }

    @Override
    public EndpointProfileDto saveEndpointProfile(EndpointProfileDto endpointProfileDto) throws KaaOptimisticLockingFailureException {
        validateObject(endpointProfileDto, "Can't save endpoint profile object. Invalid endpoint profile object " + endpointProfileDto);
        byte[] keyHash = endpointProfileDto.getEndpointKeyHash();
        validateHash(keyHash, "Incorrect key hash for endpoint profile.");
        if (endpointProfileDto.getServerProfileBody() == null) {
            ServerProfileSchemaDto serverProfileSchemaDto = serverProfileService.findLatestServerProfileSchema(endpointProfileDto.getApplicationId());
            CTLSchemaDto schemaDto = ctlService.findCTLSchemaById(serverProfileSchemaDto.getCtlSchemaId());
            LOG.debug("Set latest server profile schema [{}] and default record {} for endpoint with key [{}]", serverProfileSchemaDto.getVersion(),
                    schemaDto.getBody(), keyHash);
            endpointProfileDto.setServerProfileVersion(serverProfileSchemaDto.getVersion());
            endpointProfileDto.setServerProfileBody(schemaDto.getDefaultRecord());
        }
        EndpointProfileDto dto;
        if (isBlank(endpointProfileDto.getId())) {
            EndpointProfile storedProfile = endpointProfileDao.findByKeyHash(keyHash);
            if (storedProfile == null) {
                dto = getDto(endpointProfileDao.save(endpointProfileDto));
            } else {
                if (Arrays.equals(storedProfile.getEndpointKey(), endpointProfileDto.getEndpointKey())) {
                    LOG.debug("Got register profile for already existing profile {}. Will overwrite existing profile!", keyHash);
                    endpointProfileDto.setId(storedProfile.getId());
                    endpointProfileDto.setVersion(storedProfile.getVersion());
                    dto = getDto(endpointProfileDao.save(endpointProfileDto));
                } else {
                    LOG.warn("Endpoint profile with key hash {} already exists.", keyHash);
                    throw new DatabaseProcessingException("Can't save endpoint profile with existing key hash.");
                }
            }
        } else {
            LOG.debug("Update endpoint profile with id [{}]", endpointProfileDto.getId());
            dto = getDto(endpointProfileDao.save(endpointProfileDto));
        }
        return dto;
    }

    @Override
    public EndpointProfileDto attachEndpointToUser(String userExternalId, String tenantId, EndpointProfileDto profile) {
        validateString(userExternalId, "Incorrect userExternalId " + userExternalId);
        EndpointUser endpointUser = endpointUserDao.findByExternalIdAndTenantId(userExternalId, tenantId);
        if (endpointUser == null) {
            LOG.info("Creating new endpoint user with external id: [{}] in context of [{}] tenant", userExternalId, tenantId);
            EndpointUserDto endpointUserDto = new EndpointUserDto();
            endpointUserDto.setTenantId(tenantId);
            endpointUserDto.setExternalId(userExternalId);
            endpointUserDto.setUsername(userExternalId);
            endpointUser = endpointUserDao.save(endpointUserDto);
        }
        List<String> endpointIds = endpointUser.getEndpointIds();
        if (endpointIds == null) {
            endpointIds = new ArrayList<>();
            endpointUser.setEndpointIds(endpointIds);
        }
        else if (endpointIds != null && endpointIds.contains(profile.getId())) {
            LOG.warn("Endpoint is already assigned to current user {}.", profile.getEndpointUserId());
            return profile;
        }
        endpointIds.add(profile.getId());
        endpointUser = endpointUserDao.save(endpointUser);
        profile.setEndpointUserId(endpointUser.getId());
        while (true) {
            try {
                LOG.trace("Save endpoint user {} and endpoint profile {}", endpointUser, profile);
                return saveEndpointProfile(profile);
            } catch (KaaOptimisticLockingFailureException ex) {
                LOG.warn("Optimistic lock detected in endpoint profile ", Arrays.toString(profile.getEndpointKey()), ex);
                profile = findEndpointProfileByKeyHash(profile.getEndpointKeyHash());
                profile.setEndpointUserId(endpointUser.getId());
            }
        }
    }

    @Override
    public EndpointProfileDto attachEndpointToUser(String endpointUserId, String endpointAccessToken) throws KaaOptimisticLockingFailureException {
        LOG.info("Try to attach endpoint with access token {} to user with {}", endpointAccessToken, endpointUserId);
        validateString(endpointUserId, "Incorrect endpointUserId " + endpointUserId);
        EndpointUser endpointUser = endpointUserDao.findById(endpointUserId);
        LOG.trace("[{}] Found endpoint user with id {} ", endpointUserId, endpointUser);
        if (endpointUser != null) {
            EndpointProfile endpoint = endpointProfileDao.findByAccessToken(endpointAccessToken);
            LOG.trace("[{}] Found endpoint profile by with access token {} ", endpointAccessToken, endpoint);
            if (endpoint != null) {
                if (endpoint.getEndpointUserId() == null || endpointUserId.equals(endpoint.getEndpointUserId())) {
                    LOG.debug("Attach endpoint profile with id {} to endpoint user with id {} ", endpoint.getId(), endpointUser.getId());
                    List<String> endpointIds = endpointUser.getEndpointIds();
                    if (endpointIds != null && endpointIds.contains(endpoint.getId())) {
                        LOG.warn("Endpoint is already assigned to current user {}.", endpoint.getEndpointUserId());
                        return getDto(endpoint);
                    }
                    if (endpointIds == null) {
                        endpointIds = new ArrayList<>();
                        endpointUser.setEndpointIds(endpointIds);
                    }
                    endpointIds.add(endpoint.getId());
                    endpointUser = endpointUserDao.save(endpointUser);
                    while (true) {
                        try {
                            endpoint.setEndpointUserId(endpointUser.getId());
                            LOG.trace("Save endpoint user {} and endpoint profile {}", endpointUser, endpoint);
                            endpoint = endpointProfileDao.save(endpoint);
                            break;
                        } catch (KaaOptimisticLockingFailureException ex) {
                            LOG.warn("Optimistic lock detected in endpoint profile ", Arrays.toString(endpoint.getEndpointKey()), ex);
                            endpoint = endpointProfileDao.findByKeyHash(SHA1HashUtils.hashToBytes(endpoint.getEndpointKey()));
                        }
                    }
                    return getDto(endpoint);
                } else {
                    LOG.warn("Endpoint is already assigned to different user {}. Unassign it first!.", endpoint.getEndpointUserId());
                    throw new DatabaseProcessingException("Endpoint is already assigned to different user.");
                }
            } else {
                LOG.warn("Endpoint with accessToken {} is not present in db.", endpointAccessToken);
                throw new DatabaseProcessingException("No endpoint found for specified accessToken.");
            }
        } else {
            LOG.warn("Endpoint user with id {} is not present in db.", endpointUserId);
            throw new DatabaseProcessingException("Endpoint user is not present in db.");
        }
    }

    @Override
    public void detachEndpointFromUser(EndpointProfileDto endpoint) {
        String endpointUserId = endpoint.getEndpointUserId();
        validateString(endpointUserId, "Incorrect endpointUserId " + endpointUserId);
        EndpointUser endpointUser = endpointUserDao.findById(endpointUserId);
        if (endpointUser != null) {
            List<String> endpointIds = endpointUser.getEndpointIds();
            if (endpointIds != null && endpointIds.contains(endpoint.getId())) {
                endpointIds.remove(endpoint.getId());
            } else {
                LOG.warn("Endpoint is not assigned to current user {}!", endpointUserId);
                throw new DatabaseProcessingException("Endpoint is not assigned to current user.");
            }
            endpointUserDao.save(endpointUser);
            while (true) {
                try {
                    LOG.trace("Save endpoint user {} and endpoint profile {}", endpointUser, endpoint);
                    endpoint.setEndpointUserId(null);
                    saveEndpointProfile(endpoint);
                    break;
                } catch (KaaOptimisticLockingFailureException ex) {
                    LOG.warn("Optimistic lock detected in endpoint profile ", Arrays.toString(endpoint.getEndpointKey()), ex);
                    endpoint = getDto(endpointProfileDao.findByKeyHash(SHA1HashUtils.hashToBytes(endpoint.getEndpointKey())));
                }
            }
        } else {
            LOG.warn("Endpoint user with id {} is not present in db.", endpointUserId);
            throw new DatabaseProcessingException("Endpoint user is not present in db.");
        }
    }

    @Override
    public List<EndpointUserDto> findAllEndpointUsers() {
        return convertDtoList(endpointUserDao.find());
    }

    @Override
    public EndpointUserDto findEndpointUserById(String id) {
        EndpointUserDto endpointUserDto = null;
        if (isValidId(id)) {
            endpointUserDto = getDto(endpointUserDao.findById(id));
        }
        return endpointUserDto;
    }

    @Override
    public EndpointUserDto findEndpointUserByExternalIdAndTenantId(String externalId, String tenantId) {
        EndpointUserDto endpointUserDto = null;
        if (isValidId(externalId) && isValidId(tenantId)) {
            endpointUserDto = getDto(endpointUserDao.findByExternalIdAndTenantId(externalId, tenantId));
        }
        return endpointUserDto;
    };

    @Override
    @Transactional
    public EndpointGroupDto findDefaultGroup(String appId) {
        validateSqlId(appId, "Can't find default endpoint group by app id. Incorrect app id " + appId);
        return getDto(endpointGroupDao.findByAppIdAndWeight(appId, DEFAULT_GROUP_WEIGHT));
    }

    @Override
    public String generateEndpointUserAccessToken(String externalUid, String tenantId) {
        return endpointUserDao.generateAccessToken(externalUid, tenantId);
    }

    private HistoryDto addHistory(EndpointGroupDto dto, ChangeType type) {
        return addHistory(dto, type, null);
    }

    private HistoryDto addHistory(EndpointGroupDto dto, ChangeType type, String topicId) {
        LOG.debug("Add history information about endpoint group update");
        HistoryDto history = new HistoryDto();
        history.setApplicationId(dto.getApplicationId());
        ChangeDto change = new ChangeDto();
        change.setEndpointGroupId(dto.getId());
        change.setType(type);
        change.setTopicId(topicId);
        history.setChange(change);
        return historyService.saveHistory(history);
    }

    /**
     * This method remove endpoint group by id and check if endpoint group is
     * not default. It's forbidden to remove default endpoint group by id
     *
     * @param id
     *            endpoint group id
     * @param forceRemove
     *            boolean flag define if its removing groups by application.
     */
    private ChangeNotificationDto removeEndpointGroup(String id, boolean forceRemove) {
        EndpointGroup endpointGroup = endpointGroupDao.findById(id);
        ChangeNotificationDto changeDto = null;
        if (endpointGroup != null) {
            if (endpointGroup.getWeight() != 0 || forceRemove) {
                LOG.debug("Cascade delete endpoint group with profile filter and configurations.");
                // profileFilterDao.removeByEndpointGroupId(id);
                // configurationDao.removeByEndpointGroupId(id);
                // TODO: need to add to history about deleted configurations and
                // profile filters
                endpointGroupDao.removeById(id);
                EndpointGroupDto groupDto = endpointGroup.toDto();
                HistoryDto historyDto = addHistory(groupDto, ChangeType.REMOVE_GROUP);
                changeDto = new ChangeNotificationDto();
                changeDto.setAppId(endpointGroup.getApplicationId());
                changeDto.setAppSeqNumber(historyDto.getSequenceNumber());
                changeDto.setGroupId(groupDto.getId());
                changeDto.setGroupSeqNumber(groupDto.getSequenceNumber());
            } else {
                LOG.warn("Can't remove default endpoint group by id [{}]", id);
            }
        }
        return changeDto;
    }

    @Override
    @Transactional
    public TopicListEntryDto findTopicListEntryByHash(byte[] hash) {
        LOG.debug("Looking for a topic list entry by hash: [{}]", hash);
        TopicListEntry topicListEntry = topicListEntryDao.findByHash(hash);
        List<TopicDto> foundTopics = ModelUtils.convertDtoList(topicDao.findTopicsByIds(topicListEntry.getTopicIds()));
        TopicListEntryDto topicListEntryDto = getDto(topicListEntry);
        topicListEntryDto.setTopics(foundTopics);
        return topicListEntryDto;
    }

    @Override
    public TopicListEntryDto saveTopicListEntry(TopicListEntryDto topicListEntryDto) {
        LOG.debug("Saving topic list entry: [{}]", topicListEntryDto);
        return getDto(topicListEntryDao.save(topicListEntryDto));
    }

    @Override
    public List<EndpointProfileDto> findEndpointProfilesByUserId(String endpointUserId) {
        return convertDtoList(endpointProfileDao.findByEndpointUserId(endpointUserId));
    }

    @Override
    public List<EndpointProfileDto> findEndpointProfilesByExternalIdAndTenantId(String externalId, String tenantId) {
        if (isValidId(externalId) && isValidId(tenantId)) {
            EndpointUser endpointUser = endpointUserDao.findByExternalIdAndTenantId(externalId, tenantId);
            if (endpointUser != null) {
                return convertDtoList(endpointProfileDao.findByEndpointUserId(endpointUser.getId()));
            }
        }
        return Collections.emptyList();
    }

    public void setEndpointProfileDao(EndpointProfileDao<EndpointProfile> endpointProfileDao) {
        this.endpointProfileDao = endpointProfileDao;
    }

    public void setEndpointConfigurationDao(EndpointConfigurationDao<EndpointConfiguration> endpointConfigurationDao) {
        this.endpointConfigurationDao = endpointConfigurationDao;
    }

    public void setEndpointUserDao(EndpointUserDao<EndpointUser> endpointUserDao) {
        this.endpointUserDao = endpointUserDao;
    }

    public void setTopicListEntryDao(TopicListEntryDao<TopicListEntry> topicListEntryDao) {
        this.topicListEntryDao = topicListEntryDao;
    }

    @Override
    public EndpointUserDto saveEndpointUser(EndpointUserDto endpointUserDto) {
        EndpointUserDto endpointUser = null;
        if (isValidObject(endpointUserDto)) {
            EndpointUser user = endpointUserDao.findByExternalIdAndTenantId(endpointUserDto.getExternalId(), endpointUserDto.getTenantId());
            if (user == null || user.getId().equals(endpointUserDto.getId())) {
                endpointUser = getDto(endpointUserDao.save(endpointUserDto));
            } else {
                throw new IncorrectParameterException("Can't save endpoint user with same external id");
            }
        }
        return endpointUser;
    }

    @Override
    public void removeEndpointUserById(String id) {
        if (isValidId(id)) {
            endpointUserDao.removeById(id);
        }
    }

}
