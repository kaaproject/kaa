package org.kaaproject.kaa.server.common.core.plugin.generator.java;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilderCore;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginInterfaceBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.GeneratorEntity;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaConstant;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaImportStatement;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaMethodSignature;

public class JavaPluginInterfaceBuilder extends PluginBuilderCore implements PluginInterfaceBuilder {

    public JavaPluginInterfaceBuilder(String name, String namespace) {
        super(readFileAsString("templates/java/interface.template"), name, namespace);
    }

    public JavaPluginInterfaceBuilder(String template, String name, String namespace) {
        super(template, name, namespace);
    }

    @Override
    public PluginInterfaceBuilder withEntity(GeneratorEntity entity) {
        addEntity(entity);
        return this;
    }

    @Override
    public PluginInterfaceBuilder withImportStatement(String body) {
        addEntity(new JavaImportStatement(body));
        return this;
    }

    @Override
    public PluginInterfaceBuilder withConstant(String name, String type, String value) {
        addEntity(new JavaConstant(name, type, value));
        return this;
    }

    @Override
    public PluginInterfaceBuilder withMethodSignature(String name, String returnType, String... paramTypes) {
        addEntity(new JavaMethodSignature(name, returnType, paramTypes));
        return this;
    }

    @Override
    public SdkApiFile build() {
        // TODO: why do we need to add PluginAPI? This is wrong. Class name
        // should match file name. Maybe replace with ".java"?
        String fileName = this.getName() + "PluginAPI.java";
        byte[] fileData = this.substituteAllEntities().getBytes();

        return new SdkApiFile(fileName, fileData);
    }

    public static void main(String[] args) {
        PluginInterfaceBuilder o = new JavaPluginInterfaceBuilder("Messaging", "org.kaaproject.kaa.plugin.messaging")
                .withImportStatement("java.util.Map").withConstant("ANOTHER_TEST", "String", "\"Hello, World\"")
                .withConstant("TEST", "String", "\"Hello, World\"").withConstant("TEST_x", "String", "\"Hello, World\"")
                .withMethodSignature("foo", "void", new String[] {}).withMethodSignature("bar", "Double", "String", "Future<Void>");
        System.out.println(new String(o.build().getFileData()));
    }
}
