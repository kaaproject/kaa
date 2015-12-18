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
package org.kaaproject.kaa.client.plugin.messaging.common.v1;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.kaaproject.kaa.client.plugin.PluginAdapter;
import org.kaaproject.kaa.client.plugin.PluginContext;
import org.kaaproject.kaa.client.plugin.PluginInitializationException;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.future.MessageFuture;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.future.MessageFutureCancelListener;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.AckMessage;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.ErrorMessage;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.Message;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.MessageDecoder;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.MessageType;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.PayloadMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMessagingPlugin implements PluginAdapter, MessageFutureCancelListener {

    protected static final int MESSAGE_COUNT_SIZE = 4;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMessagingPlugin.class);

    protected PluginContext context;

    protected final ConcurrentMap<UUID, MessageFuture<? extends Object>> futureMap;

    protected final ConcurrentMap<UUID, Message> pendingMap;

    public AbstractMessagingPlugin() {
        super();
        futureMap = new ConcurrentHashMap<UUID, MessageFuture<? extends Object>>();
        pendingMap = new ConcurrentHashMap<UUID, Message>(); 
    }

    public void init(PluginContext context) throws PluginInitializationException {
        LOG.info("Initializing plugin with context {}", context);
        this.context = context;
    }

    @Override
    public byte[] getPendingData() {
        int length = 0;
        List<Message> msgs = new ArrayList<Message>(pendingMap.values());
        for (Message msg : msgs) {
            length += msg.getSize();
        }
        ByteBuffer bb = ByteBuffer.wrap(new byte[length + MESSAGE_COUNT_SIZE]);
        bb.putInt(msgs.size());
        for (Message msg : msgs) {
            msg.pushTo(bb);
        }
        return bb.array();
    }

    @Override
    public void processData(byte[] data) {
        List<Message> msgs = MessageDecoder.decode(ByteBuffer.wrap(data));
        for (Message msg : msgs) {
            if (msg.getType() == MessageType.ACK) {
                handleAckMsg(msg.getUid());
                continue;
            } else if (msg.getType() == MessageType.VOID) {
                handleVoidMsg(msg);
            } else if (msg.getType() == MessageType.ENTITY) {
                handleEntityMsg((PayloadMessage) msg);
            } else if (msg.getType() == MessageType.ERROR) {
                handleErrorMsg((ErrorMessage) msg);
            }
            ack(msg);
        }
    }

    protected void pushFuture(MessageFuture<? extends Object> future) {
        pushMsg(future.getMsg());
        futureMap.put(future.getUid(), future);
    }

    protected void pushMsg(Message msg) {
        pendingMap.put(msg.getUid(), msg);
        context.sync();
    }

    protected void handleAckMsg(UUID ackUid) {
        Message msg = pendingMap.remove(ackUid);
        if (msg != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Received acknowledgement for message {}", msg);
            } else {
                LOG.debug("[{}] Received acknowledgement", msg.getUid());
            }
        } else {
            LOG.debug("[{}] Received duplicate acknowledgement", ackUid);
        }
    }

    protected void handleErrorMsg(ErrorMessage msg) {
        MessageFuture<? extends Object> future = futureMap.remove(msg.getUid());
        if (future != null) {
            future.setFailure(msg.getException());
        } else {
            LOG.debug("[{}] Received duplicate response", msg.getUid());
        }
    }

    protected void ack(Message msg) {
        pushMsg(new AckMessage(msg.getUid(), msg.getMethodId()));
    }
    
    @Override
    public void onCanceled(UUID uid) {
        MessageFuture<? extends Object> future = futureMap.remove(uid);
        LOG.debug("[{}] Future {} was canceled", uid, future);
    }

    protected abstract void handleEntityMsg(PayloadMessage msg);

    protected abstract void handleVoidMsg(Message msg);

}
