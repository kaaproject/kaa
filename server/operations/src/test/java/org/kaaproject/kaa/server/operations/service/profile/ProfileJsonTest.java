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

package org.kaaproject.kaa.server.operations.service.profile;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.common.endpoint.gen.EndpointVersionInfo;
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;

public class ProfileJsonTest {

    @Test
    public void testJsonConversion() throws IOException{
        String expected = "{\"applicationToken\":\"test\",\"endpointPublicKeyHash\":\"\",\"profileBody\":\"\",\"acceptedUnicastNotifications\":null,\"topicStates\":null,\"versionInfo\":{\"configVersion\":1,\"profileVersion\":2,\"systemNfVersion\":1,\"userNfVersion\":1}}";
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setApplicationToken("test");
        updateRequest.setVersionInfo(new EndpointVersionInfo(1, 2, 1, 1));
        updateRequest.setEndpointPublicKeyHash(ByteBuffer.wrap(new byte[0]));
        updateRequest.setProfileBody(ByteBuffer.wrap(new byte[0]));
        
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(updateRequest.getSchema());
        
        ByteArrayOutputStream encodedStream = new ByteArrayOutputStream(); 
        Encoder binaryEncoder = EncoderFactory.get().binaryEncoder(encodedStream, null);
        datumWriter.write(updateRequest, binaryEncoder);
        binaryEncoder.flush();
        encodedStream.flush();
        String actual = GenericAvroConverter.toJson(encodedStream.toByteArray(), updateRequest.getSchema().toString());
        Assert.assertEquals(expected, actual);
        BasicEndpointProfile bep = new BasicEndpointProfile();
        bep.setProfileBody("test2");
        GenericAvroConverter converter = new GenericAvroConverter(BasicEndpointProfile.SCHEMA$);
        System.out.println(converter.endcodeToJson(bep));
    }
    
}
