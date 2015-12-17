package org.kaaproject.kaa.server.common.core.plugin.generator.java;

import java.util.HashMap;
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

    private static final String DEFAULT_TEMPLATE_FILE = "templates/java/implementation.template";

    private static final String GETTER_BODY_TEMPLATE_FILE = "templates/java/getter.template";
    private static final String SETTER_BODY_TEMPLATE_FILE = "templates/java/setter.template";

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
    public JavaPluginImplementationBuilder withConstant(String name, String type, String value, String... modifiers) {
        this.addEntity(new JavaConstant(name, type, value, modifiers));
        return this;
    }

    @Override
    public JavaPluginImplementationBuilder withProperty(String name, String type, String... modifiers) {

        String getterBody = PluginBuilderCore.readFileAsString(GETTER_BODY_TEMPLATE_FILE);
        String setterBody = PluginBuilderCore.readFileAsString(SETTER_BODY_TEMPLATE_FILE);

        Map<String, String> values = new HashMap<>();
        values.put("${propertyName}", name);
        values.put("${propertyType}", type);

        this.addEntity(new JavaField(name, type, modifiers));
        this.addEntity(new JavaMethod(this.asGetterName(name), type, new String[] {}, new String[] { "public" }, getterBody, values));
        this.addEntity(new JavaMethod(this.asSetterName(name), null, new String[] { type }, new String[] { "public" }, setterBody, values));
        return this;
    }

    @Override
    public SdkApiFile build() {
        return super.build();
    }

    private String asGetterName(String s) {
        return "get" + s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String asSetterName(String s) {
        return "set" + s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static void main(String[] args) {
        PluginImplementationBuilder o = new JavaPluginImplementationBuilder("MessagingPlugin", "org.kaaproject.kaa.plugin.messaging").withConstant("CONSTANT",
                "Integer", null).withImportStatement("java.util.Map");
        System.out.println(new String(o.withProperty("t", "int").build().getFileData()));
    }
}
