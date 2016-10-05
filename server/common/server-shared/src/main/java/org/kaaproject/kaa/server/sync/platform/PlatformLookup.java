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

package org.kaaproject.kaa.server.sync.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides ability to lookup and init {@link PlatformEncDec} instances.
 *
 * @author Andrew Shvayka
 */
public class PlatformLookup {
  public static final String DEFAULT_PROTOCOL_LOOKUP_PACKAGE_NAME = "org.kaaproject.kaa.server";
  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PlatformLookup.class);

  private PlatformLookup() {
  }

  /**
   * Lookup for platform protocols, return <code>Set</code> of string. These strings are name of
   * classes that annotated by <code>@KaaPlatformProtocol</code>
   *
   * @param packageNames is packages for scan
   * @return <code>Set</code> of string
   */
  public static Set<String> lookupPlatformProtocols(String... packageNames) {
    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(KaaPlatformProtocol.class));
    Set<BeanDefinition> beans = new HashSet<>();
    for (String packageName : packageNames) {
      beans.addAll(scanner.findCandidateComponents(packageName));
    }
    Set<String> protocols = new HashSet<>();
    for (BeanDefinition bean : beans) {
      protocols.add(bean.getBeanClassName());
    }
    return protocols;
  }

  /**
   * Method takes <code>Set</code> of protocols class name and return <code>Map</code> where key is
   * id of protocol and value is the protocol.
   *
   * @param platformProtocols is <code>Set</code> of platform protocols (strings)
   * @return map where key is id of protocol and value is the protocol
   */
  public static Map<Integer, PlatformEncDec> initPlatformProtocolMap(
      Set<String> platformProtocols) {
    Map<Integer, PlatformEncDec> platformEncDecMap = new HashMap<>();
    for (String platformProtocol : platformProtocols) {
      try {
        Class<?> clazz = Class.forName(platformProtocol);
        PlatformEncDec protocol = (PlatformEncDec) clazz.newInstance();
        platformEncDecMap.put(protocol.getId(), protocol);
        LOG.info("Successfully initialized platform protocol {}", platformProtocol);
      } catch (ReflectiveOperationException exception) {
        LOG.error("Error during instantiation of platform protocol", exception);
      }
    }
    return platformEncDecMap;
  }
}
