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

package org.kaaproject.kaa.client.transport;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DesktopHttpClientTest {
    private static final String URL = "http://some-url";
    private static final String HTTP_CLIENT_FIELD_NAME = "httpClient";
    private static final String HTTP_METHOD_FIELD_NAME = "method";
    private static final int OK = 200;
    private static final int FAILURE = 400;
    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    private static PublicKey remotePublicKey;
    private static LinkedHashMap<String, byte[]> entities = new LinkedHashMap<>();
    private CloseableHttpResponse httpResponse;

    @BeforeClass
    public static void setUp() {
        entities.put("entity1", new byte[]{1, 2, 3});
        entities.put("entity2", new byte[]{53, 1});
        privateKey = mock(PrivateKey.class);
        publicKey = mock(PublicKey.class);
        remotePublicKey = mock(PublicKey.class);
        when(privateKey.getEncoded()).thenReturn(new byte[]{1, 2, 3, 4, 5});
        when(publicKey.getEncoded()).thenReturn(new byte[]{1, 2, 3, 4, 5, 10});
        when(remotePublicKey.getEncoded()).thenReturn(new byte[]{5, 3, 2, 5, 6 , 3, 127});
    }

    @Test(expected = TransportException.class)
    public void executeInvalidHttpRequestTest() throws Exception {
        DesktopHttpClient client = new DesktopHttpClient(URL, privateKey, publicKey, remotePublicKey);
        CloseableHttpClient httpClientMock = mockForHttpClient(FAILURE, true, null);
        ReflectionTestUtils.setField(client, HTTP_CLIENT_FIELD_NAME, httpClientMock);
        client.executeHttpRequest(URL, entities, false);
        verify(httpResponse).close();
    }

    @Test
    public void executeValidHttpRequest() throws Exception {
        byte[] inputData = new byte[]{100, 101, 102};
        DesktopHttpClient client = new DesktopHttpClient(URL, privateKey, publicKey, remotePublicKey);
        CloseableHttpClient httpClientMock = mockForHttpClient(OK, true, inputData);
        ReflectionTestUtils.setField(client, HTTP_CLIENT_FIELD_NAME, httpClientMock);
        byte[] body = client.executeHttpRequest(URL, entities, false);
        Assert.assertArrayEquals(inputData, body);
        verify(httpResponse).close();
    }

    @Test
    public void canAbortTest() throws Throwable {
        DesktopHttpClient client = new DesktopHttpClient(URL, privateKey, publicKey, remotePublicKey);
        ReflectionTestUtils.setField(client, HTTP_METHOD_FIELD_NAME, null);
        Assert.assertFalse(client.canAbort());
        HttpPost method = new HttpPost();
        method.abort();
        ReflectionTestUtils.setField(client, HTTP_METHOD_FIELD_NAME, method);
        Assert.assertFalse(client.canAbort());
        method = new HttpPost();
        ReflectionTestUtils.setField(client, HTTP_METHOD_FIELD_NAME, method);
        Assert.assertTrue(client.canAbort());
    }

    @Test(expected = IOException.class)
    public void executeValidHttpRequestWithNoResponseEntityTest() throws Exception {
        DesktopHttpClient client = new DesktopHttpClient(URL, privateKey, publicKey, remotePublicKey);
        CloseableHttpClient httpClientMock = mockForHttpClient(OK, false, null);
        ReflectionTestUtils.setField(client, HTTP_CLIENT_FIELD_NAME, httpClientMock);
        client.executeHttpRequest(URL, entities, false);
        verify(httpResponse).close();
    }

    @Test
    public void closeTest() throws IOException {
        DesktopHttpClient client = new DesktopHttpClient(URL, privateKey, publicKey, remotePublicKey);
        CloseableHttpClient httpClientMock = mockForHttpClient(OK, false, null);
        ReflectionTestUtils.setField(client, HTTP_CLIENT_FIELD_NAME, httpClientMock);
        client.close();
        verify(httpClientMock).close();
    }

    @Test
    public void abortTest() throws IOException {
        DesktopHttpClient client = new DesktopHttpClient(URL, privateKey, publicKey, remotePublicKey);
        HttpPost method = mock(HttpPost.class);
        when(method.isAborted()).thenReturn(false);
        ReflectionTestUtils.setField(client, HTTP_METHOD_FIELD_NAME, method);
        client.abort();
        verify(method).abort();
    }

    private CloseableHttpClient mockForHttpClient(int status, boolean hasEntity, byte[] body) throws IOException {
        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);
        httpResponse = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
        doReturn(httpResponse).when(httpClientMock).execute(any(HttpPost.class));
        if (hasEntity) {
            HttpEntity entity = mock(HttpEntity.class);
            doReturn(entity).when(httpResponse).getEntity();
            if (body != null) {
                doReturn(new ByteArrayInputStream(body)).when(entity).getContent();
            }
        } else {
            doReturn(null).when(httpResponse).getEntity();
        }
        when(httpResponse.getStatusLine().getStatusCode()).thenReturn(status);
        return httpClientMock;
    }
}
