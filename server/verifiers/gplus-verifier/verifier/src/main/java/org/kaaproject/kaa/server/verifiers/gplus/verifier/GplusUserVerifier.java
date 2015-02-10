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

package org.kaaproject.kaa.server.verifiers.gplus.verifier;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
import org.kaaproject.kaa.server.verifiers.gplus.config.gen.GplusAvroConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GplusUserVerifier extends AbstractKaaUserVerifier<GplusAvroConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(GplusUserVerifier.class);
    private static final String GOOGLE_OAUTH = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=";
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private GplusAvroConfig configuration;
    private ExecutorService threadPool;
    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager conManager;

    @Override
    public void init(UserVerifierContext context, GplusAvroConfig configuration) {
        LOG.info("Initializing user verifier with context {} and configuration {}", context, configuration);
        this.configuration = configuration;

    }

    @Override
    public void checkAccessToken(String userExternalId, String accessToken, UserVerifierCallback callback) {


        URI uri = null;
        try {
            uri = new URI(GOOGLE_OAUTH + accessToken);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        threadPool.submit(new RunnableVerifier(uri, callback, userExternalId));

    }


    private class RunnableVerifier implements Runnable {

        private final URI uri;
        private final UserVerifierCallback callback;
        private final String userExternalId;

        public RunnableVerifier(URI uri, UserVerifierCallback callback, String userExternalId) {
            this.uri = uri;
            this.callback = callback;
            this.userExternalId = userExternalId;
        }

        @Override
        public void run() {

            CloseableHttpResponse closeableHttpResponse = null;
            try {

                String responseJson = "";
                int responseCode;

                closeableHttpResponse = establishConnection(uri);

                responseCode = closeableHttpResponse.getStatusLine().getStatusCode();

                if (responseCode == HTTP_OK) {
                    responseJson = readResponse(closeableHttpResponse.getEntity().getContent());
                    ObjectMapper mapper = new ObjectMapper();
                    Map map = mapper.readValue(responseJson, Map.class);
                    String userId = String.valueOf(map.get("user_id"));
                    if (!userExternalId.equals(userId)) {
                        callback.onVerificationFailure("User access token doesn't belong to the user");
                        LOG.trace("Input token doesn't belong to the user with {} id", userExternalId);
                    } else {
                        callback.onSuccess();
                        LOG.trace("Input token is confirmed and belongs to the user with {} id", userExternalId);
                    }
                }
                if (responseCode == HTTP_BAD_REQUEST) {
                    callback.onTokenInvalid();
                    LOG.trace("Server auth error: {}", readResponse(closeableHttpResponse.getEntity().getContent()));
                } else {
                    callback.onInternalError();
                    LOG.trace("Server returned the following error code: {}", responseCode);
                }

            } catch (IOException e) {
                LOG.warn("Internal error: ", e);
            } finally {
                try {
                    closeableHttpResponse.close();
                } catch (IOException e) {
                    LOG.warn("Internal error: ", e);
                }
            }

        }
    }

    private String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }

        byte[] bytes = bos.toByteArray();
        bos.close();
        return new String(bytes, "UTF-8");
    }


    protected CloseableHttpResponse establishConnection(URI uri) {
        CloseableHttpResponse closeableHttpResponse = null;
        try {
            closeableHttpResponse = httpClient.execute(new HttpGet(uri));
        } catch (IOException e) {
            LOG.warn("Internal error: ", e);
        }
        return closeableHttpResponse;
    }

    @Override
    public void start() {
        LOG.info("user verifier started");
        threadPool = new ThreadPoolExecutor(configuration.getMinParallelConnections(), configuration.getMaxParallelConnections(),
                configuration.getKeepAliveTimeMilliseconds(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        conManager = new PoolingHttpClientConnectionManager();
        conManager.setMaxTotal(configuration.getMaxParallelConnections());

        httpClient = HttpClients.custom().setConnectionManager(conManager).build();
    }

    @Override
    public void stop() {
        threadPool.shutdown();
        try {
            conManager.shutdown();
            httpClient.close();
        } catch (IOException e) {
            LOG.warn("Internal error: ", e);
        }
        LOG.info("user verifier stopped");
    }

    @Override
    public Class<GplusAvroConfig> getConfigurationClass() {
        return GplusAvroConfig.class;
    }
}
