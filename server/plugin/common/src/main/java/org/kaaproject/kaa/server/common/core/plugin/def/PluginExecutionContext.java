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

package org.kaaproject.kaa.server.common.core.plugin.def;

import java.util.UUID;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaMessage;

/**
 * An object used by plugin instaces to respond to incoming messages.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @see org.kaaproject.kaa.server.common.core.plugin.instance.KaaPlugin
 *
 * @since v1.0.0
 */
public interface PluginExecutionContext {

    void tellEndpoint(EndpointObjectHash endpointKey, KaaMessage sdkMessage);

    void tellPlugin(UUID uid, KaaMessage sdkMessage);
}
