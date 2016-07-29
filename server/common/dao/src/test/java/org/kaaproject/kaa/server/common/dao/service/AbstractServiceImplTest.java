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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractServiceImplTest {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    protected static final String APPLICATION_NAME = "Generated Test Application";
    protected static final String TENANT_NAME = "Generated Test Tenant";
    protected static final String USER_NAME = "Generated Test Username";
    protected static final String DEFAULT_FQN = "org.kaaproject.kaa.ctl.TestSchema";

    protected static final Random random = new Random(0);

    @Autowired
    protected UserService userService;
    @Autowired
    protected ConfigurationService configurationService;
    @Autowired
    protected ApplicationService applicationService;
    @Autowired
    protected CTLService ctlService;
    @Autowired
    protected ProfileService profileService;
    @Autowired
    protected EndpointService endpointService;

    protected TenantDto generateTenant() {
        TenantDto tenant = new TenantDto();
        tenant.setName(TENANT_NAME);
        tenant = userService.saveTenant(tenant);
        return tenant;
    }

    protected ApplicationDto generateApplication(String tenantId) {
        ApplicationDto application = new ApplicationDto();
        if (isBlank(tenantId)) {
            application.setTenantId(generateTenant().getId());
        } else {
            application.setTenantId(tenantId);
        }
        application.setName(APPLICATION_NAME);
        return applicationService.saveApp(application);
    }

    protected List<UserDto> generateUsers(String tenantId, KaaAuthorityDto authority, int count) {
        List<UserDto> users = new ArrayList<>(count);
        UserDto userDto;
        for (int i = 0; i < count; i++) {
            userDto = new UserDto();
            userDto.setUsername(USER_NAME);
            userDto.setTenantId(tenantId);
            userDto.setExternalUid(UUID.randomUUID().toString());
            userDto.setAuthority(authority);
            userDto = userService.saveUser(userDto);
            users.add(userDto);
        }
        return users;
    }


    protected String readSchemaFileAsString(String filePath) throws IOException {
        try {
            Path path = Paths.get(Thread.currentThread().getContextClassLoader().getResource(filePath).toURI());
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes);
        } catch (URISyntaxException e) {
            LOG.error("Can't generate configs {}", e);
        }
        return null;
    }
    
    protected CTLSchemaDto generateCTLSchemaDto(String fqn, String tenantId, int version) {
        CTLSchemaDto ctlSchema = new CTLSchemaDto();
        CTLSchemaMetaInfoDto metaInfoDto = new CTLSchemaMetaInfoDto(fqn, tenantId);
        ctlSchema.setMetaInfo(metaInfoDto);
        ctlSchema.setVersion(version);
        ctlSchema.setBody(UUID.randomUUID().toString());
        return ctlSchema;
    }

    protected List<EndpointProfileSchemaDto> generateProfSchema(String tenantId, String applicationId, int count) {
        List<EndpointProfileSchemaDto> schemas = Collections.emptyList();
        try {
            if (isBlank(tenantId)) {
                tenantId = generateTenant().getId();
            }
            if (isBlank(applicationId)) {
                applicationId = generateApplication(tenantId).getId();
            }
            CTLSchemaDto ctlSchemaDto = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN, tenantId, 1));
            EndpointProfileSchemaDto schemaDto;
            schemas = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                schemaDto = new EndpointProfileSchemaDto();
                schemaDto.setApplicationId(applicationId);
                schemaDto.setCtlSchemaId(ctlSchemaDto.getId());
                schemaDto.setCreatedUsername("Test User");
                schemaDto.setName("Test Name");
                schemaDto = profileService.saveProfileSchema(schemaDto);
                Assert.assertNotNull(schemaDto);
                schemas.add(schemaDto);
            }
        } catch (Exception e) {
            LOG.error("Can't generate profile schemas {}", e);
            Assert.fail("Can't generate profile schemas.");
        }
        return schemas;
    }

    protected EndpointGroupDto generateEndpointGroup(String applicationId) {
        return generateEndpointGroup(applicationId, "GROUP_ALL");
    }

    protected EndpointGroupDto generateEndpointGroup(String applicationId, String endpointGroupName) {
        EndpointGroupDto group = new EndpointGroupDto();
        if (isBlank(applicationId)) {
            applicationId = generateApplication(null).getId();
        }
        group.setApplicationId(applicationId);
        group.setName(endpointGroupName);
        group.setWeight(random.nextInt());
        return endpointService.saveEndpointGroup(group);
    }
}
