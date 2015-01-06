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
package org.kaaproject.kaa.server.operations.service.akka.messages.io.request;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;

public class NettyTcpSyncMessage extends AbstractRequestMessage implements SessionAwareRequest {

    private final SyncRequest command;
    private final NettySessionInfo sessionInfo;

    public NettyTcpSyncMessage(SyncRequest command, NettySessionInfo sessionInfo,
            ResponseBuilder responseConverter, ErrorBuilder errorConverter, SyncStatistics syncStatistics) {
        super(sessionInfo.getUuid(), sessionInfo.getPlatformId(), sessionInfo.getCtx(), sessionInfo.getChannelType(), responseConverter, errorConverter, syncStatistics);
        this.command = command;
        this.sessionInfo = sessionInfo;
    }

    @Override
    public byte[] getEncodedRequestData() {
        return command.getAvroObject();
    }

    @Override
    public NettySessionInfo getSessionInfo() {
        return sessionInfo;
    }

    @Override
    public boolean isEncrypted() {
        return command.isEncrypted();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NettyTcpSyncMessage [command=");
        builder.append(command);
        builder.append(", sessionInfo=");
        builder.append(sessionInfo);
        builder.append(", getUuid()=");
        builder.append(getChannelUuid());
        builder.append(", getChannelContext()=");
        builder.append(getChannelContext());
        builder.append(", getChannelType()=");
        builder.append(getChannelType());
        builder.append("]");
        return builder.toString();
    }
}
