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

package org.kaaproject.kaa.client.util;

public class AndroidBase64 implements Base64 {

    private static AndroidBase64 instance;
    
    public static AndroidBase64 getInstance() {
        if (instance == null) {
            instance = new AndroidBase64();
        }
        return instance;
    }
    
    @Override
    public byte[] decodeBase64(byte[] base64Data) {
        return android.util.Base64.decode(base64Data,  android.util.Base64.DEFAULT);
    }

    @Override
    public byte[] encodeBase64(byte[] binaryData) {
        return android.util.Base64.encode(binaryData, android.util.Base64.DEFAULT);
    }

    @Override
    public byte[] decodeBase64(String base64String) {
        return android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
    }

}
