package org.kaaproject.kaa.server.common.core.plugin.messaging;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaMessage;

public class EndpointMessage implements KaaMessage {

    private static final long serialVersionUID = -7358355594071995237L;

    private final EndpointObjectHash key;

    public EndpointMessage(EndpointObjectHash key) {
        super();
        this.key = key;
    }

    public EndpointObjectHash getKey() {
        return key;
    }

    public byte[] getMessageData() {
        return null;
    }

}
