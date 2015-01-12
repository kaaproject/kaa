package org.kaaproject.kaa.server.common.dao.cassandra.client;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.datastax.driver.core.ProtocolOptions.Compression;

@Component
public class CassandraClient implements Closeable {

    private static final String COMMA = ",";
    private static final String COLON = ":";

    /* Main cassandra parameters */
    @Value("#{properties[cluster_name]}")
    private String clusterName;
    @Value("#{properties[keyspace_name]}")
    private String keyspaceName;
    @Value("#{properties[node_list]}")
    private String nodeList;
    @Value("#{properties[compression]}")
    private String compression;
    @Value("#{properties[use_ssl]}")
    private Boolean useSSL;
    @Value("#{properties[use_jmx]}")
    private Boolean useJMX;
    @Value("#{properties[use_credentials]}")
    private Boolean useCredentials;
    @Value("#{properties[username]}")
    private String username;
    @Value("#{properties[password]}")
    private String password;
    @Value("#{properties[disable_metrics]}")
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

    public Mapper<?> getMapper(Class<?> clazz) {
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
//        builder.withLoadBalancingPolicy();
//        builder.withPoolingOptions();
//        builder.withReconnectionPolicy();
//        builder.withRetryPolicy();
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
