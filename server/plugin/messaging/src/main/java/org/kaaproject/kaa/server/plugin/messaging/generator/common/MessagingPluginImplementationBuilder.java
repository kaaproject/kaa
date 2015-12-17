package org.kaaproject.kaa.server.plugin.messaging.generator.common;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginImplementationBuilder;

public interface MessagingPluginImplementationBuilder extends PluginImplementationBuilder {

    MessagingPluginImplementationBuilder withMethodConstant();

    MessagingPluginImplementationBuilder withMethodListener();

    MessagingPluginImplementationBuilder withEntityConverter();
}
