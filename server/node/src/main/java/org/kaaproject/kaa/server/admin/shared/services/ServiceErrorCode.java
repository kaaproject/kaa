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

package org.kaaproject.kaa.server.admin.shared.services;

public enum ServiceErrorCode {

    GENERAL_ERROR("generalError", true),
    NOT_AUTHORIZED("notAuthorized", false),
    PERMISSION_DENIED("permissionDenied", false),
    INVALID_ARGUMENTS("invalidArguments", false),
    BAD_REQUEST_PARAMS("badRequestParams", true),
    CONFLICT("updateConflict", true),
    INVALID_SCHEMA("invalidSchema", true),
    FILE_NOT_FOUND("fileNotFound", true),
    ITEM_NOT_FOUND("itemNotFound", false);

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
