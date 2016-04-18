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

package org.kaaproject.kaa.server.verifiers.facebook.verifier;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
import org.kaaproject.kaa.server.verifiers.facebook.config.gen.FacebookAvroConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Map;
import java.util.concurrent.*;

public class FacebookUserVerifier extends AbstractKaaUserVerifier<FacebookAvroConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(FacebookUserVerifier.class);
    private static final String FACEBOOK_URI_SCHEME = "https";
    private static final String FACEBOOK_URI_AUTHORITY = "graph.facebook.com";
    private static final String FACEBOOK_URI_PATH = "/debug_token";
    private static final long MAX_SEC_FACEBOOK_REQUEST_TIME = 60;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_OK = 200;
    private static final String OAUTH_ERROR = "190";
    private static final String TOKEN_EXPIRED = "463";
    private static final String TOKEN_INVALID = "467";
    private static final String DATA = "data";
    private static final String USER_ID = "user_id";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private static final String CODE = "code";
    private static final String ERRCODE = "errcode";
    private static final String ERRROR_SUBCODE = "error_subcode";
    private FacebookAvroConfig configuration;
    private ExecutorService tokenVerifiersPool;
    private CloseableHttpClient httpClient;

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
        public void run() {
            String accessToken = config.getAppId() + "|" + config.getAppSecret();
            LOG.trace("Started token verification with accessToken [{}]", accessToken);
            CloseableHttpResponse closeableHttpResponse = null;

            try {
                closeableHttpResponse = establishConnection(userAccessToken, accessToken);
                LOG.trace("Connection established [{}]", accessToken);

                int responseCode = closeableHttpResponse.getStatusLine().getStatusCode();

                if (responseCode == HTTP_BAD_REQUEST) {
                    handleBadResponse(closeableHttpResponse, callback);
                } else if (responseCode == HTTP_OK) {
                    handleResponse(closeableHttpResponse, userExternalId, callback, userAccessToken);
                } else {                                                // other response codes
                    LOG.warn("Server response code: {}, no data can be retrieved", responseCode);
                    callback.onVerificationFailure("Server response code:" + responseCode
                            + ", no data can be retrieved");
                }
            } catch (IOException e) {
                LOG.debug("Connection error", e);
                callback.onConnectionError(e.getMessage());
            } catch (Exception e) {
                LOG.debug("Unexpected error", e);
                callback.onInternalError(e.getMessage());
            } finally {
                if (closeableHttpResponse != null) {
                    try {
                        closeableHttpResponse.close();
                    } catch (IOException e) {
                        LOG.debug("Connection error: can't close CloseableHttpResponse ", e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleResponse(CloseableHttpResponse connection, String userExternalId, UserVerifierCallback callback,
                                String userAccessToken) throws IOException {
        Map<String, Object> responseMap = getResponseMap(connection.getEntity().getContent());
        Map<String, Object> dataMap = (Map<String, Object>) responseMap.get(DATA);
        String receivedUserId = String.valueOf(dataMap.get(USER_ID));

        if (dataMap.containsKey(ERROR) || receivedUserId == null) {
            Map<String, Object> errorMap = (Map<String, Object>) dataMap.get(ERROR);
            LOG.warn("Bad input token: {}, errcode = {}", errorMap.get(MESSAGE), errorMap.get(CODE));
            callback.onTokenInvalid();
        } else if (!receivedUserId.equals(userExternalId)) {
            LOG.warn("Input token doesn't belong to the user with {} id", userExternalId);
            callback.onVerificationFailure("User access token " + userAccessToken + " doesn't belong to the user");
        } else {
            LOG.trace("Input token is confirmed and belongs to the user with {} id", userExternalId);
            callback.onSuccess();
        }
    }

    @SuppressWarnings("unchecked")
    private void handleBadResponse(CloseableHttpResponse connection, UserVerifierCallback callback) throws IOException {
        Map<String, Object> responseMap = getResponseMap(connection.getEntity().getContent());
        Map<String, Object> errorMap = null;

        // no error field in response
        if (responseMap.get(ERROR) != null) {
            errorMap = (Map<String, Object>) responseMap.get(ERROR);
        }

        // errors with OAuth
        if (errorMap != null && String.valueOf(errorMap.get(CODE)).equals(OAUTH_ERROR)) {
            if (errorMap.get(ERRROR_SUBCODE) == null) {
                LOG.trace("OAuthException: [{}], errcode: [{}], errsubcode: [{}] ", errorMap.get(MESSAGE),
                        errorMap.get(ERRCODE), errorMap.get(ERRROR_SUBCODE));
                callback.onVerificationFailure("OAuthException:" + errorMap.get(MESSAGE));
            } else if (String.valueOf(errorMap.get(ERRROR_SUBCODE)).equals(TOKEN_EXPIRED)) {         // access token has expired
                LOG.trace("Access Token has expired");
                    callback.onTokenExpired();
            } else if (String.valueOf(errorMap.get(ERRROR_SUBCODE)).equals(TOKEN_INVALID)) {        // access token is invalid
                LOG.trace("Access Token is invalid");
                callback.onTokenInvalid();
            } else {
                LOG.trace("OAuthException: [{}], errcode: [{}], errsubcode: [{}] ", errorMap.get(MESSAGE),
                        errorMap.get(ERRCODE), errorMap.get(ERRROR_SUBCODE));
                callback.onVerificationFailure("OAuthException:" + errorMap.get(MESSAGE));
            }
        } else {
            if (errorMap != null) {
                LOG.trace("Unable to verify token: {}, errcode: [{}]", errorMap.get(MESSAGE),
                        errorMap.get(ERRCODE));
                callback.onVerificationFailure("Unable to verify token: " + errorMap.get(MESSAGE) +
                        ", errorcode: " + errorMap.get(ERRCODE));
            } else {
                LOG.trace("Unable to verify token. HTTP response 400");
                callback.onVerificationFailure("Unable to verify token. HTTP response 400");
            }
        }
    }

    private Map<String, Object> getResponseMap(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            ObjectMapper responseMapper = new ObjectMapper();
            return responseMapper.readValue(reader.readLine(), Map.class);
        }
    }

    protected CloseableHttpResponse establishConnection(String userAccessToken, String accessToken) throws IOException {
        URI uri = null;
        try {
            String facebookUriQuery = "input_token=" + userAccessToken + "&access_token=" + accessToken;
            uri = new URI(FACEBOOK_URI_SCHEME, FACEBOOK_URI_AUTHORITY, FACEBOOK_URI_PATH, facebookUriQuery, null);
        } catch (URISyntaxException e) {
            LOG.debug("Malformed URI", e);
        }
        return httpClient.execute(new HttpGet(uri));
    }

    @Override
    public void start() {
        LOG.info("facebook user verifier started");
        tokenVerifiersPool = new ThreadPoolExecutor(0, configuration.getMaxParallelConnections(),
                MAX_SEC_FACEBOOK_REQUEST_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
        // Increase max total connection
        connectionManager.setMaxTotal(configuration.getMaxParallelConnections());
    }

    @Override
    public void stop() {
        LOG.info("stopping facebook verifier");
        tokenVerifiersPool.shutdown();
        try {
            httpClient.close();
        } catch (IOException e) {
            LOG.debug("Unable to close HttpClient ", e);
        }
        LOG.info("facebook user verifier stopped");
    }

    @Override
    public Class<FacebookAvroConfig> getConfigurationClass() {
        return FacebookAvroConfig.class;
    }
}
