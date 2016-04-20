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

package org.kaaproject.kaa.server.operations.service.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.UserVerifierService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFqnKey;
import org.kaaproject.kaa.server.operations.service.event.EventClassFqnVersion;
import org.kaaproject.kaa.server.operations.service.event.RouteTableKey;
import org.kaaproject.kaa.server.sync.EndpointAttachRequest;
import org.kaaproject.kaa.server.sync.EndpointAttachResponse;
import org.kaaproject.kaa.server.sync.EndpointDetachRequest;
import org.kaaproject.kaa.server.sync.EndpointDetachResponse;
import org.kaaproject.kaa.server.sync.EventListenersRequest;
import org.kaaproject.kaa.server.sync.EventListenersResponse;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class DefauleEndpointUserService is a default implementation of
 * {@link EndpointUserService EndpointUserService}.
 *
 * @author ashvayka
 */
public class DefaultEndpointUserService implements EndpointUserService {

    /** The LOG constant. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultEndpointUserService.class);

    /** The application service. */
    @Autowired
    private EndpointService endpointService;

    @Autowired
    private UserVerifierService userVerifierService;

    /** The application service. */
    @Autowired
    private CacheService cacheService;

    @Override
    public UserVerifierDto findUserVerifier(String appId, String verifierToken) {
        return userVerifierService.findUserVerifiersByAppIdAndVerifierToken(appId, verifierToken);
    }

    @Override
    public List<UserVerifierDto> findUserVerifiers(String appId) {
        return userVerifierService.findUserVerifiersByAppId(appId);
    }

    @Override
    public EndpointProfileDto attachEndpointToUser(EndpointProfileDto profile, String appToken, String userExternalId) {
        String tenantId = cacheService.getTenantIdByAppToken(appToken);
        return endpointService.attachEndpointToUser(userExternalId, tenantId, profile);
    }

    @Override
    public EndpointAttachResponse attachEndpoint(EndpointProfileDto profile, EndpointAttachRequest endpointAttachRequest) {
        EndpointAttachResponse response = new EndpointAttachResponse();
        response.setRequestId(endpointAttachRequest.getRequestId());
        response.setResult(SyncStatus.FAILURE);

        String endpointUserId = profile.getEndpointUserId();
        if (isNotEmpty(endpointUserId)) {
            try {
                EndpointProfileDto attachedEndpoint = endpointService.attachEndpointToUser(endpointUserId, endpointAttachRequest.getEndpointAccessToken());
                response.setResult(SyncStatus.SUCCESS);
                response.setEndpointKeyHash(Base64Util.encode(attachedEndpoint.getEndpointKeyHash()));
            } catch (DatabaseProcessingException e) {
                LOG.warn("[{}] failed to attach endpoint with access token {} and user {}, exception catched: {}",
                        Base64Util.encode(profile.getEndpointKeyHash()), endpointAttachRequest.getEndpointAccessToken(), profile.getEndpointUserId(), e);
            }
        } else {
            LOG.warn("[{}] received attach endpoint request, but there is no user to attach.", Base64Util.encode(profile.getEndpointKeyHash()));
        }

        return response;
    }

    @Override
    public EndpointDetachResponse detachEndpoint(EndpointProfileDto profile, EndpointDetachRequest endpointDetachRequest) {
        EndpointDetachResponse response = new EndpointDetachResponse();
        response.setRequestId(endpointDetachRequest.getRequestId());
        response.setResult(SyncStatus.FAILURE);

        if (isValid(endpointDetachRequest) && isNotEmpty(profile.getEndpointUserId())) {
            try {
                byte[] endpointKeyHash = Base64Util.decode(endpointDetachRequest.getEndpointKeyHash());
                if (Arrays.equals(profile.getEndpointKeyHash(), endpointKeyHash)) {
                    endpointService.detachEndpointFromUser(profile);
                    response.setResult(SyncStatus.SUCCESS);
                } else {
                    EndpointProfileDto detachEndpoint = endpointService.findEndpointProfileByKeyHash(endpointKeyHash);
                    if (detachEndpoint != null) {
                        if (detachEndpoint.getEndpointUserId() != null && detachEndpoint.getEndpointUserId().equals(profile.getEndpointUserId())) {
                            endpointService.detachEndpointFromUser(detachEndpoint);
                            response.setResult(SyncStatus.SUCCESS);
                        } else {
                            LOG.warn("[{}] received detach endpoint request, but requested {} and current {} user mismatch.",
                                    Base64Util.encode(profile.getEndpointKeyHash()), profile.getEndpointUserId(), detachEndpoint.getEndpointUserId());
                        }
                    } else {
                        LOG.warn("[{}] received detach endpoint request, for not existing endpoint.", Base64Util.encode(profile.getEndpointKeyHash()));
                    }
                }
            } catch (DatabaseProcessingException e) {
                LOG.warn("[{}] failed to detach endpoint {}, exception catched: ", profile, e);
            }
        } else {
            LOG.warn("[{}] detach endpoint request {} or profile {} is not valid", Base64Util.encode(profile.getEndpointKeyHash()), endpointDetachRequest,
                    profile);
        }

        return response;
    }

    protected boolean isValid(EndpointDetachRequest endpointDetachRequest) {
        return endpointDetachRequest.getEndpointKeyHash() != null && !endpointDetachRequest.getEndpointKeyHash().isEmpty();
    }

    protected boolean isNotEmpty(String userId) {
        return userId != null && !userId.isEmpty();
    }

    @Override
    public EventListenersResponse findListeners(EndpointProfileDto profile, String appToken, EventListenersRequest request) {
        if (profile.getEndpointUserId() == null || profile.getEndpointUserId().isEmpty()) {
            LOG.info("Can't find listeners for unassigned endpoint!");
            return new EventListenersResponse(request.getRequestId(), null, SyncStatus.FAILURE);
        }

        List<EndpointProfileDto> endpointProfiles = endpointService.findEndpointProfilesByUserId(profile.getEndpointUserId());
        if (endpointProfiles.size() <= 1) {
            LOG.info("There is only one endpoint(current) assigned to this user!");
            List<String> emptyList = Collections.emptyList();
            return new EventListenersResponse(request.getRequestId(), emptyList, SyncStatus.SUCCESS);
        }

        String tenantId = cacheService.getTenantIdByAppToken(appToken);
        Set<EndpointObjectHash> eventClassIntersectionSet = null;
        for (String eventClassFqn : request.getEventClassFQNs()) {
            Set<EndpointObjectHash> eventClassSet = new HashSet<>();
            LOG.debug("Lookup event class family id using tenant [{}] and event class fqn {}", tenantId, eventClassFqn);
            String ecfId = cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(tenantId, eventClassFqn));
            int version = 0;
            for (EventClassFamilyVersionStateDto ecfVersionDto : profile.getEcfVersionStates()) {
                if (ecfVersionDto.getEcfId().equals(ecfId)) {
                    version = ecfVersionDto.getVersion();
                    break;
                }
            }
            if (version > 0) {
                LOG.debug("Load recepient keys using tenant [{}] event class {} and version {}", tenantId, eventClassFqn, version);
                Set<RouteTableKey> recipientKeys = cacheService.getRouteKeys(new EventClassFqnVersion(tenantId, eventClassFqn, version));

                for (EndpointProfileDto endpointProfile : endpointProfiles) {
                    if (endpointProfile.getId().equals(profile.getId())) {
                        continue;
                    }

                    for (RouteTableKey routeTableKey : recipientKeys) {
                        AppSeqNumber endpointProfileSeqNumber = cacheService.getAppSeqNumber(routeTableKey.getAppToken());
                        if (!endpointProfile.getApplicationId().equals(endpointProfileSeqNumber.getAppId())) {
                            continue;
                        }
                        for (EventClassFamilyVersionStateDto ecfVersionDto : profile.getEcfVersionStates()) {
                            if (ecfVersionDto.getEcfId().equals(routeTableKey.getEcfVersion().getEcfId())
                                    && ecfVersionDto.getVersion() == routeTableKey.getEcfVersion().getVersion()) {
                                eventClassSet.add(EndpointObjectHash.fromBytes(endpointProfile.getEndpointKeyHash()));
                            }
                        }
                    }
                }
            } else {
                LOG.warn("Lookup event class family version using tenant [{}] and event class fqn {} FAILED!", tenantId, eventClassFqn);
            }
            if (eventClassIntersectionSet == null) {
                eventClassIntersectionSet = eventClassSet;
            } else {
                eventClassIntersectionSet.retainAll(eventClassSet);
            }
        }

        List<String> result = new ArrayList<>();
        for (EndpointObjectHash eoHash : eventClassIntersectionSet) {
            result.add(Base64Util.encode(eoHash.getData()));
        }
        return new EventListenersResponse(request.getRequestId(), result, SyncStatus.SUCCESS);
    }
}
