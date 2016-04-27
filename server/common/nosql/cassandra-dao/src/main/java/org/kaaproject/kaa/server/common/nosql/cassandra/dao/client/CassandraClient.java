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


import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ProtocolOptions.Compression;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

@Component
public class CassandraClient implements Closeable {

    private static final String COMMA = ",";
    private static final String COLON = ":";

    /* Main cassandra parameters */
    @Value("#{cassandra_properties[cluster_name]}")
    private String clusterName;
    @Value("#{cassandra_properties[keyspace_name]}")
    private String keyspaceName;
    @Value("#{cassandra_properties[node_list]}")
    private String nodeList;
    @Value("#{cassandra_properties[compression]}")
    private String compression;
    @Value("#{cassandra_properties[use_ssl]}")
    private Boolean useSSL;
    @Value("#{cassandra_properties[use_jmx]}")
    private Boolean useJMX;
    @Value("#{cassandra_properties[use_credentials]}")
    private Boolean useCredentials;
    @Value("#{cassandra_properties[username]}")
    private String username;
    @Value("#{cassandra_properties[password]}")
    private String password;
    @Value("#{cassandra_properties[disable_metrics]}")
    private Boolean disableMetrics;

    @Autowired
    private CassandraClientOptions clientOptions;

    private Cluster cluster;
    private Session session;
    private MappingManager mappingManager;


    public Session getSession() {
        if (session == null) {
            session = cluster.connect(keyspaceName);
        }
        return session;
    }

    public Session getNewSession() {
        return cluster.newSession();
    }

    public <T> Mapper<T> getMapper(Class<T> clazz) {
        return getMappingManager().mapper(clazz);
    }

    private MappingManager getMappingManager() {
        if (mappingManager == null) {
            mappingManager = new MappingManager(getSession());
        }
        return mappingManager;
    }

    @PostConstruct
    public void init() {
        Cluster.Builder builder = Cluster.builder().addContactPointsWithPorts(parseNodeList(nodeList)).withClusterName(clusterName).withSocketOptions(clientOptions.getSocketOptions());
        if (!useJMX) {
            builder.withoutJMXReporting();
        }
        if (disableMetrics) {
            builder.withoutMetrics();
        }
        if (useCredentials) {
            builder.withCredentials(username, password);
        }
        if (useSSL) {
            builder.withSSL();
        }
        builder.withQueryOptions(clientOptions.getQueryOptions());
        builder.withCompression(parseCompression(compression));
        cluster = builder.build();
    }

    private List<InetSocketAddress> parseNodeList(String nodeList) {
        List<InetSocketAddress> addresses = Collections.emptyList();
        if (StringUtils.isNotBlank(nodeList)) {
            String[] nodes;
            addresses = new ArrayList<>();
            if (nodeList.contains(COMMA)) {
                nodes = nodeList.split(COMMA);
            } else {
                nodes = new String[]{nodeList};
            }
            for (String node : nodes) {
                if (node.contains(COLON)) {
                    String[] hostAndPort = node.split(COLON);
                    addresses.add(new InetSocketAddress(hostAndPort[0], Integer.valueOf(hostAndPort[1])));
                }
            }
        }
        return addresses;
    }

    private Compression parseCompression(String cmp) {
        Compression compression = Compression.NONE;
        if (StringUtils.isNotBlank(cmp)) {
            for (Compression current : Compression.values()) {
                if (current.name().equalsIgnoreCase(cmp)) {
                    compression = current;
                    break;
                }
            }
        }
        return compression;
    }

    @Override
    public void close() {
        if (cluster != null) {
            cluster.close();
        }
    }
}
