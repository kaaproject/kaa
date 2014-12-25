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
package org.kaaproject.kaa.server.operations.service.akka.messages.io.response;

import org.kaaproject.kaa.server.operations.pojo.sync.ServerSync;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.ChannelAware;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.PlatformAware;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ErrorBuilder;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ResponseBuilder;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;

public interface SessionResponse extends ChannelAware, PlatformAware{

    int getPlatformId();
    ServerSync getResponse();
    NettySessionInfo getSessionInfo();
    ResponseBuilder getResponseConverter();
    ErrorBuilder getErrorConverter();

}
