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
package org.kaaproject.kaa.client.configuration.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.configuration.ConfigurationHashContainer;
import org.kaaproject.kaa.client.configuration.ConfigurationProcessor;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationStorage;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConfigurationManager implements ConfigurationManager {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractConfigurationManager.class);

    private final Set<ConfigurationListener> listeners = new HashSet<ConfigurationListener>();
    private final KaaClientProperties properties;
    protected final ConfigurationDeserializer deserializer = new ConfigurationDeserializer();

    private byte[] configurationData;
    private ConfigurationStorage storage;
    private ConfigurationHashContainer container;

    public AbstractConfigurationManager(KaaClientProperties properties) {
        super();
        this.properties = properties;
        container = new HashContainer();
    }
    
    @Override
    public void init() {
        getConfigurationData();
    }

    @Override
    public boolean addListener(ConfigurationListener listener) {
        if (listener != null) {
            LOG.trace("Adding listener {}", listener);
            synchronized (listeners) {
                return listeners.add(listener);
            }
        } else {
            throw new RuntimeException("Can't add null as a listener");
        }
    }

    @Override
    public boolean removeListener(ConfigurationListener listener) {
        synchronized (listeners) {
            return listeners.remove(listener);
        }
    }

    @Override
    public ConfigurationProcessor getConfigurationProcessor() {
        return new ConfigurationProcessor() {

            @Override
            public void processConfigurationData(ByteBuffer buffer, boolean fullResync) throws IOException {
                if (fullResync) {
                    configurationData = toByteArray(buffer);
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Received configuration data {}", Arrays.toString(configurationData));
                    }
                    if (storage != null) {
                        LOG.debug("Persisting configuration data from storage {}", storage);
                        storage.saveConfiguration(ByteBuffer.wrap(configurationData));
                        LOG.debug("Persisted configuration data from storage {}", storage);
                    }
                    for (ConfigurationListener listener : listeners) {
                        deserializer.notify(listener, configurationData);
                    }
                } else {
                    LOG.warn("Only full resync delta is supported!");
                }
            }
        };
    }

    @Override
    public void setConfigurationStorage(ConfigurationStorage storage) {
        this.storage = storage;
    }

    @Override
    public ConfigurationHashContainer getConfigurationHashContainer() {
        return container;
    }

    protected byte[] getConfigurationData() {
        if (configurationData == null) {
            configurationData = loadConfigurationData();
        }
        return configurationData;
    }

    private byte[] loadConfigurationData() {
        if (storage != null) {
            LOG.debug("Loading configuration data from storage {}", storage);
            configurationData = toByteArray(storage.loadConfiguration());
            if (configurationData == null) {
                LOG.debug("Loading configuration data from defaults {}", storage);
                configurationData = getDefaultConfigurationData();
            }
        } else {
            LOG.debug("Loading configuration data from defaults");
            configurationData = getDefaultConfigurationData();
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Loaded configuration data {}", Arrays.toString(configurationData));
        }
        return configurationData;
    }

    protected byte[] getDefaultConfigurationData() {
        return properties.getDefaultConfigData();
    }

    private static byte[] toByteArray(ByteBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        byte[] b = new byte[buffer.remaining()];
        buffer.get(b);
        return b;
    }

    private class HashContainer implements ConfigurationHashContainer {
        @Override
        public EndpointObjectHash getConfigurationHash() {
            return EndpointObjectHash.fromSHA1(getConfigurationData());
        }
    }
}
