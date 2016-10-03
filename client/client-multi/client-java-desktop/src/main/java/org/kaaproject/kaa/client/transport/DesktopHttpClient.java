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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.kaaproject.kaa.common.endpoint.CommonEpConstans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedHashMap;

public class DesktopHttpClient extends AbstractHttpClient {

  /**
   * The Constant LOG.
   */
  public static final Logger LOG = LoggerFactory // NOSONAR
      .getLogger(DesktopHttpClient.class);

  private CloseableHttpClient httpClient;
  private volatile HttpPost method;

  /**
   * All-args constructor.
   */
  public DesktopHttpClient(String url, PrivateKey privateKey,
                           PublicKey publicKey, PublicKey remotePublicKey) {
    super(url, privateKey, publicKey, remotePublicKey);
    this.httpClient = HttpClientBuilder.create().build();
    this.method = null;
  }

  @Override
  public byte[] executeHttpRequest(String uri, LinkedHashMap<String, byte[]> entity,
                                   boolean verifyResponse) throws Exception { //NOSONAR
    byte[] responseDataRaw = null;
    method = new HttpPost(url + uri);
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    for (String key : entity.keySet()) {
      builder.addBinaryBody(key, entity.get(key));
    }
    HttpEntity requestEntity = builder.build();
    method.setEntity(requestEntity);
    if (!Thread.currentThread().isInterrupted()) {
      LOG.debug("Executing request {}", method.getRequestLine());
      CloseableHttpResponse response = httpClient.execute(method);
      try {
        LOG.debug("Received {}", response.getStatusLine());
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
          responseDataRaw = getResponseBody(response, verifyResponse);
        } else {
          throw new TransportException(status);
        }
      } finally {
        response.close();
        method = null;
      }
    } else {
      method = null;
      throw new InterruptedException();
    }

    return responseDataRaw;
  }

  private byte[] getResponseBody(HttpResponse response, boolean verifyResponse) throws IOException,
      GeneralSecurityException {

    HttpEntity resEntity = response.getEntity();
    if (resEntity != null) {
      byte[] body = EntityUtils.toByteArray(resEntity);
      EntityUtils.consume(resEntity);

      if (verifyResponse) {
        Header signatureHeader = response
            .getFirstHeader(CommonEpConstans.SIGNATURE_HEADER_NAME);

        if (signatureHeader == null) {
          throw new IOException("can't verify message");
        }

        byte[] signature;
        if (signatureHeader.getValue() != null) {
          signature = Base64.decodeBase64(signatureHeader.getValue()
              .getBytes(Charsets.UTF_8));
        } else {
          signature = new byte[0];
        }
        return verifyResponse(body, signature);
      } else {
        return body;
      }
    } else {
      throw new IOException("can't read message");
    }
  }

  @Override
  public void close() throws IOException {
    this.httpClient.close();
  }

  @Override
  public void abort() {
    if (method != null && !method.isAborted()) {
      LOG.info("Forcely aborting current request...");
      method.abort();
    }
  }

  @Override
  public boolean canAbort() {
    return method != null && !method.isAborted();
  }

}
