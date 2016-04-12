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
    
    private final EndpointVerificationError error;
    
    public EndpointVerificationException(EndpointVerificationError error, String msg) {
        super(msg);
        this.error = error;
    }

    public EndpointVerificationError getError() {
        return error;
    }
}
