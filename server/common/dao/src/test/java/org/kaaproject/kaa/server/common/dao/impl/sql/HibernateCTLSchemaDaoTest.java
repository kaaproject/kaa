package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.UserService;
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
    private CTLService ctlService;
    @Autowired
    private UserService userService;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private TenantDto tenant;

    @Before
    public void before() {
        if (tenant == null) {
            tenant = userService.findTenantByName(SUPER_TENANT);
            if (tenant == null) {
                TenantDto tn = new TenantDto();
                tn.setName(SUPER_TENANT);
                tenant = userService.saveTenant(tn);
            }
        }
    }

    @Test(expected = RuntimeException.class)
    public void saveCTLSchema() throws InterruptedException {
        ctlService.saveCTLSchema(generateCTLSchema(null, 10));
        ctlService.saveCTLSchema(generateCTLSchema(tenant.getId(), 10));
    }

    @Test(expected = RuntimeException.class)
    public void saveCTLSchemaWithSameFqnAndVersion() throws InterruptedException {
        ctlService.saveCTLSchema(generateCTLSchema(tenant.getId(), 11));
        ctlService.saveCTLSchema(generateCTLSchema(null, 11));
    }

    @Test
    @Rollback(false)
    public void multiThreadCTLSchemaSaveTest() throws InterruptedException, ExecutionException {
        List<Future<CTLSchemaDto>> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int x = i;
            list.add(executorService.submit(new Callable<CTLSchemaDto>() {
                @Override
                public CTLSchemaDto call() {
                    CTLSchemaDto sch = null;
                    try {
                        sch = ctlService.saveCTLSchema(generateCTLSchema(generateTenantDto().getId()));
                    } catch (Throwable t) {
                        LOG.warn("---> Test Catch exception {}", t.getCause(), t);
                    }
                    return sch;
                }
            }));
        }
        for (Future<CTLSchemaDto> f : list) {
            LOG.debug("id {}", f.get());
        }
    }

    private CTLSchemaDto generateCTLSchema(String tenantId) {
        return generateCTLSchema(tenantId, 100);
    }

    private CTLSchemaDto generateCTLSchema(String tenantId, int version) {
        CTLSchemaDto ctlSchema = new CTLSchemaDto();
        ctlSchema.setMetaInfo(new CTLSchemaMetaInfoDto("org.kaaproject.kaa.ctl.TestSchema", version));
        ctlSchema.setBody(UUID.randomUUID().toString());
        ctlSchema.setTenantId(tenantId);
        return ctlSchema;
    }

    private TenantDto generateTenantDto() {
        TenantDto tn = new TenantDto();
        tn.setName(UUID.randomUUID().toString());
        return userService.saveTenant(tn);
    }
}
