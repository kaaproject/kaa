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
 * A builder that produces plugin API implementation.
 *
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public interface PluginImplementationBuilder extends PluginFileBuilder<PluginImplementationBuilder> {

    /**
     * Adds a property (a field and an appropriate accessor and mutator) to this
     * builder.
     *
     * @param name The name of a property to add
     * @param type The type of a property to add
     * @param modifiers The modifiers of a property to add
     *
     * @return A reference to this builder
     */
    PluginImplementationBuilder withProperty(String name, String type, String... modifiers);

    /**
     * Adds a method to this builder.
     *
     * @param name The name of a method to add
     * @param returnType The return type of a method to add
     * @param params The parameters of a method to add
     * @param modifiers The modifiers of a method to add
     * @param body The body of a method to add
     * @param values The values to be inserted into the method's body
     *
     * @return A reference to this builder
     */
    PluginImplementationBuilder withMethod(String name, String returnType, Map<String, String> params, String[] modifiers, String body,
            Map<String, String> values);
}
