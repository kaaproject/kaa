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

/**
 * Interface for the profile container.
 * User-defined version should be inherited from {@link AbstractProfileContainer}
 */
public interface ProfileContainer {

    /**
     * Set Kaa profile listener {@link ProfileListener} for the container.
     * DO NOT use this API explicitly. When user sets his implementation
     * of the profile container, Kaa will use this method to inject its 
     * own listener {@link DefaultProfileListener}.
     * 
     * @param listener Listener that tracks profile updates
     */
    void setProfileListener(ProfileListener listener);

    /**
     * Retrieves serialized profile
     *
     * @return byte array with serialized profile
     *
     */
    byte [] getSerializedProfile() throws IOException;

}
