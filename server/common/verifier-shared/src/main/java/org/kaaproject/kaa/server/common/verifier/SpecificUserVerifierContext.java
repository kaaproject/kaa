package org.kaaproject.kaa.server.common.verifier;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;

/**
 * Provides a context for the user verifier initialization parameters with the specific avro-decoded configuration.
 * 
 * @author Andrew Shvayka
 *
 */
public class SpecificUserVerifierContext<T extends SpecificRecordBase> extends UserVerifierContext {

    private final T configuration;
    
    public SpecificUserVerifierContext(UserVerifierDto verifierDto, T configuration) {
        super(verifierDto);
        this.configuration = configuration;
    }

    public T getConfiguration() {
        return configuration;
    }
}
