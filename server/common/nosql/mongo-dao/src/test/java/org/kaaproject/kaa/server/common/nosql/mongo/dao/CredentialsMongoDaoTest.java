package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import static org.kaaproject.kaa.common.dto.credentials.CredentialsStatus.AVAILABLE;
import static org.kaaproject.kaa.common.dto.credentials.CredentialsStatus.REVOKED;

import java.io.IOException;

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
    public void testFindCredentialsById() {
        CredentialsDto saved = this.generateCredentials(APPLICATION_ID, CREDENTIALS_BODY, AVAILABLE);
        Assert.assertNotNull(saved);
        Assert.assertNotNull(saved.getId());

        Credentials found = this.credentialsDao.find(APPLICATION_ID, saved.getId());
        Assert.assertNotNull(found);
        Assert.assertEquals(saved, found.toDto());
    }

    @Test
    public void testUpdateStatus() {
        CredentialsDto credentials = this.generateCredentials(APPLICATION_ID, CREDENTIALS_BODY, AVAILABLE);
        Assert.assertNotNull(credentials);
        Assert.assertNotNull(credentials.getId());

        Credentials updated = this.credentialsDao.updateStatus(APPLICATION_ID, credentials.getId(), REVOKED);
        Assert.assertNotNull(updated);
        Assert.assertEquals(REVOKED, updated.getStatus());
    }

    @Test
    public void testRemoveCredentials() {
        CredentialsDto credentials = this.generateCredentials(APPLICATION_ID, CREDENTIALS_BODY, AVAILABLE);
        Assert.assertNotNull(credentials);
        Assert.assertNotNull(credentials.getId());

        this.credentialsDao.remove(APPLICATION_ID, credentials.getId());
        Credentials removed = this.credentialsDao.find(APPLICATION_ID, credentials.getId());
        Assert.assertNull(removed);
    }
}
