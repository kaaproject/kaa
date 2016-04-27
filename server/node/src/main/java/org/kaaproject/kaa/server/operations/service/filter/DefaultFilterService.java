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

package org.kaaproject.kaa.server.operations.service.filter;

import java.util.LinkedList;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.server.operations.service.cache.AppProfileVersionsKey;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link FilterService FilterService}.
 *
 * @author ashvayka
 */
@Service
public class DefaultFilterService implements FilterService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultFilterService.class);

    /** The cache service. */
    @Autowired
    private CacheService cacheService;

    /**
     * Instantiates a new default filter service.
     */
    public DefaultFilterService() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.filter.FilterService#
     * getAllMatchingFilters
     * (org.kaaproject.kaa.server.operations.service.cache.AppVersionKey,
     * java.lang.String)
     */
    @Override
    public List<ProfileFilterDto> getAllMatchingFilters(AppProfileVersionsKey key, EndpointProfileDto profile) {
        String endpointProfileSchemaBody = getEndpointProfileSchemaBody(key);
        String serverProfileSchemaBody = getServerProfileSchemaBody(key);

        List<ProfileFilterDto> filters = cacheService.getFilters(key);
        LOG.trace("Found {} filters by {}", filters.size(), key);
        List<ProfileFilterDto> matchingFilters = new LinkedList<ProfileFilterDto>();
        FilterEvaluator filterEvaluator = null;
        for (ProfileFilterDto filter : filters) {
            if (filterEvaluator == null) {
                filterEvaluator = new DefaultFilterEvaluator();
                filterEvaluator.init(profile, endpointProfileSchemaBody, serverProfileSchemaBody);
            }
            LOG.trace("matching profile body with filter [{}]: {}", filter.getId(), filter.getBody());
            if (checkFilter(filterEvaluator, filter)) {
                matchingFilters.add(filter);
            }
        }
        return matchingFilters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.filter.FilterService#matches
     * (java.lang.String, java.lang.String)
     */
    @Override
    public boolean matches(String appToken, String profileFilterId, EndpointProfileDto profile) {
        AppProfileVersionsKey key = new AppProfileVersionsKey(appToken, profile.getClientProfileVersion(),
                profile.getServerProfileVersion());
        String endpointProfileSchemaBody = getEndpointProfileSchemaBody(key);
        String serverProfileSchemaBody = getServerProfileSchemaBody(key);

        FilterEvaluator filterEvaluator = new DefaultFilterEvaluator();
        filterEvaluator.init(profile, endpointProfileSchemaBody, serverProfileSchemaBody);

        ProfileFilterDto filter = cacheService.getFilter(profileFilterId);
        LOG.trace("matching profile body with filter [{}]: {}", filter.getId(), filter.getBody());
        return checkFilter(filterEvaluator, filter);
    }

    private boolean checkFilter(FilterEvaluator filterEvaluator, ProfileFilterDto filter) {
        try {
            if (filterEvaluator.matches(filter)) {
                LOG.trace("profile body matched");
                return true;
            }
        } catch (EvaluationException ee) {
            LOG.warn("Failed to process filter [{}]: {} due to evaluate exception. Please check your filter body", filter.getId(),
                    filter.getBody(), ee);
        } catch (Exception e) {
            LOG.error("Failed to process filter [{}]: {} due to exception", filter.getId(), filter.getBody(), e);
        }
        return false;
    }

    private String getServerProfileSchemaBody(AppProfileVersionsKey key) {
        ServerProfileSchemaDto serverProfileSchema = cacheService.getServerProfileSchemaByAppAndVersion(new AppVersionKey(key
                .getApplicationToken(), key.getServerProfileSchemaVersion()));

        String serverProfileSchemaBody = cacheService.getFlatCtlSchemaById(serverProfileSchema.getCtlSchemaId());
        return serverProfileSchemaBody;
    }

    private String getEndpointProfileSchemaBody(AppProfileVersionsKey key) {
        EndpointProfileSchemaDto endpointProfileSchema = cacheService.getProfileSchemaByAppAndVersion(new AppVersionKey(key
                .getApplicationToken(), key.getEndpointProfileSchemaVersion()));
        String endpointProfileSchemaBody = cacheService.getFlatCtlSchemaById(endpointProfileSchema.getCtlSchemaId());
        return endpointProfileSchemaBody;
    }

}
