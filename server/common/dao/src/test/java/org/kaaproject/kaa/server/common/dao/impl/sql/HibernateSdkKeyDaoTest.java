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
import org.kaaproject.kaa.server.common.dao.model.sql.SdkProfile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateSdkKeyDaoTest extends HibernateAbstractTest {

    @Test
    public void saveTest() {
        SdkProfile saved = this.generateSdkProfile(null, null);
        Assert.assertNotNull(saved.getId());
    }

    @Test
    public void findSdkProfileByTokenTest() {
        String token = HibernateSdkKeyDaoTest.class.getName();
        SdkProfile saved = this.generateSdkProfile(null, token);
        SdkProfile loaded = sdkProfileDao.findSdkProfileByToken(token);
        Assert.assertEquals(saved, loaded);
    }

    @Test
    public void findSdkProfilesByApplicationIdTest() {
        SdkProfile saved = this.generateSdkProfile(null, null);
        List<SdkProfile> loaded = sdkProfileDao.findSdkProfileByApplicationId(saved.getApplication().getId().toString());
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(saved, loaded.get(0));
    }
}
