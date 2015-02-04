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


package org.kaaproject.kaa.server.verifiers.gplus.verifier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.verifiers.gplus.config.gen.GplusAvroConfig;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

public class GplusUserVerifierTest extends GplusUserVerifier {
    private static GplusUserVerifier verifier;
    private static String userId = "1557997434440423";
    private static GplusAvroConfig config;
    private static HttpURLConnection connection;

    @BeforeClass
    public static void setUp() {
        config = mock(GplusAvroConfig.class);
        when(config.getMaxParallelConnections()).thenReturn(1);
    }

    @Test
    public void invalidUserAccessCodeTest() {
        verifier = new MyGplusVerifier(200, "{\n" +
                " \"error\": \"invalid_token\",\n" +
                " \"error_description\": \"Invalid Value\"\n" +
                "}\n");
        verifier.init(null, config);
        verifier.start();

        UserVerifierCallback callback = mock(UserVerifierCallback.class);

        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(anyString());
    }

    @Test
    public void incompatibleUserIds() {
        verifier = new MyGplusVerifier(200, "{\n" +
                "  \"audience\":\"8819981768.apps.googleusercontent.com\",\n" +
                "  \"user_id\":\""+userId+"\",\n" +
                "  \"scope\":\"profile email\",\n" +
                "  \"expires_in\":436\n" +
                "}");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(anyString());
    }

    @Test
    public void badRequestTest() {
        verifier = new MyGplusVerifier(400);
        verifier.init(null, config);
        verifier.start();

        UserVerifierCallback callback = mock(UserVerifierCallback.class);

        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);

        verify(callback, Mockito.timeout(1000).atLeastOnce()).onTokenInvalid();
    }

    @Test
    public void successfulVerification() {
        String userId = "12456789123456";
        verifier = new MyGplusVerifier(200, "{\n" +
                "  \"audience\":\"8819981768.apps.googleusercontent.com\",\n" +
                "  \"user_id\":\""+userId+"\",\n" +
                "  \"scope\":\"profile email\",\n" +
                "  \"expires_in\":436\n" +
                "}");


        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken(userId, "someToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onSuccess();
    }

    private static class MyGplusVerifier extends GplusUserVerifier {
        int responseCode;
        String inputStreamString = "";

        MyGplusVerifier(int responseCode) {
            this.responseCode = responseCode;
        }

        MyGplusVerifier(int responseCode, String intputStreamString) {
            this.responseCode = responseCode;
            this.inputStreamString = intputStreamString;
        }

        @Override
        protected HttpURLConnection establishConnection(URL url) {
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