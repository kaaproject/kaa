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

import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CityLightsControllerDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CityLightsControllerDemoBuilder.class);

    private static final String KAA_CLIENT_CONFIG = "kaaClientConfiguration";
    private static final String TRAFFIC_LIGHTS_APPLICATION_NAME = "Traffic lights driver";
    private static final String STREET_LIGHT_APPLICATION_NAME = "Street light driver";
    private static final String TRAFFIC_LIGHTS_APP_TOKEN_PROPERTY = "trafficLightsAppToken";
    private static final String STREET_LIGHT_APP_TOKEN_PROPERTY = "streetLightsAppToken";

    public CityLightsControllerDemoBuilder() {
        super("demo/citylightscontroller");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'City lights controller application' data...");

        loginTenantAdmin(client);

        ApplicationDto cityLightsDemoApplication = new ApplicationDto();
        cityLightsDemoApplication.setName("City lights controller demo");
        cityLightsDemoApplication = client.editApplication(cityLightsDemoApplication);
        sdkPropertiesDto.setApplicationId(cityLightsDemoApplication.getId());
        sdkPropertiesDto.setProfileSchemaVersion(1);
        sdkPropertiesDto.setNotificationSchemaVersion(1);
        sdkPropertiesDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);

        logger.info("Creating configuration schema...");
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(cityLightsDemoApplication.getId());
        configurationSchema.setName("City lights controller schema");
        configurationSchema.setDescription("Default configuration schema for the city lights controller application");
        configurationSchema = client.createConfigurationSchema(configurationSchema, getResourcePath("config_schema.avsc"));
        logger.info("Configuration schema version: {}", configurationSchema.getMajorVersion());
        sdkPropertiesDto.setConfigurationSchemaVersion(configurationSchema.getMajorVersion());
        logger.info("Configuration schema was created.");

        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroups(cityLightsDemoApplication.getId());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }

        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group for the city lights controller application!");
        }

        ConfigurationDto baseConfiguration = new ConfigurationDto();
        baseConfiguration.setApplicationId(cityLightsDemoApplication.getId());
        baseConfiguration.setEndpointGroupId(baseEndpointGroup.getId());
        baseConfiguration.setSchemaId(configurationSchema.getId());
        baseConfiguration.setMajorVersion(configurationSchema.getMajorVersion());
        baseConfiguration.setMinorVersion(configurationSchema.getMinorVersion());
        baseConfiguration.setDescription("Base city lights controller configuration");
        String body = FileUtils.readResource(getResourcePath("config_data.json"));
        logger.info("Configuration body: [{}]", body);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> configBody = objectMapper.readValue(body, Map.class);
        logger.info("Getting config body: [{}]", configBody);
        Map<String, Object> kaaClientConfig = (Map<String, Object>) configBody.get(KAA_CLIENT_CONFIG);
        logger.info("Getting kaaClientConfig: [{}]", kaaClientConfig);
        logger.info("Getting traffic lights and traffic lights app tokens...");
        List<ApplicationDto> applications = client.getApplications();
        logger.info("All available applications: [{}]", applications);
        ApplicationDto trafficLightsApplication = getApplicationWithName(applications, TRAFFIC_LIGHTS_APPLICATION_NAME);
        if (trafficLightsApplication == null) {
            logger.error(formErrorLogMessage(TRAFFIC_LIGHTS_APPLICATION_NAME));
            throw new RuntimeException("Can't get '" + TRAFFIC_LIGHTS_APPLICATION_NAME + "' application!");
        }
        logger.info(TRAFFIC_LIGHTS_APPLICATION_NAME + " application was found: {}", trafficLightsApplication);
        ApplicationDto streetLightApplication = getApplicationWithName(applications, STREET_LIGHT_APPLICATION_NAME);
        if (streetLightApplication == null) {
            logger.error(formErrorLogMessage(STREET_LIGHT_APPLICATION_NAME));
            throw new RuntimeException("Can't get '" + STREET_LIGHT_APPLICATION_NAME + "' application!");
        }
        logger.info(STREET_LIGHT_APPLICATION_NAME + " application was found: {}", streetLightApplication);
        kaaClientConfig.put(TRAFFIC_LIGHTS_APP_TOKEN_PROPERTY, trafficLightsApplication.getApplicationToken());
        kaaClientConfig.put(STREET_LIGHT_APP_TOKEN_PROPERTY, streetLightApplication.getApplicationToken());
        body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configBody);
        logger.info("Configuration body after altering: [{}]", body);
        baseConfiguration.setBody(body);
        baseConfiguration.setStatus(UpdateStatus.INACTIVE);
        logger.info("Editing the configuration...");
        baseConfiguration = client.editConfiguration(baseConfiguration);
        logger.info("Configuration was successfully edited");
        logger.info("Activating the configuration");
        client.activateConfiguration(baseConfiguration.getId());
        logger.info("Configuration was activated");

        logger.info("Finished loading 'City lights controller application' data...");
    }

    private ApplicationDto getApplicationWithName(List<ApplicationDto> applications, String name) {
        for (ApplicationDto application : applications) {
            if (name.equalsIgnoreCase(application.getName())) {
                return application;
            }
        }
        return null;
    }

    private String formErrorLogMessage(String applicationName) {
        return  "No application with name as '" + applicationName +
                "' was found. Possible reasons: " +
                "1. You haven't added '" + applicationName + " application' to DemoBuildersRegistry; " +
                "2. City lights controller build is registered before " + applicationName + ".";
    }
}
