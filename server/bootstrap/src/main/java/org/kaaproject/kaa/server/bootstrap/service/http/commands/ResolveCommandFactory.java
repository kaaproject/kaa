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
package org.kaaproject.kaa.server.bootstrap.service.http.commands;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.bootstrap.service.security.KeyStoreService;
import org.kaaproject.kaa.server.common.server.KaaCommandProcessor;
import org.kaaproject.kaa.server.common.server.KaaCommandProcessorFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ResolveCommandFactory implements KaaCommandProcessorFactory<HttpRequest, HttpResponse>{

    @Autowired
    private OperationsServerListService operationsServerListService;
    @Autowired
    private KeyStoreService keyStoreService;

    @Override
    public String getCommandName() {
        return ResolveCommand.getCommandName();
    }

    @Override
    public KaaCommandProcessor<HttpRequest, HttpResponse> createCommandProcessor() {
        ResolveCommand command = new ResolveCommand();
        command.operationsServerListService = operationsServerListService;
        command.crypt =  new MessageEncoderDecoder(keyStoreService.getPrivateKey(), keyStoreService.getPublicKey(), null);
        return command;
    }

}
