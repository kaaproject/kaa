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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.endpoint.gen.RedirectSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.server.common.http.server.BadRequestException;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.RuleTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyEncodedRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyEndpointSyncMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.NettyDecodedResponseMessage;
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

    /** The eps actor. */
    private final ActorRef epsActor;

    /** Current redirection rules */
    private final HashMap<Long, RedirectionRule> redirectionRules; //NOSONAR

    /** random */
    private final Random random;

    /**
     * Instantiates a new enc dec actor.
     *
     * @param epsActor
     *            the eps actor
     */
    public EncDecActor(ActorRef epsActor) {
        super();
        this.epsActor = epsActor;
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

        /**
         * Instantiates a new actor creator.
         *
         * @param epsActor
         *            the eps actor
         */
        public ActorCreator(ActorRef epsActor) {
            super();
            this.epsActor = epsActor;
        }

        /*
         * (non-Javadoc)
         *
         * @see akka.japi.Creator#create()
         */
        @Override
        public EncDecActor create() throws Exception {
            return new EncDecActor(epsActor);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        LOG.debug("Received: {}", message);
        if (message instanceof NettyEncodedRequestMessage) {
            RedirectionRule redirection = checkRedirection(redirectionRules, random.nextDouble());
            if (redirection == null) {
                decodeAndForward((NettyEncodedRequestMessage) message);
            } else {
                encodeAndReply(redirection, (NettyEncodedRequestMessage) message);
            }
        } else if (message instanceof NettyDecodedResponseMessage) {
            encodeAndReply((NettyDecodedResponseMessage) message);
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
                .scheduleOnce(Duration.create(body.ruleTTL, TimeUnit.MILLISECONDS), self(), new RuleTimeoutMessage(body.getRuleId()), context().dispatcher(),
                        self());
        redirectionRules.put(body.getRuleId(), body);
    }

    private void removeRedirectionRule(RuleTimeoutMessage message) {
        if (redirectionRules.remove(message.getRuleId()) != null) {
            LOG.info("Redirection rule {} removed", message.getRuleId());
        }
    }

    /**
     * Decode and forward.
     *
     * @param msg
     *            the msg
     */
    private void decodeAndForward(NettyEncodedRequestMessage msg) {
        try {
            SpecificRecordBase record = msg.getCommand().decode();
            if (record instanceof SyncRequest) {
                NettyEndpointSyncMessage nettySyncMessage = new NettyEndpointSyncMessage((SyncRequest) record, msg);
                this.epsActor.tell(nettySyncMessage.toEndpointMessage(self()), self());
            } else {
                LOG.warn("unknown request param: {}", record);
                msg.getChannelContext().fireExceptionCaught(new RuntimeException("unknown request param: " + record));
            }
        } catch (BadRequestException | GeneralSecurityException | IOException e) {
            LOG.trace("Command decode failed " + msg);
            msg.getChannelContext().fireExceptionCaught(e);
        }
    }

    /**
     * Encode and reply.
     *
     * @param msg
     *            the msg
     */
    private void encodeAndReply(NettyDecodedResponseMessage msg) {
        try {
            LOG.debug("ENCODE AND REPLY " + msg);
            msg.getCommand().encode(msg.getResponse());
            msg.getChannelContext().writeAndFlush(msg.getCommand());
        } catch (GeneralSecurityException | IOException e) {
            LOG.debug("Command decode failed " + msg);
            msg.getChannelContext().fireExceptionCaught(e);
        }
    }

    private void encodeAndReply(RedirectionRule redirection, NettyEncodedRequestMessage message) {
        RedirectSyncResponse redirectSyncResponse = new RedirectSyncResponse(redirection.getDnsName());
        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.REDIRECT);
        response.setRedirectSyncResponse(redirectSyncResponse);
        encodeAndReply(new NettyDecodedResponseMessage(message.getHandlerUuid(), message.getChannelContext(), message.getCommand(), message.getCommand()
                .getChannelType(), response));
    }

    public static RedirectionRule checkRedirection(HashMap<Long, RedirectionRule> redirectionRules, double random) { //NOSONAR
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
