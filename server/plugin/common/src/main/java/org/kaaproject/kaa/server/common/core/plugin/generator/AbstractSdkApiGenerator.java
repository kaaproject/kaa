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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSdkApiGenerator<T extends SpecificRecordBase> implements PluginSdkApiGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSdkApiGenerator.class);

    public abstract Class<T> getConfigurationClass();

    @Override
    public PluginSDKApiBundle generatePluginSdkApi(PluginSdkApiGenerationContext context) throws SdkApiGenerationException {
        AvroByteArrayConverter<T> converter = new AvroByteArrayConverter<>(this.getConfigurationClass());
        try {
            T config = converter.fromByteArray(context.getPluginConfigurationData());
            LOG.info("Initializing transport {} with {}", this.getClassName(), config);
            return this.generatePluginSdkApi(new SpecificPluginSdkApiGenerationContext<T>(context, config));
        } catch (IOException cause) {
            LOG.error(MessageFormat.format("Failed to initialize transport {0}", this.getClassName()), cause);
            throw new SdkApiGenerationException(cause);
        }
    }

    protected abstract PluginSDKApiBundle generatePluginSdkApi(SpecificPluginSdkApiGenerationContext<T> context);

    protected String readFileAsString(String fileName) {
        String fileContent = null;
        URL url = this.getClass().getClassLoader().getResource(fileName);
        if (url != null) {
            try {
                Path path = Paths.get(url.toURI());
                byte[] bytes = Files.readAllBytes(path);
                if (bytes != null) {
                    fileContent = new String(bytes);
                }
            } catch (Exception cause) {
                cause.printStackTrace();
            }
        }
        return fileContent;
    }

    private String getClassName() {
        return this.getClass().getName();
    }
}
