/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.core.plugin.instance;

import org.kaaproject.kaa.server.common.core.plugin.def.PluginExecutionContext;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;

/**
 * A plugin instance.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public interface KaaPlugin {

    /**
     * Initializes a plugin instance.
     *
     * @param context A context to initialize the plugin instance with
     *
     * @throws PluginLifecycleException - if an exception occures during
     *             initialization.
     */
    void init(PluginInitContext context) throws PluginLifecycleException;

    /**
     * Processes an incoming plugin message.
     *
     * @param message An incoming plugin message
     * @param context A context to process the message with
     */
    void onPluginMessage(KaaPluginMessage message, PluginExecutionContext context);

    /**
     * Terminates a plugin instance.
     *
     * @throws PluginLifecycleException - if an exception occures during
     *             termination.
     */
    void stop() throws PluginLifecycleException;
}
