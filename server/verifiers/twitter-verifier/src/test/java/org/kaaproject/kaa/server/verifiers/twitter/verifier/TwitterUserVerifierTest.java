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

package org.kaaproject.kaa.server.verifiers.twitter.verifier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.verifiers.twitter.config.gen.TwitterAvroConfig;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

public class TwitterUserVerifierTest extends TwitterUserVerifier {
    private static TwitterUserVerifier verifier;
    private static TwitterAvroConfig config;
    private static final String INVALID_TOKEN_CODE = "89";

    @BeforeClass
    public static void setUp() {
        config = mock(TwitterAvroConfig.class);
        when(config.getMaxParallelConnections()).thenReturn(5);
    }

    @Test
    public void successfulVerificationTest() {
        String userId = "12456789123456";
        verifier = new MyTwitterVerifier(200, "{" +
                "    \"contributors_enabled\": true," +
                "    \"geo_enabled\": true," +
                "    \"id\": 38895958," +
                "    \"id_str\": \"" + userId + "\"," +
                "    \"is_translator\": false," +
                "    \"lang\": \"en\"," +
                "    \"name\": \"Sean Cook\"}");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken(userId, "someToken someSecret", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onSuccess();
        verifier.stop();
    }

    @Test
    public void incompatibleUserIdsTest() {
        String invalidUserId = "12456789123456";
        verifier = new MyTwitterVerifier(200, "{" +
                "    \"contributors_enabled\": true," +
                "    \"geo_enabled\": true," +
                "    \"id\": 38895958," +
                "    \"id_str\": \"123456\"," +
                "    \"is_translator\": false," +
                "    \"lang\": \"en\"," +
                "    \"name\": \"Sean Cook\"}");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken(invalidUserId, "someToken someSecret", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(anyString());
        verifier.stop();
    }

    @Test
    public void invalidUserAccessTokenTest() {
        verifier = new MyTwitterVerifier(400, "{\"errors\":[{\"message\":\"Sorry," +
                                            " that page does not exist\",\"code\": " + INVALID_TOKEN_CODE + "}]}");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "someToken someSecret", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onTokenInvalid();
    }

    @Test
    public void otherResponseCodeTest() {
        verifier = new MyTwitterVerifier(406, "{}");
        verifier.init(null, config);
        verifier.start();

        UserVerifierCallback callback = mock(UserVerifierCallback.class);

        verifier.checkAccessToken("invalidUserId", "someToken someSecret", callback);

        // no exception is thrown, if onVerificationFailure(String) was called
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(anyString());
        verifier.stop();
    }

    @Test
    public void badResponseWithOtherErrorCodeTest() {
        String otherErrorCode = "215";
        verifier = new MyTwitterVerifier(400, "{\"errors\":[{\"message\":\"Sorry," +
                " that page does not exist\",\"code\": " + otherErrorCode + "}]}");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "someToken someSecret", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(any(String.class));
    }

    @Test
    public void connectionErrorTest() throws IOException {
        verifier = new TwitterUserVerifier();
        verifier.init(null, config);
        verifier.start();
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        doThrow(new IOException()).when(httpClientMock).execute(any(HttpHost.class), any(HttpRequest.class));
        ReflectionTestUtils.setField(verifier, "httpClient", httpClientMock);
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("id", "token secret", callback);
        Mockito.verify(callback, Mockito.timeout(1000)).onConnectionError(any(String.class));
    }

    @Test
    public void internalErrorTest() throws IOException {
        verifier = new TwitterUserVerifier();
        verifier.init(null, config);
        verifier.start();
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        // Throw any descendant of Exception, as the indicator of an internal error
        doThrow(new NullPointerException()).when(httpClientMock).execute(any(HttpGet.class));
        ReflectionTestUtils.setField(verifier, "httpClient", httpClientMock);
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("id", "token secret", callback);
        Mockito.verify(callback, Mockito.timeout(1000)).onInternalError(any(String.class));
    }

    private static class MyTwitterVerifier extends TwitterUserVerifier {
        int responseCode;
        String inputStreamString = "";

        MyTwitterVerifier(int responseCode, String intputStreamString) {
            this.responseCode = responseCode;
            this.inputStreamString = intputStreamString;
        }

        @Override
        protected CloseableHttpResponse establishConnection(String accessToken) {
            CloseableHttpResponse connection = mock(CloseableHttpResponse.class);

            try {
                StatusLine statusLine = mock(StatusLine.class);
                when(statusLine.getStatusCode()).thenReturn(responseCode);

                HttpEntity httpEntity = mock(HttpEntity.class);
                when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(inputStreamString.
                                                            getBytes(StandardCharsets.UTF_8)));

                when(connection.getStatusLine()).thenReturn(statusLine);
                when(connection.getEntity()).thenReturn(httpEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return connection;
        }
    }
}
