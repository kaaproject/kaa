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


import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.event.*;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.kaaproject.kaa.server.verifiers.trustful.config.TrustfulVerifierConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JEventDemoBuilder extends  AbstractDemoBuilder{

    private static final Logger logger = LoggerFactory.getLogger(JEventDemoBuilder.class);

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Java Event Demo Application' data...");

        loginTenantAdmin(client);

        EventClassFamilyDto deviceEventClassFamily = new EventClassFamilyDto();
        deviceEventClassFamily.setName("Device Event Class Family");
        deviceEventClassFamily.setNamespace("org.kaaproject.kaa.demo.jevent.device");
        deviceEventClassFamily.setClassName("DeviceEventClassFamily");
        deviceEventClassFamily = client.editEventClassFamily(deviceEventClassFamily);
        client.addEventClassFamilySchema(deviceEventClassFamily.getId(), "demo/jevent/deviceEventClassFamily.json");

        EventClassFamilyDto thermoEventClassFamily = new EventClassFamilyDto();
        thermoEventClassFamily.setName("Thermo Event Class Family");
        thermoEventClassFamily.setNamespace("org.kaaproject.kaa.demo.jevent.thermo");
        thermoEventClassFamily.setClassName("ThermoEventClassFamily");
        thermoEventClassFamily = client.editEventClassFamily(thermoEventClassFamily);
        client.addEventClassFamilySchema(thermoEventClassFamily.getId(), "demo/jevent/thermoEventClassFamily.json");

        EventClassFamilyDto musicEventClassFamily = new EventClassFamilyDto();
        musicEventClassFamily.setName("Music Event Class Family");
        musicEventClassFamily.setNamespace("org.kaaproject.kaa.demo.jevent.music");
        musicEventClassFamily.setClassName("MusicEventClassFamily");
        musicEventClassFamily = client.editEventClassFamily(musicEventClassFamily);
        client.addEventClassFamilySchema(musicEventClassFamily.getId(), "demo/jevent/musicEventClassFamily.json");

        ApplicationDto jeventApplication = new ApplicationDto();
        jeventApplication.setName("Java Event Demo");
        jeventApplication = client.editApplication(jeventApplication);

        sdkKey.setApplicationId(jeventApplication.getId());
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setConfigurationSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setLogSchemaVersion(1);
        sdkKey.setTargetPlatform(SdkPlatform.JAVA);

        loginTenantDeveloper(client);

        ApplicationEventFamilyMapDto deviceAefMap = mapEventClassFamily(client, jeventApplication, deviceEventClassFamily);
        ApplicationEventFamilyMapDto thermoAefMap = mapEventClassFamily(client, jeventApplication, thermoEventClassFamily);
        ApplicationEventFamilyMapDto musicAefMap = mapEventClassFamily(client, jeventApplication, musicEventClassFamily);

        List<String> aefMapIds = new ArrayList<>();
        aefMapIds.add(deviceAefMap.getId());
        aefMapIds.add(thermoAefMap.getId());
        aefMapIds.add(musicAefMap.getId());
        sdkKey.setAefMapIds(aefMapIds);

        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(jeventApplication.getId());
        trustfulUserVerifier.setName("Trustful verifier");
        trustfulUserVerifier.setPluginClassName(trustfulVerifierConfig.getPluginClassName());
        trustfulUserVerifier.setPluginTypeName(trustfulVerifierConfig.getPluginTypeName());
        RawSchema rawSchema = new RawSchema(trustfulVerifierConfig.getPluginConfigSchema().toString());
        DefaultRecordGenerationAlgorithm<RawData> algotithm =
                new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
        RawData rawData = algotithm.getRootData();
        trustfulUserVerifier.setJsonConfiguration(rawData.getRawData());
        trustfulUserVerifier = client.editUserVerifierDto(trustfulUserVerifier);
        sdkKey.setDefaultVerifierToken(trustfulUserVerifier.getVerifierToken());

        logger.info("Finished loading 'Java Event Demo Application' data.");
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
        projectConfig.setId("jevent_demo");
        projectConfig.setName("Java Event Demo");
        projectConfig.setDescription("Application on java platform demonstrating event subsystem (IoT)");
        projectConfig.setPlatform(Platform.JAVA);
        projectConfig.setSourceArchive("java/jevent_demo.tar.gz");
        projectConfig.setProjectFolder("jevent_demo/JEventDemo");
        projectConfig.setSdkLibDir("jevent_demo/JEventDemo/lib");
        projectConfig.setDestBinaryFile("jevent_demo/JEventDemo/bin/EventDemo.jar");
        projectConfigs.add(projectConfig);
    }
}
