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
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;


/**
 * The Class UpdateEndpointCommand.
 */
public class UpdateEndpointCommand extends AbstractOperationsCommand<ProfileUpdateRequest, SyncResponse> implements
        CommonEPConstans {

    static {
        COMMAND_NAME = ENDPOINT_UPDATE_COMMAND;
        LOG.info("CommandName: " + COMMAND_NAME);
    }

    /**
     * Instantiates a new update endpoint command.
     */
    public UpdateEndpointCommand() {
        super();
        LOG.trace("CommandName: " + COMMAND_NAME + ": Created..");
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand#getRequestConverterClass()
     */
    @Override
    protected Class<ProfileUpdateRequest> getRequestConverterClass() {
        return ProfileUpdateRequest.class;
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
    protected PublicKey getPublicKey(ProfileUpdateRequest request) throws GeneralSecurityException {
        EndpointObjectHash hash = EndpointObjectHash.fromBytes(request.getEndpointPublicKeyHash().array());
        PublicKey endpointKey = cacheService.getEndpointKey(hash);
        return endpointKey;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand#processParsedRequest(org.apache.avro.specific.SpecificRecordBase)
     */
    @Override
    protected SyncResponse processParsedRequest(ProfileUpdateRequest epRequest) throws GetDeltaException {
        return operationsService.updateProfile(epRequest).getResponse();
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.CommandProcessor#isNeedConnectionClose()
     */
    @Override
    public boolean isNeedConnectionClose() {
        return true;
    }
    
    public static String getCommandName() {
        return ENDPOINT_UPDATE_COMMAND;
    }
}
