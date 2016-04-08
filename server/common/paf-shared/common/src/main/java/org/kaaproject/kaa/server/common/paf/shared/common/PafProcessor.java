package org.kaaproject.kaa.server.common.paf.shared.common;

import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;

public interface PafProcessor {

    public PafMessage onMessage(PafMessage kaaPafMessage);
    
}
