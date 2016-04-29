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

package org.kaaproject.kaa.server.operations.service.profile;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;

public class ProfileJsonTest {

    @Test
    public void testJsonConversion() throws IOException{
        String expected = "{\"profileBody\":\"test2\"}";
        BasicEndpointProfile bep = new BasicEndpointProfile();
        bep.setProfileBody("test2");
        GenericAvroConverter converter = new GenericAvroConverter(BasicEndpointProfile.SCHEMA$);
        Assert.assertEquals(expected, converter.encodeToJson(bep));
    }

}
