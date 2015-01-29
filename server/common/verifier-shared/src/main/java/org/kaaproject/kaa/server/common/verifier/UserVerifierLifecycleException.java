package org.kaaproject.kaa.server.common.verifier;

/**
 * Indicates an exception during user verifier lifecycle operations - initialization, startup, shutdown, etc.
 * 
 * @author Andrew Shvayka
 *
 */
public class UserVerifierLifecycleException extends Exception{
    /**
     * The generated value.
     */
    private static final long serialVersionUID = 7248305662659836419L;

    public UserVerifierLifecycleException(Throwable cause) {
        super(cause);
    }
}
