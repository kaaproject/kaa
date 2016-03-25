package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.server.common.dao.model.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.kaaproject.kaa.common.dto.credentials.CredentialsStatus.AVAILABLE;
import static org.kaaproject.kaa.common.dto.credentials.CredentialsStatus.IN_USE;
import static org.kaaproject.kaa.common.dto.credentials.CredentialsStatus.REVOKED;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CredentialsMongoDaoTest extends AbstractMongoTest {
    private static final Logger LOG = LoggerFactory.getLogger(CredentialsMongoDaoTest.class);
    private static final byte[] CREDENTIALS_BODY = "credentials_body".getBytes();

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
    public void testSaveCredentials() {
        CredentialsDto credentials = this.generateCredentials(CREDENTIALS_BODY, AVAILABLE);
        Credentials saved = this.credentialsDao.save(credentials);
        Assert.assertNotNull(saved);
    }

    @Test
    public void testFindCredentialsById() {
        CredentialsDto credentials = this.generateCredentials(CREDENTIALS_BODY, AVAILABLE);
        Credentials saved = this.credentialsDao.save(credentials);
        Assert.assertNotNull(saved);

        Credentials finded = this.credentialsDao.findById(saved.getId());
        Assert.assertNotNull(finded);
        Assert.assertEquals(saved, finded);
    }

    @Test
    public void testUpdateStatus() {
        CredentialsDto credentials = this.generateCredentials(CREDENTIALS_BODY, AVAILABLE);
        Credentials saved = this.credentialsDao.save(credentials);
        Assert.assertNotNull(saved);

        Credentials updated = this.credentialsDao.updateStatusById(saved.getId(), REVOKED);
        Assert.assertNotNull(updated);
        Assert.assertEquals(REVOKED, updated.getStatus());
    }

    @Test
    public void testRemoveCredentials() {
        CredentialsDto credentials = this.generateCredentials(CREDENTIALS_BODY, AVAILABLE);
        Credentials saved = this.credentialsDao.save(credentials);
        Assert.assertNotNull(saved);

        this.credentialsDao.removeById(saved.getId());
        Credentials removed = this.credentialsDao.findById(saved.getId());
        Assert.assertNull(removed);
    }
}
