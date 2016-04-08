package org.kaaproject.kaa.server.common.paf.shared.common.resolver;

import org.kaaproject.kaa.server.common.paf.shared.common.AbstractPafProcessor;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationProfileRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.EndpointId;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionId;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionType;

public class ConfigurableResolver extends AbstractPafProcessor implements GenericResolver {
    
    protected SessionId configSessionId;
    protected SessionType configSessionType;
    protected ApplicationRoute configApplicationRoute;
    protected ApplicationProfileRoute configApplicationProfileRoute;
    protected EndpointId configEndpointId;
    
    @Override
    protected void onInit() throws Exception {
        initConfigValues();
    }
    
    protected void initConfigValues() {
        configSessionId = null;
        configSessionType = null;
        configApplicationRoute = null;
        configApplicationProfileRoute = null;
        configEndpointId = null;
    }
    
    @Override
    public PafMessage onMessage(PafMessage kaaPafMessage) {
        SessionId sessionId = resolveSessionId(kaaPafMessage);
        if (sessionId != null) {
            kaaPafMessage.setSessionId(sessionId);
        }
        SessionType sessionType = resolveSessionType(kaaPafMessage);
        if (sessionType != null) {
            kaaPafMessage.setSessionType(sessionType);
        }
        ApplicationRoute applicationRoute = resolveApplicationRoute(kaaPafMessage);
        if (applicationRoute != null) {
            kaaPafMessage.setApplicationRoute(applicationRoute);
        }
        ApplicationProfileRoute applicationProfileRoute = resolveApplicationProfileRoute(kaaPafMessage);
        if (applicationProfileRoute != null) {
            kaaPafMessage.setApplicationProfileRoute(applicationProfileRoute);
        }
        EndpointId endpointId = resolveEndpointId(kaaPafMessage);
        if (endpointId != null) {
            kaaPafMessage.setEndpointId(endpointId);
        }
        return kaaPafMessage;
    }

    @Override
    public SessionId resolveSessionId(PafMessage message) {
        return configSessionId;
    }

    @Override
    public SessionType resolveSessionType(PafMessage message) {
        return configSessionType;
    }

    @Override
    public ApplicationRoute resolveApplicationRoute(PafMessage message) {
        return configApplicationRoute;
    }

    @Override
    public ApplicationProfileRoute resolveApplicationProfileRoute(PafMessage message) {
        return configApplicationProfileRoute;
    }

    @Override
    public EndpointId resolveEndpointId(PafMessage message) {
        return configEndpointId;
    }

}
