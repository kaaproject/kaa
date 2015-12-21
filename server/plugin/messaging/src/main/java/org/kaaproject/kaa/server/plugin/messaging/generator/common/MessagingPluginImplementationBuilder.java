package org.kaaproject.kaa.server.plugin.messaging.generator.common;

import java.util.Map;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginImplementationBuilder;

public interface MessagingPluginImplementationBuilder extends PluginImplementationBuilder {

    MessagingPluginImplementationBuilder withMethodConstant(String method, String[] paramTypes, int id);

    MessagingPluginImplementationBuilder withMethodListener(String name, String type);

    MessagingPluginImplementationBuilder withEntityConverter(String name, String type);

    MessagingPluginImplementationBuilder withEntityMessageHandlersMapping(Map<String, Integer> handlersMapping);

    MessagingPluginImplementationBuilder withVoidMessageHandlersMapping(Map<String, Integer> handlersMapping);

    MessagingPluginImplementationBuilder withMessageHandler(String body, Map<String, String> values);
}
