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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointConfigurationCassandraDaoTest extends AbstractCassandraTest {

    @Test
    public void testFindById() throws Exception {
        List<CassandraEndpointConfiguration> configs = generateConfiguration(3);
        CassandraEndpointConfiguration expected = configs.get(0);
        CassandraEndpointConfiguration found = endpointConfigurationDao.findById(expected.getConfigurationHash());
        Assert.assertEquals(expected, found);
    }

    @Test
    public void testFindByIdNullKey() throws Exception {
        CassandraEndpointConfiguration found = endpointConfigurationDao.findById(null);
        Assert.assertNull(found);
    }

    @Test
    public void testFindByHash() throws Exception {
        List<CassandraEndpointConfiguration> configs = generateConfiguration(3);
        CassandraEndpointConfiguration expected = configs.get(0);
        CassandraEndpointConfiguration found = endpointConfigurationDao.findByHash(expected.getConfigurationHash().array());
        Assert.assertEquals(expected, found);
    }

    @Test
    public void testRemoveByIdNullKey() throws Exception {
        List<CassandraEndpointConfiguration> configs = generateConfiguration(3);
        CassandraEndpointConfiguration expected = configs.get(0);
        endpointConfigurationDao.removeById(null);
        CassandraEndpointConfiguration found = endpointConfigurationDao.findByHash(expected.getConfigurationHash().array());
        Assert.assertEquals(expected, found);
    }

    @Test
    public void testRemoveByHash() throws Exception {
        List<CassandraEndpointConfiguration> configs = generateConfiguration(3);
        CassandraEndpointConfiguration expected = configs.get(0);
        endpointConfigurationDao.removeByHash(expected.getConfigurationHash().array());
        CassandraEndpointConfiguration found = endpointConfigurationDao.findByHash(expected.getConfigurationHash().array());
        Assert.assertNull(found);
    }

    @Test
    public void testRemoveById() throws Exception {
        List<CassandraEndpointConfiguration> configs = generateConfiguration(3);
        CassandraEndpointConfiguration expected = configs.get(0);
        endpointConfigurationDao.removeById(expected.getConfigurationHash());
        CassandraEndpointConfiguration found = endpointConfigurationDao.findByHash(expected.getConfigurationHash().array());
        Assert.assertNull(found);
    }
}
