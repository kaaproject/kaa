package org.kaaproject.kaa.server.common.dao.exception;

public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = -8231959965606087061L;

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

