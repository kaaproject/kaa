package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointUserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        EndpointUserConfigurationDto userConfigurationDto = generateEndpointUserConfiguration(null, null, null);
        Assert.assertNotNull(userConfigurationDto);
        Assert.assertEquals(userConfigurationDto, new MongoEndpointUserConfiguration(userConfigurationDto).toDto());
    }

    @Test
    public void findByUserIdAndAppTokenAndSchemaVersionTest() {
        EndpointUserDto userDto = generateEndpointUser(null);
        ApplicationDto appDto = generateApplication();
        int foundVersion = random.nextInt();
        EndpointUserConfigurationDto firstUserConfigurationDto = generateEndpointUserConfiguration(userDto, appDto, foundVersion);
        generateEndpointUserConfiguration(userDto, appDto, random.nextInt());
        generateEndpointUserConfiguration(null, null, null);
        MongoEndpointUserConfiguration found = endpointUserConfigurationDao.findByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), foundVersion);
        Assert.assertEquals(firstUserConfigurationDto, found.toDto());
    }

    @Test
    public void removeByUserIdAndAppTokenAndSchemaVersionTest() {
        EndpointUserDto userDto = generateEndpointUser(null);
        ApplicationDto appDto = generateApplication();
        int removedVersion = random.nextInt();
        generateEndpointUserConfiguration(userDto, appDto, removedVersion);
        generateEndpointUserConfiguration(userDto, appDto, random.nextInt());
        generateEndpointUserConfiguration(userDto, appDto, random.nextInt());
        endpointUserConfigurationDao.removeByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), removedVersion);
        MongoEndpointUserConfiguration removed = endpointUserConfigurationDao.findByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), removedVersion);
        Assert.assertNull(removed);
        List<MongoEndpointUserConfiguration> foundList = endpointUserConfigurationDao.findByUserId(userDto.getId());
        Assert.assertEquals(2, foundList.size());
    }

    @Test
    public void findByUserIdTest() {
        EndpointUserDto userDto = generateEndpointUser(null);
        ApplicationDto appDto = generateApplication();
        EndpointUserConfigurationDto firstUserConfigurationDto = generateEndpointUserConfiguration(userDto, appDto, random.nextInt());
        EndpointUserConfigurationDto secondUserConfigurationDto = generateEndpointUserConfiguration(userDto, appDto, random.nextInt());
        List<MongoEndpointUserConfiguration> expectedList = new ArrayList<>();
        expectedList.add(new MongoEndpointUserConfiguration(firstUserConfigurationDto));
        expectedList.add(new MongoEndpointUserConfiguration(secondUserConfigurationDto));
        generateEndpointUserConfiguration(null, null, null);
        List<MongoEndpointUserConfiguration> foundList = endpointUserConfigurationDao.findByUserId(userDto.getId());
        Assert.assertEquals(expectedList.size(), foundList.size());
        Assert.assertEquals(expectedList, foundList);
    }
}
