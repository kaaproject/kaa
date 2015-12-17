package org.kaaproject.kaa.server.plugin.messaging.generator.java;

import org.kaaproject.kaa.server.common.core.plugin.generator.java.JavaPluginImplementationBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaConstant;
import org.kaaproject.kaa.server.plugin.messaging.generator.common.MessagingPluginImplementationBuilder;

public class JavaMessagingPluginImplementationBuilder extends JavaPluginImplementationBuilder implements MessagingPluginImplementationBuilder {

    public JavaMessagingPluginImplementationBuilder(String name, String namespace) {
        super(name, namespace);
    }

    public JavaMessagingPluginImplementationBuilder(String name, String namespace, String template) {
        super(name, namespace, template);
    }

    @Override
    public MessagingPluginImplementationBuilder withMethodConstant() {
        return null;
    }

    @Override
    public MessagingPluginImplementationBuilder withMethodListener() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessagingPluginImplementationBuilder withEntityConverter() {
        // TODO Auto-generated method stub
        return null;
    }
}
