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

import org.kaaproject.kaa.common.channels.protocols.kaatcp.KaaTcpProtocolException;

/**
 * KaaSync message Class.
 * The KAASYNC message is used to send an SyncRequest Avro object from the Endpoint
 * to the Operations Server and an SyncResponse Avro object from the Operations Server to the  Endpoint.
 *
 *
 * @author Andrey Panasenko
 *
 * Variable header
 * Protocol Name
 *     byte 1  Length MSB (0)
 *     byte 2  Length LSB (6)
 *     byte 3  K
 *     byte 4  a
 *     byte 5  a
 *     byte 6  t
 *     byte 7  c
 *     byte 8  p
 * Protocol version
 *     byte 9  Version (1)
 * Message ID (2 bytes)
 *     byte 10 ID MSB
 *     byte 11 ID LSB
 * Flags
 *     byte 12
 *         Request/Response (bit 0)
 *         1 - request, 0 - response
 *
 *         Zipped (bit 1)
 *         1 - zepped, 0 - unzipped
 *
 *         Encrypted(bit 2)
 *         1 - encrypted, 0 - unencrypted
 *
 */
public class KaaSync extends MqttFrame {

    public static final int KAASYNC_VERIABLE_HEADER_LENGTH_V1 = 12;

    public static final byte KAASYNC_REQUEST_FLAG = 0x01;
    public static final byte KAASYNC_ZIPPED_FLAG = 0x02;
    public static final byte KAASYNC_ENCRYPTED_FLAG = 0x04;

    public static final byte KAASYNC_VERSION = 0x01;
    private static final byte[] FIXED_HEADER_CONST = new byte[]{0x00,0x06,'K','a','a','t','c','p',
                                                                KAASYNC_VERSION};
    /** Avro object byte representation*/
    private byte[] avroObject;

    /** message id if used, default 0 */
    private int messageId = 0;

    /** Request/Response (bit 0) 1 - request, 0 - response */
    private boolean request = false;

    /** Zipped (bit 1) 1 - zepped, 0 - unzipped */
    private boolean zipped = false;

    /** Encrypted(bit 2) 1 - encrypted, 0 - unencrypted */
    private boolean encrypted = false;

    /**
     * Default constructor.
     * @param boolean is SyncReques, else SyncResponse
     * @param byte[] avroObject
     * @param boolean is Zipped
     * @param boolean is Encrypted
     */
    public KaaSync(boolean isRequest, byte[] avroObject, boolean isZipped, boolean isEcrypted) {
        setMessageType(MessageType.KAASYNC);
        setAvroObject(avroObject);
        setRequest(isRequest);
        setZipped(isZipped);
        setEncrypted(isEcrypted);
        remainingLength = KAASYNC_VERIABLE_HEADER_LENGTH_V1 + getAvroObject().length;
    }


    /**
     * Default Constructor is used for create message class from byte stream.
     */
    public KaaSync() {
        super();
        setMessageType(MessageType.KAASYNC);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame#pack(int)
     */
    @Override
    protected void pack() {
        packVeriableHeader();
        buffer.put(getAvroObject());
    }

    /**
     * Pack KaaSync variable header
     * @param pos start position in buffer
     * @return number of packed bytes
     */
    private void packVeriableHeader() {
        buffer.put(FIXED_HEADER_CONST);
        byte mId1 = (byte) (messageId & 0x0000FF00);
        buffer.put(mId1);
        byte mId2 = (byte) (messageId & 0x000000FF);
        buffer.put(mId2);

        byte flags = 0x00;
        if (isRequest()) {
            flags = (byte) (flags | KAASYNC_REQUEST_FLAG);
        }
        if (isZipped()) {
            flags = (byte) (flags | KAASYNC_ZIPPED_FLAG);
        }
        if (isEncrypted()) {
            flags = (byte) (flags | KAASYNC_ENCRYPTED_FLAG);
        }
        buffer.put( flags);
    }

    /**
     * Avro Object (SyncRequest/SyncResponse) getter.
     * @return byte[] avroObject
     */
    public byte[] getAvroObject() {
        return avroObject;
    }

    /**
     * Avro Object (SyncRequest/SyncResponse) setter.
     * @param byte[] avroObject
     */
    public void setAvroObject(byte[] avroObject) {
        this.avroObject = avroObject;
    }

    /**
     * Message ID getter.
     * @return int messageId
     */
    public int getMessageId() {
        return messageId;
    }

    /**
     * Message ID setter.
     * @param int messageId
     */
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    /**
     * Is avro object is SyncRequest
     * @return boolean request
     */
    public boolean isRequest() {
        return request;
    }

    /**
     * Is avro object zipped.
     * @return boolean zipped
     */
    public boolean isZipped() {
        return zipped;
    }

    /**
     * Is avro object is encrypted.
     * @return boolean encrypted
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Set to 'true' if avro object is SyncRequest.
     * @param boolean isRequest
     */
    public void setRequest(boolean request) {
        this.request = request;
    }

    /**
     * Set to 'true' if avro object is zipped.
     * @param boolean isZipped
     */
    public void setZipped(boolean zipped) {
        this.zipped = zipped;
    }

    /**
     * Set to 'true' if avro object is encrypted.
     * @param boolean isEncrypted
     */
    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }


    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame#decode()
     */
    @Override
    protected void decode() throws KaaTcpProtocolException {
        decodeVariableHeader();
        decodeAvroObject();
    }


    /**
     * Decode Avro Object
     */
    private void decodeAvroObject() {
        int avroObjectSize = buffer.capacity() - buffer.position();
        if (avroObjectSize > 0) {
            avroObject = new byte[avroObjectSize];
            buffer.get(avroObject);
        }
    }


    /**
     * Decode KaaSync variable header
     * @throws KaaTcpProtocolException - if protocol version missmatch
     */
    private void decodeVariableHeader() throws KaaTcpProtocolException {
        for (int i = 0; i < FIXED_HEADER_CONST.length; i++) {
            if(FIXED_HEADER_CONST[i] != buffer.get()) {
                throw new KaaTcpProtocolException("Kaatcp protocol version missmatch");
            }
        }
        int msb = ((buffer.get() & 0xFF) << 8);
        int lsb = (buffer.get() & 0xFF);
        messageId = (msb | lsb);
        byte flag = buffer.get();
        if (((flag & 0xFF) & KAASYNC_REQUEST_FLAG) != 0) {
            request = true;
        } else {
            request = false;
        }
        if (((flag & 0xFF) & KAASYNC_ZIPPED_FLAG) != 0) {
            zipped = true;
        } else {
            zipped = false;
        }
        if (((flag & 0xFF) & KAASYNC_ENCRYPTED_FLAG) != 0) {
            encrypted = true;
        } else {
            encrypted = false;
        }
    }

}
