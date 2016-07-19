/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import static org.kaaproject.kaa.common.dto.credentials.CredentialsStatus.AVAILABLE;
import static org.kaaproject.kaa.common.dto.credentials.CredentialsStatus.REVOKED;

import java.io.IOException;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.server.common.dao.model.Credentials;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CredentialsMongoDaoTest extends AbstractMongoTest {

    private static final byte[] CREDENTIALS_BODY = "credentials_body".getBytes();
    private static final String APPLICATION_ID = "application_id";

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @After
    public void afterTest() throws IOException {
        MongoDataLoader.clearDBData();
    }

    @Test
    public void testFindCredentialsById() throws Exception {
        CredentialsDto saved = this.generateCredentials(APPLICATION_ID, CREDENTIALS_BODY, AVAILABLE);
        Assert.assertNotNull(saved);
        Assert.assertNotNull(saved.getId());

        Optional<Credentials> found = this.credentialsDao.find(APPLICATION_ID, saved.getId());
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(saved, found.map(Credentials::toDto).get());
    }

    @Test
    public void testUpdateStatus() throws Exception {
        CredentialsDto credentials = this.generateCredentials(APPLICATION_ID, CREDENTIALS_BODY, AVAILABLE);
        Assert.assertNotNull(credentials);
        Assert.assertNotNull(credentials.getId());

        Optional<Credentials> updated = this.credentialsDao.updateStatus(APPLICATION_ID, credentials.getId(), REVOKED);
        Assert.assertTrue(updated.isPresent());
        Assert.assertEquals(REVOKED, updated.get().getStatus());
    }

    @Test
    public void testRemoveCredentials() throws Exception {
        CredentialsDto credentials = this.generateCredentials(APPLICATION_ID, CREDENTIALS_BODY, AVAILABLE);
        Assert.assertNotNull(credentials);
        Assert.assertNotNull(credentials.getId());

        this.credentialsDao.remove(APPLICATION_ID, credentials.getId());
        Optional<Credentials> removed = this.credentialsDao.find(APPLICATION_ID, credentials.getId());
        Assert.assertFalse(removed.isPresent());
    }
}
