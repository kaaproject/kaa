package org.kaaproject.kaa.server.common.paf.shared.common.resolver;

import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.EndpointId;

public interface EndpointIdResolver {

    EndpointId resolveEndpointId(PafMessage message);
    
}
