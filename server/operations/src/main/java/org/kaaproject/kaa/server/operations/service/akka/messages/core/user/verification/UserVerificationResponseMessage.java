package org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification;

import java.util.UUID;

import org.kaaproject.kaa.server.common.verifier.UserVerifierErrorCode;

public class UserVerificationResponseMessage {

    private final UUID requestId;
    private boolean success;
    private UserVerifierErrorCode errorCode;
    private String failureReason;

    private UserVerificationResponseMessage(UUID requestId, boolean success, UserVerifierErrorCode errorCode, String failureReason) {
        super();
        this.requestId = requestId;
        this.success = success;
        this.errorCode = errorCode;
        this.failureReason = failureReason;
    }

    public static UserVerificationResponseMessage success(UUID requestId) {
        return new UserVerificationResponseMessage(requestId, true, null, null);
    }

    public static UserVerificationResponseMessage failure(UUID requestId, UserVerifierErrorCode errorCode) {
        return failure(requestId, errorCode, null);
    }

    public static UserVerificationResponseMessage failure(UUID requestId, UserVerifierErrorCode errorCode, String failureReason) {
        return new UserVerificationResponseMessage(requestId, false, errorCode, failureReason);
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
