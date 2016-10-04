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

package org.kaaproject.kaa.server.common.dao.impl.sql;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.dao.CtlService;
import org.kaaproject.kaa.server.common.dao.model.sql.CtlSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.CtlSchemaMetaInfo;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateCtlSchemaDaoTest extends HibernateAbstractTest {

  private static final Logger LOG = LoggerFactory.getLogger(HibernateCtlSchemaDaoTest.class);
  private static final String SYSTEM_FQN = "org.kaaproject.kaa.ctl.SystemSchema";
  @Autowired
  private CtlService ctlService;
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
    firstSchema = ctlService.saveCtlSchema(generateCTLSchemaDto(DEFAULT_FQN + 1, tenant.getId(), null, 1));
    dependency.add(firstSchema);
    secondSchema = ctlService.saveCtlSchema(generateCTLSchemaDto(DEFAULT_FQN + 2, tenant.getId(), null, 2));
    dependency.add(secondSchema);
    thirdSchema = ctlService.saveCtlSchema(generateCTLSchemaDto(DEFAULT_FQN + 3, tenant.getId(), null, 3));
    dependency.add(thirdSchema);
    fourthSchema = ctlService.saveCtlSchema(generateCTLSchemaDto(DEFAULT_FQN + 4, tenant.getId(), null, 4));
    dependency.add(fourthSchema);
    mainSchema = generateCTLSchemaDto(DEFAULT_FQN + 5, tenant.getId(), null, 7);
    mainSchema.setDependencySet(dependency);
    mainSchema = ctlService.saveCtlSchema(mainSchema);
    systemSchema = ctlService.saveCtlSchema(generateCTLSchemaDto(SYSTEM_FQN, null, null, 50));
  }

  @Test(expected = Exception.class)
  public void saveCTLSchemaWithSameFqnAndVersion() {
    ctlSchemaDao.save(generateCTLSchema(DEFAULT_FQN, new Tenant(tenant), 11, null));
    ctlSchemaDao.save(generateCTLSchema(DEFAULT_FQN, null, 11, null));
  }

  private CtlSchema generateCTLSchema(String fqn, Tenant tenant, int version, String body) {
    CtlSchema ctlSchema = new CtlSchema();
    if (tenant == null) {
      tenant = generateTenant();
    }
    ctlSchema.setMetaInfo(new CtlSchemaMetaInfo(fqn, tenant, null));
    ctlSchema.setVersion(version);
    if (isBlank(body)) {
      body = UUID.randomUUID().toString();
    }
    ctlSchema.setBody(body);
    return ctlSchema;
  }

  @Test
  public void saveCTLSchemaWithDependency() throws InterruptedException {
    List<CTLSchemaDto> dep = convertDtoList(ctlSchemaDao.findDependentSchemas(mainSchema.getId()));
    Assert.assertTrue(dep.isEmpty());
    List<CTLSchemaDto> expected = Arrays.asList(mainSchema);
    dep = convertDtoList(ctlSchemaDao.findDependentSchemas(firstSchema.getId()));
    Assert.assertEquals(expected.size(), dep.size());
    dep = convertDtoList(ctlSchemaDao.findDependentSchemas(secondSchema.getId()));
    Assert.assertEquals(expected.size(), dep.size());
    dep = convertDtoList(ctlSchemaDao.findDependentSchemas(thirdSchema.getId()));
    Assert.assertEquals(expected.size(), dep.size());
    dep = convertDtoList(ctlSchemaDao.findDependentSchemas(fourthSchema.getId()));
    Assert.assertEquals(expected.size(), dep.size());
  }

  @Test
  public void testFindByFqnAndVerAndTenantIdAndApplicationId() {
    CtlSchema found = ctlSchemaDao.findByFqnAndVerAndTenantIdAndApplicationId(firstSchema.getMetaInfo().getFqn(),
        firstSchema.getVersion(),
        firstSchema.getMetaInfo().getTenantId(),
        firstSchema.getMetaInfo().getApplicationId());
    Assert.assertEquals(firstSchema, found.toDto());
  }

  @Test
  public void testFindSystemByFqnAndVerAndTenantIdAndApplicationId() {
    CtlSchema found = ctlSchemaDao.findByFqnAndVerAndTenantIdAndApplicationId(systemSchema.getMetaInfo().getFqn(),
        systemSchema.getVersion(), null, null);
    Assert.assertEquals(systemSchema, found.toDto());
  }

  @Test
  public void testFindSystemSchemas() {
    List<CtlSchema> found = ctlSchemaDao.findSystemSchemas();
    Assert.assertEquals(getIdsDto(Arrays.asList(systemSchema)), getIds(found));
  }

  @Test
  public void testFindLatestByFqn() {
    CtlSchema latest = ctlSchemaDao.findLatestByFqnAndTenantIdAndApplicationId(SYSTEM_FQN, null, null);
    Assert.assertEquals(systemSchema, latest.toDto());
  }

  @Test
  public void testFindAvailableSchemasForTenant() {
    List<CtlSchema> found = ctlSchemaDao.findAvailableSchemasForTenant(tenant.getId());
    Assert.assertEquals(getIdsDto(Arrays.asList(firstSchema, secondSchema, thirdSchema, fourthSchema, mainSchema, systemSchema)), getIds(found));
  }
}
