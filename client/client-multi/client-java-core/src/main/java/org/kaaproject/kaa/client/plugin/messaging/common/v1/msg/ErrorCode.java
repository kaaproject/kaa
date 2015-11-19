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
package org.kaaproject.kaa.client.plugin.messaging.common.v1.msg;

public enum ErrorCode {

    NO_LISTENER_ASSIGNED(1), EXECUTION_ERROR(2), SERIALIZATION_ERROR(3), NULL_RESPONSE_ERROR(3);

    private final int code;

    private ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ErrorCode get(int errorCode) {
        for (ErrorCode type : ErrorCode.values()) {
            if (type.code == errorCode) {
                return type;
            }
        }
        throw new IllegalArgumentException("No ErrorCode with code: " + errorCode);
    }

}
