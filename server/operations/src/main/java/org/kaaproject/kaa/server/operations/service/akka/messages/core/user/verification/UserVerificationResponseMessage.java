package org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification;

import org.kaaproject.kaa.server.common.verifier.UserVerifierErrorCode;

public class UserVerificationResponseMessage {

    private boolean success;
    private UserVerifierErrorCode errorCode;
    private String failureReason;

    private UserVerificationResponseMessage(boolean success, UserVerifierErrorCode errorCode, String failureReason) {
        super();
        this.success = success;
        this.errorCode = errorCode;
        this.failureReason = failureReason;
    }

    public static UserVerificationResponseMessage success() {
        return new UserVerificationResponseMessage(true, null, null);
    }

    public static UserVerificationResponseMessage failure(UserVerifierErrorCode errorCode) {
        return failure(errorCode, null);
    }

    public static UserVerificationResponseMessage failure(UserVerifierErrorCode errorCode, String failureReason) {
        return new UserVerificationResponseMessage(false, errorCode, failureReason);
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
        builder.append("UserVerificationResponseMessage [success=");
        builder.append(success);
        builder.append(", errorCode=");
        builder.append(errorCode);
        builder.append(", failureReason=");
        builder.append(failureReason);
        builder.append("]");
        return builder.toString();
    }
}
