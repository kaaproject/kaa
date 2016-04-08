package org.kaaproject.kaa.server.common.paf.shared.application;

import java.util.Set;

import org.kaaproject.kaa.server.common.paf.shared.common.AbstractPafChannelBean;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPafApplicationInboundChannel extends AbstractPafChannelBean 
                                                             implements PafApplicationInboundChannel {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPafApplicationInboundChannel.class);
    
    protected Set<ApplicationRoute> applicationRoutes;
    
    public AbstractPafApplicationInboundChannel() {
        super();
    }
    
    @Override
    protected void onInit() throws Exception {
        super.onInit();
        applicationRoutes = getApplicationRoutes();       
    }

    protected abstract Set<ApplicationRoute> getApplicationRoutes();

    @Override
    protected void doStart() {
        pafService.registerApplicationRoutes(applicationRoutes, this);
    };
    
    @Override
    protected void doStop() {
        pafService.deregisterApplicationRoutes(applicationRoutes);
    };

}
