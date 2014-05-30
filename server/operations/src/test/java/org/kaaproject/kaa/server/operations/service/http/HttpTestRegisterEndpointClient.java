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

package org.kaaproject.kaa.server.operations.service.http;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.security.PublicKey;

import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointVersionInfo;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class HttpTestRegisterEndpointClient extends HttpTestClient<EndpointRegistrationRequest, SyncResponse> {

    /** Defined application token */
    public static final String APPLICATION_TOKEN = "123test";
    
    /** MAX profile body size */
    private static final int MAX_PROFILE_BODY_SIZE = 4096;
    
    /**
     * Constructor
     * @param serverPublicKey - server public key
     * @param activity - interface to notify request finish.
     * @throws MalformedURLException - if URI is incorrect
     * @throws Exception - if request creation failed
     */
    public HttpTestRegisterEndpointClient(PublicKey serverPublicKey,
            String commandName, HttpActivity<SyncResponse> activity)
            throws MalformedURLException, Exception {
        super(serverPublicKey, commandName, activity);
        registerEndpointInit();
        postInit(getRequest());
    }
    
    /**
     * Generate EndpointRegistrationRequest.
     */
    private void registerEndpointInit() {
        setRequest(new EndpointRegistrationRequest());
        
        getRequest().setApplicationToken(APPLICATION_TOKEN);
        
        getRequest().setEndpointPublicKey(ByteBuffer.wrap(getClientPublicKey().getEncoded()));
        
        int profileBodySize = rnd.nextInt(MAX_PROFILE_BODY_SIZE);
        getRequest().setProfileBody(ByteBuffer.wrap(getRandomBytes(profileBodySize)));
        
        getRequest().setVersionInfo(new EndpointVersionInfo(getId(), 
                Integer.valueOf(rnd.nextInt()), 
                Integer.valueOf(rnd.nextInt()),
                Integer.valueOf(rnd.nextInt())));
    }

    @Override
    protected Class<EndpointRegistrationRequest> getRequestConverterClass() {
        return EndpointRegistrationRequest.class;
    }

    @Override
    protected Class<SyncResponse> getResponseConverterClass() {
        return SyncResponse.class;
    }

}
