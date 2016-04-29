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
import org.kaaproject.kaa.server.common.dao.model.sql.History;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateHistoryDaoTest extends HibernateAbstractTest {

    @Test
    public void findByAppIdTest() {
       List<History> histories = generateHistory(null, 3);
       Assert.assertNotNull(histories);
       History first = histories.get(0);
       List<History> found = historyDao.findByAppId(first.getApplication().getId().toString());
       Assert.assertEquals(histories, found);
    }

    @Test
    public void findBySeqNumberTest() {
        List<History> histories = generateHistory(null, 3);
        Assert.assertNotNull(histories);
        History third = histories.get(2);
        History found = historyDao.findBySeqNumber(third.getApplication().getId().toString(), 3);
        Assert.assertEquals(third, found);

    }

    @Test
    public void findBySeqNumberStartTest() {
        List<History> histories = generateHistory(null, 3);
        Assert.assertNotNull(histories);
        History third = histories.get(2);
        List<History> found = historyDao.findBySeqNumberStart(third.getApplication().getId().toString(), 2);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(third, found.get(0));

    }

    @Test
    public void findBySeqNumberRangeTest() {
        List<History> histories = generateHistory(null, 3);
        Assert.assertNotNull(histories);
        History second = histories.get(1);
        List<History> found = historyDao.findBySeqNumberRange(second.getApplication().getId().toString(), 1, 2);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(second, found.get(0));
    }
}
