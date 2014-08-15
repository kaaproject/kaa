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

package org.kaaproject.kaa.client.profile;

/**
 * Interface for the profile manager.<br>
 * <br>
 * Responsible for the management of the user-defined profile container
 * ({@link AbstractProfileContainer})<br>
 * <br>
 * Profile manager is used to track any profile updates.
 * If no container is set, Kaa won't be able to process these updates.<br>
 * <br>
 * <pre>
 * {@code
 * // Assume, BasicEndpointProfile is a profile class auto-generated according to predefined Avro schema
 * public class BasicProfileContainer extends AbstractProfileContainer<BasicEndpointProfile> {
 *     private BasicEndpointProfile profile = new BasicEndpointProfile();
 *
 *     public BasicProfileContainer() {}
 *     public BasicEndpointProfile getProfile() {
 *         return profile;
 *     }
 *     protected Class<BasicEndpointProfile> getProfileClass() {
 *         return BasicEndpointProfile.class;
 *     }
 *     // User-define method
 *     public void setNewProfile(BasicEndpointProfile profile) {
 *         this.profile = profile;
 *         // Update method should be called to notify about changes in the profile.
 *         updateProfile();
 *     }
 * }
 *
 * BasicProfileContainer container = new BasicProfileContainer();
 * ProfileManager manager = kaaClient.getProfileManager();
 * manager.setProfileContainer(container);
 *
 * // Assume, profile is changed. Current implementation of the profile container
 * // notifies Kaa inner stuff about profile update.
 * container.setNewProfile(new BasicEndpointProfile());
 * }
 * </pre>
 *
 * @author Yaroslav Zeygerman
 *
 * @see AbstractProfileContainer
 * @see SerializedProfileContainer
 */
public interface ProfileManager {

    /**
     * Sets profile container implemented by the user.
     *
     * @param container User-defined container
     * @see AbstractProfileContainer
     *
     */
    void setProfileContainer(ProfileContainer container);

    /**
     * Retrieves container responsible for profile serializing
     *
     * @return Container which contains the serialized profile
     *
     */
    SerializedProfileContainer getSerializedProfileContainer();

}
