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

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftException;
import org.kaaproject.kaa.server.common.thrift.gen.control.Sdk;
import org.kaaproject.kaa.server.common.thrift.gen.control.SdkPlatform;

public class ControlServerSdkIT extends AbstractTestControlServer {

    /**
     * Test generate java SDK.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateJavaSdk() throws TException, IOException {
        ApplicationDto application = createApplication();
        ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        ConfigurationSchemaDto configSchema = createConfigurationSchema(application.getId());
        NotificationSchemaDto notificationSchema = createUserNotificationSchema(application.getId());
        Sdk sdk = client.generateSdk(SdkPlatform.JAVA, application.getId(), profileSchema.getMajorVersion(), configSchema.getMajorVersion(), notificationSchema.getMajorVersion());
        Assert.assertNotNull(sdk);
        Assert.assertFalse(strIsEmpty(sdk.getFileName()));
        Assert.assertNotNull(sdk.getData());
    }

    /**
     * Test generate java SDK with invalid application.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test(expected = ControlThriftException.class)
    public void testGenerateJavaSdkWithInvalidApplication() throws TException, IOException {
        client.generateSdk(SdkPlatform.JAVA, "123", 1, 1, 1);
    }

    /**
     * Test generate java SDK with invalid profile schema.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test(expected = ControlThriftException.class)
    public void testGenerateJavaSdkWithInvalidProfileSchema() throws TException, IOException {
        ApplicationDto application = createApplication();
        client.generateSdk(SdkPlatform.JAVA, application.getId(), 2, 2, 2);
    }

    /**
     * Test generate java SDK with invalid configuration schema.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test(expected = ControlThriftException.class)
    public void testGenerateJavaSdkWithInvalidConfigurationSchema() throws TException, IOException {
        ApplicationDto application = createApplication();
        ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        client.generateSdk(SdkPlatform.JAVA, application.getId(), profileSchema.getMajorVersion(), 2, 2);
    }

    /**
     * Test generate java SDK with invalid notification schema.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test(expected = ControlThriftException.class)
    public void testGenerateJavaSdkWithInvalidNotificationSchema() throws TException, IOException {
        ApplicationDto application = createApplication();
        ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
        ConfigurationSchemaDto configSchema = createConfigurationSchema(application.getId());
        client.generateSdk(SdkPlatform.JAVA, application.getId(), profileSchema.getMajorVersion(), configSchema.getMajorVersion(), 2);
    }

}
