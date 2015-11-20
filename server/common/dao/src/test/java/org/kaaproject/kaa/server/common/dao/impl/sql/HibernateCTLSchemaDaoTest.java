package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateCTLSchemaDaoTest extends HibernateAbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateCTLSchemaDaoTest.class);

    @Autowired
    private CTLService ctlService;

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

    @Test(expected = Exception.class)
    public void saveCTLSchemaWithSameFqnAndVersion() {
        ctlSchemaDao.save(generateCTLSchema(DEFAULT_FQN, new Tenant(tenant), 11, null));
        ctlSchemaDao.save(generateCTLSchema(DEFAULT_FQN, null, 11, null));
    }

    @Test
    @Rollback(false)
    public void saveCTLSchemaWithDependency() throws InterruptedException {
        List<CTLSchemaDto> dep = convertDtoList(ctlSchemaDao.findDependentsSchemas(mainSchema.getId()));
        Assert.assertTrue(dep.isEmpty());
        List<CTLSchemaDto> expected = Arrays.asList(mainSchema);
        dep = convertDtoList(ctlSchemaDao.findDependentsSchemas(firstSchema.getId()));
        Assert.assertEquals(expected.size(), dep.size());
        dep = convertDtoList(ctlSchemaDao.findDependentsSchemas(secondSchema.getId()));
        Assert.assertEquals(expected.size(), dep.size());
        dep = convertDtoList(ctlSchemaDao.findDependentsSchemas(thirdSchema.getId()));
        Assert.assertEquals(expected.size(), dep.size());
        dep = convertDtoList(ctlSchemaDao.findDependentsSchemas(fourthSchema.getId()));
        Assert.assertEquals(expected.size(), dep.size());
    }


    @Test
    @Rollback(false)
    public void testFindByFqnAndVerAndTenantId() {
        CTLSchema found = ctlSchemaDao.findByFqnAndVerAndTenantId(firstSchema.getMetaInfo().getFqn(), firstSchema.getMetaInfo().getVersion(), firstSchema.getTenantId());
        Assert.assertEquals(firstSchema, found.toDto());
    }

    @Test
    @Rollback(false)
    public void testFindSystemByFqnAndVerAndTenantId() {
        CTLSchema found = ctlSchemaDao.findByFqnAndVerAndTenantId(systemSchema.getMetaInfo().getFqn(), systemSchema.getMetaInfo().getVersion(), systemSchema.getTenantId());
        Assert.assertEquals(systemSchema, found.toDto());
    }

    @Test
    @Rollback(false)
    public void testFindSystemSchemas() {
        List<CTLSchema> found = ctlSchemaDao.findSystemSchemas();
        Assert.assertEquals(getIdsDto(Arrays.asList(systemSchema)), getIds(found));
    }

    @Test
    @Rollback(false)
    public void testFindByTenantId() {
        List<CTLSchema> found = ctlSchemaDao.findByTenantId(tenant.getId());
        Assert.assertEquals(getIdsDto(Arrays.asList(firstSchema, secondSchema, thirdSchema, fourthSchema, mainSchema)), getIds(found));
    }

    @Test
    @Rollback(false)
    public void testFindByApplicationId() {

    }

    @Test
    @Rollback(false)
    public void testFindLatestByFqn() {
        CTLSchema latest = ctlSchemaDao.findLatestByFqn(DEFAULT_FQN);
        Assert.assertEquals(systemSchema, latest.toDto());
    }

    @Test
    @Rollback(false)
    public void testRemoveByFqnAndVerAndTenantId() {
        ctlSchemaDao.removeByFqnAndVerAndTenantId(systemSchema.getMetaInfo().getFqn(), systemSchema.getMetaInfo().getVersion(), systemSchema.getTenantId());
        Assert.assertNull(ctlSchemaDao.findById(systemSchema.getId()));
    }

    @Test
    @Rollback(false)
    public void testFindAvailableSchemas() {
        List<CTLSchema> found = ctlSchemaDao.findAvailableSchemas(tenant.getId());
        Assert.assertEquals(getIdsDto(Arrays.asList(firstSchema, secondSchema, thirdSchema, fourthSchema, mainSchema, systemSchema)), getIds(found));
    }
}
