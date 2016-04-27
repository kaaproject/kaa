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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.model.sql.ApplicationEventFamilyMap;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateApplicationEventFamilyMapDaoTest extends HibernateAbstractTest {

    @Test
    public void removeByAppId() {
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = generateApplicationEventFamilyMap(null, null, null, 2, true);
        String id = applicationEventFamilyMaps.get(0).getStringId();
        ApplicationEventFamilyMap dto = applicationEventFamilyMapDao.findById(id);
        Assert.assertNotNull(dto);
        String appId = dto.getApplication().getStringId();
        Assert.assertNotNull(appId);

        applicationEventFamilyMapDao.removeByApplicationId(appId);
        dto = applicationEventFamilyMapDao.findById(id);
        Assert.assertNull(dto);
    }

    @Test
    public void removeById() {
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = generateApplicationEventFamilyMap(null, null, null, 2, true);
        String id = applicationEventFamilyMaps.get(0).getStringId();
        ApplicationEventFamilyMap dto = applicationEventFamilyMapDao.findById(id);
        Assert.assertNotNull(dto);

        applicationEventFamilyMapDao.removeById(id);
        dto = applicationEventFamilyMapDao.findById(id);
        Assert.assertNull(dto);
    }

    @Test
    public void findByAppIdTest() {
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = generateApplicationEventFamilyMap(null, null, null, 2, true);
        ApplicationEventFamilyMap dto = applicationEventFamilyMapDao.findById(applicationEventFamilyMaps.get(0).getStringId());
        Assert.assertNotNull(dto);
        List<ApplicationEventFamilyMap> eventFamilyMapList = applicationEventFamilyMapDao.findByApplicationId(dto.getApplication().getStringId());
        ApplicationEventFamilyMap eventFamilyMap = null;
        for (ApplicationEventFamilyMap found : eventFamilyMapList) {
            if (dto.getId().equals(found.getId())) {
                eventFamilyMap = found;
            }
        }
        Assert.assertNotNull(eventFamilyMap);
        Assert.assertEquals(dto, eventFamilyMap);
    }

    @Test
    public void testValidateApplicationEventFamilyMap() {
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = generateApplicationEventFamilyMap(null, null, null, 2, true);
        ApplicationEventFamilyMap aefm = applicationEventFamilyMaps.get(0);
        boolean result = applicationEventFamilyMapDao.validateApplicationEventFamilyMap(aefm.getApplication().getId().toString(), aefm.getEcf().getId()
                .toString(), aefm.getVersion());
        Assert.assertFalse(result);

        result = applicationEventFamilyMapDao.validateApplicationEventFamilyMap("7", "7", 7);
        Assert.assertTrue(result);
    }

    @Test
    public void testFindByIds() {
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = generateApplicationEventFamilyMap(null, null, null, 2, true);
        List<String> ids = new ArrayList<>();
        ids.add(applicationEventFamilyMaps.get(0).getId().toString());
        ids.add(applicationEventFamilyMaps.get(1).getId().toString());
        List<ApplicationEventFamilyMap> found = applicationEventFamilyMapDao.findByIds(ids);
        sortList(found);
        Assert.assertEquals(applicationEventFamilyMaps, found);
    }

    private void sortList(List<ApplicationEventFamilyMap> found){
        found.sort((o1, o2) -> (int) (o1.getId()-o2.getId()));
    }

    @Test
    public void testFindByEcfIdAndVersion() {
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = generateApplicationEventFamilyMap(null, null, null, 2, true);
        ApplicationEventFamilyMap aefm = applicationEventFamilyMaps.get(0);
        List<ApplicationEventFamilyMap> found = applicationEventFamilyMapDao.findByEcfIdAndVersion(aefm.getEcf().getId().toString(), aefm.getVersion());
        Assert.assertEquals(applicationEventFamilyMaps, found);
    }
}
