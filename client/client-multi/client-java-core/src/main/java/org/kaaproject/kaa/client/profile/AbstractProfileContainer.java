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
 * <p>Abstract container for a profile.</p>
 *
 * <p><b>Should be used to implement user profile container.</b></p>
 *
 * <p>It is responsible for serializing profile and notifying Kaa stuff
 * about any updates ({@link AbstractProfileContainer#updateProfile()}).
 * A profile class is auto-generated according to a predefined Avro schema.</p>
 *
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
     * Retrieves profile class object.
     *
     * @return Class object of the user-defined profile
     */
    protected abstract Class<T> getProfileClass();

    /**
     * Retrieves serialized profile.
     *
     * @return Byte array with avro serialized profile.
     *
     */
    @Override
    public byte [] getSerializedProfile() throws IOException {
        return converter.toByteArray(getProfile());
    }

    /**
     * Notify Kaa about profile updates.</br>
     * </br>
     * <b>NOTE: Need to call this method every time when profile is updated.</b>
     */
    protected final void updateProfile() throws IOException {
        if (listener != null) {
            listener.onProfileUpdated(getSerializedProfile());
        }
    }

    /**
     * Kaa specific stuff.</br>
     * </br>
     * <b>NOTE: DO NOT use this API explicitly.</b></br>
     * </br>
     * This method is used for a post initialization of a user defined
     * profile container.
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
     * @return Profile class object
     *
     */
    public abstract T getProfile();

}
