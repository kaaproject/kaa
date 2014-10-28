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

/**
 * Interface for the configuration delta object
 *
 * @author Yaroslav Zeygerman
 * @see DeltaType
 */
public interface ConfigurationDelta {

    /**
     * Retrieves handler id for a current delta
     *
     * @return handler id of this delta, or null if the delta doesn't
     *         have a handler id
     *
     * @see DeltaHandlerId
     */
    DeltaHandlerId getHandlerId();

    /**
     * Checks if the field was changed
     *
     * @param field the name of the field
     * @return true if the field was changed, false otherwise
     */
    boolean hasChanged(String field);

    /**
     * Retrieves the delta type of the given field
     *
     * @param field the name of the field
     * @return delta type of the field, or null if the field was not changed
     *
     * @see DeltaType
     */
    DeltaType getDeltaType(String field);
}
