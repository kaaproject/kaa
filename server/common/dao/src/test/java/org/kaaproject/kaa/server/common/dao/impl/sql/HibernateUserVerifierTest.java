package org.kaaproject.kaa.server.common.dao.impl.sql;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.UserVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateUserVerifierTest extends HibernateAbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateUserVerifierTest.class);

    @Test
    public void findByAppIdTest() {
        UserVerifier verifier = generateUserVerifier(null, null);
        Application app = verifier.getApplication();
        List<UserVerifier> result = verifierDao.findByAppId(app.getId().toString());
        Assert.assertEquals(result.size(), 1);
    }

    @Test
    public void findByAppIdBlankIdTest() {
        UserVerifier verifier = generateUserVerifier(null, null);
        List<UserVerifier> result = verifierDao.findByAppId("");
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void findByAppIdAndVerifierTokenTest() {
        UserVerifier verifier = generateUserVerifier(null, null);
        Application app = verifier.getApplication();
        UserVerifier result = verifierDao.findByAppIdAndVerifierToken(app.getId().toString(), verifier.getVerifierToken());
        Assert.assertEquals(result, null);
    }

    @Test
    public void findByAppIdAndVerifierTokenBlankIdTest() {
        UserVerifier verifier = generateUserVerifier(null, null);
        UserVerifier result = verifierDao.findByAppIdAndVerifierToken("", verifier.getVerifierToken());
        Assert.assertNull(result);
    }

}
