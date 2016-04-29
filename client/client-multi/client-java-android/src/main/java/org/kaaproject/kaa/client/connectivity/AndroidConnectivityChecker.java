/*
 * Copyright 2014-2016 CyberVision, Inc.
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


package org.kaaproject.kaa.client.connectivity;

import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AndroidConnectivityChecker implements ConnectivityChecker {
    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(AndroidConnectivityChecker.class);

    private final Context context;

    public AndroidConnectivityChecker(Context context) {
        this.context = context;
    }

    @Override
    public boolean checkConnectivity() {
        boolean isConnectionExists = false;

        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] networkInfos = cm.getAllNetworkInfo();

            if (networkInfos != null) {
                for (NetworkInfo tempNetworkInfo : networkInfos) {
                    if (tempNetworkInfo.isConnected()) {
                        isConnectionExists = true;
                        LOG.info("Connection to the network exists");
                        break;
                    }
                }
            }
        }

        return isConnectionExists;
    }
}
