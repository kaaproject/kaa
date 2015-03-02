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
package org.kaaproject.kaa.server.operations.service.akka.actors.core.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserConfigurationUpdate;
import org.kaaproject.kaa.server.operations.service.event.GlobalRouteInfo;
import org.kaaproject.kaa.server.operations.service.event.RouteOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalUserActorMessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalUserActor.class);

    private final String userId;
    private final String tenantId;
    private final GlobalRouteTable<ConfigurationKey> map;

    public GlobalUserActorMessageProcessor(AkkaContext context, String userId, String tenantId) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.map = new GlobalRouteTable<>();
    }

    public void process(GlobalRouteInfo route) {
        if (route.getRouteOperation() == RouteOperation.ADD) {
            LOG.debug("[{}][{}] Adding route {} for cf version {}", tenantId, userId, route, route.getCfVersion());
            map.add(ConfigurationKey.fromRouteInfo(route), route);
        } else if (route.getRouteOperation() == RouteOperation.DELETE) {
            LOG.debug("[{}][{}] Remove route {} for cf version {}", tenantId, userId, route, route.getCfVersion());
            map.remove(ConfigurationKey.fromRouteInfo(route), route);
        } else {
            LOG.warn("[{}][{}] unsupported route operations {}", tenantId, userId, route.getRouteOperation());
        }
    }

    public void process(UserConfigurationUpdate update) {
        ConfigurationKey key = ConfigurationKey.fromUpdateMessage(update);
        Map<String, Set<GlobalRouteInfo>> routes = map.getRoutesByServer(key);
        for (Entry<String,Set<GlobalRouteInfo>> entry : routes.entrySet()) {
            LOG.debug("Sending notification to {} about configuration update", entry.getKey());
            for(GlobalRouteInfo route : entry.getValue()){
                LOG.debug("Sending notification to route {}", route);
            }
        }
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

        public boolean remove(T key, GlobalRouteInfo route) {
            Set<GlobalRouteInfo> keyRoutes = routes.get(key);
            if (keyRoutes == null) {
                return false;
            } else {
                return keyRoutes.remove(route);
            }
        }

        public Set<GlobalRouteInfo> getRoutes(T key) {
            return routes.get(key);
        }

        public Map<String, Set<GlobalRouteInfo>> getRoutesByServer(T key) {
            Set<GlobalRouteInfo> keyRoutes = getRoutes(key);
            Map<String, Set<GlobalRouteInfo>> result = new HashMap<String, Set<GlobalRouteInfo>>();
            for (GlobalRouteInfo route : keyRoutes) {
                String serverId = route.getAddress().getServerId();
                Set<GlobalRouteInfo> set = result.get(serverId);
                if (set == null) {
                    set = new HashSet<GlobalRouteInfo>();
                    result.put(serverId, set);
                }
                set.add(route);
            }
            return result;
        }
    }

}
