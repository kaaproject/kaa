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

import org.junit.Test;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.update.UpdateManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class DefaultProfileManagerTest {

    @Test
    public void testProfileManager() {
        UpdateManager updateManager = mock(UpdateManager.class);
        KaaClientState state = mock(KaaClientState.class);
        ProfileContainer container = mock(ProfileContainer.class);

        DefaultProfileManager profileManager = new DefaultProfileManager(updateManager, state);
        profileManager.setProfileContainer(container);

        assertNotNull(profileManager.getSerializedProfileContainer());
        verify(container, times(1)).setProfileListener(any(ProfileListener.class));
    }

}
