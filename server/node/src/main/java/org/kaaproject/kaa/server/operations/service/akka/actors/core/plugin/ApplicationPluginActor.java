/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
