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

/**
 * ConnAck message Class.
 * The CONNACK message is a message sent by the server in response to a CONNECT request from a client.
 * Variable header
 * byte 1  reserved (0)
 * byte 2 Return Code see enum ReturnCode
 * @author Andrey Panasenko
 *
 */
public class ConnAck extends MqttFrame {

    public static final int CONNACK_REMAINING_LEGTH_V1 = 2;

    /**
     * CONNACK return code enum
     *  ACCEPTED                        0x01    Connection Accepted
     *  REFUSE_BAD_PROTOCOL             0x02    Connection Refused: unacceptable protocol version
     *  REFUSE_ID_REJECT                0x03    Connection Refused: identifier rejected
     *  REFUSE_SERVER_UNAVAILABLE       0x04    Connection Refused: server unavailable
     *  REFUSE_BAD_CREDENTIALS          0x05    Connection Refused: invalid authentication parameters
     *  REFUSE_NO_AUTH                  0x06    Connection Refused: not authorized
     *  REFUSE_VERIFICATION_FAILED      0x10    Connection Refused: endpoint verification failed
     */
    public enum ReturnCode {
        ACCEPTED((byte)0x01),
        REFUSE_BAD_PROTOCOL((byte)0x02),
        REFUSE_ID_REJECT((byte)0x03),
        REFUSE_SERVER_UNAVAILABLE((byte)0x04),
        REFUSE_BAD_CREDENTIALS((byte)0x05),
        REFUSE_NO_AUTH((byte)0x06),
        REFUSE_VERIFICATION_FAILED((byte) 0x10),
        UNDEFINED((byte)0x07);

        private byte returnCode;

        private ReturnCode(byte code) {
            this.returnCode = code;
        }

        /**
         * Return byte representation of ConnAck return code
         * @return byte returnCode
         */
        public byte getReturnCode() {
            return returnCode;
        }

    }

    private ReturnCode returnCode;

    /**
     * Default constructor.
     * @param returnCode the return code
     */
    public ConnAck(ReturnCode returnCode) {
        setMessageType(MessageType.CONNACK);
        this.setReturnCode(returnCode);
        this.remainingLength = CONNACK_REMAINING_LEGTH_V1;
    }

    /**
     *
     */
    public ConnAck() {
        super();
        setReturnCode(ReturnCode.ACCEPTED);
        setMessageType(MessageType.CONNACK);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame#pack(int)
     */
    @Override
    protected void pack() {
        buffer.put((byte) 0);
        buffer.put(returnCode.getReturnCode());
    }

    /**
     * Return ConnAck return code
     * @return byte returnCode
     */
    public ReturnCode getReturnCode() {
        return returnCode;
    }

    /**
     * Set ConnAck return code
     * @param returnCode - ReturnCode
     */
    public void setReturnCode(ReturnCode returnCode) {
        this.returnCode = returnCode;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame#decode(int)
     */
    @Override
    protected void decode() {
        byte code = buffer.get(1);
        if (code == ReturnCode.ACCEPTED.getReturnCode()) {
            returnCode = ReturnCode.ACCEPTED;
        } else if(code == ReturnCode.REFUSE_BAD_CREDENTIALS.getReturnCode()) {
            returnCode = ReturnCode.REFUSE_BAD_CREDENTIALS;
        } else if(code == ReturnCode.REFUSE_BAD_PROTOCOL.getReturnCode()) {
            returnCode = ReturnCode.REFUSE_BAD_PROTOCOL;
        } else if(code == ReturnCode.REFUSE_ID_REJECT.getReturnCode()) {
            returnCode = ReturnCode.REFUSE_ID_REJECT;
        } else if(code == ReturnCode.REFUSE_NO_AUTH.getReturnCode()) {
            returnCode = ReturnCode.REFUSE_NO_AUTH;
        } else if(code == ReturnCode.REFUSE_SERVER_UNAVAILABLE.getReturnCode()) {
            returnCode = ReturnCode.REFUSE_SERVER_UNAVAILABLE;
        } else if(code == ReturnCode.REFUSE_VERIFICATION_FAILED.getReturnCode()) {
            returnCode = ReturnCode.REFUSE_VERIFICATION_FAILED;
        } else {
            returnCode = ReturnCode.UNDEFINED;
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame#isNeedCloseConnection()
     */
    @Override
    public boolean isNeedCloseConnection() {
        return false;
    }

}
