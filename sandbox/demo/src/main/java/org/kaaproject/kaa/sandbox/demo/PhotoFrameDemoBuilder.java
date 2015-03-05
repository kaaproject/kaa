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

public class PhotoFrameDemoBuilder extends AbstractDemoBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(PhotoFrameDemoBuilder.class);
    
    protected PhotoFrameDemoBuilder() {
        super();
    }
    
    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {
        
        logger.info("Loading 'Photo Frame Demo Application' data...");
        
        loginTenantAdmin(client);
        
        EventClassFamilyDto photoFrameEventClassFamily = new EventClassFamilyDto();
        photoFrameEventClassFamily.setName("Photo Frame Event Class Family");
        photoFrameEventClassFamily.setNamespace("org.kaaproject.kaa.demo.photoframe");
        photoFrameEventClassFamily.setClassName("PhotoFrameEventClassFamily");
        photoFrameEventClassFamily = client.editEventClassFamily(photoFrameEventClassFamily);
        client.addEventClassFamilySchema(photoFrameEventClassFamily.getId(), "demo/photoframe/photoFrameEventClassFamily.json");
        
        ApplicationDto photoFrameApplication = new ApplicationDto();
        photoFrameApplication.setName("Photo frame");
        photoFrameApplication = client.editApplication(photoFrameApplication);
               
        sdkKey.setApplicationId(photoFrameApplication.getId());
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setConfigurationSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setLogSchemaVersion(1);
        sdkKey.setTargetPlatform(SdkPlatform.ANDROID);
        
        loginTenantDeveloper(client);
        
        ApplicationEventFamilyMapDto photoFrameAefMap = mapEventClassFamily(client, photoFrameApplication, photoFrameEventClassFamily);

        List<String> aefMapIds = new ArrayList<>();
        aefMapIds.add(photoFrameAefMap.getId());
        sdkKey.setAefMapIds(aefMapIds);
        
        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();        
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(photoFrameApplication.getId());
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
        
        logger.info("Finished loading 'Photo Frame Demo Application' data.");
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
        projectConfig.setId("photoframe_demo");
        projectConfig.setName("Photo frame");
        projectConfig.setDescription("Photo frame application on android platform demonstrating event API");
        projectConfig.setPlatform(Platform.ANDROID);
        projectConfig.setSourceArchive("android/photoframe_demo.tar.gz");
        projectConfig.setProjectFolder("photoframe_demo/PhotoFrame");
        projectConfig.setSdkLibDir("photoframe_demo/PhotoFrame/libs");
        projectConfig.setDestBinaryFile("photoframe_demo/PhotoFrame/bin/PhotoFrame-debug.apk");
        projectConfigs.add(projectConfig);
    }

}
