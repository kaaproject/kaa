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
 * KaaSync message Class.
 * The KAASYNC message is used as intermediate class for decoding messages 
 * SyncRequest,SyncResponse,BootstraResolve,BootstrapResponse.
 * 
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
 *         Unused(bit 3)
 *         
 *         bit4-bit7 - KAASYNC subcomand id
 *         
 * 
 * KAASYNC subcomand id table
 * Mnemonic   Enumeration   Description
 * UNUSED     0             reserved value
 * SYNC       1             Sync request/response
 * BOOTSTRAP  2             Bootstrap resolve/response
 * 
 */
public class KaaSync extends MqttFrame {

    public static final int KAASYNC_VERIABLE_HEADER_LENGTH_V1 = 12;

    public static final byte KAASYNC_REQUEST_FLAG = 0x01;
    public static final byte KAASYNC_ZIPPED_FLAG = 0x02;
    public static final byte KAASYNC_ENCRYPTED_FLAG = 0x04;
    private static final int KAASYNC_MESSAGE_TYPE_SHIFT = 4;
    public static final byte KAASYNC_VERSION = 0x01;
    private static final byte[] FIXED_HEADER_CONST = new byte[]{0x00,0x06,'K','a','a','t','c','p',
                                                                KAASYNC_VERSION};
    

    /** message id if used, default 0 */
    private int messageId = 0;

    /** Request/Response (bit 0) 1 - request, 0 - response */
    private boolean request = false;

    /** Zipped (bit 1) 1 - zepped, 0 - unzipped */
    private boolean zipped = false;

    /** Encrypted(bit 2) 1 - encrypted, 0 - unencrypted */
    private boolean encrypted = false;

    /** KaaSync subcommand message type */
    private KaaSyncMessageType kaaSyncMessageType = KaaSyncMessageType.UNUSED;

    /**
     * Default constructor.
     * @param isRequest boolean 'true' is request, else response
     * @param isZipped boolean if message is Zipped
     * @param isEcrypted boolean if message is Encrypted
     */
    public KaaSync(boolean isRequest, boolean isZipped, boolean isEcrypted) {
        setMessageType(MessageType.KAASYNC);
        setRequest(isRequest);
        setZipped(isZipped);
        setEncrypted(isEcrypted);
        remainingLength = KAASYNC_VERIABLE_HEADER_LENGTH_V1;
    }

    /**
     * Constructor used to migrate from KaaSync to specific message class.
     * @param old KaaSync object from which need to migrate to specific class.
     */
    protected KaaSync(KaaSync old) {
        super((MqttFrame)old);
        this.messageId = old.getMessageId();
        this.request = old.isRequest();
        this.zipped = old.isZipped();
        this.encrypted = old.isEncrypted();
        this.kaaSyncMessageType = old.getKaaSyncMessageType();
    }

    /**
     * Default Constructor is used for create message class from byte stream.
     */
    public KaaSync() {
        super();
        setMessageType(MessageType.KAASYNC);
    }

    

    /**
     * Pack KaaSync variable header
     */
    protected void packVeriableHeader() {
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
        flags = (byte) (flags | (getKaaSyncMessageType().getType() << KAASYNC_MESSAGE_TYPE_SHIFT));
        buffer.put( flags);
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
     * @param messageId int
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
     * @param request boolean isRequest
     */
    public void setRequest(boolean request) {
        this.request = request;
    }

    /**
     * Set to 'true' if avro object is zipped.
     * @param zipped boolean isZipped
     */
    public void setZipped(boolean zipped) {
        this.zipped = zipped;
    }

    /**
     * Set to 'true' if avro object is encrypted.
     * @param encrypted boolean isEncrypted
     */
    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }


    /**
     * Decode KaaSync variable header
     * @throws KaaTcpProtocolException - if protocol version missmatch
     */
    protected void decodeVariableHeader() throws KaaTcpProtocolException {
        for (int i = 0; i < FIXED_HEADER_CONST.length; i++) {
            if(FIXED_HEADER_CONST[i] != buffer.get()) {
                throw new KaaTcpProtocolException("Kaatcp protocol version missmatch");
            }
        }
        int msb = (buffer.get() & 0xFF) << 8;
        int lsb = buffer.get() & 0xFF;
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
        byte kaaSyncType = (byte)((flag >> KAASYNC_MESSAGE_TYPE_SHIFT) & 0x0F);
        if (kaaSyncType == KaaSyncMessageType.SYNC.getType()) {
            kaaSyncMessageType = KaaSyncMessageType.SYNC;
        } else {
            kaaSyncMessageType = KaaSyncMessageType.UNUSED;
        }
    }


    /**
     * Kaa Sync Message Type getter.
     * @return KaaSyncMessageType
     */
    public KaaSyncMessageType getKaaSyncMessageType() {
        return kaaSyncMessageType ;
    }

    /**
     * Kaa Sync Message Type setter.
     * @param type KaaSyncMessageType
     */
    protected void setKaaSyncMessageType(KaaSyncMessageType type) {
        this.kaaSyncMessageType = type;
    }


    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame#pack()
     */
    @Override
    protected void pack() {
        packVeriableHeader();
    }


    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame#decode()
     */
    @Override
    protected void decode() throws KaaTcpProtocolException {
        decodeVariableHeader();
    }
    
    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame#upgradeFrame()
     */
    @Override
    public MqttFrame upgradeFrame() throws KaaTcpProtocolException {
        switch (getKaaSyncMessageType()) {
        case SYNC:
            if (isRequest()) {
                return new SyncRequest(this);
            } else {
                return new SyncResponse(this);
            }
        case UNUSED:
            throw new KaaTcpProtocolException("KaaSync Message type is incorrect");
        default:
            break;
        }
        throw new KaaTcpProtocolException("KaaSync Message type is incorrect");
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame#isNeedCloseConnection()
     */
    @Override
    public boolean isNeedCloseConnection() {
        return false;
    }
}
