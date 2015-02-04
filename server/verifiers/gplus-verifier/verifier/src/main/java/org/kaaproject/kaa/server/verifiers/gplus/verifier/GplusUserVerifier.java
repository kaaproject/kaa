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
    private static final String GOOGLE_OAUTH = "https://www.googleapis.com/oauth2/v1/tokeninfo";

    private ExecutorService threadPool;


    @Override
    public void init(UserVerifierContext context, GplusAvroConfig configuration) {
        threadPool = new ThreadPoolExecutor(configuration.getMinParallelConnections(), configuration.getMaxParallelConnections(),
                configuration.getKeepAliveTimeMilliseconds(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void checkAccessToken(String userExternalId, String accessToken, UserVerifierCallback callback) {

        URL url = null;
        try {
            url = new URL(GOOGLE_OAUTH);
        } catch (MalformedURLException e) {
            LOG.debug("message", e);
        }
        threadPool.submit(new RunnableVerifier(url, callback, userExternalId, accessToken));

    }


    private class RunnableVerifier implements Runnable {

        private URL url;
        private UserVerifierCallback callback;
        private String userExternalId;
        private String accessToken;

        public RunnableVerifier(URL url, UserVerifierCallback callback, String userExternalId, String accessToken) {
            this.url = url;
            this.callback = callback;
            this.userExternalId = userExternalId;
            this.accessToken = accessToken;
        }

        @Override
        public void run() {
            BufferedReader bufferedReader = null;
            String line;
            StringBuilder responseJson = new StringBuilder();

            HttpURLConnection connection = null;
            try {
                connection = establishConnection(url);

                try {
                    connection.setRequestMethod("POST");
                } catch (ProtocolException e) {
                    LOG.debug("message", e);
                }

                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                String urlParameters = "access_token=" + accessToken;

                connection.setRequestProperty("Content-Length", "" +
                        Integer.toString(urlParameters.getBytes().length));

                try {
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();
                } catch (IOException e) {
                    LOG.debug("message", e);
                }


                int responseCode = connection.getResponseCode();

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
                if(null!=bufferedReader){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            callback.onConnectionError();
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

    }

    @Override
    public void stop() {
        threadPool.shutdown();
    }

    @Override
    public Class<GplusAvroConfig> getConfigurationClass() {
        return GplusAvroConfig.class;
    }
}
