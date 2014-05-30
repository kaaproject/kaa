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

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.kaaproject.kaa.common.endpoint.gen.EndpointVersionInfo;

public class KaaClientProperties extends Properties {

    /**
     *
     */
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
    public static final String ENDPOINT_PUBLIC_KEY_FILE_LOCATION = "keys.endpoint";
    public static final String CONFIG_DATA_DEFAULT = "config.data.default";
    public static final String CONFIG_SCHEMA_DEFAULT = "config.schema.default";

    public KaaClientProperties(){
        super();
    }

    public KaaClientProperties(Properties properties){
        super(properties);
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

    public EndpointVersionInfo getVersionInfo(){
        return new EndpointVersionInfo(getSupportedConfigVersion(), getSupportedProfileVersion(),
                getSupportedSystemNTVersion(), getSupportedUserNTVersion());
    }

    public List<BootstrapServerInfo> getBootstrapServers() throws InvalidKeySpecException, NoSuchAlgorithmException {
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

    private List<BootstrapServerInfo> parseBootstrapServers(String servers) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String[] serversSplit = servers.split(";");
        List<BootstrapServerInfo> result = new ArrayList<BootstrapServerInfo>();
        for (String server : serversSplit) {
            if (server != null && !server.trim().isEmpty()) {
                String[] serverInfoSplit = server.split(":");
                if (serverInfoSplit.length==3) {
                    String host = serverInfoSplit[0];
                    int port = Integer.parseInt(serverInfoSplit[1]);
                    String publicKeyBase64 = serverInfoSplit[2];
                    PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(publicKeyBase64)));
                    BootstrapServerInfo serverInfo = new BootstrapServerInfo(host, port, publicKey);
                    result.add(serverInfo);
                }
            }
        }
        return result;
    }

    public byte [] getDefaultConfigData() {
        String config = getProperty(KaaClientProperties.CONFIG_DATA_DEFAULT);
        return (config != null) ? Base64.decodeBase64(config) : null;
    }

    public byte [] getDefaultConfigSchema() {
        String schema = getProperty(KaaClientProperties.CONFIG_SCHEMA_DEFAULT);
        return (schema != null) ? schema.getBytes() : null;
    }

    public static class BootstrapServerInfo {

        private final String host;
        private final int port;
        private final PublicKey publicKey;

        BootstrapServerInfo(String host, int port, PublicKey publicKey) {
            this.host = host;
            this.port = port;
            this.publicKey = publicKey;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public PublicKey getPublicKey() {
            return publicKey;
        }

        public String getURL() {
            return "http://" + host + ":" + port;
        }


        @Override
        public String toString() {
            return "BootstrapServerInfo [host=" + host + ", port=" + port + "]";
        }

    }
}
