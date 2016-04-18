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

import org.apache.avro.Schema;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.avro.avrogen.compiler.ObjectiveCCompiler;
import org.kaaproject.kaa.avro.avrogen.compiler.Compiler;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.common.Version;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.control.service.sdk.compress.TarEntryData;
import org.kaaproject.kaa.server.control.service.sdk.event.EventFamilyMetadata;
import org.kaaproject.kaa.server.control.service.sdk.event.ObjCEventClassesGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.*;

public class ObjCSdkGenerator extends SdkGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ObjCSdkGenerator.class);

    private static final String CONFIGURATION_DIR = "configuration/";
    private static final String NOTIFICATION_DIR = "notification/";
    private static final String PROFILE_DIR = "profile/";
    private static final String LOG_DIR = "log/";
    private static final String EVENT_DIR = "event/";

    private static final String SDK_TEMPLATE_DIR = "sdk/objc/";

    private static final String CONFIGURATION_TEMPLATE_DIR = SDK_TEMPLATE_DIR + CONFIGURATION_DIR;
    private static final String NOTIFICATION_TEMPLATE_DIR = SDK_TEMPLATE_DIR + NOTIFICATION_DIR;
    private static final String PROFILE_TEMPLATE_DIR = SDK_TEMPLATE_DIR + PROFILE_DIR;
    private static final String LOG_TEMPLATE_DIR = SDK_TEMPLATE_DIR + LOG_DIR;
    private static final String EVENT_TEMPLATE_DIR = SDK_TEMPLATE_DIR + EVENT_DIR;

    private static final String SDK_PREFIX = "kaa-objc-ep-sdk-";

    private static final String KAA_ROOT = "Kaa/";

    private static final String KAA_GEN_FOLDER = KAA_ROOT + "avro/gen/";

    private static final String KAA_SOURCE_PREFIX = "KAA";

    private static final String TEMPLATE_SUFFIX = ".template";
    private static final String _H = ".h";
    private static final String _M = ".m";

    private static final String NOTIFICATION_GEN = "NotificationGen";
    private static final String PROFILE_GEN = "ProfileGen";
    private static final String LOG_GEN = "LogGen";
    private static final String CONFIGURATION_GEN = "ConfigurationGen";

    private static final String KAA_DEFAULTS = "KaaDefaults.h";
    private static final String KAA_CLIENT = "KaaClient.h";
    private static final String BASE_KAA_CLIENT = "BaseKaaClient";
    private static final String CONFIGURATION_COMMON = "ConfigurationCommon.h";
    private static final String CONFIGURATION_MANAGER_IMPL = "ResyncConfigurationManager";
    private static final String CONFIGURATION_DESERIALIZER = "ConfigurationDeserializer";
    private static final String NOTIFICATION_COMMON = "NotificationCommon";
    private static final String PROFILE_COMMON = "ProfileCommon";
    private static final String LOG_RECORD = "LogRecord";
    private static final String LOG_COLLECTOR_INTERFACE = "LogCollector.h";
    private static final String LOG_COLLECTOR_SOURCE = "DefaultLogCollector";
    private static final String USER_VERIFIER_CONSTANTS = "UserVerifierConstants.h";


    private static final String PROFILE_CLASS_VAR = "\\$\\{profile_class\\}";
    private static final String CONFIGURATION_CLASS_VAR = "\\$\\{configuration_class\\}";
    private static final String NOTIFICATION_CLASS_VAR = "\\$\\{notification_class\\}";
    private static final String LOG_RECORD_CLASS_VAR = "\\$\\{log_record_class\\}";
    private static final String DEFAULT_USER_VERIFIER_TOKEN_VAR = "\\$\\{default_user_verifier_token\\}";

    @Override
    public FileData generateSdk(String buildVersion,
                           List<BootstrapNodeInfo> bootstrapNodes,
                           SdkProfileDto sdkProperties,
                           String profileSchemaBody,
                           String notificationSchemaBody,
                           String configurationProtocolSchemaBody,
                           String configurationBaseSchemaBody,
                           byte[] defaultConfigurationData,
                           List<EventFamilyMetadata> eventFamilies,
                           String logSchemaBody) throws Exception {

        String sdkToken = sdkProperties.getToken();
        String defaultVerifierToken = sdkProperties.getDefaultVerifierToken();

        String sdkTemplateLocation = System.getProperty("server_home_dir") + "/" + SDK_TEMPLATE_DIR + SDK_PREFIX + buildVersion + ".tar.gz";

        LOG.debug("Lookup Objective C SDK template: {}", sdkTemplateLocation);

        CompressorStreamFactory csf = new CompressorStreamFactory();
        ArchiveStreamFactory asf = new ArchiveStreamFactory();

        CompressorInputStream cis = csf.createCompressorInputStream(CompressorStreamFactory.GZIP,
                new FileInputStream(sdkTemplateLocation));

        ArchiveInputStream templateArchive = asf.createArchiveInputStream(ArchiveStreamFactory.TAR, cis);

        ByteArrayOutputStream sdkOutput = new ByteArrayOutputStream();
        CompressorOutputStream cos = csf.createCompressorOutputStream(CompressorStreamFactory.GZIP, sdkOutput);
        ArchiveOutputStream sdkFile = asf.createArchiveOutputStream(ArchiveStreamFactory.TAR, cos);

        Map<String, TarEntryData> replacementData = new HashMap<>();

        List<TarEntryData> objcSources = new ArrayList<>();

        if (StringUtils.isNotBlank(profileSchemaBody)) {
            LOG.debug("Generating profile schema");
            Schema profileSchema = new Schema.Parser().parse(profileSchemaBody);
            String profileClassName = KAA_SOURCE_PREFIX + profileSchema.getName();

            String profileCommonHeader = readResource(PROFILE_TEMPLATE_DIR + PROFILE_COMMON + _H + TEMPLATE_SUFFIX)
                    .replaceAll(PROFILE_CLASS_VAR, profileClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(profileCommonHeader,
                    KAA_ROOT + PROFILE_DIR + PROFILE_COMMON + _H));
            String profileCommonSource = readResource(PROFILE_TEMPLATE_DIR + PROFILE_COMMON + _M + TEMPLATE_SUFFIX)
                    .replaceAll(PROFILE_CLASS_VAR, profileClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(profileCommonSource,
                    KAA_ROOT + PROFILE_DIR + PROFILE_COMMON + _M));

            objcSources.addAll(generateSourcesFromSchema(profileSchema, PROFILE_GEN));
        }

        String logClassName = "";
        if (StringUtils.isNotBlank(logSchemaBody)) {
            LOG.debug("Generating log schema");
            Schema logSchema = new Schema.Parser().parse(logSchemaBody);
            logClassName = KAA_SOURCE_PREFIX + logSchema.getName();

            String logRecordHeader = readResource(LOG_TEMPLATE_DIR + LOG_RECORD + _H + TEMPLATE_SUFFIX)
                    .replaceAll(LOG_RECORD_CLASS_VAR, logClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(logRecordHeader,
                    KAA_ROOT + LOG_DIR + LOG_RECORD + _H));
            String logRecordSource = readResource(LOG_TEMPLATE_DIR + LOG_RECORD + _M + TEMPLATE_SUFFIX)
                    .replaceAll(LOG_RECORD_CLASS_VAR, logClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(logRecordSource,
                    KAA_ROOT + LOG_DIR + LOG_RECORD + _M));

            String logCollector = readResource(LOG_TEMPLATE_DIR + LOG_COLLECTOR_INTERFACE + TEMPLATE_SUFFIX)
                    .replaceAll(LOG_RECORD_CLASS_VAR, logClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(logCollector, KAA_ROOT + LOG_DIR + LOG_COLLECTOR_INTERFACE));

            String logCollectorImplHeader = readResource(LOG_TEMPLATE_DIR + LOG_COLLECTOR_SOURCE + _H + TEMPLATE_SUFFIX)
                    .replaceAll(LOG_RECORD_CLASS_VAR, logClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(logCollectorImplHeader,
                    KAA_ROOT + LOG_DIR + LOG_COLLECTOR_SOURCE + _H));
            String logCollectorImplSource = readResource(LOG_TEMPLATE_DIR + LOG_COLLECTOR_SOURCE + _M + TEMPLATE_SUFFIX)
                    .replaceAll(LOG_RECORD_CLASS_VAR, logClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(logCollectorImplSource,
                    KAA_ROOT + LOG_DIR + LOG_COLLECTOR_SOURCE + _M));

            objcSources.addAll(generateSourcesFromSchema(logSchema, LOG_GEN));
        }

        String configurationClassName = "";
        if (StringUtils.isNotBlank(configurationBaseSchemaBody)) {
            LOG.debug("Generating configuration schema");
            Schema configurationSchema = new Schema.Parser().parse(configurationBaseSchemaBody);
            configurationClassName = KAA_SOURCE_PREFIX + configurationSchema.getName();

            String configurationCommon = readResource(CONFIGURATION_TEMPLATE_DIR + CONFIGURATION_COMMON + TEMPLATE_SUFFIX)
                    .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(configurationCommon, KAA_ROOT + CONFIGURATION_DIR + CONFIGURATION_COMMON));

            String cfManagerImplHeader = readResource(CONFIGURATION_TEMPLATE_DIR + CONFIGURATION_MANAGER_IMPL + _H + TEMPLATE_SUFFIX)
                    .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(cfManagerImplHeader,
                    KAA_ROOT + CONFIGURATION_DIR + CONFIGURATION_MANAGER_IMPL + _H));
            String cfManagerImplSource = readResource(CONFIGURATION_TEMPLATE_DIR + CONFIGURATION_MANAGER_IMPL + _M + TEMPLATE_SUFFIX)
                    .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(cfManagerImplSource,
                    KAA_ROOT + CONFIGURATION_DIR + CONFIGURATION_MANAGER_IMPL + _M));

            String cfDeserializerHeader = readResource(CONFIGURATION_TEMPLATE_DIR + CONFIGURATION_DESERIALIZER + _H + TEMPLATE_SUFFIX)
                    .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(cfDeserializerHeader,
                    KAA_ROOT + CONFIGURATION_DIR + CONFIGURATION_DESERIALIZER + _H));
            String cfDeserializerSource = readResource(CONFIGURATION_TEMPLATE_DIR + CONFIGURATION_DESERIALIZER + _M + TEMPLATE_SUFFIX)
                    .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(cfDeserializerSource,
                    KAA_ROOT + CONFIGURATION_DIR + CONFIGURATION_DESERIALIZER + _M));

            objcSources.addAll(generateSourcesFromSchema(configurationSchema, CONFIGURATION_GEN));
        }

        if (StringUtils.isNotBlank(notificationSchemaBody)) {
            LOG.debug("Generating notification schema");
            Schema notificationSchema = new Schema.Parser().parse(notificationSchemaBody);
            String notificationClassName = KAA_SOURCE_PREFIX + notificationSchema.getName();

            String nfCommonHeader = readResource(NOTIFICATION_TEMPLATE_DIR + NOTIFICATION_COMMON + _H + TEMPLATE_SUFFIX)
                    .replaceAll(NOTIFICATION_CLASS_VAR, notificationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(nfCommonHeader,
                    KAA_ROOT + NOTIFICATION_DIR + NOTIFICATION_COMMON + _H));
            String nfCommonSource = readResource(NOTIFICATION_TEMPLATE_DIR + NOTIFICATION_COMMON + _M + TEMPLATE_SUFFIX)
                    .replaceAll(NOTIFICATION_CLASS_VAR, notificationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(nfCommonSource,
                    KAA_ROOT + NOTIFICATION_DIR + NOTIFICATION_COMMON + _M));

            objcSources.addAll(generateSourcesFromSchema(notificationSchema, NOTIFICATION_GEN));
        }

        if (eventFamilies != null && !eventFamilies.isEmpty()) {
            objcSources.addAll(ObjCEventClassesGenerator.generateEventSources(eventFamilies));
        }

        String kaaClient = readResource(SDK_TEMPLATE_DIR + KAA_CLIENT + TEMPLATE_SUFFIX)
                .replaceAll(LOG_RECORD_CLASS_VAR, logClassName)
                .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        objcSources.add(CommonSdkUtil.tarEntryForSources(kaaClient, KAA_ROOT + KAA_CLIENT));

        String baseKaaClientHeader = readResource(SDK_TEMPLATE_DIR + BASE_KAA_CLIENT + _H + TEMPLATE_SUFFIX)
                .replaceAll(LOG_RECORD_CLASS_VAR, logClassName)
                .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        objcSources.add(CommonSdkUtil.tarEntryForSources(baseKaaClientHeader, KAA_ROOT + BASE_KAA_CLIENT + _H));
        String baseKaaClientSource = readResource(SDK_TEMPLATE_DIR + BASE_KAA_CLIENT + _M + TEMPLATE_SUFFIX)
                .replaceAll(LOG_RECORD_CLASS_VAR, logClassName)
                .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        objcSources.add(CommonSdkUtil.tarEntryForSources(baseKaaClientSource, KAA_ROOT + BASE_KAA_CLIENT + _M));

        String tokenVerifier = defaultVerifierToken == null ? "nil" : "@\"" + defaultVerifierToken + "\"";
        String tokenVerifierSource = readResource(EVENT_TEMPLATE_DIR + USER_VERIFIER_CONSTANTS + TEMPLATE_SUFFIX)
                .replaceAll(DEFAULT_USER_VERIFIER_TOKEN_VAR, tokenVerifier);
        objcSources.add(CommonSdkUtil.tarEntryForSources(tokenVerifierSource, KAA_ROOT + EVENT_DIR + USER_VERIFIER_CONSTANTS));

        String kaaDefaultsTemplate = readResource(SDK_TEMPLATE_DIR + KAA_DEFAULTS + TEMPLATE_SUFFIX);
        objcSources.add(generateKaaDefaults(kaaDefaultsTemplate, bootstrapNodes, sdkToken,
                configurationProtocolSchemaBody, defaultConfigurationData));

        for (TarEntryData entryData : objcSources) {
            replacementData.put(entryData.getEntry().getName(), entryData);
        }

        ArchiveEntry e;
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

        String sdkFileName = SDK_PREFIX + sdkProperties.getToken() + ".tar.gz";

        byte[] sdkData = sdkOutput.toByteArray();

        FileData sdk = new FileData();
        sdk.setFileName(sdkFileName);
        sdk.setFileData(sdkData);
        return sdk;
    }

    private TarEntryData generateKaaDefaults(String template,
                                             List<BootstrapNodeInfo> bootstrapNodes,
                                             String sdkToken,
                                             String configurationProtocolSchemaBody,
                                             byte[] defaultConfigurationData) {
        LOG.debug("Generating kaa defaults");
        final int PROPERTIES_COUNT = 6;
        List<String> properties = new ArrayList<>(PROPERTIES_COUNT);
        properties.add(Version.PROJECT_VERSION);
        properties.add(Version.COMMIT_HASH);
        properties.add(sdkToken);
        properties.add(Base64.encodeBase64String(defaultConfigurationData));
        properties.add(Base64.encodeBase64String(configurationProtocolSchemaBody.getBytes()));
        properties.add(CommonSdkUtil.bootstrapNodesToString(bootstrapNodes));
        String source = String.format(template, properties.toArray(new Object[PROPERTIES_COUNT]));
        return CommonSdkUtil.tarEntryForSources(source, KAA_ROOT + KAA_DEFAULTS);
    }

    private List<TarEntryData> generateSourcesFromSchema(Schema schema, String sourceName) {
        LOG.debug("Generating source with name: " + sourceName);
        List<TarEntryData> tarEntries = new LinkedList<>();

        try (
                OutputStream headerStream = new ByteArrayOutputStream();
                OutputStream sourceStream = new ByteArrayOutputStream()
        ) {
            Compiler compiler = new ObjectiveCCompiler(schema, sourceName, headerStream, sourceStream);
            compiler.setNamespacePrefix(KAA_SOURCE_PREFIX);
            compiler.generate();

            tarEntries.add(CommonSdkUtil.tarEntryForSources(headerStream.toString(), KAA_GEN_FOLDER + sourceName + ".h"));
            tarEntries.add(CommonSdkUtil.tarEntryForSources(sourceStream.toString(), KAA_GEN_FOLDER + sourceName + ".m"));
        } catch (Exception e) {
            LOG.error("Failed to generate ObjectiveC sdk sources", e);
        }

        return tarEntries;
    }

}
