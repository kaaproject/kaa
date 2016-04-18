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

package org.kaaproject.kaa.server.common.dao.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.DtoByteMarshaller;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;

public class SdkTokenGenerator {
    
    private static final String SDK_TOKEN_HASH_ALGORITHM = "SHA1";

    private SdkTokenGenerator() {
    }

    public static void generateSdkToken(SdkProfileDto sdkProfileDto) {
        if (StringUtils.isEmpty(sdkProfileDto.getToken())) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(SDK_TOKEN_HASH_ALGORITHM);
                messageDigest.update(DtoByteMarshaller.toBytes(sdkProfileDto.toSdkTokenDto()));
                sdkProfileDto.setToken(Base64.encodeBase64URLSafeString(messageDigest.digest()));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
}
