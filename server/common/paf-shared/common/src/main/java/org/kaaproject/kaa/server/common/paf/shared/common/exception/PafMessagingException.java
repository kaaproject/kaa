package org.kaaproject.kaa.server.common.paf.shared.common.exception;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

public class PafMessagingException extends MessagingException {

    private static final long serialVersionUID = 4467764097902955715L;
    
    private final PafErrorCode errorCode;

    public PafMessagingException(Message<?> message, PafErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PafMessagingException(Message<?> message, PafErrorCode errorCode, String description) {
        super(message, description);
        this.errorCode = errorCode;
    }

    public PafErrorCode getErrorCode() {
        return errorCode;
    }

}
