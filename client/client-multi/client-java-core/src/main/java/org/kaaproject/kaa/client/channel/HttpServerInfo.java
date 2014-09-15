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

package org.kaaproject.kaa.client.channel;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import org.kaaproject.kaa.common.bootstrap.CommonBSConstants;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;

public class HttpServerInfo extends AbstractServerInfo {
    private final String url;

    public HttpServerInfo(ServerType type, String hostName, int port, byte[] publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        super(type, hostName, port, KeyUtil.getPublic(publicKey));
        url = constructURL(type);
    }

    public HttpServerInfo(ServerType type, String hostName, int port, PublicKey publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        super(type, hostName, port, publicKey);
        url = constructURL(type);
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.HTTP;
    }

    private String constructURL(ServerType type) {
        String url = "http://" + getHost() + ":" + getPort();
        if (type == ServerType.OPERATIONS) {
            url += CommonEPConstans.SYNC_URI;
        } else {
            url += CommonBSConstants.BOOTSTRAP_RESOLVE_URI;
        }

        return url;
    }
}
