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

public class EventDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(EventDemoBuilder.class);

    protected EventDemoBuilder() {
        super("demo/event");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Event Demo Application' data...");

        loginTenantAdmin(client);

        EventClassFamilyDto thermoEventClassFamily = new EventClassFamilyDto();
        thermoEventClassFamily.setName("Thermostat Event Class Family");
        thermoEventClassFamily.setNamespace("org.kaaproject.kaa.schema.sample.event.thermo");
        thermoEventClassFamily.setClassName("ThermostatEventClassFamily");
        thermoEventClassFamily = client.editEventClassFamily(thermoEventClassFamily);
        client.addEventClassFamilySchema(thermoEventClassFamily.getId(), getResourcePath("thermostatEventClassFamily.json"));

        ApplicationDto eventApplication = new ApplicationDto();
        eventApplication.setName("Event demo");
        eventApplication = client.editApplication(eventApplication);

        sdkPropertiesDto.setApplicationId(eventApplication.getId());
        sdkPropertiesDto.setProfileSchemaVersion(1);
        sdkPropertiesDto.setConfigurationSchemaVersion(1);
        sdkPropertiesDto.setNotificationSchemaVersion(1);
        sdkPropertiesDto.setLogSchemaVersion(1);

        loginTenantDeveloper(client);

        ApplicationEventFamilyMapDto thermoAefMap = mapEventClassFamily(client, eventApplication, thermoEventClassFamily);

        List<String> aefMapIds = new ArrayList<>();
        aefMapIds.add(thermoAefMap.getId());
        sdkPropertiesDto.setAefMapIds(aefMapIds);

        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(eventApplication.getId());
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

        logger.info("Finished loading 'Event Demo Application' data.");
    }


}
