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
package org.kaaproject.kaa.common.channels.protocols.kaatcp.messages;


import org.kaaproject.kaa.common.channels.protocols.kaatcp.Framer;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.MqttFramelistener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.ConnAckListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.ConnectListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.DisconnectListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.KaaSyncListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.PingRequestListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.PingResponseListener;

/**
 * @author Andrey Panasenko
 *
 */
public class MessageFactory implements MqttFramelistener {

    private Framer framer;
    private ConnAckListener connAckListener;
    private ConnectListener connectListener;
    private DisconnectListener disconnectListener;
    private KaaSyncListener kaaSyncListener;
    private PingRequestListener pingRequestListener;
    private PingResponseListener pingResponseListener;
    /**
     * 
     */
    public MessageFactory(Framer framer) {
        this.setFramer(framer);
        framer.registerFrameListener(this);
    }
    
    public MessageFactory() {
        this.setFramer(new Framer());
        framer.registerFrameListener(this);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.MqttFramelistener#onMqttFrame(org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame)
     */
    @Override
    public void onMqttFrame(MqttFrame frame) {
        switch (frame.getMessageType()) {
        case CONNACK:
            if (connAckListener != null) {
                connAckListener.onMessage((ConnAck)frame);
            }
            break;
        case CONNECT:
            if (connectListener != null) {
                connectListener.onMessage((Connect)frame);
            }
            break;
        case DISCONNECT:
            if (disconnectListener != null) {
                disconnectListener.onMessage((Disconnect)frame);
            }
            break;
        case KAASYNC:
            if (kaaSyncListener != null) {
                kaaSyncListener.onMessage((KaaSync)frame);
            }
            break;
        case PINGREQ:
            if (pingRequestListener != null) {
                pingRequestListener.onMessage((PingRequest)frame);
            }
            break;
        case PINGRESP:
            if (pingResponseListener != null) {
                pingResponseListener.onMessage((PingResponse)frame);
            }
            break;
        default:
            break;
        }

    }

    public void registerMessageListener(ConnAckListener listener) {
        connAckListener = listener;
    }
    
    public void registerMessageListener(ConnectListener listener) {
        connectListener = listener;
    }
    
    public void registerMessageListener(DisconnectListener listener) {
        disconnectListener = listener;
    }
    
    public void registerMessageListener(KaaSyncListener listener) {
        kaaSyncListener = listener;
    }
    
    public void registerMessageListener(PingRequestListener listener) {
        pingRequestListener = listener;
    }
    
    public void registerMessageListener(PingResponseListener listener) {
        pingResponseListener = listener;
    }

    /**
     * @return the framer
     */
    public Framer getFramer() {
        return framer;
    }

    /**
     * @param framer the framer to set
     */
    public void setFramer(Framer framer) {
        this.framer = framer;
    }
}
