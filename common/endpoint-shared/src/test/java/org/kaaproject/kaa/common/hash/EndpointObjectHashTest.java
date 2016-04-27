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

package org.kaaproject.kaa.common.hash;

import java.util.Arrays;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class EndpointObjectHashTest {

    @Test
    public void deltaSameEndpointObjectHashTest() {
        EndpointObjectHash hash1 = EndpointObjectHash.fromSHA1("test");
        EndpointObjectHash hash2 = EndpointObjectHash.fromSHA1("test");
        Assert.assertEquals(hash1, hash2);
        hash1 = EndpointObjectHash.fromSHA1("test".getBytes());
        hash2 = EndpointObjectHash.fromSHA1("test".getBytes());
        Assert.assertEquals(hash1, hash2);
    }

    @Test
    public void deltaDifferentEndpointObjectHashTest() {
        EndpointObjectHash hash1 = EndpointObjectHash.fromSHA1("test1");
        EndpointObjectHash hash2 = EndpointObjectHash.fromSHA1("test2");
        Assert.assertNotEquals(hash1, hash2);
        hash1 = EndpointObjectHash.fromSHA1("test1".getBytes());
        hash2 = EndpointObjectHash.fromSHA1("test2".getBytes());
        Assert.assertNotEquals(hash1, hash2);
    }
    
    @Test
    public void nullEndpointObjectHashTest() {
        byte[] binaryData = null;
        EndpointObjectHash hash1 = EndpointObjectHash.fromSHA1(binaryData);
        Assert.assertNull(hash1);
        hash1 = EndpointObjectHash.fromBytes(binaryData);
        Assert.assertNull(hash1);
        String strData = null;
        hash1 = EndpointObjectHash.fromSHA1(strData);
        Assert.assertNull(hash1);
    }

    @Test
    public void toStringEndpointObjectHashTest() {
        EndpointObjectHash hash1 = EndpointObjectHash.fromBytes("test".getBytes());
        Assert.assertNotNull(hash1.toString());
        Assert.assertEquals(Arrays.toString("test".getBytes()), hash1.toString());
    }

    @Test
    public void equalsEndpointObjectHashTest() {
        EqualsVerifier.forClass(EndpointObjectHash.class).verify();
    }    
    
}
