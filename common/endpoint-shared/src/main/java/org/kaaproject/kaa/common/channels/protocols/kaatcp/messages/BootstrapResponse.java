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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.KaaTcpProtocolException;

/**
 * Bootstrap Response Class.
 * Extends KaaSync message, {@link KaaSync} with:
 * 
 *      
 * Operations servers number int 4 bytes
 * byte 1  Length MSB
 * byte 2  Length
 * byte 3  Length
 * byte 4  Length LSB
 * 
 * Operations server record length (int) in network order
 * byte 5  Length MSB
 * byte 6   
 * byte 7   
 * byte 8  Length LSB
 * Operations server record 1
 * 
 * Operation server record
 *   Name UTF-8 string
 *   byte 1 Length MSB (int) in network order
 *   byte 2   
 *   byte 3   
 *   byte 4  Length LSB
 *   byte[]  UTF-8 String
 *
 *   Padding bytes 0-3 to align next priority offset to multiply of 4
 *   byte    padding 0x00
 *
 *   Priority int in network order
 *   byte N      Length MSB
 *   byte N+1     
 *   byte N+2     
 *   byte N+3    Length LSB
 *
 *   Public Key
 *   byte M  Public Key Type
 *   byte M+1    unused 0x00
 *   byte M+2    Length MSB
 *   byte M+3    Length LSB
 *   byte[] Public Key (length always multiply of 4)
 *
 *   Supported Channels number (int) in network order
 *   byte K      Length MSB
 *   byte K+1     
 *   byte K+2     
 *   byte K+3    Length LSB
 * 
 *   Supported channels record 1 length int in network order
 *   byte K+4    Length MSB
 *   byte K+5     
 *   byte K+6     
 *   byte K+7    Length LSB
 *   byte[]  record 1
 *
 *   Padding bytes from 0 to 3 to align next channels record 
 *   actual record offset from payload start should be multiply by 4
 *   padding 0x00
 *   
 *   Supported channels record
 *
 *   byte 1  Supported channel type
 *   byte 2  HostName length
 *   byte 3  port MSB
 *   byte 4  port LSB
 *   byte[]  ASCII chars Hostname
 *   
 *   Supported channel type
 *   Mnemonic    Enumeration    Description
 *   UNUSED      0              Reserved value
 *   HTTP        1              Http channel
 *   HTTPLP      2              Http Long Poll channel
 *   KaaTCP      3              KaaTCP channel
 *
 *   All channels at this moment have same communication parameters format.
 *   
 *   Public Key Type
 *   Mnemonic    Enumeration Description
 *   UNUSED      0           Reserved value
 *   RSA-PKSC8   1           RSA with  PKCS #8 standard
 * 
 * @author Andrey Panasenko
 *
 */
public class BootstrapResponse extends Bootstrap {

    /** Map of operations servers, with Key - server Name */
    private HashMap<String, OperationsServerRecord> operationsServersMap;
    
    /** List of packed records */
    private List<byte[]> operationsServersRecordPacked;
    
    
    /**
     *  Supported channel type Enum
     */
    public enum SupportedChannelType {
        UNUSED((byte)0),
        HTTP((byte)1),
        HTTPLP((byte)2),
        KAATCP((byte)3);
        
        private byte type;
        
        private SupportedChannelType(byte type) {
            this.type = type;
        }
        
        /**
         * Return byte representation of SupportedChannelType enum.
         * @return byte type
         */
        public byte getType() {
            return type;
        }
        
        /**
         * Transform byte to SupportedChannelType
         * @param b byte from stream
         * @return SupportedChannelType
         * @throws KaatcpProtocolException
         */
        public static SupportedChannelType getSupportedChannelTypeFromByte(byte b) 
                throws KaaTcpProtocolException {
            if (UNUSED.getType() == b) {
                return UNUSED;
            } else if (HTTP.getType() == b) {
                return HTTP;
            } else if (HTTPLP.getType() == b) {
                return HTTPLP;
            } else if (KAATCP.getType() == b) {
                return KAATCP;
            } else {
                throw new KaaTcpProtocolException("Error parsing SupportedChannelType, unknown type");
            }
        }
    }
    
    /**
     *  PublicKeyType Enum
     *
     */
    public enum PublicKeyType {
        UNUSED((byte)0),
        RSA_PKSC8((byte)1);
        
        private byte type;
        
        private PublicKeyType(byte type) {
            this.type = type;
        }
        
        /**
         * Return byte representation of PublicKeyType enum.
         * @return byte type
         */
        public byte getType() {
            return type;
        }
        
        /**
         * Transform byte to PublicKeyType
         * @param b byte from stream
         * @return PublicKeyType
         * @throws KaatcpProtocolException
         */
        public static PublicKeyType getPublicKeyTypeFromByte(byte b) 
                throws KaaTcpProtocolException {
            if (UNUSED.getType() == b) {
                return UNUSED;
            } else if (RSA_PKSC8.getType() == b) {
                return RSA_PKSC8;
            } else {
                throw new KaaTcpProtocolException("Error parsing PublicKeyType, unknown type");
            }
        }
    }
    
    /**
     * SupportedChannelRecord Class.
     */
    public class SupportedChannelRecord {
        public SupportedChannelType supportedChannelType;
        public String hostName;
        public int port;
    }
    
    /**
     *  OperationsServerRecord Class.
     */
    public class OperationsServerRecord {
        public String name;
        public int priority;
        public PublicKeyType publicKeyType;
        public byte[] publicKey;
        public List<SupportedChannelRecord> supportedChannelsList;
        public OperationsServerRecord() {
            supportedChannelsList = new LinkedList<>();
        }
    }
    
    /**
     *  UnknownOperationsServerExceptions Class.
     */
    public class UnknownOperationsServerExceptions extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = -3380509484310120766L;
        
    }
    
    /**
     * Constructor used to migrate from KaaSync to BootstrapResponse.
     * @param old KaaSync 
     */
    protected BootstrapResponse(KaaSync old) 
            throws KaaTcpProtocolException {
        super(old);
        setRequest(false);
        operationsServersMap = new HashMap<>();
        operationsServersRecordPacked = new LinkedList<byte[]>();
        decodeOperationsServerRecords();
    }

    /**
     * Default constructor
     */
    public BootstrapResponse() {
        setRequest(false);
        operationsServersMap = new HashMap<>();
        operationsServersRecordPacked = new LinkedList<byte[]>();
    }

    /**
     * Add new Operation Server into Map
     * @param name - Operations Server Name
     * @param priority - Priority
     * @param publicKeyType - Public Key type
     * @param publicKey - Public Key byte[]
     */
    public void addOperationsServer(String name, int priority, PublicKeyType publicKeyType, byte[] publicKey) {
        OperationsServerRecord record = null;
        if (operationsServersMap.containsKey(name)) {
            record = operationsServersMap.get(name);
        } else {
            record = new OperationsServerRecord();
            operationsServersMap.put(name, record);
        }
        record.name = name;
        record.priority = priority;
        record.publicKeyType = publicKeyType;
        record.publicKey = publicKey;
    }
    
    /**
     * Add Supported channel into previously added Operations Server, specified with opServerName
     * @param opServerName - Name of Operations Server to which Supported Channel should be added.
     * @param supportedChannelType type of Supported Channel
     * @param hostName - hostName or IP address of Operations Server
     * @param port - IP port of  Supported Channels
     * @throws UnknownOperationsServerExceptions
     */
    public void addSupportedChannel(String opServerName, SupportedChannelType supportedChannelType, String hostName, int port) 
            throws UnknownOperationsServerExceptions {
        SupportedChannelRecord record = new SupportedChannelRecord();
        record.supportedChannelType = supportedChannelType;
        record.hostName = hostName;
        record.port = port;
        if (operationsServersMap.containsKey(opServerName)) {
            operationsServersMap.get(opServerName).supportedChannelsList.add(record);
        } else {
            throw new UnknownOperationsServerExceptions();
        }
    }
    
    /**
     * OperationsServers Map getter.
     * @return Map<String, OperationsServerRecord>
     */
    public Map<String, OperationsServerRecord> getOperationsServers() {
        return operationsServersMap;
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame#pack()
     */
    @Override
    protected void pack() {
        super.pack();
        if (operationsServersRecordPacked.size() == 0) {
            packOperationsServerRecords();
        }
        packRecordsToBuffer();
    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame#getRemainingLegth()
     */
    @Override
    protected int getRemainingLegth() {
        packOperationsServerRecords();
        return remainingLength;
    }

    /**
     * Pack OperationsServer record to buffer.
     */
    private void packRecordsToBuffer() {
        buffer.putInt(operationsServersRecordPacked.size());
        for(byte[] bytesRecord : operationsServersRecordPacked) {
            buffer.putInt(bytesRecord.length);
            buffer.put(bytesRecord);
        }
    }

    /**
     * Pack operations server record in List, and calculate remainingLength
     */
    private void packOperationsServerRecords() {
        int recordsLength = 4; //First Number of records
        operationsServersRecordPacked.clear();
        for(OperationsServerRecord record : operationsServersMap.values()) {
            byte[] bytesRecord = packOperationsServerRecord(record);
            operationsServersRecordPacked.add(bytesRecord);
            recordsLength += 4; //Added record length
            recordsLength += bytesRecord.length;
        }
        //update remaining length
        remainingLength = KAASYNC_VERIABLE_HEADER_LENGTH_V1 + recordsLength;
    }

    /**
     * Pack Operations Server Record.
     * @param OperationsServerRecord record
     * @return byte[] of packed record
     */
    private byte[] packOperationsServerRecord(OperationsServerRecord record) {
        //Calculate length of record
        /*
         * 
         */
        int recordLength = 4; //Name UTF-8 string length int
        int nameLength = record.name.getBytes().length;
        nameLength = ((nameLength + 3) >> 2);
        nameLength = nameLength << 2;
        recordLength += nameLength; // Added actual NameLength with padding
        recordLength += 4; //priority
        recordLength += 4; //KeyType and KeyLength
        recordLength += record.publicKey.length; //Add KeyLength
        recordLength += 4; //SupportedChannelNumber
        
        List<byte[]> packedSupportedChannels = new LinkedList<>();
        for(SupportedChannelRecord suppRecord : record.supportedChannelsList) {
            recordLength += 4; //SupportedChannel record length
            byte[] packedSuppRecord = packSupportedChannelRecord(suppRecord);
            packedSupportedChannels.add(packedSuppRecord);
            
            int packedSuppChannelLength = packedSuppRecord.length;
            packedSuppChannelLength = ((packedSuppChannelLength + 3) >> 2);
            packedSuppChannelLength = packedSuppChannelLength << 2;
            
            recordLength += packedSuppChannelLength; //packed SupportedChannel Record with padding
        }
        
        ByteBuffer packedRecord = ByteBuffer.allocate(recordLength);
        packedRecord.putInt(record.name.getBytes().length); //Put Name UTF-8 string length int
        packedRecord.put(record.name.getBytes()); //Put Name
        int namePadding = nameLength - record.name.getBytes().length;
        packedRecord.position(packedRecord.position()+namePadding); //Shift padding.
        
        packedRecord.putInt(record.priority); //Put priority
        
        packedRecord.put(record.publicKeyType.getType()); //Public Key Type
        packedRecord.put((byte)0x00); //unused
        packedRecord.putShort((short)record.publicKey.length);
        packedRecord.put(record.publicKey); //Put public key
        packedRecord.putInt(packedSupportedChannels.size()); //Put int number of supported channels.
        for(byte[] packedSuppRecord : packedSupportedChannels) {
            packedRecord.putInt(packedSuppRecord.length); //Put supported channels record size
            packedRecord.put(packedSuppRecord); //Put supported record
            int paddLength = (4 - (packedSuppRecord.length & 0x00000003)) & 3;
            packedRecord.position(packedRecord.position() + paddLength); //Shift position to padding bytes
        }
        return packedRecord.array();
    }

    /**
     * Pack Supported channels record
     * @param SupportedChannelRecord suppRecord
     * @return byte[] packed Supported Channel record
     */
    private byte[] packSupportedChannelRecord(SupportedChannelRecord suppRecord) {
        int recordLength = 4;
        recordLength += suppRecord.hostName.getBytes().length;
        ByteBuffer packedRecord = ByteBuffer.allocate(recordLength);
        packedRecord.put(suppRecord.supportedChannelType.getType()); //Put SupportedChannel type
        packedRecord.put((byte)suppRecord.hostName.getBytes().length); //Put host name length, byte
        packedRecord.putShort((short)suppRecord.port); //Put port
        packedRecord.put(suppRecord.hostName.getBytes()); //Put hostname
        return packedRecord.array();
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame#decode()
     */
    @Override
    protected void decode() throws KaaTcpProtocolException {
        super.decode();
        decodeOperationsServerRecords();
    }

    /**
     * Decode OperationsServerRecords from current position in buffer.
     * @throws KaatcpProtocolException 
     * 
     */
    private void decodeOperationsServerRecords() throws KaaTcpProtocolException {
        int operationsServerRecordsNumber = buffer.getInt(); //Get number of records
        for(int i=0; i<operationsServerRecordsNumber; i++) {
            int operationsServerRecordLength = buffer.getInt(); //Get record length
            OperationsServerRecord record = decodeOperationsServerRecord(operationsServerRecordLength);
            operationsServersMap.put(record.name, record);
        }
    }

    /**
     * Decode OperationsServerRecord
     * @param operationsServerRecordLength
     * @return OperationsServerRecord
     * @throws KaatcpProtocolException 
     */
    private OperationsServerRecord decodeOperationsServerRecord(int operationsServerRecordLength) 
            throws KaaTcpProtocolException {
        OperationsServerRecord record = new OperationsServerRecord();
        int nameLength = buffer.getInt(); //get name length
        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes); //read name
        record.name = new String(nameBytes);
        int namePadding = (4 - (nameLength & 0x00000003)) & 3;
        buffer.position(buffer.position()+namePadding); //Shift buffer on padding bytes.
        record.priority = buffer.getInt(); //get priority
        record.publicKeyType = PublicKeyType.getPublicKeyTypeFromByte(buffer.get()); //Get PublicKeyType
        buffer.get(); //read unused byte
        int publicKeyLength = buffer.getShort(); //get public key length
        record.publicKey = new byte[publicKeyLength];
        buffer.get(record.publicKey); //get public key
        int suppChennelsNumber = buffer.getInt(); //get supported channels number
        for(int i = 0; i<suppChennelsNumber; i++) {
            int suppRecordLength = buffer.getInt(); //get supported channels record length
            SupportedChannelRecord suppRecord = decodeSupportedChannelRecord(suppRecordLength);
            record.supportedChannelsList.add(suppRecord);
            int suppPadding = (4 - (suppRecordLength & 0x00000003)) & 3;
            buffer.position(buffer.position()+suppPadding); //Shift buffer on padding bytes.
        }
        return record;
    }

    /**
     * Decode SupportedChannelRecord
     * @param suppRecordLength
     * @return SupportedChannelRecord
     * @throws KaatcpProtocolException 
     */
    private SupportedChannelRecord decodeSupportedChannelRecord(int suppRecordLength) 
            throws KaaTcpProtocolException {
        SupportedChannelRecord suppRecord = new SupportedChannelRecord();
        suppRecord.supportedChannelType = SupportedChannelType.getSupportedChannelTypeFromByte(buffer.get()); //get type
        int nameLength = buffer.get() & 0x000000FF; //get name length
        suppRecord.port = buffer.getShort();  //get port
        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes); //get name
        suppRecord.hostName = new String(nameBytes);
        return suppRecord;
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame#isNeedCloseConnection()
     */
    @Override
    public boolean isNeedCloseConnection() {
        return true;
    }
}
