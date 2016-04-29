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

package org.kaaproject.kaa.server.transport.session;

import java.util.UUID;

import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder.CipherPair;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.platform.PlatformAware;

/**
 * Represents information about the session.
 * 
 * @author Andrew Shvayka
 *
 */
public final class SessionInfo implements PlatformAware{
    private final UUID uuid;
    private final int platformId;
    private final ChannelContext ctx;
    private final ChannelType channelType;
    private final CipherPair cipherPair;
    private final EndpointObjectHash key;
    private final String applicationToken;
    private final String sdkToken;
    private final int keepAlive;
    private final boolean isEncrypted;

    public SessionInfo(UUID uuid, int platformId, ChannelContext ctx, ChannelType channelType, CipherPair cipherPair, EndpointObjectHash key,
            String applicationToken, String sdkToken, int keepAlive, boolean isEncrypted) {
        super();
        this.uuid = uuid;
        this.platformId = platformId;
        this.ctx = ctx;
        this.channelType = channelType;
        this.cipherPair = cipherPair;
        this.key = key;
        this.applicationToken = applicationToken;
        this.sdkToken = sdkToken;
        this.keepAlive = keepAlive;
        this.isEncrypted = isEncrypted;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getPlatformId() {
        return platformId;
    }

    public ChannelContext getCtx() {
        return ctx;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public CipherPair getCipherPair() {
        return cipherPair;
    }

    public EndpointObjectHash getKey() {
        return key;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public String getSdkToken() {
        return sdkToken;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SessionInfo other = (SessionInfo) obj;
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NettySessionInfo [uuid=");
        builder.append(uuid);
        builder.append(", key=");
        builder.append(key);
        builder.append(", applicationToken=");
        builder.append(applicationToken);
        builder.append(", sdkToken=");
        builder.append(sdkToken);
        builder.append("]");
        return builder.toString();
    }
}
