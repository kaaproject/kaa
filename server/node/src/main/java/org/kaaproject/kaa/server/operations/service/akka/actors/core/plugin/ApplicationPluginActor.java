package org.kaaproject.kaa.server.operations.service.akka.actors.core.plugin;

import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import akka.japi.Creator;

public class ApplicationPluginActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationPluginActor.class);

    private final String pluginInstanceId;
    private final ApplicationPluginActorMessageProcessor processor;

    private ApplicationPluginActor(AkkaContext context, String pluginInstanceId) {
        this.pluginInstanceId = pluginInstanceId;
        this.processor = new ApplicationPluginActorMessageProcessor(context, pluginInstanceId);
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<ApplicationPluginActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The Akka service context */
        private final AkkaContext context;

        private final String pluginInstanceId;

        /**
         * Instantiates a new actor creator.
         *
         * @param logAppenderService
         *            the log appender service
         */
        public ActorCreator(AkkaContext context, String pluginInstanceId) {
            super();
            this.context = context;
            this.pluginInstanceId = pluginInstanceId;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public ApplicationPluginActor create() throws Exception {
            return new ApplicationPluginActor(context, pluginInstanceId);
        }
    }

    @Override
    public void onReceive(Object arg0) throws Exception {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#preStart()
     */
    @Override
    public void preStart() {
        LOG.info("[{}] Starting ", pluginInstanceId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        processor.stop();
        LOG.info("[{}] Stoped ", pluginInstanceId);
    }

}
