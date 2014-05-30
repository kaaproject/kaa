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

import java.util.HashMap;
import java.util.Map;

/**
 * Default {@link ConfigurationDelta} implementation
 *
 * @author Yaroslav Zeygerman
 *
 */
public class DefaultConfigurationDelta implements ConfigurationDelta {
    private final Map<String, DeltaType> fieldDeltaTypes = new HashMap<String, DeltaType>();
    private DeltaHandlerId handlerId;

    DefaultConfigurationDelta() {

    }

    DefaultConfigurationDelta(DeltaHandlerId handlerId) {
        this.handlerId = handlerId;
    }

    public void updateFieldDeltaType(String field, DeltaType type) {
        fieldDeltaTypes.put(field, type);
    }

    @Override
    public DeltaHandlerId getHandlerId() {
        return handlerId;
    }

    @Override
    public boolean hasChanged(String field) {
        return fieldDeltaTypes.containsKey(field);
    }

    @Override
    public DeltaType getDeltaType(String field) {
        return fieldDeltaTypes.get(field);
    }
}
