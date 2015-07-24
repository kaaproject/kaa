/*
 * Copyright 2014-2015 CyberVision, Inc.
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

/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.sandbox.demo;

import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationDemoBuilder extends AbstractDemoBuilder{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationDemoBuilder.class);

    public ConfigurationDemoBuilder() {
        super("demo/configuration");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Configuration demo application' data...");

        loginTenantAdmin(client);

        ApplicationDto configurationApplication = new ApplicationDto();
        configurationApplication.setName("Configuration demo");
        configurationApplication = client.editApplication(configurationApplication);

        sdkPropertiesDto.setApplicationId(configurationApplication.getId());
        sdkPropertiesDto.setApplicationToken(configurationApplication.getApplicationToken());
        sdkPropertiesDto.setProfileSchemaVersion(1);
        sdkPropertiesDto.setNotificationSchemaVersion(1);
        sdkPropertiesDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);

        logger.info("Creating configuration schema...");
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(configurationApplication.getId());
        configurationSchema.setName("ConfigurationDemo schema");
        configurationSchema.setDescription("Default configuration schema for the configuration demo application");
        configurationSchema = client.createConfigurationSchema(configurationSchema, getResourcePath("config_schema.avsc"));
        logger.info("Configuration schema version: {}", configurationSchema.getMajorVersion());
        sdkPropertiesDto.setConfigurationSchemaVersion(configurationSchema.getMajorVersion());
        logger.info("Configuration schema was created.");

        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroups(configurationApplication.getId());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }

        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group for configuration demo application!");
        }

        ConfigurationDto baseConfiguration = new ConfigurationDto();
        baseConfiguration.setApplicationId(configurationApplication.getId());
        baseConfiguration.setEndpointGroupId(baseEndpointGroup.getId());
        baseConfiguration.setSchemaId(configurationSchema.getId());
        baseConfiguration.setMajorVersion(configurationSchema.getMajorVersion());
        baseConfiguration.setMinorVersion(configurationSchema.getMinorVersion());
        baseConfiguration.setDescription("Base configuration demo configuration");
        String body = FileUtils.readResource(getResourcePath("config_data.json"));
        logger.info("Configuration body: [{}]", body);
        baseConfiguration.setBody(body);
        baseConfiguration.setStatus(UpdateStatus.INACTIVE);
        logger.info("Editing the configuration...");
        baseConfiguration = client.editConfiguration(baseConfiguration);
        logger.info("Configuration was successfully edited");
        logger.info("Activating the configuration");
        client.activateConfiguration(baseConfiguration.getId());
        logger.info("Configuration was activated");

        logger.info("Finished loading 'Configuration demo application' data...");
    }
}
