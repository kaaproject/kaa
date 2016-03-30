package org.kaaproject.kaa.server.transport;

/**
 * An exception thrown by the server if the endpoint credentials are revoked
 * during existing communication session.
 *
 * @author Andrew Shvayka
 *
 * @since v0.9.0
 */
public class EndpointRevocationException extends Exception {

    private static final long serialVersionUID = 1000L;

    /**
     * Constructs a new exception with no detail message.
     */
    public EndpointRevocationException() {
        super();
    }
}
