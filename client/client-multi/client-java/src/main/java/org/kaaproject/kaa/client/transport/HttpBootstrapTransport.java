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
import java.security.PublicKey;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.bootstrap.CommonBSConstants;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.Resolve;

/**
 * HTTP {@link BootstrapTransport} implementation.
 *
 * @author Yaroslav Zeygerman
 *
 */
public class HttpBootstrapTransport extends AbstractHttpTransport implements BootstrapTransport {
    private AvroByteArrayConverter<Resolve> resolveRequestConverter;
    private AvroByteArrayConverter<OperationsServerList> resolveResponseConverter;

    public HttpBootstrapTransport(String url, PublicKey remotePublicKey) {
        super(url, null, null, remotePublicKey);
        resolveRequestConverter = new AvroByteArrayConverter<Resolve>(Resolve.class);
        resolveResponseConverter = new AvroByteArrayConverter<OperationsServerList>(OperationsServerList.class);
    }

    @Override
    public OperationsServerList sendResolveRequest(Resolve request)
            throws TransportException {
        return sendRequest(CommonBSConstants.BOOTSTRAP_RESOLVE_URI, request,
                resolveRequestConverter, resolveResponseConverter);
    }

    protected <T extends SpecificRecordBase> T retrieveResponseEntity(HttpResponse response,
            AvroByteArrayConverter<T> converter) throws IOException, GeneralSecurityException {
        return converter.fromByteArray(getResponseBody(response));
    }

    @Override
    protected <T extends SpecificRecordBase> HttpEntity createRequestEntity(T request,
            AvroByteArrayConverter<T> converter) throws IOException, GeneralSecurityException {
        byte[] requestBodyRaw = converter.toByteArray(request);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(CommonBSConstants.APPLICATION_TOKEN_ATTR_NAME, requestBodyRaw);
        HttpEntity requestEntity = builder.build();

        return requestEntity;
    }

}
