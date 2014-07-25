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

package org.kaaproject.kaa.client.configuration.delta;

import java.util.List;

/**
 * Interface of delta type object
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface DeltaType {

    /**
     * Checks if the field was set to default value
     *
     * @return true if the field set to default, false otherwise
     *
     */
    boolean isDefault();

    /**
     * Checks if the container field have been cleared
     *
     * @return true if the container field is cleared, false if not (or field is not array)
     *
     */
    boolean isReset();

    /**
     * Retrieves new field value
     *
     * @return value of the appropriate type ({@link ConfigurationDelta} for record items), or null if there is no new value (or field is array)
     *
     */
    Object getNewValue();

    /**
     * Retrieves list of removed addressable items
     *
     * @return list which contains handlers of removed items, null if there is no removed items (or field is not array)
     *
     */
    List<DeltaHandlerId> getRemovedItems();

    /**
     * Retrieves list of added items
     *
     * @return list of added items. (List of {@link ConfigurationDelta} for complex items), null if there is no added items (or field is not array)
     *
     */
    List<Object> getAddedItems();

}
