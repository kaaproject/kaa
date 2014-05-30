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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.kaaproject.kaa.server.common.thrift.gen.control.Sdk;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicBean;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicCompiler;
import org.kaaproject.kaa.server.control.service.sdk.compress.ZipEntryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

// TODO: Auto-generated Javadoc
/**
 * The Class JavaSdkGenerator.
 */
public class JavaSdkGenerator extends SdkGenerator {
    
    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(JavaSdkGenerator.class);
    
    /** The Constant JAVA_SDK_DIR. */
    private static final String JAVA_SDK_DIR = "sdk/java";
    
    /** The Constant JAVA_SDK_PREFIX. */
    private static final String JAVA_SDK_PREFIX = "kaa-client-sdk-";
    
    /** The Constant JAVA_SDK_NAME_PATTERN. */
    private static final String JAVA_SDK_NAME_PATTERN = JAVA_SDK_PREFIX + "p{}-c{}-n{}.jar";
    
    /** The Constant CLIENT_PROPERTIES. */
    private static final String CLIENT_PROPERTIES = "client.properties";
    
    /** The Constant BOOTSTRAP_SERVERS_PROPERTY. */
    private static final String BOOTSTRAP_SERVERS_PROPERTY = "transport.bootstrap.servers";
    
    /** The Constant APP_TOKEN_PROPERTY. */
    private static final String APP_TOKEN_PROPERTY = "application_token";
    
    /** The Constant CONFIG_VERSION_PROPERTY. */
    private static final String CONFIG_VERSION_PROPERTY = "config_version";
    
    /** The Constant PROFILE_VERSION_PROPERTY. */
    private static final String PROFILE_VERSION_PROPERTY = "profile_version";
    
    /** The Constant NOTIFICATION_VERSION_PROPERTY. */
    private static final String NOTIFICATION_VERSION_PROPERTY = "user_nt_version";
    
    /** The Constant CONFIG_DATA_DEFAULT_PROPERTY. */
    private static final String CONFIG_DATA_DEFAULT_PROPERTY = "config.data.default";
    
    /** The Constant CONFIG_SCHEMA_DEFAULT_PROPERTY. */
    private static final String CONFIG_SCHEMA_DEFAULT_PROPERTY = "config.schema.default";
    
    /** The Constant ABSTRACT_RPOFILE_CONTAINER_SOURCE_TEMPLATE. */
    private static final String ABSTRACT_RPOFILE_CONTAINER_SOURCE_TEMPLATE = "sdk/java/AbstractProfileContainer.java.template";

    /** The Constant ABSTRACT_NOTIFICATION_LISTENER_SOURCE_TEMPLATE. */
    private static final String ABSTRACT_NOTIFICATION_LISTENER_SOURCE_TEMPLATE = "sdk/java/AbstractNotificationListener.java.template";
    
    /** The Constant ABSTRACT_PROFILE_CONTAINER. */
    private static final String ABSTRACT_PROFILE_CONTAINER = "AbstractProfileContainer";

    /** The Constant ABSTRACT_NOTIFICATION_LISTENER. */
    private static final String ABSTRACT_NOTIFICATION_LISTENER = "AbstractNotificationListener";
    
    /** The Constant PROFILE_CLASS_PACKAGE_VAR. */
    private static final String PROFILE_CLASS_PACKAGE_VAR = "\\$\\{profile_class_package\\}";

    /** The Constant PROFILE_CLASS_VAR. */
    private static final String PROFILE_CLASS_VAR = "\\$\\{profile_class\\}";

    /** The Constant NOTIFICATION_CLASS_PACKAGE_VAR. */
    private static final String NOTIFICATION_CLASS_PACKAGE_VAR = "\\$\\{notification_class_package\\}";

    /** The Constant NOTIFICATION_CLASS_VAR. */
    private static final String NOTIFICATION_CLASS_VAR = "\\$\\{notification_class\\}";
    
    /** The Constant random. */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate sdk.
     *
     * @param buildVersion the build version
     * @param bootstrapNodes the bootstrap nodes
     * @param appToken the app token
     * @param profileSchemaVersion the profile schema version
     * @param configurationSchemaVersion the configuration schema version
     * @param notificationSchemaVersion the notification schema version
     * @param profileSchemaBody the profile schema body
     * @param notificationSchemaBody the notification schema body
     * @param configurationProtocolSchemaBody the configuration protocol schema body
     * @param defaultConfigurationData the default configuration data 
     * @return the sdk
     * @throws Exception the exception
     */
    @Override
    public Sdk generateSdk(String buildVersion, List<BootstrapNodeInfo> bootstrapNodes, 
            String appToken, int profileSchemaVersion,
            int configurationSchemaVersion, int notificationSchemaVersion,
            String profileSchemaBody, 
            String notificationSchemaBody, 
            String configurationProtocolSchemaBody, 
            byte[] defaultConfigurationData) throws Exception {
        String sdkTemplateLocation = System.getProperty("server_home_dir") + "/" + JAVA_SDK_DIR + "/" + JAVA_SDK_PREFIX + buildVersion + ".jar";
        
        LOG.debug("Lookup Java SDK template: {}", sdkTemplateLocation);
        
        File sdkTemplateFile = new File(sdkTemplateLocation);
        ZipFile templateArhive = new ZipFile(sdkTemplateFile);
        
        Map<String, ZipEntryData> replacementData = new HashMap<String, ZipEntryData>();
        
        ZipEntry clientPropertiesEntry = templateArhive.getEntry(CLIENT_PROPERTIES);
        byte[] clientPropertiesData = generateClientProperties(templateArhive.getInputStream(clientPropertiesEntry),
                                                               bootstrapNodes,
                                                               appToken,
                                                               configurationSchemaVersion,
                                                               profileSchemaVersion,
                                                               notificationSchemaVersion,
                                                               configurationProtocolSchemaBody,
                                                               defaultConfigurationData);
        
        replacementData.put(CLIENT_PROPERTIES, new ZipEntryData(new ZipEntry(CLIENT_PROPERTIES), clientPropertiesData));
        
        Schema profileSchema = new Schema.Parser().parse(profileSchemaBody);
        
        List<JavaDynamicBean> javaSources = generateSchemaSources(profileSchema);
        String profileContainerTemplate = readResource(ABSTRACT_RPOFILE_CONTAINER_SOURCE_TEMPLATE);
        String profileContainerSource = profileContainerTemplate.replaceAll(PROFILE_CLASS_PACKAGE_VAR, profileSchema.getNamespace()).
                replaceAll(PROFILE_CLASS_VAR, profileSchema.getName());
        
        JavaDynamicBean profileContainerClassBean = new JavaDynamicBean(ABSTRACT_PROFILE_CONTAINER, profileContainerSource);
        javaSources.add(profileContainerClassBean);
        
        Schema notificationSchema = new Schema.Parser().parse(notificationSchemaBody);
        javaSources.addAll(generateSchemaSources(notificationSchema));
        String notificationListenerTemplate = readResource(ABSTRACT_NOTIFICATION_LISTENER_SOURCE_TEMPLATE);
        String notificationContainerSource = notificationListenerTemplate.replaceAll(NOTIFICATION_CLASS_PACKAGE_VAR, notificationSchema.getNamespace()).
                replaceAll(NOTIFICATION_CLASS_VAR, notificationSchema.getName());

        JavaDynamicBean notificationListenerClassBean = new JavaDynamicBean(ABSTRACT_NOTIFICATION_LISTENER, notificationContainerSource);
        javaSources.add(notificationListenerClassBean);

        packageSources(javaSources, replacementData);
        
        ByteArrayOutputStream sdkOutput = new ByteArrayOutputStream();
        ZipOutputStream sdkFile = new ZipOutputStream(sdkOutput);

        Enumeration<? extends ZipEntry> entries = templateArhive.entries();

        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (replacementData.containsKey(e.getName())) {
                ZipEntryData replacementEntry = replacementData.get(e.getName());
                sdkFile.putNextEntry(replacementEntry.getEntry());
                sdkFile.write(replacementEntry.getData());
                replacementData.remove(e.getName());
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
        
        String sdkFileName = MessageFormatter.arrayFormat(JAVA_SDK_NAME_PATTERN, 
                new Object[]{profileSchemaVersion, 
                configurationSchemaVersion, 
                notificationSchemaVersion}).getMessage();
        
        byte[] sdkData = sdkOutput.toByteArray();
        
        Sdk sdk = new Sdk();
        sdk.setFileName(sdkFileName);
        sdk.setData(sdkData);
        return sdk;
    }
    
    /**
     * Generate schema class.
     *
     * @param schema the schema
     * @return the list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private List<JavaDynamicBean> generateSchemaSources(Schema schema) throws IOException {
        SpecificCompiler compiler = new SpecificCompiler(schema);
        compiler.setStringType(StringType.String);
        compiler.setFieldVisibility(FieldVisibility.PRIVATE);
        
        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        long n = RANDOM.nextLong();
        if (n == Long.MIN_VALUE) {
            n = 0;      // corner case
        } else {
            n = Math.abs(n);
        }
        File tmpOutputDir = new File(tmpdir, "tmp-gen-"+Long.toString(n));
        tmpOutputDir.mkdirs();
        
        compiler.compileToDestination(null, tmpOutputDir);
        
        List<JavaDynamicBean> sources = getJavaSources(tmpOutputDir);        
        
        tmpOutputDir.delete();

        return sources;
    }
    
    /**
     * Package sources.
     *
     * @param javaSources the java sources
     * @param data the data
     */
    private void packageSources(List<JavaDynamicBean> javaSources, Map<String, ZipEntryData> data) {
        JavaDynamicCompiler dynamicCompiler = new JavaDynamicCompiler();
        dynamicCompiler.init();
        Collection<JavaDynamicBean> compiledObjects = dynamicCompiler.compile(javaSources);
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
    private List<JavaDynamicBean> getJavaSources(File srcDir) throws IOException {
        List<JavaDynamicBean> result = new ArrayList<JavaDynamicBean>();
        File[] files = srcDir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                result.addAll(getJavaSources(f));
            } else if (f.getName().endsWith(Kind.SOURCE.extension)){
                int index = f.getName().indexOf('.');
                String className = f.getName().substring(0, index);
                String sourceCode = readFile(f);
                JavaDynamicBean sourceObject = new JavaDynamicBean(className, sourceCode);
                result.add(sourceObject);
            }
        }
        return result;
    }
        
    /**
     * Generate client properties.
     *
     * @param clientPropertiesStream the client properties stream
     * @param bootstrapNodes the bootstrap nodes
     * @param appToken the app token
     * @param configurationSchemaVersion the configuration schema version
     * @param profileSchemaVersion the profile schema version
     * @param notificationSchemaVersion the notification schema version
     * @param configurationProtocolSchemaBody the configuration protocol schema body
     * @param defaultConfigurationData the default configuration data 
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private byte[] generateClientProperties(InputStream clientPropertiesStream,
            List<BootstrapNodeInfo> bootstrapNodes,
            String appToken,
            int configurationSchemaVersion,
            int profileSchemaVersion,
            int notificationSchemaVersion, 
            String configurationProtocolSchemaBody, 
            byte[] defaultConfigurationData) throws IOException {

        Properties clientProperties = new Properties();
        clientProperties.load(clientPropertiesStream);
        
        String bootstrapServers = "";
        
        LOG.debug("[sdk generateClientProperties] bootstrapNodes.size(): {}", bootstrapNodes.size());
        for (int i=0;i<bootstrapNodes.size();i++) {
            if (i>0) {
                bootstrapServers += ";";
            }
            ConnectionInfo bootstrapConnection = bootstrapNodes.get(i).getConnectionInfo();
            String publicKeyBase64 = Base64.encodeBase64String(bootstrapConnection.getPublicKey().array());
            bootstrapServers += bootstrapConnection.getHttpHost() + ":" + bootstrapConnection.getHttpPort() + ":" + publicKeyBase64;
        }
        
        clientProperties.put(BOOTSTRAP_SERVERS_PROPERTY, bootstrapServers);
        clientProperties.put(APP_TOKEN_PROPERTY, appToken);
        clientProperties.put(CONFIG_VERSION_PROPERTY, ""+configurationSchemaVersion);
        clientProperties.put(PROFILE_VERSION_PROPERTY, ""+profileSchemaVersion);
        clientProperties.put(NOTIFICATION_VERSION_PROPERTY, ""+notificationSchemaVersion);
        clientProperties.put(CONFIG_SCHEMA_DEFAULT_PROPERTY, configurationProtocolSchemaBody);
        clientProperties.put(CONFIG_DATA_DEFAULT_PROPERTY, Base64.encodeBase64String(defaultConfigurationData));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        clientProperties.store(baos, "");
        
        return baos.toByteArray();
    }
 
}
