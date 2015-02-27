package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointUserConfigurationMongoDaoTest extends AbstractMongoTest {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointUserConfigurationMongoDaoTest.class);

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() throws IOException {
        LOG.info("EndpointProfileMongoDaoTest init before tests.");
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() throws IOException {
        MongoDataLoader.clearDBData();
    }

    @Test
    public void saveEndpointUserConfigurationDtoTest() {
        generateEndpointUserConfiguration(null, null, null);
    }

    @Test
    public void findByUserIdAndAppTokenAndSchemaVersionTest() {

    }

    @Test
    public void removeByUserIdAndAppTokenAndSchemaVersionTest() {

    }
}
