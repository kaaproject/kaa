/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.avro.ui.shared.FqnVersion;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.springframework.http.HttpStatus;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 */
public class ControlServerCTLSchemaIT extends AbstractTestControlServer {

    /**
     * Saves a CTL schema to the database.
     *
     * @throws Exception
     */
    @Test
    public void saveCTLSchemaTest() throws Exception {
        this.loginKaaAdmin();
        CTLSchemaDto beta = client.saveCTLSchema(getResourceAsString(TEST_CTL_SCHEMA_BETA), null, null);
        Assert.assertNotNull(beta.getId());

        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaDto alpha = client.saveCTLSchema(getResourceAsString(TEST_CTL_SCHEMA_ALPHA), tenantDeveloperDto.getTenantId(), null);
        Assert.assertNotNull(alpha.getId());
    }

    /**
     * Tries to save a CTL schema with a dependency that is missing from the
     * database. (This action is prohibited).
     *
     * @throws Exception
     */
    @Test
    public void saveCTLSchemaWithMissingDependenciesTest() throws Exception {
        // Declare a dependency
        String dependencyFqn = CTL_DEFAULT_NAMESPACE + "." + this.ctlRandomFieldType();
        Integer dependencyVersion = 1;
        final Set<FqnVersion> dependencies = new HashSet<>();
        dependencies.add(new FqnVersion(dependencyFqn, dependencyVersion));

        // Map a CTL schema field name to its type
        final Map<String, String> fields = new HashMap<>();
        fields.put(this.ctlRandomFieldName(), dependencyFqn);

        this.loginTenantDeveloper(tenantDeveloperUser);
        this.checkBadRequest(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                createCTLSchema(ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), null, dependencies, fields);
            }
        });
    }

    /**
     * Removes a CTL schema from the database by its fully qualified name and
     * version number.
     *
     * @throws Exception
     */
    @Test
    public void deleteCTLSchemaByFqnAndVersionTest() throws Exception {
        this.loginTenantDeveloper(tenantDeveloperUser);
        final CTLSchemaDto saved = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), null, null, null);
        client.deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(saved.getMetaInfo().getFqn(), saved.getVersion(), tenantDeveloperDto.getTenantId(), null);
        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getCTLSchemaByFqnVersionTenantIdAndApplicationId(saved.getMetaInfo().getFqn(), saved.getVersion(), tenantDeveloperDto.getTenantId(), null);
            }
        });
    }

    /**
     * Tries to remove a CTL schema that is referenced by something else. (This
     * action is prohibited).
     */
    @Test
    public void deleteCTLSchemaWithDependents() throws Exception {
        this.loginTenantDeveloper(tenantDeveloperUser);
        final CTLSchemaDto dependency = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), null,  null, null);

        Set<FqnVersion> dependencies = new HashSet<>();
        dependencies.add(new FqnVersion(dependency.getMetaInfo().getFqn(), dependency.getVersion()));

        Map<String, String> fields = new HashMap<>();
        fields.put(this.ctlRandomFieldName(), dependency.getMetaInfo().getFqn());
        this.createCTLSchema(this.ctlRandomFieldName(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), null, dependencies, fields);

        this.checkBadRequest(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(dependency.getMetaInfo().getFqn(), dependency.getVersion(), tenantDeveloperDto.getTenantId(), null);
            }
        });
        
    }

    /**
     * Tries to remove a system CTL schema by its fully qualified name and
     * version number as a regular user. (This action is prohibited).
     */
    @Test
    public void deleteSystemCTLSchemaByFqnAndVersionTest() throws Exception {
        this.loginKaaAdmin();
        final CTLSchemaDto saved = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, null, null, null, null);
        this.loginTenantDeveloper(tenantDeveloperUser);
        this.checkForbidden(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(saved.getMetaInfo().getFqn(), saved.getVersion(), null, null);
            }
        });
    }

    /**
     * Retrieves a CTL schema by its fully qualified name and version number.
     *
     * @throws Exception
     */
    @Test
    public void getCTLSchemaByFqnAndVersionTest() throws Exception {
        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaDto saved = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), null,  null, null);
        CTLSchemaDto loaded = client.getCTLSchemaByFqnVersionTenantIdAndApplicationId(saved.getMetaInfo().getFqn(), saved.getVersion(), tenantDeveloperDto.getTenantId(), null);
        Assert.assertNotNull(loaded);
        Assert.assertEquals(saved, loaded);
    }

    /**
     * Retrieves a system CTL schema by its fully qualified name and version
     * number as a regular user.
     *
     * @throws Exception
     */
    @Test
    public void getSystemCTLSchemaByFqnAndVersionTest() throws Exception {
        this.loginKaaAdmin();
        CTLSchemaDto saved = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, null, null, null, null);
        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaDto loaded = client.getCTLSchemaByFqnVersionTenantIdAndApplicationId(saved.getMetaInfo().getFqn(), saved.getVersion(), null, null);
        Assert.assertNotNull(loaded);
        Assert.assertEquals(saved, loaded);
    }
    
    /**
     * Check existence of CTL schema with same fqn and another scope
     *
     * @throws Exception
     */
    @Test
    public void checkCTLFqnExistsTest() throws Exception {
        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaDto saved = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), null,  null, null);
        String fqn = saved.getMetaInfo().getFqn();
        boolean result = client.checkFqnExists(fqn, tenantDeveloperDto.getTenantId(), null);
        Assert.assertFalse(result);
        result = client.checkFqnExists(fqn, tenantDeveloperDto.getTenantId(), "123");
        Assert.assertFalse(result);
        result = client.checkFqnExists(fqn, null, null);
        Assert.assertFalse(result);
        
        ApplicationDto application1 = createApplication(tenantAdminDto);
        ApplicationDto application2 = createApplication(tenantAdminDto);
        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaDto schema1 = this.createCTLSchema("TestAppFqn1", CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), application1.getId(),  null, null);
        this.createCTLSchema("TestAppFqn1", CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), application2.getId(),  null, null);
        
        fqn = schema1.getMetaInfo().getFqn();
        
        result = client.checkFqnExists(fqn, tenantDeveloperDto.getTenantId(), application1.getId());
        Assert.assertTrue(result);
        
        result = client.checkFqnExists(fqn, tenantDeveloperDto.getTenantId(), application2.getId());
        Assert.assertTrue(result);
        
        result = client.checkFqnExists(fqn, tenantDeveloperDto.getTenantId(), null);
        Assert.assertFalse(result);
    }
    
    @Test
    public void updateCTLSchemaScopeTest() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaDto saved = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), application.getId(),  null, null);
        CTLSchemaMetaInfoDto metaInfo = saved.getMetaInfo();
        metaInfo.setApplicationId(null);
        CTLSchemaMetaInfoDto updatedMetaInfo = client.updateCTLSchemaMetaInfoScope(metaInfo);
        Assert.assertNull(updatedMetaInfo.getApplicationId());
        Assert.assertNotNull(updatedMetaInfo.getTenantId());
        Assert.assertEquals(tenantDeveloperDto.getTenantId(), updatedMetaInfo.getTenantId());
        Assert.assertEquals(CTLSchemaScopeDto.TENANT, updatedMetaInfo.getScope());
    }
    
    @Test
    public void updateCTLSchemaScopeForbiddenTest() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaDto saved = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), application.getId(),  null, null);
        final CTLSchemaMetaInfoDto metaInfo = saved.getMetaInfo();
        metaInfo.setApplicationId(null);
        metaInfo.setTenantId(null);
        this.checkForbidden(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.updateCTLSchemaMetaInfoScope(metaInfo);
            }
        });
        saved = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), null,  null, null);
        final CTLSchemaMetaInfoDto metaInfo2 = saved.getMetaInfo();
        Assert.assertNull(metaInfo2.getApplicationId());
        metaInfo2.setApplicationId(application.getId());
        this.checkRestErrorStatusCode(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.updateCTLSchemaMetaInfoScope(metaInfo2);
            }
        }, HttpStatus.INTERNAL_SERVER_ERROR);
        
        this.loginTenantAdmin(tenantAdminUser);
        saved = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantAdminDto.getTenantId(), null,  null, null);
        final CTLSchemaMetaInfoDto metaInfo3 = saved.getMetaInfo();
        Assert.assertNull(metaInfo3.getApplicationId());
        metaInfo3.setApplicationId(application.getId());
        this.checkForbidden(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.updateCTLSchemaMetaInfoScope(metaInfo3);
            }
        });
        
        //Assert.assertNull(updatedMetaInfo.getApplicationId());
        //Assert.assertNotNull(updatedMetaInfo.getTenantId());
        //Assert.assertEquals(tenantDeveloperDto.getTenantId(), updatedMetaInfo.getTenantId());
        //Assert.assertEquals(CTLSchemaScopeDto.TENANT, updatedMetaInfo.getScope());
    }
    
}
