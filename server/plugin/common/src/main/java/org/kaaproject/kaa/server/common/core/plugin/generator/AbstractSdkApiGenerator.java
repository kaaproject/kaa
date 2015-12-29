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

package org.kaaproject.kaa.server.common.core.plugin.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSdkApiGenerator<T extends SpecificRecordBase> implements PluginSdkApiGenerator {

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractSdkApiGenerator.class);

    public abstract Class<T> getConfigurationClass();

    @Override
    public PluginSDKApiBundle generatePluginSdkApi(PluginSdkApiGenerationContext context) throws SdkApiGenerationException {
        AvroByteArrayConverter<T> converter = new AvroByteArrayConverter<>(this.getConfigurationClass());
        try {
            T config = converter.fromByteArray(context.getPluginConfigurationData().getBytes());
            LOG.info("Initializing transport {} with {}", this.getClassName(), config);
            return this.generatePluginSdkApi(new SpecificPluginSdkApiGenerationContext<T>(context, config));
        } catch (IOException cause) {
            LOG.error(MessageFormat.format("Failed to initialize transport {0}", this.getClassName()), cause);
            throw new SdkApiGenerationException(cause);
        }
    }

    protected abstract PluginSDKApiBundle generatePluginSdkApi(SpecificPluginSdkApiGenerationContext<T> context);

    protected String readFileAsString(String fileName) {
        String result = null;
        try {
            StringBuffer fileData = new StringBuffer();
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
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
            LOG.error("Unable to read from specified resource '" + fileName + "'! Error: " + e.getMessage(), e);
        }
        return result;
    }

    private String getClassName() {
        return this.getClass().getName();
    }
}
