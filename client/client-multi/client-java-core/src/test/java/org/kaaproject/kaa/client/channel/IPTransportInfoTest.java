package org.kaaproject.kaa.client.channel;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.impl.channels.TransportIdConstants;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;

public class IPTransportInfoTest {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    protected static final int SIZE_OF_INT = 4;

    @Test
    public void testInit() throws NoSuchAlgorithmException {
        IPTransportInfo info = new IPTransportInfo(createTestServerInfo(ServerType.OPERATIONS, TransportIdConstants.TCP_TRANSPORT_ID,
                "localhost", 80, KeyUtil.generateKeyPair().getPublic()));
        
        Assert.assertEquals(ServerType.OPERATIONS, info.getServerType());
        Assert.assertEquals(TransportIdConstants.TCP_TRANSPORT_ID, info.getTransportId());
        Assert.assertEquals("localhost", info.getHost());
        Assert.assertEquals(80, info.getPort());
    }

    public static ServerInfo createTestServerInfo(ServerType serverType, TransportId id, String host, int port, PublicKey key) {
        ProtocolMetaData md = buildMetaData(id, host, port, key);
        return new GenericTransportInfo(serverType, md);
    }

    public static ProtocolMetaData buildMetaData(TransportId id, String host, int port, PublicKey key) {
        ByteBuffer buf = ByteBuffer.wrap(new byte[SIZE_OF_INT * 3 + host.getBytes(UTF8).length + key.getEncoded().length]);
        buf.putInt(key.getEncoded().length);
        buf.put(key.getEncoded());
        buf.putInt(host.getBytes(UTF8).length);
        buf.put(host.getBytes(UTF8));
        buf.putInt(port);
        ProtocolMetaData md = new ProtocolMetaData((host + ":" + port).hashCode(), id.getProtocolId(), id.getProtocolVersion(), buf);
        return md;
    }

}
