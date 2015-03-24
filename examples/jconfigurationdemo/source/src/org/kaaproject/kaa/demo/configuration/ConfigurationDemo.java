/*
 * Copyright 2015 CyberVision, Inc.
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
import org.kaaproject.kaa.client.configuration.base.SimpleConfigurationStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * A demo application that shows how to use the Kaa configuration API.
 */
public class ConfigurationDemo {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationDemo.class);
    private static KaaClient kaaClient;

    public static void main(String[] args) {
        LOG.info("Configuration demo application has started");

        // Create the Kaa desktop context for the application.
        DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext();

        // Create a Kaa client and add a listener which displays the Kaa client configuration 
        // as soon as the Kaa client is started. 
        kaaClient = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                super.onStarted();
                displayConfiguration();
            }
        });

        // Persist configuration in a local storage to avoid downloading it each time the Kaa client is started.
        kaaClient.setConfigurationStorage(new SimpleConfigurationStorage(desktopKaaPlatformContext, "saved_config.cfg"));

        // Add a listener which displays the Kaa client configuration each time it is updated.
        kaaClient.addConfigurationListener(new ConfigurationListener() {
            @Override
            public void onConfigurationUpdate(SampleConfiguration sampleConfiguration) {
                LOG.info("Configuration was updated");
                displayConfiguration();
            }
        });

        // Start the Kaa client and connect it to the Kaa server.
        kaaClient.start();

        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException was caught", e);
        }

        // Stop the Kaa client and release all the resources which were in use.
        kaaClient.stop();

        LOG.info("Configuration demo application has finished");
    }

    private static void displayConfiguration() {
        SampleConfiguration configuration = kaaClient.getConfiguration();
        List<Link> links = configuration.getAddressList();
        LOG.info("Configuration body:");
        for (Link l : links) {
            LOG.info("{} - {}", l.getLabel(), l.getUrl());
        }
    }
}
