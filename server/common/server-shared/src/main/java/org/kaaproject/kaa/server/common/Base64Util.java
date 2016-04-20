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

package org.kaaproject.kaa.server.common;

import org.apache.commons.codec.binary.Base64;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;

public class Base64Util {

    private static final String UNKNOWN = "Unknown";

    private Base64Util() {
    }

    public static String encode(byte[] data){
        return Base64.encodeBase64String(data);
    }

    public static String encode(EndpointProfileDto profile){
        if(profile != null && profile.getEndpointKeyHash() != null){
            return encode(profile.getEndpointKeyHash());
        }else{
            return UNKNOWN;
        }
    }

    public static byte[] decode(String base64String) {
        return Base64.decodeBase64(base64String);
    }


}
