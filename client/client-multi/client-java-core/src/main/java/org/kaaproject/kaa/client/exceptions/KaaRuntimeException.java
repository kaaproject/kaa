package org.kaaproject.kaa.client.exceptions;

public class KaaRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -691997490865841478L;

    public KaaRuntimeException(Exception cause) {
        super(cause);
    }
}
