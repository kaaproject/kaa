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

package org.kaaproject.kaa.server.appenders.cdap.appender;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.log.shared.appender.CustomLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.client.rest.RestStreamClient;
import co.cask.cdap.security.authentication.client.AuthenticationClient;

public class CdapLogAppender extends CustomLogAppender {

    /**
     * Configuration properties constants
     */
    private static final String STREAM = "stream";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String SSL = "ssl";
    private static final String VERIFY_SSL_CERT = "verify_SSL_CERT";
    private static final String VERSION = "version";
    private static final String AUTH_CLIENT = "auth_client";

    /**
     * Default values
     */
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "10000";
    private static final String DEFAULT_SSL = "false";

    private static final Logger LOG = LoggerFactory.getLogger(CdapLogAppender.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private boolean closed = false;

    private StreamClient streamClient;
    private StreamWriter streamWriter;

    public CdapLogAppender() {
        super();
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        if (!closed && streamWriter != null) {
            LOG.debug("[{}] appending {} logs to cdap stream", this.getApplicationToken(), logEventPack.getEvents().size());
            for(LogEventDto dto : generateLogEvent(logEventPack, header)){
                StringBuilder sb = new StringBuilder();
                sb.append("{\"header\":").append(dto.getHeader()).append(",");
                sb.append("\"event\":").append(dto.getEvent()).append("}");
                streamWriter.write(sb.toString(), UTF8);
            }
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
        }
    }

    @Override
    protected void initFromProperties(Properties properties){
        try {
            streamClient = initStreamClient(properties);
            if(properties.containsKey(STREAM)){
                streamWriter = streamClient.createWriter(properties.getProperty(STREAM));
            }else{
                throw new IllegalArgumentException(STREAM + " property is not set!");
            }
        } catch (Exception e) {
            LOG.error("Failed to init stream client: ", e);
        }
    }
    
    @Override
    public void close() {
        closed = true;
        try {
            streamClient.close();
        } catch (IOException e) {
            LOG.error("Failed to close stream client: ", e);
        }
        LOG.debug("Stopped Cdap log appender.");
    }    
    
    private StreamClient initStreamClient(Properties properties) throws Exception{
        String host = properties.getProperty(HOST, DEFAULT_HOST);
        Integer port = Integer.valueOf(properties.getProperty(PORT, DEFAULT_PORT));
        boolean ssl = Boolean.parseBoolean(properties.getProperty(SSL, DEFAULT_SSL));
        RestStreamClient.Builder builder = RestStreamClient.builder(host, port).ssl(ssl);
        
        if(properties.containsKey(VERSION)){
            builder.version(properties.getProperty(VERSION));
        }
        if(properties.containsKey(VERIFY_SSL_CERT)){
            builder.verifySSLCert(Boolean.parseBoolean(properties.getProperty(VERIFY_SSL_CERT)));
        }
        if(properties.containsKey(AUTH_CLIENT)){
            AuthenticationClient authClient =
                    (AuthenticationClient) Class.forName(properties.getProperty(AUTH_CLIENT)).getConstructor().newInstance();
            authClient.setConnectionInfo(host, port, ssl);
            if (authClient.isAuthEnabled()) {
                authClient.configure(properties);
            }
            builder.authClient(authClient);
        }
        return builder.build();
    }
}
