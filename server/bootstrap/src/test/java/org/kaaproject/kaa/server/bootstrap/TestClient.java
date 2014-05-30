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

package org.kaaproject.kaa.server.bootstrap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.bootstrap.CommonBSConstants;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.Resolve;
import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.server.bootstrap.service.http.commands.ResolveCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class TestClient implements Runnable {

    /** The Constant logger. */
    protected static final Logger logger = LoggerFactory
            .getLogger(TestClient.class);

    /** Destination URL connection */
    private final HttpURLConnection connection;

    /** Random generator */
    protected static Random rnd = new Random();

    /** Test ID, random generated */
    private final int testId;

    /** Activity interface */
    private final HttpActivity activity;

    /** encoder/decoder */
    private final MessageEncoderDecoder crypt;

    /** AVRO response converter */
    private final AvroByteArrayConverter<OperationsServerList> responseConverter;

    /** Multipart objects container */
    private final MultipartObjects objects;

    public TestClient(String nettyHost, int nettyPort, PublicKey serverPublicKey, HttpActivity activity) throws MalformedURLException, IOException {
        testId = rnd.nextInt();
        String url = "http://"+nettyHost+":"+nettyPort+"/domain/"+ResolveCommand.getCommandName();
        connection = (HttpURLConnection)new URL(url).openConnection();
        this.activity = activity;
        crypt = new MessageEncoderDecoder(null,null,serverPublicKey);
        responseConverter = new AvroByteArrayConverter<>(OperationsServerList.class);
        objects = new MultipartObjects();
        String appToken = Integer.toString(rnd.nextInt());

        AvroByteArrayConverter<Resolve> requestConverter = new AvroByteArrayConverter<Resolve>(Resolve.class);
        Resolve r = new Resolve(appToken);
        objects.addObject(CommonBSConstants.APPLICATION_TOKEN_ATTR_NAME, requestConverter.toByteArray(r));
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
            OperationsServerList response = decodeHttpResponse(header, body);
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
    protected OperationsServerList decodeHttpResponse(Map<String, List<String>> header, byte[] body) throws Exception {
        if (header.containsKey(CommonEPConstans.SIGNATURE_HEADER_NAME)
                && header.get(CommonEPConstans.SIGNATURE_HEADER_NAME) != null
                && header.get(CommonEPConstans.SIGNATURE_HEADER_NAME).size() > 0) {
            String sigHeader = header.get(CommonEPConstans.SIGNATURE_HEADER_NAME).get(0);
            byte[] respSignature = Base64.decodeBase64(sigHeader);
            byte[] respData = body;
            crypt.verify(respData, respSignature);
            logger.trace("Test "+getId()+" response verified, body size "+body.length);
            return responseConverter.fromByteArray(respData);
        } else {
           throw new Exception("HTTP response incorrect, no signature fields "+CommonBSConstants.SIGNATURE_HEADER_NAME);
        }
    }

    /**
     * Test ID getter.
     * @return int Test ID
     */
    public int getId() {
        return testId;
    }
}
