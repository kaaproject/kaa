/*
 * Copyright 2014 CyberVision, Inc.
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

import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import java.util.concurrent.TimeUnit;

public class CustomCassandraCQLUnit extends CassandraCQLUnit {

    private static long DEFAULT_CASSANDRA_TIMEOUT = 30000L;

    public CustomCassandraCQLUnit(CQLDataSet dataSet) {
        super(dataSet);
    }

    public CustomCassandraCQLUnit(CQLDataSet dataSet, String configurationFileName) {
        super(dataSet, configurationFileName);
    }

    public CustomCassandraCQLUnit(CQLDataSet dataSet, String configurationFileName, String hostIp, int port) {
        super(dataSet, configurationFileName, hostIp, port);
    }

    @Override
    protected void before() throws Exception {
        /* start an embedded Cassandra */
        if (configurationFileName != null) {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(configurationFileName, DEFAULT_CASSANDRA_TIMEOUT);
        } else {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(DEFAULT_CASSANDRA_TIMEOUT);
        }

        /* create structure and load data */
        load();
    }

    @Override
    protected void load() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.load();
    }
}
