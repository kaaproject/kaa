/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
