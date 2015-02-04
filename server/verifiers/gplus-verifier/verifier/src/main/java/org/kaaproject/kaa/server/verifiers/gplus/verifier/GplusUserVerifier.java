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
            LOG.debug("message", e);
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
            BufferedReader bufferedReader = null;
            String line;
            StringBuilder responseJson = new StringBuilder();
            int responseCode = 0;

            HttpURLConnection connection = null;
            try {
                connection = establishConnection(url);

                connection.setRequestMethod("GET");

                responseCode = connection.getResponseCode();

                if (responseCode == 400) {
                    callback.onTokenInvalid();
                    return;
                }

                if (responseCode == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = bufferedReader.readLine()) != null) {
                        responseJson.append(line);
                    }
                    bufferedReader.close();

                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> map = mapper.readValue(responseJson.toString(), Map.class);
                    String userId = map.get("user_id");

                    if (!userExternalId.equals(userId)) {
                        callback.onVerificationFailure("wrong user id");
                        return;
                    } else {
                        callback.onSuccess();
                        return;
                    }

                }
            } catch (IOException e) {
                LOG.debug("message", e);
            } finally {
                if (null != connection) {
                    connection.disconnect();
                }
                if (null != bufferedReader) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            LOG.trace("Server response code: {} no data can be retrieved" + responseCode);

            callback.onVerificationFailure("Server response code:" + responseCode + ", no data can be retrieved");
        }
    }

    protected HttpURLConnection establishConnection(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            LOG.debug("message", e);
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
