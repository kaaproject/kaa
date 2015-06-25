/**
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

import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Denis Kimcherenko
 */
public class PowerPlantDemoBuilder extends AbstractDemoBuilder {


    private static final Logger logger = LoggerFactory.getLogger(PowerPlantDemoBuilder.class);

    protected PowerPlantDemoBuilder() {
        super("demo/powerplant");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Power plant demo application' data...");

        loginTenantAdmin(client);

        ApplicationDto powerPlantApplication = new ApplicationDto();
        powerPlantApplication.setName("Power plant");
        powerPlantApplication = client.editApplication(powerPlantApplication);

        sdkPropertiesDto.setApplicationId(powerPlantApplication.getId());
        sdkPropertiesDto.setProfileSchemaVersion(1);
        sdkPropertiesDto.setNotificationSchemaVersion(1);

        loginTenantDeveloper(client);

        /*
         * Configure the data collection feature.
         */

        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setApplicationId(powerPlantApplication.getId());
        logSchemaDto.setName("Power plant log schema");
        logSchemaDto.setDescription("Log schema describes incoming voltage reports");
        logSchemaDto = client.createLogSchema(logSchemaDto, getResourcePath("logSchema.json"));
        sdkPropertiesDto.setLogSchemaVersion(logSchemaDto.getMajorVersion());

        LogAppenderDto powerPlantLogAppender = new LogAppenderDto();
        powerPlantLogAppender.setName("Power plant log appender");
        powerPlantLogAppender.setDescription("Log appender used to deliver voltage reports from power plant application to the REST server");
        powerPlantLogAppender.setApplicationId(powerPlantApplication.getId());
        powerPlantLogAppender.setApplicationToken(powerPlantApplication.getApplicationToken());
        powerPlantLogAppender.setTenantId(powerPlantApplication.getTenantId());
        powerPlantLogAppender.setMinLogSchemaVersion(1);
        powerPlantLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        powerPlantLogAppender.setConfirmDelivery(true);
        powerPlantLogAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.KEYHASH,
                LogHeaderStructureDto.TIMESTAMP, LogHeaderStructureDto.TOKEN, LogHeaderStructureDto.VERSION));
        powerPlantLogAppender.setPluginTypeName("REST");
        powerPlantLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.rest.appender.RestLogAppender");
        powerPlantLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("restAppender.json")));
        powerPlantLogAppender = client.editLogAppenderDto(powerPlantLogAppender);

        /*
         * Configure the configuration feature.
         */

        logger.info("Creating configuration schema...");

        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(powerPlantApplication.getId());
        configurationSchema.setName("Power plant configuration schema");
        configurationSchema.setDescription("Default configuration schema for the power plant application");
        configurationSchema = client.createConfigurationSchema(configurationSchema, getResourcePath("configSchema.json"));

        logger.info("Configuration schema version: {}", configurationSchema.getMajorVersion());
        sdkPropertiesDto.setConfigurationSchemaVersion(configurationSchema.getMajorVersion());
        logger.info("Configuration schema was created.");

        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroups(powerPlantApplication.getId());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }

        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group for the power plant application!");
        }

        ConfigurationDto baseConfiguration = new ConfigurationDto();
        baseConfiguration.setApplicationId(powerPlantApplication.getId());
        baseConfiguration.setEndpointGroupId(baseEndpointGroup.getId());
        baseConfiguration.setSchemaId(configurationSchema.getId());
        baseConfiguration.setMajorVersion(configurationSchema.getMajorVersion());
        baseConfiguration.setMinorVersion(configurationSchema.getMinorVersion());
        baseConfiguration.setDescription("Base power plant configuration");
        String body = FileUtils.readResource(getResourcePath("configData.json"));
        logger.info("Configuration body: [{}]", body);
        baseConfiguration.setBody(body);
        baseConfiguration.setStatus(UpdateStatus.INACTIVE);
        logger.info("Editing the configuration...");
        baseConfiguration = client.editConfiguration(baseConfiguration);
        logger.info("Activating the configuration");
        client.activateConfiguration(baseConfiguration.getId());
        logger.info("Configuration was activated");

        logger.info("Finished loading 'Power plant application' data.");
    }

}
