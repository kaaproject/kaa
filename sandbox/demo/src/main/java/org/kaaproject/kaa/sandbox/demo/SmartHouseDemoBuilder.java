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
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartHouseDemoBuilder extends AbstractDemoBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartHouseDemoBuilder.class);
    
    protected SmartHouseDemoBuilder() {
        super();
    }
    
    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {
        
        logger.info("Loading 'Smart House Demo Application' data...");
        
        loginTenantAdmin(client);
        
        EventClassFamilyDto deviceEventClassFamily = new EventClassFamilyDto();
        deviceEventClassFamily.setName("Device Event Class Family");
        deviceEventClassFamily.setNamespace("org.kaaproject.kaa.demo.smarthouse.device");
        deviceEventClassFamily.setClassName("DeviceEventClassFamily");
        deviceEventClassFamily = client.editEventClassFamily(deviceEventClassFamily);
        client.addEventClassFamilySchema(deviceEventClassFamily.getId(), "demo/smarthouse/deviceEventClassFamily.json");

        EventClassFamilyDto thermoEventClassFamily = new EventClassFamilyDto();
        thermoEventClassFamily.setName("Thermo Event Class Family");
        thermoEventClassFamily.setNamespace("org.kaaproject.kaa.demo.smarthouse.thermo");
        thermoEventClassFamily.setClassName("ThermoEventClassFamily");
        thermoEventClassFamily = client.editEventClassFamily(thermoEventClassFamily);
        client.addEventClassFamilySchema(thermoEventClassFamily.getId(), "demo/smarthouse/thermoEventClassFamily.json");

        EventClassFamilyDto musicEventClassFamily = new EventClassFamilyDto();
        musicEventClassFamily.setName("Music Event Class Family");
        musicEventClassFamily.setNamespace("org.kaaproject.kaa.demo.smarthouse.music");
        musicEventClassFamily.setClassName("MusicEventClassFamily");
        musicEventClassFamily = client.editEventClassFamily(musicEventClassFamily);
        client.addEventClassFamilySchema(musicEventClassFamily.getId(), "demo/smarthouse/musicEventClassFamily.json");
        
        ApplicationDto smartHouseApplication = new ApplicationDto();
        smartHouseApplication.setName("Smart House");
        smartHouseApplication = client.editApplication(smartHouseApplication);
        sdkKey.setApplicationId(smartHouseApplication.getId());
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setConfigurationSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setLogSchemaVersion(1);
        sdkKey.setTargetPlatform(SdkPlatform.ANDROID);
        
        loginTenantDeveloper(client);
        
        ApplicationEventFamilyMapDto deviceAefMap = mapEventClassFamily(client, smartHouseApplication, deviceEventClassFamily);
        ApplicationEventFamilyMapDto thermoAefMap = mapEventClassFamily(client, smartHouseApplication, thermoEventClassFamily);
        ApplicationEventFamilyMapDto musicAefMap = mapEventClassFamily(client, smartHouseApplication, musicEventClassFamily);

        List<String> aefMapIds = new ArrayList<>();
        aefMapIds.add(deviceAefMap.getId());
        aefMapIds.add(thermoAefMap.getId());
        aefMapIds.add(musicAefMap.getId());
        sdkKey.setAefMapIds(aefMapIds);
        logger.info("Finished loading 'Smart House Demo Application' data.");
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
    protected void setupProjectConfig() {
        projectConfig.setId("smarthouse_demo");
        projectConfig.setName("Smart House Demo");
        projectConfig.setDescription("Smart house application on android platform demonstrating event subsystem (IoT)");
        projectConfig.setPlatform(Platform.ANDROID);
        projectConfig.setSourceArchive("android/smarthouse_demo.tar.gz");
        projectConfig.setProjectFolder("smarthouse_demo/SmartHouseDemo");
        projectConfig.setSdkLibDir("smarthouse_demo/SmartHouseDemo/libs");
        projectConfig.setDestBinaryFile("smarthouse_demo/SmartHouseDemo/bin/SmartHouseDemo-debug.apk");
    }

}
