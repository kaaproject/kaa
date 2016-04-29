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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.user;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConfigurationUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConfigurationUpdate;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserConfigurationUpdate;
import org.kaaproject.kaa.server.operations.service.event.EventService;
import org.kaaproject.kaa.server.operations.service.event.GlobalRouteInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorContext;

public class GlobalUserActorMessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalUserActor.class);

    /** The event service. */
    private final EventService eventService;
    private final String userId;
    private final String tenantId;
    private final GlobalRouteTable<ConfigurationKey> map;
    private final Map<ConfigurationKey, byte[]> ucfHashes;

    public GlobalUserActorMessageProcessor(AkkaContext context, String userId, String tenantId) {
        this.eventService = context.getEventService();
        this.userId = userId;
        this.tenantId = tenantId;
        this.map = new GlobalRouteTable<>();
        this.ucfHashes = new HashMap<>();
    }

    public void process(ActorContext context, GlobalRouteInfo route) {
        if (route.getRouteOperation() == RouteOperation.ADD) {
            LOG.debug("[{}][{}] Adding route {} for cf version {}", tenantId, userId, route, route.getCfVersion());
            ConfigurationKey key = ConfigurationKey.fromRouteInfo(route);
            map.add(key, route);
            checkHashAndSendNotification(context, key, route);
        } else if (route.getRouteOperation() == RouteOperation.DELETE) {
            LOG.debug("[{}][{}] Remove route {} for cf version {}", tenantId, userId, route, route.getCfVersion());
            map.remove(route);
        } else {
            LOG.warn("[{}][{}] unsupported route operations {}", tenantId, userId, route.getRouteOperation());
        }
    }

    public void process(ActorContext context, UserConfigurationUpdate update) {
        LOG.debug("Processing notification {}", update);
        ConfigurationKey key = ConfigurationKey.fromUpdateMessage(update);
        ucfHashes.put(key, update.getHash());
        sendStateUpdatesToLocalServers(context, key, update);
        sendStateUpdatesToRemoteServers(context, key, update);
    }

    private void sendStateUpdatesToLocalServers(ActorContext context, ConfigurationKey key, UserConfigurationUpdate update) {
        for (GlobalRouteInfo route : map.getLocalRoutes(key)) {
            checkHashAndSendNotification(context, update.getHash(), route);
        }
    }

    private void sendStateUpdatesToRemoteServers(ActorContext context, ConfigurationKey key, UserConfigurationUpdate update) {
        Map<String, Set<GlobalRouteInfo>> routes = map.getRemoteRoutes(key);
        for (Entry<String, Set<GlobalRouteInfo>> entry : routes.entrySet()) {
            LOG.debug("Sending notification to {} about configuration update", entry.getKey());
            for (GlobalRouteInfo route : entry.getValue()) {
                checkHashAndSendNotification(context, update.getHash(), route);
            }
        }
    }

    private void checkHashAndSendNotification(ActorContext context, ConfigurationKey key, GlobalRouteInfo route) {
        byte[] currentUcfHash = ucfHashes.get(key);
        if (currentUcfHash != null) {
            checkHashAndSendNotification(context, currentUcfHash, route);
        } else {
            LOG.trace("No updates for key {} yet", key);
        }
    }

    private void checkHashAndSendNotification(ActorContext context, byte[] newHash, GlobalRouteInfo route) {
        if (!Arrays.equals(newHash, route.getUcfHash())) {
            LOG.trace("Sending notification to route {}", route);
            if (route.isLocal()) {
                context.parent().tell(new EndpointUserConfigurationUpdateMessage(toUpdate(newHash, route)), context.self());
            } else {
                eventService.sendEndpointStateInfo(route.getAddress().getServerId(), toUpdate(newHash, route));
            }
        } else {
            LOG.trace("Ignoring notification to route {} due to matching hashes", route);
        }
    }

    private EndpointUserConfigurationUpdate toUpdate(byte[] newHash, GlobalRouteInfo route) {
        return new EndpointUserConfigurationUpdate(tenantId, userId, route.getAddress().getApplicationToken(), route.getAddress()
                .getEndpointKey(), newHash);
    }

    private static class ConfigurationKey {
        private final int schemaVersion;
        private final String appToken;

        public static ConfigurationKey fromRouteInfo(GlobalRouteInfo route) {
            return new ConfigurationKey(route.getCfVersion(), route.getAddress().getApplicationToken());
        }

        public static ConfigurationKey fromUpdateMessage(UserConfigurationUpdate msg) {
            return new ConfigurationKey(msg.getSchemaVersion(), msg.getApplicationToken());
        }

        private ConfigurationKey(int schemaVersion, String appToken) {
            super();
            this.schemaVersion = schemaVersion;
            this.appToken = appToken;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((appToken == null) ? 0 : appToken.hashCode());
            result = prime * result + schemaVersion;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ConfigurationKey other = (ConfigurationKey) obj;
            if (appToken == null) {
                if (other.appToken != null) {
                    return false;
                }
            } else if (!appToken.equals(other.appToken)) {
                return false;
            }
            if (schemaVersion != other.schemaVersion) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ConfigurationKey [schemaVersion=");
            builder.append(schemaVersion);
            builder.append(", appToken=");
            builder.append(appToken);
            builder.append("]");
            return builder.toString();
        }
    }

    private static class GlobalRouteTable<T> {

        private final Map<T, Set<GlobalRouteInfo>> routes;

        public GlobalRouteTable() {
            routes = new HashMap<>();
        }

        public boolean add(T key, GlobalRouteInfo route) {
            Set<GlobalRouteInfo> keyRoutes = routes.get(key);
            if (keyRoutes == null) {
                keyRoutes = new HashSet<>();
                routes.put(key, keyRoutes);
            }
            return keyRoutes.add(route);
        }

        public boolean remove(GlobalRouteInfo route) {
            boolean found = false;
            for (T key : routes.keySet()) {
                found = found || routes.get(key).remove(route);
            }
            return found;
        }

        public Set<GlobalRouteInfo> getRoutes(T key) {
            return notNull(routes.get(key));
        }

        public Set<GlobalRouteInfo> getLocalRoutes(T key) {
            Set<GlobalRouteInfo> result = new HashSet<GlobalRouteInfo>();
            for (GlobalRouteInfo route : getRoutes(key)) {
                if (route.getAddress().getServerId() != null) {
                    continue;
                }
                result.add(route);
            }
            return notNull(result);
        }

        public Map<String, Set<GlobalRouteInfo>> getRemoteRoutes(T key) {
            Map<String, Set<GlobalRouteInfo>> result = new HashMap<String, Set<GlobalRouteInfo>>();
            for (GlobalRouteInfo route : getRoutes(key)) {
                String serverId = route.getAddress().getServerId();
                if (serverId == null) {
                    continue;
                }
                Set<GlobalRouteInfo> set = result.get(serverId);
                if (set == null) {
                    set = new HashSet<GlobalRouteInfo>();
                    result.put(serverId, set);
                }
                set.add(route);
            }
            return result;
        }

        public void clear() {
            routes.clear();
        }

        private static Set<GlobalRouteInfo> notNull(Set<GlobalRouteInfo> result) {
            if (result != null) {
                return result;
            } else {
                return Collections.emptySet();
            }
        }
    }

    public void processClusterUpdate(ActorContext context) {
        if (!eventService.isMainUserNode(userId)) {
            LOG.trace("No longer a global user node for user {}", userId);
            map.clear();
            context.stop(context.self());
        }
    }
}
