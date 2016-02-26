/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.appenders.cdap.appender;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.cdap.config.gen.CdapConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.client.rest.RestStreamClient;
import co.cask.cdap.common.http.exception.HttpFailureException;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import co.cask.cdap.security.authentication.client.basic.BasicAuthenticationClient;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class CdapLogAppender extends AbstractLogAppender<CdapConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(CdapLogAppender.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final int DEFAULT_CALLBACK_THREAD_POOL_SIZE = 2;

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
        super(CdapConfig.class);
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header, LogDeliveryCallback listener) {
        if (!closed) {
            if (streamWriter != null) {
                LOG.debug("[{}] appending {} logs to cdap stream", this.getApplicationToken(), logEventPack.getEvents().size());
                try {
                    for (LogEventDto dto : generateLogEvent(logEventPack, header)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("{\"header\":").append(dto.getHeader()).append(",");
                        sb.append("\"event\":").append(dto.getEvent()).append("}");
                        ListenableFuture<Void> result = streamWriter.write(sb.toString(), UTF8);
                        Futures.addCallback(result, new Callback(listener), callbackExecutor);
                    }
                } catch (IOException e) {
                    LOG.debug("[{}] Failed to generate log event.", getName());
                    listener.onInternalError();
                }
            } else {
                LOG.info("[{}] Attempted to append to empty streamWriter.", getName());
                listener.onInternalError();
            }
        } else {
            LOG.info("[{}] Attempted to append to closed appender.", getName());
            listener.onInternalError();
        }
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender, CdapConfig configuration) {
        try {
            streamClient = initStreamClient(configuration);
            Integer callbackPoolSize = configuration.getCallbackThreadPoolSize();
            if (callbackPoolSize == null) {
                callbackPoolSize = DEFAULT_CALLBACK_THREAD_POOL_SIZE;
            }
            callbackPoolSize = Math.min(callbackPoolSize, MAX_CALLBACK_THREAD_POOL_SIZE);
            callbackExecutor = Executors.newFixedThreadPool(callbackPoolSize);

            if (configuration.getStream() != null) {
                streamWriter = streamClient.createWriter(configuration.getStream());
            } else {
                throw new IllegalArgumentException("Stream parameter is not set!");
            }
        } catch (Exception e) {
            LOG.error("Failed to init stream client: ", e);
        }
    }

    @Override
    public void close() {
        closed = true;
        try {
            if (streamClient != null) {
                streamClient.close();
            }
        } catch (IOException e) {
            LOG.error("Failed to close stream client: ", e);
        }
        streamClient = null;
        LOG.debug("Stopped Cdap log appender.");
    }

    private StreamClient initStreamClient(CdapConfig configuration) throws Exception {
        String host = configuration.getHost();
        int port = configuration.getPort();
        boolean ssl = configuration.getSsl() != null ? configuration.getSsl().booleanValue() : false;
        RestStreamClient.Builder builder = RestStreamClient.builder(host, port);
        builder.ssl(ssl);
        if (configuration.getWriterPoolSize() != null) {
            int writerPoolSize = Math.min(MAX_WRITER_POOL_SIZE, configuration.getWriterPoolSize().intValue());
            builder.writerPoolSize(writerPoolSize);
        }
        if (configuration.getVersion() != null) {
            builder.version(configuration.getVersion());
        }
        if (configuration.getVerifySslCert() != null) {
            builder.verifySSLCert(configuration.getVerifySslCert().booleanValue());
        }
        if (configuration.getAuthClient() != null) {
            AuthenticationClient authClient = (AuthenticationClient) Class.forName(configuration.getAuthClient()).getConstructor()
                    .newInstance();
            authClient.setConnectionInfo(host, port, ssl);
            if (authClient.isAuthEnabled()) {
                Properties properties = new Properties();
                if (configuration.getUsername() != null) {
                    properties.put("security.auth.client.username", configuration.getUsername());
                }
                if (configuration.getPassword() != null) {
                    properties.put("security.auth.client.password", configuration.getPassword());
                }
                if (configuration.getVerifySslCert() != null) {
                    properties.put(BasicAuthenticationClient.VERIFY_SSL_CERT_PROP_NAME, configuration.getVerifySslCert());
                }
                authClient.configure(properties);
            }
            builder.authClient(authClient);
        }
        return builder.build();
    }

    private static final class Callback implements FutureCallback<Void> {
        private final LogDeliveryCallback callback;

        private Callback(LogDeliveryCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onSuccess(Void result) {
            callback.onSuccess();
        }

        @Override
        public void onFailure(Throwable t) {
            if (t instanceof HttpFailureException) {
                callback.onRemoteError();
            } else if (t instanceof IOException) {
                callback.onConnectionError();
            } else {
                callback.onInternalError();
            }
        }
    }
}
