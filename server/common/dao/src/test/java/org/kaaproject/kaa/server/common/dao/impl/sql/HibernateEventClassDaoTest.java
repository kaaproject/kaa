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
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.server.common.dao.model.sql.EventClass;
import org.kaaproject.kaa.server.common.dao.model.sql.EventClassFamily;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateEventClassDaoTest extends HibernateAbstractTest {

    @Test
    public void removeByEcfId() {
        List<EventClass> eventClasses = generateEventClass(null, null, 2);
        String id = eventClasses.get(0).getStringId();
        EventClass dto = eventClassDao.findById(id);
        Assert.assertNotNull(dto);
        String ecfId = dto.getEcf().getStringId();
        Assert.assertNotNull(ecfId);

        eventClassDao.removeByEcfId(ecfId);
        dto = eventClassDao.findById(id);
        Assert.assertNull(dto);
    }

    @Test
    public void removeById() {
        List<EventClass> eventClasses = generateEventClass(null, null, 2);
        String id = eventClasses.get(0).getStringId();
        EventClass dto = eventClassDao.findById(id);
        Assert.assertNotNull(dto);

        eventClassDao.removeById(id);
        dto = eventClassDao.findById(id);
        Assert.assertNull(dto);
    }

    @Test
    public void findByEcfIdTest() {
        List<EventClass> eventClasses = generateEventClass(null, null, 2);
        EventClass dto = eventClassDao.findById(eventClasses.get(0).getStringId());
        Assert.assertNotNull(dto);
        List<EventClass> eventClassesList = eventClassDao.findByEcfId(dto.getEcf().getStringId());
        EventClass eventClass = null;
        for (EventClass found : eventClassesList) {
            if (dto.getId().equals(found.getId())) {
                eventClass = found;
            }
        }
        Assert.assertNotNull(eventClass);
        Assert.assertEquals(dto, eventClass);
    }

    @Test
    public void testFindByTenantIdAndFqn() {
        EventClassFamily classFamily = generateEventClassFamily(null, 1).get(0);
        Tenant tenant = classFamily.getTenant();
        List<EventClass> events = generateEventClass(tenant, classFamily, 3);
        EventClass ec = events.get(0);
        List<EventClass> found = eventClassDao.findByTenantIdAndFqn(tenant.getId().toString(), ec.getFqn());
        Assert.assertEquals(ec, found.get(0));
    }

    @Test
    public void testFindByTenantIdAndFqnAndVersion() {
        EventClassFamily classFamily = generateEventClassFamily(null, 1).get(0);
        Tenant tenant = classFamily.getTenant();
        List<EventClass> events = generateEventClass(tenant, classFamily, 1);
        EventClass ec = events.get(0);
        EventClassDto dto = ec.toDto();
        dto.setVersion(2);
        EventClass converted  = new EventClass(dto);
        eventClassDao.save(converted);
        EventClass found = eventClassDao.findByTenantIdAndFqnAndVersion(tenant.getId().toString(), ec.getFqn(), converted.getVersion());
        Assert.assertEquals(converted.getFqn(), found.getFqn());
        Assert.assertEquals(converted.getVersion(), found.getVersion());
    }
}
