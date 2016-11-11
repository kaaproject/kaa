package org.kaaproject.kaa.server.control.service;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.gen.VersionConnectionInfoPair;
import org.kaaproject.kaa.server.resolve.OperationsServerResolver;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DefaultControlServiceTest {
    private static OperationsNodeInfo node;
    private static DefaultControlService service;

    @BeforeClass
    public static void prepareData() {
        List<VersionConnectionInfoPair> connectionInfoList = new ArrayList<>();
        List<VersionConnectionInfoPair> connectionInfoList2 = new ArrayList<>();
        connectionInfoList.add(new VersionConnectionInfoPair(1, ByteBuffer.wrap(new byte[]{1, 2, 3})));
        connectionInfoList.add(new VersionConnectionInfoPair(2, ByteBuffer.wrap(new byte[]{4, 5, 6})));
        connectionInfoList.add(new VersionConnectionInfoPair(3, ByteBuffer.wrap(new byte[]{5, 6, 7})));
        connectionInfoList2.add(new VersionConnectionInfoPair(4, ByteBuffer.wrap(new byte[]{7, 8, 9})));
        connectionInfoList2.add(new VersionConnectionInfoPair(5, ByteBuffer.wrap(new byte[]{1, 2, 4})));
        connectionInfoList2.add(new VersionConnectionInfoPair(6, ByteBuffer.wrap(new byte[]{5, 6, 7})));
        TransportMetaData transport1 = new TransportMetaData(1, 1, 100, connectionInfoList);
        TransportMetaData transport2 = new TransportMetaData(2, 2, 200, connectionInfoList2);
        List<TransportMetaData> transports = new ArrayList<>();
        transports.add(transport1);
        transports.add(transport2);

        node = new OperationsNodeInfo();
        node.setTransports(transports);
        node.setConnectionInfo(new ConnectionInfo(null, 0, ByteBuffer.wrap(new byte[]{101, 102, 103})));
        service = new DefaultControlService();
    }

    @Test
    public void testWriteLogWithoutByteBuffer() throws Exception {
        String logStr = "Update of node {} is pushed to resolver {}";

        Method method = service.getClass().getDeclaredMethod("writeLogWithoutByteBuffer", String.class, node.getClass(), OperationsServerResolver.class);
        method.setAccessible(true);
        String beforeReplacing = node.toString();
        System.out.println("Before replacing");
        System.out.println(beforeReplacing);
        method.invoke(service, logStr, node, null);
        System.out.println("After replacing");
        System.out.println(node);
        Assert.assertEquals("Object corrupted, some fields changed and not recover ", beforeReplacing, node.toString());
    }

}