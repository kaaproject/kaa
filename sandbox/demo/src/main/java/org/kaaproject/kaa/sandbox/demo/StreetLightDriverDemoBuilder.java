package org.kaaproject.kaa.sandbox.demo;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StreetLightDriverDemoBuilder extends AbstractDemoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(StreetLightDriverDemoBuilder.class);

    private static final int LIGHT_ZONE_COUNT = 4;

    protected StreetLightDriverDemoBuilder() {
        super("demo/streetlight");
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Street lights driver application' data...");

        loginTenantAdmin(client);

        ApplicationDto streetLightApplication = new ApplicationDto();
        streetLightApplication.setName("Street light driver");
        streetLightApplication = client.editApplication(streetLightApplication);

        sdkKey.setApplicationId(streetLightApplication.getId());
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setLogSchemaVersion(1);
        sdkKey.setConfigurationSchemaVersion(1);

        loginTenantDeveloper(client);

        logger.info("Creating profile schema...");
        ProfileSchemaDto profileSchemaDto = new ProfileSchemaDto();
        profileSchemaDto.setApplicationId(streetLightApplication.getId());
        profileSchemaDto.setName("StreetLightsDriverProfile schema");
        profileSchemaDto.setDescription("Street light driver profile schema");
        profileSchemaDto = client.createProfileSchema(profileSchemaDto, getResourcePath("profile.avsc"));
        logger.info("Profile schema version: {}", profileSchemaDto.getMajorVersion());
        sdkKey.setProfileSchemaVersion(profileSchemaDto.getMajorVersion());
        logger.info("Profile schema was created.");

        logger.info("Creating configuration schema...");
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(streetLightApplication.getId());
        configurationSchema.setName("StreetLightsConfiguration schema");
        configurationSchema.setDescription("Street Light configuration schema");
        configurationSchema = client.createConfigurationSchema(configurationSchema, getResourcePath("configuration.avsc"));
        logger.info("Configuration schema version: {}", configurationSchema.getMajorVersion());
        sdkKey.setConfigurationSchemaVersion(configurationSchema.getMajorVersion());
        logger.info("Configuration schema was created");

        for (int i = 0; i < LIGHT_ZONE_COUNT; ++i) {
            EndpointGroupDto group = new EndpointGroupDto();
            group.setApplicationId(streetLightApplication.getId());
            group.setName("Zone " + Integer.toString(i));
            group.setWeight(i + 1);
            logger.info("Creating Endpoint group for Light Zone {}", i);
            group = client.editEndpointGroup(group);
            logger.info("Created Endpoint group for Light Zone {}", i);

            ProfileFilterDto filter = new ProfileFilterDto();
            filter.setApplicationId(streetLightApplication.getId());
            filter.setEndpointGroupId(group.getId());
            filter.setSchemaId(profileSchemaDto.getId());
            filter.setMajorVersion(profileSchemaDto.getMajorVersion());
            filter.setBody("lightZones.contains(new Integer(" + Integer.toString(i) + "))");
            filter.setStatus(UpdateStatus.INACTIVE);
            logger.info("Creating Profile filter for Light Zone {}", i);
            filter = client.editProfileFilter(filter);
            logger.info("Activating Profile filter for Light Zone {}", i);
            client.activateProfileFilter(filter.getId());
            logger.info("Created and activated Profile filter for Light Zone {}", i);

            ConfigurationDto baseGroupConfiguration = new ConfigurationDto();
            baseGroupConfiguration.setApplicationId(streetLightApplication.getId());
            baseGroupConfiguration.setEndpointGroupId(group.getId());
            baseGroupConfiguration.setSchemaId(configurationSchema.getId());
            baseGroupConfiguration.setMajorVersion(configurationSchema.getMajorVersion());
            baseGroupConfiguration.setDescription("Base street light driver configuration");
            String body = getConfigurationBodyForEndpointGroup(i);
            logger.info("Configuration body: [{}]", body);
            baseGroupConfiguration.setBody(body);
            logger.info("Editing the configuration...");
            baseGroupConfiguration = client.editConfiguration(baseGroupConfiguration);
            logger.info("Configuration was successfully edited");
            logger.info("Activating the configuration");
            client.activateConfiguration(baseGroupConfiguration.getId());
            logger.info("Configuration was activated");
        }

        logger.info("Finished loading 'Street lights driver application' data...");
    }

    private String getConfigurationBodyForEndpointGroup(int zoneId) {
        return "{\n" +
                "  \"lightZones\": \n" +
                "    { \"array\": \n" +
                "      [\n" +
                "        {\"zoneId\":\n" +
                "          { \"int\": " + zoneId + " },\n" +
                "          \"zoneStatus\": \n" +
                "            { \"org.kaaproject.kaa.demo.iotworld.lights.street.ZoneStatus\" :\"DISABLE\"},\n" +
                "          \"__uuid\": null\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "  \"__uuid\": null\n" +
                "}\n";
    }

}
