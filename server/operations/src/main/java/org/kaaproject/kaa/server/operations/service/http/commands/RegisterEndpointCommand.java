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

/**
 * 
 */
package org.kaaproject.kaa.server.operations.service.http.commands;

import java.security.GeneralSecurityException;
import java.security.PublicKey;

import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;


/**
 * The Class UpdateEndpointCommand.
 */
public class RegisterEndpointCommand extends AbstractOperationsCommand<EndpointRegistrationRequest, SyncResponse>
        implements CommonEPConstans {

    static {
        COMMAND_NAME = ENDPOINT_REGISTER_COMMAND;
        LOG.info("CommandName: " + COMMAND_NAME);
    }

    /**
     * Instantiates a new register endpoint command.
     */
    public RegisterEndpointCommand() {
        super();
        LOG.trace("CommandName: " + COMMAND_NAME + ": Created..");
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand#getRequestConverterClass()
     */
    @Override
    protected Class<EndpointRegistrationRequest> getRequestConverterClass() {
        return EndpointRegistrationRequest.class;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand#getResponseConverterClass()
     */
    @Override
    protected Class<SyncResponse> getResponseConverterClass() {
        return SyncResponse.class;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand#getPublicKey(org.apache.avro.specific.SpecificRecordBase)
     */
    @Override
    protected PublicKey getPublicKey(EndpointRegistrationRequest request) throws GeneralSecurityException {
        byte[] pKeyData = request.getEndpointPublicKey().array();
        return KeyUtil.getPublic(pKeyData);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand#processParsedRequest(org.apache.avro.specific.SpecificRecordBase)
     */
    @Override
    protected SyncResponse processParsedRequest(EndpointRegistrationRequest request) throws GetDeltaException {
        return operationsService.registerEndpoint(request).getResponse();
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.CommandProcessor#isNeedConnectionClose()
     */
    @Override
    public boolean isNeedConnectionClose() {
        return true;
    }
    
    public static String getCommandName() {
        return ENDPOINT_REGISTER_COMMAND;
    }
}
