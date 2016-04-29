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

package org.kaaproject.kaa.server.control.service.sdk;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avro.Schema;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.common.Environment;
import org.kaaproject.kaa.server.common.Version;
import org.kaaproject.kaa.server.common.zk.ServerNameUtil;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.gen.VersionConnectionInfoPair;
import org.kaaproject.kaa.server.control.service.sdk.compress.TarEntryData;
import org.kaaproject.kaa.server.control.service.sdk.event.CppEventSourcesGenerator;
import org.kaaproject.kaa.server.control.service.sdk.event.EventFamilyMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CppSdkGenerator.
 */
public class CppSdkGenerator extends SdkGenerator {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(CppSdkGenerator.class);

    /** The Constant CPP_SDK_DIR. */
    private static final String CPP_SDK_DIR = "sdk/cpp";

    /** The Constant CPP_SDK_PREFIX. */
    private static final String CPP_SDK_PREFIX = "kaa-cpp-ep-sdk-";

    /** The Constant APPLICATION_TOKEN_VAR. */
    private static final String SDK_VAR = "%{application.sdk_token}";

    /** The Constant SDK_PROFILE_VERSION_VAR. */
    private static final String SDK_PROFILE_VERSION_VAR = "%{application.profile_version}";

    /** The Constant CLIENT_PUB_KEY_LOCATION_VAR. */
    private static final String CLIENT_PUB_KEY_LOCATION_VAR = "%{application.public_key_location}";

    /** The Constant CLIENT_PRIV_KEY_LOCATION_VAR. */
    private static final String CLIENT_PRIV_KEY_LOCATION_VAR = "%{application.private_key_location}";

    /** The Constant CLIENT_STATUS_FILE_LOCATION_VAR. */
    private static final String CLIENT_STATUS_FILE_LOCATION_VAR = "%{application.status_file_location}";

    /** The Constant POLLING_PERIOD_SECONDS_VAR. */
    private static final String POLLING_PERIOD_SECONDS_VAR = "%{application.polling_period_seconds}";

    /** The Constant BOOTSTRAP_SERVERS_INFO_VAR. */
    private static final String BOOTSTRAP_SERVERS_INFO_VAR = "%{bootstrap_servers_info}";

    /** The Constant CONFIG_DATA_DEFAULT_VAR. */
    private static final String CONFIG_DATA_DEFAULT_VAR = "%{config.data.default}";

    /** The Constant BUILD_VERSION. */
    private static final String BUILD_VERSION = "%{build.version}";

    /** The Constant BUILD_COMMIT_HASH. */
    private static final String BUILD_COMMIT_HASH = "%{build.commit_hash}";

    /** The Constant DEFAULT_USER_VERIFIER_TOKEN. */
    private static final String DEFAULT_USER_VERIFIER_TOKEN = "%{user_verifier_token}";

    private static final String SDK_DEFAULTS_TEMPLATE = "sdk/cpp/KaaDefaults.cpp.template";
    private static final String SDK_DEFAULTS_PATH = "impl/KaaDefaults.cpp";

    private static final String RECORD_CLASS_NAME_VAR = "%{record_class_name}";

    private static final String PROFILE_SCHEMA_AVRO_SRC = "avro/profile.avsc";
    private static final String PROFILE_DEFINITIONS_TEMPLATE = "sdk/cpp/profile/ProfileDefinitions.hpp.template";
    private static final String PROFILE_DEFINITIONS_PATH = "kaa/profile/gen/ProfileDefinitions.hpp";

    private static final String LOG_SCHEMA_AVRO_SRC = "avro/log.avsc";
    private static final String LOG_DEFINITIONS_TEMPLATE = "sdk/cpp/log/LogDefinitions.hpp.template";
    private static final String LOG_DEFINITIONS_PATH = "kaa/log/gen/LogDefinitions.hpp";

    private static final String NOTIFICATION_SCHEMA_AVRO_SRC = "avro/notification.avsc";
    private static final String NOTIFICATION_DEFINITIONS_TEMPLATE = "sdk/cpp/notification/NotificationDefinitions.hpp.template";
    private static final String NOTIFICATION_DEFINITIONS_PATH = "kaa/notification/gen/NotificationDefinitions.hpp";

    private static final String CONFIGURATION_SCHEMA_AVRO_SRC = "avro/configuration.avsc";
    private static final String CONFIGURATION_DEFINITIONS_TEMPLATE = "sdk/cpp/configuration/ConfigurationDefinitions.hpp.template";
    private static final String CONFIGURATION_DEFINITIONS_PATH = "kaa/configuration/gen/ConfigurationDefinitions.hpp";

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.control.service.sdk.SdkGenerator#generateSdk(java.lang.String, java.util.List, java.lang.String, int, int, int, java.lang.String, java.lang.String, java.lang.String, byte[], java.util.List)
     */
    @Override
    public FileData generateSdk(String buildVersion,
            List<BootstrapNodeInfo> bootstrapNodes, 
            SdkProfileDto sdkProfile,
            String profileSchemaBody,
            String notificationSchemaBody,
            String configurationProtocolSchemaBody,
            String configurationBaseSchema,
            byte[] defaultConfigurationData,
            List<EventFamilyMetadata> eventFamilies,
            String logSchemaBody) throws Exception {

        String sdkToken = sdkProfile.getToken();
        Integer configurationSchemaVersion = sdkProfile.getConfigurationSchemaVersion();
        Integer profileSchemaVersion = sdkProfile.getProfileSchemaVersion();
        Integer notificationSchemaVersion = sdkProfile.getNotificationSchemaVersion();
        Integer logSchemaVersion = sdkProfile.getLogSchemaVersion();
        String defaultVerifierToken = sdkProfile.getDefaultVerifierToken();

        String sdkTemplateLocation = Environment.getServerHomeDir() + "/" + CPP_SDK_DIR + "/" + CPP_SDK_PREFIX + buildVersion + ".tar.gz";

        LOG.debug("Lookup Java SDK template: {}", sdkTemplateLocation);

        CompressorStreamFactory csf = new CompressorStreamFactory();
        ArchiveStreamFactory asf = new ArchiveStreamFactory();

        CompressorInputStream cis = csf.createCompressorInputStream(CompressorStreamFactory.GZIP,
                new FileInputStream(sdkTemplateLocation));

        ArchiveInputStream templateArchive = asf.createArchiveInputStream(ArchiveStreamFactory.TAR, cis);

        ByteArrayOutputStream sdkOutput = new ByteArrayOutputStream();
        CompressorOutputStream cos = csf.createCompressorOutputStream(CompressorStreamFactory.GZIP, sdkOutput);
        ArchiveOutputStream sdkFile = asf.createArchiveOutputStream(ArchiveStreamFactory.TAR, cos);

        Map<String, TarEntryData> replacementData = new HashMap<>();

        List<TarEntryData> cppSources = new ArrayList<>();

        // TODO: remove all version fields and add single sdkToken field
        // create entry for default properties
        TarArchiveEntry entry = new TarArchiveEntry(SDK_DEFAULTS_PATH);
        byte[] data = generateKaaDefaults(bootstrapNodes, sdkToken, defaultConfigurationData, defaultVerifierToken);
        entry.setSize(data.length);
        TarEntryData tarEntry = new TarEntryData(entry, data);
        cppSources.add(tarEntry);

        Map<String, String> profileVars = new HashMap<>();
        profileVars.put(SDK_PROFILE_VERSION_VAR, profileSchemaVersion.toString());
        cppSources.addAll(processFeatureSchema(profileSchemaBody, PROFILE_SCHEMA_AVRO_SRC,
                                               PROFILE_DEFINITIONS_TEMPLATE, PROFILE_DEFINITIONS_PATH, profileVars));

        cppSources.addAll(processFeatureSchema(notificationSchemaBody, NOTIFICATION_SCHEMA_AVRO_SRC,
                                               NOTIFICATION_DEFINITIONS_TEMPLATE, NOTIFICATION_DEFINITIONS_PATH, null));
        cppSources.addAll(processFeatureSchema(logSchemaBody, LOG_SCHEMA_AVRO_SRC,
                                               LOG_DEFINITIONS_TEMPLATE, LOG_DEFINITIONS_PATH, null));
        cppSources.addAll(processFeatureSchema(configurationBaseSchema, CONFIGURATION_SCHEMA_AVRO_SRC,
                                               CONFIGURATION_DEFINITIONS_TEMPLATE, CONFIGURATION_DEFINITIONS_PATH, null));

        if (eventFamilies != null && !eventFamilies.isEmpty()) {
            cppSources.addAll(CppEventSourcesGenerator.generateEventSources(eventFamilies));
        }

        for (TarEntryData entryData : cppSources) {
            replacementData.put(entryData.getEntry().getName(), entryData);
        }

        ArchiveEntry e = null;
        while ((e = templateArchive.getNextEntry()) != null) {
            if (!e.isDirectory()) {
                if (replacementData.containsKey(e.getName())) {
                    TarEntryData entryData = replacementData.remove(e.getName());
                    sdkFile.putArchiveEntry(entryData.getEntry());
                    sdkFile.write(entryData.getData());
                } else {
                    sdkFile.putArchiveEntry(e);
                    IOUtils.copy(templateArchive, sdkFile);
                }
            } else {
                sdkFile.putArchiveEntry(e);
            }
            sdkFile.closeArchiveEntry();
        }

        templateArchive.close();

        for (String entryName : replacementData.keySet()) {
            TarEntryData entryData = replacementData.get(entryName);
            sdkFile.putArchiveEntry(entryData.getEntry());
            sdkFile.write(entryData.getData());
            sdkFile.closeArchiveEntry();
        }

        sdkFile.finish();
        sdkFile.close();

        String sdkFileName = CPP_SDK_PREFIX + sdkProfile.getToken() + ".tar.gz";

        byte[] sdkData = sdkOutput.toByteArray();

        FileData sdk = new FileData();
        sdk.setFileName(sdkFileName);
        sdk.setFileData(sdkData);
        return sdk;
    }

    private List<TarEntryData> processFeatureSchema(String schemaBody, String schemaPath,
                                                    String templatePath, String outputPath,
                                                    Map<String, String> vars) throws IOException {
        List<TarEntryData> cppSources = new LinkedList<>();

        if (!StringUtils.isBlank(schemaBody)) {
            TarArchiveEntry entry = new TarArchiveEntry(schemaPath);
            byte[] data = schemaBody.getBytes();
            entry.setSize(data.length);
            TarEntryData tarEntry = new TarEntryData(entry, data);
            cppSources.add(tarEntry);

            Schema schema = new Schema.Parser().parse(schemaBody);
            String definitionsHpp = SdkGenerator.readResource(templatePath);
            entry = new TarArchiveEntry(outputPath);

            String templateStr = replaceVar(definitionsHpp, RECORD_CLASS_NAME_VAR, schema.getName());
            if (vars != null && !vars.isEmpty()) {
                for (Entry<String, String> var : vars.entrySet()) {
                    templateStr = replaceVar(templateStr, var.getKey(), var.getValue());
                }
            }

            byte [] definitionsData = templateStr.getBytes();
            entry.setSize(definitionsData.length);
            tarEntry = new TarEntryData(entry, definitionsData);
            cppSources.add(tarEntry);
        }

        return cppSources;
    }

    /**
     * Generate client properties.
     *
     * @param bootstrapNodes the bootstrap nodes
     * @param sdkToken the app token
     * @param defaultConfigurationData the default configuration data
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private byte[] generateKaaDefaults(List<BootstrapNodeInfo> bootstrapNodes,
                                       String sdkToken,
                                       byte[] defaultConfigurationData,
                                       String defaultVerifierToken) throws IOException {
        String kaaDefaultsString = SdkGenerator.readResource(SDK_DEFAULTS_TEMPLATE);

        LOG.debug("[sdk generateClientProperties] bootstrapNodes.size(): {}", bootstrapNodes.size());

        kaaDefaultsString = replaceVar(kaaDefaultsString, BUILD_VERSION, Version.PROJECT_VERSION);
        kaaDefaultsString = replaceVar(kaaDefaultsString, BUILD_COMMIT_HASH, Version.COMMIT_HASH);

        kaaDefaultsString = replaceVar(kaaDefaultsString, SDK_VAR, sdkToken);

        kaaDefaultsString = replaceVar(kaaDefaultsString, POLLING_PERIOD_SECONDS_VAR, "5");

        kaaDefaultsString = replaceVar(kaaDefaultsString, CLIENT_PUB_KEY_LOCATION_VAR, "key.public");
        kaaDefaultsString = replaceVar(kaaDefaultsString, CLIENT_PRIV_KEY_LOCATION_VAR, "key.private");
        kaaDefaultsString = replaceVar(kaaDefaultsString, CLIENT_STATUS_FILE_LOCATION_VAR, "kaa.status");

        kaaDefaultsString = replaceVar(kaaDefaultsString,
                                       DEFAULT_USER_VERIFIER_TOKEN,
                                       defaultVerifierToken != null ? defaultVerifierToken : "");

        String bootstrapServers = "";

        LOG.debug("[sdk generateClientProperties] bootstrapNodes.size(): {}", bootstrapNodes.size());

        for (BootstrapNodeInfo node : bootstrapNodes) {
            List<TransportMetaData> supportedChannels = node.getTransports();

            int accessPointId = ServerNameUtil.crc32(node.getConnectionInfo());
            for (TransportMetaData transport : supportedChannels) {
                for(VersionConnectionInfoPair pair : transport.getConnectionInfo()) {
                    String serverPattern = "listOfServers.push_back(createTransportInfo(";
                    serverPattern += "0x" + Integer.toHexString(accessPointId);
                    serverPattern += ", ";
                    serverPattern += "0x" + Integer.toHexString(transport.getId());
                    serverPattern += ", ";
                    serverPattern += pair.getVersion();
                    serverPattern += ", \"";
                    serverPattern += Base64.encodeBase64String(pair.getConenctionInfo().array());
                    serverPattern += "\"";
                    serverPattern += "));\n";
                    bootstrapServers += serverPattern;
                }
            }
        }

        kaaDefaultsString = replaceVar(kaaDefaultsString, BOOTSTRAP_SERVERS_INFO_VAR, bootstrapServers);
        kaaDefaultsString = replaceVar(kaaDefaultsString, CONFIG_DATA_DEFAULT_VAR, Base64.encodeBase64String(defaultConfigurationData));

        return kaaDefaultsString.getBytes();
    }

    /**
     * Replace string.
     *
     * @param body the body
     * @param variable the variable
     * @param value the value
     * @return the string
     */
    private static String replaceVar(String body, String variable, String value) {
        return body.replace(variable, value);
    }

}
