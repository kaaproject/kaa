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

package org.kaaproject.kaa.client.profile;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.ProfileTransport;
import org.kaaproject.kaa.schema.system.EmptyData;
import org.mockito.Mockito;

public class DefaultProfileManagerTest {

    @Test
    public void testProfileManagerIsInitialized() {
        ProfileTransport transport = mock(ProfileTransport.class);
        DefaultProfileManager profileManager = new DefaultProfileManager(transport);

        ProfileSerializer profileSerializer = new ProfileSerializer();

        if (profileSerializer.isDefault()) {
            Assert.assertTrue(profileManager.isInitialized());
        } else {
            Assert.assertFalse(profileManager.isInitialized());

            profileManager.setProfileContainer(new ProfileContainer() {
                @Override
                public EmptyData getProfile() {
                    return new EmptyData();
                }
            });

            Assert.assertTrue(profileManager.isInitialized());
        }
    }

    @Test
    public void testProfileManager() throws IOException {
        ProfileTransport transport = mock(ProfileTransport.class);

        DefaultProfileManager profileManager = new DefaultProfileManager(transport);
        profileManager.setProfileContainer(new ProfileContainer() {

            @Override
            public EmptyData getProfile() {
                return new EmptyData();
            }
        });
        Assert.assertNotNull(profileManager.getSerializedProfile());
        profileManager.updateProfile();
        Mockito.verify(transport).sync();
    }

}
