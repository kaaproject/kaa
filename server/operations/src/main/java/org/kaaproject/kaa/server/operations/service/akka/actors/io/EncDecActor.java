/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.operations.service.akka.actors.io;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.RuleTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SessionAware;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SessionAwareRequest;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SessionInitRequest;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.SessionResponse;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.metrics.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.japi.Creator;

/**
 * The Class EncDecActor.
 */
public class EncDecActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EncDecActor.class);

    private final EncDecActorMessageProcessor messageProcessor;
    /** Current redirection rules */
    private final HashMap<Long, RedirectionRule> redirectionRules; // NOSONAR
    /** random */
    private final Random random;

    /**
     * Instantiates a new enc dec actor.
     * 
     * @param epsActor
     *            the eps actor
     * @param supportUnencryptedConnection
     */
    public EncDecActor(ActorRef epsActor, MetricsService metricsService, CacheService cacheService, KeyPair serverKeys,
            Set<String> platformProtocols, Boolean supportUnencryptedConnection) {
        super();
        this.messageProcessor = new EncDecActorMessageProcessor(epsActor, metricsService, cacheService, serverKeys, platformProtocols,
                supportUnencryptedConnection);
        this.redirectionRules = new HashMap<>();
        this.random = new Random();
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#preStart()
     */
    @Override
    public void preStart() {
        LOG.info("Starting " + this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        LOG.info("Stoped " + this);
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<EncDecActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The eps actor. */
        private final ActorRef epsActor;

        private final MetricsService metricsService;

        private final CacheService cacheService;

        private final KeyPair serverKeys;

        private final Boolean supportUnencryptedConnection;

        private final Set<String> platformProtocols;

        /**
         * Instantiates a new actor creator.
         * 
         * @param epsActor
         *            the eps actor
         */
        public ActorCreator(ActorRef epsActor, MetricsService metricsService, CacheService cacheService, KeyPair serverKeys,
                Set<String> platformProtocols, Boolean supportUnencryptedConnection) {
            super();
            this.epsActor = epsActor;
            this.metricsService = metricsService;
            this.cacheService = cacheService;
            this.serverKeys = serverKeys;
            this.platformProtocols = new HashSet<String>(platformProtocols);
            this.supportUnencryptedConnection = supportUnencryptedConnection;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public EncDecActor create() throws Exception {
            return new EncDecActor(epsActor, metricsService, cacheService, serverKeys, platformProtocols, supportUnencryptedConnection);
        }

        public Set<String> getPlatformProtocols() {
            return platformProtocols;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        LOG.debug("Received: {}", message.getClass().getName());
        if (message instanceof SessionInitRequest) {
            RedirectionRule redirection = checkRedirection(redirectionRules, random.nextDouble());
            if (redirection == null) {
                messageProcessor.decodeAndForward(context(), (SessionInitRequest) message);
            } else {
                messageProcessor.redirect(redirection, (SessionInitRequest) message);
            }
        } else if (message instanceof SessionAware) {
            if (message instanceof SessionAwareRequest) {
                RedirectionRule redirection = checkRedirection(redirectionRules, random.nextDouble());
                if (redirection == null) {
                    messageProcessor.decodeAndForward(context(), (SessionAwareRequest) message);
                } else {
                    messageProcessor.redirect(redirection, (SessionAwareRequest) message);
                }
            } else {
                messageProcessor.forward(context(), (SessionAware) message);
            }
        } else if (message instanceof SessionResponse) {
            messageProcessor.encodeAndReply((SessionResponse) message);
        } else if (message instanceof RedirectionRule) {
            applyRedirectionRule((RedirectionRule) message);
        } else if (message instanceof RuleTimeoutMessage) {
            removeRedirectionRule((RuleTimeoutMessage) message);
        }
    }

    private void applyRedirectionRule(RedirectionRule body) {
        context()
                .system()
                .scheduler()
                .scheduleOnce(Duration.create(body.ruleTTL, TimeUnit.MILLISECONDS), self(), new RuleTimeoutMessage(body.getRuleId()),
                        context().dispatcher(), self());
        redirectionRules.put(body.getRuleId(), body);
    }

    private void removeRedirectionRule(RuleTimeoutMessage message) {
        if (redirectionRules.remove(message.getRuleId()) != null) {
            LOG.info("Redirection rule {} removed", message.getRuleId());
        }
    }

    public static RedirectionRule checkRedirection(HashMap<Long, RedirectionRule> redirectionRules, double random) { // NOSONAR
        RedirectionRule result = null;
        for (RedirectionRule rule : redirectionRules.values()) {
            if (random <= rule.redirectionProbability) {
                result = rule;
                break;
            } else {
                random -= rule.redirectionProbability;
            }
        }
        return result;
    }
}
