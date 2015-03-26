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
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;

import android.util.Log;

/** The implementation of the detach endpoint from user command. */
public class DetachEndpointCommand extends AbstractClientCommand<Boolean> {

    private String endpointKeyHash;
    
    public DetachEndpointCommand(KaaClient client, String endpointKeyHash) {
        super(client);
        this.endpointKeyHash = endpointKeyHash;
    }

    @Override
    protected void executeAsync() {
        Log.d("Kaa", "Detaching endpoint from user account!");
        EndpointKeyHash endpointKey = new EndpointKeyHash(endpointKeyHash);
        client.detachEndpoint(endpointKey, new OnDetachEndpointOperationCallback() {
            @Override
            public void onDetach(SyncResponseResultType result) {
                if (result == SyncResponseResultType.SUCCESS) {
                    Log.d("Kaa", "Endpoint detached from user account!");
                    onComplete(true);
                } else {
                    Log.w("Kaa", "Endpoint already detached from user account!");
                    onComplete(false);
                }
            }
        });
    }

}
