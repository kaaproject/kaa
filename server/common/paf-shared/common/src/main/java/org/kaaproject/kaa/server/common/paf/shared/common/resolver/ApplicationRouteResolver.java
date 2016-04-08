package org.kaaproject.kaa.server.common.paf.shared.common.resolver;

import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationRoute;

public interface ApplicationRouteResolver {

    ApplicationRoute resolveApplicationRoute(PafMessage message);
    
}
