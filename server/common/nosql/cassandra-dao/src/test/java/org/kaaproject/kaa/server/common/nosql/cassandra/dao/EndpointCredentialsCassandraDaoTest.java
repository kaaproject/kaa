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
    private static final String ENDPOINT_ID = "endpoint_id";
    private static final String PUBLIC_KEY = "public_key";

    @Test
    public void findByApplicationIdTest() throws Exception {
        EndpointCredentialsDto endpointCredentials = this.generateEndpointCredentials(APPLICATION_ID, ENDPOINT_ID, PUBLIC_KEY);
        Assert.assertNotNull(endpointCredentials);
        Assert.assertNotNull(endpointCredentials.getId());

        List<CassandraEndpointCredentials> databaseRecords = this.endpointCredentialsDao.findByApplicationId(APPLICATION_ID);
        Assert.assertNotNull(databaseRecords);
        Assert.assertEquals(1, databaseRecords.size());

        EndpointCredentials databaseRecord = databaseRecords.get(0);
        Assert.assertNotNull(databaseRecord);
        Assert.assertEquals(endpointCredentials.getId(), databaseRecord.getId());
        Assert.assertEquals(endpointCredentials.getEndpointId(), databaseRecord.getEndpointId());
    }

    @Test
    public void findByEndpointIdTest() throws Exception {
        EndpointCredentialsDto endpointCredentials = this.generateEndpointCredentials(APPLICATION_ID, ENDPOINT_ID, PUBLIC_KEY);
        Assert.assertNotNull(endpointCredentials);
        Assert.assertNotNull(endpointCredentials.getId());

        EndpointCredentials databaseRecord = this.endpointCredentialsDao.findByEndpointId(ENDPOINT_ID);
        Assert.assertNotNull(databaseRecord);
        Assert.assertEquals(endpointCredentials.getId(), databaseRecord.getId());
        Assert.assertEquals(endpointCredentials.getEndpointId(), databaseRecord.getEndpointId());
    }

    @Test
    public void removeByEndpointIdTest() throws Exception {
        EndpointCredentialsDto endpointCredentials = this.generateEndpointCredentials(APPLICATION_ID, ENDPOINT_ID, PUBLIC_KEY);
        Assert.assertNotNull(endpointCredentials);
        Assert.assertNotNull(endpointCredentials.getId());

        this.endpointCredentialsDao.removeByEndpointId(ENDPOINT_ID);
        EndpointCredentials databaseRecord = this.endpointCredentialsDao.findByEndpointId(ENDPOINT_ID);
        Assert.assertNull(databaseRecord);
    }
}
