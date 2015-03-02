/*
 * Copyright 2014 CyberVision, Inc.
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

public class EventDemo {

	public static void main(String[] args) {
		System.out.println("Event demo has been started");
		doWork();
		System.out.println("Event demo has been stopped");
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
						System.out.println("Attach response"
								+ response.getResult());
					}
				});

		EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
		CustomThermoEventClassFamily tecf = eventFamilyFactory
				.getCustomThermoEventClassFamily();

		List<String> FQNs = new LinkedList<>();
		FQNs.add("org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoRequest");
		FQNs.add("org.kaaproject.kaa.schema.sample.event.thermo.ChangeDegreeRequest");

		EventListenersResolver eventListenersResolver = kaaClient
				.getEventListenerResolver();

		eventListenersResolver.findEventListeners(FQNs,
				new FetchEventListeners() {
					@Override
					public void onRequestFailed() {
						// Some code
					}

					@Override
					public void onEventListenersReceived(
							List<String> eventListeners) {
						// Some code
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
		eventFamilyFactory.removeEventsBlock(trxId);
		
		
		
		tecf.addListener(new DefaultEventFamilyListener() {
	
			@Override
			public void onEvent(ChangeDegreeRequest arg0, String arg1) {
				System.out.println("\n ChangeDegreeRequest event fired!\n");
			}
			
			@Override
			public void onEvent(ThermostatInfoResponse arg0, String arg1) {
				System.out.println("\n ThermostatInfoResponse event fired!\n");
			}
			
			@Override
			public void onEvent(ThermostatInfoRequest arg0, String arg1) {
				System.out.println("\n ThermostatInfoRequest event fired!\n");
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
