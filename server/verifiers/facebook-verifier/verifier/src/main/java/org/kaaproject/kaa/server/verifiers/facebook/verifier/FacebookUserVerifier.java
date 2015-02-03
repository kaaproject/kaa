package org.kaaproject.kaa.server.verifiers.facebook.verifier;

import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
import org.kaaproject.kaa.server.verifiers.facebook.config.gen.FacebookAvroConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.*;

public class FacebookUserVerifier extends AbstractKaaUserVerifier<FacebookAvroConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(FacebookUserVerifier.class);
    private static final String FACEBOOK_URL_PREFIX = "https://graph.facebook.com/debug_token";
    private FacebookAvroConfig configuration;
    private ExecutorService tokenVerifiersPool;
    private static ObjectMapper responseMapper;

    @Override
    public void init(UserVerifierContext context, FacebookAvroConfig configuration) {
        LOG.info("Initializing user verifier with context {} and configuration {}", context, configuration);
        this.configuration = configuration;
        tokenVerifiersPool = new ThreadPoolExecutor(0, configuration.getMaxParallelConnections(),
                            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void checkAccessToken(String userExternalId, String userAccessToken, UserVerifierCallback callback) {
        LOG.trace("Received user verification request for user {} and input token {}", userExternalId, userAccessToken);

        tokenVerifiersPool.submit(new TokenVerifier(userExternalId, userAccessToken, callback, configuration));
    }

    private class TokenVerifier implements Runnable {
        String userExternalId;
        String userAccessToken;
        final UserVerifierCallback callback;
        final FacebookAvroConfig config;

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
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                connection = establishConnection(userAccessToken, accessToken);

                responseMapper = new ObjectMapper();

                // no data field means that token is invalid
                if (connection.getResponseCode() == 400) {
                    LOG.trace("400 Bad request");
                    callback.onVerificationFailure("400 Bad request");
                } else if (connection.getResponseCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    // we always get a map
                    HashMap<String, Object> responseMap =
                            responseMapper.readValue(reader.readLine(), HashMap.class);

                    HashMap<String, Object> dataMap = (HashMap) responseMap.get("data");
                    String receivedUserId = (String) dataMap.get("user_id");
                    if (receivedUserId == null) {
                        HashMap<String, Object> errorMap = (HashMap) dataMap.get("error");
                        LOG.trace("Bad input token: {}, errcode = ", errorMap.get("message"), errorMap.get("code"));
                        callback.onVerificationFailure("Bad input token: " + errorMap.get("message") +
                                ", errcode = " + errorMap.get("code"));
                    } else if (!receivedUserId.equals(userExternalId)) {
                        LOG.trace("Input token {} doesn't belong to the user with {} id", userAccessToken, userExternalId);

                        callback.onVerificationFailure("User access token " + userAccessToken + " doesn't belong to the user");
                    } else {
                        LOG.trace("Input token {} is confirmed and belongs to the user with {} id",
                                userAccessToken, userExternalId);

                        callback.onSuccess();
                    }
                } else {
                    LOG.trace("Server response code: {}, no data can be retrieved" + connection.getResponseCode());

                    callback.onVerificationFailure("Server response code:" + connection.getResponseCode()
                            + ", no data can be retrieved");
                }
            } catch (MalformedURLException e) {
                LOG.debug(e.toString());

                // should be unreachable, as URL is correct
                callback.onVerificationFailure("Internal error: malformed url");
            } catch (IOException e) {
                LOG.debug(e.toString());
                callback.onVerificationFailure("Internal error: IOException");
            } finally {
                if (connection != null) connection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOG.debug(e.toString());
                    }
                }
            }
        }
    }

    protected HttpURLConnection establishConnection(String userAccessToken, String accessToken) throws IOException {
        URL myUrl = new URL(FACEBOOK_URL_PREFIX + "?" +
                            "input_token=" + userAccessToken +
                            "&access_token=" + accessToken);


        HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
        return connection;
    }

    @Override
    public void start() {
        LOG.info("user verifier started");
    }

    @Override
    public void stop() {
        LOG.info("user verifier stopped");
    }

    @Override
    public Class<FacebookAvroConfig> getConfigurationClass() {
        return FacebookAvroConfig.class;
    }

    protected ObjectMapper getResponseMapper() {
        return responseMapper;
    }
}
