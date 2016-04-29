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
 * Sync message Class.
 * The SYNC message is used as intermediate class for decoding messages 
 * SyncRequest,SyncResponse
 * 
 * 
 * @author Andrey Panasenko
 *
 * Sync message extend KaaSync with  Payload Avro object.
 *  
 *  Payload Avro object depend on Flags object can be zipped and than encrypted with AES SessionKey 
 *  exchanged with the CONNECT message.
 *
 */
public class Sync extends KaaSync {

    /** Avro object byte representation*/
    private byte[] avroObject;
    
    
    /**
     * Constructor for migrating from KaaSync to specific SyncRequest or SyncResponse
     * @param old KaaSync object which used to create new
     */
    protected Sync(KaaSync old) {
        super(old);
        setKaaSyncMessageType(KaaSyncMessageType.SYNC);
    }
    
    /**
     * Default constructor.
     * @param isRequest boolean 'true' SyncRequest, else SyncResponse
     * @param avroObject byte[] byte array of Avro object
     * @param isZipped boolean 'true' if Avro object is should be zipped
     * @param isEcrypted boolean 'true' if Avro object is Encrypted
     */
    public Sync(boolean isRequest, byte[] avroObject, boolean isZipped, boolean isEcrypted) {
        super(isRequest, isZipped, isEcrypted);
        setAvroObject(avroObject);
        setKaaSyncMessageType(KaaSyncMessageType.SYNC);
    }
    
    /**
     * Default constructor.
     */
    public Sync() {
        setKaaSyncMessageType(KaaSyncMessageType.SYNC);
    }

    /**
     * Decode Avro Object
     */
    protected void decodeAvroObject() {
        int avroObjectSize = buffer.capacity() - buffer.position();
        if (avroObjectSize > 0) {
            avroObject = new byte[avroObjectSize];
            buffer.get(avroObject);
        }
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.mqttFrame#pack(int)
     */
    @Override
    protected void pack() {
        packVeriableHeader();
        buffer.put(getAvroObject());
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
     * Avro Object (SyncRequest/SyncResponse) getter.
     * @return byte[] avroObject
     */
    public byte[] getAvroObject() {
        return avroObject;
    }

    /**
     * Avro Object (SyncRequest/SyncResponse) setter.
     * @param avroObject byte[] byte array of Avro object
     */
    public void setAvroObject(byte[] avroObject) {
        this.avroObject = avroObject;
        remainingLength = KAASYNC_VERIABLE_HEADER_LENGTH_V1 + avroObject.length;
    }
}
