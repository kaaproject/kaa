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

package org.kaaproject.kaa.server.operations.service.event;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RouteTable {

  private final Map<RouteTableKey, Map<String, RouteTableAddress>> routes;
  private final Map<RouteTableAddress, Set<String>> reportedAddressMap;
  private final Set<String> remoteServersSet;
  private final Map<RouteTableAddress, Set<RouteTableKey>> localAddressMap;

  /**
   * Instantiates new route table.
   */
  public RouteTable() {
    super();
    routes = new HashMap<RouteTableKey, Map<String, RouteTableAddress>>();
    reportedAddressMap = new HashMap<>();
    remoteServersSet = new HashSet<>();
    localAddressMap = new HashMap<>();
  }

  /**
   * Add route to table.
   *
   * @param key the key of RouteTable associative array
   * @param address the value of RouteTable associative array
   */
  public void add(RouteTableKey key, RouteTableAddress address) {
    Map<String, RouteTableAddress> directionRoutes = routes.get(key);
    if (directionRoutes == null) {
      directionRoutes = new HashMap<>();
      routes.put(key, directionRoutes);
    }
    directionRoutes.put(Base64Util.encode(address.getEndpointKey().getData()), address);

    if (address.isLocal()) {
      Set<RouteTableKey> routeKeys = localAddressMap.get(address);
      if (routeKeys == null) {
        routeKeys = new HashSet<>();
        localAddressMap.put(address, routeKeys);
      }
      routeKeys.add(key);
    } else {
      remoteServersSet.add(address.getServerId());
    }
  }

  /**
   * Get route from table.
   *
   * @param key the key of RouteTable associative array
   * @param target the specific target
   * @return collection of addresses
   */
  public Collection<RouteTableAddress> getRoutes(RouteTableKey key, String target) {
    Map<String, RouteTableAddress> directionRoutes = routes.get(key);
    if (directionRoutes != null) {
      if (target == null) {
        return directionRoutes.values();
      } else {
        RouteTableAddress address = directionRoutes.get(target);
        if (address != null) {
          return Collections.singletonList(address);
        }
      }
    }
    return Collections.emptyList();
  }

  /**
   * Get routes from table by key set.
   *
   * @param keys the keys
   * @param target the specific target
   * @return set of addresses
   */
  public Set<RouteTableAddress> getRoutes(Set<RouteTableKey> keys, String target) {
    Set<RouteTableAddress> result = new HashSet<>();
    for (RouteTableKey key : keys) {
      result.addAll(getRoutes(key, target));
    }
    return result;
  }

  public Set<RouteTableAddress> getAllLocalRoutes() {
    return Collections.unmodifiableSet(localAddressMap.keySet());
  }

  /**
   * Returns local route table keys.
   *
   * @param localAddress local address
   * @return             local route table keys
   */
  public Set<RouteTableKey> getLocalRouteTableKeys(RouteTableAddress localAddress) {
    Set<RouteTableKey> keys = localAddressMap.get(localAddress);
    if (keys != null) {
      return keys;
    } else {
      return Collections.emptySet();
    }
  }

  /**
   * Clear remote server data.
   *
   * @param serverId server identifier
   */
  public void clearRemoteServerData(String serverId) {
    remoteServersSet.remove(serverId);
    clearReportedAddressMap(serverId);
    clearRoutes(serverId);
  }

  /**
   * Returns a remote server list.
   *
   * @return remote server list
   */
  public Set<String> getRemoteServers() {
    return Collections.unmodifiableSet(remoteServersSet);
  }

  /**
   * Register a route info report.
   *
   * @param localAddresses local address
   * @param serverId       server identifier
   */
  public void registerRouteInfoReport(Set<RouteTableAddress> localAddresses, String serverId) {
    for (RouteTableAddress address : localAddresses) {
      Set<String> serverIds = reportedAddressMap.get(address);
      if (serverIds == null) {
        serverIds = new HashSet<>();
        reportedAddressMap.put(address, serverIds);
      }
      serverIds.add(serverId);
    }
  }

  /**
   * Returns whether delivery is required.
   *
   * @param serverId server identifier
   * @param address  address
   * @return         true if delivery is required otherwise false
   */
  public boolean isDeliveryRequired(String serverId, RouteTableAddress address) {
    Set<String> servers = reportedAddressMap.get(address);
    return servers == null || !servers.contains(serverId);
  }

  /**
   * Remove a local address.
   *
   * @param endpoint endpoint object hash
   * @return         removed address
   */
  public RouteTableAddress removeLocal(EndpointObjectHash endpoint) {
    clearRoutes(endpoint);
    RouteTableAddress addressToRemove = null;
    for (RouteTableAddress address : localAddressMap.keySet()) {
      if (address.isLocal() && endpoint.equals(address.getEndpointKey())) {
        addressToRemove = address;
        break;
      }
    }
    if (addressToRemove != null) {
      reportedAddressMap.remove(addressToRemove);
      localAddressMap.remove(addressToRemove);
    }

    return addressToRemove;
  }

  private void clearRoutes(String serverId) {
    Set<Entry<RouteTableKey, Map<String, RouteTableAddress>>> entrySet = routes.entrySet();
    Iterator<Entry<RouteTableKey, Map<String, RouteTableAddress>>> iterator = entrySet.iterator();
    while (iterator.hasNext()) {
      Entry<RouteTableKey, Map<String, RouteTableAddress>> entry = iterator.next();
      Map<String, RouteTableAddress> addressMap = entry.getValue();
      Set<Entry<String, RouteTableAddress>> innerEntrySet = addressMap.entrySet();
      Iterator<Entry<String, RouteTableAddress>> innerIterator = innerEntrySet.iterator();
      while (innerIterator.hasNext()) {
        Entry<String, RouteTableAddress> innerEntry = innerIterator.next();
        if (serverId.equals(innerEntry.getValue().getServerId())) {
          innerIterator.remove();
        }
      }
      if (addressMap.isEmpty()) {
        iterator.remove();
      }
    }
  }

  /**
   * Clear routes for a specified endpoint.
   *
   * @param endpoint endpoint object hash
   */
  private void clearRoutes(EndpointObjectHash endpoint) {
    Set<Entry<RouteTableKey, Map<String, RouteTableAddress>>> entrySet = routes.entrySet();
    Iterator<Entry<RouteTableKey, Map<String, RouteTableAddress>>> iterator = entrySet.iterator();
    while (iterator.hasNext()) {
      Entry<RouteTableKey, Map<String, RouteTableAddress>> entry = iterator.next();
      Map<String, RouteTableAddress> addressMap = entry.getValue();
      Set<Entry<String, RouteTableAddress>> innerEntrySet = addressMap.entrySet();
      Iterator<Entry<String, RouteTableAddress>> innerIterator = innerEntrySet.iterator();
      while (innerIterator.hasNext()) {
        Entry<String, RouteTableAddress> innerEntry = innerIterator.next();
        if (endpoint.equals(innerEntry.getValue().getEndpointKey())) {
          innerIterator.remove();
        }
      }
      if (addressMap.isEmpty()) {
        iterator.remove();
      }
    }
  }

  private void clearReportedAddressMap(String serverId) {
    Set<Entry<RouteTableAddress, Set<String>>> entrySet = reportedAddressMap.entrySet();
    Iterator<Entry<RouteTableAddress, Set<String>>> iterator = entrySet.iterator();
    while (iterator.hasNext()) {
      Entry<RouteTableAddress, Set<String>> entry = iterator.next();
      Set<String> servers = entry.getValue();
      servers.remove(serverId);
      if (servers.isEmpty()) {
        iterator.remove();
      }
    }
  }

  /**
   * Remove a route table address.
   *
   * @param address address
   */
  public void removeByAddress(RouteTableAddress address) {
    Set<Entry<RouteTableKey, Map<String, RouteTableAddress>>> entrySet = routes.entrySet();
    Iterator<Entry<RouteTableKey, Map<String, RouteTableAddress>>> iterator = entrySet.iterator();
    while (iterator.hasNext()) {
      Entry<RouteTableKey, Map<String, RouteTableAddress>> entry = iterator.next();
      Map<String, RouteTableAddress> addressMap = entry.getValue();
      Set<Entry<String, RouteTableAddress>> innerEntrySet = addressMap.entrySet();
      Iterator<Entry<String, RouteTableAddress>> innerIterator = innerEntrySet.iterator();
      while (innerIterator.hasNext()) {
        Entry<String, RouteTableAddress> innerEntry = innerIterator.next();
        if (address.equals(innerEntry.getValue())) {
          innerIterator.remove();
        }
      }
      if (addressMap.isEmpty()) {
        iterator.remove();
      }
    }
  }

}
