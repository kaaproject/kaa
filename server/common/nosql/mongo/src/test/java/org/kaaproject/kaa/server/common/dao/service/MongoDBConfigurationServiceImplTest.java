package org.kaaproject.kaa.server.common.dao.service;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDataLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MongoDBConfigurationServiceImplTest extends ConfigurationServiceImplTest {

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
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() {
        clearDBData();
    }
}
