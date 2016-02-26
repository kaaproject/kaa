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

package org.kaaproject.kaa.server.common;

import org.cassandraunit.BaseCassandraUnit;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CustomCassandraCQLUnit extends BaseCassandraUnit {
    private CQLDataSet dataSet;

    public Session session;
    public Cluster cluster;

    public CustomCassandraCQLUnit(CQLDataSet dataSet) {
        this.dataSet = dataSet;
    }

    public CustomCassandraCQLUnit(CQLDataSet dataSet, int readTimeoutMillis) {
        this.dataSet = dataSet;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public CustomCassandraCQLUnit(CQLDataSet dataSet, String configurationFileName) {
        this(dataSet);
        this.configurationFileName = configurationFileName;
    }

    public CustomCassandraCQLUnit(CQLDataSet dataSet, String configurationFileName, int readTimeoutMillis) {
        this(dataSet);
        this.configurationFileName = configurationFileName;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public CustomCassandraCQLUnit(CQLDataSet dataSet, String configurationFileName, long startUpTimeoutMillis) {
        super(startUpTimeoutMillis);
        this.dataSet = dataSet;
        this.configurationFileName = configurationFileName;
    }

    public CustomCassandraCQLUnit(CQLDataSet dataSet, String configurationFileName, long startUpTimeoutMillis, int readTimeoutMillis) {
        super(startUpTimeoutMillis);
        this.dataSet = dataSet;
        this.configurationFileName = configurationFileName;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    @Override
    protected void load() {
        String hostIp = EmbeddedCassandraServerHelper.getHost();
        int port = EmbeddedCassandraServerHelper.getNativeTransportPort();
        cluster = new Cluster.Builder().addContactPoints(hostIp).withPort(port).withSocketOptions(getSocketOptions())
                .build();
        session = cluster.connect();
        CQLDataLoader dataLoader = new CQLDataLoader(session);
        dataLoader.load(dataSet);
        session = dataLoader.getSession();
    }

    @Override
    protected void after() {
        super.after();
        try (Cluster c = cluster; Session s = session) {
            session = null;
            cluster = null;
        }
    }

    // Getters for those who do not like to directly access fields

    public Session getSession() {
        return session;
    }

    public Cluster getCluster() {
        return cluster;
    }
}
