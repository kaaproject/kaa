/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointCredentialsDto;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public class CassandraEndpointCredentialsTest {

    private static final String APPLICATION_ID = "application_id";
    private static final byte[] ENDPOINT_KEY = "endpoint_key".getBytes();
    private static final byte[] ENDPOINT_KEY_HASH = "endpoint_key_hash".getBytes();

    @Test
    public void equalsVerifierTest() throws Exception {
        EqualsVerifier.forClass(CassandraEndpointCredentials.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void dataConversionTest() throws Exception {
        EndpointCredentialsDto endpointCredentials = new EndpointCredentialsDto(APPLICATION_ID, ENDPOINT_KEY, ENDPOINT_KEY_HASH);
        CassandraEndpointCredentials endpointCredentialsModel = new CassandraEndpointCredentials(endpointCredentials);
        Assert.assertEquals(endpointCredentials, endpointCredentialsModel.toDto());
    }
}
