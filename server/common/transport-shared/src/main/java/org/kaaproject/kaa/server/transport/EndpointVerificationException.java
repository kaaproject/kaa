package org.kaaproject.kaa.server.transport;

/**
 * An exception thrown by the server if the verification process fails while
 * endpoint setup connection to the server.
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
}
