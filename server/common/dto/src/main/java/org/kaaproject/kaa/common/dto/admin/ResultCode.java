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

package org.kaaproject.kaa.common.dto.admin;

public enum ResultCode {

    OK("ok"),
    USERNAME_EXISTS("usernameExists"),
    EMAIL_EXISTS("emailExists"),
    USER_NOT_FOUND("userNotFound"),
    USER_EMAIL_NOT_FOUND("userEmailNotFound"),
    USER_OR_EMAIL_NOT_FOUND("userOrEmailNotFound"),
    USER_EMAIL_NOT_DEFINED("userEmailNotDefined"),
    OLD_PASSWORD_MISMATCH("oldPasswordMismatch"),
    BAD_PASSWORD_STRENGTH("badPasswordStrength");

    String key;

    ResultCode(String key) {
        this.key = key;
    }

    public String getResourceKey() {
        return key;
    }
}
