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

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.kaaproject.kaa.common.Constants.RESPONSE_CONTENT_TYPE;
import io.netty.handler.codec.http.HttpResponse;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transports.http.transport.netty.AbstractCommand;

public class SyncCommandTest {

    @Test
    public void testSyncCommand() throws Exception {
        SyncCommandFactory commandFactory = new SyncCommandFactory();
        SyncCommand command = (SyncCommand)commandFactory.createCommandProcessor();
        command.setResponseBody("responseBody".getBytes());
        HttpResponse response = command.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(RESPONSE_CONTENT_TYPE, response.headers().get(CONTENT_TYPE));
        Assert.assertEquals(ChannelType.SYNC, command.getChannelType());
        Assert.assertEquals("", command.getName());
        Assert.assertEquals("", AbstractCommand.getCommandName());
        command.call();
    }

    @Test
    public void testLongSyncCommand(){
        SyncCommandFactory commandFactory = new LongSyncCommandFactory();
        SyncCommand command = (LongSyncCommand)commandFactory.createCommandProcessor();
        command.setResponseBody("responseBody".getBytes());
        HttpResponse response = command.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(RESPONSE_CONTENT_TYPE, response.headers().get(CONTENT_TYPE));
        Assert.assertEquals(ChannelType.SYNC_WITH_TIMEOUT, command.getChannelType());
    }
}
