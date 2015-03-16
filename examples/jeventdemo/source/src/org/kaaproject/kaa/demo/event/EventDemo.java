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

package org.kaaproject.kaa.demo.event;

import java.io.IOException;
import java.lang.String;
import java.util.LinkedList;
import java.util.List;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.EventListenersResolver;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.transact.TransactionId;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.schema.sample.event.thermo.ChangeDegreeRequest;
import org.kaaproject.kaa.schema.sample.event.thermo.ThermostatEventClassFamily;
import org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoRequest;
import org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class demonstrates how to send/receive events to/from endpoints via Kaa event subsystem.
 */
public class EventDemo {

    private static final Logger LOG = LoggerFactory.getLogger(EventDemo.class);

    // Credentials for attaching user
    private static final String USER_EXTERNAL_ID = "userExternalId";
    private static final String USER_ACCESS_TOKEN = "userAccessToken";
    // Kaa client
    private static KaaClient kaaClient;

    public static void main(String[] args) {
        LOG.info("Event demo started");

        // Creating Kaa desktop client instance
        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                LOG.info("Kaa client started");
            }
            @Override
            public void onStopped() {
                LOG.info("Kaa client stopped");
            }
        });

        // Starting Kaa client
        kaaClient.start();

        // Our demo application uses trustful verifier, so it does not matter
        // which credentials you would pass to registration manager
        kaaClient.attachUser(USER_EXTERNAL_ID, USER_ACCESS_TOKEN, new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse response) {
                LOG.info("Attach response {}", response.getResult());

                //If our endpoint was successfully attached
                if (response.getResult() == SyncResponseResultType.SUCCESS) {
                    onUserAttached();
                }
                //If not - release all network connections and application resources.
                //Shutdown all Kaa client tasks.
                else {
                    kaaClient.stop();
                    LOG.info("Event demo stopped");
                }
            }
        });

        try {
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException was caught", e);
        }

        //Release all network connections and application resources.
        //Shutdown all Kaa client tasks.
        kaaClient.stop();

        LOG.info("Event demo stopped");
    }


    public static void onUserAttached() {

        List<String> listenerFQNs = new LinkedList<>();
        listenerFQNs.add(ThermostatInfoRequest.class.getName());
        listenerFQNs.add(ChangeDegreeRequest.class.getName());

        //And then finding all listener listening to events in FQNs list
        kaaClient.findEventListeners(listenerFQNs, new FindEventListenersCallback() {

            //Doing something with event listeners in case of success
            @Override
            public void onEventListenersReceived(List<String> eventListeners) {
                LOG.info("{} event listeners received", eventListeners.size());
                for (String listener : eventListeners) {
                    LOG.info("listener: {}", listener);
                }
            }

            //Or if something gone wrong handling fail
            @Override
            public void onRequestFailed() {
                LOG.info("Request failed");
            }
        });

        //Getting event family factory
        EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        //Getting concrete event family
        ThermostatEventClassFamily tecf = eventFamilyFactory.getThermostatEventClassFamily();

        //Adding event listeners for family factory
        tecf.addListener(new ThermostatEventClassFamily.Listener() {

            @Override
            public void onEvent(ChangeDegreeRequest changeDegreeRequest, String senderId) {
                LOG.info("ChangeDegreeRequest event fired! change temperature by {} degrees, sender: {}", changeDegreeRequest.getDegree(), senderId);
            }

            @Override
            public void onEvent(ThermostatInfoResponse thermostatInfoResponse, String senderId) {
                LOG.info("ThermostatInfoResponse event fired! thermostat info: {}, sender: {}", thermostatInfoResponse.getThermostatInfo(), senderId);
            }

            @Override
            public void onEvent(ThermostatInfoRequest thermostatInfoRequest, String senderId) {
                LOG.info("ThermostatInfoRequest event fired! sender: {}", senderId);
            }
        });


        //Broadcasting ChangeDegreeRequest event
        tecf.sendEventToAll(new ChangeDegreeRequest(10));
        LOG.info("ChangeDegreeRequest sent");

        TransactionId trxId = eventFamilyFactory.startEventsBlock();
        // Add events to the block
        // Adding a broadcasted event to the block
        tecf.addEventToBlock(trxId, new ThermostatInfoRequest());
        // Adding a targeted event to the block
        tecf.addEventToBlock(trxId, new ChangeDegreeRequest(-30), "thermostat_endpoint_id");

        // Send added events in a batch
        eventFamilyFactory.submitEventsBlock(trxId);
        LOG.info("Batch of events (ThermostatInfoRequest & ChangeDegreeRequest) sent");
        // Dismiss the event batch (if the batch was not submitted as shown in the previous line)
        // eventFamilyFactory.removeEventsBlock(trxId);
    }
}
