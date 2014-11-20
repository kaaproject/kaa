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

package org.kaaproject.kaa.demo.smarthousedemo.command;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.EndpointOperationResultListener;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.demo.smarthousedemo.exception.CommandException;

import android.util.Log;

/** Implementation of attach endpoint to user command. */
public class AttachEndpointCommand extends AbstractClientCommand<String> {

    private String endpointAccessToken;
    
    public AttachEndpointCommand(KaaClient client, String endpointAccessToken) {
        super(client);
        this.endpointAccessToken = endpointAccessToken;
    }

    @Override
    protected void executeAsync() {
        Log.d("Kaa", "Attaching endpoint to user account!");
        EndpointAccessToken accessToken = new EndpointAccessToken(endpointAccessToken);
        client.getEndpointRegistrationManager().attachEndpoint(accessToken, 
            new EndpointOperationResultListener() {
                @Override
                public void sendResponse(String operation, SyncResponseResultType result, Object context) {
                    if (result==SyncResponseResultType.SUCCESS) {
                        Log.d("Kaa", "Endpoint attached to user account!");
                        String endpointKeyHash = ((EndpointKeyHash)context).getKeyHash();
                        onComplete(endpointKeyHash);
                    }
                    else {
                        Log.e("Kaa", "Unable to attach endpoint to user account!");
                        onException(new CommandException("Unable to attach endpoint to user account!"));
                    }
                }
        });
    }

}
