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

    private JavaPluginInterfaceBuilder interfaceBuilder;
    private JavaMessagingPluginImplementationBuilder implementationBuilder;

    private Map<String, Integer> entityMethodConstants = new LinkedHashMap<>();
    private Map<String, Integer> voidMethodConstants = new LinkedHashMap<>();
    private Map<String, String> entityConverters = new HashMap<>();

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

        String namespace = context.getConfiguration().getMessageFamilyFqn();
        this.interfaceBuilder = new JavaPluginInterfaceBuilder("MessagingPluginAPI", namespace);
        this.implementationBuilder = new JavaMessagingPluginImplementationBuilder("MessagingPlugin", namespace);

        this.interfaceBuilder.withImportStatement("java.util.concurrent.Future");
        this.implementationBuilder.withImportStatement("java.util.concurrent.Future");

        for (PluginContractInstance contract : context.getPluginContracts()) {
            for (PluginContractItemDef def : contract.getDef().getPluginContractItems()) {
                for (PluginContractItemInfo item : contract.getContractItemInfo(def)) {

                    String name = null;
                    try {
                        AvroByteArrayConverter<ItemConfiguration> converter = new AvroByteArrayConverter<>(ItemConfiguration.class);
                        name = converter.fromByteArray(item.getConfigurationData()).getMethodName();
                    } catch (IOException cause) {
                        throw new RuntimeException(cause);
                    }

                    String inputType = null;
                    try {
                        inputType = new Schema.Parser().parse(item.getInMessageSchema()).getFullName();
                        if (!this.entityConverters.containsKey(inputType)) {
                            String entityConverterName = "entity" + Integer.toString(this.entityConverters.size() + 1) + "Converter";
                            this.entityConverters.put(inputType, entityConverterName);
                            this.implementationBuilder.withEntityConverter(entityConverterName, inputType);
                        }
                    } catch (Exception cause) {
                    }

                    String outputType = null;
                    try {
                        outputType = new Schema.Parser().parse(item.getOutMessageSchema()).getFullName();
                        if (!this.entityConverters.containsKey(outputType)) {
                            String entityConverterName = "entity" + Integer.toString(this.entityConverters.size() + 1) + "Converter";
                            this.entityConverters.put(outputType, entityConverterName);
                            this.implementationBuilder.withEntityConverter(entityConverterName, outputType);
                        }
                    } catch (Exception cause) {
                    }

                    if (def.equals(SEND_MESSAGE_CONTRACT)) {

                        inputType = (inputType != null) ? inputType : "";
                        outputType = String.format(outputType != null ? "Future<%s>" : "Future<Void>", outputType);

                        this.interfaceBuilder.withMethodSignature(name, outputType, new String[] {}, null);

                        // use either handleEntityMsg or handleVoidMsg

                        this.entityMethodConstants.put(name, this.entityMethodConstants.size() + 1);
                        this.implementationBuilder.withMethodConstant(name, this.entityMethodConstants.get(name));

                    } else if (def.equals(RECEIVE_MESSAGE_CONTRACT)) {
                        Map<String, String> parameters = new LinkedHashMap<>();
                        parameters.put("listener", "PLACEHOLDER");
                        this.interfaceBuilder.withMethodSignature(name, "void", parameters, null);
                        this.voidMethodConstants.put(name, this.voidMethodConstants.size() + 1);
                        this.implementationBuilder.withMethodConstant(name, this.voidMethodConstants.get(name));

                    } else {
                        throw new RuntimeException();
                    }
                }
            }
        }

        this.implementationBuilder.withEntityMessageHandlersMapping(this.entityMethodConstants);
        this.implementationBuilder.withVoidMessageHandlersMapping(this.voidMethodConstants);

        files.add(this.interfaceBuilder.build());
        files.add(this.implementationBuilder.build());
        return new PluginSDKApiBundle(files);
    }
}
