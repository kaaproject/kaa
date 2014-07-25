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
package org.kaaproject.kaa.server.common.dao.impl.mongo;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.server.common.dao.impl.SecureUserDao;
import org.kaaproject.kaa.server.common.dao.model.mongo.SecureUser;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.test.util.ReflectionTestUtils;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class SecureUserMongoDaoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecureUserMongoDaoTest.class);

    SecureUserDao<SecureUser> secureUserMongoDao;
    MongoTemplate mongoTemplate;
    MongoConverter mongoConverter;
    DB dB;
    DBCollection dBCollection;

    @Before
    public void beforeTest() {
        secureUserMongoDao = new SecureUserMongoDao();
        mongoTemplate = mock(MongoTemplate.class);
        mongoConverter = mock(MongoConverter.class);
        dB = mock(DB.class);
        dBCollection = mock(DBCollection.class);
        ReflectionTestUtils.setField(secureUserMongoDao, "mongoTemplate", mongoTemplate);
    }

    @Test
    public void saveUserTest() {

        when(mongoTemplate.getConverter()).thenReturn(mongoConverter);
        when(mongoTemplate.getDb()).thenReturn(dB);
        when(dB.getCollection("system.users")).thenReturn(dBCollection);

        secureUserMongoDao.saveUser(new SecureUser());

        verify(mongoConverter).write(Mockito.any(Object.class), Mockito.any(DBObject.class));
        verify(dBCollection).save(Mockito.any(DBObject.class));
    }
}
