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
import org.kaaproject.kaa.client.event.FetchEventListeners;
import org.kaaproject.kaa.client.event.registration.EndpointRegistrationManager;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.transact.TransactionId;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.schema.sample.event.thermo.ChangeDegreeRequest;
import org.kaaproject.kaa.schema.sample.event.thermo.ThermostatEventClassFamily;
import org.kaaproject.kaa.schema.sample.event.thermo.ThermostatEventClassFamily.DefaultEventFamilyListener;
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

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            LOG.info("{}", e1);
        }

        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener());
        kaaClient.start();

        EndpointRegistrationManager registrationManager = kaaClient.getEndpointRegistrationManager();

        // Our demo application uses trustful verifier, so it does not matter
        // which credentials you would pass to registration manager
        registrationManager.attachUser(USER_EXTERNAL_ID, USER_ACCESS_TOKEN,
                new UserAttachCallback() {
                    @Override
                    public void onAttachResult(UserAttachResponse response) {
                        LOG.info("Attach response {}", response.getResult());

                        //If our endpoint was successfully attached
                        if (response.getResult() == SyncResponseResultType.SUCCESS) {
                            doWork();
                        }
                        //If not - Release all network connections and application resources.
                        // Shutdown all Kaa client tasks.
                        else {
                            kaaClient.stop();
                            LOG.info("Event demo stopped");
                        }
                    }
                });

    }


    public static void doWork() {

        List<String> FQNs = new LinkedList<>();
        FQNs.add(ThermostatInfoRequest.class.getName());
        FQNs.add(ChangeDegreeRequest.class.getName());

        //Getting event listener resolver
        EventListenersResolver eventListenersResolver = kaaClient.getEventListenerResolver();

        //And then finding all listener listening to events in FQNs list
        eventListenersResolver.findEventListeners(FQNs, new FetchEventListeners() {

            //Doing something with event listeners in case of success
            @Override
            public void onEventListenersReceived(List<String> eventListeners) {
                LOG.info("{} event listeners received", eventListeners.size());
                for (String listener : eventListeners) {
                    LOG.info("listener: {}", listener);
                }
            }

            //Or if something gone wrong handling fail somehow
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
        tecf.addListener(new DefaultEventFamilyListener() {

            @Override
            public void onEvent(ChangeDegreeRequest arg0, String arg1) {
                LOG.info("ChangeDegreeRequest event fired!");
            }

            @Override
            public void onEvent(ThermostatInfoResponse arg0, String arg1) {
                LOG.info("ThermostatInfoResponse event fired!");
            }

            @Override
            public void onEvent(ThermostatInfoRequest arg0, String arg1) {
                LOG.info("ThermostatInfoRequest event fired!");
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
        tecf.addEventToBlock(trxId, new ChangeDegreeRequest(-30), "home_thermostat");

        // Send added events in a batch
        eventFamilyFactory.submitEventsBlock(trxId);
        // Dismiss the event batch (if the batch was not submitted as shown in the previous line)
        // eventFamilyFactory.removeEventsBlock(trxId);


        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Release all network connections and application resources.
        //Shutdown all Kaa client tasks.
        kaaClient.stop();

        LOG.info("Event demo stopped");
    }
}
