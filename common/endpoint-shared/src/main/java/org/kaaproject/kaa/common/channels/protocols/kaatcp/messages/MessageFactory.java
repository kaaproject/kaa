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

package org.kaaproject.kaa.common.channels.protocols.kaatcp.messages;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.Framer;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.MqttFramelistener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.ConnAckListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.ConnectListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.DisconnectListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.PingRequestListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.PingResponseListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.SyncRequestListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.SyncResponseListener;

/**
 * MessageFactory Class. Used to transform byte stream to specific protocol
 * messages. Typical use:
 * 
 * MessageFactory factory = new MessageFactory();
 * factory.registerMessageListener(listener);
 * factory.getFramer().pushBytes(bytes);
 * 
 * Where listener instance of class which implements one of protocol message
 * listeners. and bytes - byte[] stream received from TCP/IP.
 * 
 * @author Andrey Panasenko
 *
 */
public class MessageFactory implements MqttFramelistener {

    private Framer framer;
    private ConnAckListener connAckListener;
    private ConnectListener connectListener;
    private DisconnectListener disconnectListener;
    private PingRequestListener pingRequestListener;
    private PingResponseListener pingResponseListener;
    private SyncRequestListener syncRequestListener;
    private SyncResponseListener syncResponseListener;

    /**
     * Constructor with externally created Framer.
     * 
     * @param framer
     *            Framer
     */
    public MessageFactory(Framer framer) {
        this.setFramer(framer);
        framer.registerFrameListener(this);
    }

    /**
     * Default Constructor.
     */
    public MessageFactory() {
        this.setFramer(new Framer());
        framer.registerFrameListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.common.channels.protocols.kaatcp.MqttFramelistener
     * #onMqttFrame
     * (org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame)
     */
    @Override
    public void onMqttFrame(MqttFrame frame) {
        switch (frame.getMessageType()) {
        case CONNACK:
            if (connAckListener != null) {
                connAckListener.onMessage((ConnAck) frame);
            }
            break;
        case CONNECT:
            if (connectListener != null) {
                connectListener.onMessage((Connect) frame);
            }
            break;
        case DISCONNECT:
            if (disconnectListener != null) {
                disconnectListener.onMessage((Disconnect) frame);
            }
            break;
        case KAASYNC:

            onKaaSyncMessage((KaaSync) frame);

            break;
        case PINGREQ:
            if (pingRequestListener != null) {
                pingRequestListener.onMessage((PingRequest) frame);
            }
            break;
        case PINGRESP:
            if (pingResponseListener != null) {
                pingResponseListener.onMessage((PingResponse) frame);
            }
            break;
        default:
            break;
        }

    }

    /**
     * Private message processor in case of Mqtt frame is KaaSync Message Type
     * 
     * @param frame
     *            KaaSync object
     */
    private void onKaaSyncMessage(KaaSync frame) {
        KaaSyncMessageType type = frame.getKaaSyncMessageType();
        switch (type) {
        case SYNC:
            if (frame.isRequest()) {
                if (syncRequestListener != null) {
                    syncRequestListener.onMessage((SyncRequest) frame);
                }
            } else {
                if (syncResponseListener != null) {
                    syncResponseListener.onMessage((SyncResponse) frame);
                }
            }
            break;
        case UNUSED:
            break;
        default:
            break;
        }
    }

    /**
     * Register ConnAck message listener.
     * 
     * @param listener
     *            ConnAckListener
     */
    public void registerMessageListener(ConnAckListener listener) {
        connAckListener = listener;
    }

    /**
     * Register Connect message listener.
     * 
     * @param listener
     *            ConnectListener
     */
    public void registerMessageListener(ConnectListener listener) {
        connectListener = listener;
    }

    /**
     * Register Disconnect message listener.
     * 
     * @param listener
     *            DisconnectListener
     */
    public void registerMessageListener(DisconnectListener listener) {
        disconnectListener = listener;
    }

    /**
     * Register PingRequest message listener.
     * 
     * @param listener
     *            PingRequestListener
     */
    public void registerMessageListener(PingRequestListener listener) {
        pingRequestListener = listener;
    }

    /**
     * Register PingResponse message listener.
     * 
     * @param listener
     *            PingResponseListener
     */
    public void registerMessageListener(PingResponseListener listener) {
        pingResponseListener = listener;
    }

    /**
     * Register SyncRequest message listener.
     * 
     * @param syncRequestListener
     *            SyncRequestListener
     */
    public void registerMessageListener(SyncRequestListener syncRequestListener) {
        this.syncRequestListener = syncRequestListener;
    }

    /**
     * Register SyncResponse message listener.
     * 
     * @param syncResponseListener
     *            SyncResponseListener
     */
    public void registerMessageListener(SyncResponseListener syncResponseListener) {
        this.syncResponseListener = syncResponseListener;
    }

    /**
     * Framer getter.
     * 
     * @return Framer
     */
    public Framer getFramer() {
        return framer;
    }

    /**
     * Framer setter.
     * 
     * @param framer
     *            Framer
     */
    public void setFramer(Framer framer) {
        this.framer = framer;
    }
}
