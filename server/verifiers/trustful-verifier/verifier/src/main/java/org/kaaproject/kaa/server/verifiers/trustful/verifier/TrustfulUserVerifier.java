package org.kaaproject.kaa.server.verifiers.trustful.verifier;

import org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
import org.kaaproject.kaa.server.verifiers.trustful.config.gen.TrustfulAvroConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustfulUserVerifier extends AbstractKaaUserVerifier<TrustfulAvroConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(TrustfulUserVerifier.class);

    @Override
    public void init(UserVerifierContext context, TrustfulAvroConfig configuration) {
        LOG.info("Initializing user verifier with context {} and configuration {}", context, configuration);
    }

    @Override
    public void checkAccessToken(String userExternalId, String accessToken, UserVerifierCallback callback) {
        LOG.trace("Received user verification request for user {} and access token {}", userExternalId, accessToken);
        callback.onSuccess();
    }

    @Override
    public void start() {
        LOG.info("user verifier started");
    }

    @Override
    public void stop() {
        LOG.info("user verifier stopped");
    }
    
    @Override
    public Class<TrustfulAvroConfig> getConfigurationClass() {
        return TrustfulAvroConfig.class;
    }
}
