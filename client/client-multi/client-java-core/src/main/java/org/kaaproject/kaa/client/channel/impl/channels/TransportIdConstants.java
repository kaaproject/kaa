package org.kaaproject.kaa.client.channel.impl.channels;

import org.kaaproject.kaa.client.channel.TransportId;

/**
 * Class to hold transport id constants. Please note that this constants should
 * match same constants in appropriate transport configs on server side
 * 
 * @author Andrew Shvayka
 *
 */
public class TransportIdConstants {

    private static final int HTTP_TRANSPORT_PROTOCOL_ID = 0xfb9a3cf0;
    private static final int HTTP_TRANSPORT_PROTOCOL_VERSION = 1;
    
    private static final int TCP_TRANSPORT_PROTOCOL_ID = 0x56c8ff92;
    private static final int TCP_TRANSPORT_PROTOCOL_VERSION = 1;
    
    public static final TransportId HTTP_TRANSPORT_ID = new TransportId(HTTP_TRANSPORT_PROTOCOL_ID, HTTP_TRANSPORT_PROTOCOL_VERSION);
    public static final TransportId TCP_TRANSPORT_ID = new TransportId(TCP_TRANSPORT_PROTOCOL_ID, TCP_TRANSPORT_PROTOCOL_VERSION);
}
