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
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.tools.JavaFileObject.Kind;

import org.apache.avro.Schema;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.compiler.specific.SpecificCompiler.FieldVisibility;
import org.apache.avro.generic.GenericData.StringType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.common.Environment;
import org.kaaproject.kaa.server.common.Version;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicBean;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicCompiler;
import org.kaaproject.kaa.server.control.service.sdk.compress.ZipEntryData;
import org.kaaproject.kaa.server.control.service.sdk.event.EventFamilyMetadata;
import org.kaaproject.kaa.server.control.service.sdk.event.JavaEventClassesGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JavaSdkGenerator extends SdkGenerator {


    private static final Logger LOG = LoggerFactory.getLogger(JavaSdkGenerator.class);


    private static final String JAVA_SDK_DIR = "sdk/java";


    private static final String JAVA_SDK_PREFIX = "kaa-java-ep-sdk-";


    private static final String ANDROID_SDK_DIR = "sdk/android";


    private static final String ANDROID_SDK_PREFIX = "kaa-android-ep-sdk-";


    private static final String CLIENT_PROPERTIES = "client.properties";


    private static final String BUILD_VERSION = "build.version";


    private static final String BUILD_COMMIT_HASH = "build.commit_hash";


    private static final String BOOTSTRAP_SERVERS_PROPERTY = "transport.bootstrap.servers";


    private static final String SDK_TOKEN_PROPERTY = "sdk_token";


    private static final String CONFIG_DATA_DEFAULT_PROPERTY = "config.data.default";


    private static final String CONFIG_SCHEMA_DEFAULT_PROPERTY = "config.schema.default";


    private static final String KAA_CLIENT_SOURCE_TEMPLATE = "sdk/java/KaaClient.java.template";


    private static final String BASE_KAA_CLIENT_SOURCE_TEMPLATE = "sdk/java/BaseKaaClient.java.template";


    private static final String CONFIGURATION_MANAGER_SOURCE_TEMPLATE = "sdk/java/cf/ConfigurationManager.java.template";


    private static final String CONFIGURATION_MANAGER_IMPL_SOURCE_TEMPLATE = "sdk/java/cf/ResyncConfigurationManager.java.template";


    private static final String CONFIGURATION_LISTENER_SOURCE_TEMPLATE = "sdk/java/cf/ConfigurationListener.java.template";


    private static final String CONFIGURATION_DESERIALIZER_SOURCE_TEMPLATE = "sdk/java/cf/ConfigurationDeserializer.java.template";


    private static final String NOTIFICATION_LISTENER_SOURCE_TEMPLATE = "sdk/java/nf/NotificationListener.java.template";


    private static final String NOTIFICATION_DESERIALIZER_SOURCE_TEMPLATE = "sdk/java/nf/NotificationDeserializer.java.template";


    private static final String PROFILE_CONTAINER_SOURCE_TEMPLATE = "sdk/java/profile/ProfileContainer.java.template";


    private static final String PROFILE_SERIALIZER_SOURCE_TEMPLATE = "sdk/java/profile/ProfileSerializer.java.template";


    private static final String DEFAULT_PROFILE_SERIALIZER_SOURCE_TEMPLATE = "sdk/java/profile/DefaultProfileSerializer.java.template";


    private static final String LOG_RECORD_SOURCE_TEMPLATE = "sdk/java/log/LogRecord.java.template";


    private static final String LOG_COLLECTOR_INTERFACE_TEMPLATE = "sdk/java/log/LogCollector.java.template";


    private static final String LOG_COLLECTOR_SOURCE_TEMPLATE = "sdk/java/log/DefaultLogCollector.java.template";


    private static final String USER_VERIFIER_CONSTANTS_SOURCE_TEMPLATE = "sdk/java/event/UserVerifierConstants.java.template";


    private static final String PROFILE_CONTAINER = "ProfileContainer";


    private static final String PROFILE_SERIALIZER = "ProfileSerializer";


    private static final String CONFIGURATION_MANAGER = "ConfigurationManager";


    private static final String CONFIGURATION_MANAGER_IMPL = "ResyncConfigurationManager";


    private static final String CONFIGURATION_LISTENER = "ConfigurationListener";


    private static final String CONFIGURATION_DESERIALIZER = "ConfigurationDeserializer";


    private static final String NOTIFICATION_LISTENER = "NotificationListener";


    private static final String NOTIFICATION_DESERIALIZER = "NotificationDeserializer";


    private static final String USER_VERIFIER_CONSTANTS = "UserVerifierConstants";


    private static final int DEFAULT_SCHEMA_VERSION = 1;
    

    private static final int DEFAULT_PROFILE_SCHEMA_VERSION = 0;


    private static final String KAA_CLIENT = "KaaClient";


    private static final String BASE_KAA_CLIENT = "BaseKaaClient";


    private static final String LOG_RECORD = "LogRecord";


    private static final String LOG_COLLECTOR_INTERFACE = "LogCollector";


    private static final String LOG_COLLECTOR_SOURCE = "DefaultLogCollector";


    private static final String PROFILE_CLASS_PACKAGE_VAR = "\\$\\{profile_class_package\\}";


    private static final String PROFILE_CLASS_VAR = "\\$\\{profile_class\\}";


    private static final String CONFIGURATION_CLASS_PACKAGE_VAR = "\\$\\{configuration_class_package\\}";


    private static final String CONFIGURATION_CLASS_VAR = "\\$\\{configuration_class\\}";


    private static final String NOTIFICATION_CLASS_PACKAGE_VAR = "\\$\\{notification_class_package\\}";


    private static final String NOTIFICATION_CLASS_VAR = "\\$\\{notification_class\\}";


    private static final String LOG_RECORD_CLASS_PACKAGE_VAR = "\\$\\{log_record_class_package\\}";


    private static final String LOG_RECORD_CLASS_VAR = "\\$\\{log_record_class\\}";


    private static final String DEFAULT_USER_VERIFIER_TOKEN_VAR = "\\$\\{default_user_verifier_token\\}";
    

    private static final String JAVA_SOURCE_COMPILER_RELEASE = "7";

    private static final String JAVA_TARGET_COMPILER_RELEASE = "7";


    private static final SecureRandom RANDOM = new SecureRandom();

    private final SdkPlatform sdkPlatform;

    public JavaSdkGenerator(SdkPlatform sdkPlatform) {
        this.sdkPlatform = sdkPlatform;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.sdk.SdkGenerator#generateSdk
     * (java.lang.String, java.util.List, java.lang.String, int, int, int,
     * java.lang.String, java.lang.String, java.lang.String, byte[],
     * java.util.List)
     */
    @Override
    public FileData generateSdk(String buildVersion, List<BootstrapNodeInfo> bootstrapNodes, SdkProfileDto sdkProfile,
                           String profileSchemaBody, String notificationSchemaBody, String configurationProtocolSchemaBody,
                           String configurationSchemaBody, byte[] defaultConfigurationData, List<EventFamilyMetadata> eventFamilies,
                           String logSchemaBody)
            throws Exception {

        String sdkToken = sdkProfile.getToken();
        Integer profileSchemaVersion = sdkProfile.getProfileSchemaVersion();
        Integer notificationSchemaVersion = sdkProfile.getNotificationSchemaVersion();
        Integer logSchemaVersion = sdkProfile.getLogSchemaVersion();
        String defaultVerifierToken = sdkProfile.getDefaultVerifierToken();

        Schema configurationSchema = new Schema.Parser().parse(configurationSchemaBody);
        Schema profileSchema = new Schema.Parser().parse(profileSchemaBody);
        Schema notificationSchema = new Schema.Parser().parse(notificationSchemaBody);
        Schema logSchema = new Schema.Parser().parse(logSchemaBody);

        List<Schema> eventFamilySchemas = new LinkedList<>();
        if (eventFamilies != null && !eventFamilies.isEmpty()) {
            for (EventFamilyMetadata eventFamily : eventFamilies) {
                Schema eventFamilySchema = new Schema.Parser().parse(eventFamily.getEcfSchema());
                eventFamilySchemas.add(eventFamilySchema);
            }
        }

        List<Schema> schemasToCheck = new LinkedList<>();
        schemasToCheck.add(configurationSchema);
        schemasToCheck.add(profileSchema);
        schemasToCheck.add(notificationSchema);
        schemasToCheck.add(logSchema);
        schemasToCheck.addAll(eventFamilySchemas);

        Map<String, Schema> uniqueSchemasMap = SchemaUtil.getUniqueSchemasMap(schemasToCheck);

        String sdkTemplateLocation;
        if (sdkPlatform == SdkPlatform.JAVA) {
            sdkTemplateLocation = Environment.getServerHomeDir() + "/" + JAVA_SDK_DIR + "/" + JAVA_SDK_PREFIX + buildVersion
                    + ".jar";
            LOG.debug("Lookup Java SDK template: {}", sdkTemplateLocation);
        } else { // ANDROID
            sdkTemplateLocation = Environment.getServerHomeDir() + "/" + ANDROID_SDK_DIR + "/" + ANDROID_SDK_PREFIX + buildVersion
                    + ".jar";
            LOG.debug("Lookup Android SDK template: {}", sdkTemplateLocation);
        }

        File sdkTemplateFile = new File(sdkTemplateLocation);
        ZipFile templateArhive = new ZipFile(sdkTemplateFile);

        Map<String, ZipEntryData> replacementData = new HashMap<String, ZipEntryData>();

        ZipEntry clientPropertiesEntry = templateArhive.getEntry(CLIENT_PROPERTIES);
        byte[] clientPropertiesData = generateClientProperties(templateArhive.getInputStream(clientPropertiesEntry), bootstrapNodes,
                sdkToken, configurationProtocolSchemaBody, defaultConfigurationData);

        replacementData.put(CLIENT_PROPERTIES, new ZipEntryData(new ZipEntry(CLIENT_PROPERTIES), clientPropertiesData));

        List<JavaDynamicBean> javaSources = new ArrayList<JavaDynamicBean>();

        String configurationClassName = configurationSchema.getName();
        String configurationClassPackage = configurationSchema.getNamespace();

        javaSources.addAll(generateSchemaSources(configurationSchema, uniqueSchemasMap));

        String configurationManagerImplTemplate = readResource(CONFIGURATION_MANAGER_IMPL_SOURCE_TEMPLATE);
        String configurationManagerImplSource = configurationManagerImplTemplate.replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR,
                configurationClassPackage).replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);

        JavaDynamicBean configurationManagerImplClassBean = new JavaDynamicBean(CONFIGURATION_MANAGER_IMPL, configurationManagerImplSource);
        javaSources.add(configurationManagerImplClassBean);

        String configurationManagerTemplate = readResource(CONFIGURATION_MANAGER_SOURCE_TEMPLATE);
        String configurationManagerSource = configurationManagerTemplate.replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR,
                configurationClassPackage).replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);

        JavaDynamicBean configurationManagerClassBean = new JavaDynamicBean(CONFIGURATION_MANAGER, configurationManagerSource);
        javaSources.add(configurationManagerClassBean);

        String configurationListenerTemplate = readResource(CONFIGURATION_LISTENER_SOURCE_TEMPLATE);
        String configurationListenerSource = configurationListenerTemplate.replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR,
                configurationClassPackage).replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);

        JavaDynamicBean configurationListenerClassBean = new JavaDynamicBean(CONFIGURATION_LISTENER, configurationListenerSource);
        javaSources.add(configurationListenerClassBean);


        String configurationDeserializerSourceTemplate = readResource(CONFIGURATION_DESERIALIZER_SOURCE_TEMPLATE);
        String configurationDeserializerSource = configurationDeserializerSourceTemplate.replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR,
                configurationClassPackage).replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);

        JavaDynamicBean configurationDeserializerClassBean = new JavaDynamicBean(CONFIGURATION_DESERIALIZER,
                configurationDeserializerSource);
        javaSources.add(configurationDeserializerClassBean);

        String profileClassName = profileSchema.getName();
        String profileClassPackage = profileSchema.getNamespace();

        if (profileSchemaVersion != DEFAULT_PROFILE_SCHEMA_VERSION) {
            javaSources.addAll(generateSchemaSources(profileSchema, uniqueSchemasMap));
        }

        String profileContainerTemplate = readResource(PROFILE_CONTAINER_SOURCE_TEMPLATE);
        String profileContainerSource = profileContainerTemplate.replaceAll(PROFILE_CLASS_PACKAGE_VAR, profileClassPackage).replaceAll(
                PROFILE_CLASS_VAR, profileClassName);
        JavaDynamicBean profileContainerClassBean = new JavaDynamicBean(PROFILE_CONTAINER, profileContainerSource);
        javaSources.add(profileContainerClassBean);

        String profileSerializerTemplate;
        if (profileSchemaVersion == DEFAULT_PROFILE_SCHEMA_VERSION) {
            profileSerializerTemplate = readResource(DEFAULT_PROFILE_SERIALIZER_SOURCE_TEMPLATE);
        } else {
            profileSerializerTemplate = readResource(PROFILE_SERIALIZER_SOURCE_TEMPLATE);
        }
        String profileSerializerSource = profileSerializerTemplate.replaceAll(PROFILE_CLASS_PACKAGE_VAR, profileClassPackage).replaceAll(
                PROFILE_CLASS_VAR, profileClassName);
        JavaDynamicBean profileSerializerClassBean = new JavaDynamicBean(PROFILE_SERIALIZER, profileSerializerSource);
        javaSources.add(profileSerializerClassBean);

        String notificationClassName = notificationSchema.getName();
        String notificationClassPackage = notificationSchema.getNamespace();

        if (notificationSchemaVersion != DEFAULT_SCHEMA_VERSION) {
            javaSources.addAll(generateSchemaSources(notificationSchema, uniqueSchemasMap));
        }

        String notificationListenerTemplate = readResource(NOTIFICATION_LISTENER_SOURCE_TEMPLATE);
        String notificationListenerSource = notificationListenerTemplate.replaceAll(NOTIFICATION_CLASS_PACKAGE_VAR,
                notificationClassPackage).replaceAll(NOTIFICATION_CLASS_VAR, notificationClassName);

        JavaDynamicBean notificationListenerClassBean = new JavaDynamicBean(NOTIFICATION_LISTENER, notificationListenerSource);
        javaSources.add(notificationListenerClassBean);

        String notificationDeserializerSourceTemplate = readResource(NOTIFICATION_DESERIALIZER_SOURCE_TEMPLATE);
        String notificationDeserializerSource = notificationDeserializerSourceTemplate.replaceAll(NOTIFICATION_CLASS_PACKAGE_VAR,
                notificationClassPackage).replaceAll(NOTIFICATION_CLASS_VAR, notificationClassName);

        JavaDynamicBean notificationDeserializerClassBean = new JavaDynamicBean(NOTIFICATION_DESERIALIZER, notificationDeserializerSource);
        javaSources.add(notificationDeserializerClassBean);

        if (logSchemaVersion != DEFAULT_SCHEMA_VERSION) {
            javaSources.addAll(generateSchemaSources(logSchema, uniqueSchemasMap));
        }

        String logRecordTemplate = readResource(LOG_RECORD_SOURCE_TEMPLATE);
        String logRecordSource = logRecordTemplate.replaceAll(LOG_RECORD_CLASS_PACKAGE_VAR, logSchema.getNamespace()).replaceAll(
                LOG_RECORD_CLASS_VAR, logSchema.getName());

        String logCollectorInterfaceTemplate = readResource(LOG_COLLECTOR_INTERFACE_TEMPLATE);
        String logCollectorInterface = logCollectorInterfaceTemplate.replaceAll(LOG_RECORD_CLASS_PACKAGE_VAR, logSchema.getNamespace())
                .replaceAll(LOG_RECORD_CLASS_VAR, logSchema.getName());

        String logCollectorSourceTemplate = readResource(LOG_COLLECTOR_SOURCE_TEMPLATE);
        String logCollectorSource = logCollectorSourceTemplate.replaceAll(LOG_RECORD_CLASS_PACKAGE_VAR, logSchema.getNamespace())
                .replaceAll(LOG_RECORD_CLASS_VAR, logSchema.getName());

        JavaDynamicBean logRecordClassBean = new JavaDynamicBean(LOG_RECORD, logRecordSource);
        JavaDynamicBean logCollectorInterfaceClassBean = new JavaDynamicBean(LOG_COLLECTOR_INTERFACE, logCollectorInterface);
        JavaDynamicBean logCollectorSourceClassBean = new JavaDynamicBean(LOG_COLLECTOR_SOURCE, logCollectorSource);

        javaSources.add(logRecordClassBean);
        javaSources.add(logCollectorInterfaceClassBean);
        javaSources.add(logCollectorSourceClassBean);

        if (eventFamilies != null && !eventFamilies.isEmpty()) {
            for (Schema eventFamilySchema : eventFamilySchemas) {
                javaSources.addAll(generateSchemaSources(eventFamilySchema, uniqueSchemasMap));
            }
            javaSources.addAll(JavaEventClassesGenerator.generateEventClasses(eventFamilies));
        }

        String userVerifierConstantsTemplate = readResource(USER_VERIFIER_CONSTANTS_SOURCE_TEMPLATE);
        if (defaultVerifierToken == null) {
            defaultVerifierToken = "null";
        } else {
            defaultVerifierToken = "\"" + defaultVerifierToken + "\"";
        }
        String userVerifierConstantsSource = userVerifierConstantsTemplate
                .replaceAll(DEFAULT_USER_VERIFIER_TOKEN_VAR, defaultVerifierToken);

        JavaDynamicBean userVerifierConstantsClassBean = new JavaDynamicBean(USER_VERIFIER_CONSTANTS, userVerifierConstantsSource);
        javaSources.add(userVerifierConstantsClassBean);

        String kaaClientTemplate = readResource(KAA_CLIENT_SOURCE_TEMPLATE);
        String kaaClientSource = kaaClientTemplate.replaceAll(LOG_RECORD_CLASS_PACKAGE_VAR, logSchema.getNamespace())
                .replaceAll(LOG_RECORD_CLASS_VAR, logSchema.getName())
                .replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR, configurationClassPackage)
                .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        JavaDynamicBean kaaClientClassBean = new JavaDynamicBean(KAA_CLIENT, kaaClientSource);
        javaSources.add(kaaClientClassBean);

        String baseKaaClientTemplate = readResource(BASE_KAA_CLIENT_SOURCE_TEMPLATE);
        String baseKaaClientSource = baseKaaClientTemplate.replaceAll(LOG_RECORD_CLASS_PACKAGE_VAR, logSchema.getNamespace())
                .replaceAll(LOG_RECORD_CLASS_VAR, logSchema.getName())
                .replaceAll(CONFIGURATION_CLASS_PACKAGE_VAR, configurationClassPackage)
                .replaceAll(CONFIGURATION_CLASS_VAR, configurationClassName);
        JavaDynamicBean baseKaaClientClassBean = new JavaDynamicBean(BASE_KAA_CLIENT, baseKaaClientSource);
        javaSources.add(baseKaaClientClassBean);

        packageSources(javaSources, replacementData);

        ByteArrayOutputStream sdkOutput = new ByteArrayOutputStream();
        ZipOutputStream sdkFile = new ZipOutputStream(sdkOutput);

        Enumeration<? extends ZipEntry> entries = templateArhive.entries();

        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (replacementData.containsKey(e.getName())) {
                ZipEntryData replacementEntry = replacementData.remove(e.getName());
                sdkFile.putNextEntry(replacementEntry.getEntry());
                sdkFile.write(replacementEntry.getData());
            } else {
                sdkFile.putNextEntry(e);
                if (!e.isDirectory()) {
                    IOUtils.copy(templateArhive.getInputStream(e), sdkFile);
                }
            }
            sdkFile.closeEntry();
        }
        templateArhive.close();

        for (String entryName : replacementData.keySet()) {
            ZipEntryData replacementEntry = replacementData.get(entryName);
            sdkFile.putNextEntry(replacementEntry.getEntry());
            sdkFile.write(replacementEntry.getData());
            sdkFile.closeEntry();
        }

        sdkFile.close();

        String fileNamePrefix = (sdkPlatform == SdkPlatform.JAVA ? JAVA_SDK_PREFIX : ANDROID_SDK_PREFIX);
        String sdkFileName = fileNamePrefix + sdkProfile.getToken() + ".jar";

        byte[] sdkData = sdkOutput.toByteArray();

        FileData sdk = new FileData();
        sdk.setFileName(sdkFileName);
        sdk.setFileData(sdkData);
        return sdk;
    }

    /**
     * Generate schema class.
     *
     * @param   schema          the schema
     * @param   uniqueSchemas   the unique schemas
     * @return  the list
     * @throws  IOException Signals that an I/O exception has occurred.
     */
    public static List<JavaDynamicBean> generateSchemaSources(Schema schema, Map<String, Schema> uniqueSchemas) throws IOException {
        SpecificCompiler compiler = new SpecificCompiler(schema);
        compiler.setStringType(StringType.String);
        compiler.setFieldVisibility(FieldVisibility.PRIVATE);

        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        long n = RANDOM.nextLong();
        if (n == Long.MIN_VALUE) {
            // corner case
            n = 0;
        } else {
            n = Math.abs(n);
        }
        File tmpOutputDir = new File(tmpdir, "tmp-gen-" + Long.toString(n));
        tmpOutputDir.mkdirs();

        compiler.compileToDestination(null, tmpOutputDir);

        List<JavaDynamicBean> sources = getJavaSources(tmpOutputDir, uniqueSchemas);

        tmpOutputDir.delete();

        return sources;
    }

    /**
     * Package sources.
     *
     * @param javaSources the java sources
     * @param data        the data
     */
    private void packageSources(List<JavaDynamicBean> javaSources, Map<String, ZipEntryData> data) {
        JavaDynamicCompiler dynamicCompiler = new JavaDynamicCompiler();
        dynamicCompiler.init();
        for (JavaDynamicBean bean : javaSources) {
            LOG.debug("Compiling bean [{}]...", bean.getName());
            LOG.trace("Bean source:\n{}", bean.getCharContent(true));
            Stream<String> sourceLines = Arrays.stream(bean.getCharContent(true).split("\n"));
            String packageLine = sourceLines.filter(line -> line.startsWith("package")).findFirst().orElse("");
            String sourceFileName = packageLine.replaceAll("package", "").replaceAll("\\.|;", "/").trim() + bean.getName();
            data.put(sourceFileName, new ZipEntryData(new ZipEntry(sourceFileName), bean.getCharContent(true).getBytes()));
        }
        Collection<JavaDynamicBean> compiledObjects = dynamicCompiler.compile(javaSources, 
                "-source", JAVA_SOURCE_COMPILER_RELEASE,
                "-target", JAVA_TARGET_COMPILER_RELEASE);
        for (JavaDynamicBean compiledObject : compiledObjects) {
            String className = compiledObject.getName();
            String classFileName = className.replace('.', '/') + Kind.CLASS.extension;
            ZipEntry classFile = new ZipEntry(classFileName);
            ZipEntryData zipEntryData = new ZipEntryData(classFile, compiledObject.getBytes());
            data.put(classFileName, zipEntryData);
        }
    }

    /**
     * Gets the java sources.
     *
     * @param srcDir the src dir
     * @return the java sources
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static List<JavaDynamicBean> getJavaSources(File srcDir, Map<String, Schema> uniqueSchemas) throws IOException {
        List<JavaDynamicBean> result = new ArrayList<JavaDynamicBean>();
        File[] files = srcDir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                result.addAll(getJavaSources(f, uniqueSchemas));
            } else if (f.getName().endsWith(Kind.SOURCE.extension)) {
                int index = f.getName().indexOf('.');
                String className = f.getName().substring(0, index);
                String sourceCode = readFile(f);

                String classPackageAndName = "";

                String[] sourceLines = sourceCode.split("\n");
                for (String ss : sourceLines) {
                    if (ss.startsWith("package ")) {
                        classPackageAndName = ss.replace("package ", "").trim().replaceAll(";", ".") + className;
                        break;
                    }
                }

                if (uniqueSchemas.containsKey(classPackageAndName)) {
                    uniqueSchemas.remove(classPackageAndName);
                    JavaDynamicBean sourceObject = new JavaDynamicBean(className, sourceCode);
                    result.add(sourceObject);
                }
            }
        }
        return result;
    }

    /**
     * Generate client properties.
     *
     * @param clientPropertiesStream          the client properties stream
     * @param bootstrapNodes                  the bootstrap nodes
     * @param configurationProtocolSchemaBody the configuration protocol schema body
     * @param defaultConfigurationData        the default configuration data
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private byte[] generateClientProperties(InputStream clientPropertiesStream,
                                            List<BootstrapNodeInfo> bootstrapNodes,
                                            String sdkToken,
                                            String configurationProtocolSchemaBody,
                                            byte[] defaultConfigurationData) throws IOException {

        Properties clientProperties = new Properties();
        clientProperties.load(clientPropertiesStream);

        LOG.debug("[sdk generateClientProperties] bootstrapNodes.size(): {}", bootstrapNodes.size());

        clientProperties.put(BUILD_VERSION, Version.PROJECT_VERSION);
        clientProperties.put(BUILD_COMMIT_HASH, Version.COMMIT_HASH);
        clientProperties.put(BOOTSTRAP_SERVERS_PROPERTY, CommonSdkUtil.bootstrapNodesToString(bootstrapNodes));
        clientProperties.put(SDK_TOKEN_PROPERTY, sdkToken);
        clientProperties.put(CONFIG_SCHEMA_DEFAULT_PROPERTY, configurationProtocolSchemaBody);
        clientProperties.put(CONFIG_DATA_DEFAULT_PROPERTY, Base64.encodeBase64String(defaultConfigurationData));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        clientProperties.store(baos, "");

        return baos.toByteArray();
    }

}
