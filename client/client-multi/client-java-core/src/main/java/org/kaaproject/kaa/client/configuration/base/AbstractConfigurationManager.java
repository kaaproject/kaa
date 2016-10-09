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

import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.configuration.ConfigurationHashContainer;
import org.kaaproject.kaa.client.configuration.ConfigurationProcessor;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationStorage;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractConfigurationManager implements ConfigurationManager {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractConfigurationManager.class);
  protected final ConfigurationDeserializer deserializer;
  private final Set<ConfigurationListener> listeners = Collections.newSetFromMap(
          new ConcurrentHashMap<ConfigurationListener, Boolean>());
  private final KaaClientProperties properties;
  private final ExecutorContext executorContext;
  private volatile byte[] configurationData;
  private ConfigurationStorage storage;
  private ConfigurationHashContainer container = new HashContainer();
  private KaaClientState state;

  /**
   * All-args constructor.
   */
  public AbstractConfigurationManager(KaaClientProperties properties, KaaClientState state,
                                      ExecutorContext executorContext) {
    super();
    this.properties = properties;
    this.state = state;
    this.executorContext = executorContext;
    this.deserializer = new ConfigurationDeserializer(executorContext);
  }

  private static byte[] toByteArray(ByteBuffer buffer) {
    if (buffer == null) {
      return null;
    }
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    return bytes;
  }

  @Override
  public void init() {
    getConfigurationData();
  }

  @Override
  public boolean addListener(ConfigurationListener listener) {
    if (listener != null) {
      LOG.trace("Adding listener {}", listener);
      return listeners.add(listener);
    } else {
      throw new RuntimeException("Can't add null as a listener");
    }
  }

  @Override
  public boolean removeListener(ConfigurationListener listener) {
    if (listener != null) {
      return listeners.remove(listener);
    } else {
      throw new RuntimeException("Can't remove null listener");
    }
  }

  @Override
  public ConfigurationProcessor getConfigurationProcessor() {
    return new ConfigurationProcessor() {

      @Override
      public void processConfigurationData(ByteBuffer buffer, boolean fullResync)
              throws IOException {
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
          deserializer.notify(Collections.unmodifiableCollection(listeners), configurationData);
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
      if (state.isConfigurationVersionUpdated()) {
        LOG.info("Clearing old configuration data from storage {}", storage);
        try {
          storage.clearConfiguration();
        } catch (IOException ex) {
          LOG.error("Failed to clear configuration from storage", ex);
        }
      } else {
        LOG.debug("Loading configuration data from storage {}", storage);
        try {
          configurationData = toByteArray(storage.loadConfiguration());
        } catch (IOException ex) {
          LOG.error("Failed to load configuration from storage", ex);
        }
      }
    }
    if (configurationData == null) {
      LOG.debug("Loading configuration data from defaults {}", storage);
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

  private class HashContainer implements ConfigurationHashContainer {
    @Override
    public EndpointObjectHash getConfigurationHash() {
      return EndpointObjectHash.fromSha1(getConfigurationData());
    }
  }
}
