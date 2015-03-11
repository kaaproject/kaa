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

package org.kaaproject.kaa.demo.configuration;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ConfigurationDemo {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationDemo.class);
    private SampleConfiguration configuration;
    private KaaClient kaaClient;

    public static void main(String[] args) {
        LOG.info("Notification demo application has started");
        new ConfigurationDemo().doWork();
        LOG.info("Notification demo application has finished");
    }

    public void doWork() {
        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new ExampleSimpleKaaClientStateListener());
        kaaClient.addConfigurationListener(new ExampleConfigurationListener());
        kaaClient.start();

        displayConfiguration();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        kaaClient.stop();
    }

    private class ExampleSimpleKaaClientStateListener extends SimpleKaaClientStateListener {
        @Override
        public void onStarted() {
            super.onStarted();
            LOG.info("KaaClientStateListener started");
            configuration = kaaClient.getConfiguration();
            LOG.info("Configuration body: {}", configuration.toString());
        }
    }

    private class ExampleConfigurationListener implements ConfigurationListener {
        @Override
        public void onConfigurationUpdate(SampleConfiguration sampleConfiguration) {
            LOG.info("Configuration was updated");
            configuration = sampleConfiguration;
            LOG.info("Configuration body: [messageConf = {}, numberConf = {}]", configuration.getMessageConf(),
                    configuration.getNumberConf());
        }
    }

    private void displayConfiguration() {
        if (configuration == null) {
            LOG.info("Configuration isn't loaded");
        } else {
            LOG.info("Configuration body: [messageConf = {}, numberConf = {}]", configuration.getMessageConf(),
                    configuration.getNumberConf());
        }
    }
}
