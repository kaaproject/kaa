package org.kaaproject.kaa.sandbox.demo;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
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

public class NotificationDemoBuilder extends AbstractDemoBuilder{

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        loginTenantAdmin(client);

        ApplicationDto notificationApplication = new ApplicationDto();
        notificationApplication.setName("Android notification");
        notificationApplication = client.editApplication(notificationApplication);

        sdkKey.setApplicationId(notificationApplication.getId());
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setConfigurationSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setLogSchemaVersion(1);
        sdkKey.setTargetPlatform(SdkPlatform.ANDROID);


        NotificationSchemaDto notificationSchemaDto = new NotificationSchemaDto();
        notificationSchemaDto.setApplicationId(notificationApplication.getId());
        notificationSchemaDto.setName("Notification schema");
        notificationSchemaDto.setDescription("Notification schema describing incoming notifications");
        notificationSchemaDto = client.createNotificationSchema(notificationSchemaDto, "demo/notification/notificationSchema.json");
        sdkKey.setNotificationSchemaVersion(notificationSchemaDto.getMajorVersion());


        loginTenantDeveloper(client);

        TrustfulVerifierConfig trustfulVerifierConfig = new TrustfulVerifierConfig();
        UserVerifierDto trustfulUserVerifier = new UserVerifierDto();
        trustfulUserVerifier.setApplicationId(notificationApplication.getId());
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

    }

    @Override
    protected void setupProjectConfigs() {
        Project projectConfig = new Project();
        projectConfig.setId("notification_demo");
        projectConfig.setName("Android Notification Demo");
        projectConfig.setDescription("Application on android platform demonstrating notification subsystem (IoT)");
        projectConfig.setPlatform(Platform.ANDROID);
        projectConfig.setSourceArchive("android/notification_demo.tar.gz");
        projectConfig.setProjectFolder("notification_demo/NotificationDemo");
        projectConfig.setSdkLibDir("notification_demo/NotificationDemo/libs");
        projectConfig.setDestBinaryFile("notification_demo/NotificationDemo/bin/Notification-debug.apk");
        projectConfigs.add(projectConfig);
    }
}
