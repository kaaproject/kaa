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
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.kaaproject.kaa.server.verifiers.trustful.config.TrustfulVerifierConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartHouseDemoBuilder extends AbstractDemoBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartHouseDemoBuilder.class);
    
    protected SmartHouseDemoBuilder() {
        super("demo/smarthouse");
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
        client.addEventClassFamilySchema(deviceEventClassFamily.getId(), getResourcePath("deviceEventClassFamily.json"));

        EventClassFamilyDto thermoEventClassFamily = new EventClassFamilyDto();
        thermoEventClassFamily.setName("Thermo Event Class Family");
        thermoEventClassFamily.setNamespace("org.kaaproject.kaa.demo.smarthouse.thermo");
        thermoEventClassFamily.setClassName("ThermoEventClassFamily");
        thermoEventClassFamily = client.editEventClassFamily(thermoEventClassFamily);
        client.addEventClassFamilySchema(thermoEventClassFamily.getId(), getResourcePath("thermoEventClassFamily.json"));

        EventClassFamilyDto musicEventClassFamily = new EventClassFamilyDto();
        musicEventClassFamily.setName("Music Event Class Family");
        musicEventClassFamily.setNamespace("org.kaaproject.kaa.demo.smarthouse.music");
        musicEventClassFamily.setClassName("MusicEventClassFamily");
        musicEventClassFamily = client.editEventClassFamily(musicEventClassFamily);
        client.addEventClassFamilySchema(musicEventClassFamily.getId(), getResourcePath("musicEventClassFamily.json"));
        
        ApplicationDto smartHouseApplication = new ApplicationDto();
        smartHouseApplication.setName("Smart house");
        smartHouseApplication = client.editApplication(smartHouseApplication);
               
        sdkPropertiesDto.setApplicationId(smartHouseApplication.getId());
        sdkPropertiesDto.setApplicationToken(smartHouseApplication.getApplicationToken());
        sdkPropertiesDto.setProfileSchemaVersion(1);
        sdkPropertiesDto.setConfigurationSchemaVersion(1);
        sdkPropertiesDto.setNotificationSchemaVersion(1);
        sdkPropertiesDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);
        
        ApplicationEventFamilyMapDto deviceAefMap = mapEventClassFamily(client, smartHouseApplication, deviceEventClassFamily);
        ApplicationEventFamilyMapDto thermoAefMap = mapEventClassFamily(client, smartHouseApplication, thermoEventClassFamily);
        ApplicationEventFamilyMapDto musicAefMap = mapEventClassFamily(client, smartHouseApplication, musicEventClassFamily);

        List<String> aefMapIds = new ArrayList<>();
        aefMapIds.add(deviceAefMap.getId());
        aefMapIds.add(thermoAefMap.getId());
        aefMapIds.add(musicAefMap.getId());
        sdkPropertiesDto.setAefMapIds(aefMapIds);
        
        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();        
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(smartHouseApplication.getId());
        trustfulUserVerifier.setName("Trustful verifier");
        trustfulUserVerifier.setPluginClassName(trustfulVerifierConfig.getPluginClassName());
        trustfulUserVerifier.setPluginTypeName(trustfulVerifierConfig.getPluginTypeName());
        RawSchema rawSchema = new RawSchema(trustfulVerifierConfig.getPluginConfigSchema().toString());
        DefaultRecordGenerationAlgorithm<RawData> algotithm = 
                    new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
        RawData rawData = algotithm.getRootData();
        trustfulUserVerifier.setJsonConfiguration(rawData.getRawData());        
        trustfulUserVerifier = client.editUserVerifierDto(trustfulUserVerifier);
        sdkPropertiesDto.setDefaultVerifierToken(trustfulUserVerifier.getVerifierToken());
        
        logger.info("Finished loading 'Smart House Demo Application' data.");
    }

}
