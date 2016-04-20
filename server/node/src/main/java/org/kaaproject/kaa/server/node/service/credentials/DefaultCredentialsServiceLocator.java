/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.node.service.credentials;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The default implementation of the {@link CredentialsServiceLocator}
 * interface.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Service("rootCredentialsServiceLocator")
public final class DefaultCredentialsServiceLocator implements CredentialsServiceLocator, CredentialsServiceRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCredentialsServiceLocator.class);

    public static final String DEFAULT_CREDENTIALS_SERVICE_NAME = "Trustful";

    @Autowired
    private CacheService cacheService;

    @Resource
    private Map<String, CredentialsServiceLocator> credentialsServiceLocatorMap;

    @Override
    public CredentialsService getCredentialsService(String applicationId) {
        String serviceName = this.cacheService.findAppById(applicationId).getCredentialsServiceName();
        if (StringUtils.isBlank(serviceName)) {
            serviceName = DEFAULT_CREDENTIALS_SERVICE_NAME;
            LOG.debug("No credentials service configured for application [{}], using [{}]", applicationId, serviceName);
        }
        CredentialsServiceLocator locator = credentialsServiceLocatorMap.get(serviceName);
        if (locator == null) {
            throw new IllegalStateException("Can't find credentials service factory for name: " + serviceName);
        } else {
            return locator.getCredentialsService(applicationId);
        }
    }

    @Override
    public List<String> getCredentialsServiceNames() {
        return this.credentialsServiceLocatorMap.keySet().stream().sorted().collect(Collectors.toList());
    }
}
