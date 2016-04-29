/*
 * Copyright 2014-2016 CyberVision, Inc.
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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
public class CTLServiceImplTest extends AbstractTest {

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    private TenantDto tenant;
    private ApplicationDto appDto;
    private ApplicationDto appDto2;
    private CTLSchemaDto firstSchema;
    private CTLSchemaDto secondSchema;
    private CTLSchemaDto thirdSchema;
    private CTLSchemaDto fourthSchema;
    private CTLSchemaDto mainSchema;
    private CTLSchemaDto defaultSystemSchema;
    private CTLSchemaDto systemSchema;
    private CTLSchemaDto tenantSchema;
    private CTLSchemaDto tenantSchema2;
    private CTLSchemaDto appSchema;
    private CTLSchemaDto app2Schema;
    private CTLSchemaDto appSchema2;
    private CTLSchemaDto appSchema3;

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
                appDto = generateApplicationDto(tenant.getId(), "The app 1");
                appDto2 = generateApplicationDto(tenant.getId(), "The app 2");
                List<CTLSchemaDto> ctlSchemas = ctlService.findSystemCTLSchemas();
                defaultSystemSchema = ctlSchemas.get(0);
            }
        }
        Set<CTLSchemaDto> dependency = new HashSet<>();
        firstSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN+1, tenant.getId(), null, 1));
        dependency.add(firstSchema);
        secondSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN+2, tenant.getId(), null, 2));
        dependency.add(secondSchema);
        thirdSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN+3, tenant.getId(), null, 3));
        dependency.add(thirdSchema);
        fourthSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN+4, tenant.getId(), null, 4));
        dependency.add(fourthSchema);
        mainSchema = generateCTLSchemaDto(DEFAULT_FQN+5, tenant.getId(), null, 7);
        mainSchema.setDependencySet(dependency);
        mainSchema = ctlService.saveCTLSchema(mainSchema);
        systemSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN+6, null, null, 50));
        tenantSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN+7, tenant.getId(), null, 77));
        tenantSchema2 = ctlService.saveCTLSchema(generateCTLSchemaDto(DEFAULT_FQN+7, tenant.getId(), null, 78));
        CTLSchemaDto unsaved = generateCTLSchemaDto(DEFAULT_FQN+8, tenant.getId(), appDto.getId(), 80);
        appSchema = ctlService.saveCTLSchema(unsaved);
        unsaved = generateCTLSchemaDto(DEFAULT_FQN+8, tenant.getId(), appDto.getId(), 81);
        appSchema2 = ctlService.saveCTLSchema(unsaved);
        unsaved = generateCTLSchemaDto(DEFAULT_FQN+9, tenant.getId(), appDto.getId(), 2);
        appSchema3 = ctlService.saveCTLSchema(unsaved);
        unsaved = generateCTLSchemaDto(DEFAULT_FQN+8, tenant.getId(), appDto2.getId(), 11);
        app2Schema = ctlService.saveCTLSchema(unsaved);        

        gamma = new CTLSchemaDto();
        CTLSchemaMetaInfoDto gammaMetaInfo = new CTLSchemaMetaInfoDto("org.kaaproject.kaa.Gamma", tenant.getId());
        gamma.setMetaInfo(gammaMetaInfo);
        gamma.setVersion(1);
        gamma.setBody(readSchemaFileAsString(TEST_CTL_SCHEMA_GAMMA));
        gamma = ctlService.saveCTLSchema(gamma);
        
        gamma = ctlService.findCTLSchemaById(gamma.getId());
        
        beta = new CTLSchemaDto();
        CTLSchemaMetaInfoDto betaMetaInfo = new CTLSchemaMetaInfoDto("org.kaaproject.kaa.Beta", tenant.getId());
        beta.setMetaInfo(betaMetaInfo);
        beta.setVersion(1);
        Set<CTLSchemaDto> betaDependencies = new HashSet<>();
        betaDependencies.add(gamma);
        beta.setDependencySet(betaDependencies);
        beta.setBody(readSchemaFileAsString(TEST_CTL_SCHEMA_BETA));
        beta = ctlService.saveCTLSchema(beta);

        beta = ctlService.findCTLSchemaById(beta.getId());
        
        alpha = new CTLSchemaDto();
        CTLSchemaMetaInfoDto alphaMetaInfo = new CTLSchemaMetaInfoDto("org.kaaproject.kaa.Alpha", tenant.getId());
        alpha.setMetaInfo(alphaMetaInfo);
        alpha.setVersion(1);
        Set<CTLSchemaDto> alphaDependencies = new HashSet<>();
        alphaDependencies.add(beta);
        alpha.setDependencySet(alphaDependencies);
        alpha.setBody(readSchemaFileAsString(TEST_CTL_SCHEMA_ALPHA));
        
        alpha = ctlService.saveCTLSchema(alpha);
        
        alpha = ctlService.findCTLSchemaById(alpha.getId());
    }

    @Test
    public void testRemoveCTLSchemaByFqnAndVerAndTenantIdAndApplicationId() {
        String schemaId = tenantSchema.getId();
        ctlService.removeCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(tenantSchema.getMetaInfo().getFqn(), tenantSchema.getVersion(),
                tenantSchema.getMetaInfo().getTenantId(), tenantSchema.getMetaInfo().getApplicationId());
        Assert.assertNull(ctlService.findCTLSchemaById(schemaId));
    }

    @Test
    public void testRemoveCTLSchemaByFqnAndVerAndWithoutTenantId() {
        String schemaId = systemSchema.getId();
        ctlService.removeCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(systemSchema.getMetaInfo().getFqn(), systemSchema.getVersion(),
                systemSchema.getMetaInfo().getTenantId(), systemSchema.getMetaInfo().getApplicationId());
        Assert.assertNull(ctlService.findCTLSchemaById(schemaId));
    }

    @Test
    public void testFindCTLSchemaByFqnAndVerAndTenantIdAndApplicationId() {
        CTLSchemaMetaInfoDto metaInfo = firstSchema.getMetaInfo();
        CTLSchemaDto found = ctlService.findCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(metaInfo.getFqn(), 
                firstSchema.getVersion(), metaInfo.getTenantId(), metaInfo.getApplicationId());
        Assert.assertEquals(firstSchema, found);
    }

    @Test
    public void testFindCTLSchemaById() {
        CTLSchemaDto found = ctlService.findCTLSchemaById(firstSchema.getId());
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
        Comparator<HasId> comparator = new Comparator<HasId>() {
            @Override
            public int compare(HasId o1, HasId o2) {
                return o1.getId().compareTo(o2.getId());
            }
        };
        Collections.sort(appSchemas, comparator);
        List<CTLSchemaMetaInfoDto> expectedSchemas = Arrays.asList(defaultSystemSchema.getMetaInfo(), systemSchema.getMetaInfo());
        Collections.sort(expectedSchemas, comparator);        
        Assert.assertEquals(expectedSchemas, appSchemas);
    }

    @Test
    public void testFindLatestCTLSchemaByFqn() {
        CTLSchemaDto latestTenantScope = ctlService.findLatestCTLSchemaByFqnAndTenantIdAndApplicationId(DEFAULT_FQN+7, tenant.getId(), null);
        Assert.assertEquals(Integer.valueOf(78), latestTenantScope.getVersion());
        CTLSchemaDto latestAppScope = ctlService.findLatestCTLSchemaByFqnAndTenantIdAndApplicationId(DEFAULT_FQN+8, tenant.getId(), appDto.getId());
        Assert.assertEquals(Integer.valueOf(81), latestAppScope.getVersion());
    }
    
    @Test
    public void testScopeUpdate() {
        CTLSchemaMetaInfoDto metaInfo = appSchema3.getMetaInfo();
        metaInfo.setApplicationId(null);
        ctlService.updateCTLSchemaMetaInfoScope(metaInfo);
        CTLSchemaDto found = ctlService.findCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(metaInfo.getFqn(), appSchema3.getVersion(), metaInfo.getTenantId(), null);
        Assert.assertEquals(appSchema3, found);
    }
    
    @Test(expected = DatabaseProcessingException.class)
    public void testScopeUpdateForbidden() {
        CTLSchemaMetaInfoDto metaInfo = appSchema.getMetaInfo();
        metaInfo.setApplicationId(null);
        ctlService.updateCTLSchemaMetaInfoScope(metaInfo);
    }
    
    @Test
    public void testFindSiblingsFqns() {
        List<CTLSchemaMetaInfoDto> siblingSchemas = 
                ctlService.findSiblingsByFqnTenantIdAndApplicationId(appSchema.getMetaInfo().getFqn(), appSchema.getMetaInfo().getTenantId(), appSchema.getMetaInfo().getApplicationId());
        
        Assert.assertNotNull(siblingSchemas);
        Assert.assertEquals(1, siblingSchemas.size());
        Assert.assertEquals(app2Schema.getMetaInfo(), siblingSchemas.get(0));
    }

    @Test
    public void testFindCTLSchemaDependentsByFqnVersionTenantId() {
        List<CTLSchemaDto> appSchemas = ctlService.findCTLSchemaDependents(firstSchema.getMetaInfo().getFqn(), firstSchema.getVersion(),
                tenant.getId(), null);
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(secondSchema.getMetaInfo().getFqn(), secondSchema.getVersion(), tenant.getId(), null);
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(thirdSchema.getMetaInfo().getFqn(), thirdSchema.getVersion(), tenant.getId(), null);
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(fourthSchema.getMetaInfo().getFqn(), fourthSchema.getVersion(), tenant.getId(), null);
        Assert.assertEquals(Arrays.asList(mainSchema), appSchemas);

        appSchemas = ctlService.findCTLSchemaDependents(mainSchema.getMetaInfo().getFqn(), mainSchema.getVersion(), tenant.getId(), null);
        Assert.assertTrue(appSchemas.isEmpty());

    }

    @Test
    public void multiThreadCTLSchemaSaveTest() throws InterruptedException, ExecutionException {
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
        List<CTLSchemaDto> schemas = new ArrayList<>();
        while (iterator.hasNext()) {
            Future<CTLSchemaDto> f = iterator.next();
            while (!f.isDone()) {
            }
            schemas.add(f.get());
            iterator.remove();
        }
        Assert.assertEquals(100, schemas.size());
        for (CTLSchemaDto schema : schemas) {
            CTLSchemaDto savedSchema = ctlService.findCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(DEFAULT_FQN, 100, schema.getMetaInfo().getTenantId(), null);
            Assert.assertNotNull(savedSchema);
            Assert.assertEquals(schema, savedSchema);
        }
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
