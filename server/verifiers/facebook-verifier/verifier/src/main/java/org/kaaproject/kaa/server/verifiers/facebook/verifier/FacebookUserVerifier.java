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
                ObjectMapper responseMapper = new ObjectMapper();

                if (connection.getResponseCode() == 400) {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));

                    // we always get a map
                    Map<String, Object> responseMap =
                            responseMapper.readValue(reader.readLine(), Map.class);
                    Map<String, Object> errorMap = null;

                    // no error field in response
                    if (responseMap.get("error") != null) {
                        errorMap = (Map<String, Object>) responseMap.get("error");
                    }

                    // errors with OAuth
                    if (errorMap != null && String.valueOf(errorMap.get("code")).equals("190")) {
                        if (errorMap.get("error_subcode") == null) {
                            LOG.trace("OAuthException: [{}], errcode: [{}], errsubcode: [{}] ", errorMap.get("message"),
                                    errorMap.get("errcode"), errorMap.get("error_subcode"));
                            callback.onVerificationFailure("OAuthException:" + errorMap.get("message"));
                        } else if (String.valueOf(errorMap.get("error_subcode")).equals("463")) {         // access token has expired
                            LOG.trace("Access Token has expired");
                            callback.onTokenExpired();
                        } else if (String.valueOf(errorMap.get("error_subcode")).equals("467")) {  // access token is invalid
                            LOG.trace("Access Token is invalid");
                            callback.onTokenInvalid();
                        } else {
                            LOG.trace("OAuthException: [{}], errcode: [{}], errsubcode: [{}] ", errorMap.get("message"),
                                    errorMap.get("errcode"), errorMap.get("error_subcode"));
                            callback.onVerificationFailure("OAuthException:" + errorMap.get("message"));
                        }
                    } else {
                        if (errorMap != null) {
                            LOG.trace("Unable to verify token: {}, errcode: [{}]", errorMap.get("message"),
                                    errorMap.get("errcode"));
                            callback.onVerificationFailure("Unable to verify token: " + errorMap.get("message") +
                                    ", errorcode: " + errorMap.get("errcode"));
                        } else {
                            LOG.trace("Unable to verify token. HTTP response 400");
                            callback.onVerificationFailure("Unable to verify token. HTTP response 400");
                        }
                    }
                } else if (connection.getResponseCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    // we always get a map
                    Map<String, Object> responseMap =
                            responseMapper.readValue(reader.readLine(), Map.class);

                    Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");
                    String receivedUserId = String.valueOf(dataMap.get("user_id"));

                    if (dataMap.containsKey("error") || receivedUserId == null) {
                        Map<String, Object> errorMap = (Map<String, Object>) dataMap.get("error");
                        LOG.trace("Bad input token: {}, errcode = {}", errorMap.get("message"), errorMap.get("code"));
                        callback.onTokenInvalid();
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
                        LOG.debug("Reader can't be closed", e);
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
