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
 * Container for the profile object which should be implemented by the user
 *
 * @author Yaroslav Zeygerman
 *
 */
public abstract class AbstractProfileContainer<T extends SpecificRecordBase> implements ProfileContainer {
    private ProfileListener listener;
    private AvroByteArrayConverter<T> converter;

    /**
     * Constructor for the AbstractProfileContainer.
     *
     * @param listener listener of profile updates.
     * @param profileClass class object of the user-defined profile.
     *
     */
    public AbstractProfileContainer() {
        this.converter = new AvroByteArrayConverter<T>(getProfileClass());
    }

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
     * Updates profile. Call this method when you finished updating your profile.
     *
     */
    protected final void updateProfile() throws IOException {
        if (listener != null) {
            listener.onProfileUpdated(getSerializedProfile());
        }
    }

    /**
     * Sets new profile listener.
     *
     * @param listener new listener.
     *
     */
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
