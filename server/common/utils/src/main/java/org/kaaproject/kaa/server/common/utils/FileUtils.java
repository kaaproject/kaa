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

package org.kaaproject.kaa.server.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
    }

    /**
     * Read resource.
     *
     * @param resource the resource
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String readResource(String resource) throws IOException{
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
    
    /**
     * Read resource bytes.
     *
     * @param resource the resource
     * @return the byte array
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static byte[] readResourceBytes(String resource) throws IOException {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            return IOUtils.toByteArray(input);
        } catch (IOException e) {
            LOG.error("Unable to read from specified resource '"
                    + resource + "'! Error: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Read resource peroperties.
     *
     * @param resource the resource
     * @return the properties
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Properties readResourceProperties(String resource) throws IOException{
        Properties result = null;
        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            result = new Properties();
            result.load(input);
            input.close();
        } catch (IOException e) {
            LOG.error("Unable to read from specified resource '"
                    + resource + "'! Error: " + e.getMessage(), e);
            throw e;
        }
        return result;
    }
}
