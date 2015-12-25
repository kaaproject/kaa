package org.kaaproject.kaa.server.control.service.exception;

public class KaaPluginLoadException extends Exception {

    private static final long serialVersionUID = -5987219829591697207L;

    public KaaPluginLoadException(String message) {
        super(message);
    }

    public KaaPluginLoadException(Throwable cause) {
        super(cause);
    }
}
