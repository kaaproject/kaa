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

package org.kaaproject.kaa.client;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.Charsets;
import org.kaaproject.kaa.client.channel.HttpLongPollServerInfo;
import org.kaaproject.kaa.client.channel.HttpServerInfo;
import org.kaaproject.kaa.client.channel.KaaTcpServerInfo;
import org.kaaproject.kaa.client.channel.ServerInfo;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.endpoint.gen.EndpointVersionInfo;
import org.kaaproject.kaa.common.endpoint.gen.EventClassFamilyVersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class to store base endpoint configuration
 */
public class KaaClientProperties extends Properties {
    private static final Logger LOG = LoggerFactory.getLogger(KaaClientProperties.class);

    private static final String DEFAULT_CLIENT_PROPERTIES = "client.properties";
    public static final String KAA_CLIENT_PROPERTIES_FILE = "kaaClientPropertiesFile";

    private static final long serialVersionUID = 8793954229852581418L;

    public static final String TRANSPORT_POLL_DELAY = "transport.poll.initial_delay";
    public static final String TRANSPORT_POLL_PERIOD = "transport.poll.period";
    public static final String TRANSPORT_POLL_UNIT = "transport.poll.unit";
    public static final String BOOTSTRAP_SERVERS = "transport.bootstrap.servers";
    public static final String CONFIG_VERSION = "config_version";
    public static final String PROFILE_VERSION = "profile_version";
    public static final String SYSTEM_NT_VERSION = "system_nt_version";
    public static final String USER_NT_VERSION = "user_nt_version";
    public static final String APPLICATION_TOKEN = "application_token";
    public static final String CONFIG_DATA_DEFAULT = "config.data.default";
    public static final String CONFIG_SCHEMA_DEFAULT = "config.schema.default";
    public static final String EVENT_CLASS_FAMILY_VERSION = "event_cf_version";
    public static final String LOG_SCHEMA_VERSION = "logs_version";

    private static final String PROPERTIES_HASH_ALGORITHM = "SHA";
    private byte[] propertiesHash;

    public KaaClientProperties(Properties properties) {
        super(properties);
    }

    public KaaClientProperties() throws IOException{
        super(loadProperties());
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = null;
        String propertiesLocation = DEFAULT_CLIENT_PROPERTIES;
        if (System.getProperty(KAA_CLIENT_PROPERTIES_FILE) != null) {
            propertiesLocation = System.getProperty(KAA_CLIENT_PROPERTIES_FILE);
        }
        properties = new Properties();
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        properties
                .load(classLoader.getResourceAsStream(propertiesLocation));
        return properties;
    }

    public byte[] getPropertiesHash() {
        if (propertiesHash == null) {
            try {
                MessageDigest digest = MessageDigest.getInstance(PROPERTIES_HASH_ALGORITHM);

                updateDigest(digest, TRANSPORT_POLL_DELAY);
                updateDigest(digest, TRANSPORT_POLL_PERIOD);
                updateDigest(digest, TRANSPORT_POLL_UNIT);
                updateDigest(digest, BOOTSTRAP_SERVERS);
                updateDigest(digest, CONFIG_VERSION);
                updateDigest(digest, PROFILE_VERSION);
                updateDigest(digest, SYSTEM_NT_VERSION);
                updateDigest(digest, USER_NT_VERSION);
                updateDigest(digest, APPLICATION_TOKEN);
                updateDigest(digest, CONFIG_DATA_DEFAULT);
                updateDigest(digest, CONFIG_SCHEMA_DEFAULT);
                updateDigest(digest, EVENT_CLASS_FAMILY_VERSION);
                updateDigest(digest, LOG_SCHEMA_VERSION);

                propertiesHash = digest.digest();
            } catch (NoSuchAlgorithmException e) {
                LOG.warn("Failed to calculate hash for SDK properties: {}", e);
            }
        }

        return propertiesHash;
    }

    private void updateDigest(MessageDigest digest, String propertyName) {
        String value = getProperty(propertyName);
        if (value != null) {
            digest.update(value.getBytes());
        }
    }

    public int getSupportedConfigVersion() {
        return Integer.parseInt(getProperty(KaaClientProperties.CONFIG_VERSION));
    }

    public int getSupportedProfileVersion() {
        return Integer.parseInt(getProperty(KaaClientProperties.PROFILE_VERSION));
    }

    public int getSupportedSystemNTVersion() {
        return Integer.parseInt(getProperty(KaaClientProperties.SYSTEM_NT_VERSION));
    }

    public int getSupportedUserNTVersion() {
        return Integer.parseInt(getProperty(KaaClientProperties.USER_NT_VERSION));
    }

    public int getLogSchemaVersion() {
        return Integer.parseInt(getProperty(KaaClientProperties.LOG_SCHEMA_VERSION));
    }

    public EndpointVersionInfo getVersionInfo(){
        return new EndpointVersionInfo(getSupportedConfigVersion(), getSupportedProfileVersion(),
                getSupportedSystemNTVersion(), getSupportedUserNTVersion(), getEventFamilyVersions(), getLogSchemaVersion());
    }

    public Map<ChannelType, List<ServerInfo>> getBootstrapServers() throws InvalidKeySpecException, NoSuchAlgorithmException {
        return parseBootstrapServers(getProperty(KaaClientProperties.BOOTSTRAP_SERVERS));
    }

    public String getApplicationToken() {
        return getProperty(KaaClientProperties.APPLICATION_TOKEN);
    }

    public Integer getPollDelay() {
        return Integer.valueOf(getProperty(KaaClientProperties.TRANSPORT_POLL_DELAY));
    }

    public Integer getPollPeriod() {
        return Integer.valueOf(getProperty(KaaClientProperties.TRANSPORT_POLL_PERIOD));
    }

    public TimeUnit getPollUnit() {
        return TimeUnit.valueOf(getProperty(KaaClientProperties.TRANSPORT_POLL_UNIT));
    }

    public List<EventClassFamilyVersionInfo> getEventFamilyVersions() {
        return parseEventClassFamilyVersions(getProperty(KaaClientProperties.EVENT_CLASS_FAMILY_VERSION));
    }

    private Map<ChannelType, List<ServerInfo>> parseBootstrapServers(String serversStr) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Map<ChannelType, List<ServerInfo>> servers = new HashMap<>();
        String[] serversSplit = serversStr.split(";");

        for (String server : serversSplit) {
            if (server != null && !server.trim().isEmpty()) {
                String[] communicationParams = server.split("\\|");

                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(
                        new X509EncodedKeySpec(Base64.decodeBase64(
                                communicationParams[communicationParams.length - 1].getBytes(Charsets.UTF_8))));

                for (int i = 0; i < (communicationParams.length - 1); ++i) {
                    String[] serverInfoSplit = communicationParams[i].split(":");
                    if (serverInfoSplit.length == 3) {
                        ServerInfo si = null;
                        ChannelType type = null;
                        String host = serverInfoSplit[1];
                        int port = Integer.parseInt(serverInfoSplit[2]);

                        switch (Integer.parseInt(serverInfoSplit[0])) {
                        case 0:
                            type = ChannelType.HTTP;
                            si = new HttpServerInfo(ServerType.BOOTSTRAP, host, port, publicKey);
                            break;
                        case 1:
                            type = ChannelType.HTTP_LP;
                            si = new HttpLongPollServerInfo(ServerType.BOOTSTRAP, host, port, publicKey);
                            break;
                        case 2:
                            type = ChannelType.KAATCP;
                            si = new KaaTcpServerInfo(ServerType.BOOTSTRAP, host, port, publicKey);
                            break;
                        default:
                            //TODO
                            break;
                        }

                        List<ServerInfo> serverList = servers.get(type);

                        if (serverList == null) {
                            serverList = new LinkedList<>();
                            servers.put(type, serverList);
                        }

                        serverList.add(si);
                    }
                }
            }
        }
        return servers;
    }

    private List<EventClassFamilyVersionInfo> parseEventClassFamilyVersions(String eventFamiliesInfo) {
        List<EventClassFamilyVersionInfo> result = new ArrayList<EventClassFamilyVersionInfo>();
        if (eventFamiliesInfo != null) {
            String[] eventFamilyInfoSplit = eventFamiliesInfo.split(";");
            for (String efInfo : eventFamilyInfoSplit) {
                if (efInfo != null && !efInfo.trim().isEmpty()) {
                    String[] eventFamilyInfo = efInfo.split(":");
                    if (eventFamilyInfo.length == 2) {
                        String name = eventFamilyInfo[0];
                        int version = Integer.parseInt(eventFamilyInfo[1]);
                        result.add(new EventClassFamilyVersionInfo(name, version));
                    }
                }
            }
        }
        return result;
    }

    public byte [] getDefaultConfigData() {
        String config = getProperty(KaaClientProperties.CONFIG_DATA_DEFAULT);
        return (config != null) ? Base64.decodeBase64(config.getBytes(Charsets.UTF_8)) : null;
    }

    public byte [] getDefaultConfigSchema() {
        String schema = getProperty(KaaClientProperties.CONFIG_SCHEMA_DEFAULT);
        return (schema != null) ? schema.getBytes(Charsets.UTF_8) : null;
    }

}
