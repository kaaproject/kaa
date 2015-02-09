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

import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
import org.kaaproject.kaa.server.verifiers.gplus.config.gen.GplusAvroConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GplusUserVerifier extends AbstractKaaUserVerifier<GplusAvroConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(GplusUserVerifier.class);
    private static final String GOOGLE_OAUTH = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=";
    private GplusAvroConfig configuration;
    private ExecutorService threadPool;


    @Override
    public void init(UserVerifierContext context, GplusAvroConfig configuration) {
        LOG.info("Initializing user verifier with context {} and configuration {}", context, configuration);
        this.configuration = configuration;

    }

    @Override
    public void checkAccessToken(String userExternalId, String accessToken, UserVerifierCallback callback) {

        URL url = null;
        try {
            url = new URL(GOOGLE_OAUTH + accessToken);
        } catch (MalformedURLException e) {
            callback.onVerificationFailure("Internal error: malformed url");
            LOG.debug("Internal error: malformed url", e);
        }
        threadPool.submit(new RunnableVerifier(url, callback, userExternalId));

    }


    private class RunnableVerifier implements Runnable {

        private URL url;
        private UserVerifierCallback callback;
        private String userExternalId;

        public RunnableVerifier(URL url, UserVerifierCallback callback, String userExternalId) {
            this.url = url;
            this.callback = callback;
            this.userExternalId = userExternalId;
        }

        @Override
        public void run() {

            HttpURLConnection connection = null;
            try {

                String responseJson = "";
                int responseCode;
                connection = establishConnection(url);
                connection.setRequestMethod("GET");
                responseCode = connection.getResponseCode();

                if (responseCode == 200) {
                    responseJson = readResponse(connection.getInputStream());
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> map = mapper.readValue(responseJson.toString(), Map.class);
                    String userId = String.valueOf(map.get("user_id"));
                    if (!userExternalId.equals(userId)) {
                        callback.onVerificationFailure("User access token doesn't belong to the user");
                        LOG.trace("Input token doesn't belong to the user with {} id", userExternalId);
                    } else {
                        callback.onSuccess();
                        LOG.trace("Input token is confirmed and belongs to the user with {} id", userExternalId);
                    }
                } if (responseCode == 400) {
                    callback.onTokenInvalid();
                    LOG.trace("Server auth error: {}", readResponse(connection.getErrorStream()));
                } else {
                    callback.onInternalError();
                    LOG.trace("Server returned the following error code: {}", responseCode);
                }


            } catch (IOException e) {
                LOG.debug("Internal error: ", e);
            } finally {
                if (null != connection) {
                    connection.disconnect();
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

        byte [] bytes = bos.toByteArray();
        bos.close();
        return new String(bytes, "UTF-8");
    }


    protected HttpURLConnection establishConnection(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            LOG.debug("Establishing connection failed", e);
        }
        return connection;
    }

    @Override
    public void start() {
        LOG.info("user verifier started");
        threadPool = new ThreadPoolExecutor(configuration.getMinParallelConnections(), configuration.getMaxParallelConnections(),
                configuration.getKeepAliveTimeMilliseconds(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void stop() {
        threadPool.shutdown();
        LOG.info("user verifier stopped");
    }

    @Override
    public Class<GplusAvroConfig> getConfigurationClass() {
        return GplusAvroConfig.class;
    }
}
