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

package org.kaaproject.kaa.client.transport;

import java.io.Closeable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partially implemented Http transport.
 *
 * @author Yaroslav Zeygerman
 *
 */
public abstract class AbstractHttpTransport implements Closeable {

    /** The Constant logger. */
    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(AbstractHttpTransport.class);

    private String host;
    private CloseableHttpClient httpClient;
    private MessageEncoderDecoder messageEncDec;
    private boolean verificationEnabled = true;
    private volatile HttpPost method;
    private volatile boolean isForcedAbortion = false;

    public AbstractHttpTransport(String url, PrivateKey privateKey,
            PublicKey publicKey, PublicKey remotePublicKey) {
        this.messageEncDec = new MessageEncoderDecoder(privateKey, publicKey, remotePublicKey);
        this.host = url;
        this.httpClient = HttpClientBuilder.create().build();
        this.method = null;
    }

    public void disableVerification(){
        this.verificationEnabled = false;
    }

    /**
     * Sends request to the server.
     *
     * @param uri request's uri.
     * @param param request's body.
     * @param requestConverter converter for the request object.
     * @param responseConverter converter for the response object.
     *
     */
    public <T extends SpecificRecordBase,O extends SpecificRecordBase> O sendRequest(String uri, T param,
            AvroByteArrayConverter<T> requestConverter, AvroByteArrayConverter<O> responseConverter)
                    throws TransportException {
        O responseData = null;
        isForcedAbortion = false;

        try {
            method = new HttpPost(host + uri);
            HttpEntity requestEntity = createRequestEntity(param, requestConverter);

            method.setEntity(requestEntity);
            LOG.debug("Executing request {}", method.getRequestLine());
            CloseableHttpResponse response = httpClient.execute(method);
            try {
                LOG.debug("Received {}", response.getStatusLine());
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    responseData = retrieveResponseEntity(response, responseConverter);
                }
                if(responseData == null){
                    throw new TransportException(
                            "Invalid response code from server: " + status);
                }
            } finally {
                response.close();
                method = null;
            }
        } catch (Exception e) {
            if (!isForcedAbortion) {
                LOG.error("Request Failed", e);
                if (e instanceof TransportException) {
                    throw (TransportException) e;
                } else {
                    throw new TransportException(e);
                }
            }
        }
        return responseData;
    }

    /**
     *
     * @return MessageEncoderDecoder of this transport.
     *
     */
    protected final MessageEncoderDecoder getEncoderDecoder() {
        return messageEncDec;
    }

    /**
     * Retrieves the response's body from the given HttpResponse.
     *
     * @param response response from the server.
     * @return byte array with response's body.
     *
     */
    protected byte [] getResponseBody(HttpResponse response) throws IOException, GeneralSecurityException {
        Header signatureHeader = response.getFirstHeader(CommonEPConstans.SIGNATURE_HEADER_NAME);

        HttpEntity resEntity = response.getEntity();
        if (resEntity != null && signatureHeader != null) {

            byte[] body = EntityUtils.toByteArray(resEntity);
            byte[] signature = Base64.decodeBase64(signatureHeader.getValue());

            EntityUtils.consume(resEntity);

//            LOG.debug("Remote Public Key: {}" + messageEncDec.getRemotePublicKey().getEncoded().length);
//            LOG.debug(MessageEncoderDecoder.bytesToHex(messageEncDec.getRemotePublicKey().getEncoded()));
//            LOG.debug("Signature size: {}" + signature.length);
//            LOG.debug(MessageEncoderDecoder.bytesToHex(signature));
//            LOG.debug("Body size: {}" + body.length);
//            LOG.debug(MessageEncoderDecoder.bytesToHex(body));

            if (!verificationEnabled || messageEncDec.verify(body, signature)) {
                return body;
            } else {
                throw new SecurityException("message can't be verified");
            }
        } else {
            throw new IOException("can't read message");
        }
    }

    /**
     * Retrieves avro object from the HttpResponse. Should be implemented by the user.
     *
     * @param response response from the server.
     * @return avro object.
     *
     */
    protected abstract <T extends SpecificRecordBase> T retrieveResponseEntity(HttpResponse response,
            AvroByteArrayConverter<T> converter) throws IOException, GeneralSecurityException;

    /**
     * Creates request's HttpEntity from the given avro request object. Should be implemented by the user.
     *
     * @param request avro request
     * @return request's entity
     *
     */
    protected abstract <T extends SpecificRecordBase> HttpEntity createRequestEntity(T request,
            AvroByteArrayConverter<T> converter) throws IOException, GeneralSecurityException;

    @Override
    public void close() throws IOException {
        this.httpClient.close();
    }

    public void abort() {
        if (method != null && !method.isAborted()) {
            LOG.info("Forcely aborting current request...");
            isForcedAbortion = true;
            method.abort();
        }
    }

}
