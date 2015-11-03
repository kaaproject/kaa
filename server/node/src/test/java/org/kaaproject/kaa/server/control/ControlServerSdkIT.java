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

package org.kaaproject.kaa.server.control;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;

public class ControlServerSdkIT extends AbstractTestControlServer {

    /**
     * Test generate java SDK.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateJavaSdk() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        ConfigurationSchemaDto configSchema = createConfigurationSchema(application.getId());
        NotificationSchemaDto notificationSchema = createUserNotificationSchema(application.getId());
        LogSchemaDto logSchema = createLogSchema(application.getId());

        FileData sdk = client.downloadSdk(getSdkProperties(SdkPlatform.JAVA, application.getId(),
                profileSchema.getMajorVersion(), configSchema.getMajorVersion(), notificationSchema.getMajorVersion(),
                null, logSchema.getMajorVersion(), null));
        Assert.assertNotNull(sdk);
        Assert.assertFalse(strIsEmpty(sdk.getFileName()));
        Assert.assertNotNull(sdk.getFileData());
        Assert.assertEquals(SdkPlatform.JAVA.getContentType(), sdk.getContentType());
    }

    /**
     * Test generate java SDK with event support.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateJavaSdkWithEventSupport() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        ConfigurationSchemaDto configSchema = createConfigurationSchema(application.getId());
        NotificationSchemaDto notificationSchema = createUserNotificationSchema(application.getId());

        LogSchemaDto logSchema = createLogSchema(application.getId());
        ApplicationEventFamilyMapDto aefMap = createApplicationEventFamilyMap(application.getId(), null, 1);
        List<String> aefMapIds = Collections.singletonList(aefMap.getId());

        FileData sdk = client.downloadSdk(getSdkProperties(SdkPlatform.JAVA,
                application.getId(),
                profileSchema.getMajorVersion(),
                configSchema.getMajorVersion(),
                notificationSchema.getMajorVersion(),
                aefMapIds, logSchema.getMajorVersion(), null));

        Assert.assertNotNull(sdk);
        Assert.assertFalse(strIsEmpty(sdk.getFileName()));
        Assert.assertNotNull(sdk.getFileData());
        Assert.assertEquals(SdkPlatform.JAVA.getContentType(), sdk.getContentType());
    }

    /**
     * Test generate android SDK with event support.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateAndroidSdkWithEventSupport() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        ConfigurationSchemaDto configSchema = createConfigurationSchema(application.getId());
        NotificationSchemaDto notificationSchema = createUserNotificationSchema(application.getId());
        LogSchemaDto logSchema = createLogSchema(application.getId());

        ApplicationEventFamilyMapDto aefMap = createApplicationEventFamilyMap(application.getId(), null, 1);
        List<String> aefMapIds = Collections.singletonList(aefMap.getId());

        FileData sdk = client.downloadSdk(getSdkProperties(SdkPlatform.ANDROID,
                application.getId(),
                profileSchema.getMajorVersion(),
                configSchema.getMajorVersion(),
                notificationSchema.getMajorVersion(),
                aefMapIds, logSchema.getMajorVersion(), null));

        Assert.assertNotNull(sdk);
        Assert.assertFalse(strIsEmpty(sdk.getFileName()));
        Assert.assertNotNull(sdk.getFileData());
        Assert.assertEquals(SdkPlatform.ANDROID.getContentType(), sdk.getContentType());
    }

    /**
     * Test generate Cpp SDK.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateCppSdk() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        ConfigurationSchemaDto configSchema = createConfigurationSchema(application.getId());
        NotificationSchemaDto notificationSchema = createUserNotificationSchema(application.getId());
        LogSchemaDto logSchema = createLogSchema(application.getId());

        FileData sdk = client.downloadSdk(getSdkProperties(SdkPlatform.CPP, application.getId(),
                profileSchema.getMajorVersion(),
                configSchema.getMajorVersion(),
                notificationSchema.getMajorVersion(),
                null, logSchema.getMajorVersion(), null));
        Assert.assertNotNull(sdk);
        Assert.assertFalse(strIsEmpty(sdk.getFileName()));
        Assert.assertNotNull(sdk.getFileData());
        Assert.assertEquals(SdkPlatform.CPP.getContentType(), sdk.getContentType());
    }

    /**
     * Test generate Cpp SDK with event support.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateCppSdkWithEventSupport() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        ConfigurationSchemaDto configSchema = createConfigurationSchema(application.getId());
        NotificationSchemaDto notificationSchema = createUserNotificationSchema(application.getId());
        LogSchemaDto logSchema = createLogSchema(application.getId());

        ApplicationEventFamilyMapDto aefMap = createApplicationEventFamilyMap(application.getId(), null, 1);
        List<String> aefMapIds = Collections.singletonList(aefMap.getId());

        FileData sdk = client.downloadSdk(getSdkProperties(SdkPlatform.CPP,
                application.getId(),
                profileSchema.getMajorVersion(),
                configSchema.getMajorVersion(),
                notificationSchema.getMajorVersion(),
                aefMapIds, logSchema.getMajorVersion(), null));

        Assert.assertNotNull(sdk);
        Assert.assertFalse(strIsEmpty(sdk.getFileName()));
        Assert.assertNotNull(sdk.getFileData());
        Assert.assertEquals(SdkPlatform.CPP.getContentType(), sdk.getContentType());
    }

    /**
     * Test generate C SDK.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateCSdk() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        ConfigurationSchemaDto configSchema = createConfigurationSchema(application.getId());
        NotificationSchemaDto notificationSchema = createUserNotificationSchema(application.getId());
        LogSchemaDto logSchema = createLogSchema(application.getId());

        FileData sdk = client.downloadSdk(getSdkProperties(SdkPlatform.C, application.getId(),
                profileSchema.getMajorVersion(),
                configSchema.getMajorVersion(),
                notificationSchema.getMajorVersion(),
                null, logSchema.getMajorVersion(), null));
        Assert.assertNotNull(sdk);
        Assert.assertFalse(strIsEmpty(sdk.getFileName()));
        Assert.assertNotNull(sdk.getFileData());
        Assert.assertEquals(SdkPlatform.C.getContentType(), sdk.getContentType());
    }

    /**
     * Test generate C SDK with event support.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateCSdkWithEventSupport() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        ConfigurationSchemaDto configSchema = createConfigurationSchema(application.getId());
        NotificationSchemaDto notificationSchema = createUserNotificationSchema(application.getId());
        LogSchemaDto logSchema = createLogSchema(application.getId());

        ApplicationEventFamilyMapDto aefMap = createApplicationEventFamilyMap(application.getId(), null, 1);
        List<String> aefMapIds = Collections.singletonList(aefMap.getId());

        FileData sdk = client.downloadSdk(getSdkProperties(SdkPlatform.C,
                application.getId(),
                profileSchema.getMajorVersion(),
                configSchema.getMajorVersion(),
                notificationSchema.getMajorVersion(),
                aefMapIds, logSchema.getMajorVersion(), null));

        Assert.assertNotNull(sdk);
        Assert.assertFalse(strIsEmpty(sdk.getFileName()));
        Assert.assertNotNull(sdk.getFileData());
        Assert.assertEquals(SdkPlatform.C.getContentType(), sdk.getContentType());
    }

    /**
     * Test generate java SDK with invalid application.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateJavaSdkWithInvalidApplication() throws Exception {
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.downloadSdk(getSdkProperties(SdkPlatform.JAVA, "123", 1, 1, 1, null, 0, null));
            }
        });
    }

    /**
     * Test generate java SDK with invalid profile schema.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateJavaSdkWithInvalidProfileSchema() throws Exception {
        final ApplicationDto application = createApplication(tenantAdminDto);
        loginTenantDeveloper(tenantDeveloperUser);
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.downloadSdk(getSdkProperties(SdkPlatform.JAVA, application.getId(), 2, 2, 2, null, 0, null));
            }
        });
    }

    /**
     * Test generate java SDK with invalid configuration schema.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateJavaSdkWithInvalidConfigurationSchema() throws Exception {
        final ApplicationDto application = createApplication(tenantAdminDto);
        loginTenantDeveloper(tenantDeveloperUser);
        final ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.downloadSdk(getSdkProperties(SdkPlatform.JAVA, application.getId(),
                        profileSchema.getMajorVersion(), 2, 2, null, 0, null));
            }
        });
    }

    /**
     * Test generate java SDK with invalid notification schema.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateJavaSdkWithInvalidNotificationSchema() throws Exception {
        final ApplicationDto application = createApplication(tenantAdminDto);
        loginTenantDeveloper(tenantDeveloperUser);
        final ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        final ConfigurationSchemaDto configSchema = createConfigurationSchema(application.getId());
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.downloadSdk(getSdkProperties(SdkPlatform.JAVA, application.getId(), profileSchema.getMajorVersion(),
                        configSchema.getMajorVersion(), 2, null, 0, null));
            }
        });
    }


    private SdkPropertiesDto getSdkProperties(org.kaaproject.kaa.common.dto.admin.SdkPlatform sdkPlatform, String appId,
                                        Integer profileSchemaVersion, Integer configSchemaVersion,
                                        Integer notificationSchemaVersion, List<String> aefMapIds,
                                        Integer logSchemaVersion, String defaultVerifierToken) {
        return new SdkPropertiesDto(appId, configSchemaVersion, profileSchemaVersion,
                notificationSchemaVersion, logSchemaVersion, sdkPlatform, aefMapIds, defaultVerifierToken, null);
    }


}
