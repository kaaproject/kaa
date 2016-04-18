/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.operations.service.akka;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.OperationsServerActor;
import org.kaaproject.kaa.server.operations.service.akka.actors.io.EncDecActor;
import org.kaaproject.kaa.server.operations.service.akka.actors.supervision.SupervisionStrategyFactory;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.stats.StatusRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserConfigurationUpdate;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserConfigurationUpdateMessage;
import org.kaaproject.kaa.server.sync.platform.PlatformLookup;
import org.kaaproject.kaa.server.transport.message.SessionInitMessage;
import org.kaaproject.kaa.server.transport.session.SessionAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.Broadcast;
import akka.routing.RoundRobinPool;

/**
 * The Class DefaultAkkaService.
 */
@Service
public class DefaultAkkaService implements AkkaService {

    public static final String IO_DISPATCHER_NAME = "io-dispatcher";
    public static final String CORE_DISPATCHER_NAME = "core-dispatcher";
    public static final String USER_DISPATCHER_NAME = "user-dispatcher";
    public static final String ENDPOINT_DISPATCHER_NAME = "endpoint-dispatcher";
    public static final String LOG_DISPATCHER_NAME = "log-dispatcher";
    public static final String VERIFIER_DISPATCHER_NAME = "verifier-dispatcher";
    public static final String TOPIC_DISPATCHER_NAME = "topic-dispatcher";

    private static final String IO_ROUTER_ACTOR_NAME = "ioRouter";

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAkkaService.class);

    /** The Constant EPS. */
    public static final String EPS = "EPS";

    /** The akka. */
    private ActorSystem akka;

    /** The eps actor. */
    private ActorRef opsActor;

    /** The io router. */
    private ActorRef ioRouter;

    /** The akka service context. */
    @Autowired
    private AkkaContext context;

    private AkkaEventServiceListener eventListener;
    
    private AkkaClusterServiceListener clusterListener;

    private StatusListenerThread statusListenerThread;

    /**
     * Inits the actor system.
     */
    @PostConstruct
    public void initActorSystem() {
        LOG.info("Initializing Akka system...");
        akka = ActorSystem.create(EPS, context.getConfig());
        LOG.info("Initializing Akka EPS actor...");
        opsActor = akka.actorOf(Props.create(new OperationsServerActor.ActorCreator(context)).withDispatcher(CORE_DISPATCHER_NAME), EPS);
        LOG.info("Lookup platform protocols");
        Set<String> platformProtocols = PlatformLookup.lookupPlatformProtocols(PlatformLookup.DEFAULT_PROTOCOL_LOOKUP_PACKAGE_NAME);
        LOG.info("Initializing Akka io router...");
        ioRouter = akka.actorOf(
                new RoundRobinPool(context.getIOWorkerCount())
                        .withSupervisorStrategy(SupervisionStrategyFactory.createIORouterStrategy(context))
                        .props(Props.create(new EncDecActor.ActorCreator(opsActor, context, platformProtocols))
                                .withDispatcher(IO_DISPATCHER_NAME)), IO_ROUTER_ACTOR_NAME);
        LOG.info("Initializing Akka event service listener...");
        eventListener = new AkkaEventServiceListener(opsActor);
        context.getEventService().addListener(eventListener);
        clusterListener = new AkkaClusterServiceListener(opsActor);
        context.getClusterService().setListener(clusterListener);
        LOG.info("Initializing Akka system done");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.akka.AkkaService#getActorSystem
     * ()
     */
    @Override
    public ActorSystem getActorSystem() {
        return akka;
    }

    AkkaEventServiceListener getListener() {
        return eventListener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.akka.AkkaService#
     * onRedirectionRule
     * (org.kaaproject.kaa.server.common.thrift.gen.endpoint.RedirectionRule)
     */
    @Override
    public void onRedirectionRule(RedirectionRule redirectionRule) {
        ioRouter.tell(new Broadcast(redirectionRule), ActorRef.noSender());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.akka.AkkaService#onNotification
     * (org.kaaproject.kaa.server.common.thrift.gen.endpoint.Notification)
     */
    @Override
    public void onNotification(Notification notification) {
        ApplicationDto applicationDto = context.getApplicationService().findAppById(notification.getAppId());
        if (applicationDto != null) {
            LOG.debug("Sending message {} to EPS actor", notification);
            opsActor.tell(new ThriftNotificationMessage(applicationDto.getApplicationToken(), notification), ActorRef.noSender());
        } else {
            LOG.warn("Can't find corresponding application for: {}", notification);
        }
    }

    @PreDestroy
    public void preDestroy() {
        context.getEventService().removeListener(eventListener);
    }

    @Override
    public void process(SessionAware message) {
        ioRouter.tell(message, ActorRef.noSender());
    }

    @Override
    public void process(SessionInitMessage message) {
        ioRouter.tell(message, ActorRef.noSender());
    }

    @Override
    public void onUserConfigurationUpdate(UserConfigurationUpdate update) {
        opsActor.tell(new UserConfigurationUpdateMessage(update), ActorRef.noSender());
    }

    @Override
    public void setStatusListener(final AkkaStatusListener listener, final long statusUpdateFrequency) {
        this.statusListenerThread = new StatusListenerThread(listener, statusUpdateFrequency);
        this.statusListenerThread.start();
    }

    @Override
    public void removeStatusListener() {
        this.statusListenerThread.stopped = true;
        this.statusListenerThread.interrupt();
    }

    public class StatusListenerThread extends Thread {

        private final AkkaStatusListener listener;
        private final long statusUpdateFrequency;

        private volatile boolean stopped = false;

        public StatusListenerThread(AkkaStatusListener listener, long statusUpdateFrequency) {
            super();
            this.listener = listener;
            this.statusUpdateFrequency = statusUpdateFrequency;
        }

        @Override
        public void run() {
            while (!stopped) {
                try {
                    Thread.sleep(statusUpdateFrequency);
                } catch (InterruptedException e) {
                    if (!stopped) {
                        LOG.warn("Status update thread was interrupted", e);
                    } else {
                        break;
                    }
                }
                opsActor.tell(new StatusRequestMessage(listener), ActorRef.noSender());
            }
        }
    }
}
