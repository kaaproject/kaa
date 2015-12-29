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

/**
 * A source code entity (such as import statement or method signature) used by
 * plugin API builders to produce output.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public interface GenericEntity {

    /**
     * Returns the template variable this entity is a part of.
     *
     * @return The template variable this entity is a part of
     */
    TemplateVariable getTemplateVariable();

    /**
     * Returns the body of this entity.
     *
     * @return The body of this entity
     */
    String getBody();

    /**
     * Determines whether this entity must be terminated with a semicolon.
     *
     * @return <code>true</code> if the body of this entity must be terminated
     *         <code>false</code> otherwise
     */
    default boolean requiresTermination() {
        return true;
    }

    /**
     * Returns the number of empty lines that must follow the body of this
     * entity.
     *
     * @return The number of empty lines that must follow the body of this
     *         entity
     */
    default int emptyLines() {
        return 0;
    }
}
