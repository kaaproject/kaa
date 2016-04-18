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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.common.Environment;
import org.kaaproject.kaa.avro.avrogen.compiler.Compiler;
import org.kaaproject.kaa.avro.avrogen.compiler.CCompiler;
import org.kaaproject.kaa.avro.avrogen.StyleUtils;
import org.kaaproject.kaa.server.common.Version;
import org.kaaproject.kaa.server.common.zk.ServerNameUtil;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.control.service.sdk.compress.TarEntryData;
import org.kaaproject.kaa.server.control.service.sdk.event.CEventSourcesGenerator;
import org.kaaproject.kaa.server.control.service.sdk.event.EventFamilyMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSdkGenerator extends SdkGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(CppSdkGenerator.class);

    private static final String C_SDK_DIR    = "sdk/c";
    private static final String TEMPLATE_DIR = "sdk/c";
    /**
     * The KAA_SRC_FOLDER variable is also set in CMakeList.txt located in the root folder of the C SDK project.
     */
    private static final String KAA_SRC_FOLDER = "src/kaa";
    private static final String KAA_GEN_SOURCE_DIR = KAA_SRC_FOLDER + "/gen/";

    private static final String C_SDK_PREFIX       = "kaa-c-ep-sdk-";

    private static final String KAA_SOURCE_PREFIX = "kaa";

    private static final String C_HEADER_SUFFIX = ".h";
    private static final String C_SOURCE_SUFFIX = ".c";

    private static final String KAA_CMAKEGEN         = "listfiles/CMakeGen.cmake";
    private static final String KAA_DEFAULTS_HEADER  = KAA_SRC_FOLDER + "/kaa_defaults.h";

    private static final String LOGGING_HEADER       = KAA_GEN_SOURCE_DIR + "kaa_logging_definitions.h";
    private static final String PROFILE_HEADER       = KAA_GEN_SOURCE_DIR + "kaa_profile_definitions.h";
    private static final String CONFIGURATION_HEADER = KAA_GEN_SOURCE_DIR + "kaa_configuration_definitions.h";
    private static final String NOTIFICATION_HEADER  = KAA_GEN_SOURCE_DIR + "kaa_notification_definitions.h";

    private static final String PROFILE_SOURCE_NAME_PATTERN       = "kaa_profile_gen";
    private static final String LOGGING_SOURCE_NAME_PATTERN       = "kaa_logging_gen";
    private static final String CONFIGURATION_SOURCE_NAME_PATTERN = "kaa_configuration_gen";
    private static final String NOTIFICATION_SOURCE_NAME_PATTERN  = "kaa_notification_gen";

    private static final String PROFILE_NAMESPACE       = KAA_SOURCE_PREFIX + "_" + "profile";
    private static final String LOGGING_NAMESPACE       = KAA_SOURCE_PREFIX + "_" + "logging";
    private static final String CONFIGURATION_NAMESPACE = KAA_SOURCE_PREFIX + "_" + "configuration";
    private static final String NOTIFICATION_NAMESPACE  = KAA_SOURCE_PREFIX + "_" + "notification";

    private final VelocityEngine velocityEngine;

    public CSdkGenerator() {
        velocityEngine = new VelocityEngine();

        velocityEngine.addProperty("resource.loader", "class, file");
        velocityEngine.addProperty("class.resource.loader.class",
                                   "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.addProperty("file.resource.loader.class",
                                   "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        velocityEngine.addProperty("file.resource.loader.path", "/, .");
        velocityEngine.setProperty("runtime.references.strict", true);
        velocityEngine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
    }

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
                           String configurationBaseSchemaBody,
                           byte[] defaultConfigurationData,
                           List<EventFamilyMetadata> eventFamilies,
                           String logSchemaBody) throws Exception {

        String sdkToken = sdkProfile.getToken();
        Integer profileSchemaVersion = sdkProfile.getProfileSchemaVersion();
        String defaultVerifierToken = sdkProfile.getDefaultVerifierToken();

        String sdkTemplateLocation = Environment.getServerHomeDir() + "/" + C_SDK_DIR + "/" + C_SDK_PREFIX + buildVersion + ".tar.gz";

        LOG.debug("Lookup C SDK template: {}", sdkTemplateLocation);

        CompressorStreamFactory csf = new CompressorStreamFactory();
        ArchiveStreamFactory asf = new ArchiveStreamFactory();

        CompressorInputStream cis = csf.createCompressorInputStream(CompressorStreamFactory.GZIP,
                                                                    new FileInputStream(sdkTemplateLocation));

        ArchiveInputStream templateArchive = asf.createArchiveInputStream(ArchiveStreamFactory.TAR, cis);

        ByteArrayOutputStream sdkOutput = new ByteArrayOutputStream();
        CompressorOutputStream cos = csf.createCompressorOutputStream(CompressorStreamFactory.GZIP, sdkOutput);
        ArchiveOutputStream sdkFile = asf.createArchiveOutputStream(ArchiveStreamFactory.TAR, cos);

        Map<String, TarEntryData> replacementData = new HashMap<String, TarEntryData>();

        List<TarEntryData> cSources = new ArrayList<>();

        if (!StringUtils.isBlank(profileSchemaBody)) {
            cSources.addAll(generateProfileSources(profileSchemaBody));
        }

        if (!StringUtils.isBlank(logSchemaBody)) {
            cSources.addAll(generateLogSources(logSchemaBody));
        }

        if (!StringUtils.isBlank(configurationBaseSchemaBody)) {
            cSources.addAll(generateConfigurationSources(configurationBaseSchemaBody));
        }

        if (!StringUtils.isBlank(notificationSchemaBody)) {
            cSources.addAll(generateNotificationSources(notificationSchemaBody));
        }

        if (eventFamilies != null && !eventFamilies.isEmpty()) {
            cSources.addAll(CEventSourcesGenerator.generateEventSources(eventFamilies));
        }

        for (TarEntryData entryData : cSources) {
            replacementData.put(entryData.getEntry().getName(), entryData);
        }

        ArchiveEntry e = null;
        while ((e = templateArchive.getNextEntry()) != null) {
            if (!e.isDirectory()) {
                if (e.getName().equals(KAA_DEFAULTS_HEADER)) {
                    // TODO: eliminate schema versions and substitute them for a single sdkToken
                    byte[] kaaDefaultsData = generateKaaDefaults(bootstrapNodes, sdkToken,
                                                                 profileSchemaVersion,
                                                                 configurationProtocolSchemaBody,
                                                                 defaultConfigurationData,
                                                                 defaultVerifierToken);

                    TarArchiveEntry kaaDefaultsEntry = new TarArchiveEntry(KAA_DEFAULTS_HEADER);
                    kaaDefaultsEntry.setSize(kaaDefaultsData.length);
                    sdkFile.putArchiveEntry(kaaDefaultsEntry);
                    sdkFile.write(kaaDefaultsData);
                } else if (e.getName().equals(KAA_CMAKEGEN)) {
                    // Ignore duplicate source names
                    List<String> sourceNames = new LinkedList<>();
                    for (TarEntryData sourceEntry : cSources) {
                        String fileName = sourceEntry.getEntry().getName();
                        if (fileName.endsWith(C_SOURCE_SUFFIX) && !sourceNames.contains(fileName)) {
                            sourceNames.add(fileName);
                        }
                    }

                    VelocityContext context = new VelocityContext();
                    context.put("sourceNames", sourceNames);
                    String cSourceData = generateSourceFromTemplate(TEMPLATE_DIR + File.separator + "CMakeGen.vm", context);

                    TarArchiveEntry kaaCMakeEntry = new TarArchiveEntry(KAA_CMAKEGEN);
                    kaaCMakeEntry.setSize(cSourceData.length());
                    sdkFile.putArchiveEntry(kaaCMakeEntry);
                    sdkFile.write(cSourceData.getBytes());
                } else if (replacementData.containsKey(e.getName())) {
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

        String sdkFileName = C_SDK_PREFIX + sdkProfile.getToken() + ".tar.gz";

        FileData sdk = new FileData();
        sdk.setFileName(sdkFileName);
        sdk.setFileData(sdkOutput.toByteArray());

        return sdk;
    }

    private List<TarEntryData> generateSourcesFromSchema(Schema schema, String sourceName, String namespace) {
        List<TarEntryData> tarEntries = new LinkedList<>();

        try (
            OutputStream headerStream = new ByteArrayOutputStream();
            OutputStream sourceStream = new ByteArrayOutputStream();
        ) {
            Compiler compiler = new CCompiler(schema, sourceName, headerStream, sourceStream);
            compiler.setNamespacePrefix(namespace);
            compiler.generate();

            tarEntries.add(createTarEntry(KAA_GEN_SOURCE_DIR + sourceName + C_HEADER_SUFFIX, headerStream.toString()));
            tarEntries.add(createTarEntry(KAA_GEN_SOURCE_DIR + sourceName + C_SOURCE_SUFFIX, sourceStream.toString()));
        } catch (Exception e) {
            LOG.error("Failed to generate C sdk sources", e);
        }

        return tarEntries;
    }

    private String generateSourceFromTemplate(String templateFullName, VelocityContext context) {
        StringWriter writer = new StringWriter();
        velocityEngine.getTemplate(templateFullName).merge(context, writer);
        return writer.toString();
    }

    private String processHeaderTemplate(String templateName, Schema schema, String namespace) {
        VelocityContext context = new VelocityContext();
        context.put("record_name", StyleUtils.toLowerUnderScore(schema.getName()));
        context.put("namespace", namespace);
        return generateSourceFromTemplate(TEMPLATE_DIR + File.separator + templateName, context);
    }

    private TarEntryData createTarEntry(String tarEntryName, String data) {
        TarArchiveEntry entry = new TarArchiveEntry(tarEntryName);
        entry.setSize(data.length());
        return new TarEntryData(entry, data.getBytes());
    }

    /**
     * Generate client properties.
     *
     * @param bootstrapNodes the bootstrap nodes
     * @param sdkToken the sdk token
     * @param profileSchemaVersion the profile schema version
     * @param configurationProtocolSchemaBody the configuration protocol schema body
     * @param defaultConfigurationData the default configuration data
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private byte[] generateKaaDefaults(List<BootstrapNodeInfo> bootstrapNodes,
                                       String sdkToken,
                                       int profileSchemaVersion,
                                       String configurationProtocolSchemaBody,
                                       byte[] defaultConfigurationData,
                                       String defaultVerifierToken) throws IOException {

        VelocityContext context = new VelocityContext();

        LOG.debug("[sdk generateClientProperties] bootstrapNodes.size(): {}", bootstrapNodes.size());

        context.put("build_version", Version.PROJECT_VERSION);
        context.put("build_commit_hash", Version.COMMIT_HASH);
        context.put("sdk_token", sdkToken);
        
        context.put("profile_version", profileSchemaVersion);
        context.put("user_verifier_token", (defaultVerifierToken != null ? defaultVerifierToken : ""));
        
        context.put("bootstrapNodes", bootstrapNodes);
        context.put("configurationData", defaultConfigurationData);

        context.put("Base64", Base64.class);
        context.put("Integer", Integer.class);
        context.put("ServerNameUtil", ServerNameUtil.class);

        return generateSourceFromTemplate(TEMPLATE_DIR + File.separator + "kaa_defaults.hvm", context).getBytes();
    }

    private List<TarEntryData> generateProfileSources(String profileSchemaBody) {
        Schema schema = new Schema.Parser().parse(profileSchemaBody);
        List<TarEntryData> tarEntries = new LinkedList<>();

        tarEntries.add(createTarEntry(PROFILE_HEADER,
                                      processHeaderTemplate("kaa_profile_definitions.hvm", schema, PROFILE_NAMESPACE)));

        tarEntries.addAll(generateSourcesFromSchema(schema, PROFILE_SOURCE_NAME_PATTERN, PROFILE_NAMESPACE));

        return tarEntries;
    }

    private List<TarEntryData> generateLogSources(String logSchemaBody) {
        Schema schema = new Schema.Parser().parse(logSchemaBody);
        List<TarEntryData> tarEntries = new LinkedList<>();

        tarEntries.add(createTarEntry(LOGGING_HEADER,
                                      processHeaderTemplate("kaa_logging_definitions.hvm", schema, LOGGING_NAMESPACE)));

        tarEntries.addAll(generateSourcesFromSchema(schema, LOGGING_SOURCE_NAME_PATTERN, LOGGING_NAMESPACE));

        return tarEntries;
    }

    private List<TarEntryData> generateConfigurationSources(String configurationSchemaBody) {
        Schema schema = new Schema.Parser().parse(configurationSchemaBody);
        List<TarEntryData> tarEntries = new LinkedList<>();

        tarEntries.add(createTarEntry(CONFIGURATION_HEADER,
                                      processHeaderTemplate("kaa_configuration_definitions.hvm", schema, CONFIGURATION_NAMESPACE)));

        tarEntries.addAll(generateSourcesFromSchema(schema, CONFIGURATION_SOURCE_NAME_PATTERN, CONFIGURATION_NAMESPACE));

        return tarEntries;
    }

    private List<TarEntryData> generateNotificationSources(String notificationSchemaBody) {
        Schema schema = new Schema.Parser().parse(notificationSchemaBody);
        List<TarEntryData> tarEntries = new LinkedList<>();

        tarEntries.add(createTarEntry(NOTIFICATION_HEADER,
                                      processHeaderTemplate("kaa_notification_definitions.hvm", schema, NOTIFICATION_NAMESPACE)));

        tarEntries.addAll(generateSourcesFromSchema(schema, NOTIFICATION_SOURCE_NAME_PATTERN, NOTIFICATION_NAMESPACE));

        return tarEntries;
    }
}
