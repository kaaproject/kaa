/*
 * Copyright 2014-2015 CyberVision, Inc.
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


package org.kaaproject.kaa.demo.twitterled.board;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.notification.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoardController extends Thread implements NotificationListener, ConfigurationListener {
    private static final Logger LOG = LoggerFactory.getLogger(BoardController.class);

    private static final String LED_MATRIX_COMMAND = "sudo /home/pi/display16x32/rpi-rgb-led-matrix/led-matrix -r 16 -D 1 -m ";
    private static final String DEFAULT_FILE_NAME = "default.ppm";
    private static final String CUSTOM_FILE_NAME = "text.ppm";
    private static final int MIN_REPEAT_COUNT = 1;
    private static final int MAX_MESSAGES_IN_QUEUE = 1;
    private volatile boolean stopped = false;
    private volatile TwitterBoardConfiguration configuration;
    private volatile Process displayProcess;

    private final BlockingQueue<TwitterBoardNotification> queue;
    private final BlockingQueue<TwitterBoardUpdate> updates;

    public BoardController(TwitterBoardConfiguration configuration) {
        this.configuration = configuration;
        this.queue = new LinkedBlockingQueue<TwitterBoardNotification>();
        this.updates = new LinkedBlockingQueue<BoardController.TwitterBoardUpdate>();
    }

    public void run() {
        generateDefaultMessageImage();
        while (!stopped) {
            try {
                if (queue.size() == 0) {
                    display(DEFAULT_FILE_NAME);
                    LOG.info("Waiting for updates");
                    updates.take();
                    continue;
                }

                TwitterBoardNotification notification = queue.take();
                while (queue.size() >= MAX_MESSAGES_IN_QUEUE) {
                    LOG.info("Queue size is to big ({}). Skipping message {} ", queue.size(), notification);
                    notification = queue.take();
                }

                long drawFinishTime = 0l;
                boolean redraw = true;
                boolean completed = false;
                while (!completed) {
                    if (redraw) {
                        LOG.info("Going to display {}", notification);
                        int width = generateMessageImage(notification);
                        LOG.info("Width of this message {} and repeat count is {}", width, configuration.getRepeatCount());
                        display(CUSTOM_FILE_NAME);
                        drawFinishTime = System.currentTimeMillis() + width * configuration.getScrollSpeed()
                                * (Math.max(MIN_REPEAT_COUNT, configuration.getRepeatCount()));
                    }
                    long timeout = drawFinishTime - System.currentTimeMillis();
                    if (timeout < 0) {
                        completed = true;
                        continue;
                    }
                    LOG.info("Will sleep for {} ms", timeout);
                    TwitterBoardUpdate update = updates.poll(timeout, TimeUnit.MILLISECONDS);
                    if (update != null) {
                        LOG.info("Update detected: {}", update);
                        if (update.isNewNotification()) {
                            completed = configuration.getQueueSize() == 0;
                        } else if (update.isNewConfiguration()) {
                            LOG.info("Restarting message due to configuration update");
                            redraw = update.isRedraw();
                        }
                    } else {
                        completed = true;
                    }
                }
            } catch (Exception e) {
                LOG.error("Failed to display message: {}", e.getMessage(), e);
            }
        }
    }

    public void shutdown() {
        stopped = true;
        interrupt();
    }

    @Override
    public void onNotification(String id, TwitterBoardNotification notification) {
        LOG.info("Notification for topic id [{}] received.", id);
        LOG.info("Notification body: {}", notification.getMessage());
        try {
            this.queue.put(notification);
        } catch (InterruptedException e) {
            LOG.info("Failed to put notification to queue {}", notification.getMessage(), e);
        }
        updates.add(TwitterBoardUpdate.newNotification());
    }

    @Override
    public void onConfigurationUpdate(TwitterBoardConfiguration newConfiguration) {
        LOG.info("New configuration body: {}", newConfiguration);
        TwitterBoardConfiguration oldConfiguration = configuration;
        configuration = newConfiguration;

        boolean visualUpdate = false;

        visualUpdate = visualUpdate || !configuration.getBackgroundColor().equals(oldConfiguration.getBackgroundColor());
        visualUpdate = visualUpdate || !configuration.getAtTagsColor().equals(oldConfiguration.getAtTagsColor());
        visualUpdate = visualUpdate || !configuration.getHashTagsColor().equals(oldConfiguration.getHashTagsColor());
        visualUpdate = visualUpdate || !configuration.getKeywordsColor().equals(oldConfiguration.getKeywordsColor());
        visualUpdate = visualUpdate || !configuration.getTextColor().equals(oldConfiguration.getTextColor());

        if (visualUpdate || !configuration.getDefaultMessage().equals(oldConfiguration.getDefaultMessage())) {
            generateDefaultMessageImage();
        }

        updates.add(TwitterBoardUpdate.newConfiguration(visualUpdate));
    }

    private void generateDefaultMessageImage() {
        try {
            generateMessageImage(configuration.getDefaultMessage(), Arrays.asList("#KaaIoT", "@KaaIoT"), null, DEFAULT_FILE_NAME);
        } catch (Exception e) {
            LOG.error("Failed to display default message: {}", e.getMessage(), e);
        }
    }

    private int generateMessageImage(TwitterBoardNotification notification) throws Exception {
        return generateMessageImage(notification.getMessage(), notification.getKeywords(), notification.getAuthor(), CUSTOM_FILE_NAME);
    }

    private int generateMessageImage(String messageSrc, List<String> keywords, String author, String fileName) throws Exception {
        long time = System.currentTimeMillis();
        TwitterMessage message = new TwitterMessage(messageSrc, author, keywords);
        List<TwitterMessageToken> tokens = message.toTokens();
        int width = PPMFactory.createAndSave(fileName, tokens, toRGB(configuration.getBackgroundColor()));
        LOG.info("Prepared ppm for {} in {} ms", fileName, (System.currentTimeMillis() - time));
        return width;
    }

    private void display(String fileName) throws IOException {
        if (displayProcess != null) {
            displayProcess.destroy();
        }

        displayProcess = Runtime.getRuntime().exec(LED_MATRIX_COMMAND + " " + configuration.getScrollSpeed() + " " + fileName);

        Thread loggintThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String line;
                    BufferedReader bri = new BufferedReader(new InputStreamReader(displayProcess.getInputStream()));
                    BufferedReader bre = new BufferedReader(new InputStreamReader(displayProcess.getErrorStream()));
                    while ((line = bri.readLine()) != null) {
                        LOG.info("Matrix output: {}", line);
                    }
                    bri.close();
                    while ((line = bre.readLine()) != null) {
                        LOG.info("Matrix error: {}", line);
                    }
                    bre.close();
                } catch (Exception e) {
                    LOG.warn("Failed to monitor process: {}", e.getMessage());
                }
            }
        });
        loggintThread.start();
    }

    private class TwitterMessage {
        private final String message;
        private final String author;
        private final List<String> keywords;

        public TwitterMessage(String message, String author, List<String> keywords) {
            super();
            this.message = message;
            this.author = author;
            if (keywords != null) {
                this.keywords = keywords;
            } else {
                this.keywords = Collections.emptyList();
            }
        }

        public List<TwitterMessageToken> toTokens() {
            List<TwitterMessageToken> result = new ArrayList<BoardController.TwitterMessageToken>();
            if (author != null) {
                result.add(new TwitterMessageToken(author, toRGB(configuration.getAtTagsColor())));
            }

            String[] tokens = message.split("\\s+");
            for (String token : tokens) {
                boolean isKeyword = false;
                for (String keyword : keywords) {
                    if (token.equals(keyword)) {
                        isKeyword = true;
                        break;
                    }
                }
                if (isKeyword) {
                    result.add(new TwitterMessageToken(token, toRGB(configuration.getKeywordsColor())));
                } else if (token.startsWith("#")) {
                    result.add(new TwitterMessageToken(token, toRGB(configuration.getHashTagsColor())));
                } else if (token.startsWith("@")) {
                    result.add(new TwitterMessageToken(token, toRGB(configuration.getAtTagsColor())));
                } else {
                    result.add(new TwitterMessageToken(token, toRGB(configuration.getTextColor())));
                }
            }

            return result;
        }

    }

    public static class TwitterMessageToken {
        private final String token;
        private final int color;

        public TwitterMessageToken(String token, int color) {
            super();
            this.token = token;
            this.color = color;
        }

        public String getToken() {
            return token;
        }

        public int getColor() {
            return color;
        }
    }

    private static int toRGB(String color) {
        return Color.decode(color).getRGB();
    }

    private static class TwitterBoardUpdate {

        private final boolean newConfiguration;
        private final boolean redraw;
        private final boolean newNotification;

        private TwitterBoardUpdate(boolean newConfiguration, boolean redraw, boolean newNotification) {
            super();
            this.newConfiguration = newConfiguration;
            this.newNotification = newNotification;
            this.redraw = redraw;
        }

        public static TwitterBoardUpdate newConfiguration(boolean redraw) {
            return new TwitterBoardUpdate(true, redraw, false);
        }

        public static TwitterBoardUpdate newNotification() {
            return new TwitterBoardUpdate(false, false, true);
        }

        public boolean isNewConfiguration() {
            return newConfiguration;
        }

        public boolean isNewNotification() {
            return newNotification;
        }

        public boolean isRedraw() {
            return redraw;
        }

        @Override
        public String toString() {
            return "TwitterBoardUpdate [newConfiguration=" + newConfiguration + ", redraw=" + redraw + ", newNotification="
                    + newNotification + "]";
        }
    }

}
