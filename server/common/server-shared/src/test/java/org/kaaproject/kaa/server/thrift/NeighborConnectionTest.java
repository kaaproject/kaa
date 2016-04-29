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

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.sync.Event;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

public class NeighborConnectionTest {
    private NeighborConnection<NeighborTemplate<Event>, Event> neighborConnection;
    private NeighborTemplate<Event> template = (NeighborTemplate<Event>) mock(NeighborTemplate.class);

    @Before
    public void before() throws InterruptedException {
        ConnectionInfo connectionInfo = new ConnectionInfo("thriftHost", 10101, ByteBuffer.allocate(10));
        neighborConnection = new NeighborConnection<>(connectionInfo, 1, template);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void startTest() throws TException, InterruptedException {
        neighborConnection.start();
        ExecutorService executorSpy = getSpyOnExecutorAndInjectIt();
        neighborConnection.sendMessages(Collections.singleton(new Event()));
        verify(template, timeout(1000)).process(any(OperationsThriftService.Iface.class), anyList());
        neighborConnection.shutdown();
        verify(executorSpy, timeout(1000)).shutdown();
    }

    @Test
    public void startTExceptionThrownTest() throws InterruptedException, TException {
        TException tException = new TException();
        doThrow(tException).when(template).process(any(OperationsThriftService.Iface.class), anyList());
        neighborConnection.start();
        ExecutorService executorSpy = getSpyOnExecutorAndInjectIt();
        neighborConnection.sendMessages(Arrays.asList(new Event()));
        verify(template, timeout(1000)).onServerError(anyString(), eq(tException));
        neighborConnection.shutdown();
        verify(executorSpy, timeout(1000)).shutdown();
    }

    private ExecutorService getSpyOnExecutorAndInjectIt() {
        ExecutorService executorSpy = spy((ExecutorService) ReflectionTestUtils.getField(neighborConnection, "executor"));
        ReflectionTestUtils.setField(neighborConnection, "executor", executorSpy);
        return executorSpy;
    }

    @Test
    public void equalsHashCodeTest() {
        ConnectionInfo connectionInfo1 = new ConnectionInfo("thriftHost1", 9991, null);
        ConnectionInfo connectionInfo2 = new ConnectionInfo("thriftHost1", 9991, null);
        ConnectionInfo connectionInfo3 = new ConnectionInfo("thriftHost1", 9993, null);

        NeighborConnection<NeighborTemplate<Event>, Event> neighborConnection1 = new NeighborConnection<>(connectionInfo1, 10, null);
        NeighborConnection<NeighborTemplate<Event>, Event> neighborConnection2 = new NeighborConnection<>(connectionInfo2, 8, null);
        NeighborConnection<NeighborTemplate<Event>, Event> neighborConnection3 = new NeighborConnection<>(connectionInfo3, 7, null);

        Assert.assertEquals(neighborConnection1.hashCode(), neighborConnection2.hashCode());
        Assert.assertNotEquals(neighborConnection1.hashCode(), neighborConnection3.hashCode());

        Assert.assertEquals(neighborConnection1, neighborConnection1);
        Assert.assertNotEquals(neighborConnection1, null);
        Assert.assertNotEquals(neighborConnection1, new Object());
        Assert.assertEquals(neighborConnection1, neighborConnection2);
        Assert.assertNotEquals(neighborConnection1, neighborConnection3);
    }
}
