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
 * Configuration demo application, which demonstrates Kaa configuration API
 */
public class ConfigurationDemo {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationDemo.class);
    private static KaaClient kaaClient;

    public static void main(String[] args) {
        LOG.info("Configuration demo application has started");
        LOG.info("--= Press any key to exit =--");

        // Kaa desktop context of the application
        DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext();

        // Create new Kaa client, which listens on Kaa client state changes
        kaaClient = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                super.onStarted();
                displayConfiguration();
            }
        });

        // Persist configuration locally to prevent its download in future launches
        kaaClient.setConfigurationStorage(new SimpleConfigurationStorage(desktopKaaPlatformContext, "saved_config.cfg"));

        // Listen to configuration changes
        kaaClient.addConfigurationListener(new ConfigurationListener() {
            @Override
            public void onConfigurationUpdate(SampleConfiguration sampleConfiguration) {
                LOG.info("Configuration was updated");
                displayConfiguration();
            }
        });

        // Start Kaa client, which establishes client connection
        kaaClient.start();

        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException was caught", e);
        }

        // Stop Kaa client, which will gracefully close all used resources
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
