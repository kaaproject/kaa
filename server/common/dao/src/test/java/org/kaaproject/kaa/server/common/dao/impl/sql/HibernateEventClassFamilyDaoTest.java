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
import org.kaaproject.kaa.server.common.dao.model.sql.EventClassFamily;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateEventClassFamilyDaoTest extends HibernateAbstractTest {

    @Test
    public void removeByTenantId() {
        List<EventClassFamily> eventClassFamilies = generateEventClassFamily(null, 2);
        EventClassFamily eventClassFamily = eventClassFamilies.get(0);
        EventClassFamily dto = eventClassFamilyDao.findById(eventClassFamily.getStringId());
        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getTenant());

        eventClassFamilyDao.removeByTenantId(dto.getTenant().getStringId());
        dto = eventClassFamilyDao.findById(eventClassFamily.getStringId());
        Assert.assertNull(dto);
    }

    @Test
    public void removeById() {
        List<EventClassFamily> eventClassFamilies = generateEventClassFamily(null, 2);
        EventClassFamily eventClassFamily = eventClassFamilies.get(0);
        EventClassFamily dto = eventClassFamilyDao.findById(eventClassFamily.getStringId());
        Assert.assertNotNull(dto);

        eventClassFamilyDao.removeById(eventClassFamily.getStringId());
        dto = eventClassFamilyDao.findById(eventClassFamily.getStringId());
        Assert.assertNull(dto);
    }

    @Test
    public void findByTenantIdTest() {
        List<EventClassFamily> eventClassFamilies = generateEventClassFamily(null, 2);
        EventClassFamily dto = eventClassFamilyDao.findById(eventClassFamilies.get(0).getStringId());
        Assert.assertNotNull(dto);
        List<EventClassFamily> eventClassFamilyList = eventClassFamilyDao.findByTenantId(dto.getTenant().getStringId());
        EventClassFamily eventClassFamily = null;
        for (EventClassFamily found : eventClassFamilyList) {
            if (dto.getId().equals(found.getId())) {
                eventClassFamily = found;
            }
        }
        Assert.assertNotNull(eventClassFamily);
        Assert.assertEquals(dto, eventClassFamily);
    }

    @Test
    public void findByTenantIdAndNameTest() {
        List<EventClassFamily> eventClassFamilies = generateEventClassFamily(null, 2);
        EventClassFamily dto = eventClassFamilyDao.findById(eventClassFamilies.get(0).getStringId());
        Assert.assertNotNull(dto);
        EventClassFamily eventClassFamily = eventClassFamilyDao.findByTenantIdAndName(dto.getTenant().getStringId(), dto.getName());
        Assert.assertNotNull(eventClassFamily);
        Assert.assertEquals(dto, eventClassFamily);
    }
}
