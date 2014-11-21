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
package org.kaaproject.kaa.server.common.log.shared.appender;

import java.io.IOException;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.avro.CustomAppenderParametersDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CustomLogAppender<T extends SpecificRecordBase> extends AbstractLogAppender {

    private static final Logger LOG = LoggerFactory.getLogger(CustomLogAppender.class);
    
    private Class<T> configurationClass;
    
    public CustomLogAppender(Class<T> configurationClass) {
        this.configurationClass = configurationClass;
    }
    
    @Override
    public void initLogAppender(LogAppenderDto appender) {
        CustomAppenderParametersDto customParameters = 
                (CustomAppenderParametersDto)appender.getProperties().getParameters();
        
        byte[] rawConfiguration = customParameters.getRawConfiguration();
        try {
            AvroByteArrayConverter<T> converter = new AvroByteArrayConverter<>(configurationClass);
            T configuration = converter.fromByteArray(rawConfiguration);
            initFromConfiguration(appender, configuration);
        } catch (IOException e) {
            LOG.error("Unable to parse configuration for appender '" + getName() + "'", e);
        }
    }
    
    protected abstract void initFromConfiguration(LogAppenderDto appender, T configuration);
    
}
