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

package org.kaaproject.kaa.server.transport;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.common.zk.gen.VersionConnectionInfoPair;
import org.kaaproject.kaa.server.transport.message.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Responsible for a classpath scan and initialization of {@link Transport} implementations.
 * Provides a capability to add/remove listeners on the transports start-up events.
 *
 * @author Andrew Shvayka
 */
public abstract class AbstractTransportService implements TransportService {
  protected static final String TRANSPORT_CONFIGURATION_SCAN_PACKAGE =
      "org.kaaproject.kaa.server.transport";

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTransportService.class);

  private final Map<Integer, TransportConfig> configs;
  private final Map<Integer, Transport> transports;
  private final Set<TransportUpdateListener> listeners;

  /**
   * Create new instance of <code>AbstractTransportService</code>.
   */
  public AbstractTransportService() {
    super();
    this.configs = new HashMap<Integer, TransportConfig>();
    this.transports = new HashMap<Integer, Transport>();
    this.listeners = new HashSet<TransportUpdateListener>();
  }

  private static List<org.kaaproject.kaa.server.common.zk.gen.TransportMetaData> toTransportMdList(
      Map<Integer, Transport> transportMap) {
    List<org.kaaproject.kaa.server.common.zk.gen.TransportMetaData> mdList =
        new ArrayList<>(transportMap.size());
    for (Entry<Integer, Transport> entry : transportMap.entrySet()) {
      TransportMetaData source = entry.getValue().getConnectionInfo();
      org.kaaproject.kaa.server.common.zk.gen.TransportMetaData md =
          new org.kaaproject.kaa.server.common.zk.gen.TransportMetaData();
      md.setId(entry.getKey());
      md.setMinSupportedVersion(source.getMinSupportedVersion());
      md.setMaxSupportedVersion(source.getMaxSupportedVersion());
      List<VersionConnectionInfoPair> connectionInfoList = new ArrayList<>();
      for (int i = md.getMinSupportedVersion(); i <= md.getMaxSupportedVersion(); i++) {
        for (byte[] connectionInfo : source.getConnectionInfoList(i)) {
          connectionInfoList.add(new VersionConnectionInfoPair(i, ByteBuffer.wrap(connectionInfo)));
        }
      }
      md.setConnectionInfo(connectionInfoList);
      mdList.add(md);
    }
    return mdList;
  }

  @Override
  public void lookupAndInit() {
    LOG.info("Lookup of available transport configurations started in package {}.",
        TRANSPORT_CONFIGURATION_SCAN_PACKAGE);
    configs.clear();
    transports.clear();
    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(KaaTransportConfig.class));
    Set<BeanDefinition> beans = scanner.findCandidateComponents(
        TRANSPORT_CONFIGURATION_SCAN_PACKAGE);
    for (BeanDefinition bean : beans) {
      LOG.info("Found transport configuration {}", bean.getBeanClassName());
      try {
        Class<?> clazz = Class.forName(bean.getBeanClassName());
        TransportConfig transportConfig = (TransportConfig) clazz.newInstance();
        configs.put(transportConfig.getId(), transportConfig);
      } catch (ReflectiveOperationException exception) {
        LOG.error(MessageFormat.format("Failed to init transport configuration for {0}",
            bean.getBeanClassName()), exception);
      }
    }
    LOG.info("Lookup of available transport configurations found {} configurations.",
        configs.size());

    LOG.info("Lookup of transport properties started");
    TransportProperties transportProperties = new TransportProperties(getServiceProperties());
    LOG.info("Lookup of transport properties found {} properties", transportProperties.size());

    for (TransportConfig config : configs.values()) {
      LOG.info("Initializing transport with name {} and class {}",
          config.getName(), config.getTransportClass());
      try {
        Class<?> clazz = Class.forName(config.getTransportClass());
        Transport transport = (Transport) clazz.newInstance();
        String transportConfigFile = getTransportConfigPrefix() + "-" + config.getConfigFileName();
        LOG.info("Lookup of transport configuration file {}", transportConfigFile);
        URL configFileUrl = this.getClass().getClassLoader().getResource(transportConfigFile);
        GenericAvroConverter<GenericRecord> configConverter =
            new GenericAvroConverter<>(config.getConfigSchema());
        GenericRecord configRecord = configConverter.decodeJson(
            Files.readAllBytes(Paths.get(configFileUrl.toURI())));
        LOG.info("Lookup of transport configuration file {}", transportConfigFile);
        TransportContext context = new TransportContext(
            transportProperties, getPublicKey(), getMessageHandler());
        transport.init(new GenericTransportContext(context, configConverter.encode(configRecord)));
        transports.put(config.getId(), transport);
      } catch (ReflectiveOperationException | IOException
          | URISyntaxException | TransportLifecycleException exception) {
        LOG.error(MessageFormat.format("Failed to init transport for {0}",
            config.getTransportClass()), exception);
      }
    }
  }

  @Override
  public void start() {
    LOG.info("Starting {} available transports.", transports.size());
    for (Entry<Integer, Transport> entry : transports.entrySet()) {
      LOG.info("Starting transport {}.", configs.get(entry.getKey()).getName());
      entry.getValue().start();
    }
    notifyListeners();
  }

  @Override
  public void stop() {
    LOG.info("Stoping {} available transports.", transports.size());
    for (Entry<Integer, Transport> entry : transports.entrySet()) {
      LOG.info("Stoping transport {}.", configs.get(entry.getKey()).getName());
      entry.getValue().stop();
    }
  }

  @Override
  public boolean addListener(TransportUpdateListener listener) {
    LOG.info("Adding transport update listener {}.", listener);
    return listeners.add(listener);
  }

  @Override
  public boolean removeListener(TransportUpdateListener listener) {
    LOG.info("Removing transport update listener {}.", listener);
    return listeners.remove(listener);
  }

  protected abstract String getTransportConfigPrefix();

  protected abstract Properties getServiceProperties();

  protected abstract MessageHandler getMessageHandler();

  protected abstract PublicKey getPublicKey();

  private void notifyListeners() {
    List<org.kaaproject.kaa.server.common.zk.gen.TransportMetaData> mdList = toTransportMdList(
        transports);
    for (TransportUpdateListener listener : listeners) {
      listener.onTransportsStarted(mdList);
    }
  }

}
