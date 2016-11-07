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

import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.model.sql.LogSchema;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateLogSchemaDaoTest extends HibernateAbstractTest {

    @Test
    public void findByApplicationIdTest() {
        LogSchema logSchema = generateLogSchema(null, 1, null, 1).get(0);
        Assert.assertNotNull(logSchema);
        List<LogSchema> schemaList = logSchemaDao.findByApplicationId(logSchema.getApplication().getStringId());
        Assert.assertNotNull(schemaList);
        LogSchema found = null;
        for (LogSchema schema : schemaList) {
            if (schema.getId().equals(logSchema.getId())) {
                found = schema;
            }
        }
        Assert.assertEquals(logSchema, found);
    }

    @Test
    public void findByAppIdAndVersionTest() {
        LogSchema logSchema = generateLogSchema(null, 1, null, 1).get(0);
        Assert.assertNotNull(logSchema);
        LogSchema found = logSchemaDao.findByApplicationIdAndVersion(logSchema.getApplication().getStringId(), logSchema.getVersion());
        Assert.assertEquals(logSchema, found);
    }

    @Test
    public void removeByApplicationIdTest() {
        LogSchema logSchema = generateLogSchema(null, 1, null, 1).get(0);
        Assert.assertNotNull(logSchema);
        logSchemaDao.removeByApplicationId(logSchema.getApplication().getStringId());
        LogSchema schema = logSchemaDao.findById(logSchema.getStringId());
        Assert.assertNull(schema);
    }

    @Test
    public void removeByIdTest() {
        LogSchema logSchema = generateLogSchema(null, 1, null, 1).get(0);
        Assert.assertNotNull(logSchema);
        logSchemaDao.removeById(logSchema.getStringId());
        LogSchema schema = logSchemaDao.findById(logSchema.getStringId());
        Assert.assertNull(schema);
    }
}
