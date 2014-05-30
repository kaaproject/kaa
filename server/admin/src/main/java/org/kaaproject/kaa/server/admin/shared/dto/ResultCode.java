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

package org.kaaproject.kaa.server.admin.shared.dto;

public enum ResultCode {

    OK("ok"),
    USERNAME_EXISTS("username_exists"),
    EMAIL_EXISTS("email_exists"),
    USER_NOT_FOUND("user_not_found"),
    USER_EMAIL_NOT_FOUND("user_email_not_found"),
    OLD_PASSWORD_MISMATCH("old_password_mismatch"),
    BAD_PASSWORD_STRENGTH("bad_password_strength");

    String key;

    ResultCode(String _key) {
        key = _key;
    }

    public String getResourceKey() {
        return key;
    }
}
