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

package org.kaaproject.kaa.demo.twitterled.board;

import java.io.IOException;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A demo application that shows how to use the Kaa notifications API.
 */
public class BoardControllerApplication {
    private static final Logger LOG = LoggerFactory.getLogger(BoardControllerApplication.class);
    private static KaaClient kaaClient;

    private static BoardController controller;
    
    public static void main(String[] args) {
        LOG.info("Board controller started");
        LOG.info("--= Press any key to exit =--");
        
        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener(){
            
            @Override
            public void onStarted(){
                controller.start();
            }
        });
        
        controller = new BoardController(kaaClient.getConfiguration());

        // Add a notification listener that listens to all notifications.
        kaaClient.addNotificationListener(controller);

        kaaClient.addConfigurationListener(controller);

        // Start the Kaa client and connect it to the Kaa server.
        kaaClient.start();

        try {
            // Wait for some input before exiting.
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException was caught", e);
        }

        // Stop the Kaa client and release all the resources which were in use.
        kaaClient.stop();

        // Stop the Kaa client and release all the resources which were in use.
        controller.shutdown();
        
        LOG.info("Notification demo stopped");
    }
}
