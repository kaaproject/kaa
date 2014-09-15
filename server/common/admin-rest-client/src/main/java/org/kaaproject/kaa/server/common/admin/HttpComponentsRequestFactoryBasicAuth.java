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
package org.kaaproject.kaa.server.common.admin;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public class HttpComponentsRequestFactoryBasicAuth extends
        HttpComponentsClientHttpRequestFactory {

    private static final Logger logger = LoggerFactory.getLogger(HttpComponentsRequestFactoryBasicAuth.class);
    
    private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 100;

    private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 5;

    private HttpHost host;
    private CredentialsProvider credsProvider;
    
    public HttpComponentsRequestFactoryBasicAuth(HttpHost host) {
        super(createHttpClient());
        this.host = host;
        credsProvider = new BasicCredentialsProvider();
        this.setConnectTimeout(60000);
        this.setReadTimeout(0);
    }
    
    private static HttpClient createHttpClient() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
        connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
        connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);

        DefaultHttpClient defaultHttpClient = new DefaultHttpClient(connectionManager);
        defaultHttpClient.setHttpRequestRetryHandler(new BasicHttpRequestRetryHandler(5, 10000));
        HttpClient httpClient = new AutoRetryHttpClient(defaultHttpClient, new BaseServiceUnavailableRetryStrategy(3, 5000));
        return httpClient;
    }

    protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
        return createHttpContext();
    }

    private HttpContext createHttpContext() {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(host, basicAuth);
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        localcontext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);
        return localcontext;
    }
    
    public CredentialsProvider getCredentialsProvider() {
        return credsProvider;
    }
    
    public void setCredentials(String username, String password) {
        credsProvider.setCredentials(
                new AuthScope(host.getHostName(), host.getPort(), AuthScope.ANY_REALM),
                 new UsernamePasswordCredentials(username, password));
    }
    
    static class BasicHttpRequestRetryHandler extends DefaultHttpRequestRetryHandler {

        private final long connectRetryInterval;
        
        public BasicHttpRequestRetryHandler(int retryCount, long connectRetryInterval) {
            super(retryCount, false);
            this.connectRetryInterval = connectRetryInterval;
        }
        
        @Override
        public boolean retryRequest(IOException exception, int executionCount,
                HttpContext context) {
            if (executionCount <= getRetryCount()) {
                try {
                    logger.warn("IOException '{}'. Wait for {} before next attempt to connect...", exception.getMessage(), connectRetryInterval);
                    Thread.sleep(connectRetryInterval);
                } catch (InterruptedException e) {}
                return true;
            }
            else {
                return super.retryRequest(exception, executionCount, context);
            }
        }
        
    }
    
    static class BaseServiceUnavailableRetryStrategy implements ServiceUnavailableRetryStrategy {

        /**
         * Maximum number of allowed retries if the server responds with a HTTP code
         * in our retry code list. Default value is 1.
         */
        private final int maxRetries;

        /**
         * Retry interval between subsequent requests, in milliseconds. Default
         * value is 1 second.
         */
        private final long retryInterval;

        public BaseServiceUnavailableRetryStrategy(int maxRetries, int retryInterval) {
            super();
            if (maxRetries < 1) {
                throw new IllegalArgumentException("MaxRetries must be greater than 1");
            }
            if (retryInterval < 1) {
                throw new IllegalArgumentException("Retry interval must be greater than 1");
            }
            this.maxRetries = maxRetries;
            this.retryInterval = retryInterval;
        }

        public BaseServiceUnavailableRetryStrategy() {
            this(1, 1000);
        }

        public boolean retryRequest(final HttpResponse response, int executionCount, final HttpContext context) {
            return executionCount <= maxRetries &&
                (response.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE ||
                 response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND);
        }

        public long getRetryInterval() {
            return retryInterval;
        }

    }
}