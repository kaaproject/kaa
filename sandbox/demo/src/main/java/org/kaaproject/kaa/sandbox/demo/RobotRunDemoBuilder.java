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
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RobotRunDemoBuilder extends AbstractDemoBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(RobotRunDemoBuilder.class);

    protected RobotRunDemoBuilder() {
        super();
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
        client.addEventClassFamilySchema(robotRunEventClassFamily.getId(), "demo/robotrun/robotRunEventClassFamily.json");

        ApplicationDto robotRunApplication = new ApplicationDto();
        robotRunApplication.setName("Robot Run");
        robotRunApplication = client.editApplication(robotRunApplication);
        
        sdkKey.setApplicationId(robotRunApplication.getId());
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);

        loginTenantDeveloper(client);
        
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(robotRunApplication.getId());
        configurationSchema.setName("Labirynth schema");
        configurationSchema.setDescription("Configuration schema describing labirynth");
        configurationSchema = client.createConfigurationSchema(configurationSchema, "demo/robotrun/configSchema.json");
        sdkKey.setConfigurationSchemaVersion(configurationSchema.getMajorVersion());
        
        LogSchemaDto logSchema = new LogSchemaDto();
        logSchema.setApplicationId(robotRunApplication.getId());
        logSchema.setName("Cell log schema");
        logSchema.setDescription("Log schema describing information about discovered cell");
        logSchema = client.createLogSchema(logSchema, "demo/robotrun/logSchema.json");
        sdkKey.setLogSchemaVersion(logSchema.getMajorVersion());
        
        ApplicationEventFamilyMapDto robotRunAefMap = mapEventClassFamily(client, robotRunApplication, robotRunEventClassFamily);

        List<String> aefMapIds = new ArrayList<>();
        aefMapIds.add(robotRunAefMap.getId());

        sdkKey.setAefMapIds(aefMapIds);
        
        logger.info("Finished loading 'Robot Run Demo Application' data.");

    }
    
    private ApplicationEventFamilyMapDto mapEventClassFamily(AdminClient client, ApplicationDto application, EventClassFamilyDto eventClassFamily) throws Exception {
        List<EventClassDto> eventClasses = 
                client.getEventClassesByFamilyIdVersionAndType(eventClassFamily.getId(), 1, EventClassType.EVENT);

        ApplicationEventFamilyMapDto aefMap = new ApplicationEventFamilyMapDto();
        aefMap.setApplicationId(application.getId());
        aefMap.setEcfId(eventClassFamily.getId());
        aefMap.setEcfName(eventClassFamily.getName());
        aefMap.setVersion(1);
        
        List<ApplicationEventMapDto> eventMaps = new ArrayList<>(eventClasses.size());
        for (EventClassDto eventClass : eventClasses) {
            ApplicationEventMapDto eventMap = new ApplicationEventMapDto();
            eventMap.setEventClassId(eventClass.getId());
            eventMap.setFqn(eventClass.getFqn());
                eventMap.setAction(ApplicationEventAction.BOTH);
            eventMaps.add(eventMap);
        }
        
        aefMap.setEventMaps(eventMaps);
        aefMap = client.editApplicationEventFamilyMap(aefMap);
        return aefMap;
    }

    @Override
    protected void setupProjectConfigs() {
        Project projectConfig = new Project();
        projectConfig.setId("robotrun_demo_desktop");
        projectConfig.setName("Robot Run Demo Desktop");
        projectConfig.setDescription("Robot Run java application to control and visualize labirynth");
        projectConfig.setPlatform(Platform.JAVA);
        projectConfig.setSourceArchive("java/robotrun_demo.tar.gz");
        projectConfig.setProjectFolder("");
        projectConfig.setSdkLibDir("lib");
        projectConfig.setDestBinaryFile("target/visualization.tar.gz");
        projectConfigs.add(projectConfig);
        
        projectConfig = new Project();
        projectConfig.setId("robotrun_demo_android");
        projectConfig.setName("Robot Run Demo Android");
        projectConfig.setDescription("Robot Run android application to control robot");
        projectConfig.setPlatform(Platform.ANDROID);
        projectConfig.setSourceArchive("android/robotrun_demo.tar.gz");
        projectConfig.setProjectFolder("robotrun_demo/RobotRun");
        projectConfig.setSdkLibDir("robotrun_demo/RobotRun/libs");
        projectConfig.setDestBinaryFile("robotrun_demo/RobotRun/bin/RobotRun-debug.apk");
        projectConfigs.add(projectConfig);
    }

}
