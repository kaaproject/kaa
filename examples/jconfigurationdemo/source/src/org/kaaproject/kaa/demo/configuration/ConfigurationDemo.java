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

import java.util.Scanner;

public class ConfigurationDemo {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationDemo.class);
    private static final String EXIT = "exit";
    private static final String QUIT = "quit";
    private static final String HELP = "help";
    private static final String CONFIGURATION = "configuration";
    private static final String SHOW = "show";
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
        try (Scanner in = new Scanner(System.in)) {
            // wait for 'exit' or 'quit' or EOF (Ctrl + D)
            while (in.hasNextLine()) {
                String command = extractCommand(in.nextLine());
                if (closeCommand(command)) break;
                else if (command.isEmpty()) continue;
                else if (command.equals(HELP)) displayHelpMessage();
                else if (command.equals(EXIT) || command.equals(QUIT)) break;
                else if (command.equals(CONFIGURATION) || command.equals(SHOW)) displayConfiguration();
                else System.out.println("Error: Unknown command: '" + command + "'. Print 'help' to list available commands");
            }
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
            LOG.info("Configuration body: [string: {}, integer: {}]", configuration.getMessageConf(),
                    configuration.getNumberConf());
        }
    }

    private boolean closeCommand(String command) {
        return command.equals(EXIT) || command.equals(QUIT);
    }

    private String extractCommand(String line) {
        return line.trim();
    }

    private void displayHelpMessage() {
        System.out.println("Usage: \n" +
                "'help' - displays this message\n" +
                "'configuration' or 'show' - displays configuration\n" +
                "'exit' or 'quit' - finishes the program");
    }

    private void displayConfiguration() {
        if (configuration == null) {
            System.out.println("Configuration isn't loaded");
        } else {
            System.out.println("Configuration: {messageConf = " + configuration.getMessageConf() + "; numberConf = " +
                    configuration.getNumberConf() + "}");
        }
    }
}
