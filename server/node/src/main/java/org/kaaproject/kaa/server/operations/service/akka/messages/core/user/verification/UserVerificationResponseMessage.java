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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification;

import java.util.UUID;

import org.kaaproject.kaa.server.common.verifier.UserVerifierErrorCode;

public class UserVerificationResponseMessage {

    private final UUID requestId;
    private final String userId;
    private final boolean success;
    private final UserVerifierErrorCode errorCode;
    private final String failureReason;

    private UserVerificationResponseMessage(UUID requestId, String userId, boolean success, UserVerifierErrorCode errorCode, String failureReason) {
        super();
        this.requestId = requestId;
        this.userId = userId;
        this.success = success;
        this.errorCode = errorCode;
        this.failureReason = failureReason;
    }

    public static UserVerificationResponseMessage success(UUID requestId, String userId) {
        return new UserVerificationResponseMessage(requestId, userId, true, null, null);
    }

    public static UserVerificationResponseMessage failure(UUID requestId, String userId, UserVerifierErrorCode errorCode) {
        return failure(requestId, userId, errorCode, null);
    }

    public static UserVerificationResponseMessage failure(UUID requestId, String userId, UserVerifierErrorCode errorCode, String failureReason) {
        return new UserVerificationResponseMessage(requestId, userId, false, errorCode, failureReason);
    }

    public UUID getRequestId() {
        return requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public UserVerifierErrorCode getErrorCode() {
        return errorCode;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserVerificationResponseMessage [requestId=");
        builder.append(requestId);
        builder.append(", success=");
        builder.append(success);
        builder.append(", errorCode=");
        builder.append(errorCode);
        builder.append(", failureReason=");
        builder.append(failureReason);
        builder.append("]");
        return builder.toString();
    }
}
