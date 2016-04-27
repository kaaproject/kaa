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

package org.kaaproject.kaa.server.transports.http.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.codec.binary.Base64;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract HTTP Test Client Class.
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
abstract public class HttpTestClient<T extends SpecificRecordBase,R  extends SpecificRecordBase> implements Runnable {
    
    /** The Constant logger. */
    protected static final Logger logger = LoggerFactory
            .getLogger(HttpTestClient.class);
    
    /** Destination URL connection */
    private HttpURLConnection connection;
    
    /** Random generator */
    protected static Random rnd = new Random();
    
    /** Multipart objects container */
    private MultipartObjects objects;
    
    /** Test ID, random generated */
    private int testId;
    
    /** byte array for signature */
    private byte[] signature;
    
    /** byte array for encrypted SessionKey */
    private byte[] key;
    
    /** byte array for POST Data */
    private byte[] data;
    
    /** encoder/decoder */
    private MessageEncoderDecoder crypt;
    
    /** Client Private Key */
    private PrivateKey clientPrivateKey;
    
    /** Client Public Key */
    private PublicKey clientPublicKey;
    
    /** Client Public Key Hash */
    private EndpointObjectHash clientPublicKeyHash;

    /** AVRO request converter */
    private AvroByteArrayConverter<T> requestConverter;
    
    /** AVRO response converter */
    private AvroByteArrayConverter<R> responseConverter;
    
    /** Activity interface */
    private HttpActivity<R> activity;
    
    /** generated test SyncRequest */
    private T request;
    
    
    /**
     * Constructor.
     * @param serverPublicKey - server public key
     * @param commandName - command name, used as end of URL
     * @param activity  - Activity interface implementation.
     * @throws MalformedURLException - throws if URL is incorrect
     * @throws Exception - throws if request creation failed
     */
    public HttpTestClient(PublicKey serverPublicKey, String commandName, HttpActivity<R> activity) 
            throws MalformedURLException, Exception {
        testId = rnd.nextInt();
        this.activity = activity;
        //TODO: replace
        int bindPort = 7888;
        String url = "http://localhost:"+bindPort+"/domain/"+commandName;
        connection = (HttpURLConnection)new URL(url).openConnection();
        objects = new MultipartObjects();
        requestConverter = new AvroByteArrayConverter<>(getRequestConverterClass());
        responseConverter = new AvroByteArrayConverter<>(getResponseConverterClass());
        init(serverPublicKey);
    }
    
    /**
     * Initialization of request keys and encoder/decoder
     * @param serverPublicKey - server public key
     * @throws Exception - if key generation failed.
     */
    private void init(PublicKey serverPublicKey) 
            throws Exception {
        KeyPairGenerator clientKeyGen;
        try {
            clientKeyGen = KeyPairGenerator.getInstance("RSA");
            clientKeyGen.initialize(2048);
            KeyPair clientKeyPair = clientKeyGen.genKeyPair();
            clientPrivateKey = clientKeyPair.getPrivate();
            clientPublicKey = clientKeyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(e.toString());
        }
        crypt = new MessageEncoderDecoder(clientPrivateKey, clientPublicKey, serverPublicKey);
        try {
            key = crypt.getEncodedSessionKey();
        } catch (GeneralSecurityException e) {
            throw new Exception(e.toString());
        }
        
        ByteBuffer publicKeyBuffer = ByteBuffer.wrap(EndpointObjectHash.fromSHA1(clientPublicKey.getEncoded()).getData());
                
        clientPublicKeyHash = EndpointObjectHash.fromBytes(publicKeyBuffer.array());
        
    }
    
    /**
     * Post initialization, encrypt and sign request
     * @param request - request to encrypt and sign
     * @throws Exception - in case of encrypt error
     */
    protected void postInit(T request) 
        throws Exception {
        
        try {
            byte[] requestBodyRaw = requestConverter.toByteArray(request);
            data = crypt.encodeData(requestBodyRaw);
            signature = crypt.sign(data);
            if (signature.length > 256) {
                throw new Exception("Error signature length must not be more than 256, but "+signature.length);
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new Exception(e.toString());
        }
        
        objects.addObject(CommonEPConstans.REQUEST_SIGNATURE_ATTR_NAME, signature);
        objects.addObject(CommonEPConstans.REQUEST_KEY_ATTR_NAME, key);
        objects.addObject(CommonEPConstans.REQUEST_DATA_ATTR_NAME, data);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        logger.trace("Test: "+testId+" started...");
        IOException error = null;
        try {
            //connection.setChunkedStreamingMode(2048);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", objects.getContentType());
            
            DataOutputStream out =
                    new DataOutputStream(connection.getOutputStream());
            objects.dumbObjects(out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            error = e;
        }
        List<Byte> bodyArray = new Vector<>();
        
        try {
            DataInputStream r = new DataInputStream(connection.getInputStream());
            while (true) {
                bodyArray.add(new Byte(r.readByte()));
            }
        } catch (EOFException eof) {
            
        } catch (IOException e) {
            e.printStackTrace();
            error = e;
        }
        byte[] body = new byte[bodyArray.size()];
        for (int i = 0; i < body.length; i++) {
            body[i] = bodyArray.get(i);
        }
        processComplete(error, connection.getHeaderFields(), body);
    }

    /**
     * push Response to client invocation code
     * @param e - set if error received during HTTP request processing
     * @param header - header list
     * @param body - body byte array
     */
    private void processComplete(IOException e,
            Map<String, List<String>> header, byte[] body) {
        if (e != null) {
            e.printStackTrace();
            activity.httpRequestComplete(e, this.testId, null);
            return;
        }
        try {
            R response = decodeHttpResponse(header, body);
            activity.httpRequestComplete(null, this.testId, response);
        } catch (Exception e1) {
            e1.printStackTrace();
            activity.httpRequestComplete(e1, this.testId, null);
        }
        
    }
    
    /**
     * Decode http response to Response
     * @param header
     * @param body
     * @return type R Response
     * @throws Exception
     */
    protected R decodeHttpResponse(Map<String, List<String>> header, byte[] body) throws Exception {
        if (header.containsKey(CommonEPConstans.SIGNATURE_HEADER_NAME) 
                && header.get(CommonEPConstans.SIGNATURE_HEADER_NAME) != null
                && header.get(CommonEPConstans.SIGNATURE_HEADER_NAME).size() > 0) {
            String sigHeader = header.get(CommonEPConstans.SIGNATURE_HEADER_NAME).get(0);
            byte[] respSignature = Base64.decodeBase64(sigHeader);
            byte[] respData = body;
            crypt.verify(respData, respSignature);
            logger.trace("Test "+getId()+" response verified, body size "+body.length);
            byte[] respDecoded = crypt.decodeData(respData);
            return responseConverter.fromByteArray(respDecoded);
        } else {
           throw new Exception("HTTP response incorrect, no signature fields "+CommonEPConstans.SIGNATURE_HEADER_NAME); 
        }
    }
    
    
    /**
     * Test ID getter.
     * @return int Test ID
     */
    public int getId() {
        return testId;
    }
    
    /**
     * Client Public Key getter.
     * @return the clientPublicKey
     */
    public PublicKey getClientPublicKey() {
        return clientPublicKey;
    }
    
    /**
     * Client Public Key Hash getter.
     * @return the clientPublicKeyHash
     */
    public EndpointObjectHash getClientPublicKeyHash() {
        return clientPublicKeyHash;
    }
    
    /**
     * @return the request
     */
    public T getRequest() {
        return request;
    }
    
    /**
     * 
     * @param request
     */
    public void setRequest(T request) {
        this.request = request;
    }
    
    /**
     * Gets the request converter class.
     * 
     * @return the request converter class
     */
    protected abstract Class<T> getRequestConverterClass();

    /**
     * Gets the response converter class.
     * 
     * @return the response converter class
     */
    protected abstract Class<R> getResponseConverterClass();

    
    /**
     * Generate String with random ascii symbols from 48 till 122 with length size.
     * 
     * @param size of String
     * @return String with random ascii symbols
     */
    public static String getRandomString(int size) {
        return MultipartObjects.getRandomString(size);
    }
    
    /**
     * generate random bytes array with size
     * @param size of bytes
     * @return byte[] array of random bytes
     */
    public static byte[] getRandomBytes(int size) {
        byte[] rndbytes = new byte[size];
        rnd.nextBytes(rndbytes);
        return rndbytes;
    }
}
