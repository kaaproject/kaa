/*
 * Copyright 2014-2015 CyberVision, Inc.
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
import org.kaaproject.kaa.server.common.dao.model.sql.SdkKey;
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
    public void saveSdkTokenTest() {
        SdkKey sdkKey = generateSdkKey(null, null, null);
        Assert.assertNotNull(sdkKey.getId());
    }

    @Test
    public void findSdkKeyByTokenTest() {
        String token = "someSdkToken";
        SdkKey sdkKeyToPersist = generateSdkKey(null, token, new byte[]{10, 2, 3, 4, 2, 3, 3, 4, 100, 3, 4});
        SdkKey sdkKeyLoaded = sdkKeyDao.findSdkKeyByToken(token);
        Assert.assertEquals(sdkKeyToPersist, sdkKeyLoaded);
    }

    @Test
    public void findSdkKeysByApplicationIdTest() {
        SdkKey saved = generateSdkKey(null, "token", new byte[] { 35, 16, 94, 60, 11, 52, 22, 78, 81, 97 });
        List<SdkKey> loaded = sdkKeyDao.findSdkKeysByApplicationId(saved.getApplication().getId().toString());
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(saved, loaded.get(0));
    }
}
