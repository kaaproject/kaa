package org.kaaproject.kaa.server.common.core.plugin.generator.java;

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

    public JavaPluginImplementationBuilder(String name, String namespace) {
        this(name, namespace, PluginBuilderCore.readFileAsString(DEFAULT_TEMPLATE_FILE));
    }

    public JavaPluginImplementationBuilder(String name, String namespace, String template) {
        super(name, namespace, template);
    }

    @Override
    public PluginImplementationBuilder withEntity(GeneratorEntity entity) {
        this.addEntity(entity);
        return this;
    }

    @Override
    public PluginImplementationBuilder withImportStatement(String body) {
        this.addEntity(new JavaImportStatement(body));
        return this;
    }

    @Override
    public PluginImplementationBuilder withConstant(String name, String type, String value) {
        this.addEntity(new JavaConstant(name, type, value));
        return this;
    }

    @Override
    public PluginImplementationBuilder withProperty(String name, String type) {
        this.addEntity(new JavaField(name, type));
        this.addEntity(new JavaMethod(this.asGetterName(name), type, new String[] {}, null));
        this.addEntity(new JavaMethod(this.asSetterName(name), null, new String[] { type }, null));
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
        PluginImplementationBuilder o = new JavaPluginImplementationBuilder("MessagingPlugin", "org.kaaproject.kaa.plugin.messaging");
        System.out.println(new String(o.withProperty("t", "int").build().getFileData()));
    }

}
