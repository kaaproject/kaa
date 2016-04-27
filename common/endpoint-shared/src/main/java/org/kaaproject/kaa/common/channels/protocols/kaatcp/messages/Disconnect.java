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

import org.kaaproject.kaa.common.channels.protocols.kaatcp.KaaTcpProtocolException;

/**
 * Disconnect message Class.
 * The DISCONNECT message is sent from the client to the server to indicate that it is about to close
 * its TCP/IP connection. This provides a clean disconnection, rather than just dropping the line.
 * If the client had connected with the clean session flag set,
 * then all previously maintained information about the client will be discarded.
 * A server should not rely on the client to close the TCP/IP connection after receiving a DISCONNECT.
 *
 * @author Andrey Panasenko
 *
 */
public class Disconnect extends MqttFrame {

    public static final int DISCONNECT_REMAINING_LEGTH_V1 = 2;

    /**
     *  DISCONNECT reason
     *  NONE                        0x00    No error
     *  BAD_REQUEST                 0x01    Client sent a corrupted data
     *  INTERNAL_ERROR              0x02    Internal error has been occurred
     */
    public enum DisconnectReason {
        NONE((byte)0x00),
        BAD_REQUEST((byte)0x01),
        INTERNAL_ERROR((byte)0x02),
        CREDENTIALS_REVOKED((byte)0x03);

        private byte reason;

        private DisconnectReason(byte code) {
            this.reason = code;
        }

        /**
         * Return byte representation of ConnAck return code
         * @return byte returnCode
         */
        public byte getReason() {
            return reason;
        }

    }

    private DisconnectReason reason;

    /**
     * Default constructor.
     * @param reason the reason
     */
    public Disconnect(DisconnectReason reason) {
        setMessageType(MessageType.DISCONNECT);
        setReason(reason);
        remainingLength = DISCONNECT_REMAINING_LEGTH_V1;
    }

    /**
     * Default Constructor.
     */
    public Disconnect() {
        setMessageType(MessageType.DISCONNECT);
    }

    /**
     * Return Disconnect reason
     * @return byte reason
     */
    public DisconnectReason getReason() {
        return reason;
    }

    /**
     * Set Disconnect reason
     * @param reason DisconnectReason
     */
    public void setReason(DisconnectReason reason) {
        this.reason = reason;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame#pack(int)
     */
    @Override
    protected void pack() {
        buffer.put((byte) 0);
        buffer.put(reason.getReason());
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame#decode()
     */
    @Override
    protected void decode() throws KaaTcpProtocolException {
        byte code = buffer.get(1);
        for(DisconnectReason tmp : DisconnectReason.values()){
            if(code == tmp.getReason()){
                reason = tmp;
                return;
            }
        }
        throw new KaaTcpProtocolException("Unknown disconnect reason!");
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame#isNeedCloseConnection()
     */
    @Override
    public boolean isNeedCloseConnection() {
        return true;
    }

}
