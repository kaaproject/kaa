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

package org.kaaproject.kaa.server.common.plugin;

import org.apache.avro.Schema;

/**
 * Represents configuration of particular plugin.
 * 
 * @author Igor Kulikov
 *
 */
public interface PluginConfig {

    /**
     * Returns the plugin display name. There is no strict rule for this
     * name to be unique.
     * 
     * @return the plugin display name
     */
    String getPluginTypeName();
    
    /**
     * Returns the class name of the plugin implementation.
     *
     * @return the class name of the plugin implementation
     */
    String getPluginClassName();
    
    /**
     * Returns the avro schema of the plugin configuration.
     *
     * @return the avro schema of the plugin configuration
     */
    Schema getPluginConfigSchema();
    
}
