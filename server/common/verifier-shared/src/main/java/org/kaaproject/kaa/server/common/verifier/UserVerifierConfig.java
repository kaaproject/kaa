package org.kaaproject.kaa.server.common.verifier;

import org.apache.avro.Schema;

/**
 * Represents configuration of particular {@link UserVerifier}.
 * 
 * @author Andrew Shvayka
 *
 */
public interface UserVerifierConfig {

    /**
     * Returns the verifier id. The verifier id must be unique.
     * 
     * @return the verifier id
     */
    int getId();
    
    /**
     * Returns the verifier name. There is no strict rule for this
     * name to be unique.
     * 
     * @return the verifier name
     */
    String getName();
    
    /**
     * Returns the class name of the {@link UserVerifier} implementation..
     * 
     * @return the class name of the {@link UserVerifier} implementation
     */
    String getUserVerifierClass();
    
    /**
     * Returns the avro schema of the {@link Transport} configuration.
     * 
     * @return the avro schema of the {@link Transport} configuration
     */
    Schema getConfigSchema();
}
