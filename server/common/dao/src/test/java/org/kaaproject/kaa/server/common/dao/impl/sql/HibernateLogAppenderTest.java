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
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.LogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateLogAppenderTest extends HibernateAbstractTest {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateLogAppenderTest.class);

    @Test
    public void findByAppIdTest() {
        LogAppender appender = generateLogAppender(null);
        Application app = appender.getApplication();
        List<LogAppender> result = appenderDao.findByAppId(app.getId().toString());
        System.out.println(result.get(0));
        Assert.assertEquals(result.size(), 1);
    }

    @Test
    public void findByAppIdBlankIdTest() {
        List<LogAppender> result = appenderDao.findByAppId("");
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void findByAppIdAndSchemaVersionBlankIdTest() {
        LogAppender appender = generateLogAppender(null);
        List<LogAppender> result = appenderDao.findByAppIdAndSchemaVersion("", appender.getMinLogSchemaVersion());
        Assert.assertTrue(result.isEmpty());
    }

}
