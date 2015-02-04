package org.kaaproject.kaa.server.verifiers.gplus.verifier;

import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.server.common.verifier.AbstractKaaUserVerifier;
import org.kaaproject.kaa.server.common.verifier.UserVerifierCallback;
import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
import org.kaaproject.kaa.server.verifiers.gplus.config.gen.GplusAvroConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GplusUserVerifier extends AbstractKaaUserVerifier<GplusAvroConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(GplusUserVerifier.class);
    private static final String GOOGLE_OAUTH = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=";

    private ExecutorService thredPool;


    @Override
    public void init(UserVerifierContext context, GplusAvroConfig configuration) {
        thredPool = new ThreadPoolExecutor(configuration.getMinParallelConnections(), configuration.getMaxParallelConnections(),
                configuration.getKeepAliveTimeMilliseconds(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void checkAccessToken(String userExternalId, String accessToken, UserVerifierCallback callback) {

        URL url = null;
        try {
            url = new URL(GOOGLE_OAUTH + URLEncoder.encode(accessToken,"UTF-8"));
        } catch (MalformedURLException e) {
            LOG.debug("message", e);
        } catch (UnsupportedEncodingException e) {
            LOG.debug("message", e);
        }
        thredPool.submit(new RunnableVerifier(url, callback, userExternalId));

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
            BufferedReader br = null;
            String line;
            StringBuilder responseJson = new StringBuilder();

            try {
                HttpURLConnection connection = establishConnection(url);
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();

                if (responseCode == 400) {
                    callback.onTokenInvalid();
                    return;
                }

                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        responseJson.append(line);
                    }
                    br.close();

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
            }

            callback.onConnectionError();
        }
    }

    protected HttpURLConnection establishConnection(URL url) {
        HttpURLConnection connection =null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            LOG.debug("message", e);
        }
        return connection;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        thredPool.shutdown();
    }

    @Override
    public Class<GplusAvroConfig> getConfigurationClass() {
        return GplusAvroConfig.class;
    }
}
