package org.kaaproject.kaa.demo.twitterled.monitor;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.JSONArray;
import twitter4j.JSONObject;
import twitter4j.Status;
import twitter4j.UserStreamAdapter;
import twitter4j.UserStreamListener;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.twitter.hbc.twitter4j.Twitter4jUserstreamClient;

public class TwitterMonitor implements ConfigurationListener {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterMonitor.class);

    private static final int EVENT_QUEUE_SIZE = 1000;
    private static final int MESSAGE_QUEUE_SIZE = 1000;

    private final KaaClient client;
    private volatile TwitterMonitorWorker worker;

    public static void main(String[] args) throws Exception {
        TwitterMonitor monitor = new TwitterMonitor(Kaa.newClient(new DesktopKaaPlatformContext()));
        LOG.info("Starting monitor");
        monitor.init();
    }

    public TwitterMonitor(KaaClient client) {
        this.client = client;
    }

    private void init() {
        client.addConfigurationListener(this);
        client.start();
        onConfigurationUpdate(client.getConfiguration());
    }

    @Override
    public void onConfigurationUpdate(TwitterMonitorConfiguration config) {
        if (isValid(config)) {
            if (worker != null) {
                worker.shutdown();
            }
            worker = new TwitterMonitorWorker(config);
            worker.start();
        } else {
            LOG.info("Client have not received valid configuration yet!");
        }
    }

    private static class TwitterMonitorWorker extends Thread {

        private final TwitterMonitorConfiguration config;
        private volatile boolean stopped;

        public TwitterMonitorWorker(TwitterMonitorConfiguration config) {
            this.config = config;
        }

        @Override
        public void run() {
            LOG.info("Starting twitter monitor thread");
            BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(MESSAGE_QUEUE_SIZE);
            try {
                final AdminClient adminClient = buildAdminClient(config.getKaaClientConfiguration());

                final ApplicationDto app = filterApplications(adminClient, config.getKaaClientConfiguration());

                final NotificationSchemaDto nfSchema = fetchNotificationSchema(adminClient, app, config.getKaaClientConfiguration());

                final TopicDto topic = fetchNotificationTopic(adminClient, app, config.getKaaClientConfiguration());

                UserStreamListener listener = new UserStreamAdapter() {
                    
                    @Override
                    public void onStatus(Status status) {
                        LOG.info("Status \n message.text -> {} \n message.user -> {}", status.getText(), status.getUser().getName());
                        NotificationDto notification = new NotificationDto();
                        notification.setApplicationId(app.getId());
                        notification.setSchemaId(nfSchema.getId());
                        notification.setTopicId(topic.getId());
                        notification.setVersion(nfSchema.getMajorVersion());
                        notification.setType(NotificationTypeDto.USER);
                        notification.setExpiredAt(new Date(System.currentTimeMillis() + 60000));
                        String body = null;
                        try {
                            JSONObject object = new JSONObject();
                            JSONArray keywords = new JSONArray();
                            for(String keyword : config.getTwitterClientConfiguration().getFilters()){
                                keywords.put(keyword);
                            }
                            object.put("message", status.getText());
                            object.put("keywords", keywords);
                            object.put("author", status.getUser().getName());
                            body = object.toString();
                            adminClient.sendNotification(notification, "notification", body);
                        } catch (Exception e) {
                            LOG.warn("Failed to send notification : {}", body, e);
                        }
                    }
                };

                Twitter4jUserstreamClient twitterClient = new Twitter4jUserstreamClient(buildTwitterClient(msgQueue,
                        config.getTwitterClientConfiguration()), msgQueue, Arrays.asList(listener), Executors.newSingleThreadExecutor());

                twitterClient.connect();
                twitterClient.process();
                while (!stopped) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOG.info("Worker thread interrupted", e);
                    }
                }
                twitterClient.stop();
                LOG.info("Twitter monitor thread stopped");
            } catch (Exception e) {
                LOG.error("Exception during twitter monitor", e);
            }
        }

        public void shutdown() {
            this.stopped = true;
            LOG.info("Stopping twitter monitor thread");
            interrupt();
        }
    }

    private static ApplicationDto filterApplications(AdminClient adminClient, KaaClientConfiguration config) throws Exception {
        String appToken = config.getAppToken();
        for (ApplicationDto appDto : adminClient.getApplications()) {
            if (appDto.getApplicationToken().equalsIgnoreCase(appToken)) {
                return appDto;
            }
        }
        throw new IllegalArgumentException("App with token " + appToken + " not found");
    }

    private static NotificationSchemaDto fetchNotificationSchema(AdminClient adminClient, ApplicationDto appDto,
            KaaClientConfiguration kaaClientConfiguration) throws Exception {
        int schemaVersion = kaaClientConfiguration.getNfSchemaVersion();
        for (NotificationSchemaDto schemaDto : adminClient.getNotificationSchemas(appDto.getId())) {
            if (schemaDto.getMajorVersion() == schemaVersion) {
                return schemaDto;
            }
        }
        throw new IllegalArgumentException("Notification shema with version " + schemaVersion + " not found");
    }

    private static TopicDto fetchNotificationTopic(AdminClient adminClient, ApplicationDto appDto,
            KaaClientConfiguration kaaClientConfiguration) throws Exception {
        String topicName = kaaClientConfiguration.getTopicName();
        for (TopicDto topicDto : adminClient.getTopics(appDto.getId())) {
            if (topicDto.getName().equalsIgnoreCase(topicName)) {
                return topicDto;
            }
        }
        throw new IllegalArgumentException("Topic with name" + topicName + " not found");
    }

    protected static AdminClient buildAdminClient(KaaClientConfiguration configuration) {
        AdminClient adminClient = new AdminClient(configuration.getHost(), configuration.getPort());
        adminClient.login(configuration.getLogin(), configuration.getPassword());
        return adminClient;
    }

    protected static Client buildTwitterClient(BlockingQueue<String> msgQueue, TwitterClientConfiguration configuration) {
        BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(EVENT_QUEUE_SIZE);

        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        
        hosebirdEndpoint.trackTerms(configuration.getFilters());

        Authentication hosebirdAuth = new OAuth1(configuration.getConsumerKey(), configuration.getConsumerSecret(),
                configuration.getToken(), configuration.getTokenSecret());

        ClientBuilder builder = new ClientBuilder().name("Kaa-Twitter-Monitor-Client").hosts(hosebirdHosts).authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint).processor(new StringDelimitedProcessor(msgQueue)).eventMessageQueue(eventQueue);

        Client hosebirdClient = builder.build();
        return hosebirdClient;
    }

    /**
     * Check if developer updated all required fields
     * 
     * @param configuration
     * @return
     */
    private boolean isValid(TwitterMonitorConfiguration configuration) {
        TwitterClientConfiguration twitterConf = configuration.getTwitterClientConfiguration();
        KaaClientConfiguration kaaConf = configuration.getKaaClientConfiguration();
        return !twitterConf.getToken().isEmpty() && !twitterConf.getTokenSecret().isEmpty() && !twitterConf.getConsumerKey().isEmpty()
                && !twitterConf.getConsumerSecret().isEmpty() && !twitterConf.getFilters().isEmpty() && !kaaConf.getHost().isEmpty()
                && kaaConf.getPort() > 0 && !kaaConf.getLogin().isEmpty() && !kaaConf.getPassword().isEmpty()
                && !kaaConf.getAppToken().isEmpty() && !kaaConf.getTopicName().isEmpty() && kaaConf.getNfSchemaVersion() > 1;
    }
}
