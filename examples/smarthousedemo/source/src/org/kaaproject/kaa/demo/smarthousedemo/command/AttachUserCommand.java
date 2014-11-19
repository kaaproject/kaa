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
import org.kaaproject.kaa.client.event.registration.UserAuthResultListener;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.smarthousedemo.exception.CommandException;

import android.util.Log;

/** Implementation of attach to user command. */
public class AttachUserCommand extends AbstractClientCommand<Boolean> {

    /** Default user account access token */
    private static final String USER_ACCESS_TOKEN = "dummy_acess_token";
    
    private String userAccount;
    
    public AttachUserCommand(KaaClient client, String userAccount) {
        super(client);
        this.userAccount = userAccount;
    }

    @Override
    protected void executeAsync() {
        Log.d("Kaa", "Attaching to user account!");
        client.getEndpointRegistrationManager().attachUser(userAccount, USER_ACCESS_TOKEN, 
                new UserAuthResultListener() {
            @Override
            public void onAuthResult(UserAttachResponse response) {
                if (response.getResult()==SyncResponseResultType.SUCCESS) {
                    Log.d("Kaa", "Attached to user account!");
                    onComplete(true);
                }
                else {
                    Log.e("Kaa", "Unable to attach to user account!");
                    onException(new CommandException("Unable to attach to user account!"));
                }
            }
        });
    }

}
