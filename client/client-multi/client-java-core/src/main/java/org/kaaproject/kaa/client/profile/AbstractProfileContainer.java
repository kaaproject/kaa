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

import java.io.IOException;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;

/**
 * Abstract container for the profile object.<br>
 * <br>
 * Should be used to implement user profile container.
 * It is responsible for serializing profile and notifying Kaa stuff
 * about any updates ({@link AbstractProfileContainer#updateProfile()}).
 * Profile class is auto-generated according to predefined Avro schema.<br>
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
 */
public abstract class AbstractProfileContainer<T extends SpecificRecordBase> implements ProfileContainer {
    private ProfileListener listener;
    private final AvroByteArrayConverter<T> converter;

    /**
     * Constructor for the AbstractProfileContainer.
     */
    public AbstractProfileContainer() {
        this.converter = new AvroByteArrayConverter<T>(getProfileClass());
    }

    /**
     * @return Class object of the user-defined profile
     */
    protected abstract Class<T> getProfileClass();

    /**
     * Retrieves serialized profile.
     *
     * @return byte array with avro serialized profile.
     *
     */
    @Override
    public byte [] getSerializedProfile() throws IOException {
        return converter.toByteArray(getProfile());
    }

    /**
     * Updates profile. Call this method when you finish to update your profile.
     */
    protected final void updateProfile() throws IOException {
        if (listener != null) {
            listener.onProfileUpdated(getSerializedProfile());
        }
    }

    /**
     * Sets profile listener.<br>
     * <br>
     * <b>NOTE:</b>DO NOT use this API explicitly.
     * This method is used for post initialization of a user defined profile container.
     *
     * @param listener New profile listener.
     */
    @Override
    public final void setProfileListener(ProfileListener listener) {
        this.listener = listener;
    }

    /**
     * Retrieves user-defined profile object. Should be implemented by the user.
     *
     * @return profile object
     *
     */
    public abstract T getProfile();

}
