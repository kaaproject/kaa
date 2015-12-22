package org.kaaproject.kaa.server.common.dao.impl.sql.plugin;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.impl.sql.HibernateAbstractTest;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.ContractMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateContractMessageDaoTest extends HibernateAbstractTest {

    @Test
    public void testFindContractMessageByFqnAndVersion() {
        ContractMessage contractMessage = new ContractMessage();
        String fqn = "a.b.c.d";
        Integer version = 1;
        contractMessage.setFqn(fqn);
        contractMessage.setVersion(version);
        contractMessageDao.save(contractMessage);

        ContractMessage foundContractMessage = contractMessageDao.findByFqnAndVersion("abcdef", 5);
        Assert.assertNull(foundContractMessage);
        foundContractMessage = contractMessageDao.findByFqnAndVersion(fqn, version);
        Assert.assertNotNull(foundContractMessage);
        Assert.assertNotNull(foundContractMessage.getId());
    }
}
