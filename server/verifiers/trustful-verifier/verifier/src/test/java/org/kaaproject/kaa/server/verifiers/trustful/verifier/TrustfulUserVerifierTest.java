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
