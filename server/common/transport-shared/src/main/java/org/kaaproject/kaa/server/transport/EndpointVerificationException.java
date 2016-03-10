package org.kaaproject.kaa.server.transport;

/**
 * An exception thrown by the server if an endpoint tries to connect with an SDK
 * configured to verify endpoint security credentials and fails the verification
 * process.
 *
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public class EndpointVerificationException extends Exception {

    private static final long serialVersionUID = 1000L;

    /**
     * Constructs a new exception with no detail message.
     */
    public EndpointVerificationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *        The detail message
     */
    public EndpointVerificationException(String message) {
        super(message);
    }
}
