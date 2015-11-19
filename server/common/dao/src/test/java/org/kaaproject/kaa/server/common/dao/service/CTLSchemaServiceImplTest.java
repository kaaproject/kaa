package org.kaaproject.kaa.server.common.dao.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CTLSchemaServiceImplTest extends AbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(CTLSchemaServiceImplTest.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(10);


    private TenantDto tenant;
    private CTLSchemaDto firstSchema;
    private CTLSchemaDto secondSchema;
    private CTLSchemaDto thirdSchema;
    private CTLSchemaDto fourthSchema;
    private CTLSchemaDto mainSchema;
    private CTLSchemaDto systemSchema;


    @Before
    public void before() {
        clearDBData();
        if (tenant == null) {
            tenant = userService.findTenantByName(SUPER_TENANT);
            if (tenant == null) {
                TenantDto tn = new TenantDto();
                tn.setName(SUPER_TENANT);
                tenant = userService.saveTenant(tn);
            }
        }
        Set<CTLSchemaDto> dependency = new HashSet<>();
        firstSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN, tenant.getId(), 1, null));
        dependency.add(firstSchema);
        secondSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN, tenant.getId(), 2, null));
        dependency.add(secondSchema);
        thirdSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN, tenant.getId(), 3, null));
        dependency.add(thirdSchema);
        fourthSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN, tenant.getId(), 4, null));
        dependency.add(fourthSchema);
        mainSchema = generateCTLSchemaDto(DEFAULT_FQN, tenant.getId(), 7, null);
        mainSchema.setDependencySet(dependency);
        mainSchema = ctlService.saveCTLSchema(mainSchema);
        systemSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN, null, 50, null));
    }

    @Test
    public void testUpdateCTLSchemaScope() {
    }

    @Test
    public void testRemoveCTLSchemaByFqnAndVerAndTenantId() {
    }

    @Test
    public void testFindCTLSchemaById() {
    }

    @Test
    public void testSaveCTLSchema() {
    }

    @Test
    public void testRemoveCTLSchemaById() {
    }

    @Test
    public void testFindCTLSchemaByFqnAndVerAndTenantId() {
    }

    @Test
    public void testFindCTLSchemasByApplicationId() {
    }

    @Test
    public void testFindCTLSchemasByTenantId() {
    }

    @Test
    public void testFindSystemCTLSchemas() {
    }

    @Test
    public void testFindSystemCTLSchemasMetaInfo() {
    }

    @Test
    public void testFindLatestCTLSchemaByFqn() {
    }

    @Test
    public void testFindCTLSchemas() {
    }

    @Test
    public void testFindCTLSchemasMetaInfoByApplicationId() {
    }

    @Test
    public void testFindCTLSchemasMetaInfoByTenantId() {
    }

    @Test
    public void testFindAvailableCTLSchemas() {
    }

    @Test
    public void testFindAvailableCTLSchemasMetaInfo() {
    }

    @Test
    public void testFindCTLSchemaDependents() {
    }

    @Test
    public void testFindCTLSchemaDependentsByFqnVersionTenantId() {
    }

    @Test
    public void multiThreadCTLSchemaSaveTest() throws InterruptedException, ExecutionException {
        String tenantId = null;
        List<Future<CTLSchemaDto>> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(executorService.submit(new Callable<CTLSchemaDto>() {
                @Override
                public CTLSchemaDto call() {
                    CTLSchemaDto sch = null;
                    try {
                        sch = ctlService.saveCTLSchema(generateCTLSchemaDto(generateTenantDto().getId()));
                    } catch (Throwable t) {
                        throw t;
                    }
                    return sch;
                }
            }));
        }
        Iterator<Future<CTLSchemaDto>> iterator = list.iterator();
        while (iterator.hasNext()) {
            Future<CTLSchemaDto> f = iterator.next();
            while (!f.isDone()) {
            }
            tenantId = f.get().getTenantId();
            iterator.remove();
        }
        CTLSchemaDto schemaDto = ctlService.findCTLSchemaByFqnAndVerAndTenantId(DEFAULT_FQN, 100, tenantId);
        Assert.assertEquals(Long.valueOf(100L), schemaDto.getMetaInfo().getCount());
    }
}
