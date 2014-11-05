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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.log.shared.appender.CustomLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.client.rest.RestStreamClient;
import co.cask.cdap.security.authentication.client.AuthenticationClient;

public class CdapLogAppender extends CustomLogAppender {

    private static final Logger LOG = LoggerFactory.getLogger(CdapLogAppender.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Configuration properties constants
     */
    private static final String STREAM = "stream";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String SSL = "ssl";
    private static final String VERIFY_SSL_CERT = "verify_ssl_cert";
    private static final String VERSION = "version";
    private static final String AUTH_CLIENT = "auth_client";
    private static final String CALLBACK_THREAD_POOL_SIZE = "callback_thread_pool_size";
    private static final String WRITER_POOL_SIZE = "writer_pool_size";

    /**
     * Default values
     */
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "10000";
    private static final String DEFAULT_SSL = "false";
    private static final String DEFAULT_CALLBACK_THREAD_POOL_SIZE = "2";

    /**
     * Max values for security purposes
     */
    private static final int MAX_CALLBACK_THREAD_POOL_SIZE = 10;
    private static final int MAX_WRITER_POOL_SIZE = 100;

    private boolean closed = false;

    private StreamClient streamClient;
    private StreamWriter streamWriter;
    private Executor callbackExecutor;

    public CdapLogAppender() {
        super();
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        if (!closed) {
            if (streamWriter != null) {
                final String appToken = this.getApplicationToken();
                LOG.debug("[{}] appending {} logs to cdap stream", this.getApplicationToken(), logEventPack.getEvents()
                        .size());
                for (LogEventDto dto : generateLogEvent(logEventPack, header)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("{\"header\":").append(dto.getHeader()).append(",");
                    sb.append("\"event\":").append(dto.getEvent()).append("}");
                    ListenableFuture<Void> result = streamWriter.write(sb.toString(), UTF8);
                    Futures.addCallback(result, new Callback(appToken), callbackExecutor);
                }
            } else {
                LOG.info("[{}] Attempted to append to empty streamWriter.", getName());
            }
        } else {
            LOG.info("[{}] Attempted to append to closed appender.", getName());
        }
    }

    @Override
    protected void initFromProperties(Properties properties) {
        try {
            streamClient = initStreamClient(properties);
            callbackExecutor = Executors.newFixedThreadPool(getPropertyValue(properties, CALLBACK_THREAD_POOL_SIZE,
                    DEFAULT_CALLBACK_THREAD_POOL_SIZE, MAX_CALLBACK_THREAD_POOL_SIZE));
            if (properties.containsKey(STREAM)) {
                streamWriter = streamClient.createWriter(properties.getProperty(STREAM));
            } else {
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

    private static int getPropertyValue(Properties properties, String propertyName, int maxValue) {
        return Math.min(Integer.parseInt(properties.getProperty(propertyName)), maxValue);
    }

    private static int getPropertyValue(Properties properties, String propertyName, String defaultValue, int maxValue) {
        return Math.min(Integer.parseInt(properties.getProperty(propertyName, defaultValue)), maxValue);
    }

    private StreamClient initStreamClient(Properties properties) throws Exception {
        String host = properties.getProperty(HOST, DEFAULT_HOST);
        int port = Integer.valueOf(properties.getProperty(PORT, DEFAULT_PORT));
        boolean ssl = Boolean.parseBoolean(properties.getProperty(SSL, DEFAULT_SSL));
        RestStreamClient.Builder builder = RestStreamClient.builder(host, port);

        if (properties.containsKey(SSL)) {
            builder.ssl(ssl);
        }
        if (properties.containsKey(WRITER_POOL_SIZE)) {
            builder.writerPoolSize(getPropertyValue(properties, WRITER_POOL_SIZE, MAX_WRITER_POOL_SIZE));
        }
        if (properties.containsKey(VERSION)) {
            builder.version(properties.getProperty(VERSION));
        }
        if (properties.containsKey(VERIFY_SSL_CERT)) {
            builder.verifySSLCert(Boolean.parseBoolean(properties.getProperty(VERIFY_SSL_CERT)));
        }
        if (properties.containsKey(AUTH_CLIENT)) {
            AuthenticationClient authClient = (AuthenticationClient) Class.forName(properties.getProperty(AUTH_CLIENT))
                    .getConstructor().newInstance();
            authClient.setConnectionInfo(host, port, ssl);
            if (authClient.isAuthEnabled()) {
                authClient.configure(properties);
            }
            builder.authClient(authClient);
        }
        return builder.build();
    }

    private static final class Callback implements FutureCallback<Void> {
        private final String appToken;

        private Callback(String appToken) {
            this.appToken = appToken;
        }

        @Override
        public void onSuccess(Void result) {
            LOG.trace("[{}] Successfull log delivery", appToken);
        }

        @Override
        public void onFailure(Throwable t) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("[%s] Error during log delivery", appToken), t);
            }
        }
    }
}
