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

package org.kaaproject.kaa.server.common.verifier;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKaaUserVerifier<T extends SpecificRecordBase> implements UserVerifier {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractKaaUserVerifier.class);

    @Override
    public void init(UserVerifierContext context) throws UserVerifierLifecycleException{
        LOG.info("Initializing user verifier with {}", context);
        AvroByteArrayConverter<T> converter = new AvroByteArrayConverter<>(getConfigurationClass());
        try {
            T configuration = converter.fromByteArray(context.getVerifierDto().getRawConfiguration());
            LOG.info("Initializing user verifier {} with {}", getClassName(), configuration);
            init(context, configuration);
        } catch (IOException e) {
            LOG.error(MessageFormat.format("Failed to initialize user verifier {0}", getClassName()), e);
            throw new UserVerifierLifecycleException(e);
        }
    }
    
    public abstract void init(UserVerifierContext context, T configuration) throws UserVerifierLifecycleException;
    
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
