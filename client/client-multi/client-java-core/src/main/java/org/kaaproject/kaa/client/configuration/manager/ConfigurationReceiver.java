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

package org.kaaproject.kaa.client.configuration.manager;

import org.kaaproject.kaa.client.common.CommonRecord;

/**
 * Interface for subscriber which is going to receive full configuration
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface ConfigurationReceiver {

    /**
     * This callback will be called on any configuration update
     *
     * @param configuration full configuration in common objects
     * @see CommonRecord
     */
    void onConfigurationUpdated(CommonRecord configuration);

}
