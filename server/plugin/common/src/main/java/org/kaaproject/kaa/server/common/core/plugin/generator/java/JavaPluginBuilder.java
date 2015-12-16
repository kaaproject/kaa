package org.kaaproject.kaa.server.common.core.plugin.generator.java;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginImplementationBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginInterfaceBuilder;

public class JavaPluginBuilder implements PluginBuilder {

    private String template;

    @Override
    public PluginBuilder fromTemplate(String template) {
        this.template = template;
        return this;
    }

    @Override
    public PluginInterfaceBuilder createInterface(String name, String namespace) {
        JavaPluginInterfaceBuilder object = new JavaPluginInterfaceBuilder(name, namespace);
        object.setTemplate(this.template);
        return object;
    }

    @Override
    public PluginImplementationBuilder createImplementation(String name, String namespace) {
        JavaPluginImplementationBuilder object = new JavaPluginImplementationBuilder(name, namespace);
        object.setTemplate(this.template);
        return object;
    }
}
