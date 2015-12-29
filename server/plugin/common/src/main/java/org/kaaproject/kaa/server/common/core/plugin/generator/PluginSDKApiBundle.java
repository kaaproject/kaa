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

package org.kaaproject.kaa.server.common.core.plugin.generator;

import java.util.List;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;

/**
 * A wrapper class for a source code file list.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public class PluginSDKApiBundle {

    private final int extensionId;
    private final String pluginInterfaceFQN;
    private final String pluginImplementationFQN;
    private final List<SdkApiFile> files;

    public PluginSDKApiBundle(int extensionId, String pluginInterfaceFQN, String pluginImplementationFQN, List<SdkApiFile> files) {
        this.extensionId = extensionId;
        this.pluginInterfaceFQN = pluginInterfaceFQN;
        this.pluginImplementationFQN = pluginImplementationFQN;
        this.files = files;
    }

    public int getExtensionId() {
        return extensionId;
    }

    public String getPluginInterfaceFQN() {
        return pluginInterfaceFQN;
    }

    public String getPluginImplementationFQN() {
        return pluginImplementationFQN;
    }

    public List<SdkApiFile> getFiles() {
        return files;
    }
}
