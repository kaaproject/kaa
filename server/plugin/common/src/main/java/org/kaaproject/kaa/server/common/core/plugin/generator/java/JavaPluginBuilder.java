/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.core.plugin.generator.java;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginImplementationBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginInterfaceBuilder;

/**
 * A master Java plugin API builder.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public interface JavaPluginBuilder extends PluginBuilder {

    /**
     * Tells the builder to use the given plugin API interface.
     *
     * @param interfaceBuilder A plugin API interface builder
     *
     * @return A reference to this builder
     */
    PluginBuilder withInterface(PluginInterfaceBuilder interfaceBuilder);

    /**
     * Tells the builder to use the given plugin API implementation.
     *
     * @param implementationBuilder A plugin API implementation builder
     *
     * @return A reference to this builder
     */
    PluginBuilder withImplementation(PluginImplementationBuilder implementationBuilder);
}
