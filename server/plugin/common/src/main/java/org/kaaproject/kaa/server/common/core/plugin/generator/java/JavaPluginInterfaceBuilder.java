package org.kaaproject.kaa.server.common.core.plugin.generator.java;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilderCore;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginInterfaceBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.GeneratorEntity;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaConstant;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaImportStatement;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaMethodSignature;

public class JavaPluginInterfaceBuilder extends PluginBuilderCore implements PluginInterfaceBuilder {

    private static final String DEFAULT_TEMPLATE_FILE = "templates/java/interface.template";

    public JavaPluginInterfaceBuilder(String name, String namespace) {
        super(name, namespace, PluginBuilderCore.readFileAsString(DEFAULT_TEMPLATE_FILE));
    }

    public JavaPluginInterfaceBuilder(String name, String namespace, String template) {
        super(name, namespace, template);
    }

    @Override
    public PluginInterfaceBuilder withEntity(GeneratorEntity entity) {
        this.addEntity(entity);
        return this;
    }

    @Override
    public PluginInterfaceBuilder withImportStatement(String body) {
        this.addEntity(new JavaImportStatement(body));
        return this;
    }

    @Override
    public PluginInterfaceBuilder withConstant(String name, String type, String value) {
        this.addEntity(new JavaConstant(name, type, value));
        return this;
    }

    @Override
    public PluginInterfaceBuilder withMethodSignature(String name, String returnType, String... paramTypes) {
        this.addEntity(new JavaMethodSignature(name, returnType, paramTypes));
        return this;
    }

    @Override
    public SdkApiFile build() {
        return super.build();
    }

    public static void main(String[] args) {
        PluginInterfaceBuilder o = new JavaPluginInterfaceBuilder("MessagingPluginAPI", "org.kaaproject.kaa.plugin.messaging")
                .withImportStatement("java.util.Map").withImportStatement("java.lang.*").withConstant("ANOTHER_TEST", "String", "\"Hello, World\"")
                .withConstant("TEST", "String", "\"Hello, World\"").withConstant("TEST_x", "String", "\"Hello, World\"")
                .withMethodSignature("foo", "void", new String[] {}).withMethodSignature("bar", "Double", "String", "Future<Void>");
        System.out.println(new String(o.build().getFileData()));
    }
}
