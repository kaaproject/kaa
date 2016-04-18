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

import org.apache.commons.io.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedHashMap;

import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AndroidHttpClient extends AbstractHttpClient {

    /** The Constant LOG. */
    public static final Logger LOG = LoggerFactory // NOSONAR
            .getLogger(AndroidHttpClient.class);

    private DefaultHttpClient httpClient;
    private volatile HttpPost method;

    public AndroidHttpClient(String url, PrivateKey privateKey,
            PublicKey publicKey, PublicKey remotePublicKey) {
        super(url, privateKey, publicKey, remotePublicKey);
        this.httpClient = new DefaultHttpClient();
        this.method = null;
    }

    @Override
    public byte[] executeHttpRequest(String uri, LinkedHashMap<String, byte[]> entity
            , boolean verifyResponse) throws Exception { //NOSONAR

        byte[] responseDataRaw = null;
        method = new HttpPost(url + uri);
        MultipartEntity requestEntity = new MultipartEntity();
        for (String key : entity.keySet()) {
            requestEntity.addPart(key, new ByteArrayBody(entity.get(key), null));
        }
        method.setEntity(requestEntity);
        if (!Thread.currentThread().isInterrupted()) {
            LOG.debug("Executing request {}", method.getRequestLine());
            HttpResponse response = httpClient.execute(method);
            try {
                LOG.debug("Received {}", response.getStatusLine());
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    responseDataRaw = getResponseBody(response, verifyResponse);
                } else {
                    throw new TransportException(status);
                }
            } finally {
                method = null;
            }
        }
        else {
            method = null;
            throw new InterruptedException();
        }
        return responseDataRaw;
    }

    private byte[] getResponseBody(HttpResponse response, boolean verifyResponse) throws IOException,
            GeneralSecurityException {

        HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
            byte[] body = toByteArray(resEntity);
            consume(resEntity);

            if (verifyResponse) {
                Header signatureHeader = response
                        .getFirstHeader(CommonEPConstans.SIGNATURE_HEADER_NAME);

                if (signatureHeader == null) {
                    throw new IOException("can't verify message");
                }

                byte[] signature;
                if (signatureHeader.getValue() != null) {
                    signature = android.util.Base64.decode(signatureHeader.getValue()
                            .getBytes(Charsets.UTF_8), android.util.Base64.DEFAULT);
                } else {
                    signature = new byte[0];
                }

                // LOG.debug("Remote Public Key: {}" +
                // messageEncDec.getRemotePublicKey().getEncoded().length);
                // LOG.debug(MessageEncoderDecoder.bytesToHex(messageEncDec.getRemotePublicKey().getEncoded()));
                // LOG.debug("Signature size: {}" + signature.length);
                // LOG.debug(MessageEncoderDecoder.bytesToHex(signature));
                // LOG.debug("Body size: {}" + body.length);
                // LOG.debug(MessageEncoderDecoder.bytesToHex(body));

                return verifyResponse(body, signature);
            } else {
                return body;
            }
        } else {
            throw new IOException("can't read message");
        }
    }

    private static byte[] toByteArray(final HttpEntity entity) throws IOException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        InputStream instream = entity.getContent();
        if (instream == null) {
            return new byte[] {};
        }
        if (entity.getContentLength() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
        }
        int i = (int)entity.getContentLength();
        if (i < 0) {
            i = 4096;
        }
        ByteArrayBuffer buffer = new ByteArrayBuffer(i);
        try {
            byte[] tmp = new byte[4096];
            int l;
            while((l = instream.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
        } finally {
            instream.close();
        }
        return buffer.toByteArray();
    }

    private static void consume(final HttpEntity entity) throws IOException {
        if (entity == null) {
            return;
        }
        if (entity.isStreaming()) {
            final InputStream instream = entity.getContent();
            if (instream != null) {
                instream.close();
            }
        }
    }

    @Override
    public void close() throws IOException {
        abort();
        method = null;
        httpClient = null;
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
