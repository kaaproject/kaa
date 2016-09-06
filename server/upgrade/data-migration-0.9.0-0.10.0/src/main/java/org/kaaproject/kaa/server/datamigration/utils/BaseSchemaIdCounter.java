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

package org.kaaproject.kaa.server.datamigration.utils;

public class BaseSchemaIdCounter {
    private static BaseSchemaIdCounter instance;
    private Long value;
    private static boolean isInitMethodCalled;

    // can be called only once
    public static void setInitValue(Long value) {
        getInstance();
        if (isInitMethodCalled) {
            return;
        }
        isInitMethodCalled = true;
        instance.value = value;
    }

    public Long getAndShift(Long shift) {
        Long oldValue = value;
        value += shift;
        return oldValue;
    }

    private BaseSchemaIdCounter() {

    }

    public static BaseSchemaIdCounter getInstance() {
        if (instance == null) {
            instance = new BaseSchemaIdCounter();
        }
        return instance;
    }

}
