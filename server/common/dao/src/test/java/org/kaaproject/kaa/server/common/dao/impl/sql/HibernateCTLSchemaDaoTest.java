package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaScope;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateCTLSchemaDaoTest extends HibernateAbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateCTLSchemaDaoTest.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Tenant tenant;

    @Before
    @Rollback(false)
    public void go() {
        if (tenant == null) {
            tenant = generateTenant();
        }
    }

    @After
    public void afterTest() throws InterruptedException {
        LOG.debug("If sdsfsddf");
    }

    @Test
    @Rollback(false)
    public void saveCTLSchema() throws InterruptedException {
        ctlSchemaDao.save(generateCTLSchema(tenant));
        ctlSchemaDao.save(generateCTLSchema(generateTenant()));
    }

    @Test
    @Rollback(false)
    public void multiThreadCTLSchemaSaveTest() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    ctlSchemaDao.save(generateCTLSchema(generateTenant()));
                }
            });
        }
    }

    private CTLSchema generateCTLSchema(Tenant tenant) {
        CTLSchema ctlSchema = new CTLSchema();
        ctlSchema.setBody(UUID.randomUUID().toString());
        ctlSchema.setVersion(1);
        ctlSchema.setTenant(tenant);
        ctlSchema.setFqn("org.kaaproject.kaa.ctl.TestSchema");
        ctlSchema.setScope(CTLSchemaScope.SYSTEM);
        return ctlSchema;
    }
}
