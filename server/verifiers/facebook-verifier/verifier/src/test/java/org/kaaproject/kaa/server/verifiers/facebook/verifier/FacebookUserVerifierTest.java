package org.kaaproject.kaa.server.verifiers.facebook.verifier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.verifiers.facebook.config.gen.FacebookAvroConfig;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

public class FacebookUserVerifierTest extends FacebookUserVerifier {
    private static FacebookUserVerifier verifier;
    private static String userId = "1557997434440423";
    private static FacebookAvroConfig config;
    private static HttpURLConnection connection;

    @BeforeClass
    public static void setUp() {
        config = mock(FacebookAvroConfig.class);
        when(config.getMaxParallelConnections()).thenReturn(1);
        when(config.getAppId()).thenReturn("xxx");
        when(config.getAppSecret()).thenReturn("xxx");
    }

    @Test
    public void invalidUserAccessCodeTest() {
        verifier = new MyFacebookVerifier(200, "{\"data\":{\"error\":{\"code\":190,\"message\":\"" +
                                                "The access token could not be decrypte" +
                                                "d\"},\"is_valid\":false,\"scopes\":[]}}");
        verifier.init(null, config);
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(anyString());
    }

    @Test
    public void incompatibleUserIds() {
        verifier = new MyFacebookVerifier(200, "{\"data\":{\"app_id\":\"1557997434440423\"," +
                                                "\"application\":\"testApp\",\"expires_at\":1422990000," +
                                                "\"is_valid\":true,\"scopes\":[\"public_profile\"],\"user_id\"" +
                                                 ":\"800033850084728\"}}");

        verifier.init(null, config);
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(anyString());
    }

    @Test
    public void badRequestTest() {
        verifier = new MyFacebookVerifier(400);
        verifier.init(null, config);

        UserVerifierCallback callback = mock(UserVerifierCallback.class);

        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);

        // no exception is thrown, if onVerificationFailure(String) was called
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(anyString());
    }

    @Test
    public void successfulVerification() {
        String userId = "12456789123456";
        verifier = new MyFacebookVerifier(200, "{\"data\":{\"app_id\":\"1557997434440423\"," +
                "\"application\":\"testApp\",\"expires_at\":1422990000," +
                "\"is_valid\":true,\"scopes\":[\"public_profile\"],\"user_id\"" +
                ":\"" + userId + "\"}}");

        verifier.init(null, config);
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken(userId, "someToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onSuccess();
    }

    private static class MyFacebookVerifier extends FacebookUserVerifier {
        int responseCode;
        String inputStreamString = "";

        MyFacebookVerifier(int responseCode) {
            this.responseCode = responseCode;
        }

        MyFacebookVerifier(int responseCode, String intputStreamString) {
            this.responseCode = responseCode;
            this.inputStreamString = intputStreamString;
        }

        @Override
        protected HttpURLConnection establishConnection(String userAccessToken, String accessToken) {
            connection = mock(HttpURLConnection.class);

            try {
                when(connection.getResponseCode()).thenReturn(responseCode);
                when(connection.getInputStream()).thenReturn(
                        new ByteArrayInputStream(inputStreamString.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return connection;
        }
    }
}
