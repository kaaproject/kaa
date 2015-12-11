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

package org.kaaproject.kaa.server.common.dao.impl.sql;


import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointProfileSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.ProfileFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateProfileFilterDaoTest extends HibernateAbstractTest {

    @Test
    public void findAllByProfileSchemaId() {
        List<ProfileFilter> filters = generateFilter(null, null, 1, null);
        EndpointProfileSchema schema = filters.get(0).getProfileSchema();
        List<ProfileFilter> found = profileFilterDao.findAllByProfileSchemaId(schema.getId().toString());
        Assert.assertEquals(filters, found);
    }

    @Test
    public void findActualByEndpointGroupId() {
        List<ProfileFilter> filters = generateFilter(null, null, 4, UpdateStatus.DEPRECATED);
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        List<ProfileFilter> active = generateFilter(null, group, 1, UpdateStatus.ACTIVE);
        List<ProfileFilter> inactive = generateFilter(null, group, 1, UpdateStatus.INACTIVE);
        List<ProfileFilter> actual = new ArrayList<>();
        actual.addAll(active);
        actual.addAll(inactive);
        List<ProfileFilter> found = profileFilterDao.findActualByEndpointGroupId(group.getId().toString());
        Assert.assertEquals(actual.size(), found.size());
        Assert.assertEquals(actual, found);
    }

    @Test
    public void findActualBySchemaIdAndGroupId() {
        List<ProfileFilter> filters = generateFilter(null, null, 4, UpdateStatus.DEPRECATED);
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        EndpointProfileSchema schema = first.getProfileSchema();
        List<ProfileFilter> active = generateFilter(schema, group, 1, UpdateStatus.ACTIVE);
        generateConfiguration(null, group, 1, UpdateStatus.ACTIVE);
        List<ProfileFilter> inactive = generateFilter(schema, group, 1, UpdateStatus.INACTIVE);
        List<ProfileFilter> actual = new ArrayList<>();
        actual.addAll(active);
        actual.addAll(inactive);
        List<ProfileFilter> found = profileFilterDao.findActualBySchemaIdAndGroupId(schema.getId().toString(), group.getId().toString());
        Assert.assertEquals(actual.size(), found.size());
        Assert.assertEquals(actual, found);
    }

    @Test
    public void findLatestDeprecated() {
        List<ProfileFilter> filters = generateFilter(null, null, 10, UpdateStatus.DEPRECATED);
        Assert.assertEquals(10, filters.size());
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        EndpointProfileSchema schema = first.getProfileSchema();
        ProfileFilter deprecated = profileFilterDao.findLatestDeprecated(schema.getId().toString(), group.getId().toString());
        Assert.assertEquals(UpdateStatus.DEPRECATED, deprecated.getStatus());
        Assert.assertEquals(9, deprecated.getSequenceNumber());
    }

    @Test
    public void removeByEndpointGroupId() {
        List<ProfileFilter> filters = generateFilter(null, null, 3, UpdateStatus.ACTIVE);
        Assert.assertEquals(3, filters.size());
        Long id = filters.get(0).getId();
        EndpointGroup group = filters.get(0).getEndpointGroup();
        profileFilterDao.removeByEndpointGroupId(group.getId().toString());
        ProfileFilter found = profileFilterDao.findById(id.toString());
        Assert.assertNull(found);
    }

    @Test
    public void findByAppIdAndSchemaVersion() {
        List<ProfileFilter> filters = generateFilter(null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        Application app = first.getApplication();
        List<ProfileFilter> found = profileFilterDao.findByAppIdAndSchemaVersions(app.getId().toString(), endpointSchemaVersion, first.getSchemaVersion());
        Assert.assertEquals(filters, found);
    }

    @Test
    public void findInactiveFilter() {
        List<ProfileFilter> filters = generateFilter(null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        EndpointProfileSchema schema = first.getProfileSchema();
        List<ProfileFilter> inactiveConfigs = generateFilter(schema, group, 1, UpdateStatus.INACTIVE);
        generateFilter(schema, group, 3, UpdateStatus.DEPRECATED);
        ProfileFilter found = profileFilterDao.findInactiveFilter(schema.getId().toString(), group.getId().toString());
        Assert.assertEquals(inactiveConfigs.get(0), found);
    }

    @Test
    public void findLatestFilter() {
        List<ProfileFilter> filters = generateFilter(null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        EndpointProfileSchema schema = first.getProfileSchema();
        generateFilter(schema, group, 3, UpdateStatus.DEPRECATED);
        generateFilter(schema, group, 1, UpdateStatus.INACTIVE);
        ProfileFilter found = profileFilterDao.findLatestFilter(schema.getId().toString(), group.getId().toString());
        Assert.assertEquals(first, found);
    }

    @Test
    public void activate() {
        List<ProfileFilter> filters = generateFilter(null, null, 1, null);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        ProfileFilter activated = profileFilterDao.activate(first.getId().toString(), "Test user");
        ProfileFilter found = profileFilterDao.findById(first.getId().toString());
        Assert.assertEquals(UpdateStatus.ACTIVE, activated.getStatus());
        Assert.assertEquals(activated, found);
    }

    @Test
    public void deactivate() {
        List<ProfileFilter> filters = generateFilter(null, null, 1, UpdateStatus.ACTIVE);
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
        List<ProfileFilter> filters = generateFilter(null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        EndpointProfileSchema schema = first.getProfileSchema();
        EndpointGroup group = first.getEndpointGroup();
        Assert.assertEquals(UpdateStatus.ACTIVE, first.getStatus());
        ProfileFilter deactivated = profileFilterDao.deactivateOldFilter(schema.getId().toString(), group.getId().toString(), "Test user");
        ProfileFilter found = profileFilterDao.findById(first.getId().toString());
        Assert.assertEquals(UpdateStatus.DEPRECATED, deactivated.getStatus());
        Assert.assertEquals(deactivated, found);
    }

    @Test
    public void findActiveFilterCount() {
        List<ProfileFilter> filters = generateFilter(null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, filters.size());
        ProfileFilter first = filters.get(0);
        EndpointGroup group = first.getEndpointGroup();
        EndpointProfileSchema schema = first.getProfileSchema();
        generateFilter(schema, group, 3, UpdateStatus.DEPRECATED);
        generateFilter(schema, group, 1, UpdateStatus.ACTIVE);
        long count = profileFilterDao.findActiveFilterCount(schema.getId().toString(), group.getId().toString());
        Assert.assertEquals(2, count);
    }
}
