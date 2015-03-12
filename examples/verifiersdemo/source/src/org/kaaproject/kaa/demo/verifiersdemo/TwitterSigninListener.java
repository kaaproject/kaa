/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.demo.verifiersdemo;

import android.util.Log;
import android.view.View;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;

public class TwitterSigninListener extends Callback<TwitterSession> implements View.OnClickListener {
    private LoginActivity parentActivity;
    private static String TAG = "Example-Twitter";
    private boolean isClicked;

    public TwitterSigninListener(LoginActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    // Is called after onClick()
    @Override
    public void success(Result<TwitterSession> twitterSessionResult) {
        Log.i(TAG, twitterSessionResult.toString());
        if (isClicked) {
            String accessToken = twitterSessionResult.data.getAuthToken().token + " " +
                    twitterSessionResult.data.getAuthToken().secret;
            String userId = String.valueOf(twitterSessionResult.data.getUserId());
            String userName = twitterSessionResult.data.getUserName();

            Log.i(TAG, "Token: " +  accessToken);
            Log.i(TAG, "User id: " + userId);
            Log.i(TAG, "User name: " + userName);

            parentActivity.updateUI(userName, userId, accessToken, LoginActivity.AccountType.TWITTER);
        }
    }

    @Override
    public void failure(TwitterException e) {
        Log.i(TAG, e.toString());
    }


    @Override
    public void onClick(View v) {
        Log.i(TAG, "Twitter button clicked");
        isClicked = true;
    }
}
