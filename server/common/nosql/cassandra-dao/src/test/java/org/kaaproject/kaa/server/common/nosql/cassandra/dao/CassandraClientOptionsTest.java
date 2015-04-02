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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import org.junit.Test;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.client.CassandraClientOptions;

public class CassandraClientOptionsTest {

    @Test
    public void cassandraClientOptionsTest(){
        CassandraClientOptions clientOptions = new CassandraClientOptions();
        clientOptions.setConnectTimeoutMillis(0);
        clientOptions.setReadTimeoutMillis(0);
        clientOptions.setKeepAlive(null);
        clientOptions.setReuseAddress(null);
        clientOptions.setSoLinger(null);
        clientOptions.setTcpNoDelay(null);
        clientOptions.setReceiveBufferSize(null);
        clientOptions.setSendBufferSize(null);
        clientOptions.setSocketOptions(null);
        clientOptions.setConsistencyLevel(null);
        clientOptions.setDefaultFetchSize(null);
        clientOptions.setQueryOptions(null);
    }

}
