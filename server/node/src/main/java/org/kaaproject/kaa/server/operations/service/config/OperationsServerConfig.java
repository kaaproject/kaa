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

package org.kaaproject.kaa.server.operations.service.config;

/**
 * The Class OperationsServerConfig.
 */
public class OperationsServerConfig {

    private static final int DEFAULT_USER_HASH_PARTITIONS_SIZE = 10;
    private static final int DEFAULT_MAX_NEIGHBOR_CONNECTIONS = 10;

    private int userHashPartitions = DEFAULT_USER_HASH_PARTITIONS_SIZE;

    private int maxNumberNeighborConnections = DEFAULT_MAX_NEIGHBOR_CONNECTIONS;

    public int getUserHashPartitions() {
        return userHashPartitions;
    }

    public void setUserHashPartitions(int userHashPartitions) {
        this.userHashPartitions = userHashPartitions;
    }

    public int getMaxNumberNeighborConnections() {
        return maxNumberNeighborConnections;
    }

    public void setMaxNumberNeighborConnections(int maxNumberNeighborConnections) {
        this.maxNumberNeighborConnections = maxNumberNeighborConnections;
    }
}
