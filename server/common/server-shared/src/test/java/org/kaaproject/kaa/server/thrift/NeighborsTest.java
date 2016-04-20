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

package org.kaaproject.kaa.server.thrift;

import org.junit.Test;
import org.kaaproject.kaa.server.common.thrift.KaaThriftService;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.sync.Event;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class NeighborsTest {
    @SuppressWarnings("unchecked")
    @Test
    public void sendMessageTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
        ConnectionInfo connectionInfo1 = new ConnectionInfo("thriftHost", 9999, ByteBuffer.allocate(10));
        NeighborTemplate<Event> template = (NeighborTemplate<Event>) mock(NeighborTemplate.class);
        Neighbors<NeighborTemplate<Event>, Event> neighbors = new Neighbors<>(KaaThriftService.OPERATIONS_SERVICE, template, 1);
        ReflectionTestUtils.setField(neighbors, "zkId", "someZkId");
        NeighborConnection<NeighborTemplate<Event>, Event> neighborConnection = new NeighborConnection<>(connectionInfo1, 1, null);
        LinkedBlockingQueue<Event> eventQueue = spy(new LinkedBlockingQueue<Event>());
        ReflectionTestUtils.setField(neighborConnection, "messageQueue", eventQueue);
        ConcurrentMap<String, NeighborConnection<NeighborTemplate<Event>, Event>> neighborMap =
                spy((ConcurrentMap<String, NeighborConnection<NeighborTemplate<Event>, Event>>) ReflectionTestUtils.getField(neighbors, "neigbors"));
        ReflectionTestUtils.setField(neighbors, "neigbors", neighborMap);
        when(neighborMap.get(anyString())).thenReturn(neighborConnection);
        Collection<Event> messages = new ArrayList<>();
        Event e = new Event(10, "FQN", null, null, null);
        messages.add(e);
        Collection<Event> messagesSpy = spy(messages);
        neighbors.sendMessages(connectionInfo1, messagesSpy);
        verify(messagesSpy, timeout(1000)).iterator();
        verify(eventQueue, timeout(1000)).offer(eq(e), anyLong(), any(TimeUnit.class));
        neighbors.shutdown();
    }
}
