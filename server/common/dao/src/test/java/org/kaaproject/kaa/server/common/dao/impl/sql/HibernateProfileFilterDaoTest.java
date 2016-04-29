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

package org.kaaproject.kaa.server.common.dao.impl.sql;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointProfileSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.ProfileFilter;
import org.kaaproject.kaa.server.common.dao.model.sql.ServerProfileSchema;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateProfileFilterDaoTest extends HibernateAbstractTest {

    @Test
    public void findActualByEndpointGroupId() {
        List<ProfileFilter> filters = generateFilter(null, null, null, 4, UpdateStatus.DEPRECATED);
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        List<ProfileFilter> active = generateFilter(null, null, group, 1, UpdateStatus.ACTIVE);
        List<ProfileFilter> inactive = generateFilter(null, null, group, 1, UpdateStatus.INACTIVE);
        List<ProfileFilter> actual = new ArrayList<>();
        actual.addAll(active);
        actual.addAll(inactive);
        List<ProfileFilter> found = profileFilterDao.findActualByEndpointGroupId(group.getId().toString());
        Assert.assertEquals(actual.size(), found.size());
        Assert.assertEquals(actual, found);
    }

    @Test
    public void findActualBySchemaIdAndGroupId() {
        List<ProfileFilter> filters = generateFilter(null, null, null, 4, UpdateStatus.DEPRECATED);
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        EndpointProfileSchema schema = first.getEndpointProfileSchema();
        ServerProfileSchema server = first.getServerProfileSchema();
        List<ProfileFilter> active = generateFilter(schema, server, group, 1, UpdateStatus.ACTIVE);
        generateConfiguration(null, group, 1, UpdateStatus.ACTIVE);
        List<ProfileFilter> inactive = generateFilter(schema, server, group, 1, UpdateStatus.INACTIVE);
        List<ProfileFilter> actual = new ArrayList<>();
        actual.addAll(active);
        actual.addAll(inactive);
        List<ProfileFilter> found = profileFilterDao.findActualBySchemaIdAndGroupId(schema.getStringId(), server.getStringId(), group.getStringId());
        Assert.assertEquals(actual.size(), found.size());
        Assert.assertEquals(actual, found);
    }

    @Test
    public void findActualBySchemaIdAndGroupIdWithNullServerSchema() {
        EndpointProfileSchema ps = generateProfSchema(null, 1).get(0);
        List<ProfileFilter> filters = generateFilterWithoutSchemaGeneration(ps, null, null, 1, UpdateStatus.ACTIVE);
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        EndpointProfileSchema schema = first.getEndpointProfileSchema();
        List<ProfileFilter> found = profileFilterDao.findActualBySchemaIdAndGroupId(schema.getStringId(), null, group.getStringId());
        Assert.assertFalse(found.isEmpty());
    }

    @Test
    public void findActualBySchemaIdAndGroupIdWithNullEndpointSchema() {
        ServerProfileSchemaDto ss = generateServerProfileSchema(null, null);
        List<ProfileFilter> filters = generateFilterWithoutSchemaGeneration(null, serverProfileSchemaDao.findById(ss.getId()), null, 1, UpdateStatus.ACTIVE);
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        ServerProfileSchema srv = first.getServerProfileSchema();
        List<ProfileFilter> found = profileFilterDao.findActualBySchemaIdAndGroupId(null, srv.getStringId(), group.getStringId());
        Assert.assertFalse(found.isEmpty());
    }

    @Test
    public void findLatestDeprecated() {
        List<ProfileFilter> filters = generateFilter(null, null, null, 10, UpdateStatus.DEPRECATED);
        Assert.assertEquals(10, filters.size());
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        EndpointProfileSchema schema = first.getEndpointProfileSchema();
        ServerProfileSchema server = first.getServerProfileSchema();
        ProfileFilter deprecated = profileFilterDao.findLatestDeprecated(schema.getStringId(), server.getStringId(), group.getStringId());
        Assert.assertEquals(UpdateStatus.DEPRECATED, deprecated.getStatus());
        Assert.assertEquals(9, deprecated.getSequenceNumber());
    }

    @Test
    public void findByAppIdAndSchemaVersion() {
        Application app = generateApplication(null);
        EndpointProfileSchema schema = generateProfSchema(app, 1).get(0);
        ServerProfileSchemaDto srvSchema = generateServerProfileSchema(app.getStringId(), app.getTenant().getStringId(), 101);

        generateFilter(schema, new ServerProfileSchema(srvSchema), null, 2, UpdateStatus.ACTIVE);

        List<ProfileFilter> filters = generateFilterWithoutSchemaGeneration(schema, new ServerProfileSchema(srvSchema), null, 1, UpdateStatus.ACTIVE);
        filters.addAll(generateFilterWithoutSchemaGeneration(null, new ServerProfileSchema(srvSchema), null, 1, UpdateStatus.ACTIVE));
        filters.addAll(generateFilterWithoutSchemaGeneration(schema, null,null, 1, UpdateStatus.ACTIVE));

        List<ProfileFilter> found = profileFilterDao.findByAppIdAndSchemaVersionsCombination(app.getStringId(), schema.getVersion(), srvSchema.getVersion());
        Assert.assertEquals(filters, found);
    }

    @Test
    public void findInactiveFilter() {
        List<ProfileFilter> filters = generateFilter(null, null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        EndpointProfileSchema schema = first.getEndpointProfileSchema();
        ServerProfileSchema server = first.getServerProfileSchema();
        List<ProfileFilter> inactiveConfigs = generateFilter(schema, server, group, 1, UpdateStatus.INACTIVE);
        generateFilter(schema, server, group, 3, UpdateStatus.DEPRECATED);
        ProfileFilter found = profileFilterDao.findInactiveFilter(schema.getStringId(), server.getStringId(), group.getStringId());
        Assert.assertEquals(inactiveConfigs.get(0), found);
    }

    @Test
    public void findLatestFilter() {
        List<ProfileFilter> filters = generateFilter(null, null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        EndpointProfileSchema schema = first.getEndpointProfileSchema();
        ServerProfileSchema server = first.getServerProfileSchema();
        generateFilter(schema, server, group, 3, UpdateStatus.DEPRECATED);
        generateFilter(schema, server, group, 1, UpdateStatus.INACTIVE);
        ProfileFilter found = profileFilterDao.findLatestFilter(schema.getStringId(), server.getStringId(), group.getStringId());
        Assert.assertEquals(first, found);
    }

    @Test
    public void activate() {
        List<ProfileFilter> filters = generateFilter(null, null, null, 1, null);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        ProfileFilter activated = profileFilterDao.activate(first.getId().toString(), "Test user");
        ProfileFilter found = profileFilterDao.findById(first.getId().toString());
        Assert.assertEquals(UpdateStatus.ACTIVE, activated.getStatus());
        Assert.assertEquals(activated, found);
    }

    @Test
    public void deactivate() {
        List<ProfileFilter> filters = generateFilter(null, null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        Assert.assertEquals(UpdateStatus.ACTIVE, first.getStatus());
        ProfileFilter deactivated = profileFilterDao.deactivate(first.getId().toString(), "Test user");
        ProfileFilter found = profileFilterDao.findById(first.getId().toString());
        Assert.assertEquals(UpdateStatus.DEPRECATED, deactivated.getStatus());
        Assert.assertEquals(deactivated, found);
    }

    @Test
    public void deactivateOldFilter() {
        List<ProfileFilter> filters = generateFilter(null, null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        EndpointProfileSchema schema = first.getEndpointProfileSchema();
        ServerProfileSchema server = first.getServerProfileSchema();
        EndpointGroup group = first.getEndpointGroup();
        Assert.assertEquals(UpdateStatus.ACTIVE, first.getStatus());
        ProfileFilter deactivated = profileFilterDao.deactivateOldFilter(schema.getStringId(), server.getStringId(), group.getStringId(), "Test user");
        ProfileFilter found = profileFilterDao.findById(first.getId().toString());
        Assert.assertEquals(UpdateStatus.DEPRECATED, deactivated.getStatus());
        Assert.assertEquals(deactivated, found);
    }

    @Test
    public void findActiveFilterCount() {
        List<ProfileFilter> filters = generateFilter(null, null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        EndpointProfileSchema schema = first.getEndpointProfileSchema();
        ServerProfileSchema server = first.getServerProfileSchema();
        generateFilter(schema, server, group, 3, UpdateStatus.DEPRECATED);
        generateFilter(schema, server, group, 1, UpdateStatus.ACTIVE);
        long count = profileFilterDao.findActiveFilterCount(schema.getStringId(), server.getStringId(), group.getStringId());
        Assert.assertEquals(2, count);
    }
}
