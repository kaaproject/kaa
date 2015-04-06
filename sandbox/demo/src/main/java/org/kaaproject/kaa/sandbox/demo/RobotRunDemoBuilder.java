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

package org.kaaproject.kaa.sandbox.demo;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RobotRunDemoBuilder extends AbstractDemoBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(RobotRunDemoBuilder.class);

    protected RobotRunDemoBuilder() {
        super("demo/robotrun");
    }
    
    @Override
    protected void buildDemoApplicationImpl(AdminClient client)
            throws Exception {
        logger.info("Loading 'Robot Run Demo Application' data...");
        
        loginTenantAdmin(client);
        
        EventClassFamilyDto robotRunEventClassFamily = new EventClassFamilyDto();
        robotRunEventClassFamily.setName("Robot Run Event Class Family");
        robotRunEventClassFamily.setNamespace("org.kaaproject.kaa.examples.robotrun.gen.event");
        robotRunEventClassFamily.setClassName("RobotRunEventClassFamily");
        robotRunEventClassFamily = client.editEventClassFamily(robotRunEventClassFamily);
        client.addEventClassFamilySchema(robotRunEventClassFamily.getId(), getResourcePath("robotRunEventClassFamily.json"));

        ApplicationDto robotRunApplication = new ApplicationDto();
        robotRunApplication.setName("Robot Run");
        robotRunApplication = client.editApplication(robotRunApplication);
        
        sdkPropertiesDto.setApplicationId(robotRunApplication.getId());
        sdkPropertiesDto.setProfileSchemaVersion(1);
        sdkPropertiesDto.setNotificationSchemaVersion(1);

        loginTenantDeveloper(client);
        
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(robotRunApplication.getId());
        configurationSchema.setName("Labirynth schema");
        configurationSchema.setDescription("Configuration schema describing labirynth");
        configurationSchema = client.createConfigurationSchema(configurationSchema, getResourcePath("configSchema.json"));
        sdkPropertiesDto.setConfigurationSchemaVersion(configurationSchema.getMajorVersion());
        
        LogSchemaDto logSchema = new LogSchemaDto();
        logSchema.setApplicationId(robotRunApplication.getId());
        logSchema.setName("Cell log schema");
        logSchema.setDescription("Log schema describing information about discovered cell");
        logSchema = client.createLogSchema(logSchema, getResourcePath("logSchema.json"));
        sdkPropertiesDto.setLogSchemaVersion(logSchema.getMajorVersion());
        
        ApplicationEventFamilyMapDto robotRunAefMap = mapEventClassFamily(client, robotRunApplication, robotRunEventClassFamily);

        List<String> aefMapIds = new ArrayList<>();
        aefMapIds.add(robotRunAefMap.getId());

        sdkPropertiesDto.setAefMapIds(aefMapIds);
        
        logger.info("Finished loading 'Robot Run Demo Application' data.");

    }

}
