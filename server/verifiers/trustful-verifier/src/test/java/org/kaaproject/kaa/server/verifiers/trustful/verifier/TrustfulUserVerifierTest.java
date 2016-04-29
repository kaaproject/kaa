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

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.verifiers.trustful.config.gen.TrustfulAvroConfig;
import org.mockito.Mockito;

public class TrustfulUserVerifierTest {
    private static TrustfulUserVerifier verifier;

    @Test
    public void getConfigurationClassTest() {
        verifier = new TrustfulUserVerifier();
        Assert.assertEquals(verifier.getConfigurationClass(), TrustfulAvroConfig.class);
    }

    @Test
    public void successfulVerificationTest() {
        verifier = new TrustfulUserVerifier();
        verifier.start();
        verifier.init(null, new TrustfulAvroConfig());
        UserVerifierCallback callback = Mockito.mock(UserVerifierCallback.class);
        verifier.checkAccessToken("dummyId", "dummyToken", callback);
        Mockito.verify(callback, Mockito.timeout(1000)).onSuccess();
        verifier.stop();
    }
}
