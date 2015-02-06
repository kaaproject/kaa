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
package org.kaaproject.kaa.server.verifiers.facebook.verifier;

import org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
import org.kaaproject.kaa.server.verifiers.facebook.config.gen.FacebookAvroConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.*;

public class FacebookUserVerifier extends AbstractKaaUserVerifier<FacebookAvroConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(FacebookUserVerifier.class);
    private static final String FACEBOOK_URL_PREFIX = "https://graph.facebook.com/debug_token";
    private static final long MAX_SEC_FACEBOOK_REQUEST_TIME = 60;
    private FacebookAvroConfig configuration;
    private ExecutorService tokenVerifiersPool;
    private static ObjectMapper responseMapper;

    @Override
    public void init(UserVerifierContext context, FacebookAvroConfig configuration) {
        LOG.info("Initializing facebook user verifier with context {} and configuration {}", context, configuration);
        this.configuration = configuration;
    }

    @Override
    public void checkAccessToken(String userExternalId, String userAccessToken, UserVerifierCallback callback) {
        tokenVerifiersPool.submit(new TokenVerifier(userExternalId, userAccessToken, callback, configuration));
    }

    private class TokenVerifier implements Runnable {
        private final String userExternalId;
        private final String userAccessToken;
        private final UserVerifierCallback callback;
        private final FacebookAvroConfig config;

        public TokenVerifier(String userExternalId, String userAccessToken,
                             UserVerifierCallback callback, FacebookAvroConfig config) {
            this.userExternalId = userExternalId;
            this.userAccessToken = userAccessToken;
            this.callback = callback;
            this.config = config;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            String accessToken = config.getAppId() + "|" + config.getAppSecret();
            LOG.trace("Started token verification with accessToken [{}]", accessToken);
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                connection = establishConnection(userAccessToken, accessToken);
                
                LOG.trace("Connection established [{}]", accessToken);
                
                responseMapper = new ObjectMapper();

                // no data field means that token is invalid
                if (connection.getResponseCode() == 400) {
                    LOG.trace("400: The request could not be understood by the verifier due to malformed syntax");
                    callback.onVerificationFailure("400: The request could not be understood by the verifier due" +
                                                   " to malformed syntax");
                } else if (connection.getResponseCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    // we always get a map
                    Map<String, Object> responseMap =
                            responseMapper.readValue(reader.readLine(), Map.class);

                    Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");
                    String receivedUserId = String.valueOf(dataMap.get("user_id"));
                    if (receivedUserId == null) {
                        Map<String, Object> errorMap = (Map<String, Object>) dataMap.get("error");
                        LOG.trace("Bad input token: {}, errcode = {}", errorMap.get("message"), errorMap.get("code"));
                        callback.onVerificationFailure("Bad input token: " + errorMap.get("message") +
                                ", errcode = " + errorMap.get("code"));
                    } else if (!receivedUserId.equals(userExternalId)) {
                        LOG.trace("Input token doesn't belong to the user with {} id", userExternalId);
                        callback.onVerificationFailure("User access token " + userAccessToken + " doesn't belong to the user");
                    } else {
                        LOG.trace("Input token is confirmed and belongs to the user with {} id", userExternalId);
                        callback.onSuccess();
                    }
                } else {
                    LOG.trace("Server response code: {}, no data can be retrieved", connection.getResponseCode());
                    callback.onVerificationFailure("Server response code:" + connection.getResponseCode()
                            + ", no data can be retrieved");
                }
            } catch (MalformedURLException e) {
                LOG.debug("Internal error", e);
                // should be unreachable, as URL is correct
                callback.onInternalError(e.getMessage());
            } catch (IOException e) {
                LOG.debug("Connection error", e);
                callback.onConnectionError(e.getMessage());
            } catch (Exception e) {
                LOG.debug("Unexpected error", e);
                callback.onInternalError(e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOG.debug("message", e);
                    }
                }
            }
        }
    }

    protected HttpURLConnection establishConnection(String userAccessToken, String accessToken) throws IOException {
        URL myUrl = new URL(FACEBOOK_URL_PREFIX + "?" +
                            "input_token=" + userAccessToken +
                            "&access_token=" + accessToken);

        return (HttpURLConnection) myUrl.openConnection();
    }

    @Override
    public void start() {
        LOG.info("facebook user verifier started");
        tokenVerifiersPool = new ThreadPoolExecutor(0, configuration.getMaxParallelConnections(),
                MAX_SEC_FACEBOOK_REQUEST_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void stop() {
        LOG.info("stopping facebook verifier");
        tokenVerifiersPool.shutdown();
        LOG.info("facebook user verifier stopped");
    }

    @Override
    public Class<FacebookAvroConfig> getConfigurationClass() {
        return FacebookAvroConfig.class;
    }
}
