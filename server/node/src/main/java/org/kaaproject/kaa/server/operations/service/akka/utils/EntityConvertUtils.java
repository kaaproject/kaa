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

package org.kaaproject.kaa.server.operations.service.akka.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification.UserVerificationResponseMessage;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;
import org.kaaproject.kaa.server.sync.LogDeliveryErrorCode;
import org.kaaproject.kaa.server.sync.LogDeliveryStatus;
import org.kaaproject.kaa.server.sync.LogServerSync;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.kaaproject.kaa.server.sync.UserAttachResponse;
import org.kaaproject.kaa.server.sync.UserVerifierErrorCode;

public class EntityConvertUtils {

    private EntityConvertUtils() {
    }

    public static LogServerSync convert(Map<Integer, LogDeliveryMessage> responseMap) {
        List<LogDeliveryStatus> statusList = new ArrayList<>();
        for (Entry<Integer, LogDeliveryMessage> response : responseMap.entrySet()) {
            LogDeliveryMessage message = response.getValue();
            statusList.add(new LogDeliveryStatus(response.getKey(), message.isSuccess() ? SyncStatus.SUCCESS : SyncStatus.FAILURE,
                    EntityConvertUtils.toErrorCode(message.getErrorCode())));
        }
        return new LogServerSync(statusList);
    }

    public static UserAttachResponse convert(UserVerificationResponseMessage value) {
        UserAttachResponse response = new UserAttachResponse(value.isSuccess() ? SyncStatus.SUCCESS : SyncStatus.FAILURE,
                EntityConvertUtils.toErrorCode(value.getErrorCode()), value.getFailureReason());
        return response;
    }

    public static UserVerifierErrorCode toErrorCode(org.kaaproject.kaa.server.common.verifier.UserVerifierErrorCode errorCode) {
        if (errorCode == null) {
            return null;
        }
        switch (errorCode) {
        case NO_VERIFIER_CONFIGURED:
            return UserVerifierErrorCode.NO_VERIFIER_CONFIGURED;
        case TOKEN_INVALID:
            return UserVerifierErrorCode.TOKEN_INVALID;
        case TOKEN_EXPIRED:
            return UserVerifierErrorCode.TOKEN_EXPIRED;
        case INTERNAL_ERROR:
            return UserVerifierErrorCode.INTERNAL_ERROR;
        case CONNECTION_ERROR:
            return UserVerifierErrorCode.CONNECTION_ERROR;
        case REMOTE_ERROR:
            return UserVerifierErrorCode.REMOTE_ERROR;
        default:
            return UserVerifierErrorCode.OTHER;
        }
    }

    public static LogDeliveryErrorCode toErrorCode(org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryErrorCode errorCode) {
        if (errorCode == null) {
            return null;
        }
        switch (errorCode) {
        case APPENDER_INTERNAL_ERROR:
            return LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR;
        case NO_APPENDERS_CONFIGURED:
            return LogDeliveryErrorCode.NO_APPENDERS_CONFIGURED;
        case REMOTE_CONNECTION_ERROR:
            return LogDeliveryErrorCode.REMOTE_CONNECTION_ERROR;
        case REMOTE_INTERNAL_ERROR:
            return LogDeliveryErrorCode.REMOTE_INTERNAL_ERROR;
        default:
            return null;
        }
    }

    public static List<EventClassFamilyVersion> convertToECFVersions(List<EventClassFamilyVersionStateDto> ecfVersionStates) {
        List<EventClassFamilyVersion> result = new ArrayList<>(ecfVersionStates.size());
        for (EventClassFamilyVersionStateDto dto : ecfVersionStates) {
            result.add(new EventClassFamilyVersion(dto.getEcfId(), dto.getVersion()));
        }
        return result;
    }
}
