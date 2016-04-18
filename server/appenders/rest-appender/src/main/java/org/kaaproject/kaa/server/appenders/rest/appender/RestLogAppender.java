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

package org.kaaproject.kaa.server.appenders.rest.appender;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.rest.config.gen.MethodType;
import org.kaaproject.kaa.server.appenders.rest.config.gen.RequestType;
import org.kaaproject.kaa.server.appenders.rest.config.gen.RestConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestLogAppender extends AbstractLogAppender<RestConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(RestLogAppender.class);

    private ExecutorService executor;
    private CloseableHttpClient client;
    private HttpHost target;
    private URI targetURI;
    private RestConfig configuration;
    private boolean closed = false;

    public RestLogAppender() {
        super(RestConfig.class);
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender, RestConfig configuration) {
        this.configuration = configuration;
        this.executor = Executors.newFixedThreadPool(configuration.getConnectionPoolSize());
        target = new HttpHost(configuration.getHost(), configuration.getPort(), configuration.getSsl() ? "https" : "http");
        HttpClientBuilder builder = HttpClients.custom();
        if (configuration.getUsername() != null && configuration.getPassword() != null) {
            LOG.info("Adding basic auth credentials provider");
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()), new UsernamePasswordCredentials(
                    configuration.getUsername(), configuration.getPassword()));
            builder.setDefaultCredentialsProvider(credsProvider);
        }
        if (!configuration.getVerifySslCert()) {
            LOG.info("Adding trustful ssl context");
            SSLContextBuilder sslBuilder = new SSLContextBuilder();
            try {
                sslBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslBuilder.build());
                builder.setSSLSocketFactory(sslsf);
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                LOG.error("Failed to init socket factory {}", e.getMessage(), e);
            }
        }
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(configuration.getConnectionPoolSize());
        cm.setMaxTotal(configuration.getConnectionPoolSize());
        builder.setConnectionManager(cm);
        this.client = builder.build();
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header, final LogDeliveryCallback listener) {
        if (closed) {
            LOG.warn("Attempt to append data to already stopped appender");
            listener.onInternalError();
        }
        if(targetURI == null){
            try {
                targetURI = new URIBuilder().setHost(target.getHostName()).setPort(target.getPort()).setScheme(target.getSchemeName()).setPath(configuration.getPath()).build();
            } catch (URISyntaxException e) {
                LOG.warn("[{}] failed to build request URI", this.getApplicationToken(), e);
                listener.onInternalError();
            }
        }
        LOG.trace("[{}] appending {} logs to rest endpoint", this.getApplicationToken(), logEventPack.getEvents().size());
        final RestConfig configuration = this.configuration;
        try {
            for (final LogEventDto dto : generateLogEvent(logEventPack, header)) {
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            LOG.trace("[{}] appending {} to rest endpoint", RestLogAppender.this.getApplicationToken(), dto);
                            final HttpRequest request = createRequest(configuration, dto);
                            LOG.trace("[{}] executing {}", RestLogAppender.this.getApplicationToken(), request.getRequestLine());
                            
                            CloseableHttpResponse response = client.execute(target, request);
                            try {
                                int responseCode = response.getStatusLine().getStatusCode();
                                LOG.trace("[{}] received {} response code", RestLogAppender.this.getApplicationToken(), response);
                                if (responseCode >= 200 && responseCode < 400) {
                                    LOG.trace("[{}] logs appended successfully", getName());
                                    listener.onSuccess();
                                } else {
                                    LOG.warn("[{}] bad response code {}", getName(), responseCode);
                                    listener.onRemoteError();
                                }
                            } finally {
                                response.close();
                            }
                        } catch (IOException e) {
                            LOG.error("[{}] Failed to send log event.", getName(), e);
                            listener.onConnectionError();
                        } catch (Exception e) {
                            LOG.error("[{}] Failed to send log event.", getName(), e);
                            listener.onInternalError();
                        }
                    }
                });
            }
        } catch (IOException e) {
            LOG.error("[{}] Failed to send log events.", getName(), e);
            listener.onInternalError();
        }
    }

    private HttpRequest createRequest(RestConfig configuration, LogEventDto dto) throws URISyntaxException {
        String body = buildRequestBody(configuration, dto);
        ContentType contentType = buildContentType(configuration);
        StringEntity entity = new StringEntity(body, contentType);
        final HttpEntityEnclosingRequestBase request;
        
        if (configuration.getMethod() == MethodType.POST) {
            request = new HttpPost(targetURI);
        } else {
            request = new HttpPut(targetURI);
        }
        request.setEntity(entity);
        return request;
    }

    private ContentType buildContentType(RestConfig configuration) {
        ContentType contentType;
        if (configuration.getMimeType() == RequestType.TEXT) {
            contentType = ContentType.create("text/plain", "UTF-8");
        } else {
            contentType = ContentType.create("application/json", "UTF-8");
        }
        return contentType;
    }

    private String buildRequestBody(RestConfig configuration, LogEventDto dto) {
        String body;
        if (configuration.getHeader()) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"header\":").append(dto.getHeader()).append(",");
            sb.append("\"event\":").append(dto.getEvent()).append("}");
            body = sb.toString();
        } else {
            body = dto.getEvent();
        }
        return body;
    }

    @Override
    public void close() {
        closed = true;
        try {
            client.close();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            LOG.error("Failed to close appender: {}", e.getMessage(), e);
        }
    }
}
