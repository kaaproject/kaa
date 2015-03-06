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
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.schema.sample.event.thermo.ChangeDegreeRequest;
import org.kaaproject.kaa.schema.sample.event.thermo.CustomThermoEventClassFamily;
import org.kaaproject.kaa.schema.sample.event.thermo.CustomThermoEventClassFamily.DefaultEventFamilyListener;
import org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoRequest;
import org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventDemo {


    private static final Logger LOG = LoggerFactory.getLogger(EventDemo.class);

    public static void main(String[] args) {
        LOG.info("Event demo has been started");
        doWork();
        LOG.info("Event demo has been stopped");
    }

    public static void doWork() {
        KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(),
                new SimpleKaaClientStateListener());
        kaaClient.start();

        EndpointRegistrationManager registrationManager = kaaClient
                .getEndpointRegistrationManager();

        registrationManager.attachUser("userExternalId", "userAccessToken",
                new UserAttachCallback() {
                    @Override
                    public void onAttachResult(UserAttachResponse response) {
                        LOG.info("Attach response {}", response.getResult());
                    }
                });

        EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        CustomThermoEventClassFamily tecf = eventFamilyFactory
                .getCustomThermoEventClassFamily();

        List<String> FQNs = new LinkedList<>();
        FQNs.add(ThermostatInfoRequest.class.getName());
        FQNs.add(ChangeDegreeRequest.class.getName());

        EventListenersResolver eventListenersResolver = kaaClient.getEventListenerResolver();

        eventListenersResolver.findEventListeners(FQNs, new FetchEventListeners() {
            @Override
            public void onEventListenersReceived(List<String> eventListeners) {
                LOG.info("{} event listeners received", eventListeners.size());
                for (String listener : eventListeners) {
                    LOG.info("listener: {}", listener);
                }
            }

            @Override
            public void onRequestFailed() {
                LOG.info("Request failed");
            }
        });

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

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        kaaClient.stop();
    }

}
