/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.operations.service.delta.merge;

import java.util.List;

import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;


/**
 * Performs configuration merge from given configurations.
 */
public interface ConfigurationMerger {

    /**
     * Merges configurations into single configuration.
     *
     * @param endpointGroups list of endpoint groups that configurations are associated with 
     * @param configurations list of configuration to merge from
     * @param configurationSchema configuration schema for input configurations
     * @return the merged configuration
     * @throws MergeException the configuration merger exception
     */
    byte[] merge(List<EndpointGroupDto> endpointGroups, List<ConfigurationDto> configurations, ConfigurationSchemaDto configurationSchema) throws MergeException;
}
