package org.kaaproject.kaa.server.plugin.messaging;

import java.util.UUID;

import org.kaaproject.kaa.server.common.core.plugin.instance.KaaMessage;

public class SdkMessage implements KaaMessage {

    private static final long serialVersionUID = -8369085602140052037L;

    private final UUID uid;
    private final byte[] data;

    public SdkMessage(UUID uid, byte[] data) {
        super();
        this.uid = uid;
        this.data = data;
    }

    public UUID getUid() {
        return uid;
    }

    public byte[] getData() {
        return data;
    }

}
