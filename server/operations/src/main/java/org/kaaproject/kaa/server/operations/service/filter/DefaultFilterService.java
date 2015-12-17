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

package org.kaaproject.kaa.server.operations.service.filter;

import java.util.LinkedList;
import java.util.List;

import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
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

    /** The Constant logger. */
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
    public List<ProfileFilterDto> getAllMatchingFilters(AppVersionKey appProfileVersionKey, String profileBody) {
        ProfileSchemaDto profileSchema = cacheService.getProfileSchemaByAppAndVersion(appProfileVersionKey);
        List<ProfileFilterDto> filters = cacheService.getFilters(appProfileVersionKey);
        LOG.trace("Found {} filters by {}", filters.size(), appProfileVersionKey);
        List<ProfileFilterDto> matchingFilters = new LinkedList<ProfileFilterDto>();
        Filter filter = null;
        for (ProfileFilterDto filterBody : filters) {
            if (filter == null) {
                filter = new DefaultFilter(filterBody.getBody(), profileSchema.getSchema());
            } else {
                filter.updateFilterBody(filterBody.getBody());
            }
            LOG.trace("matching profile body with filter {}", filter);
            try {
                if (filter.matches(profileBody)) {
                    matchingFilters.add(filterBody);
                    LOG.trace("profile body matched");
                }
            } catch (EvaluationException ee) {
                LOG.warn("Failed to process filter {} due to evaluate exception. Please check your filter body", filterBody.getBody(), ee);
            } catch (Exception e) {
                LOG.error("Failed to process filter {} due to exception", filterBody.getBody(), e);
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
    public boolean matches(String appToken, String profileFilterId, String profileBody) {
        try {
            ProfileFilterDto filterDto = cacheService.getFilter(profileFilterId);
            AppVersionKey appProfileVersionKey = new AppVersionKey(appToken, filterDto.getMajorVersion());
            ProfileSchemaDto profileSchema = cacheService.getProfileSchemaByAppAndVersion(appProfileVersionKey);
            Filter filter = new DefaultFilter(filterDto.getBody(), profileSchema.getSchema());
            LOG.trace("matching profile body with filter {}", filter);
            return filter.matches(profileBody);
        } catch (EvaluationException ee) {
            LOG.warn("Failed to process filter {} due to evaluate exception. Please check your filter body", profileFilterId, ee);
        } catch (Exception e) {
            LOG.error("Failed to process filter {} due to exception", profileFilterId, e);
        }
        return false;
    }

}
