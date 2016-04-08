package org.kaaproject.kaa.server.common.paf.shared.system;

import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.common.exception.PafErrorCode;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.PafService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

public class DefaultPafApplicationRouter extends AbstractMappingMessageRouter implements PafApplicationRouter {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPafApplicationRouter.class);
    
    @Autowired
    private PafService pafService;
    
    @Override
    protected List<Object> getChannelKeys(Message<?> message) {
      PafMessage pafMessage = (PafMessage) message;
      ApplicationRoute applicationRoute = pafMessage.getApplicationRoute();
      if (applicationRoute != null) {
          MessageChannel channel = pafService.findApplicationRequestChannel(applicationRoute);
          LOG.info("Detected channel [{}]", channel);
          if (channel != null) {
              return Collections.singletonList(channel);
          }
      } else {
          throw pafMessage.error(PafErrorCode.MISSING_ATTRIBUTE, "Missing ApplicationRoute attribute");
      }
      return Collections.emptyList();
    }

}
