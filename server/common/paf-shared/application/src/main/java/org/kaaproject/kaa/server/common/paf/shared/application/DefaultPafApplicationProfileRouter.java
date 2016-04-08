package org.kaaproject.kaa.server.common.paf.shared.application;

import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.common.exception.PafErrorCode;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationProfileRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.PafService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

public class DefaultPafApplicationProfileRouter extends AbstractMappingMessageRouter implements PafApplicationProfileRouter {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPafApplicationProfileRouter.class);
    
    @Autowired
    private PafService pafService;
    
    @Override
    protected List<Object> getChannelKeys(Message<?> message) {
      PafMessage pafMessage = (PafMessage) message;
      ApplicationProfileRoute applicationProfileRoute = pafMessage.getApplicationProfileRoute();
      if (applicationProfileRoute != null) {
          MessageChannel channel = pafService.findApplicationProfileRequestChannel(applicationProfileRoute);
          LOG.info("Detected channel [{}]", channel);
          if (channel != null) {
              return Collections.singletonList(channel);
          }
      } else {
          throw pafMessage.error(PafErrorCode.MISSING_ATTRIBUTE, "Missing ApplicationProfileRoute attribute");
      }
      return Collections.emptyList();
    }

}
