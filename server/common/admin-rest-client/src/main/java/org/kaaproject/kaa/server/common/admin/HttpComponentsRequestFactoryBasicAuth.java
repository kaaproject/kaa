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

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public class HttpComponentsRequestFactoryBasicAuth extends
        HttpComponentsClientHttpRequestFactory {

    private HttpHost host;
    private CredentialsProvider credsProvider;
    
    public HttpComponentsRequestFactoryBasicAuth(HttpHost host) {
        super();
        this.host = host;
        credsProvider = new BasicCredentialsProvider();
        this.setConnectTimeout(60000);
        this.setReadTimeout(0);
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
}