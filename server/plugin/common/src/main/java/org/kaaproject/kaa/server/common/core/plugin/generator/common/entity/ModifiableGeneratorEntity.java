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

package org.kaaproject.kaa.server.common.core.plugin.generator.common.entity;

import java.util.Set;

/**
 * A source code entity the behaviour of which can be modified with reserved
 * words (such as <code>volatile</code> in Java).
 *
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public interface ModifiableGeneratorEntity extends GenericEntity {

    /**
     * Returns this entity's modifiers.
     *
     * @return This entity's modifiers
     */
    Set<String> getModifiers();

    /**
     * Formats a set of modifiers for output.
     *
     * @param modifiers A set of modifiers
     *
     * @return A formatted string that contains the given modifiers
     */
    default String formatModifiers(Set<String> modifiers) {
        StringBuilder buffer = new StringBuilder();
        modifiers.forEach(modifier -> buffer.append(modifier).append(" "));
        return buffer.toString().trim();
    }
}
