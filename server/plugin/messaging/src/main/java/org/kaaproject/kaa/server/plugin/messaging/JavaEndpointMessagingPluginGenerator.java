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

package org.kaaproject.kaa.server.plugin.messaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.avro.Schema;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.avro.AvroJsonConverter;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractInstance;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemInfo;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.AbstractSdkApiGenerator;
import org.kaaproject.kaa.server.common.core.plugin.generator.PluginSDKApiBundle;
import org.kaaproject.kaa.server.common.core.plugin.generator.PluginSdkApiGenerationContext;
import org.kaaproject.kaa.server.common.core.plugin.generator.SpecificPluginSdkApiGenerationContext;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.JavaPluginInterfaceBuilder;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractInstance;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractItemInfo;
import org.kaaproject.kaa.server.plugin.messaging.gen.Configuration;
import org.kaaproject.kaa.server.plugin.messaging.gen.ItemConfiguration;
import org.kaaproject.kaa.server.plugin.messaging.gen.test.ClassA;
import org.kaaproject.kaa.server.plugin.messaging.gen.test.ClassB;
import org.kaaproject.kaa.server.plugin.messaging.gen.test.ClassC;
import org.kaaproject.kaa.server.plugin.messaging.generator.java.JavaMessagingPluginImplementationBuilder;

public class JavaEndpointMessagingPluginGenerator extends AbstractSdkApiGenerator<Configuration> {

    private static final PluginContractItemDef SEND_MESSAGE_CONTRACT = MessagingSDKContract.buildSendMsgDef();
    private static final PluginContractItemDef RECEIVE_MESSAGE_CONTRACT = MessagingSDKContract.buildReceiveMsgDef();

    // Template variables
    private static final String NAME_VAR = "${name}";
    private static final String METHOD_ID_VAR = "${methodId}";
    private static final String INPUT_TYPE_VAR = "${inputType}";
    private static final String OUTPUT_TYPE_VAR = "${outputType}";
    private static final String MESSAGE_INPUT_TYPE_VAR = "${messageInputType}";
    private static final String MESSAGE_OUTPUT_TYPE_VAR = "${messageOutputType}";
    private static final String MESSAGE_INPUT_TYPE_CONVERTER_VAR = "${messageInputTypeConverter}";
    private static final String MESSAGE_OUTPUT_TYPE_CONVERTER_VAR = "${messageOutputTypeConverter}";
    private static final String LISTENER_VAR = "${listener}";

    /**
     * The plugin API interface builder.
     */
    private JavaPluginInterfaceBuilder pluginInterface;

    /**
     * The plugin API implementation builder.
     */
    private JavaMessagingPluginImplementationBuilder pluginImplementation;

    /**
     * Maps a type to its entity class converter.
     */
    private Map<String, String> entityConverters = new HashMap<>();

    /**
     * Maps a method listener interface to an appropriate field.
     */
    private Map<String, String> methodListeners = new LinkedHashMap<>();

    /**
     * Entity method handler constants
     */
    private Set<Integer> entityMethodConstants = new HashSet<Integer>();

    /**
     * Void method handler constants
     */
    private Set<Integer> voidMethodConstants = new HashSet<Integer>();

    @Override
    public Class<Configuration> getConfigurationClass() {
        return Configuration.class;
    }

    // TODO: Used for testing purposes, remove when unnecessary
    public static void main(String[] args) throws Exception {
        JavaEndpointMessagingPluginGenerator object = new JavaEndpointMessagingPluginGenerator();
        object.generatePluginSdkApi(JavaEndpointMessagingPluginGenerator.getHardcodedContext()).getFiles().forEach(file -> {
            System.out.println(file.getFileName());
        });
    }

    // TODO: Used for testing purposes, remove when unnecessary
    public static PluginSdkApiGenerationContext getHardcodedContext() throws IOException {
        PluginContractDef def = MessagingSDKContract.buildMessagingSDKContract();
        final BasePluginContractInstance instance = new BasePluginContractInstance(def);

        AvroJsonConverter<ItemConfiguration> methodNameConverter = new AvroJsonConverter<>(ItemConfiguration.SCHEMA$, ItemConfiguration.class);

        PluginContractItemDef sendMsgDef = MessagingSDKContract.buildSendMsgDef();
        PluginContractItemDef receiveMsgDef = MessagingSDKContract.buildReceiveMsgDef();

        PluginContractItemInfo info;

        // Future<Void> sendA(ClassA msg);
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encodeToJson(new ItemConfiguration("sendA")))
                .withInMsgSchema(ClassA.SCHEMA$.toString()).build();
        instance.addContractItemInfo(sendMsgDef, info);

        // Future<ClassA> getA()
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encodeToJson(new ItemConfiguration("getA")))
                .withOutMsgSchema(ClassA.SCHEMA$.toString()).build();
        instance.addContractItemInfo(sendMsgDef, info);

        // Future<ClassB> getB(ClassA msg);
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encodeToJson(new ItemConfiguration("getB")))
                .withInMsgSchema(ClassA.SCHEMA$.toString()).withOutMsgSchema(ClassB.SCHEMA$.toString()).build();
        instance.addContractItemInfo(sendMsgDef, info);

        // Future<ClassC> getC(ClassA msg);
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encodeToJson(new ItemConfiguration("getC")))
                .withInMsgSchema(ClassA.SCHEMA$.toString()).withOutMsgSchema(ClassC.SCHEMA$.toString()).build();
        instance.addContractItemInfo(sendMsgDef, info);

        // void setMethodAListener(MethodAListener listener);
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encodeToJson(new ItemConfiguration("setMethodAListener")))
                .withInMsgSchema(ClassC.SCHEMA$.toString()).withOutMsgSchema(ClassA.SCHEMA$.toString()).build();
        instance.addContractItemInfo(receiveMsgDef, info);

        // void setMethodBListener(MethodBListener listener);
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encodeToJson(new ItemConfiguration("setMethodBListener")))
                .withInMsgSchema(ClassC.SCHEMA$.toString()).withOutMsgSchema(ClassB.SCHEMA$.toString()).build();
        instance.addContractItemInfo(receiveMsgDef, info);

        // void setMethodCListener(MethodCListener listener);
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encodeToJson(new ItemConfiguration("setMethodCListener")))
                .withOutMsgSchema(ClassC.SCHEMA$.toString()).build();
        instance.addContractItemInfo(receiveMsgDef, info);

        PluginSdkApiGenerationContext base = new PluginSdkApiGenerationContext() {

            @Override
            public Set<PluginContractInstance> getPluginContracts() {
                return Collections.<PluginContractInstance> singleton(instance);
            }

            @Override
            public String getPluginConfigurationData() {
                Configuration config = new Configuration("org.kaaproject.kaa.client.plugin.messaging");
                try {
                    return new String(new AvroByteArrayConverter<>(Configuration.class).toByteArray(config));
                } catch (IOException cause) {
                    return null;
                }
            }

            @Override
            public int getExtensionId() {
                // This is used in order to put all auto-generated code into
                // "org.kaaproject.kaa.client.plugin.messaging.ext1" folder
                return 1;
            }
        };

        return base;
    }

    @Override
    protected PluginSDKApiBundle generatePluginSdkApi(SpecificPluginSdkApiGenerationContext<Configuration> context) {

        List<SdkApiFile> files = new ArrayList<>();

        String namespace = context.getConfiguration().getMessageFamilyFqn() + ".ext" + context.getExtensionId();
        this.pluginInterface = new JavaPluginInterfaceBuilder("MessagingPluginAPI", namespace);
        this.pluginImplementation = new JavaMessagingPluginImplementationBuilder("MessagingPlugin", namespace, "AbstractMessagingPlugin",
                this.pluginInterface.getName());

        // Plugin API interface imports
        this.pluginInterface.withImportStatement(Future.class.getCanonicalName());

        // Plugin API implementation imports
        this.pluginImplementation.withImportStatement(IOException.class.getCanonicalName());
        this.pluginImplementation.withImportStatement(Future.class.getCanonicalName());
        this.pluginImplementation.withImportStatement(UUID.class.getCanonicalName());
        this.pluginImplementation.withImportStatement("org.kaaproject.kaa.client.plugin.messaging.common.v1.*");
        this.pluginImplementation.withImportStatement("org.kaaproject.kaa.client.plugin.messaging.common.v1.future.*");
        this.pluginImplementation.withImportStatement("org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.*");

        // Iterate over contract items provided
        for (PluginContractInstance contract : context.getPluginContracts()) {
            for (PluginContractItemDef def : contract.getDef().getPluginContractItems()) {
                List<PluginContractItemInfo> items = contract.getContractItemInfo(def);
                if (items != null) {
                    for (PluginContractItemInfo item : items) {

                        // Assign an ID to this method
                        int methodId = entityMethodConstants.size() + voidMethodConstants.size() + 1;

                        // Get the name of this method
                        String name = this.getMethodName(item);

                        // Get the input type of this method and add an
                        // appropriate entity converter
                        String messageInputType = this.parseMessageType(item.getInMessageSchema());
                        this.addEntityConverter(messageInputType);

                        // Get the input type of this method and add an
                        // appropriate entity converter
                        String messageOutputType = parseMessageType(item.getOutMessageSchema());
                        this.addEntityConverter(messageOutputType);

                        // Process the data gathered based on the item contract
                        if (def.equals(SEND_MESSAGE_CONTRACT)) {

                            // The value to insert into the template as the
                            // input type
                            String inputType = (messageInputType != null) ? messageInputType : "";

                            // The value to insert into the template as the
                            // output type
                            String outputType;
                            if (messageOutputType != null) {
                                outputType = String.format("Future<%s>", messageOutputType);
                                this.entityMethodConstants.add(methodId);
                            } else {
                                outputType = "Future<Void>";
                                this.voidMethodConstants.add(methodId);
                            }

                            // Add an appropriate method signature to the API
                            // interface
                            this.pluginInterface.withMethodSignature(name, outputType, new String[] { inputType }, null);

                            // Add an appropriate method constant to the API
                            // implementation
                            this.pluginImplementation.withMethodConstant(name, new String[] { messageInputType }, methodId);

                            // The method parameters
                            Map<String, String> parameters = new LinkedHashMap<>();
                            parameters.put("param", messageInputType);

                            // The values to insert into the method body
                            Map<String, String> values = new HashMap<>();
                            values.put(NAME_VAR, name);
                            values.put(MESSAGE_INPUT_TYPE_VAR, messageInputType);
                            values.put(MESSAGE_OUTPUT_TYPE_VAR, messageOutputType);
                            values.put(INPUT_TYPE_VAR, inputType);
                            values.put(OUTPUT_TYPE_VAR, outputType);
                            values.put(MESSAGE_INPUT_TYPE_CONVERTER_VAR, this.entityConverters.get(messageInputType));
                            values.put(MESSAGE_OUTPUT_TYPE_CONVERTER_VAR, this.entityConverters.get(messageOutputType));
                            values.put(METHOD_ID_VAR, Integer.toString(methodId));

                            // Get the method body
                            String body;
                            if (messageOutputType == null || messageOutputType.isEmpty()) {
                                body = this.readFileAsString("templates/java/send.template");
                            } else {
                                if (messageInputType != null && !messageInputType.isEmpty()) {
                                    body = this.readFileAsString("templates/java/receive.template");
                                } else {
                                    body = this.readFileAsString("templates/java/receive_void.template");
                                }
                            }

                            // Add the method to the API implementation
                            this.pluginImplementation.withMethod(name, outputType, parameters, new String[] { "public" }, body, values);

                            // Add a method handler
                            if (messageOutputType == null || messageOutputType.isEmpty()) {
                                parameters = new LinkedHashMap<>();
                                parameters.put("uid", "final UUID");
                                body = this.readFileAsString("templates/java/handle_send.template");
                                this.pluginImplementation.withMethod("handleMethod" + values.get(METHOD_ID_VAR) + "Void", "void", parameters,
                                        new String[] { "private" }, body, values);
                            } else {
                                parameters = new LinkedHashMap<>();
                                parameters.put("msg", "final PayloadMessage");
                                body = this.readFileAsString("templates/java/handle_receive.template");
                                this.pluginImplementation.withMethod("handleMethod" + values.get(METHOD_ID_VAR) + "Msg", "void", parameters,
                                        new String[] { "private" }, body, values);
                            }

                        } else if (def.equals(RECEIVE_MESSAGE_CONTRACT)) {

                            if (messageInputType != null) {
                                this.entityMethodConstants.add(methodId);
                            } else {
                                this.voidMethodConstants.add(methodId);
                            }

                            // Add a method listener field
                            String methodName = name;
                            String fieldName = "method" + (this.methodListeners.size() + 1) + "Listener";
                            String fieldType = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                            this.methodListeners.put(fieldType, fieldName);
                            this.pluginImplementation.withMethodListener(methodName, fieldName, fieldType);

                            // Add an appropriate method signature to the API
                            // interface
                            this.pluginInterface.withMethodSignature(name, "void", new String[] { fieldType }, null);

                            // Add an appropriate method constant to the API
                            // implementation
                            this.pluginImplementation.withMethodConstant(name, new String[] { fieldType }, methodId);

                            // Generates an interface for the method listener
                            // field class
                            JavaPluginInterfaceBuilder listenerInterface = new JavaPluginInterfaceBuilder(fieldType, namespace,
                                    this.readFileAsString("templates/java/listenerClass.template"));
                            listenerInterface.withMethodSignature("onEvent", messageOutputType, new String[] { messageInputType }, null);
                            files.add(listenerInterface.build());

                            // The values to insert into the method body
                            Map<String, String> values = new HashMap<>();
                            values.put(NAME_VAR, name);
                            values.put(MESSAGE_INPUT_TYPE_VAR, messageInputType);
                            values.put(MESSAGE_OUTPUT_TYPE_VAR, messageOutputType);
                            values.put(MESSAGE_INPUT_TYPE_CONVERTER_VAR, this.entityConverters.get(messageInputType));
                            values.put(MESSAGE_OUTPUT_TYPE_CONVERTER_VAR, this.entityConverters.get(messageOutputType));
                            values.put(METHOD_ID_VAR, Integer.toString(methodId));
                            values.put(LISTENER_VAR, fieldName);

                            // Add a method handler
                            if (messageInputType == null || messageInputType.isEmpty()) {
                                Map<String, String> parameters = new LinkedHashMap<>();
                                parameters.put("uid", "final UUID");
                                String body = this.readFileAsString("templates/java/handle_listener_void.template");
                                this.pluginImplementation.withMethod("handleMethod" + values.get(METHOD_ID_VAR) + "Void", "void", parameters,
                                        new String[] { "private" }, body, values);
                            } else {
                                Map<String, String> parameters = new LinkedHashMap<>();
                                parameters.put("msg", "final PayloadMessage");
                                String body = this.readFileAsString("templates/java/handle_listener.template");
                                this.pluginImplementation.withMethod("handleMethod" + values.get(METHOD_ID_VAR) + "Msg", "void", parameters,
                                        new String[] { "private" }, body, values);
                            }
                        } else {
                            LOG.error("Unknown plugin item definition [{}]", def);
                            throw new RuntimeException();
                        }
                    }
                }
            }
        }

        // Generate a method that delegates entity messages to appropriate
        // handlers.
        this.pluginImplementation.withEntityMessageHandlersMapping(entityMethodConstants);

        // Generate a method that delegates void messages to appropriate
        // handlers.
        this.pluginImplementation.withVoidMessageHandlersMapping(voidMethodConstants);

        files.add(this.pluginInterface.build());
        files.add(this.pluginImplementation.build());

        return new PluginSDKApiBundle(context.getExtensionId(), this.pluginInterface.getFqn(), this.pluginImplementation.getFqn(), files);
    }

    /**
     * Adds an entity converter for the type specified.
     *
     * @param typeName A fully qualified type name
     */
    private void addEntityConverter(String typeName) {
        if (typeName != null && !this.entityConverters.containsKey(typeName)) {
            String entityConverter = "entity" + Integer.toString(this.entityConverters.size() + 1) + "Converter";
            this.entityConverters.put(typeName, entityConverter);
            this.pluginImplementation.withEntityConverter(entityConverter, typeName);
        }
    }

    /**
     * Parses the given Avro schema and returns the name of the type parsed.
     *
     * @param typeSchema An Avro schema
     *
     * @return The name of the type parsed
     */
    private String parseMessageType(String typeSchema) {
        return (typeSchema != null) ? new Schema.Parser().parse(typeSchema).getFullName() : null;
    }

    /**
     * Figures out the method name of the given item.
     *
     * @param item An item to process
     *
     * @return The name of the item processed. If
     */
    private String getMethodName(PluginContractItemInfo item) {
        String name = null;
        try {
            AvroJsonConverter<ItemConfiguration> converter = new AvroJsonConverter<>(ItemConfiguration.SCHEMA$, ItemConfiguration.class);
            name = converter.decodeJson(item.getConfigurationData()).getMethodName();
        } catch (IOException cause) {
            LOG.debug("Unable to parse the item [{}]", item);
        }
        return name;
    }
}
