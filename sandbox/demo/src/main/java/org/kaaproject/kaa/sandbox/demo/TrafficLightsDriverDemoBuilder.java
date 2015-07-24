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

import java.util.Arrays;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficLightsDriverDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(TrafficLightsDriverDemoBuilder.class);

    protected TrafficLightsDriverDemoBuilder() {
        super("demo/trafficlights");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Traffic Lights driver application' data...");

        loginTenantAdmin(client);

        ApplicationDto trafficLightsApplication = new ApplicationDto();
        trafficLightsApplication.setName("Traffic lights driver");
        trafficLightsApplication = client.editApplication(trafficLightsApplication);

        sdkPropertiesDto.setApplicationId(trafficLightsApplication.getId());
        sdkPropertiesDto.setProfileSchemaVersion(1);
        sdkPropertiesDto.setNotificationSchemaVersion(1);
        sdkPropertiesDto.setLogSchemaVersion(1);
        sdkPropertiesDto.setConfigurationSchemaVersion(1);

        loginTenantDeveloper(client);

        logger.info("Creating log schema...");
        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setApplicationId(trafficLightsApplication.getId());
        logSchemaDto.setName("TrafficLightsLog schema");
        logSchemaDto.setDescription("Traffic Lights driver log schema");
        logSchemaDto = client.createLogSchema(logSchemaDto, getResourcePath("log.avsc"));
        logger.info("Log schema version: {}", logSchemaDto.getMajorVersion());
        sdkPropertiesDto.setLogSchemaVersion(logSchemaDto.getMajorVersion());
        logger.info("Log schema was created.");

        LogAppenderDto appenderDto = new LogAppenderDto();
        appenderDto.setName("Data collection log appender");
        appenderDto.setApplicationId(trafficLightsApplication.getId());
        appenderDto.setApplicationToken(trafficLightsApplication.getApplicationToken());
        appenderDto.setTenantId(trafficLightsApplication.getTenantId());
        appenderDto.setMinLogSchemaVersion(1);
        appenderDto.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        appenderDto.setConfirmDelivery(true);

        appenderDto.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.KEYHASH,
                LogHeaderStructureDto.TIMESTAMP, LogHeaderStructureDto.TOKEN, LogHeaderStructureDto.VERSION));
        appenderDto.setPluginTypeName("REST");
        appenderDto.setPluginClassName("org.kaaproject.kaa.server.appenders.rest.appender.RestLogAppender");
        appenderDto.setJsonConfiguration(FileUtils.readResource(getResourcePath("rest_appender.json")));
        appenderDto = client.editLogAppenderDto(appenderDto);

        logger.info("Creating configuration schema...");
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(trafficLightsApplication.getId());
        configurationSchema.setName("TrafficLightsConfiguration schema");
        configurationSchema.setDescription("Traffic Lights configuration schema");
        configurationSchema = client.createConfigurationSchema(configurationSchema, getResourcePath("configuration.avsc"));
        logger.info("Configuration schema version: {}", configurationSchema.getMajorVersion());
        sdkPropertiesDto.setConfigurationSchemaVersion(configurationSchema.getMajorVersion());
        logger.info("Configuration schema was created");

        logger.info("Finished loading 'Traffic lights driver application' data...");
    }


}
