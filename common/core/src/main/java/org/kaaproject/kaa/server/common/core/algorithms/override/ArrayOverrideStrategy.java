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

package org.kaaproject.kaa.server.common.core.algorithms.override;


/**
 * The Enum ArrayMergeStrategy.
 */
public enum ArrayOverrideStrategy {

    /** The replace. */
    REPLACE("replace"),
    /** The append. */
    APPEND("append");

    /** The name. */
    private final String name;

    /**
     * Instantiates a new array merge strategy.
     *
     * @param name the name
     */
    private ArrayOverrideStrategy(String name) {
        this.name = name;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the by name.
     *
     * @param name the name
     * @return the by name
     */
    public static ArrayOverrideStrategy getByName(String name) {
        for (ArrayOverrideStrategy strategy : ArrayOverrideStrategy.values()) {
            if (strategy.getName().equalsIgnoreCase(name)) {
                return strategy;
            }
        }
        return null;
    }
}
