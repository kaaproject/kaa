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

package org.kaaproject.kaa.server.admin.shared.services;

public enum ServiceErrorCode {

    GENERAL_ERROR("general_error", true),
    NOT_AUTHORIZED("not_authorized", false),
    PERMISSION_DENIED("permission_denied", false),
    INVALID_ARGUMENTS("invalid_arguments", false),
    INVALID_SCHEMA("invalid_schema", true),
    FILE_NOT_FOUND("file_not_found", true),
    ITEM_NOT_FOUND("item_not_found", false);

    String resKey;
    boolean showErrorMessage;

    ServiceErrorCode(String _resKey, boolean _showErrorMessage) {
        resKey = _resKey;
        showErrorMessage = _showErrorMessage;
    }

    public String getResKey() {
        return resKey;
    }

    public boolean showErrorMessage() {
        return showErrorMessage;
    }
}
