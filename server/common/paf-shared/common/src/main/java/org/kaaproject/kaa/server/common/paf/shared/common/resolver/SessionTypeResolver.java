package org.kaaproject.kaa.server.common.paf.shared.common.resolver;

import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionType;

public interface SessionTypeResolver {

    SessionType resolveSessionType(PafMessage message);
    
}
