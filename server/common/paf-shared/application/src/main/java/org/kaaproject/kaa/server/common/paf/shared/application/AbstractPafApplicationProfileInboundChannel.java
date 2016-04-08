package org.kaaproject.kaa.server.common.paf.shared.application;

import java.util.Set;

import org.kaaproject.kaa.server.common.paf.shared.common.AbstractPafChannelBean;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationProfileRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPafApplicationProfileInboundChannel extends AbstractPafChannelBean 
                                                             implements PafApplicationProfileInboundChannel {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPafApplicationProfileInboundChannel.class);
    
    protected Set<ApplicationProfileRoute> applicationProfileRoutes;
    
    public AbstractPafApplicationProfileInboundChannel() {
        super();
    }
    
    @Override
    protected void onInit() throws Exception {
        super.onInit();
        applicationProfileRoutes = getApplicationProfileRoutes();       
    }

    protected abstract Set<ApplicationProfileRoute> getApplicationProfileRoutes();

    @Override
    protected void doStart() {
        pafService.registerApplicationProfileRoutes(applicationProfileRoutes, this);
    };
    
    @Override
    protected void doStop() {
        pafService.deregisterApplicationProfileRoutes(applicationProfileRoutes);
    };

}
