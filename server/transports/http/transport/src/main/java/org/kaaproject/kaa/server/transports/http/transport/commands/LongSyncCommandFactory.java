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

package org.kaaproject.kaa.server.transports.http.transport.commands;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.server.common.server.KaaCommandProcessor;

public class LongSyncCommandFactory extends SyncCommandFactory{

    @Override
    public String getCommandName() {
        return CommonEPConstans.LONG_SYNC_COMMAND;
    }

    @Override
    public KaaCommandProcessor<HttpRequest, HttpResponse> createCommandProcessor() {
        return setupCommand(new LongSyncCommand());
    }

}
