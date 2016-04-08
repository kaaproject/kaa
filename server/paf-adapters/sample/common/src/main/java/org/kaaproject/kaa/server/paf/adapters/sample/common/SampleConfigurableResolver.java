package org.kaaproject.kaa.server.paf.adapters.sample.common;

import org.apache.commons.codec.binary.Base64;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.common.resolver.ConfigurableResolver;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationProfileRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.EndpointId;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionId;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionType;
import org.kaaproject.kaa.server.common.paf.shared.context.impl.DefaultEndpointId;
import org.kaaproject.kaa.server.common.paf.shared.context.impl.StringApplicationProfileRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.impl.StringApplicationRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.impl.StringSessionId;

public class SampleConfigurableResolver extends ConfigurableResolver {

    @Override
    protected void initConfigValues() {
        configSessionType = SessionType.ASYNC;
    }

    @Override
    public SessionId resolveSessionId(PafMessage message) {
        byte[] payload = message.getPayload();
        String[] data = new String(payload).split("\\|");
        return new StringSessionId(data[0]); 
    }

    @Override
    public ApplicationRoute resolveApplicationRoute(PafMessage message) {
        String[] data = new String(message.getPayload()).split("\\|");
        return new StringApplicationRoute(data[1]);
    }

    @Override
    public ApplicationProfileRoute resolveApplicationProfileRoute(PafMessage message) {
        String[] data = new String(message.getPayload()).split("\\|");
        return new StringApplicationProfileRoute(data[2]);
    }

    @Override
    public EndpointId resolveEndpointId(PafMessage message) {
        byte[] payload = message.getPayload();
        String[] data = new String(payload).split("\\|");
        String strEndpointId = data[3];
        return new DefaultEndpointId(Base64.decodeBase64(strEndpointId));        
    }
    
}
