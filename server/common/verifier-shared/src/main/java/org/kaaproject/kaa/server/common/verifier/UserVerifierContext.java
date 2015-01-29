package org.kaaproject.kaa.server.common.verifier;

import org.kaaproject.kaa.common.dto.user.UserVerifierDto;


/**
 * Provides a context for verifier initialization parameters and {@link MessageHandler
 * }.
 * 
 * @author Andrew Shvayka
 *
 */
public class UserVerifierContext {

    private final UserVerifierDto verifierDto;

    public UserVerifierContext(UserVerifierDto verifierDto) {
        super();
        this.verifierDto = verifierDto;
    }

    public UserVerifierDto getVerifierDto() {
        return verifierDto;
    }
}
