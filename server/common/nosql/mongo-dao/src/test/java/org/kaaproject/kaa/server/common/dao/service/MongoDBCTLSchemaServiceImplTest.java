package org.kaaproject.kaa.server.common.dao.service;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDataLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MongoDBCTLSchemaServiceImplTest extends CTLSchemaServiceImplTest {

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @After
    public void afterTest() {
        MongoDataLoader.clearDBData();
        clearDBData();
    }
}
