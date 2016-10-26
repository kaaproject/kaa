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

package org.kaaproject.kaa.client;

import static org.kaaproject.kaa.client.util.Utils.isBlank;

import org.apache.commons.compress.utils.Charsets;
import org.kaaproject.kaa.client.channel.GenericTransportInfo;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.util.Base64;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolVersionPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Service class to store base endpoint configuration.
 */
public class KaaClientProperties extends Properties {
  public static final String KAA_CLIENT_PROPERTIES_FILE = "kaaClientPropertiesFile";
  public static final String BUILD_VERSION = "build.version";
  public static final String BUILD_COMMIT_HASH = "build.commit_hash";
  public static final String TRANSPORT_POLL_DELAY = "transport.poll.initial_delay";
  public static final String TRANSPORT_POLL_PERIOD = "transport.poll.period";
  public static final String TRANSPORT_POLL_UNIT = "transport.poll.unit";
  public static final String BOOTSTRAP_SERVERS = "transport.bootstrap.servers";
  public static final String CONFIG_DATA_DEFAULT = "config.data.default";
  public static final String CONFIG_SCHEMA_DEFAULT = "config.schema.default";
  public static final String SDK_TOKEN = "sdk_token";
  public static final String WORKING_DIR_PROPERTY = "kaa.work_dir";
  public static final String FILE_SEPARATOR = File.separator;
  public static final String WORKING_DIR_DEFAULT = "." + FILE_SEPARATOR;
  public static final String STATE_FILE_NAME_DEFAULT = "state.properties";
  public static final String CLIENT_PRIVATE_KEY_NAME_DEFAULT = "key.private";
  public static final String CLIENT_PUBLIC_KEY_NAME_DEFAULT = "key.public";
  public static final String STATE_FILE_NAME_PROPERTY = "state.file_name";
  public static final String CLIENT_PRIVATE_KEY_FILE_NAME_PROPERTY = "keys.private_name";
  public static final String CLIENT_PUBLIC_KEY_FILE_NAME_PROPERTY = "keys.public_name";
  private static final Logger LOG = LoggerFactory.getLogger(KaaClientProperties.class);
  private static final String DEFAULT_CLIENT_PROPERTIES = "client.properties";
  private static final long serialVersionUID = 8793954229852581418L;
  private static final String PROPERTIES_HASH_ALGORITHM = "SHA";

  private Base64 base64;

  private byte[] propertiesHash;

  public KaaClientProperties() throws IOException {
    super(loadProperties(null));
  }

  public KaaClientProperties(String propertiesLocation) throws IOException {
    super(loadProperties(propertiesLocation));
  }

  public KaaClientProperties(Properties properties) {
    super(properties);
  }

  private static Properties loadProperties(String propsLocation) throws IOException {
    Properties properties = null;
    String propertiesLocation = isBlank(propsLocation) ? DEFAULT_CLIENT_PROPERTIES : propsLocation;
    if (System.getProperty(KAA_CLIENT_PROPERTIES_FILE) != null) {
      propertiesLocation = System.getProperty(KAA_CLIENT_PROPERTIES_FILE);
    }
    properties = new Properties();
    ClassLoader classLoader = Kaa.class.getClassLoader();
    properties.load(classLoader.getResourceAsStream(propertiesLocation));
    return properties;
  }

  private static void checkNotBlankProperty(String fileName, String errorMessage) {
    if (isBlank(fileName)) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  /**
   * Calculates a hash for SDK properties.
   *
   * @return hash for SDK properties
   */
  public byte[] getPropertiesHash() {
    if (propertiesHash == null) {
      try {
        MessageDigest digest = MessageDigest.getInstance(PROPERTIES_HASH_ALGORITHM);

        updateDigest(digest, TRANSPORT_POLL_DELAY);
        updateDigest(digest, TRANSPORT_POLL_PERIOD);
        updateDigest(digest, TRANSPORT_POLL_UNIT);
        updateDigest(digest, BOOTSTRAP_SERVERS);
        updateDigest(digest, CONFIG_DATA_DEFAULT);
        updateDigest(digest, CONFIG_SCHEMA_DEFAULT);
        updateDigest(digest, SDK_TOKEN);

        propertiesHash = digest.digest();
      } catch (NoSuchAlgorithmException ex) {
        LOG.warn("Failed to calculate hash for SDK properties: {}", ex);
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

  public String getBuildVersion() {
    return getProperty(BUILD_VERSION);
  }

  public String getCommitHash() {
    return getProperty(BUILD_COMMIT_HASH);
  }

  public Map<TransportProtocolId, List<TransportConnectionInfo>> getBootstrapServers()
          throws InvalidKeySpecException, NoSuchAlgorithmException {
    return parseBootstrapServers(getProperty(KaaClientProperties.BOOTSTRAP_SERVERS));
  }

  public String getSdkToken() {
    return getProperty(KaaClientProperties.SDK_TOKEN);
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

  private Map<TransportProtocolId, List<TransportConnectionInfo>> parseBootstrapServers(
          String serversStr) throws InvalidKeySpecException, NoSuchAlgorithmException {
    Map<TransportProtocolId, List<TransportConnectionInfo>> servers = new HashMap<>();
    String[] serversSplit = serversStr.split(";");

    for (String server : serversSplit) {
      if (server != null && !server.trim().isEmpty()) {
        String[] tokens = server.split(":");
        ProtocolMetaData md = new ProtocolMetaData();
        md.setAccessPointId(Integer.valueOf(tokens[0]));
        md.setProtocolVersionInfo(new ProtocolVersionPair(Integer.valueOf(tokens[1]),
                Integer.valueOf(tokens[2])));
        md.setConnectionInfo(ByteBuffer.wrap(getBase64().decodeBase64(tokens[3])));
        TransportProtocolId key = new TransportProtocolId(md.getProtocolVersionInfo().getId(),
                md.getProtocolVersionInfo().getVersion());
        List<TransportConnectionInfo> serverList = servers.get(key);
        if (serverList == null) {
          serverList = new ArrayList<TransportConnectionInfo>();
          servers.put(key, serverList);
        }
        serverList.add(new GenericTransportInfo(ServerType.BOOTSTRAP, md));
      }
    }
    return servers;
  }

  public byte[] getDefaultConfigData() {
    String config = getProperty(KaaClientProperties.CONFIG_DATA_DEFAULT);
    return (config != null) ? getBase64().decodeBase64(config.getBytes(Charsets.UTF_8)) : null;
  }

  public byte[] getDefaultConfigSchema() {
    String schema = getProperty(KaaClientProperties.CONFIG_SCHEMA_DEFAULT);
    return (schema != null) ? schema.getBytes(Charsets.UTF_8) : null;
  }

  public Base64 getBase64() {
    return base64;
  }

  public void setBase64(Base64 base64) {
    this.base64 = base64;
  }

  public String getWorkingDirectory() {
    String workingDir = getProperty(WORKING_DIR_PROPERTY);
    return isBlank(workingDir) ? WORKING_DIR_DEFAULT : checkDir(workingDir);
  }

  public void setWorkingDirectory(String workDir) {
    checkNotBlankProperty(workDir, "Working directory folder name couldn't be blank");
    setProperty(WORKING_DIR_PROPERTY, checkDir(workDir));
  }

  private String checkDir(String workDir) {
    return workDir.endsWith(FILE_SEPARATOR) ? workDir : workDir + FILE_SEPARATOR;
  }

  public String getStateFileName() {
    String stateFileName = getProperty(STATE_FILE_NAME_PROPERTY);
    return isBlank(stateFileName) ? STATE_FILE_NAME_DEFAULT : stateFileName;
  }

  public void setStateFileName(String fileName) {
    checkNotBlankProperty(fileName, "State file name couldn't be blank");
    setProperty(STATE_FILE_NAME_PROPERTY, fileName);
  }

  public String getStateFileFullName() {
    return getWorkingDirectory() + getStateFileName();
  }

  public String getPublicKeyFileName() {
    String privateKeyName = getProperty(CLIENT_PUBLIC_KEY_FILE_NAME_PROPERTY);
    return isBlank(privateKeyName) ? CLIENT_PRIVATE_KEY_NAME_DEFAULT : privateKeyName;
  }

  public void setPublicKeyFileName(String fileName) {
    checkNotBlankProperty(fileName, "Public key file name couldn't be blank");
    setProperty(CLIENT_PUBLIC_KEY_FILE_NAME_PROPERTY, fileName);
  }

  public String getPublicKeyFileFullName() {
    return getWorkingDirectory() + getPublicKeyFileName();
  }

  public String getPrivateKeyFileName() {
    String publicKeyName = getProperty(CLIENT_PRIVATE_KEY_FILE_NAME_PROPERTY);
    return isBlank(publicKeyName) ? CLIENT_PUBLIC_KEY_NAME_DEFAULT : publicKeyName;
  }

  public void setPrivateKeyFileName(String fileName) {
    checkNotBlankProperty(fileName, "Private key file name couldn't be blank");
    setProperty(CLIENT_PRIVATE_KEY_FILE_NAME_PROPERTY, fileName);
  }

  public String getPrivateKeyFileFullName() {
    return getWorkingDirectory() + getPrivateKeyFileName();
  }

}
