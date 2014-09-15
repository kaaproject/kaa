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
package org.kaaproject.kaa.server.operations.service.netty;

import java.util.UUID;

import io.netty.channel.ChannelHandlerContext;

import javax.crypto.SecretKey;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;

public final class NettySessionInfo {
    private final UUID uuid;
    private final ChannelHandlerContext ctx;
    private final ChannelType channelType;
    private final SecretKey sessionKey;
    private final EndpointObjectHash key;
    private final String applicationToken;
    private final int keepAlive;
    private final boolean isEncrypted;

    public NettySessionInfo(UUID uuid, ChannelHandlerContext ctx, ChannelType channelType, SecretKey sessionKey, EndpointObjectHash key,
            String applicationToken, int keepAlive, boolean isEncrypted) {
        super();
        this.uuid = uuid;
        this.ctx = ctx;
        this.channelType = channelType;
        this.sessionKey = sessionKey;
        this.key = key;
        this.applicationToken = applicationToken;
        this.keepAlive = keepAlive;
        this.isEncrypted = isEncrypted;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public SecretKey getSessionKey() {
        return sessionKey;
    }

    public EndpointObjectHash getKey() {
        return key;
    }

    public String getApplicationToken() {
        return applicationToken;
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
        NettySessionInfo other = (NettySessionInfo) obj;
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
        builder.append("KaaTcpSessionInfo [sessionKey=");
        builder.append(sessionKey);
        builder.append(", key=");
        builder.append(key);
        builder.append(", applicationToken=");
        builder.append(applicationToken);
        builder.append("]");
        return builder.toString();
    }
}