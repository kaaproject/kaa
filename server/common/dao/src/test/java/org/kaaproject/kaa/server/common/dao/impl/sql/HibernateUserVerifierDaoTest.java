package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.model.sql.UserVerifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateUserVerifierDaoTest extends HibernateAbstractTest {

    @Test
    public void testFindByAppIdAndVerifierToken() {
        String token = UUID.randomUUID().toString();
        UserVerifier expected = generateUserVerifier(null, token);
        generateUserVerifier(expected.getApplication(), UUID.randomUUID().toString());
        generateUserVerifier(null, UUID.randomUUID().toString());
        UserVerifier found = verifierDao.findByAppIdAndVerifierToken(expected.getApplication().getStringId(), token);
        Assert.assertEquals(expected, found);
        UserVerifier expectNull = verifierDao.findByAppIdAndVerifierToken(expected.getApplication().getStringId(), UUID.randomUUID().toString());
        Assert.assertNull(expectNull);
    }

}
