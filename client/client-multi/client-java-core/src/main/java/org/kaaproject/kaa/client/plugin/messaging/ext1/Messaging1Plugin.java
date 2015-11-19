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
package org.kaaproject.kaa.client.plugin.messaging.ext1;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Future;

import org.kaaproject.kaa.client.plugin.PluginAdapter;
import org.kaaproject.kaa.client.plugin.PluginInstance;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.AbstractMessagingPlugin;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.future.MessageFuture;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.EntityMessage;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.ErrorCode;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.ErrorMessage;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.Message;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.PayloadMessage;
import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.VoidMessage;
import org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA;
import org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB;
import org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Messaging1Plugin extends AbstractMessagingPlugin implements PluginInstance<Messaging1PluginAPI>, Messaging1PluginAPI {

    private static final Logger LOG = LoggerFactory.getLogger(Messaging1Plugin.class);

    /**
     * Auto-generated constant for method
     * {@link org.kaaproject.kaa.client.plugin.messaging.ext1.Messaging1PluginAPI#setMethodAListener(MethodAListener)
     * setMethodAListener}
     */
    private final short METHOD_1_ID = 1;

    /**
     * Auto-generated constant for method
     * {@link org.kaaproject.kaa.client.plugin.messaging.ext1.Messaging1PluginAPI#setMethodBListener(MethodBListener)
     * setMethodBListener}
     */
    private final short METHOD_2_ID = 2;

    /**
     * Auto-generated constant for method
     * {@link org.kaaproject.kaa.client.plugin.messaging.ext1.Messaging1PluginAPI#setMethodCListener(MethodCListener)
     * setMethodCListener}
     */
    private final short METHOD_3_ID = 3;

    /**
     * Auto-generated constant for method
     * {@link org.kaaproject.kaa.client.plugin.messaging.ext1.Messaging1PluginAPI#sendA(ClassA)
     * sendA}
     */
    private final short METHOD_4_ID = 4;

    /**
     * Auto-generated constant for method
     * {@link org.kaaproject.kaa.client.plugin.messaging.ext1.Messaging1PluginAPI#getA()
     * get}
     */
    private final short METHOD_5_ID = 5;

    /**
     * Auto-generated constant for method
     * {@link org.kaaproject.kaa.client.plugin.messaging.ext1.Messaging1PluginAPI#getB(ClassA)
     * getB}
     */
    private final short METHOD_6_ID = 6;

    /**
     * Auto-generated constant for method
     * {@link org.kaaproject.kaa.client.plugin.messaging.ext1.Messaging1PluginAPI#getC(ClassA)
     * getC}
     */
    private final short METHOD_7_ID = 7;

    /**
     * Auto-generated thread-safe entity converter for {@link ClassA}
     */
    private static final ThreadLocal<AvroByteArrayConverter<ClassA>> entity1Converter = new ThreadLocal<AvroByteArrayConverter<ClassA>>() {
        @Override
        protected AvroByteArrayConverter<ClassA> initialValue() {
            return new AvroByteArrayConverter<ClassA>(ClassA.class);
        }
    };

    /**
     * Auto-generated thread-safe entity converter for {@link ClassB}
     */
    private static final ThreadLocal<AvroByteArrayConverter<ClassB>> entity2Converter = new ThreadLocal<AvroByteArrayConverter<ClassB>>() {
        @Override
        protected AvroByteArrayConverter<ClassB> initialValue() {
            return new AvroByteArrayConverter<ClassB>(ClassB.class);
        }
    };

    /**
     * Auto-generated thread-safe entity converter for {@link ClassC}
     */
    private static final ThreadLocal<AvroByteArrayConverter<ClassC>> entity3Converter = new ThreadLocal<AvroByteArrayConverter<ClassC>>() {
        @Override
        protected AvroByteArrayConverter<ClassC> initialValue() {
            return new AvroByteArrayConverter<ClassC>(ClassC.class);
        }
    };

    public Messaging1Plugin() {
        super();
    }

    @Override
    public PluginAdapter getPluginAdapter() {
        return this;
    }

    @Override
    public Messaging1PluginAPI getPluginAPI() {
        return this;
    }

    @Override
    protected void handleEntityMsg(PayloadMessage msg) {
        if (msg.getMethodId() == METHOD_1_ID) {
            handleMethod1Msg(msg);
        } else if (msg.getMethodId() == METHOD_2_ID) {
            handleMethod2Msg(msg);
        } else if (msg.getMethodId() == METHOD_5_ID) {
            handleMethod5Msg(msg);
        } else if (msg.getMethodId() == METHOD_6_ID) {
            handleMethod6Msg(msg);
        } else if (msg.getMethodId() == METHOD_7_ID) {
            handleMethod7Msg(msg);
        }
    }

    @Override
    protected void handleVoidMsg(Message msg) {
        if (msg.getMethodId() == METHOD_4_ID) {
            handleMethod4Void(msg.getUid());
        } else if (msg.getMethodId() == METHOD_3_ID) {
            handleMethod3Void(msg.getUid());
        }
    }

    /*
     * Auto-generated listeners and corresponding setters
     */

    private volatile MethodAListener method1Listener;
    private volatile MethodBListener method2Listener;
    private volatile MethodCListener method3Listener;

    @Override
    public void setMethodAListener(MethodAListener listener) {
        method1Listener = listener;
    }

    @Override
    public void setMethodBListener(MethodBListener listener) {
        method2Listener = listener;
    }

    @Override
    public void setMethodCListener(MethodCListener listener) {
        method3Listener = listener;
    }

    /*
     * Auto-generated message handlers.
     */

    /**
     * ClassA RMI request sample
     */
    private void handleMethod1Msg(final PayloadMessage msg) {
        context.getCallbackExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ClassC requestEntity = entity3Converter.get().fromByteArray(msg.getPayload());
                    LOG.debug("Processing {}", requestEntity);
                    if (method1Listener != null) {
                        ClassA responseEntity = method1Listener.onEvent(requestEntity);
                        if (responseEntity != null) {
                            try {
                                byte[] payload = entity1Converter.get().toByteArray(responseEntity);
                                pushMsg(new EntityMessage(msg.getUid(), payload, METHOD_1_ID));
                            } catch (IOException e) {
                                pushMsg(new ErrorMessage(msg.getUid(), METHOD_1_ID, ErrorCode.SERIALIZATION_ERROR, e.getMessage()));
                            }
                        } else {
                            pushMsg(new ErrorMessage(msg.getUid(), METHOD_1_ID, ErrorCode.NULL_RESPONSE_ERROR));
                        }
                    } else {
                        pushMsg(new ErrorMessage(msg.getUid(), METHOD_1_ID, ErrorCode.NO_LISTENER_ASSIGNED));
                    }
                } catch (Exception e) {
                    LOG.error("Failed to process call from server", e);
                    pushMsg(new ErrorMessage(msg.getUid(), METHOD_1_ID, ErrorCode.EXECUTION_ERROR, e.getMessage()));
                }
            }
        });
    }

    /**
     * ClassA RMI request sample
     */
    private void handleMethod2Msg(final PayloadMessage msg) {
        context.getCallbackExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ClassC requestEntity = entity3Converter.get().fromByteArray(msg.getPayload());
                    LOG.debug("Processing {}", requestEntity);
                    if (method2Listener != null) {
                        ClassB responseEntity = method2Listener.onEvent(requestEntity);
                        if (responseEntity != null) {
                            try {
                                byte[] payload = entity2Converter.get().toByteArray(responseEntity);
                                pushMsg(new EntityMessage(msg.getUid(), payload, METHOD_2_ID));
                            } catch (IOException e) {
                                pushMsg(new ErrorMessage(msg.getUid(), METHOD_2_ID, ErrorCode.SERIALIZATION_ERROR, e.getMessage()));
                            }
                        } else {
                            pushMsg(new ErrorMessage(msg.getUid(), METHOD_2_ID, ErrorCode.NULL_RESPONSE_ERROR));
                        }
                    } else {
                        pushMsg(new ErrorMessage(msg.getUid(), METHOD_2_ID, ErrorCode.NO_LISTENER_ASSIGNED));
                    }
                } catch (Exception e) {
                    LOG.error("Failed to process call from server", e);
                    pushMsg(new ErrorMessage(msg.getUid(), METHOD_2_ID, ErrorCode.EXECUTION_ERROR, e.getMessage()));
                }
            }
        });
    }

    /**
     * Void RMI request sample
     */
    private void handleMethod3Void(final UUID uid) {
        context.getCallbackExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (method3Listener != null) {
                        ClassC responseEntity = method3Listener.onEvent();
                        byte[] payload = entity3Converter.get().toByteArray(responseEntity);
                        pushMsg(new EntityMessage(uid, payload, METHOD_3_ID));
                    } else {
                        pushMsg(new ErrorMessage(uid, METHOD_3_ID, ErrorCode.NO_LISTENER_ASSIGNED));
                    }
                } catch (Exception e) {
                    LOG.error("Failed to process call from server", e);
                    pushMsg(new ErrorMessage(uid, METHOD_3_ID, ErrorCode.EXECUTION_ERROR, e.getMessage()));
                }
            }
        });
    }

    /**
     * Void RMI response sample
     */
    private void handleMethod4Void(UUID uid) {
        @SuppressWarnings("unchecked")
        MessageFuture<Void> future = (MessageFuture<Void>) futureMap.remove(uid);
        if (future != null) {
            future.setValue(null);
        } else {
            LOG.debug("[{}] Received duplicate response", uid);
        }
    }

    private void handleMethod5Msg(PayloadMessage msg) {
        @SuppressWarnings("unchecked")
        MessageFuture<ClassA> future = (MessageFuture<ClassA>) futureMap.remove(msg.getUid());
        if (future != null) {
            try {
                future.setValue(entity1Converter.get().fromByteArray(msg.getPayload()));
            } catch (Exception e) {
                LOG.error("Failed to process response from server", e);
            }
        } else {
            LOG.debug("[{}] Received duplicate response", msg.getUid());
        }
    }

    private void handleMethod6Msg(PayloadMessage msg) {
        @SuppressWarnings("unchecked")
        MessageFuture<ClassB> future = (MessageFuture<ClassB>) futureMap.remove(msg.getUid());
        if (future != null) {
            try {
                future.setValue(entity2Converter.get().fromByteArray(msg.getPayload()));
            } catch (Exception e) {
                LOG.error("Failed to process response from server", e);
            }
        } else {
            LOG.debug("[{}] Received duplicate response", msg.getUid());
        }
    }

    private void handleMethod7Msg(PayloadMessage msg) {
        @SuppressWarnings("unchecked")
        MessageFuture<ClassC> future = (MessageFuture<ClassC>) futureMap.remove(msg.getUid());
        if (future != null) {
            try {
                future.setValue(entity3Converter.get().fromByteArray(msg.getPayload()));
            } catch (Exception e) {
                LOG.error("Failed to process response from server", e);
            }
        } else {
            LOG.debug("[{}] Received duplicate response", msg.getUid());
        }
    }

    /**
     * Void RMI request sample
     */
    @Override
    public Future<Void> sendA(ClassA param) {
        byte[] payload;
        try {
            payload = entity1Converter.get().toByteArray(param);
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode requst parameter", e);
        }
        short methodId = METHOD_4_ID;
        MessageFuture<Void> future = new MessageFuture<Void>(new EntityMessage(payload, methodId), this);
        pushFuture(future);
        return future;
    }

    @Override
    public Future<ClassA> getA() {
        short methodId = METHOD_5_ID;
        MessageFuture<ClassA> future = new MessageFuture<ClassA>(new VoidMessage(methodId), this);
        futureMap.put(future.getUid(), future);
        return future;
    }

    @Override
    public Future<ClassB> getB(ClassA param) {
        byte[] payload;
        try {
            payload = entity1Converter.get().toByteArray(param);
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode requst parameter", e);
        }
        short methodId = METHOD_6_ID;
        MessageFuture<ClassB> future = new MessageFuture<ClassB>(new EntityMessage(payload, methodId), this);
        futureMap.put(future.getUid(), future);
        return future;
    }

    @Override
    public Future<ClassC> getC(ClassA param) {
        byte[] payload;
        try {
            payload = entity1Converter.get().toByteArray(param);
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode requst parameter", e);
        }
        short methodId = METHOD_7_ID;
        MessageFuture<ClassC> future = new MessageFuture<ClassC>(new EntityMessage(payload, methodId), this);
        futureMap.put(future.getUid(), future);
        return future;
    }
}
