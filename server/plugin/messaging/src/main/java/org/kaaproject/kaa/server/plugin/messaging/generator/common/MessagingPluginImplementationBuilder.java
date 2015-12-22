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

package org.kaaproject.kaa.server.plugin.messaging.generator.common;

import java.util.Map;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginImplementationBuilder;

/**
 * A specific endpoint messaging builder that produces plugin API
 * implementation.
 *
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public interface MessagingPluginImplementationBuilder extends PluginImplementationBuilder {

    /**
     * Adds a constant that references the given method.
     *
     * @param method A method name
     * @param paramTypes The parameter types of the given method
     * @param id The value of the constant
     *
     * @return A reference to this builder
     */
    MessagingPluginImplementationBuilder withMethodConstant(String method, String[] paramTypes, int id);

    /**
     * Adds a method listener field.
     *
     * @param name The name of a field to add
     * @param type The type of a method listener add
     *
     * @return A reference to this builder
     */
    MessagingPluginImplementationBuilder withMethodListener(String name, String type);

    /**
     * Adds an entity class converter.
     *
     * @param name An entity class converter name
     * @param type An entity class converter type
     *
     * @return A reference to this builder
     */
    MessagingPluginImplementationBuilder withEntityConverter(String name, String type);

    /**
     * Adds a method that delegates entity messages to appropriate handlers.
     *
     * @param handlersMapping Maps a method name to its method constant
     *
     * @return A reference to this builder
     */
    MessagingPluginImplementationBuilder withEntityMessageHandlersMapping(Map<String, Integer> handlersMapping);

    /**
     * Adds a method that delegates void messages to appropriate handlers.
     *
     * @param handlersMapping Maps a method name to its method constant
     *
     * @return A reference to this builder
     */
    MessagingPluginImplementationBuilder withVoidMessageHandlersMapping(Map<String, Integer> handlersMapping);
}
