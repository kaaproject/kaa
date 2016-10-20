/*
 * Copyright 2014-2016 CyberVision, Inc.
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

/**
 * <p>Provides implementation of a profile management.</p>
 *
 * <p>The endpoint profile is a structured data set of an arbitrary complexity
 * that describes the endpoint characteristics. It is used to classify
 * endpoints into endpoint groups. The profile data structure is defined using
 * <a href="http://avro.apache.org/docs/current/spec.html#schemas"> the Apache
 * Avro schema format</a>. The Profile schema supports all Avro features:
 * primitive types, complex types, arrays, maps, etc. The endpoint SDK is
 * responsible for delivery of the profile to the Kaa server.</p>
 *
 * <p>The developer is able to report profile updates to the endpoint SDK using
 * a profile container. The profile related API varies depending on the target
 * SDK platform, however the general approach is the same.</p>
 *
 * Assume a profile schema has the following form:
 * <pre>
 * {@code
 * {
 *     "name": "BasicEndpointProfile",
 *     "namespace": "org.kaaproject.kaa.client.example",
 *     "type": "record",
 *     "fields": [
 *         {
 *             "name": "data",
 *             "type": "string"
 *         }
 *     ]
 * }
 * }
 * </pre>
 *
 * Below is an example of the profile container:
 * <pre>
 * {@code
 * // Assume, BasicEndpointProfile is a profile class auto-generated
 * // according to predefined Avro schema
 * public class BasicProfileContainer extends AbstractProfileContainer<BasicEndpointProfile> {
 *     private BasicEndpointProfile profile = new BasicEndpointProfile();
 *
 *     \@Override
 *     public BasicEndpointProfile getProfile() {
 *         return profile;
 *     }
 *
 *     \@Override
 *     protected Class<BasicEndpointProfile> getProfileClass() {
 *         return BasicEndpointProfile.class;
 *     }
 *
 *     public void setNewProfile(BasicEndpointProfile profile) {
 *         this.profile = profile;
 *         // NOTE: Update method should be called to notify about changes in the profile.
 *         updateProfile();
 *     }
 * }
 *
 * ...
 *
 * // Desktop Kaa client initialization based on BasicProfileContainer
 * private void init(){
 *     //Create instance of desktop Kaa application
 *     Kaa kaa = new KaaDesktop();
 *     //Create client for Kaa SDK
 *     KaaClient client = kaa.getClient();
 *     //Create instance of profile container and set profile
 *     ProfileContainer container = new BasicProfileContainer(profile);
 *     container.setNewProfile(new BasicEndpointProfile("test profile data"));
 *     //Set simple profile container to the profile manager.
 *     client.getProfileManager().setProfileContainer(container);
 *     //Start Kaa
 *     kaa.start();
 * }
 * }
 * </pre>
 *
 * @see org.kaaproject.kaa.client.profile.ProfileManager
 */
package org.kaaproject.kaa.client.profile;