/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.control.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.gen.VersionConnectionInfoPair;
import org.kaaproject.kaa.server.resolve.OperationsServerResolver;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultControlServiceTest {

  private DefaultControlService service;

  private OperationsNodeInfo node;

  @Test
  public void writeLogWithoutByteBufferTest() throws Exception {
    final String format = "Update of node {} is pushed to resolver {}";
    String beforeReplacing = node.toString();

    Method method = service.getClass().getDeclaredMethod("writeLogWithoutByteBuffer", String.class, node.getClass(), OperationsServerResolver.class);
    method.setAccessible(true);
    method.invoke(service, format, node, null);

    Assert.assertEquals("Object corrupted, some fields changed and not recover", beforeReplacing, node.toString());
  }

  @Before
  public void setUp() {
    List<VersionConnectionInfoPair> connectionInfoList = new ArrayList<>();
    connectionInfoList.addAll(Arrays.asList(
        new VersionConnectionInfoPair(1, ByteBuffer.wrap(new byte[] {1, 2, 3})),
        new VersionConnectionInfoPair(2, ByteBuffer.wrap(new byte[] {4, 5, 6})),
        new VersionConnectionInfoPair(3, ByteBuffer.wrap(new byte[] {5, 6, 7}))
        ));

    List<VersionConnectionInfoPair> connectionInfoList2 = new ArrayList<>();
    connectionInfoList2.addAll(Arrays.asList(
        new VersionConnectionInfoPair(4, ByteBuffer.wrap(new byte[] {7, 8, 9})),
        new VersionConnectionInfoPair(5, ByteBuffer.wrap(new byte[] {1, 2, 4})),
        new VersionConnectionInfoPair(6, ByteBuffer.wrap(new byte[] {5, 6, 7}))
    ));

    List<TransportMetaData> transports = new ArrayList<>();
    transports.addAll(Arrays.asList(
        new TransportMetaData(1, 1, 100, connectionInfoList),
        new TransportMetaData(2, 2, 200, connectionInfoList2)
    ));

    node = new OperationsNodeInfo();
    node.setTransports(transports);
    node.setConnectionInfo(new ConnectionInfo(null, 0, ByteBuffer.wrap(new byte[] {101, 102, 103})));
    service = new DefaultControlService();
  }
}