/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.control.service.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.control.service.sdk.event.EventFamilyMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SdkGenerator.
 */
public abstract class SdkGenerator {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SdkGenerator.class);

    /**
     * Generate sdk.
     *
     * @param buildVersion the build version
     * @param bootstrapNodes the bootstrap nodes
     * @param sdkProfile the sdk profile
     * @param profileSchemaBody the profile schema body
     * @param notificationSchemaBody the notification schema body
     * @param configurationProtocolSchemaBody the configuration protocol schema body
     * @param configurationBaseSchemaBody the configuration base schema body
     * @param defaultConfigurationData the default configuration data
     * @param eventFamilies the event families meta information
     * @param logSchemaBody the log schema body
     *
     * @return the sdk
     * @throws Exception the exception
     */
    public abstract FileData generateSdk(String buildVersion,
            List<BootstrapNodeInfo> bootstrapNodes,
            SdkProfileDto sdkProfile,
            String profileSchemaBody,
            String notificationSchemaBody,
            String configurationProtocolSchemaBody,
            String configurationBaseSchemaBody,
            byte[] defaultConfigurationData,
            List<EventFamilyMetadata> eventFamilies,
            String logSchemaBody) throws Exception; //NOSONAR

    /**
     * Read file.
     *
     * @param file the file
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static String readFile(File file) throws IOException {
        String result = null;
        try {
            StringBuffer fileData = new StringBuffer();
            FileInputStream input = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            result = fileData.toString();
        } catch (IOException e) {
            LOG.error("Unable to read from specified file '"
                    + file + "'! Error: " + e.getMessage(), e);
            throw e;
        }
        return result;
    }

    /**
     * Read resource.
     *
     * @param resource the resource
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String readResource(String resource) throws IOException {
        String result = null;
        try {
            StringBuffer fileData = new StringBuffer();
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            result = fileData.toString();
        } catch (IOException e) {
            LOG.error("Unable to read from specified resource '"
                    + resource + "'! Error: " + e.getMessage(), e);
            throw e;
        }
        return result;
    }
}
