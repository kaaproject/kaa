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

package org.kaaproject.kaa.demo.iotworld.citylights.client;

import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.demo.iotworld.citylights.KaaClientConfiguration;
import org.kaaproject.kaa.server.common.admin.AdminClient;

public class KaaRestController {

    private final KaaClientConfiguration configuration;

    public KaaRestController(KaaClientConfiguration configuration) {
        this.configuration = configuration;
    }

    public void updateTrafficLights(boolean allow) throws Exception {
        AdminClient client = new AdminClient(configuration.getHost(), configuration.getPort());
        client.login(configuration.getLogin(), configuration.getPassword());
        ApplicationDto application = getApplication(client, configuration.getTrafficLightsAppToken());
        EndpointGroupDto groupAll = getGroupAll(client, application.getId());
        ConfigurationRecordDto currentConfiguration = getConfiguration(client, groupAll.getId(),
                configuration.getTrafficLightsCfSchemaVersion());
        ConfigurationDto activeConfiguration = currentConfiguration.getActiveConfiguration();

        ConfigurationDto newConfiguration = new ConfigurationDto();
        newConfiguration.setApplicationId(activeConfiguration.getApplicationId());
        newConfiguration.setEndpointGroupId(activeConfiguration.getEndpointGroupId());
        newConfiguration.setSchemaId(activeConfiguration.getSchemaId());
        newConfiguration.setMajorVersion(activeConfiguration.getMajorVersion());
        newConfiguration.setBody("{\"mainRoadState\" : \"" + (allow ? "ALLOW" : "DISALLOW") + "\", \"__uuid\":null}");
        newConfiguration = client.editConfiguration(newConfiguration);
        client.activateConfiguration(newConfiguration.getId());
    }

    public void updateStreetLights(int zoneId, boolean on) throws Exception {
        AdminClient client = new AdminClient(configuration.getHost(), configuration.getPort());
        client.login(configuration.getLogin(), configuration.getPassword());
        ApplicationDto application = getApplication(client, configuration.getStreetLightsAppToken());
        EndpointGroupDto group = getGroupByName(client, application.getId(), "Zone " + zoneId);
        ConfigurationRecordDto currentConfiguration = getConfiguration(client, group.getId(),
                configuration.getStreetLightsCfSchemaVersion());
        ConfigurationDto activeConfiguration = currentConfiguration.getActiveConfiguration();

        ConfigurationDto newConfiguration = new ConfigurationDto();
        newConfiguration.setApplicationId(activeConfiguration.getApplicationId());
        newConfiguration.setEndpointGroupId(activeConfiguration.getEndpointGroupId());
        newConfiguration.setSchemaId(activeConfiguration.getSchemaId());
        newConfiguration.setMajorVersion(activeConfiguration.getMajorVersion());
        newConfiguration.setBody("{\"lightZones\": { \"array\": [{\"zoneId\": { \"int\": " + zoneId + " }, \"zoneStatus\": { \"org.kaaproject.kaa.demo.iotworld.lights.street.ZoneStatus\" :\"" + (on ? "ENABLE" : "DISABLE") + "\"}, \"__uuid\": null}] }, \"__uuid\": null}");
        newConfiguration = client.editConfiguration(newConfiguration);
        client.activateConfiguration(newConfiguration.getId());
    }

    private ConfigurationRecordDto getConfiguration(AdminClient client, String endpointGroupId, int cfSchemaVersion) throws Exception {
        List<ConfigurationRecordDto> records = client.getConfigurationRecords(endpointGroupId, false);
        for (ConfigurationRecordDto dto : records) {
            if (dto.getActiveConfiguration().getMajorVersion() == cfSchemaVersion) {
                return dto;
            }
        }
        throw new IllegalStateException("Configuraton for schema version " + cfSchemaVersion + " not found!");
    }

    private ApplicationDto getApplication(AdminClient client, String trafficLightsAppToken) throws Exception {
        List<ApplicationDto> applications = client.getApplications();
        for (ApplicationDto dto : applications) {
            if (dto.getApplicationToken().equals(trafficLightsAppToken)) {
                return dto;
            }
        }
        throw new IllegalStateException("Application with token " + trafficLightsAppToken + " not found!");
    }

    private EndpointGroupDto getGroupAll(AdminClient client, String applicationId) throws Exception {
        List<EndpointGroupDto> groups = client.getEndpointGroups(applicationId);
        for (EndpointGroupDto dto : groups) {
            if (dto.getWeight() == 0) {
                return dto;
            }
        }
        throw new IllegalStateException("Endpoint group with weight 0 not found!");
    }

    private EndpointGroupDto getGroupByName(AdminClient client, String applicationId, String name) throws Exception {
        List<EndpointGroupDto> groups = client.getEndpointGroups(applicationId);
        for (EndpointGroupDto dto : groups) {
            if (dto.getName().equals(name)) {
                return dto;
            }
        }
        throw new IllegalStateException("Endpoint group with name " + name + "  not found!");
    }
}
