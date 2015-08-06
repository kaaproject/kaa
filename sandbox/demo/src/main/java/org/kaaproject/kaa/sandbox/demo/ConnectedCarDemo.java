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
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.kaaproject.kaa.server.verifiers.trustful.config.TrustfulVerifierConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectedCarDemo extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ConnectedCarDemo.class);

    protected ConnectedCarDemo() {
        super("demo/connectedcar");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Connected Car Demo Application' data...");

        loginTenantAdmin(client);

        /*
         * Configure the event feature.
         */
        EventClassFamilyDto geoFencingEventClassFamily = client.getEventClassFamily("GeoFencingEventClassFamily");

        ApplicationDto connectedCarApplication = new ApplicationDto();
        connectedCarApplication.setName("Connected Car demo");
        connectedCarApplication = client.editApplication(connectedCarApplication);

        sdkPropertiesDto.setApplicationId(connectedCarApplication.getId());
        sdkPropertiesDto.setProfileSchemaVersion(1);
        sdkPropertiesDto.setNotificationSchemaVersion(1);

        loginTenantDeveloper(client);

        ApplicationEventFamilyMapDto thermoAefMap = mapEventClassFamily(client, connectedCarApplication, geoFencingEventClassFamily);

        List<String> aefMapIds = new ArrayList<>();
        aefMapIds.add(thermoAefMap.getId());
        sdkPropertiesDto.setAefMapIds(aefMapIds);

        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(connectedCarApplication.getId());
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

        /*
         * Configure the logging feature.
         */

        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setApplicationId(connectedCarApplication.getId());
        logSchemaDto.setName("Connected Car log schema");
        logSchemaDto.setDescription("Log schema describes incoming RFID reports");
        logSchemaDto = client.createLogSchema(logSchemaDto, getResourcePath("logSchema.json"));
        sdkPropertiesDto.setLogSchemaVersion(logSchemaDto.getMajorVersion());

        LogAppenderDto connectedCarLogAppender = new LogAppenderDto();
        connectedCarLogAppender.setName("Connected Car log appender");
        connectedCarLogAppender.setDescription("Log appender used to deliver RFID reports from connected car application to the REST server");
        connectedCarLogAppender.setApplicationId(connectedCarApplication.getId());
        connectedCarLogAppender.setApplicationToken(connectedCarApplication.getApplicationToken());
        connectedCarLogAppender.setTenantId(connectedCarApplication.getTenantId());
        connectedCarLogAppender.setMinLogSchemaVersion(1);
        connectedCarLogAppender.setMaxLogSchemaVersion(Integer.MAX_VALUE);
        connectedCarLogAppender.setConfirmDelivery(true);
        connectedCarLogAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.KEYHASH,
                LogHeaderStructureDto.TIMESTAMP, LogHeaderStructureDto.TOKEN, LogHeaderStructureDto.VERSION));
        connectedCarLogAppender.setPluginTypeName("REST");
        connectedCarLogAppender.setPluginClassName("org.kaaproject.kaa.server.appenders.rest.appender.RestLogAppender");
        connectedCarLogAppender.setJsonConfiguration(FileUtils.readResource(getResourcePath("restAppender.json")));
        connectedCarLogAppender = client.editLogAppenderDto(connectedCarLogAppender);

        /*
         * Configure the configuration feature.
         */

        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(connectedCarApplication.getId());
        configurationSchema.setName("Connected car configuration schema");
        configurationSchema.setDescription("Default configuration schema for the connected car application");
        configurationSchema = client.createConfigurationSchema(configurationSchema, getResourcePath("configurationSchema.json"));

        sdkPropertiesDto.setConfigurationSchemaVersion(configurationSchema.getMajorVersion());

        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroups(connectedCarApplication.getId());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }

        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group for the connected car application!");
        }

        ConfigurationDto baseConfiguration = new ConfigurationDto();
        baseConfiguration.setApplicationId(connectedCarApplication.getId());
        baseConfiguration.setEndpointGroupId(baseEndpointGroup.getId());
        baseConfiguration.setSchemaId(configurationSchema.getId());
        baseConfiguration.setMajorVersion(configurationSchema.getMajorVersion());
        baseConfiguration.setMinorVersion(configurationSchema.getMinorVersion());
        baseConfiguration.setDescription("Base configuration for the connected car demo ");
        String body = FileUtils.readResource(getResourcePath("configurationBody.json"));
        baseConfiguration.setBody(body);
        baseConfiguration.setStatus(UpdateStatus.INACTIVE);
        baseConfiguration = client.editConfiguration(baseConfiguration);
        client.activateConfiguration(baseConfiguration.getId());

        logger.info("Finished loading 'Connected Car Demo Application' data.");
    }


}
