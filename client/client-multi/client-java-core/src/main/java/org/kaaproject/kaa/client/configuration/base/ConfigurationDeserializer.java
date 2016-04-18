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

package org.kaaproject.kaa.client.configuration.base;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.Generated;

import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.schema.base.Configuration;

/**
 * This class deserialize binary data to configuration object.
 * 
 * This implementation is auto-generated. Please modify corresponding template
 * file.
 * 
 * @author Andrew Shvayka
 *
 */
@Generated("ConfigurationDeserializer.java.template")
class ConfigurationDeserializer {

    private final AvroByteArrayConverter<Configuration> converter = new AvroByteArrayConverter<Configuration>(Configuration.class);
    private final ExecutorContext executorContext;

    public ConfigurationDeserializer(ExecutorContext executorContext) {
        this.executorContext = executorContext;
    }

    void notify(Collection<ConfigurationListener> listeners, byte[] configurationData) throws IOException {
        final Configuration configuration = fromByteArray(configurationData);
        for (final ConfigurationListener listener : listeners) {
            executorContext.getCallbackExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    listener.onConfigurationUpdate(configuration);
                }
            });
        }
    }

    Configuration fromByteArray(byte[] data) throws IOException {
        return converter.fromByteArray(data);
    }
}
