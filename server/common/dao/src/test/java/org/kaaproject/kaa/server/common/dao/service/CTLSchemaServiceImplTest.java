package org.kaaproject.kaa.server.common.dao.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class CTLSchemaServiceImplTest extends AbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(CTLSchemaServiceImplTest.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    private TenantDto tenant;
    private ApplicationDto appDto;
    private CTLSchemaDto firstSchema;
    private CTLSchemaDto secondSchema;
    private CTLSchemaDto thirdSchema;
    private CTLSchemaDto fourthSchema;
    private CTLSchemaDto mainSchema;
    private CTLSchemaDto systemSchema;
    private CTLSchemaDto tenantSchema;
    private CTLSchemaDto appSchema;
    private List<CTLSchemaDto> allSchemaList;


    @Before
    public void before() {
        clearDBData();
        if (tenant == null) {
            tenant = userService.findTenantByName(SUPER_TENANT);
            if (tenant == null) {
                TenantDto tn = new TenantDto();
                tn.setName(SUPER_TENANT);
                tenant = userService.saveTenant(tn);
                appDto = generateApplicationDto(tenant.getId());
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
        tenantSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN, tenant.getId(), 77, null));
        CTLSchemaDto unsaved = generateCTLSchemaDto(DEFAULT_FQN, tenant.getId(), 80, CTLSchemaScopeDto.APPLICATION);
        unsaved.setAppId(appDto.getId());
        appSchema = ctlService.saveCTLSchema(unsaved);
        allSchemaList = Arrays.asList(firstSchema, secondSchema, thirdSchema, fourthSchema, mainSchema, systemSchema, tenantSchema, appSchema);
    }

    @Test
    public void testUpdateCTLSchema() {
    }

    @Test
    public void testFindCTLSchemasMetaInfoByApplicationId() {
    }

    @Test
    public void testFindCTLSchemasMetaInfoByTenantId() {
    }

    @Test
    public void testRemoveCTLSchemaByFqnAndVerAndTenantId() {
        String schemaId = tenantSchema.getId();
        ctlService.removeCTLSchemaByFqnAndVerAndTenantId(tenantSchema.getMetaInfo().getFqn(),
                tenantSchema.getMetaInfo().getVersion(), tenantSchema.getTenantId());
        Assert.assertNull(ctlService.findCTLSchemaById(schemaId));
    }

    @Test(expected = IncorrectParameterException.class)
    public void testRemoveCTLSchemaByFqnAndVerAndWithoutTenantId() {
        String schemaId = systemSchema.getId();
        ctlService.removeCTLSchemaByFqnAndVerAndTenantId(systemSchema.getMetaInfo().getFqn(),
                systemSchema.getMetaInfo().getVersion(), systemSchema.getTenantId());
        Assert.assertNull(ctlService.findCTLSchemaById(schemaId));
    }

    @Test
    public void testRemoveCTLSchemaById() {
        String schemaId = mainSchema.getId();
        ctlService.removeCTLSchemaById(schemaId);
        Assert.assertNull(ctlService.findCTLSchemaById(schemaId));
    }

    @Test(expected = IncorrectParameterException.class)
    public void testRemoveCTLSchemaByIdWithNull() {
        ctlService.removeCTLSchemaById(null);
    }

    @Test
    public void testFindCTLSchemaByFqnAndVerAndTenantId() {
        CTLSchemaMetaInfoDto metaInfo = firstSchema.getMetaInfo();
        CTLSchemaDto found = ctlService.findCTLSchemaByFqnAndVerAndTenantId(metaInfo.getFqn(), metaInfo.getVersion(), firstSchema.getTenantId());
        Assert.assertEquals(firstSchema, found);
    }

    @Test
    public void testFindCTLSchemasByApplicationId() {
        List<CTLSchemaDto> appSchemas = ctlService.findCTLSchemasByApplicationId(appDto.getId());
        Assert.assertEquals(Arrays.asList(appSchema), appSchemas);
    }

    @Test
    public void testFindCTLSchemasByTenantId() {
        List<CTLSchemaDto> appSchemas = ctlService.findCTLSchemasByTenantId(tenant.getId());
        Assert.assertEquals(getIdsDto(Arrays.asList(firstSchema, secondSchema, thirdSchema, fourthSchema, mainSchema, tenantSchema, appSchema)),
                getIdsDto(appSchemas));
    }

    @Test
    public void testFindSystemCTLSchemas() {
        List<CTLSchemaDto> appSchemas = ctlService.findSystemCTLSchemas();
        Assert.assertEquals(getIdsDto(Arrays.asList(systemSchema)), getIdsDto(appSchemas));
    }

    @Test
    public void testFindSystemCTLSchemasMetaInfo() {
        List<CTLSchemaMetaInfoDto> appSchemas = ctlService.findSystemCTLSchemasMetaInfo();
        Assert.assertEquals(getIdsDto(Arrays.asList(systemSchema.getMetaInfo())), getIdsDto(appSchemas));
    }

    @Test
    public void testFindLatestCTLSchemaByFqn() {
        CTLSchemaDto latest = ctlService.findLatestCTLSchemaByFqn(DEFAULT_FQN);
        Assert.assertEquals(Integer.valueOf(80), latest.getMetaInfo().getVersion());
    }

    @Test
    public void testFindAvailableCTLSchemas() {
        List<CTLSchemaDto> appSchemas = ctlService.findAvailableCTLSchemas(tenant.getId());
        Assert.assertEquals(getIdsDto(allSchemaList), getIdsDto(appSchemas));
    }

    @Test
    public void testFindAvailableCTLSchemasMetaInfo() {
        List<CTLSchemaDto> appSchemas = ctlService.findAvailableCTLSchemas(tenant.getId());
        Assert.assertEquals(allSchemaList.size(), appSchemas.size());
    }

    @Test
    public void testFindCTLSchemaDependents() {
        List<CTLSchemaDto> appSchemas = ctlService.findAvailableCTLSchemas(tenant.getId());
        Assert.assertEquals(allSchemaList.size(), appSchemas.size());
    }

    @Test
    public void testFindCTLSchemaDependentsByFqnVersionTenantId() {
        List<CTLSchemaDto> appSchemas = ctlService.findCTLSchemaDependents(firstSchema.getMetaInfo().getFqn(),
                firstSchema.getMetaInfo().getVersion(), tenant.getId());
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(secondSchema.getMetaInfo().getFqn(),
                secondSchema.getMetaInfo().getVersion(), tenant.getId());
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(thirdSchema.getMetaInfo().getFqn(),
                thirdSchema.getMetaInfo().getVersion(), tenant.getId());
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(fourthSchema.getMetaInfo().getFqn(),
                fourthSchema.getMetaInfo().getVersion(), tenant.getId());
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(mainSchema.getMetaInfo().getFqn(),
                mainSchema.getMetaInfo().getVersion(), tenant.getId());
        Assert.assertTrue(appSchemas.isEmpty());

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
