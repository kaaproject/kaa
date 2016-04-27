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

package org.kaaproject.kaa.common.dto;

public enum ChangeType {

    UPDATE,
    DELETE,
    INSERT,
    ADD_TOPIC,
    REMOVE_TOPIC,
    UPDATE_WEIGHT,
    ADD_CONF,
    REMOVE_CONF,
    ADD_PROF,
    REMOVE_PROF,
    REMOVE_GROUP,
    REMOVE_CONF_VERSION,
    REMOVE_PROF_VERSION,
    REMOVE_NOTIFICATION_VERSION;


    public static ChangeType typeFromString(String stringType) {
        for (ChangeType type : ChangeType.values()) {
            if (type.name().equalsIgnoreCase(stringType)) {
                return type;
            }
        }
        return null;
    }
}
