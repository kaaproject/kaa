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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointRegistration;
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
public class EndpointRegistrationCassandraDaoTest extends AbstractCassandraTest {

    private static final String APPLICATION_ID = "application_id";
    private static final String ENDPOINT_ID = "endpoint_id";
    private static final String CREDENTIALS_ID = "credentials_id";

    @Test
    public void testFindByEndpointId() throws Exception {
        EndpointRegistrationDto endpointRegistrationDto = this.generateEndpointRegistration(APPLICATION_ID, ENDPOINT_ID, CREDENTIALS_ID, null, null);
        Assert.assertNotNull(endpointRegistrationDto);
        Assert.assertNotNull(endpointRegistrationDto.getId());
        EndpointRegistration endpointRegistration = this.endpointRegistrationDao.findByEndpointId(ENDPOINT_ID).orElse(null);
        Assert.assertNotNull(endpointRegistration);
        Assert.assertEquals(endpointRegistrationDto, endpointRegistration.toDto());
    }

    @Test
    public void testFindByCredentialsId() throws Exception {
        EndpointRegistrationDto endpointRegistrationDto = this.generateEndpointRegistration(APPLICATION_ID, ENDPOINT_ID, CREDENTIALS_ID, null, null);
        Assert.assertNotNull(endpointRegistrationDto);
        Assert.assertNotNull(endpointRegistrationDto.getId());
        EndpointRegistration endpointRegistration = this.endpointRegistrationDao.findByCredentialsId(CREDENTIALS_ID).orElse(null);
        Assert.assertNotNull(endpointRegistration);
        Assert.assertEquals(endpointRegistrationDto, endpointRegistration.toDto());
    }

    @Test
    public void testRemoveByEndpointId() throws Exception {
        EndpointRegistrationDto endpointRegistrationDto = this.generateEndpointRegistration(APPLICATION_ID, ENDPOINT_ID, CREDENTIALS_ID, null, null);
        Assert.assertNotNull(endpointRegistrationDto);
        Assert.assertNotNull(endpointRegistrationDto.getId());
        this.endpointRegistrationDao.removeByEndpointId(ENDPOINT_ID);
        Assert.assertNull(this.endpointRegistrationDao.findByEndpointId(ENDPOINT_ID).orElse(null));
    }
}
