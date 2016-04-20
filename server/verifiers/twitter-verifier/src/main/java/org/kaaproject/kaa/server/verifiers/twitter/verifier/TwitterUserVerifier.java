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

package org.kaaproject.kaa.server.verifiers.twitter.verifier;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
import org.kaaproject.kaa.server.verifiers.twitter.config.gen.TwitterAvroConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class TwitterUserVerifier extends AbstractKaaUserVerifier<TwitterAvroConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(TwitterUserVerifier.class);
    private static final String TWITTER_PATH = "/1.1/account/verify_credentials.json";
    private static final String SIGNATURE_METHOD = "HMAC-SHA1";
    private static final String ENCRYPTION_ALGO = "HmacSHA1";
    private static final String REQUEST_METHOD = "GET";
    private static final String REQUEST_HEADER_NAME = "Authorization";
    private static final String ID = "id_str";
    private static final String ERRORS = "errors";
    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String INVALID_TOKEN_CODE = "89";
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_UNATHORIZED = 401;
    private static final long MAX_SEC_TWITTER_REQUEST_TIME = 60;
    private static final HttpHost TWITTER_HOST = new HttpHost("api.twitter.com", 443, "https");
    private static OAuthHeaderBuilder oAuthHeaderBuilder;
    private TwitterAvroConfig configuration;
    private ExecutorService tokenVerifiersPool;
    private CloseableHttpClient httpClient;

    @Override
    public void init(UserVerifierContext context, TwitterAvroConfig configuration) {
        LOG.info("Initializing tiwtter user verifier with context {} and configuration {}", context, configuration);
        this.configuration = configuration;
    }

    @Override
    public void checkAccessToken(String userExternalId, String tokenAndSecret, UserVerifierCallback callback) {
        tokenVerifiersPool.submit(new TokenVerifier(userExternalId, tokenAndSecret, callback));
    }

    private class TokenVerifier implements Runnable {
        private final String userExternalId;
        private final String tokenAndSecret;
        private final UserVerifierCallback callback;

        public TokenVerifier(String userExternalId, String tokenAndSecret, UserVerifierCallback callback) {
            this.userExternalId = userExternalId;
            this.tokenAndSecret = tokenAndSecret;
            this.callback = callback;
        }

        @Override
        public void run() {
            LOG.trace("Started twitter token verification of [{}] tokenAndSecret", tokenAndSecret);
            CloseableHttpResponse closeableHttpResponse = null;

            try {
                closeableHttpResponse = establishConnection(tokenAndSecret);
                LOG.trace("Connection established [{}]", tokenAndSecret);
                int responseCode = closeableHttpResponse.getStatusLine().getStatusCode();
                if (responseCode == HTTP_BAD_REQUEST || responseCode == HTTP_UNATHORIZED) {
                    handleBadResponse(closeableHttpResponse, callback);
                } else if (responseCode == HTTP_OK) {
                    handleResponse(closeableHttpResponse, userExternalId, callback, tokenAndSecret);
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

    private void handleResponse(CloseableHttpResponse connection, String userExternalId, UserVerifierCallback callback,
                                String userAccessToken) throws IOException {
        Map<String, Object> responseMap = getResponseMap(connection.getEntity().getContent());
        String receivedUserId = String.valueOf(responseMap.get(ID));

        if (!receivedUserId.equals(userExternalId)) {
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
        List<Map<String, Object>> errorList = (List<Map<String, Object>>) responseMap.get(ERRORS);

        // there is only one error in the list
        Map<String, Object> error = errorList.get(0);

        if (INVALID_TOKEN_CODE.equals(String.valueOf(error.get(CODE)))) {
            LOG.trace("Access Token is invalid or expired");
            callback.onTokenInvalid();
        } else {
            LOG.trace("Unable to verify token. Error code: [{}], message[{}]", error.get(CODE), error.get(MESSAGE));
            callback.onVerificationFailure("Unable to verify token. Error code: " + error.get(CODE) +
                    ", message: " + error.get(MESSAGE));
        }
    }

    private Map<String, Object> getResponseMap(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            ObjectMapper responseMapper = new ObjectMapper();
            return responseMapper.readValue(reader.readLine(), Map.class);
        }
    }

    protected CloseableHttpResponse establishConnection(String tokenAndSecret) throws IOException,
            NoSuchAlgorithmException, InvalidKeyException {
        HttpRequest request = null;
        request = new BasicHttpEntityEnclosingRequest(REQUEST_METHOD, TWITTER_PATH);

        String[] tokenThenSecret = tokenAndSecret.split(" ");   // now user access token is tokenThenSecret[0]
                                                                // and user secret is tokenAndSecret[1]
        request.setHeader(REQUEST_HEADER_NAME,
                    oAuthHeaderBuilder.generateHeader(tokenThenSecret[0], tokenThenSecret[1]));

        return httpClient.execute(TWITTER_HOST, request);
    }

    @Override
    public void start() {
        LOG.info("twitter user verifier started");
        tokenVerifiersPool = new ThreadPoolExecutor(0, configuration.getMaxParallelConnections(),
                MAX_SEC_TWITTER_REQUEST_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
        // Increase max total connection
        connectionManager.setMaxTotal(configuration.getMaxParallelConnections());
        oAuthHeaderBuilder = new OAuthHeaderBuilder(SIGNATURE_METHOD, REQUEST_METHOD,
                configuration.getTwitterVerifyUrl(), ENCRYPTION_ALGO,
                configuration.getConsumerKey(), configuration.getConsumerSecret());
    }

    @Override
    public void stop() {
        LOG.info("stopping twitter verifier");
        tokenVerifiersPool.shutdown();
        try {
            httpClient.close();
        } catch (IOException e) {
            LOG.debug("Unable to close HttpClient ", e);
        }
        LOG.info("twitter user verifier stopped");
    }

    @Override
    public Class<TwitterAvroConfig> getConfigurationClass() {
        return TwitterAvroConfig.class;
    }
}
