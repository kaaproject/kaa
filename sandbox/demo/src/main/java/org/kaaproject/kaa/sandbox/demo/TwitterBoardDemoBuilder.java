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

import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class TwitterBoardDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(TwitterBoardDemoBuilder.class);

    private static final String KAA_CLIENT_CONFIG = "kaaClientConfiguration";
    private static final String TOPIC_NAME = "topicName";
    private static final String LED_BOARD_TOPIC = "Led board topic";

    public TwitterBoardDemoBuilder() {
        super("demo/twitterboard");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {
        logger.info("Loading 'Twitter board application' data...");

        loginTenantAdmin(client);

        ApplicationDto twitterBoardApplication = new ApplicationDto();
        twitterBoardApplication.setName("Twitter board");
        twitterBoardApplication = client.editApplication(twitterBoardApplication);

        sdkKey.setApplicationId(twitterBoardApplication.getId());
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setLogSchemaVersion(1);

        loginTenantDeveloper(client);

        logger.info("Creating configuration schema...");
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(twitterBoardApplication.getId());
        configurationSchema.setName("TwitterBoard schema");
        configurationSchema.setDescription("Default configuration schema for the twitter board application");
        configurationSchema = client.createConfigurationSchema(configurationSchema, getResourcePath("config_schema.avsc"));
        logger.info("Configuration schema version: {}", configurationSchema.getMajorVersion());
        sdkKey.setConfigurationSchemaVersion(configurationSchema.getMajorVersion());
        logger.info("Configuration schema was created.");

        logger.info("Creating notification schema...");
        NotificationSchemaDto notificationSchemaDto = new NotificationSchemaDto();
        notificationSchemaDto.setApplicationId(twitterBoardApplication.getId());
        notificationSchemaDto.setName("Twitter board notification schema");
        notificationSchemaDto.setDescription("Notification schema for Twitter board application");
        notificationSchemaDto = client.createNotificationSchema(notificationSchemaDto, getResourcePath("notification_schema.avsc"));
        sdkKey.setNotificationSchemaVersion(notificationSchemaDto.getMajorVersion());
        logger.info("Notification schema was created.");

        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroups(twitterBoardApplication.getId());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }

        if (baseEndpointGroup == null) {
            throw new RuntimeException("Can't get default endpoint group twitter board application!");
        }

        TopicDto mandatoryTopic = new TopicDto();
        mandatoryTopic.setApplicationId(twitterBoardApplication.getId());
        String twitterMonitorBody = FileUtils.readResource(getResourcePath("TwitterMonitor/config_data.json"));
        logger.info("Configuration body of Twitter monitor: [{}]", twitterMonitorBody);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> configBody = objectMapper.readValue(twitterMonitorBody, Map.class);
        logger.info("Getting config body of Twitter monitor: [{}]", configBody);
        Map<String, Object> kaaClientConfig = (Map<String, Object>) configBody.get(KAA_CLIENT_CONFIG);
        logger.info("Getting kaaClientConfig of Twitter monitor: [{}]", kaaClientConfig);
        String topicName = (String) kaaClientConfig.get(TOPIC_NAME);
        logger.info("Got topic name: {}", topicName);
        mandatoryTopic.setName(topicName);
        mandatoryTopic.setType(TopicTypeDto.MANDATORY);
        mandatoryTopic.setDescription("Twitter led topic");
        logger.info("Creating mandatory topic: {}", mandatoryTopic);
        mandatoryTopic = client.createTopic(mandatoryTopic);
        client.addTopicToEndpointGroup(baseEndpointGroup, mandatoryTopic);
        logger.info("Mandatory topic {} was created", mandatoryTopic);

        ConfigurationDto baseConfiguration = new ConfigurationDto();
        baseConfiguration.setApplicationId(twitterBoardApplication.getId());
        baseConfiguration.setEndpointGroupId(baseEndpointGroup.getId());
        baseConfiguration.setSchemaId(configurationSchema.getId());
        baseConfiguration.setMajorVersion(configurationSchema.getMajorVersion());
        baseConfiguration.setMinorVersion(configurationSchema.getMinorVersion());
        baseConfiguration.setDescription("Base twitter board configuration");
        String body = FileUtils.readResource(getResourcePath("config_data.json"));
        logger.info("Configuration body: [{}]", body);
        baseConfiguration.setBody(body);
        baseConfiguration.setStatus(UpdateStatus.INACTIVE);
        logger.info("Editing the configuration...");
        baseConfiguration = client.editConfiguration(baseConfiguration);
        logger.info("Configuration was successfully edited");
        logger.info("Activating the configuration");
        client.activateConfiguration(baseConfiguration.getId());
        logger.info("Configuration was activated");

        logger.info("Finished loading 'Twitter board application' data...");
    }
}
