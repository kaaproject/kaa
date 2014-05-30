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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.LongSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;

/**
 * HTTP {@link OperationsTransport} implementation.
 *
 * @author Yaroslav Zeygerman
 *
 */
public class HttpOperationsTransport extends AbstractHttpTransport implements OperationsTransport {
    private AvroByteArrayConverter<EndpointRegistrationRequest> endpointRegistrationRequestConverter;
    private AvroByteArrayConverter<ProfileUpdateRequest> profileUpdateRequestConverter;
    private AvroByteArrayConverter<SyncRequest> syncRequestConverter;
    private AvroByteArrayConverter<LongSyncRequest> longSyncRequestConverter;
    private AvroByteArrayConverter<SyncResponse> syncResponseConverter;


    public HttpOperationsTransport(String url, PrivateKey privateKey,
            PublicKey publicKey, PublicKey remotePublicKey) {
        super("http://" + url, privateKey, publicKey, remotePublicKey);
        this.endpointRegistrationRequestConverter = new AvroByteArrayConverter<>(
                EndpointRegistrationRequest.class);
        this.profileUpdateRequestConverter = new AvroByteArrayConverter<>(
                ProfileUpdateRequest.class);
        this.syncRequestConverter = new AvroByteArrayConverter<>(
                SyncRequest.class);
        this.longSyncRequestConverter = new AvroByteArrayConverter<>(
                LongSyncRequest.class);
        this.syncResponseConverter = new AvroByteArrayConverter<>(
                SyncResponse.class);
    }

    @Override
    public SyncResponse sendRegisterCommand(EndpointRegistrationRequest request)
            throws TransportException {
        return sendRequest(CommonEPConstans.ENDPOINT_REGISTER_URI, request,
                endpointRegistrationRequestConverter, syncResponseConverter);
    }

    @Override
    public SyncResponse sendUpdateCommand(ProfileUpdateRequest request)
            throws TransportException {
        return sendRequest(CommonEPConstans.ENDPOINT_UPDATE_URI, request,
                profileUpdateRequestConverter, syncResponseConverter);
    }

    @Override
    public SyncResponse sendSyncRequest(SyncRequest request)
            throws TransportException {
        return sendRequest(CommonEPConstans.SYNC_URI,
                request, syncRequestConverter, syncResponseConverter);
    }

    @Override
    public SyncResponse sendLongSyncRequest(LongSyncRequest request)
            throws TransportException {
        return sendRequest(CommonEPConstans.LONG_SYNC_URI,
                request, longSyncRequestConverter, syncResponseConverter);
    }

    protected <T extends SpecificRecordBase> T retrieveResponseEntity(HttpResponse response,
            AvroByteArrayConverter<T> converter) throws IOException, GeneralSecurityException {
        byte[] decodedBody = getEncoderDecoder().decodeData(getResponseBody(response));
        return converter.fromByteArray(decodedBody);
    }

    @Override
    protected <T extends SpecificRecordBase> HttpEntity createRequestEntity(T request,
            AvroByteArrayConverter<T> converter) throws IOException, GeneralSecurityException {
        MessageEncoderDecoder messageEncDec = getEncoderDecoder();

        byte[] requestBodyRaw = converter.toByteArray(request);
        byte[] requestKeyEncoded = messageEncDec.getEncodedSessionKey();
        byte[] requestBodyEncoded = messageEncDec.encodeData(requestBodyRaw);
        byte[] signature = messageEncDec.sign(requestBodyEncoded);

        if(LOG.isTraceEnabled()){
            LOG.trace("Signature size: {}", signature.length);
            LOG.trace(MessageEncoderDecoder.bytesToHex(signature));
            LOG.trace("RequestKeyEncoded size: {}", requestKeyEncoded.length);
            LOG.trace(MessageEncoderDecoder.bytesToHex(requestKeyEncoded));
            LOG.trace("RequestBodyEncoded size: {}", requestBodyEncoded.length);
            LOG.trace(MessageEncoderDecoder.bytesToHex(requestBodyEncoded));
        }
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(CommonEPConstans.REQUEST_SIGNATURE_ATTR_NAME, signature);
        builder.addBinaryBody(CommonEPConstans.REQUEST_KEY_ATTR_NAME, requestKeyEncoded);
        builder.addBinaryBody(CommonEPConstans.REQUEST_DATA_ATTR_NAME, requestBodyEncoded);
        HttpEntity requestEntity = builder.build();

        return requestEntity;
    }

    @Override
    public void abortRequest() {
        abort();
    }
}
