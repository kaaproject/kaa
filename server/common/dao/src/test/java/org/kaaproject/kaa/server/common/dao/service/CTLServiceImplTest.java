/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.dao.service;

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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
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

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class CTLServiceImplTest extends AbstractTest {

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    private TenantDto tenant;
    private ApplicationDto appDto;
    private CTLSchemaDto firstSchema;
    private CTLSchemaDto secondSchema;
    private CTLSchemaDto thirdSchema;
    private CTLSchemaDto fourthSchema;
    private CTLSchemaDto mainSchema;
    private CTLSchemaDto defaultSystemSchema;
    private CTLSchemaDto systemSchema;
    private CTLSchemaDto tenantSchema;
    private CTLSchemaDto appSchema;

    private static final String TEST_CTL_SCHEMA_ALPHA = "dao/ctl/alpha.json";
    private static final String TEST_CTL_SCHEMA_ALPHA_FLAT = "dao/ctl/alphaFlat.json";
    private static final String TEST_CTL_SCHEMA_BETA = "dao/ctl/beta.json";
    private static final String TEST_CTL_SCHEMA_GAMMA = "dao/ctl/gamma.json";

    private CTLSchemaDto alpha;
    private CTLSchemaDto beta;
    private CTLSchemaDto gamma;

    @Before
    public void before() throws Exception {
        clearDBData();
        if (tenant == null) {
            tenant = userService.findTenantByName(SUPER_TENANT);
            if (tenant == null) {
                TenantDto tn = new TenantDto();
                tn.setName(SUPER_TENANT);
                tenant = userService.saveTenant(tn);
                appDto = generateApplicationDto(tenant.getId());
                List<CTLSchemaDto> ctlSchemas = ctlService.findSystemCTLSchemas();
                defaultSystemSchema = ctlSchemas.get(0);
            }
        }
        Set<CTLSchemaDto> dependency = new HashSet<>();
        firstSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN+1, tenant.getId(), 1, null));
        dependency.add(firstSchema);
        secondSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN+2, tenant.getId(), 2, null));
        dependency.add(secondSchema);
        thirdSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN+3, tenant.getId(), 3, null));
        dependency.add(thirdSchema);
        fourthSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN+4, tenant.getId(), 4, null));
        dependency.add(fourthSchema);
        mainSchema = generateCTLSchemaDto(DEFAULT_FQN, tenant.getId(), 7, null);
        mainSchema.setDependencySet(dependency);
        mainSchema = ctlService.saveCTLSchema(mainSchema);
        systemSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN, null, 50, null));
        tenantSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN, tenant.getId(), 77, null));
        CTLSchemaDto unsaved = generateCTLSchemaDto(DEFAULT_FQN, tenant.getId(), 80, CTLSchemaScopeDto.APPLICATION);
        unsaved.setApplicationId(appDto.getId());
        appSchema = ctlService.saveCTLSchema(unsaved);

        gamma = new CTLSchemaDto();
        CTLSchemaMetaInfoDto gammaMetaInfo = new CTLSchemaMetaInfoDto("org.kaaproject.kaa.Gamma", 1);
        gammaMetaInfo.setScope(CTLSchemaScopeDto.TENANT);
        gamma.setMetaInfo(gammaMetaInfo);
        gamma.setBody(readSchemaFileAsString(TEST_CTL_SCHEMA_GAMMA));
        gamma = ctlService.saveCTLSchema(gamma);
        
        gamma = ctlService.findCTLSchemaById(gamma.getId());
        
        beta = new CTLSchemaDto();
        CTLSchemaMetaInfoDto betaMetaInfo = new CTLSchemaMetaInfoDto("org.kaaproject.kaa.Beta", 1);
        betaMetaInfo.setScope(CTLSchemaScopeDto.TENANT);
        Set<CTLSchemaDto> betaDependencies = new HashSet<>();
        betaDependencies.add(gamma);
        beta.setMetaInfo(betaMetaInfo);
        beta.setDependencySet(betaDependencies);
        beta.setBody(readSchemaFileAsString(TEST_CTL_SCHEMA_BETA));
        beta = ctlService.saveCTLSchema(beta);

        beta = ctlService.findCTLSchemaById(beta.getId());
        
        alpha = new CTLSchemaDto();
        CTLSchemaMetaInfoDto alphaMetaInfo = new CTLSchemaMetaInfoDto("org.kaaproject.kaa.Alpha", 1);
        alphaMetaInfo.setScope(CTLSchemaScopeDto.TENANT);
        Set<CTLSchemaDto> alphaDependencies = new HashSet<>();
        alphaDependencies.add(beta);
        alpha.setMetaInfo(alphaMetaInfo);
        alpha.setDependencySet(alphaDependencies);
        alpha.setBody(readSchemaFileAsString(TEST_CTL_SCHEMA_ALPHA));
        
        alpha = ctlService.saveCTLSchema(alpha);
        
        alpha = ctlService.findCTLSchemaById(alpha.getId());
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
        ctlService.removeCTLSchemaByFqnAndVerAndTenantId(tenantSchema.getMetaInfo().getFqn(), tenantSchema.getMetaInfo().getVersion(),
                tenantSchema.getTenantId());
        Assert.assertNull(ctlService.findCTLSchemaById(schemaId));
    }

    @Test
    public void testRemoveCTLSchemaByFqnAndVerAndWithoutTenantId() {
        String schemaId = systemSchema.getId();
        ctlService.removeCTLSchemaByFqnAndVerAndTenantId(systemSchema.getMetaInfo().getFqn(), systemSchema.getMetaInfo().getVersion(),
                systemSchema.getTenantId());
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
    public void testFindSystemCTLSchemas() {
        List<CTLSchemaDto> appSchemas = ctlService.findSystemCTLSchemas();
        Assert.assertEquals(getIdsDto(Arrays.asList(defaultSystemSchema, systemSchema)), getIdsDto(appSchemas));
    }

    @Test
    public void testFindSystemCTLSchemasMetaInfo() {
        List<CTLSchemaMetaInfoDto> appSchemas = ctlService.findSystemCTLSchemasMetaInfo();
        Assert.assertEquals(Arrays.asList(defaultSystemSchema.getMetaInfo(), systemSchema.getMetaInfo()), appSchemas);
    }

    @Test
    public void testFindLatestCTLSchemaByFqn() {
        CTLSchemaDto latest = ctlService.findLatestCTLSchemaByFqn(DEFAULT_FQN);
        Assert.assertEquals(Integer.valueOf(80), latest.getMetaInfo().getVersion());
    }

    @Test
    public void testFindCTLSchemaDependentsByFqnVersionTenantId() {
        List<CTLSchemaDto> appSchemas = ctlService.findCTLSchemaDependents(firstSchema.getMetaInfo().getFqn(), firstSchema.getMetaInfo().getVersion(),
                tenant.getId());
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(secondSchema.getMetaInfo().getFqn(), secondSchema.getMetaInfo().getVersion(), tenant.getId());
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(thirdSchema.getMetaInfo().getFqn(), thirdSchema.getMetaInfo().getVersion(), tenant.getId());
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(fourthSchema.getMetaInfo().getFqn(), fourthSchema.getMetaInfo().getVersion(), tenant.getId());
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(mainSchema.getMetaInfo().getFqn(), mainSchema.getMetaInfo().getVersion(), tenant.getId());
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

    @Test
    public void testShallowExport() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode expected = mapper.readTree(readSchemaFileAsString(TEST_CTL_SCHEMA_ALPHA));
        JsonNode actual = mapper.readTree(ctlService.shallowExport(alpha).getFileData());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testFlatExport() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode expected = mapper.readTree(readSchemaFileAsString(TEST_CTL_SCHEMA_ALPHA_FLAT));
        JsonNode actual = mapper.readTree(ctlService.flatExport(alpha).getFileData());
        Assert.assertEquals(expected, actual);
    }
}
