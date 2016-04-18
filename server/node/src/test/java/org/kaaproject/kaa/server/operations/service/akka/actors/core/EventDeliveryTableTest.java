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

package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.event.EndpointEvent;
import org.kaaproject.kaa.server.operations.service.event.EventDeliveryTable;
import org.kaaproject.kaa.server.operations.service.event.RouteTableAddress;
import org.kaaproject.kaa.server.sync.Event;

public class EventDeliveryTableTest {

    @Test
    public void testEventDeliveryTable(){
        EventDeliveryTable table = new EventDeliveryTable();
        EndpointEvent event = new EndpointEvent(EndpointObjectHash.fromSHA1("sender"), new Event(1, "eventClassFQN", ByteBuffer.wrap(new byte[3]), "sender", "target"));
        RouteTableAddress routeAddress = new RouteTableAddress(EndpointObjectHash.fromSHA1("target"), "applicationToken");
        RouteTableAddress routeAddress2 = new RouteTableAddress(EndpointObjectHash.fromSHA1("target2"), "applicationToken");

        Assert.assertFalse(table.clear(event));
        table.registerDeliveryAttempt(event, routeAddress);

        Assert.assertTrue(table.isDeliveryStarted(event, routeAddress));
        Assert.assertFalse(table.isDeliveryStarted(event, routeAddress2));

        table.registerDeliveryFailure(event, routeAddress);
        Assert.assertFalse(table.isDeliveryStarted(event, routeAddress));

        table.registerDeliveryAttempt(event, routeAddress2);
        table.registerDeliverySuccess(event, routeAddress2);
        table.registerDeliverySuccess(event, routeAddress);

        Assert.assertTrue(table.isDeliveryStarted(event, routeAddress));
        Assert.assertTrue(table.isDeliveryStarted(event, routeAddress2));



    }

}
