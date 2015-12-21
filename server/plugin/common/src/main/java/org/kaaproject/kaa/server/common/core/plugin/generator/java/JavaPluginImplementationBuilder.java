package org.kaaproject.kaa.server.common.core.plugin.generator.java;

import java.util.Map;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilderCore;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginImplementationBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.GeneratorEntity;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaConstant;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaField;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaImportStatement;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaMethod;

public class JavaPluginImplementationBuilder extends PluginBuilderCore implements PluginImplementationBuilder {

    protected static final String DEFAULT_TEMPLATE_FILE = "templates/java/implementation.template";

    public JavaPluginImplementationBuilder(String name, String namespace) {
        this(name, namespace, PluginBuilderCore.readFileAsString(DEFAULT_TEMPLATE_FILE));
    }

    public JavaPluginImplementationBuilder(String name, String namespace, String template) {
        super(name, namespace, template);
    }

    @Override
    public JavaPluginImplementationBuilder withEntity(GeneratorEntity entity) {
        this.addEntity(entity);
        return this;
    }

    @Override
    public JavaPluginImplementationBuilder withImportStatement(String body) {
        this.addEntity(new JavaImportStatement(body));
        return this;
    }

    @Override
    public JavaPluginImplementationBuilder withConstant(String name, String type, String value) {
        return this.withConstant(name, type, value, new String[] {});
    }

    @Override
    public JavaPluginImplementationBuilder withConstant(String name, String type, String value, String... modifiers) {
        this.addEntity(new JavaConstant(name, type, value, modifiers));
        return this;
    }

    @Override
    public JavaPluginImplementationBuilder withProperty(String name, String type, String... modifiers) {
        this.addEntity(new JavaField(name, type, modifiers));
        this.addEntity(JavaMethod.getter(name, type));
        this.addEntity(JavaMethod.setter(name, type));
        return this;
    }

    @Override
    public JavaPluginImplementationBuilder withMethod(String name, String returnType, Map<String, String> params, String[] modifiers, String body,
            Map<String, String> values) {

        this.addEntity(new JavaMethod(name, returnType, params, modifiers, body, values));
        return this;
    }

    @Override
    public SdkApiFile build() {
        return super.build();
    }

    public static void main(String[] args) {
        PluginImplementationBuilder o = new JavaPluginImplementationBuilder("MessagingPlugin", "org.kaaproject.kaa.plugin.messaging").withConstant("CONSTANT",
                "Integer", null, new String[] { "volatile", "private" }).withImportStatement("java.util.Map");
        System.out.println(new String(o.withProperty("t", "int").build().getFileData()));
    }
}
