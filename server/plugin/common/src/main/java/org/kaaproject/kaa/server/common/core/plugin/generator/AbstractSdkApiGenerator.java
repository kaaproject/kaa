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
import java.text.MessageFormat;
import java.util.List;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSdkApiGenerator<T extends SpecificRecordBase> implements PluginSdkApiGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSdkApiGenerator.class);

    @Override
    public List<SdkApiFile> generatePluginSdkApi(PluginSdkApiGenerationContext context) throws SdkApiGenerationException {
        AvroByteArrayConverter<T> converter = new AvroByteArrayConverter<>(getConfigurationClass());
        try {
            T config = converter.fromByteArray(context.getPluginConfigurationData().getBytes());
            LOG.info("Initializing transport {} with {}", getClassName(), config);
            return generatePluginSdkApi(new SpecificPluginSdkApiGenerationContext<>(context, config));
        } catch (IOException e) {
            LOG.error(MessageFormat.format("Failed to initialize transport {0}", getClassName()), e);
            throw new SdkApiGenerationException(e);
        }

    }

    protected abstract List<SdkApiFile> generatePluginSdkApi(SpecificPluginSdkApiGenerationContext<T> context);

    /**
     * Gets the configuration class.
     *
     * @return the configuration class
     */
    public abstract Class<T> getConfigurationClass();

    private String getClassName() {
        return this.getClass().getName();
    }
}
