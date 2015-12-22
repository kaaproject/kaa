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

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.GenericEntity;

/**
 * The core functionality of a plugin source file builder.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @param <T> A specific plugin builder type
 */
public interface PluginFileBuilder<T> {

    /**
     * Adds the given generic entity to this builder.
     *
     * @param entity An entity to add
     *
     * @return A reference to this builder
     */
    T withEntity(GenericEntity entity);

    /**
     * Adds the given body as an import statement.
     *
     * @param body An import statement body
     *
     * @return A reference to this builder
     */
    T withImportStatement(String body);

    /**
     * Adds a constant to this builder.
     *
     * @param name The name of a constant to add
     * @param type The type of a constant to add
     * @param value The value of a constant to add
     *
     * @return A reference to this builder
     */
    T withConstant(String name, String type, String value);

    /**
     * Adds a constant to this builder.
     *
     * @param name The name of a constant to add
     * @param type The type of a constant to add
     * @param value The value of a constant to add
     * @param modifiers The modifiers of a constant to add
     *
     * @return A reference to this builder
     */
    T withConstant(String name, String type, String value, String... modifiers);

    /**
     * Generates a plugin API source file.
     *
     * @return A plugin API source file
     */
    SdkApiFile build();
}
