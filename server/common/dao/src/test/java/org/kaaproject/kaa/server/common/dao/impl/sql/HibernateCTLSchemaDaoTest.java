package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.server.common.dao.UserService;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaScope;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.kaaproject.kaa.server.common.dao.service.CTLSchemaServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HibernateCTLSchemaDaoTest extends HibernateAbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateCTLSchemaDaoTest.class);
    public static final String SUPER_TENANT = "SuperTenant";
    @Autowired
    private CTLSchemaServiceImpl ctlSchemaService;
    @Autowired
    private UserService userService;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private TenantDto tenant;

    @Before
    public void go() {
        if (tenant == null) {
            tenant = userService.findTenantByName(SUPER_TENANT);
            if (tenant == null) {
                TenantDto tn = new TenantDto();
                tn.setName(SUPER_TENANT);
                tenant = userService.saveTenant(tn);
            }
        }
    }

    @Test
    @Rollback(false)
    public void saveCTLSchema() throws InterruptedException {
        ctlSchemaService.save(generateCTLSchema(new Tenant(tenant)));
        ctlSchemaService.save(generateCTLSchema(null));
    }

    @Test
    @Rollback(false)
    public void multiThreadCTLSchemaSaveTest() throws InterruptedException, ExecutionException {
        List<Future<CTLSchema>> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int x = i;
            list.add(executorService.submit(new Callable<CTLSchema>() {
                @Override
                public CTLSchema call() {
                    CTLSchema sch = null;
                    try {
                        if (x % 2 == 0) {
                            sch = ctlSchemaService.save(generateCTLSchema(new Tenant(tenant)));
                        } else {
                            sch = ctlSchemaService.save(generateCTLSchema(null));
                        }
                    } catch (Throwable t) {
                        LOG.warn("Catch exception {}", t.getCause(), t);
                    }
                    return sch;
                }
            }));
        }
        for (Future<CTLSchema> f : list) {
            LOG.debug("id {}", f.get().getId());
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
