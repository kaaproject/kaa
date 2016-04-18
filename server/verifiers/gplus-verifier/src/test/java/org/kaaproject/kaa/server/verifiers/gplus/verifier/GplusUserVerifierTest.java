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


package org.kaaproject.kaa.server.verifiers.gplus.verifier;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.verifiers.gplus.config.gen.GplusAvroConfig;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

public class GplusUserVerifierTest extends GplusUserVerifier {
    private static GplusUserVerifier verifier;
    private static String userId = "1557997434440423";
    private static GplusAvroConfig config;

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
    public void incompatibleUserIdsTest() {
        verifier = new MyGplusVerifier(200, "{\n" +
                "  \"audience\":\"8819981768.apps.googleusercontent.com\",\n" +
                "  \"user_id\":\"" + userId + "\",\n" +
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
    public void successfulVerificationTest() {
        String userId = "12456789123456";
        verifier = new MyGplusVerifier(200, "{\n" +
                "  \"audience\":\"8819981768.apps.googleusercontent.com\",\n" +
                "  \"user_id\":\"" + userId + "\",\n" +
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
        protected CloseableHttpResponse establishConnection(URI uri) {
            CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
            try {
                StatusLine statusLine = mock(StatusLine.class);
                when(statusLine.getStatusCode()).thenReturn(responseCode);
                HttpEntity httpEntity = mock(HttpEntity.class);
                when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(inputStreamString.
                        getBytes(StandardCharsets.UTF_8)));
                when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
                when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return closeableHttpResponse;
        }
    }

    @Test
    public void internalErrorIOExceptionTest() throws IOException {
        verifier = new GplusUserVerifier();
        verifier.init(null, config);
        verifier.start();
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        doThrow(new IOException()).when(httpClientMock).execute(any(HttpGet.class));
        ReflectionTestUtils.setField(verifier, "httpClient", httpClientMock);
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("id", "token", callback);
        verifier.stop();
        Mockito.verify(callback, Mockito.timeout(1000)).onInternalError();
    }

    @Test
    public void internalErrorExceptionTest() throws Exception {
        verifier = new GplusUserVerifier();
        verifier.init(null, config);
        verifier.start();
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        doThrow(new NullPointerException()).when(httpClientMock).execute(any(HttpGet.class));
        ReflectionTestUtils.setField(verifier, "httpClient", httpClientMock);
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("id", "token", callback);
        verifier.stop();
        Mockito.verify(callback, Mockito.timeout(2000)).onInternalError();
    }

    @Test
    public void unableToCloseHttpClientTest() throws Exception {
        verifier = new GplusUserVerifier();
        verifier.init(null, config);
        verifier.start();
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        // Throw any descendant of Exception, as the indicator of an internal error
        doThrow(new IOException()).when(httpClientMock).close();
        ReflectionTestUtils.setField(verifier, "httpClient", httpClientMock);
        Logger LOG = mock(Logger.class);
        Field logField = GplusUserVerifier.class.getDeclaredField("LOG");
        setFinalStatic(logField, LOG);
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("id", "token", callback);
        verifier.stop();
        Mockito.verify(callback, Mockito.timeout(1000)).onInternalError();
    }


    @Test
    public void getConfigurationClassTest() {
        verifier = new GplusUserVerifier();
        Assert.assertEquals(verifier.getConfigurationClass(), GplusAvroConfig.class);
    }



    @Test
    public void internalErrorBadResponseCodeTest() throws IOException {
        verifier = new MyGplusVerifier(503, "{\n" +
                "  \"audience\":\"8819981768.apps.googleusercontent.com\",\n" +
                "  \"user_id\":\"" + userId + "\",\n" +
                "  \"scope\":\"profile email\",\n" +
                "  \"expires_in\":436\n" +
                "}");
        verifier.init(null, config);
        verifier.start();
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("invalidUserId", "falseUserAccessToken", callback);
        verifier.stop();
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onInternalError();
    }



    @Test
    public void checkMalformedUri() throws Exception {
        verifier = new GplusUserVerifier();
        verifier.init(null, config);
        verifier.start();
        Field uriPart = GplusUserVerifier.class.getDeclaredField("GOOGLE_OAUTH");
        setFinalStatic(uriPart, "\\\\\\\\\\");
        UserVerifierCallback callback = mock(UserVerifierCallback.class);
        verifier.checkAccessToken("id", "token", callback);
        verifier.stop();
        verify(callback, Mockito.timeout(1000).atLeastOnce()).onInternalError();
        setFinalStatic(uriPart, "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=");
    }


    private void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

}
