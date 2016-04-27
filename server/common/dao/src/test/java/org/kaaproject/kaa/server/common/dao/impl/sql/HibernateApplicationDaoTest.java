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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateApplicationDaoTest extends HibernateAbstractTest {

    @Test
    public void testSaveApplication() {
        Application application = generateApplication(null);
        Assert.assertNotNull(application.getId());
    }

    @Test
    public void testFindByTenantId() {
        Tenant ten = generateTenant();
        Application app = generateApplication(ten);
        List<Application> found = applicationDao.findByTenantId(ten.getId().toString());
        Assert.assertEquals(app, found.get(0));
    }

    @Test
    public void testFindByApplicationToken() {
        Tenant ten = generateTenant();
        Application app = generateApplication(ten);
        String token = app.getApplicationToken();
        Application found = applicationDao.findByApplicationToken(token);
        Assert.assertEquals(app, found);
    }

    @Test
    public void testFindByNameAndTenantId() {
        Tenant ten = generateTenant();
        Application app = generateApplication(ten);
        String name = app.getName();
        Application found = applicationDao.findByNameAndTenantId(name, ten.getId().toString());
        Assert.assertEquals(app, found);
    }

    @Test
    public void testRemoveByApplicationToken() {
        Tenant ten = generateTenant();
        Application app = generateApplication(ten);
        String token = app.getApplicationToken();
        applicationDao.removeByApplicationToken(token);
        Assert.assertNull(applicationDao.findByApplicationToken(token));
    }

    @Test
    public void testGetNextSeqNumber() {
        Tenant ten = generateTenant();
        Application app = generateApplication(ten);
        int sequenceNumber = app.getSequenceNumber();
        app = applicationDao.getNextSeqNumber(app.getId().toString());
        Assert.assertNotEquals(sequenceNumber, app.getSequenceNumber());
    }

    @Test
    public void testGetNextSeqNumberWithIncorrectAppId() {
        Application app = applicationDao.getNextSeqNumber("777");
        Assert.assertNull(app);
    }

}
