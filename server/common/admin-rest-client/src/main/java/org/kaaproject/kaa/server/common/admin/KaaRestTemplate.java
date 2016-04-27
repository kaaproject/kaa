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

package org.kaaproject.kaa.server.common.admin;

import java.io.IOException;
import java.net.URI;
import java.util.Random;

import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class KaaRestTemplate extends RestTemplate {

    private static final Logger logger = LoggerFactory.getLogger(AdminClient.class);

    private static final int DEFAULT_PORT = 80;

    private String[] hosts;

    private int[] ports;

    private String currentUrl;

    private String username;

    private String password;

    private static final String restApiSuffix = "/kaaAdmin/rest/api/";

    private int index;

    public KaaRestTemplate(String host, int port) {
        checkHostPortLists(new String[] { host }, new int[] { port });
    }

    /**
     * Initialize KaaRestTempalte using following format host1:port1,host2:port2
     *
     * @param hostPortList
     */
    public KaaRestTemplate(String hostPortList) {
        if (hostPortList == null) {
            throw new IllegalArgumentException("String of addresses must be not null");
        }

        String[] splitedAddresses = hostPortList.split(",");

        String[] hosts = new String[splitedAddresses.length];
        int[] ports = new int[splitedAddresses.length];

        for (int i = 0; i < hosts.length; i++) {
            String[] separatedAddresses = splitedAddresses[i].split(":");
            hosts[i] = separatedAddresses[0];
            if (separatedAddresses.length == 2) {
                ports[i] = Integer.parseInt(separatedAddresses[1]);
            } else {
                ports[i] = DEFAULT_PORT;
            }
        }
        checkHostPortLists(hosts, ports);
    }

    private void checkHostPortLists(String[] hosts, int[] ports) {
        if ((hosts.length != ports.length) && (hosts != null)) {
            throw new IllegalArgumentException("Length of arrays of hosts and ports must be the same length and not null");
        } else {
            this.hosts = hosts;
            this.ports = ports;
            index = new Random().nextInt(hosts.length);
            setNewRequestFactory(index);
        }
    }

    public String getUrl() {
        return currentUrl;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor)
            throws ResourceAccessException {
        int maxRetry = hosts.length;
        while (true) {
            try {
                return super.doExecute(url, method, requestCallback, responseExtractor);
            } catch (ResourceAccessException ex) {
                logger.info("Connect to ({}:{}) failed", getCurHost(), getCurPort(), ex);
                boolean isRequestFactorySet = false;
                while (!isRequestFactorySet) {
                    if (index < hosts.length - 1) {
                        index++;
                    } else {
                        index = 0;
                    }
                    logger.info("Trying connect to ({}:{})", getCurHost(), getCurPort(), ex);
                    if (maxRetry <= 0) {
                        logger.error("Failed to connect to ({}:{})", getCurHost(), getCurPort(), ex);
                        throw new ResourceAccessException(
                                "I/O error on " + method.name() + " request for \"" + url + "\":" + ex.getMessage(), new IOException(ex));
                    } else {
                        maxRetry--;
                    }
                    try {
                        setNewRequestFactory(index);
                    } catch (Exception e) {
                        logger.info("Failed to initialize new request factory ({}:{})", getCurHost(), getCurPort(), e);
                        continue;
                    }
                    url = updateURL(url);
                    isRequestFactorySet = true;
                }
            } catch (RestClientException e) {
                throw e;
            }
        }
    }

    private URI updateURL(URI url) {
        String currentURI = url.toString();

        int sufixPartIdx = currentURI.indexOf(restApiSuffix);

        String defaultURIPartWithVariableHostPort = currentURI.substring(0, sufixPartIdx);
        String sufixPart = currentURI.substring(sufixPartIdx);

        defaultURIPartWithVariableHostPort = defaultURIPartWithVariableHostPort.replaceFirst(url.getHost(), getCurHost());
        defaultURIPartWithVariableHostPort = defaultURIPartWithVariableHostPort.replaceFirst(String.valueOf(url.getPort()),
                String.valueOf(getCurPort()));

        return URI.create(defaultURIPartWithVariableHostPort + sufixPart);
    }

    private int getCurPort() {
        return ports[index];
    }

    private String getCurHost() {
        return hosts[index];
    }

    private void setNewRequestFactory(int index) {
        String host = hosts[index];
        int port = ports[index];
        setRequestFactory(new HttpComponentsRequestFactoryBasicAuth(new HttpHost(host, port, "http")));
        currentUrl = "http://" + host + ":" + port + restApiSuffix;
        if (username != null && password != null) {
            login(username, password);
        }
    }

    public void login(String username, String password) {
        this.username = username;
        this.password = password;
        HttpComponentsRequestFactoryBasicAuth requestFactory = (HttpComponentsRequestFactoryBasicAuth) getRequestFactory();
        requestFactory.setCredentials(username, password);
    }

}
