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

import java.nio.ByteBuffer;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.KaaTcpProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Basic Mqtt message.
 * Fixed header format
 * bit     7   6   5   4      3          2   1       0
 * byte 1  Message Type    Dup flag    QoS level   RETAIN
 * byte 2           Remaining length
 * @author Andrey Panasenko
 *
 */
abstract public class MqttFrame {

    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(MqttFrame.class);

    public static final int MQTT_FIXED_HEADER_LEGTH = 2;

    protected enum FrameParsingState {
        NONE,
        PROCESSING_LENGTH,
        PROCESSING_PAYLOAD,
    }

    /*
     * If adding any filed, don't forget to update MqttFrame(MqttFrame old) 
     * to clone all fileds.
     */
    private MessageType messageType;
    protected ByteBuffer buffer;
    protected boolean frameDecodeComplete = false;
    protected int remainingLength = 0;
    protected int multiplier = 1;
    protected FrameParsingState currentState = FrameParsingState.NONE;

    /**
     * @return the messageType
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * @param messageType the messageType to set
     */
    protected void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     *
     */
    protected MqttFrame() {

    }


    /**
     * @param old te old
     */
    protected MqttFrame(MqttFrame old) {
        this.messageType = old.getMessageType();
        this.buffer = old.getBuffer();
        this.frameDecodeComplete = old.frameDecodeComplete;
        this.remainingLength = old.remainingLength;
        this.multiplier = old.multiplier;
        this.currentState = old.currentState;
    }

    /**
     * Return mqtt Frame.
     * @return ByteBuffer mqtt frame
     */
    public ByteBuffer getFrame() {
        if (buffer == null) {
            int remainingLegth = getRemainingLegth();
            byte [] kaaTcpHeader = new byte[6];
            int headerSize = fillFixedHeader(remainingLegth, kaaTcpHeader);
            int remainingSize = remainingLegth + headerSize;
            LOG.trace("Allocating buffer size = {}", remainingSize);
            buffer = ByteBuffer.allocate(remainingSize);
            buffer.put(kaaTcpHeader, 0, headerSize);
            pack();
            buffer.position(0);
        }
        return buffer;
    }

    /**
     * Pack message into mqtt frame
     */
    abstract protected void pack();

    /**
     * Return remaining length of mqtt frame, necessary for ByteBuffer size calculation
     * @return remaining length of mqtt frame
     */
    protected int getRemainingLegth() {
        return remainingLength;
    }

    /**
     * Decode message from mqttFrame ByteBuffer
     * @throws KaaTcpProtocolException the kaa tcp protocol exception
     */
    abstract protected void decode() throws KaaTcpProtocolException;
    
    /**
     * Check if this Mqtt frame should be last frame on connection and connection should be closed.
     * @return boolean 'true' if connection should be closed after frame transmition.
     */
    abstract public boolean isNeedCloseConnection();

    /**
     * Fill mqtt frame fixed header
     * @param   remainingLegth  the remaining legth
     * @param   dst             the dst
     * @return  number of packet bytes
     */
    private int fillFixedHeader(int remainingLegth, byte [] dst) {
        int size = 1;
        byte byte1 = getMessageType().getType();
        byte1 = (byte) (byte1 & (byte) 0x0F);
        byte1 = (byte) (byte1 << 4);
        dst[0] = byte1;
        byte digit = 0x00;
        do {
            digit = (byte) (remainingLegth % 0x00000080);
            remainingLegth /= 0x00000080;
            // if there are more digits to encode, set the top bit of this digit
            if (remainingLegth > 0) {
                digit = (byte) (digit | 0x80);
            }
            dst[size] = digit;
            ++size;
        } while ( remainingLegth > 0 );
        return size;
    }

    protected ByteBuffer getBuffer() {
        return buffer;
    }

    private void onFrameDone() throws KaaTcpProtocolException {
        LOG.trace("Frame ({}): payload processed", getMessageType());
        if (buffer != null) {
            buffer.position(0);
        }
        decode();
        frameDecodeComplete = true;
    }

    private void processByte(byte b) throws KaaTcpProtocolException {
        if (currentState.equals(FrameParsingState.PROCESSING_LENGTH)) {
            remainingLength += ((b & 0xFF) & 127) * multiplier;
            multiplier *= 128;
            if (((b & 0xFF) & 128) == 0) {
                LOG.trace("Frame ({}): payload length = {}", getMessageType(), remainingLength);
                if (remainingLength != 0) {
                    buffer = ByteBuffer.allocate(remainingLength);
                    currentState = FrameParsingState.PROCESSING_PAYLOAD;
                } else {
                    onFrameDone();
                }
            }
        }
    }

    /**
     * Push bytes of frame
     * @param   bytes       the bytes array
     * @param   position    the position in buffer
     * @return  int used bytes from buffer
     * @throws  KaaTcpProtocolException the kaa tcp protocol exception
     */
    public int push(byte[] bytes, int position) throws KaaTcpProtocolException {
        int pos = position;
        if (currentState.equals(FrameParsingState.NONE)) {
            remainingLength = 0;
            currentState = FrameParsingState.PROCESSING_LENGTH;
        }
        while (pos < bytes.length && !frameDecodeComplete) {
            if (currentState.equals(FrameParsingState.PROCESSING_PAYLOAD)) {
                int bytesToCopy = (remainingLength > bytes.length - pos) ? bytes.length - pos : remainingLength;
                buffer.put(bytes, pos, bytesToCopy);
                pos += bytesToCopy;
                remainingLength -= bytesToCopy;
                LOG.trace("Frame ({}): copied {} bytes of payload. {} bytes left", getMessageType(), bytesToCopy, remainingLength);
                if (remainingLength == 0) {
                    onFrameDone();
                }
            } else {
                processByte(bytes[pos]);
                ++pos;
            }
        }
        return pos - position;
    }

    /**
     * Test if Mqtt frame decode complete
     * @return boolean 'true' if decode complete
     */
    public boolean decodeComplete() {
        return frameDecodeComplete;
    }
    
    /**
     * Used in case if Frame Class should be changed during frame decode,
     * Used for migrate from KaaSync() general frame to specific classes like Sync, Bootstrap.
     * Default implementation is to return this. 
     * @return new MqttFrame as specific class.
     * @throws KaaTcpProtocolException the kaa tcp protocol exception
     */
    public MqttFrame upgradeFrame() throws KaaTcpProtocolException {
        return this;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MqttFrame [messageType=");
        builder.append(messageType);
        builder.append(", currentState=");
        builder.append(currentState);
        builder.append("]");
        return builder.toString();
    }
}
