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

package org.kaaproject.kaa.server.common;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;

public class Base64UtilTest {
    private static final String UNKNOWN= "Unknown";

    @Test
    public void encodeNullProfileTest() {
        EndpointProfileDto profileDto = null;
        String encoded = Base64Util.encode(profileDto);
        Assert.assertEquals(UNKNOWN, encoded);
    }

    @Test
    public void encodeNullEndpointKeyHashTest() {
        EndpointProfileDto endpointProfileDto = new EndpointProfileDto();
        String encoded = Base64Util.encode(endpointProfileDto);
        Assert.assertEquals(UNKNOWN, encoded);
    }

    @Test
    public void successfulEncodingTest() {
        EndpointProfileDto endpointProfileDto = new EndpointProfileDto();
        byte[] data = new byte[256];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        endpointProfileDto.setEndpointKeyHash(data);
        String encoded = Base64Util.encode(endpointProfileDto);
        Assert.assertEquals(encoded, Base64.encodeBase64String(data));
    }
}
