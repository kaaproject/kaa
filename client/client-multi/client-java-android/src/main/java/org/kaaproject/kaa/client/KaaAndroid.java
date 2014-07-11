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

package org.kaaproject.kaa.client;

import android.content.Context;


public class KaaAndroid extends Kaa {

    private static Context context; 
    
    public KaaAndroid(Context context) throws Exception {
        KaaAndroid.context = context;
        init();
    }
    
    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException("Android context is not set!");
        }
        return context;
    }
    
    @Override
    protected AbstractKaaClient createClient() throws Exception {
        return new AndroidKaaClient();
    }


}
