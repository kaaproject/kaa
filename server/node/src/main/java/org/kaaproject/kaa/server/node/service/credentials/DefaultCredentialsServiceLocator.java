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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.service.TrustfulCredentialsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
@Service
public final class DefaultCredentialsServiceLocator implements CredentialsServiceLocator {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCredentialsServiceLocator.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ApplicationService applicationService;

    @Override
    public CredentialsService getCredentialsService(String applicationId) {
        String serviceName = this.applicationService.findAppById(applicationId).getCredentialsServiceName();
        if (StringUtils.isBlank(serviceName)) {
            serviceName = StringUtils.uncapitalize(TrustfulCredentialsService.class.getSimpleName());
            LOG.debug("No credentials service configured for application [{}], using [{}]", applicationId, serviceName);
        }
        return new CredentialsServiceAdapter(applicationId, this.applicationContext.getBean(serviceName,
            org.kaaproject.kaa.server.common.dao.CredentialsService.class));
    }

    @Override
    public List<String> getCredentialsServiceNames() {
        Map<String, org.kaaproject.kaa.server.common.dao.CredentialsService> beans =
            this.applicationContext.getBeansOfType(org.kaaproject.kaa.server.common.dao.CredentialsService.class);
        return beans.keySet().stream().flatMap(name -> Arrays.stream(this.applicationContext.getAliases(name))).collect(Collectors.toList());
    }
}
