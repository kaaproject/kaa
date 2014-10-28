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
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerDemoBuilder extends AbstractDemoBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerDemoBuilder.class);
    
    protected PlayerDemoBuilder() {
        super();
    }
    
    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {
        
        logger.info("Loading 'Player Demo Application' data...");
        
        loginTenantAdmin(client);
        
        EventClassFamilyDto playerEventClassFamily = new EventClassFamilyDto();
        playerEventClassFamily.setName("Player Event Class Family");
        playerEventClassFamily.setNamespace("org.kaaproject.kaa.demo.player");
        playerEventClassFamily.setClassName("PlayerClassFamily");
        
        playerEventClassFamily = client.editEventClassFamily(playerEventClassFamily);
        
        client.addEventClassFamilySchema(playerEventClassFamily.getId(), "demo/player/eventClassFamily.json");
        
        ApplicationDto playerApplication = new ApplicationDto();
        playerApplication.setName("Player");
        playerApplication = client.editApplication(playerApplication);
        sdkKey.setApplicationId(playerApplication.getId());
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setConfigurationSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setLogSchemaVersion(1);
        sdkKey.setTargetPlatform(SdkPlatform.ANDROID);
        
        loginTenantDeveloper(client);
        
        List<EventClassDto> eventClasses = 
                client.getEventClassesByFamilyIdVersionAndType(playerEventClassFamily.getId(), 1, EventClassType.EVENT);

        ApplicationEventFamilyMapDto playerAefMap = new ApplicationEventFamilyMapDto();
        playerAefMap.setApplicationId(playerApplication.getId());
        playerAefMap.setEcfId(playerEventClassFamily.getId());
        playerAefMap.setEcfName(playerEventClassFamily.getName());
        playerAefMap.setVersion(1);
        
        List<ApplicationEventMapDto> playerEventMaps = new ArrayList<>(eventClasses.size());
        for (EventClassDto eventClass : eventClasses) {
            ApplicationEventMapDto eventMap = new ApplicationEventMapDto();
            eventMap.setEventClassId(eventClass.getId());
            eventMap.setFqn(eventClass.getFqn());
                eventMap.setAction(ApplicationEventAction.BOTH);
            playerEventMaps.add(eventMap);
        }
        
        playerAefMap.setEventMaps(playerEventMaps);
        playerAefMap = client.editApplicationEventFamilyMap(playerAefMap);
        List<String> aefMapIds = Collections.singletonList(playerAefMap.getId());
        sdkKey.setAefMapIds(aefMapIds);
        logger.info("Finished loading 'Player Demo Application' data.");
    }

    @Override
    protected void setupProjectConfigs() {
        Project projectConfig = new Project();
        projectConfig.setId("player_demo");
        projectConfig.setName("Player Demo");
        projectConfig.setDescription("Music player on android platform demonstrating event subsystem");
        projectConfig.setPlatform(Platform.ANDROID);
        projectConfig.setSourceArchive("android/player_demo.tar.gz");
        projectConfig.setProjectFolder("player_demo/PlayerDemo");
        projectConfig.setSdkLibDir("player_demo/PlayerDemo/libs");
        projectConfig.setDestBinaryFile("player_demo/PlayerDemo/bin/PlayerDemo-debug.apk");
        projectConfigs.add(projectConfig);
    }

}
