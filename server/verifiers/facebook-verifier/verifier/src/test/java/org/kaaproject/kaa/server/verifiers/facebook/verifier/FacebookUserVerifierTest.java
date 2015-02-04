/*
 * Copyright 2014 CyberVision, Inc.
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
    private static FacebookAvroConfig config;

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
        verifier.start();
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
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(anyString());
        verifier.stop();
    }

    @Test
    public void badRequestTest() {
        verifier = new MyFacebookVerifier(400);
        verifier.init(null, config);
        verifier.start();

        UserVerifierCallback callback = mock(UserVerifierCallback.class);

        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);

        // no exception is thrown, if onVerificationFailure(String) was called
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(anyString());
        verifier.stop();
    }

    @Test
    public void successfulVerification() {
        String userId = "12456789123456";
        verifier = new MyFacebookVerifier(200, "{\"data\":{\"app_id\":\"1557997434440423\"," +
                "\"application\":\"testApp\",\"expires_at\":1422990000," +
                "\"is_valid\":true,\"scopes\":[\"public_profile\"],\"user_id\"" +
                ":\"" + userId + "\"}}");

        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken(userId, "someToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onSuccess();
        verifier.stop();
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
            HttpURLConnection connection = mock(HttpURLConnection.class);

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
