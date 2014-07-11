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
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;


/**
 * The Class UpdateEndpointCommand.
 */
public class SyncCommand extends AbstractOperationsCommand<SyncRequest, SyncResponse> implements CommonEPConstans {

    static {
        COMMAND_NAME = SYNC_COMMAND;
        LOG.info("CommandName: " + COMMAND_NAME);
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.HTTP;
    }

    /**
     * Instantiates a new sync command.
     */
    public SyncCommand() {
        super();
        LOG.trace("CommandName: " + COMMAND_NAME + ": Created..");
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand#getRequestConverterClass()
     */
    @Override
    protected Class<SyncRequest> getRequestConverterClass() {
        return SyncRequest.class;
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
    protected PublicKey getPublicKey(SyncRequest request) throws GeneralSecurityException {
        PublicKey endpointKey = null;
        if(request.getProfileSyncRequest() != null && request.getProfileSyncRequest().getEndpointPublicKey() != null){
            byte[] publicKeySrc = request.getProfileSyncRequest().getEndpointPublicKey().array();
            endpointKey = KeyUtil.getPublic(publicKeySrc);
        }
        if(endpointKey == null){
            EndpointObjectHash hash = EndpointObjectHash.fromBytes(request.getSyncRequestMetaData().getEndpointPublicKeyHash().array());
            endpointKey = cacheService.getEndpointKey(hash);
        }
        return endpointKey;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand#processParsedRequest(org.apache.avro.specific.SpecificRecordBase)
     */
    @Override
    protected SyncResponse processParsedRequest(SyncRequest epRequest) throws GetDeltaException {
        return operationsService.sync(epRequest).getResponse();
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.CommandProcessor#isNeedConnectionClose()
     */
    @Override
    public boolean isNeedConnectionClose() {
        return true;
    }

    public static String getCommandName() {
        return SYNC_COMMAND;
    }
}
