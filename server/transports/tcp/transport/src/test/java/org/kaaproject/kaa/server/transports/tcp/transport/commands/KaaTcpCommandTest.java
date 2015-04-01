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
package org.kaaproject.kaa.server.transports.tcp.transport.commands;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame;

public class KaaTcpCommandTest {


    @Test
    public void testKaaTcpCommand(){
        KaaTcpCommandFactory commandFactory = new KaaTcpCommandFactory();
        KaaTcpCommand command = (KaaTcpCommand)commandFactory.createCommandProcessor();
        Assert.assertNotNull(command);
        Assert.assertEquals(KaaTcpCommand.KAA_TCP, command.getName());
    }


    @Test
    public void testGetSet(){
        int id = 1;
        long syncTime = 1;
        MqttFrame mqttFrame = new Connect();
        KaaTcpCommand kaaTcpCommand = new KaaTcpCommand();
        kaaTcpCommand.setCommandId(id);
        kaaTcpCommand.setResponse(mqttFrame);
        kaaTcpCommand.setSyncTime(syncTime);
        Assert.assertEquals(id, kaaTcpCommand.getCommandId());
        Assert.assertEquals(mqttFrame, kaaTcpCommand.getResponse());
        Assert.assertEquals(syncTime, kaaTcpCommand.getSyncTime());
    }

}
