package org.kaaproject.kaa.server.common.paf.shared.application;

import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.common.exception.PafErrorCode;
import org.kaaproject.kaa.server.common.paf.shared.context.PafService;
import org.kaaproject.kaa.server.common.paf.shared.context.RegistrationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

public class DefaultPafEndpointRegistrationRouter extends AbstractMappingMessageRouter implements PafEndpointRegistrationRouter {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPafEndpointRegistrationRouter.class);
    
    @Autowired
    private PafService pafService;
    
    private MessageChannel registrationChannel;
    
    private MessageChannel extensionChannel;
    
    @Override
    protected List<Object> getChannelKeys(Message<?> message) {
      PafMessage pafMessage = (PafMessage) message;
      if (pafMessage.getEndpointId() != null) {
          MessageChannel targetChannel = null;
          if (pafMessage.getRegistrationResult() != null &&
                  pafMessage.getRegistrationResult() == RegistrationResult.OK) {
              targetChannel = extensionChannel;
          } else {
              EndpointProfileDto endpointProfile = pafService.getEndpointProfileById(pafMessage.getEndpointId());
              targetChannel = endpointProfile != null ? extensionChannel : registrationChannel;
          }
          return Collections.singletonList(targetChannel);
      } else {
          throw pafMessage.error(PafErrorCode.MISSING_ATTRIBUTE, "Missing EndpointId attribute");
      }
    }

    @Override
    public void setRegistrationChannel(MessageChannel registrationChannel) {
        this.registrationChannel = registrationChannel;
    }

    @Override
    public void setExtensionChannel(MessageChannel extensionChannel) {
        this.extensionChannel = extensionChannel;
    }

}
