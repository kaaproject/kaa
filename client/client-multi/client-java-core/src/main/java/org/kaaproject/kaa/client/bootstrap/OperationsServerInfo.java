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

package org.kaaproject.kaa.client.bootstrap;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import org.kaaproject.kaa.common.endpoint.security.KeyUtil;

public final class OperationsServerInfo {
    private final PublicKey key;
    private final String hostName;

    public OperationsServerInfo(String hostName, byte [] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.hostName = hostName;
        this.key = KeyUtil.getPublic(publicKey);
    }

    public PublicKey getKey() {
        return key;
    }

    public String getHostName() {
        return hostName;
    }
    
    public String getURL() {
        return "http://" + hostName;
    }

}
