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

package org.kaaproject.kaa.server.control.service.sdk;

import java.io.ByteArrayOutputStream;
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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.kaaproject.kaa.avro.avrogenc.Compiler;
import org.kaaproject.kaa.avro.avrogenc.StyleUtils;
import org.kaaproject.kaa.server.common.thrift.gen.control.Sdk;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpLpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel;
import org.kaaproject.kaa.server.control.service.sdk.compress.TarEntryData;
import org.kaaproject.kaa.server.control.service.sdk.event.CEventSourcesGenerator;
import org.kaaproject.kaa.server.control.service.sdk.event.EventFamilyMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

public class CSdkGenerator extends SdkGenerator {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(CppSdkGenerator.class);

    /** The Constant CPP_SDK_DIR. */
    private static final String C_SDK_DIR = "sdk/c";

    /** The Constant CPP_SDK_PREFIX. */
    private static final String C_SDK_PREFIX = "kaa-client-sdk-";

    /** The Constant CPP_SDK_NAME_PATTERN. */
    private static final String C_SDK_NAME_PATTERN = C_SDK_PREFIX + "p{}-c{}-n{}-l{}.tar.gz";

    private static final String C_HEADER_SUFFIX = ".h";

    private static final String C_SOURCE_SUFFIX = ".c";

    /** The Constant KAA_DEFAULTS_CPP. */
    private static final String KAA_DEFAULTS_HPP = "src/kaa_defaults.h";

    /** The Constant CMakeLists.txt. */
    private static final String KAA_CMAKELISTS_TXT = "CMakeLists.txt";

    private static final String KAA_CMAKELISTS_TEMPLATE = "sdk/c/CMakeLists.vm";

    /** The Constant PROFILE_SCHEMA_AVRO_SRC. */
    private static final String PROFILE_HDR = "src/kaa_profile.h";

    private static final String KAA_GEN_SOURCE_DIR = "src/gen/";

    private static final String KAA_PROFILE_SOURCE_NAME_PATTERN = "kaa_profile_gen";

    private static final String NAME_PREFIX_TEMPLATE = "kaa_{name}";

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
    public Sdk generateSdk(String buildVersion,
            List<BootstrapNodeInfo> bootstrapNodes, String appToken,
            int profileSchemaVersion, int configurationSchemaVersion,
            int notificationSchemaVersion, int logSchemaVersion,
            String profileSchemaBody,
            String notificationSchemaBody,
            String configurationProtocolSchemaBody,
            byte[] defaultConfigurationData,
            List<EventFamilyMetadata> eventFamilies,
            String logSchemaBody) throws Exception {

        String sdkTemplateLocation = System.getProperty("server_home_dir") + "/" + C_SDK_DIR + "/" + C_SDK_PREFIX + buildVersion + ".tar.gz";

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
        cSources.addAll(generateProfileSources(profileSchemaBody));

        if (eventFamilies != null && !eventFamilies.isEmpty()) {
            cSources.addAll(CEventSourcesGenerator.generateEventSources(eventFamilies));
        }

        for (TarEntryData entryData : cSources) {
            replacementData.put(entryData.getEntry().getName(), entryData);
        }

        ArchiveEntry e = null;
        while ((e = templateArchive.getNextEntry()) != null) {
            if (!e.isDirectory()) {
                if (e.getName().equals(KAA_DEFAULTS_HPP)) {
                    TarArchiveEntry kaaDefaultsEntry = new TarArchiveEntry(
                            KAA_DEFAULTS_HPP);
                    byte[] kaaDefaultsData = generateKaaDefaults(
                            bootstrapNodes, appToken,
                            configurationSchemaVersion, profileSchemaVersion,
                            notificationSchemaVersion, logSchemaVersion,
                            configurationProtocolSchemaBody,
                            defaultConfigurationData,
                            eventFamilies);
                    kaaDefaultsEntry.setSize(kaaDefaultsData.length);
                    sdkFile.putArchiveEntry(kaaDefaultsEntry);
                    sdkFile.write(kaaDefaultsData);
                } else if (e.getName().equals(KAA_CMAKELISTS_TXT)) {
                    TarArchiveEntry kaaCMakeEntry = new TarArchiveEntry(
                            KAA_CMAKELISTS_TXT);

                    List<String> sourceNames = new LinkedList<>();
                    for (TarEntryData sourceEntry : cSources) {
                        String fileName = sourceEntry.getEntry().getName();
                        if (fileName.endsWith(C_SOURCE_SUFFIX)) {
                            sourceNames.add(fileName);
                        }
                    }

                    VelocityContext context = new VelocityContext();
                    context.put("sourceNames", sourceNames);

                    StringWriter cSourceWriter = new StringWriter();
                    velocityEngine.getTemplate(KAA_CMAKELISTS_TEMPLATE).merge(context, cSourceWriter);
                    String cSourceData = cSourceWriter.toString();

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

        String sdkFileName = MessageFormatter.arrayFormat(C_SDK_NAME_PATTERN,
                new Object[]{profileSchemaVersion,
                configurationSchemaVersion,
                notificationSchemaVersion,
                logSchemaVersion}).getMessage();

        byte[] sdkData = sdkOutput.toByteArray();

        Sdk sdk = new Sdk();
        sdk.setFileName(sdkFileName);
        sdk.setData(sdkData);
        return sdk;
    }

    /**
     * Generate client properties.
     *
     * @param kaaDefaultsStream the kaa defaults stream
     * @param bootstrapNodes the bootstrap nodes
     * @param appToken the app token
     * @param configurationSchemaVersion the configuration schema version
     * @param profileSchemaVersion the profile schema version
     * @param notificationSchemaVersion the notification schema version
     * @param logSchemaVersion the log schema version
     * @param configurationProtocolSchemaBody the configuration protocol schema body
     * @param defaultConfigurationData the default configuration data
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private byte[] generateKaaDefaults(
            List<BootstrapNodeInfo> bootstrapNodes,
            String appToken,
            int configurationSchemaVersion,
            int profileSchemaVersion,
            int notificationSchemaVersion,
            int logSchemaVersion,
            String configurationProtocolSchemaBody,
            byte[] defaultConfigurationData,
            List<EventFamilyMetadata> eventFamilies) throws IOException {

        VelocityContext context = new VelocityContext();

        LOG.debug("[sdk generateClientProperties] bootstrapNodes.size(): {}", bootstrapNodes.size());

        context.put("app_token", appToken);
        context.put("config_version", configurationSchemaVersion);
        context.put("profile_version", profileSchemaVersion);
        context.put("user_nf_version", notificationSchemaVersion);
        context.put("log_version", logSchemaVersion);
        context.put("system_nf_version", 1);

        context.put("eventFamilies", eventFamilies);
        context.put("bootstrapNodes", bootstrapNodes);

        context.put("Base64", Base64.class);
        context.put("Utils", CSdkGenerator.class);

        StringWriter writer = new StringWriter();
        velocityEngine.getTemplate("sdk/c/kaa_defaults.vm").merge(context, writer);

        return writer.toString().getBytes();
    }

    static public IpComunicationParameters getIPParameters(ZkSupportedChannel channel) {
        IpComunicationParameters params = null;

        switch (channel.getChannelType()) {
        case HTTP:
            params = ((ZkHttpComunicationParameters)channel.
                    getCommunicationParameters()).getZkComunicationParameters();
            break;
        case HTTP_LP:
            params = ((ZkHttpLpComunicationParameters)channel.
                    getCommunicationParameters()).getZkComunicationParameters();
            break;
        case KAATCP:
            params = ((ZkKaaTcpComunicationParameters)channel.
                    getCommunicationParameters()).getZkComunicationParameters();
            break;
        default:
            break;
        }

        return params;
    }

    private List<TarEntryData> generateProfileSources(String profileSchemaBody) {
        List<TarEntryData> tarEntries = new LinkedList<>();

        TarArchiveEntry entry = new TarArchiveEntry(PROFILE_HDR);
        Schema profileSchema = new Schema.Parser().parse(profileSchemaBody);

        VelocityContext profileContext = new VelocityContext();
        profileContext.put("profileName", StyleUtils.toLowerUnderScore(profileSchema.getName()));

        StringWriter profileWriter = new StringWriter();
        velocityEngine.getTemplate("sdk/c/kaa_profile.vm").merge(profileContext, profileWriter);

        entry.setSize(profileWriter.toString().length());
        TarEntryData tarEntry = new TarEntryData(entry, profileWriter.toString().getBytes());
        tarEntries.add(tarEntry);

        OutputStream hdrStream = new ByteArrayOutputStream();
        OutputStream srcStream = new ByteArrayOutputStream();

        try {
            Compiler compiler = new Compiler(profileSchema, KAA_PROFILE_SOURCE_NAME_PATTERN, hdrStream, srcStream);
            compiler.setNamespacePrefix(NAME_PREFIX_TEMPLATE.replace("{name}", "profile"));
            compiler.generate();

            String profileData = hdrStream.toString();

            entry = new TarArchiveEntry(KAA_GEN_SOURCE_DIR + KAA_PROFILE_SOURCE_NAME_PATTERN + C_HEADER_SUFFIX);
            entry.setSize(profileData.length());
            tarEntry = new TarEntryData(entry, profileData.getBytes());
            tarEntries.add(tarEntry);

            entry = new TarArchiveEntry(KAA_GEN_SOURCE_DIR + KAA_PROFILE_SOURCE_NAME_PATTERN + C_SOURCE_SUFFIX);
            profileData = srcStream.toString();
            entry.setSize(profileData.length());
            tarEntry = new TarEntryData(entry, profileData.getBytes());
            tarEntries.add(tarEntry);
        } catch (Exception e) {
            //TODO
        }

        return tarEntries;
    }
}
