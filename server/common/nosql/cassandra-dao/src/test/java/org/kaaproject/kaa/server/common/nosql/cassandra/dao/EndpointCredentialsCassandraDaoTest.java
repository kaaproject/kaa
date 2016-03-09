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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointCredentialsDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointCredentials;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointCredentials;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointCredentialsCassandraDaoTest extends AbstractCassandraTest {

    private static final String APPLICATION_ID = "application_id";
    private static final byte[] ENDPOINT_KEY = "endpoint_key".getBytes();
    private static final byte[] ENDPOINT_KEY_HASH = "endpoint_key_hash".getBytes();

    @Test
    public void findByApplicationIdTest() throws Exception {
        EndpointCredentialsDto endpointCredentials = this.generateEndpointCredentials(APPLICATION_ID, ENDPOINT_KEY, ENDPOINT_KEY_HASH);
        Assert.assertNotNull(endpointCredentials);
        Assert.assertNotNull(endpointCredentials.getId());

        List<CassandraEndpointCredentials> databaseRecords = this.endpointCredentialsDao.findByApplicationId(APPLICATION_ID);
        Assert.assertNotNull(databaseRecords);
        Assert.assertEquals(1, databaseRecords.size());

        EndpointCredentials databaseRecord = databaseRecords.get(0);
        Assert.assertNotNull(databaseRecord);
        Assert.assertEquals(endpointCredentials, databaseRecord.toDto());
    }

    @Test
    public void findByEndpointKeyHashTest() throws Exception {
        EndpointCredentialsDto endpointCredentials = this.generateEndpointCredentials(APPLICATION_ID, ENDPOINT_KEY, ENDPOINT_KEY_HASH);
        Assert.assertNotNull(endpointCredentials);
        Assert.assertNotNull(endpointCredentials.getId());

        EndpointCredentials databaseRecord = this.endpointCredentialsDao.findByEndpointKeyHash(ENDPOINT_KEY_HASH);
        Assert.assertNotNull(databaseRecord);
        Assert.assertEquals(endpointCredentials, databaseRecord.toDto());
    }

    @Test
    public void removeByEndpointKeyHashTest() throws Exception {
        EndpointCredentialsDto endpointCredentials = this.generateEndpointCredentials(APPLICATION_ID, ENDPOINT_KEY, ENDPOINT_KEY_HASH);
        Assert.assertNotNull(endpointCredentials);
        Assert.assertNotNull(endpointCredentials.getId());

        this.endpointCredentialsDao.removeByEndpointKeyHash(ENDPOINT_KEY_HASH);
        EndpointCredentials databaseRecord = this.endpointCredentialsDao.findByEndpointKeyHash(ENDPOINT_KEY_HASH);
        Assert.assertNull(databaseRecord);
    }
}
