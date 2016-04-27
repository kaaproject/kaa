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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Connect message Class.
 * When a TCP/IP socket connection is established from a client to a server,
 * a protocol level session must be created using a CONNECT flow.
 * Variable header
 * Protocol Name
 *    byte 1  Length MSB (0)
 *    byte 2  Length LSB (6)
 *    byte 3  K
 *    byte 4  a
 *    byte 5  a
 *    byte 6  t
 *    byte 7  c
 *    byte 8  p
 * Protocol version
 *    byte 9  Version (1)
 * Connect Flags
 *    byte 10
 *            User name flag (0)
 *            Password flag (0)
 *            Will RETAIN (0)
 *            Will QoS (00)
 *            Will flag (0)
 *            Clean Session (1)
 *                0x02 - value
 * Keep Alive timer
 *    byte 11 Keep alive MSB (0)
 *    byte 12 Keep alive LSB (200)
 *            Keep Alive timer - default value 200 seconds.
 *
 * Payload:
 *  Session Key:   AES Session encoding key (16 byte) - encrypted with the Operations server RSA Public Key
 *  EndpointPublicKeyHash: SHA Hash of Endpoint Public Key (32 byte)
 *  Signature: RSA signature (32 byte) signed with the Endpoint Private Key of Session key (16 byte) + EndpointPublicKeyHash (32 byte)
 *
 * @author Andrey Panasenko
 *
 */
public class Connect extends MqttFrame {

    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(Connect.class);

    public static final int CONNECT_VERIABLE_HEADER_LENGTH_V1 = 18;
    public static final int CONNECT_AES_SESSION_KEY_LENGTH = 256;
    public static final int CONNECT_SIGNATURE_LENGTH = 256;
    public static final byte CONNECT_VERSION = 0x01;
    public static final byte CONNECT_FIXED_HEADER_FLAG = 0x02;
    public static final byte CONNECT_SESSION_KEY_FLAGS = 0x11;
    public static final byte CONNECT_SIGNATURE_FLAGS = 0x01;
    private static final byte[] FIXED_HEADER_CONST = new byte[]{0x00,0x06,'K','a','a','t','c','p',
                                                                CONNECT_VERSION,
                                                                CONNECT_FIXED_HEADER_FLAG};

    /** kaatcp keep alive interval, default 200 seconds. */
    private int keepAlive = 200;

    /** The next protocol identifier. */
    private int nextProtocolId;

    /** AES session key */
    private byte[] aesSessionKey;

    /** Signature of aesSessionKey and endpointPublicKeyHash */
    private byte[] signature;

    /** SyncRequest in Connect message */
    private byte[] syncRequest;

    private boolean hasSignature = false;

    private boolean hasAesSessionKey = false;

    /**
     * Default Constructor
     * @param keepAlive         the keep alive in seconds, max value 65535 seconds.
     * @param nextProtocolId    the next protocol id
     * @param aesSessionKey     the byte[] of AES session key, length 16 byte.
     * @param syncRequest       the byte[] of Avro SyncRequest object
     * @param signature         the byte[] of Signature of aesSessionKey and endpointPublicKeyHash, length 32 byte.
     */
    public Connect(int keepAlive, int nextProtocolId, byte[] aesSessionKey, byte[] syncRequest, byte[] signature) {
        setMessageType(MessageType.CONNECT);
        this.setKeepAlive(keepAlive);
        this.setNextProtocolId(nextProtocolId);
        this.setAesSessionKey(aesSessionKey);
        this.setSyncRequest(syncRequest);
        this.setSignature(signature);
        remainingLength = CONNECT_VERIABLE_HEADER_LENGTH_V1;
        if (aesSessionKey != null) {
            remainingLength += CONNECT_AES_SESSION_KEY_LENGTH;
        }
        if (signature != null) {
            remainingLength += CONNECT_SIGNATURE_LENGTH;
        }
        if (syncRequest != null) {
            remainingLength += syncRequest.length;
        }
        LOG.debug("Created Connect message: session key size = {}, signature size = {}, sync request size = {}",
                aesSessionKey != null ? aesSessionKey.length : "null",
                signature != null ? signature.length : "null",
                syncRequest != null ? syncRequest.length : "null");
    }


    /**
     *
     */
    public Connect() {
        super();
        setMessageType(MessageType.CONNECT);
    }



    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame#pack(int)
     */
    @Override
    protected void pack() {
        packVeriableHeader();
        if (getAesSessionKey() != null) {
            buffer.put(getAesSessionKey());
        }
        if (getSignature() != null) {
            buffer.put(getSignature());
        }
        if (getSyncRequest() != null) {
            buffer.put(getSyncRequest());
        }
    }

    /**
     * Pack Connect variable header
     * @param pos - start position in buffer
     * @return - number of packed bytes.
     */
    private void packVeriableHeader() {
        buffer.put(FIXED_HEADER_CONST);
        buffer.putInt(nextProtocolId);
        if (getAesSessionKey() != null) {
            buffer.put(CONNECT_SESSION_KEY_FLAGS);
        } else {
            buffer.put((byte) 0);
        }
        if (getSignature() != null) {
            buffer.put(CONNECT_SIGNATURE_FLAGS);
        } else {
            buffer.put((byte) 0);
        }
        buffer.putChar((char) keepAlive);
    }

    /**
     * KeepAlive getter.
     * @return int keepAlive
     */
    public int getKeepAlive() {
        return keepAlive;
    }

    /**
     * KeepAlive setter.
     * @param keepAlive int
     */
    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * Next protocol ID getter.
     * @return Next protocol ID int
     */
    public int getNextProtocolId() {
        return nextProtocolId;
    }

    /**
     * Next protocol ID setter.
     * @param nextProtocolId protocol ID int
     */
    public void setNextProtocolId(int nextProtocolId) {
        this.nextProtocolId = nextProtocolId;
    }

    /**
     * AES Session Key getter
     * @return byte[] aesSessionKey
     */
    public byte[] getAesSessionKey() {
        return aesSessionKey;
    }

    /**
     * AES Session Key setter.
     * @param aesSessionKey byte[]
     */
    public void setAesSessionKey(byte[] aesSessionKey) {
        this.aesSessionKey = aesSessionKey;
        if(aesSessionKey != null) {
            this.hasAesSessionKey = true;
        }
    }

    /**
     * Signature getter.
     * @return byte[] signature
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Signature setter.
     * @param signature byte[]
     */
    public void setSignature(byte[] signature) {
        this.signature = signature;
        if(signature != null) {
            this.hasSignature = true;
        }
    }



    /**
     * @return the syncRequest
     */
    public byte[] getSyncRequest() {
        return syncRequest;
    }



    /**
     * @param syncRequest the syncRequest to set
     */
    public void setSyncRequest(byte[] syncRequest) {
        this.syncRequest = syncRequest;
    }


    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame#decode(byte[], int)
     */
    @Override
    protected void decode() throws KaaTcpProtocolException {
        decodeVariableHeader();
        nextProtocolId = buffer.getInt();
        hasAesSessionKey = buffer.get() != 0;
        hasSignature = buffer.get() != 0;
        decodeKeepAlive();
        if (hasAesSessionKey) {
            decodeSessionKey();
        }
        if (hasSignature) {
            decodeSignature();
        }
        decodeSyncRequest();
    }


    /**
     *
     */
    private void decodeSyncRequest() {
        int syncRequestSize = buffer.capacity() - buffer.position();
        if (syncRequestSize > 0) {
            syncRequest = new byte[syncRequestSize];
            buffer.get(syncRequest);
        }
    }


    /**
     *
     */
    private void decodeSignature() {
        signature = new byte[CONNECT_SIGNATURE_LENGTH];
        buffer.get(signature);
    }


    /**
     *
     */
    private void decodeSessionKey() {
        aesSessionKey = new byte[CONNECT_AES_SESSION_KEY_LENGTH];
        buffer.get(aesSessionKey);
    }


    /**
     * Decode variable header fields.
     * @throws KaaTcpProtocolException - if protocol version missmatch
     */
    private void decodeVariableHeader() throws KaaTcpProtocolException {
        for (int i = 0; i < FIXED_HEADER_CONST.length; i++) {
            if(FIXED_HEADER_CONST[i] != buffer.get()) {
                throw new KaaTcpProtocolException("Kaatcp protocol version missmatch");
            }
        }
    }

    private void decodeKeepAlive() {
        int msb = (buffer.get() & 0xFF) << 8;
        int lsb = buffer.get() & 0xFF;
        keepAlive = (msb | lsb);
    }


    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame#isNeedCloseConnection()
     */
    @Override
    public boolean isNeedCloseConnection() {
        return false;
    }

    public boolean hasSignature() {
        return hasSignature;
    }

    public boolean isEncrypted() {
        return hasAesSessionKey;
    }

}
