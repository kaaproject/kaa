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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Test;
import org.kaaproject.kaa.client.profile.ProfileListener;

public class ProfileContainerTest {

    @Test
    public void testProfileContainer() throws IOException {
        ProfileListener listener = mock(ProfileListener.class);
        BasicProfileContainer container = new BasicProfileContainer();
        container.setProfileListener(listener);
        assertEquals("profileBody1", container.getProfile().getProfileBody());

        container.setProfileBody("profileBody2");
        assertEquals("profileBody2", container.getProfile().getProfileBody());
        assertNotNull(container.getSerializedProfile());
        verify(listener, times(1)).onProfileUpdated(container.getSerializedProfile());
    }

}
