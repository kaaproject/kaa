package org.kaaproject.kaa.server.common.paf.shared.common;

import org.kaaproject.kaa.server.common.paf.shared.context.PafService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.endpoint.AbstractEndpoint;

public abstract class AbstractPafBean extends AbstractEndpoint {

    @Autowired 
    protected PafService pafService;
    
    @Override
    protected void doStart() {};
    
    @Override
    protected void doStop() {};

}
