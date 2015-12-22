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

package org.kaaproject.kaa.server.common.core.plugin.generator.common;

import java.util.Map;

/**
 * A builder that produces plugin API interface.
 *
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public interface PluginInterfaceBuilder extends PluginFileBuilder<PluginInterfaceBuilder> {

    /**
     * Adds a method signature to this builder.
     *
     * @param name The name of a method signature to add
     * @param returnType The return type of a method signature to add
     * @param paramTypes The parameter types of a signature to add
     * @param modifiers The modifiers of a signature to add
     *
     * @return A reference to this builder
     */
    PluginInterfaceBuilder withMethodSignature(String name, String returnType, String[] paramTypes, String[] modifiers);

    /**
     * Adds a method signature to this builder.
     *
     * @param name The name of a method signature to add
     * @param returnType The return type of a method signature to add
     * @param params The parameters of a method signature to add
     * @param modifiers The modifiers of a signature to add
     *
     * @return A reference to this builder
     */
    PluginInterfaceBuilder withMethodSignature(String name, String returnType, Map<String, String> params, String[] modifiers);
}
