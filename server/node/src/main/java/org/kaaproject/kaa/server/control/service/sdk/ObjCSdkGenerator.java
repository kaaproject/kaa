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
import org.slf4j.helpers.MessageFormatter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.*;

public class ObjCSdkGenerator extends SdkGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ObjCSdkGenerator.class);

    private static final String SDK_DIR = "sdk/objc/";

    private static final String CF_DIR = SDK_DIR + "cf/";
    private static final String NF_DIR = SDK_DIR + "nf/";
    private static final String PRF_DIR = SDK_DIR + "profile/";
    private static final String LOG_DIR = SDK_DIR + "log/";
    private static final String EV_DIR = SDK_DIR + "event/";

    private static final String SDK_PREFIX = "kaa-client-sdk-";
    private static final String SDK_NAME_PATTERN = SDK_PREFIX + "p{}-c{}-n{}-l{}.tar.gz";

    private static final String KAA_ROOT_FOLDER = "Kaa/";

    private static final String KAA_GEN_FOLDER = KAA_ROOT_FOLDER + "gen/";

    private static final String KAA_SOURCE_PREFIX = "KAA";

    private static final String TEMPLATE_SUFFIX = ".template";


    private static final String NOTIFICATION_GEN = "NotificationGen";
    private static final String PROFILE_GEN = "ProfileGen";
    private static final String LOG_GEN = "LogGen";
    private static final String CONFIGURATION_GEN = "ConfigurationGen";


    private static final String KAA_DEFAULTS = "KaaDefaults.h";

    private static final String KAA_CLIENT = "KaaClient.h";

    private static final String BASE_KAA_CLIENT = "BaseKaaClient.%s";

    private static final String CONFIGURATION_COMMON = "ConfigurationCommon.h";

    private static final String CONFIGURATION_MANAGER_IMPL = "ResyncConfigurationManager.%s";

    private static final String CONFIGURATION_DESERIALIZER = "ConfigurationDeserializer.%s";

    private static final String NOTIFICATION_COMMON = "NotificationCommon.%s";

    private static final String PROFILE_COMMON = "ProfileCommon.%s";

    private static final String LOG_RECORD = "LogRecord.%s";

    private static final String LOG_COLLECTOR_INTERFACE = "LogCollector.h";

    private static final String LOG_COLLECTOR_SOURCE = "DefaultLogCollector.%s";

    private static final String USER_VERIFIER_CONSTANTS = "UserVerifierConstants.h";


    private static final String PROFILE_CLASS_VAR = "\\$\\{profile_class\\}";

    private static final String CONFIGURATION_CLASS_VAR = "\\$\\{configuration_class\\}";

    private static final String NOTIFICATION_CLASS_VAR = "\\$\\{notification_class\\}";

    private static final String LOG_RECORD_CLASS_VAR = "\\$\\{log_record_class\\}";

    private static final String DEFAULT_USER_VERIFIER_TOKEN_VAR = "\\$\\{default_user_verifier_token\\}";

    @Override
    public FileData generateSdk(String buildVersion,
                           List<BootstrapNodeInfo> bootstrapNodes,
                           String sdkToken, SdkProfileDto sdkProperties,
                           String profileSchemaBody,
                           String notificationSchemaBody,
                           String configurationProtocolSchemaBody,
                           String configurationBaseSchemaBody,
                           byte[] defaultConfigurationData,
                           List<EventFamilyMetadata> eventFamilies,
                           String logSchemaBody) throws Exception {

        Integer configurationSchemaVersion = sdkProperties.getConfigurationSchemaVersion();
        Integer profileSchemaVersion = sdkProperties.getProfileSchemaVersion();
        Integer notificationSchemaVersion = sdkProperties.getNotificationSchemaVersion();
        Integer logSchemaVersion = sdkProperties.getLogSchemaVersion();
        String defaultVerifierToken = sdkProperties.getDefaultVerifierToken();

        String sdkTemplateLocation = System.getProperty("server_home_dir") + "/" + SDK_DIR + SDK_PREFIX + buildVersion + ".tar.gz";

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

            String profileCommonHeader = readResource(String.format(PRF_DIR + PROFILE_COMMON + TEMPLATE_SUFFIX, "h"))
                    .replaceAll(PROFILE_CLASS_VAR, profileClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(profileCommonHeader,
                    KAA_ROOT_FOLDER + String.format(PROFILE_COMMON, "h")));
            String profileCommonSource = readResource(String.format(PRF_DIR + PROFILE_COMMON + TEMPLATE_SUFFIX, "m"))
                    .replaceAll(PROFILE_CLASS_VAR, profileClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(profileCommonSource,
                    KAA_ROOT_FOLDER + String.format(PROFILE_COMMON, "m")));

            objcSources.addAll(generateSourcesFromSchema(profileSchema, PROFILE_GEN));
        }

        String logClassName = "";
        if (StringUtils.isNotBlank(logSchemaBody)) {
            LOG.debug("Generating log schema");
            Schema logSchema = new Schema.Parser().parse(logSchemaBody);
            logClassName = KAA_SOURCE_PREFIX + logSchema.getName();

            String logRecordHeader = readResource(String.format(LOG_DIR + LOG_RECORD + TEMPLATE_SUFFIX, "h"))
                    .replaceAll(LOG_RECORD_CLASS_VAR, logClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(logRecordHeader,
                    KAA_ROOT_FOLDER + String.format(LOG_RECORD, "h")));
            String logRecordSource = readResource(String.format(LOG_DIR + LOG_RECORD + TEMPLATE_SUFFIX, "m"))
                    .replaceAll(LOG_RECORD_CLASS_VAR, logClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(logRecordSource,
                    KAA_ROOT_FOLDER + String.format(LOG_RECORD, "m")));

            String logCollector = readResource(LOG_DIR + LOG_COLLECTOR_INTERFACE + TEMPLATE_SUFFIX)
                    .replaceAll(LOG_RECORD_CLASS_VAR, logClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(logCollector, KAA_ROOT_FOLDER + LOG_COLLECTOR_INTERFACE));

            String logCollectorImplHeader = readResource(String.format(LOG_DIR + LOG_COLLECTOR_SOURCE + TEMPLATE_SUFFIX, "h"))
                    .replaceAll(LOG_RECORD_CLASS_VAR, logClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(logCollectorImplHeader,
                    KAA_ROOT_FOLDER + String.format(LOG_COLLECTOR_SOURCE, "h")));
            String logCollectorImplSource = readResource(String.format(LOG_DIR + LOG_COLLECTOR_SOURCE + TEMPLATE_SUFFIX, "m"))
                    .replaceAll(LOG_RECORD_CLASS_VAR, logClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(logCollectorImplSource,
                    KAA_ROOT_FOLDER + String.format(LOG_COLLECTOR_SOURCE, "m")));

            objcSources.addAll(generateSourcesFromSchema(logSchema, LOG_GEN));
        }

        String configurationClassName = "";
        if (StringUtils.isNotBlank(configurationBaseSchemaBody)) {
            LOG.debug("Generating configuration schema");
            Schema configurationSchema = new Schema.Parser().parse(configurationBaseSchemaBody);
            configurationClassName = KAA_SOURCE_PREFIX + configurationSchema.getName();

            String configurationCommon = readResource(CF_DIR + CONFIGURATION_COMMON + TEMPLATE_SUFFIX)
                    .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(configurationCommon, KAA_ROOT_FOLDER + CONFIGURATION_COMMON));

            String cfManagerImplHeader = readResource(String.format(CF_DIR + CONFIGURATION_MANAGER_IMPL + TEMPLATE_SUFFIX, "h"))
                    .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(cfManagerImplHeader,
                    KAA_ROOT_FOLDER + String.format(CONFIGURATION_MANAGER_IMPL, "h")));
            String cfManagerImplSource = readResource(String.format(CF_DIR + CONFIGURATION_MANAGER_IMPL + TEMPLATE_SUFFIX, "m"))
                    .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(cfManagerImplSource,
                    KAA_ROOT_FOLDER + String.format(CONFIGURATION_MANAGER_IMPL, "m")));

            String cfDeserializerHeader = readResource(String.format(CF_DIR + CONFIGURATION_DESERIALIZER + TEMPLATE_SUFFIX, "h"))
                    .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(cfDeserializerHeader,
                    KAA_ROOT_FOLDER + String.format(CONFIGURATION_DESERIALIZER, "h")));
            String cfDeserializerSource = readResource(String.format(CF_DIR + CONFIGURATION_DESERIALIZER + TEMPLATE_SUFFIX, "m"))
                    .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(cfDeserializerSource,
                    KAA_ROOT_FOLDER + String.format(CONFIGURATION_DESERIALIZER, "m")));

            objcSources.addAll(generateSourcesFromSchema(configurationSchema, CONFIGURATION_GEN));
        }

        if (StringUtils.isNotBlank(notificationSchemaBody)) {
            LOG.debug("Generating notification schema");
            Schema notificationSchema = new Schema.Parser().parse(notificationSchemaBody);
            String notificationClassName = KAA_SOURCE_PREFIX + notificationSchema.getName();

            String nfCommonHeader = readResource(String.format(NF_DIR + NOTIFICATION_COMMON + TEMPLATE_SUFFIX, "h"))
                    .replaceAll(NOTIFICATION_CLASS_VAR, notificationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(nfCommonHeader,
                    KAA_ROOT_FOLDER + String.format(NOTIFICATION_COMMON, "h")));
            String nfCommonSource = readResource(String.format(NF_DIR + NOTIFICATION_COMMON + TEMPLATE_SUFFIX, "m"))
                    .replaceAll(NOTIFICATION_CLASS_VAR, notificationClassName);
            objcSources.add(CommonSdkUtil.tarEntryForSources(nfCommonSource,
                    KAA_ROOT_FOLDER + String.format(NOTIFICATION_COMMON, "m")));

            objcSources.addAll(generateSourcesFromSchema(notificationSchema, NOTIFICATION_GEN));
        }

        if (eventFamilies != null && !eventFamilies.isEmpty()) {
            objcSources.addAll(ObjCEventClassesGenerator.generateEventSources(eventFamilies));
        }

        String kaaClient = readResource(SDK_DIR + KAA_CLIENT + TEMPLATE_SUFFIX)
                .replaceAll(LOG_RECORD_CLASS_VAR, logClassName)
                .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        objcSources.add(CommonSdkUtil.tarEntryForSources(kaaClient, KAA_ROOT_FOLDER + KAA_CLIENT));

        String baseKaaClientHeader = readResource(String.format(SDK_DIR + BASE_KAA_CLIENT + TEMPLATE_SUFFIX, "h"))
                .replaceAll(LOG_RECORD_CLASS_VAR, logClassName)
                .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        objcSources.add(CommonSdkUtil.tarEntryForSources(baseKaaClientHeader,
                KAA_ROOT_FOLDER + String.format(BASE_KAA_CLIENT, "h")));
        String baseKaaClientSource = readResource(String.format(SDK_DIR + BASE_KAA_CLIENT + TEMPLATE_SUFFIX, "m"))
                .replaceAll(LOG_RECORD_CLASS_VAR, logClassName)
                .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        objcSources.add(CommonSdkUtil.tarEntryForSources(baseKaaClientSource,
                KAA_ROOT_FOLDER + String.format(BASE_KAA_CLIENT, "m")));

        String tokenVerifier = defaultVerifierToken == null ? "nil" : "@\"" + defaultVerifierToken + "\"";
        String tokenVerifierSource = readResource(EV_DIR + USER_VERIFIER_CONSTANTS + TEMPLATE_SUFFIX)
                .replaceAll(DEFAULT_USER_VERIFIER_TOKEN_VAR, tokenVerifier);
        objcSources.add(CommonSdkUtil.tarEntryForSources(tokenVerifierSource, KAA_ROOT_FOLDER + USER_VERIFIER_CONSTANTS));

        String kaaDefaultsTemplate = readResource(SDK_DIR + KAA_DEFAULTS + TEMPLATE_SUFFIX);
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

        String sdkFileName = MessageFormatter.arrayFormat(SDK_NAME_PATTERN,
                new Object[]{profileSchemaVersion,
                        configurationSchemaVersion,
                        notificationSchemaVersion,
                        logSchemaVersion}).getMessage();

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
        final int PROPERTIES_COUNT = 6;
        List<String> properties = new ArrayList<>(PROPERTIES_COUNT);
        properties.add(Version.PROJECT_VERSION);
        properties.add(Version.COMMIT_HASH);
        properties.add(sdkToken);
        properties.add(Base64.encodeBase64String(defaultConfigurationData));
        properties.add(Base64.encodeBase64String(configurationProtocolSchemaBody.getBytes()));
        properties.add(CommonSdkUtil.bootstrapNodesToString(bootstrapNodes));
        String source = String.format(template, properties.toArray(new String[PROPERTIES_COUNT]));
        return CommonSdkUtil.tarEntryForSources(source, KAA_ROOT_FOLDER + KAA_DEFAULTS);
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
