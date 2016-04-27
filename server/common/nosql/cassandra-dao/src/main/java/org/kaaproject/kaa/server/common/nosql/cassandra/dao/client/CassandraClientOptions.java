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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.client;


import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SocketOptions;

@Component
public class CassandraClientOptions {

    /* Socket parameters */
    @Value("#{cassandra_properties[socket_connect_timeout]}")
    private int connectTimeoutMillis;
    @Value("#{cassandra_properties[socket_read_timeout]}")
    private int readTimeoutMillis;
    @Value("#{cassandra_properties[socket_keep_alive]}")
    private Boolean keepAlive;
    @Value("#{cassandra_properties[socket_reuse_address]}")
    private Boolean reuseAddress;
    @Value("#{cassandra_properties[socket_so_linger]}")
    private Integer soLinger;
    @Value("#{cassandra_properties[socket_tcp_no_delay]}")
    private Boolean tcpNoDelay;
    @Value("#{cassandra_properties[socket_receive_buffer_size]}")
    private Integer receiveBufferSize;
    @Value("#{cassandra_properties[socket_send_buffer_size]}")
    private Integer sendBufferSize;

    private SocketOptions socketOptions;

    /* Query parameters */
    @Value("#{cassandra_properties[query_consistency_level]}")
    private String consistencyLevel;
    @Value("#{cassandra_properties[query_default_fetch_size]}")
    private Integer defaultFetchSize;

    private QueryOptions queryOptions;

    public QueryOptions getQueryOptions() {
        if (queryOptions == null) {
            queryOptions = new QueryOptions();
            queryOptions.setConsistencyLevel(parseConsistencyLevel(consistencyLevel));
            queryOptions.setFetchSize(defaultFetchSize);
        }
        return queryOptions;
    }

    public SocketOptions getSocketOptions() {
        if (socketOptions == null) {
            socketOptions = new SocketOptions();
            socketOptions.setConnectTimeoutMillis(connectTimeoutMillis);
            socketOptions.setReadTimeoutMillis(readTimeoutMillis);
            if (keepAlive != null) {
                socketOptions.setKeepAlive(keepAlive);
            }
            if (reuseAddress != null) {
                socketOptions.setReuseAddress(reuseAddress);
            }
            if (soLinger != null) {
                socketOptions.setSoLinger(soLinger);
            }
            if (tcpNoDelay != null) {
                socketOptions.setTcpNoDelay(tcpNoDelay);
            }
            if (receiveBufferSize != null) {
                socketOptions.setReceiveBufferSize(receiveBufferSize);
            }
            if (sendBufferSize != null) {
                socketOptions.setSendBufferSize(sendBufferSize);
            }
        }
        return socketOptions;
    }

    private ConsistencyLevel parseConsistencyLevel(String level) {
        ConsistencyLevel consistencyLevel = ConsistencyLevel.ANY;
        if (isNotBlank(level)) {
            for (ConsistencyLevel current : ConsistencyLevel.values()) {
                if (current.name().equalsIgnoreCase(level)) {
                    consistencyLevel = current;
                    break;
                }
            }
        }
        return consistencyLevel;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public void setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setReuseAddress(Boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public void setSoLinger(Integer soLinger) {
        this.soLinger = soLinger;
    }

    public void setTcpNoDelay(Boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public void setReceiveBufferSize(Integer receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public void setSendBufferSize(Integer sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public void setSocketOptions(SocketOptions socketOptions) {
        this.socketOptions = socketOptions;
    }

    public void setConsistencyLevel(String consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public void setDefaultFetchSize(Integer defaultFetchSize) {
        this.defaultFetchSize = defaultFetchSize;
    }

    public void setQueryOptions(QueryOptions queryOptions) {
        this.queryOptions = queryOptions;
    }
}
