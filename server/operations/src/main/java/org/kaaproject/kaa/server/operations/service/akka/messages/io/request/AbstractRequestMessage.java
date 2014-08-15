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

import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;

import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;

public class AbstractRequestMessage {
    private final UUID uuid;
    private final ChannelHandlerContext channelContext;
    private final ChannelType channelType;
    private final ResponseBuilder responseConverter;
    private final ErrorBuilder errorConverter;
    private final SyncStatistics syncStatistics;

    protected AbstractRequestMessage(UUID uuid, ChannelHandlerContext channelContext, ChannelType channelType, ResponseBuilder responseConverter,
            ErrorBuilder errorConverter, SyncStatistics syncStatistics) {
        super();
        this.uuid = uuid;
        this.channelContext = channelContext;
        this.channelType = channelType;
        this.responseConverter = responseConverter;
        this.errorConverter = errorConverter;
        this.syncStatistics = syncStatistics;
    }

    public UUID getChannelUuid() {
        return uuid;
    }

    public ChannelHandlerContext getChannelContext() {
        return channelContext;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public ResponseBuilder getResponseBuilder() {
        return responseConverter;
    }

    public ErrorBuilder getErrorBuilder() {
        return errorConverter;
    }

    public SyncStatistics getSyncStatistics() {
        return syncStatistics;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AbstractRequestMessage [uuid=");
        builder.append(uuid);
        builder.append(", channelContext=");
        builder.append(channelContext);
        builder.append(", channelType=");
        builder.append(channelType);
        builder.append(", responseConverter=");
        builder.append(responseConverter);
        builder.append(", errorConverter=");
        builder.append(errorConverter);
        builder.append("]");
        return builder.toString();
    }
}
