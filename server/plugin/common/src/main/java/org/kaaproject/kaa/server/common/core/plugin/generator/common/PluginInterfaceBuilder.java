package org.kaaproject.kaa.server.common.core.plugin.generator.common;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;

public interface PluginInterfaceBuilder {

    PluginInterfaceBuilder withImportStatement(String body);

    PluginInterfaceBuilder withConstant(String name, String type, String value);

    PluginInterfaceBuilder withMethodSignature(String name, String returnType, String... paramTypes);

    SdkApiFile generateFile();
}
