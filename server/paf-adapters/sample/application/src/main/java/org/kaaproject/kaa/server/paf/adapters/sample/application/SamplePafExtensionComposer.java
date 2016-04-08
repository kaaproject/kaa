package org.kaaproject.kaa.server.paf.adapters.sample.application;

import org.kaaproject.kaa.server.common.paf.shared.application.PafExtensionComposer;
import org.kaaproject.kaa.server.common.paf.shared.common.AbstractPafProcessor;
import org.kaaproject.kaa.server.common.paf.shared.common.context.PafInboundSessionMessage;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;

public class SamplePafExtensionComposer extends AbstractPafProcessor implements PafExtensionComposer {

    @Override
    public PafMessage onMessage(PafMessage kaaPafMessage) {        
        SamplePafSessionContext context = new SamplePafSessionContext(kaaPafMessage, 
                kaaPafMessage.getSystemLevelReplyChannel());
        pafService.sendMessage(new PafInboundSessionMessage(context));
        return null;
    }
    
}
