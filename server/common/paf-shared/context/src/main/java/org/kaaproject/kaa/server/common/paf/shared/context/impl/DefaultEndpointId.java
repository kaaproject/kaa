package org.kaaproject.kaa.server.common.paf.shared.context.impl;

import java.util.Arrays;

import org.kaaproject.kaa.server.common.paf.shared.context.EndpointId;

public class DefaultEndpointId implements EndpointId {
    
    private final byte[] data;

    public DefaultEndpointId(byte[] data) {
        super();
        this.data = data;
    }

    @Override
    public byte[] toBytes() {
        return data;
    }

    @Override
    public String toString() {
        return "DefaultEndpointId [data=" + Arrays.toString(data) + "]";
    }

}
