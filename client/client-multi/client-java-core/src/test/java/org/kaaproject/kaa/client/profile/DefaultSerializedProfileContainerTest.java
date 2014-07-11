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

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;

public class DefaultSerializedProfileContainerTest {

    @Test
    public void testGetSerializedProfile() throws IOException {
        ProfileListener listener = mock(ProfileListener.class);
        DefaultSerializedProfileContainer serializedContainer = new DefaultSerializedProfileContainer();
        BasicProfileContainer container = new BasicProfileContainer();
        container.setProfileListener(listener);
        serializedContainer.setProfileContainer(container);
        AvroByteArrayConverter<BasicEndpointProfile> converter = new AvroByteArrayConverter<>(BasicEndpointProfile.class);
        assertArrayEquals(converter.toByteArray(container.getProfile()), serializedContainer.getSerializedProfile());
    }

}
