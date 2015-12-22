/*
 * Copyright 2014-2015 CyberVision, Inc.
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
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

public class EndpointMessagePluginGenerator extends AbstractSdkApiGenerator<Configuration> {

    private static final PluginContractItemDef SEND_MESSAGE_CONTRACT = MessagingSDKContract.buildSendMsgDef();
    private static final PluginContractItemDef RECEIVE_MESSAGE_CONTRACT = MessagingSDKContract.buildReceiveMsgDef();

    /**
     * The plugin API interface.
     */
    private JavaPluginInterfaceBuilder interfaceBuilder;

    /**
     * The plugin API implementation.
     */
    private JavaMessagingPluginImplementationBuilder implementationBuilder;

    /**
     * Maps an entity message handler to its method constant.
     */
    private Map<String, Integer> entityMethodConstants = new LinkedHashMap<>();

    /**
     * Maps a void message handler to its method constant.
     */
    private Map<String, Integer> voidMethodConstants = new LinkedHashMap<>();

    /**
     * Maps an input type to its entity class converter.
     */
    private Map<String, String> entityConverters = new HashMap<>();

    /**
     * Maps a method listener interface to an appropriate field.
     */
    private Map<String, String> methodListeners = new LinkedHashMap<>();

    @Override
    public Class<Configuration> getConfigurationClass() {
        return Configuration.class;
    }

    // TODO: Used for testing purposes, remove when unnecessary
    public static void main(String[] args) throws IOException {
        EndpointMessagePluginGenerator object = new EndpointMessagePluginGenerator();
        object.generatePluginSdkApi(EndpointMessagePluginGenerator.getHardcodedContext()).getFiles().forEach(file -> {
            System.out.println(new String(file.getFileData()));
        });
    }

    // TODO: Used for testing purposes, remove when unnecessary
    private static SpecificPluginSdkApiGenerationContext<Configuration> getHardcodedContext() throws IOException {
        PluginContractDef def = MessagingSDKContract.buildMessagingSDKContract();
        final BasePluginContractInstance instance = new BasePluginContractInstance(def);

        GenericAvroConverter<ItemConfiguration> methodNameConverter = new GenericAvroConverter<ItemConfiguration>(ItemConfiguration.SCHEMA$);

        PluginContractItemDef sendMsgDef = MessagingSDKContract.buildSendMsgDef();
        PluginContractItemDef receiveMsgDef = MessagingSDKContract.buildReceiveMsgDef();

        // Future<Void> sendA(ClassA msg);
        PluginContractItemInfo info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encode(new ItemConfiguration("sendA")))
                .withInMsgSchema(ClassA.SCHEMA$.toString()).build();
        instance.addContractItemInfo(sendMsgDef, info);

        // Future<ClassA> getA()
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encode(new ItemConfiguration("getA")))
                .withOutMsgSchema(ClassA.SCHEMA$.toString()).build();
        instance.addContractItemInfo(sendMsgDef, info);

        // Future<ClassB> getB(ClassA msg);
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encode(new ItemConfiguration("getB")))
                .withInMsgSchema(ClassA.SCHEMA$.toString()).withOutMsgSchema(ClassB.SCHEMA$.toString()).build();
        instance.addContractItemInfo(sendMsgDef, info);

        // Future<ClassC> getC(ClassA msg);
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encode(new ItemConfiguration("getC")))
                .withInMsgSchema(ClassA.SCHEMA$.toString()).withOutMsgSchema(ClassC.SCHEMA$.toString()).build();
        instance.addContractItemInfo(sendMsgDef, info);

        // void setMethodAListener(MethodAListener listener);
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encode(new ItemConfiguration("setMethodAListener")))
                .withInMsgSchema(ClassC.SCHEMA$.toString()).withOutMsgSchema(ClassA.SCHEMA$.toString()).build();
        instance.addContractItemInfo(receiveMsgDef, info);

        // void setMethodBListener(MethodBListener listener);
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encode(new ItemConfiguration("setMethodBListener")))
                .withInMsgSchema(ClassC.SCHEMA$.toString()).withOutMsgSchema(ClassB.SCHEMA$.toString()).build();
        instance.addContractItemInfo(receiveMsgDef, info);

        // void setMethodCListener(MethodCListener listener);
        info = BasePluginContractItemInfo.builder().withData(methodNameConverter.encode(new ItemConfiguration("setMethodCListener")))
                .withOutMsgSchema(ClassC.SCHEMA$.toString()).build();
        instance.addContractItemInfo(receiveMsgDef, info);

        PluginSdkApiGenerationContext base = new PluginSdkApiGenerationContext() {

            @Override
            public Set<PluginContractInstance> getPluginContracts() {
                return Collections.<PluginContractInstance> singleton(instance);
            }

            @Override
            public byte[] getPluginConfigurationData() {
                // Not used here
                return null;
            }

            @Override
            public int getExtensionId() {
                // This is used in order to put all auto-generated code into
                // "org.kaaproject.kaa.client.plugin.messaging.ext1" folder
                return 1;
            }
        };
        Configuration configuration = new Configuration("org.kaaproject.kaa.client.plugin.messaging");
        return new SpecificPluginSdkApiGenerationContext<Configuration>(base, configuration);
    }

    @Override
    protected PluginSDKApiBundle generatePluginSdkApi(SpecificPluginSdkApiGenerationContext<Configuration> context) {

        List<SdkApiFile> files = new ArrayList<>();

        String namespace = context.getConfiguration().getMessageFamilyFqn() + ".ext" + context.getExtensionId();
        this.interfaceBuilder = new JavaPluginInterfaceBuilder("MessagingPluginAPI", namespace);
        this.implementationBuilder = new JavaMessagingPluginImplementationBuilder("MessagingPlugin", namespace, "AbstractMessagingPlugin",
                this.interfaceBuilder.getName());

        // Plugin API interface imports
        this.interfaceBuilder.withImportStatement("java.util.concurrent.Future");

        // Plugin API implementation imports
        this.implementationBuilder.withImportStatement("java.io.IOException");
        this.implementationBuilder.withImportStatement("java.util.concurrent.Future");
        this.implementationBuilder.withImportStatement("java.util.UUID");
        this.implementationBuilder.withImportStatement("org.kaaproject.kaa.client.plugin.messaging.common.v1.*");
        this.implementationBuilder.withImportStatement(namespace + ".*");

        // Iterate over contract items provided
        for (PluginContractInstance contract : context.getPluginContracts()) {
            for (PluginContractItemDef def : contract.getDef().getPluginContractItems()) {
                for (PluginContractItemInfo item : contract.getContractItemInfo(def)) {

                    // Get the name of this item
                    String name = null;
                    try {
                        AvroByteArrayConverter<ItemConfiguration> converter = new AvroByteArrayConverter<>(ItemConfiguration.class);
                        name = converter.fromByteArray(item.getConfigurationData()).getMethodName();
                    } catch (IOException cause) {
                    }

                    // Get the input type of this item
                    String rawInputType = null;
                    try {
                        // Try to parse the schema. Use a new parser each time
                        // as it remembers all the parsed types and throws an
                        // exception if the type has been already defined
                        rawInputType = new Schema.Parser().parse(item.getInMessageSchema()).getFullName();

                        // The schema has been parsed successfully. Add an
                        // appropriate entity converter for the type
                        // parsed
                        if (!this.entityConverters.containsKey(rawInputType)) {
                            String entityConverter = "entity" + Integer.toString(this.entityConverters.size() + 1) + "Converter";
                            this.entityConverters.put(rawInputType, entityConverter);
                            this.implementationBuilder.withEntityConverter(entityConverter, rawInputType);
                        }
                    } catch (Exception ignored) {
                        // The method lacks any formal parameters, do nothing.
                    }

                    // Get the output type of this item
                    String rawOutputType = null;
                    try {
                        // Try to parse the schema. Use a new parser each time
                        // as it remembers all the parsed types and throws an
                        // exception if the type has been already defined
                        rawOutputType = new Schema.Parser().parse(item.getOutMessageSchema()).getFullName();

                        // The schema has been parsed successfully. Add an
                        // appropriate entity converter for the type
                        // parsed
                        if (!this.entityConverters.containsKey(rawOutputType)) {
                            String entityConverter = "entity" + Integer.toString(this.entityConverters.size() + 1) + "Converter";
                            this.entityConverters.put(rawOutputType, entityConverter);
                            this.implementationBuilder.withEntityConverter(entityConverter, rawOutputType);
                        }
                    } catch (Exception ignored) {
                        // The method does not return a value, do nothing.
                    }

                    // Process the data gathered based on the item contract
                    if (def.equals(SEND_MESSAGE_CONTRACT)) {

                        // The value to insert into the template as the input
                        // type
                        String wrappedInputType = (rawInputType != null) ? rawInputType : "";

                        // The value to insert into the template as the output
                        // type
                        String wrappedOutputType;

                        // Whether the item denotes an entity message handler or
                        // a void message one
                        Map<String, Integer> constants;
                        if (rawOutputType != null) {
                            wrappedOutputType = String.format("Future<%s>", rawOutputType);
                            constants = this.entityMethodConstants;
                        } else {
                            wrappedOutputType = "Future<Void>";
                            constants = this.voidMethodConstants;
                        }

                        // Add an appropriate method signature to the API
                        // interface
                        this.interfaceBuilder.withMethodSignature(name, wrappedOutputType, new String[] { wrappedInputType }, null);

                        // Add an appropriate method constant to the API
                        // implementation
                        constants.put(name, this.entityMethodConstants.size() + this.voidMethodConstants.size() + 1);
                        this.implementationBuilder.withMethodConstant(name, new String[] { rawInputType }, constants.get(name));

                        // The method parameters
                        Map<String, String> parameters = new LinkedHashMap<>();
                        parameters.put("param", rawInputType);

                        // The values to insert into the method body
                        Map<String, String> values = new HashMap<>();
                        values.put("${name}", name);
                        values.put("${rawInputType}", rawInputType);
                        values.put("${rawOutputType}", rawOutputType);
                        values.put("${wrappedInputType}", wrappedInputType);
                        values.put("${wrappedOutputType}", wrappedOutputType);
                        values.put("${inputTypeConverter}", this.entityConverters.get(rawInputType));
                        values.put("${outputTypeConverter}", this.entityConverters.get(rawOutputType));
                        values.put("${id}", this.entityMethodConstants.getOrDefault(name, this.voidMethodConstants.get(name)).toString());

                        // Get the method body
                        String body;
                        if (rawOutputType == null || rawOutputType.isEmpty()) {
                            body = this.readFileAsString("templates/java/send.template");
                        } else {
                            if (rawInputType != null && !rawInputType.isEmpty()) {
                                body = this.readFileAsString("templates/java/receive.template");
                            } else {
                                body = this.readFileAsString("templates/java/receive_void.template");
                            }
                        }

                        // Add the method to the API implementation
                        this.implementationBuilder.withMethod(name, wrappedOutputType, parameters, new String[] { "public" }, body, values);

                        // Add a method handler
                        if (rawOutputType == null || rawOutputType.isEmpty()) {
                            parameters = new LinkedHashMap<>();
                            parameters.put("uid", "final UUID");
                            body = this.readFileAsString("templates/java/handle_send.template");
                            this.implementationBuilder.withMethod("handleMethod" + values.get("${id}") + "Void", "void", parameters,
                                    new String[] { "private" }, body, values);
                        } else {
                            parameters = new LinkedHashMap<>();
                            parameters.put("msg", "final PayloadMessage");
                            body = this.readFileAsString("templates/java/handle_receive.template");
                            this.implementationBuilder.withMethod("handleMethod" + values.get("${id}") + "Msg", "void", parameters, new String[] { "private" },
                                    body, values);
                        }

                    } else if (def.equals(RECEIVE_MESSAGE_CONTRACT)) {

                        // Whether the item denotes an entity message handler or
                        // a void message one
                        Map<String, Integer> constants = (rawInputType != null) ? this.entityMethodConstants : this.voidMethodConstants;

                        // Add a method listener field
                        String propertyName = "method" + (this.methodListeners.size() + 1) + "Listener";
                        String propertyType = "Method" + (this.methodListeners.size() + 1) + "Listener";
                        this.methodListeners.put(propertyType, propertyName);
                        this.implementationBuilder.withMethodListener(propertyName, propertyType);

                        // Add an appropriate method signature to the API
                        // interface
                        this.interfaceBuilder.withMethodSignature(name, "void", new String[] { propertyType }, null);
                        constants.put(name, this.entityMethodConstants.size() + this.voidMethodConstants.size() + 1);

                        // Add an appropriate method constant to the API
                        // implementation
                        this.implementationBuilder.withMethodConstant(name, new String[] { propertyType }, constants.get(name));

                        // Generates an interface for the method listener field
                        // class
                        JavaPluginInterfaceBuilder listenerInterface = new JavaPluginInterfaceBuilder(propertyType, namespace,
                                this.readFileAsString("templates/java/listenerClass.template"));
                        listenerInterface.withMethodSignature("onEvent", rawOutputType, new String[] { rawInputType }, null);
                        files.add(listenerInterface.build());

                        // The values to insert into the method body
                        Map<String, String> values = new HashMap<>();
                        values.put("${name}", name);
                        values.put("${rawInputType}", rawInputType);
                        values.put("${rawOutputType}", rawOutputType);
                        values.put("${inputTypeConverter}", this.entityConverters.get(rawInputType));
                        values.put("${outputTypeConverter}", this.entityConverters.get(rawOutputType));
                        values.put("${id}", this.entityMethodConstants.getOrDefault(name, this.voidMethodConstants.get(name)).toString());
                        values.put("${listener}", propertyName);

                        // Add a method handler
                        if (rawInputType == null || rawInputType.isEmpty()) {
                            Map<String, String> parameters = new LinkedHashMap<>();
                            parameters.put("uid", "final UUID");
                            String body = this.readFileAsString("templates/java/handle_listener_void.template");
                            this.implementationBuilder.withMethod("handleMethod" + values.get("${id}") + "Void", "void", parameters,
                                    new String[] { "private" }, body, values);
                        } else {
                            Map<String, String> parameters = new LinkedHashMap<>();
                            parameters.put("msg", "final PayloadMessage");
                            String body = this.readFileAsString("templates/java/handle_listener.template");
                            this.implementationBuilder.withMethod("handleMethod" + values.get("${id}") + "Msg", "void", parameters, new String[] { "private" },
                                    body, values);
                        }

                    } else {
                        // Unknown contract item definition
                        throw new RuntimeException();
                    }

                }
            }
        }

        // Generate a method that delegates entity messages to appropriate
        // handlers.
        this.implementationBuilder.withEntityMessageHandlersMapping(this.entityMethodConstants);

        // Generate a method that delegates void messages to appropriate
        // handlers.
        this.implementationBuilder.withVoidMessageHandlersMapping(this.voidMethodConstants);

        files.add(this.interfaceBuilder.build());
        files.add(this.implementationBuilder.build());

        return new PluginSDKApiBundle(files);
    }
}
