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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateLogAppenderTest extends HibernateAbstractTest{


    private static final Logger LOG = LoggerFactory.getLogger(HibernateUserVerifierTest.class);

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
        LogAppender appender = generateLogAppender(null);
        List<LogAppender> result = appenderDao.findByAppId("");
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void findByAppAndSchemaVersionIdTest() {
        LogAppender appender = generateLogAppender(null);
        Application app = appender.getApplication();
        List<LogAppender> result = appenderDao.findByAppIdAndSchemaVersion(app.getId().toString(), appender.getMaxLogSchemaVersion());
        Assert.assertEquals(result.size(), 1);
    }

    @Test
    public void findByAppIdAndSchemaVersionBlankIdTest() {
        LogAppender appender = generateLogAppender(null);
        List<LogAppender> result = appenderDao.findByAppIdAndSchemaVersion("", appender.getMinLogSchemaVersion());
        Assert.assertTrue(result.isEmpty());
    }

}
