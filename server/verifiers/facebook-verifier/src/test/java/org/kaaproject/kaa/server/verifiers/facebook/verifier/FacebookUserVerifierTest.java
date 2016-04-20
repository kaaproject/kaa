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

package org.kaaproject.kaa.server.verifiers.facebook.verifier;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.verifiers.facebook.config.gen.FacebookAvroConfig;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
        verifier = new MyFacebookVerifier(400, " {" +
                "       \"error\": {" +
                "         \"message\": \"Message describing the error\", " +
                "         \"type\": \"OAuthException\", " +
                "         \"code\": 190," +
                "         \"error_subcode\": 467," +
                "         \"error_user_title\": \"A title\"," +
                "         \"error_user_msg\": \"A message\"" +
                "       }" +
                "     }");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onTokenInvalid();
    }

    @Test
    public void expiredUserAccessTokenTest() {
        verifier = new MyFacebookVerifier(400, " {" +
                "       \"error\": {" +
                "         \"message\": \"Message describing the error\", " +
                "         \"type\": \"OAuthException\", " +
                "         \"code\": 190," +
                "         \"error_subcode\": 463," +
                "         \"error_user_title\": \"A title\"," +
                "         \"error_user_msg\": \"A message\"" +
                "       }" +
                "     }");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onTokenExpired();
    }

    @Test
    public void incompatibleUserIdsTest() {
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
        verifier = new MyFacebookVerifier(400, "{}");
        verifier.init(null, config);
        verifier.start();

        UserVerifierCallback callback = mock(UserVerifierCallback.class);

        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);

        // no exception is thrown, if onVerificationFailure(String) was called
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(anyString());
        verifier.stop();
    }

    @Test
    public void successfulVerificationTest() {
        String userId = "12456789123456";
        verifier = new MyFacebookVerifier(200, "{\"data\":{\"app_id\":\"1557997434440423\"," +
                "\"application\":\"testApp\",\"expires_at\":1422990000," +
                "\"is_valid\":true,\"scopes\":[\"public_profile\"],\"user_id\"" +
                ":" + userId + "}}");

        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken(userId, "someToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onSuccess();
        verifier.stop();
    }

    @Test
    public void connectionErrorTest() throws IOException {
        verifier = new FacebookUserVerifier();
        verifier.init(null, config);
        verifier.start();
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        doThrow(new IOException()).when(httpClientMock).execute(any(HttpGet.class));
        ReflectionTestUtils.setField(verifier, "httpClient", httpClientMock);
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("id", "token", callback);
        Mockito.verify(callback, Mockito.timeout(1000)).onConnectionError(any(String.class));
        verifier.stop();
    }

    @Test
    public void internalErrorTest() throws IOException {
        verifier = new FacebookUserVerifier();
        verifier.init(null, config);
        verifier.start();
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        // Throw any descendant of Exception, as the indicator of an internal error
        doThrow(new NullPointerException()).when(httpClientMock).execute(any(HttpGet.class));
        ReflectionTestUtils.setField(verifier, "httpClient", httpClientMock);
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("id", "token", callback);
        Mockito.verify(callback, Mockito.timeout(1000)).onInternalError(any(String.class));
        verifier.stop();
    }

    @Test
    public void unrecognizedResponseCodeTest() throws IOException {
        verifier = new MyFacebookVerifier(300, "");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("id", "token", callback);
        Mockito.verify(callback, Mockito.timeout(1000)).onVerificationFailure(any(String.class));
        verifier.stop();
    }

    @Test
    public void oauthErrorNoSubcodeTest() {
        verifier = new MyFacebookVerifier(400, " {" +
                "       \"error\": {" +
                "         \"message\": \"Message describing the error\", " +
                "         \"type\": \"OAuthException\", " +
                "         \"code\": 190," +
                "         \"error_subcode\": null," +
                "         \"error_user_title\": \"A title\"," +
                "         \"error_user_msg\": \"A message\"" +
                "       }" +
                "     }");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(any(String.class));
    }

    @Test
    public void unrecognizedOauthErrorSubcodeTest() {
        verifier = new MyFacebookVerifier(400, " {" +
                "       \"error\": {" +
                "         \"message\": \"Message describing the error\", " +
                "         \"type\": \"OAuthException\", " +
                "         \"code\": 190," +
                "         \"error_subcode\": 111," +
                "         \"error_user_title\": \"A title\"," +
                "         \"error_user_msg\": \"A message\"" +
                "       }" +
                "     }");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(any(String.class));
    }

    @Test
    public void unrecognizedResponseErrorCodeTest() {
        verifier = new MyFacebookVerifier(400, " {" +
                "       \"error\": {" +
                "         \"message\": \"Message describing the error\", " +
                "         \"type\": \"OAuthException\", " +
                "         \"code\": 111," +
                "         \"error_subcode\": 111," +
                "         \"error_user_title\": \"A title\"," +
                "         \"error_user_msg\": \"A message\"" +
                "       }" +
                "     }");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onVerificationFailure(any(String.class));
    }

    @Test
    public void unableToCloseHttpClientTest() throws Exception {
        verifier = new FacebookUserVerifier();
        verifier.init(null, config);
        verifier.start();
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        // Throw any descendant of Exception, as the indicator of an internal error
        doThrow(new IOException()).when(httpClientMock).close();
        ReflectionTestUtils.setField(verifier, "httpClient", httpClientMock);
        Logger LOG = mock(Logger.class);
        Field logField = FacebookUserVerifier.class.getDeclaredField("LOG");

        // set final static field
        setFinalStatic(logField, LOG);
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("id", "token", callback);
        verifier.stop();
        Mockito.verify(callback, Mockito.timeout(1000)).onInternalError(any(String.class));
    }

    @Test
    public void getConfigurationClassTest() {
        verifier = new FacebookUserVerifier();
        Assert.assertEquals(verifier.getConfigurationClass(), FacebookAvroConfig.class);
    }

    @Test
    public void errorInDataResponseTest() {
        String userId = "12456789123456";
        verifier = new MyFacebookVerifier(200, "{" +
                "\"data\":{" +
                "\"app_id\":\"1557997434440423\"," +
                "\"application\":\"testApp\"," +
                "\"expires_at\":1422990000," +
                "\"is_valid\":true," +
                "\"scopes\":[" +
                "\"public_profile\"" +
                "]," +
                "\"user_id\":134," +
                "\"error\":{" +
                "\"message\":\"Message describing the error\"," +
                "\"code\":111}}}");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken(userId, "someToken", callback);
        verify(callback, Mockito.timeout(1000)).onTokenInvalid();
        verifier.stop();
    }

    private static class MyFacebookVerifier extends FacebookUserVerifier {
        int responseCode;
        String inputStreamString = "";

        MyFacebookVerifier(int responseCode, String intputStreamString) {
            this.responseCode = responseCode;
            this.inputStreamString = intputStreamString;
        }

        @Override
        protected CloseableHttpResponse establishConnection(String userAccessToken, String accessToken) {
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

    private void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
