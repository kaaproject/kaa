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

import org.kaaproject.kaa.common.dto.ChangeNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.common.dto.TopicListEntryDto;
import org.kaaproject.kaa.common.dto.UpdateNotificationDto;
import org.kaaproject.kaa.server.common.dao.exception.KaaOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * The interface Endpoint service.
 */
public interface EndpointService {

    /**
     * Find endpoint profile by endpoint group id.
     *
     * @param pageLink
     *            the page link dto
     * @return the endpoint profiles page dto
     */
    EndpointProfilesPageDto findEndpointProfileByEndpointGroupId(PageLinkDto pageLink);

    /**
     * Find endpoint profile body by endpoint group id.
     *
     * @param pageLink
     *            the page link dto
     * @return the endpoint profiles page dto
     */
    EndpointProfilesBodyDto findEndpointProfileBodyByEndpointGroupId(PageLinkDto pageLink);

    /**
     * Find endpoint groups by application id.
     *
     * @param applicationId
     *            the application id
     * @return the list
     */
    List<EndpointGroupDto> findEndpointGroupsByAppId(String applicationId);

    /**
     * Find endpoint group by id.
     *
     * @param id
     *            the id
     * @return the endpoint group dto
     */
    EndpointGroupDto findEndpointGroupById(String id);

    /**
     * Remove endpoint group by application id.
     *
     * @param applicationId
     *            the application id
     */
    void removeEndpointGroupByAppId(String applicationId);

    /**
     * Remove endpoint group by id.
     *
     * @param applicationId
     *            the application id
     * @return the change notification dto
     */
    ChangeNotificationDto removeEndpointGroupById(String applicationId);

    /**
     * Save endpoint group. Can't save endpoint group with same weight or update
     * weight for default group. Application id and group weight is unique
     *
     * @param endpointGroupDto
     *            the endpoint group dto
     * @return the endpoint group dto
     */
    EndpointGroupDto saveEndpointGroup(EndpointGroupDto endpointGroupDto);

    /**
     * Removes the topic id from endpoint group.
     *
     * @param id
     *            the id
     * @param topicId
     *            the topic id
     * @return the update notification dto
     */
    UpdateNotificationDto<EndpointGroupDto> removeTopicFromEndpointGroup(String id, String topicId);

    /**
     * Subscribe existing topic to existing group.
     *
     * @param id
     *            the endpoint group id
     * @param topicId
     *            the topic id
     * @return the update notification dto
     */
    UpdateNotificationDto<EndpointGroupDto> addTopicToEndpointGroup(String id, String topicId);

    /**
     * Find endpoint configuration by hash.
     *
     * @param hash
     *            the hash
     * @return the endpoint configuration dto
     */
    EndpointConfigurationDto findEndpointConfigurationByHash(byte[] hash);

    /**
     * Save endpoint configuration.
     *
     * @param endpointConfigurationDto
     *            the endpoint configuration dto
     * @return the endpoint configuration dto
     */
    EndpointConfigurationDto saveEndpointConfiguration(EndpointConfigurationDto endpointConfigurationDto);

    /**
     * Find endpoint profile by key hash.
     *
     * @param endpointProfileKeyHash
     *            the endpoint profile key hash
     * @return the endpoint profile dto
     */
    EndpointProfileDto findEndpointProfileByKeyHash(byte[] endpointProfileKeyHash);

    /**
     * Find endpoint profile by key hash.
     *
     * @param endpointProfileKeyHash
     *            the endpoint profile key hash
     * @return the endpoint profile body dto
     */
    EndpointProfileBodyDto findEndpointProfileBodyByKeyHash(byte[] endpointProfileKeyHash);

    /**
     * Find topic list entry by key hash.
     *
     * @param hash
     *            the hash
     * @return the topic list entry dto
     */
    TopicListEntryDto findTopicListEntryByHash(byte[] hash);

    /**
     * Save topic list entry
     *
     * @param topicListEntryDto
     *            the endpoint list entry dto
     * @return the topic list entry dto
     */
    TopicListEntryDto saveTopicListEntry(TopicListEntryDto topicListEntryDto);

    /**
     * Remove endpoint profile by key hash.
     *
     * @param endpointProfileKeyHash
     *            the endpoint profile key hash
     */
    void removeEndpointProfileByKeyHash(byte[] endpointProfileKeyHash);

    /**
     * Remove endpoint profile by application id.
     * 
     * @param appId
     *            application id
     */
    void removeEndpointProfileByAppId(String appId);

    /**
     * Save endpoint profile.
     *
     * @param endpointProfileDto
     *            the endpoint profile dto
     * @return the endpoint profile dto
     */
    EndpointProfileDto saveEndpointProfile(EndpointProfileDto endpointProfileDto);

    /**
     * Attach endpoint profile to user.
     *
     * @param userExternalId
     *            the user external id
     * @param tenantId
     *            the tenant id
     * @param profile
     *            the profile
     * @return the endpoint profile dto
     */
    EndpointProfileDto attachEndpointToUser(String userExternalId, String tenantId, EndpointProfileDto profile);

    /**
     * Attach endpoint profile to user.
     *
     * @param endpointUserId
     *            the endpoint user id
     * @param endpointAccessToken
     *            the endpoint access token
     * @return the endpoint profile dto
     */

    @Retryable(maxAttempts = 10, backoff = @Backoff(delay = 100) , value = { KaaOptimisticLockingFailureException.class })
    EndpointProfileDto attachEndpointToUser(String endpointUserId, String endpointAccessToken) throws KaaOptimisticLockingFailureException;

    /**
     * Detach endpoint profile from user.
     *
     * @param detachEndpoint
     *            the detach endpoint
     */
    @Retryable(maxAttempts = 10, backoff = @Backoff(delay = 100) , value = { KaaOptimisticLockingFailureException.class })
    void detachEndpointFromUser(EndpointProfileDto detachEndpoint);

    /**
     * Find all endpoint users.
     *
     * @return the list of endpoint user dto's.
     */
    List<EndpointUserDto> findAllEndpointUsers();

    /**
     * Find endpoint user by id.
     *
     * @param id
     *            the id
     * @return the endpoint user dto
     */
    EndpointUserDto findEndpointUserById(String id);

    /**
     * Find endpoint user by id.
     *
     * @param externalId
     *            the external id
     * @param tenantId
     *            the tenant id
     * @return the endpoint user dto
     */
    EndpointUserDto findEndpointUserByExternalIdAndTenantId(String externalId, String tenantId);

    /**
     * Generate endpoint user access token from external user id.
     *
     * @param externalUid
     *            the external user id
     * @param tenantId
     *            the tenant id
     * @return generated access token
     */
    String generateEndpointUserAccessToken(String externalUid, String tenantId);

    /**
     * Find endpoint profiles by user id.
     *
     * @param endpointUserId
     *            the endpoint user id
     * @return the list
     */
    List<EndpointProfileDto> findEndpointProfilesByUserId(String endpointUserId);
    
    /**
     * Find endpoint profiles by user external id and tenant id.
     *
     * @param externalId
     *            the endpoint user external id
     * @param tenantId
     *            the tenant id
     * @return the list of endpoint profiles
     */
    List<EndpointProfileDto> findEndpointProfilesByExternalIdAndTenantId(String externalId, String tenantId);

    /**
     * Returns the default group for the given application.
     *
     * @param applicationId The application ID
     *
     * @return The default group for the given application.
     */
    EndpointGroupDto findDefaultGroup(String applicationId);

    /**
     * Save endpoint user.
     *
     * @param endpointUserDto
     *            the endpoint user dto
     * @return the endpoint user dto
     */
    EndpointUserDto saveEndpointUser(EndpointUserDto endpointUserDto);

    /**
     * Remove endpoint user by id.
     *
     * @param id
     *            the endpoint user id
     */
    void removeEndpointUserById(String id);
}
