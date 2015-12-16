package org.kaaproject.kaa.server.common.core.plugin.generator.common;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;

public interface PluginImplementationBuilder {

    PluginImplementationBuilder withImportStatement(String body);

    PluginImplementationBuilder withConstant(String name, String type, String value);

    PluginImplementationBuilder withProperty(String name, String type);

    SdkApiFile generateFile();
}
