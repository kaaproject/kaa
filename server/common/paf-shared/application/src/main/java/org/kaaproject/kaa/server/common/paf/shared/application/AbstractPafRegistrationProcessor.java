package org.kaaproject.kaa.server.common.paf.shared.application;

import org.kaaproject.kaa.server.common.paf.shared.common.AbstractPafProcessor;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.common.exception.PafErrorCode;
import org.kaaproject.kaa.server.common.paf.shared.context.RegistrationResult;
import org.springframework.messaging.MessageChannel;

public abstract class AbstractPafRegistrationProcessor extends AbstractPafProcessor implements PafRegistrationProcessor {

    protected MessageChannel registrationProcessorChannel;
    
    @Override
    protected void onInit() throws Exception {
        super.onInit();
        registrationProcessorChannel = getApplicationContext().getBean("registrationProcessorChannel", MessageChannel.class);
    }

    @Override
    public PafMessage onMessage(PafMessage kaaPafMessage) {
        if (kaaPafMessage.getRegistrationResult() == null) {
            performRegistration(kaaPafMessage);
        } else {
            if (kaaPafMessage.getRegistrationResult() == RegistrationResult.KO) {
                throw kaaPafMessage.error(PafErrorCode.ENDPOINT_REGISTRATION_FAILED, "Endpoint registration failed!");
            } else {
                return kaaPafMessage;
            }
        }
        return null;
    }
    
    protected abstract void performRegistration(PafMessage message);

}
