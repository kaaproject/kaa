package org.kaaproject.kaa.server.paf.adapters.sample.application;

import org.kaaproject.kaa.server.common.paf.shared.application.AbstractPafRegistrationProcessor;
import org.kaaproject.kaa.server.common.paf.shared.common.context.PafInboundSessionMessage;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;

public class SamplePafRegistrationProcessor extends AbstractPafRegistrationProcessor {

    @Override
    protected void performRegistration(PafMessage message) {
        SamplePafSessionContext context = new SamplePafSessionContext(message, 
                registrationProcessorChannel);
        pafService.sendRegistrationMessage(new PafInboundSessionMessage(context)); 
    }
    
}
