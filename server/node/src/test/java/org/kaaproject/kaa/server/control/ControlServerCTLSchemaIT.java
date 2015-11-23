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

package org.kaaproject.kaa.server.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 */
public class ControlServerCTLSchemaIT extends AbstractTestControlServer {

    private static final Logger LOG = LoggerFactory.getLogger(ControlServerCTLSchemaIT.class);

    private static final String DEFAULT_NAME = "name";
    private static final String DEFAULT_NAMESPACE = "org.kaaproject.kaa.tests";
    private static final String DEFAULT_TYPE = "Type";

    private final Random random = new Random();

    private String randomFieldName() {
        return DEFAULT_NAME + random.nextInt(100000);
    }

    private String randomFieldType() {
        return DEFAULT_TYPE + random.nextInt(100000);
    }

    @Override
    public void beforeTest() throws Exception {
        super.beforeTest();
    }

    /**
     * Saves a CTL schema to the database.
     *
     * @throws Exception
     */
    @Test
    public void saveCTLSchemaTest() throws Exception {
        this.loginKaaAdmin();
        CTLSchemaInfoDto beta = this.createCTLSchema(this.randomFieldType(), DEFAULT_NAMESPACE, 1, CTLSchemaScopeDto.SYSTEM, null, null);
        Assert.assertNotNull(beta.getId());

        Set<CTLSchemaMetaInfoDto> dependencies = new HashSet<>();
        dependencies.add(new CTLSchemaMetaInfoDto(beta.getFqn(), beta.getVersion()));

        Map<String, String> fields = new HashMap<>();
        fields.put(this.randomFieldName(), beta.getFqn());

        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaInfoDto alpha = this.createCTLSchema(this.randomFieldType(), DEFAULT_NAMESPACE, 1, CTLSchemaScopeDto.TENANT, dependencies, fields);
        Assert.assertNotNull(alpha.getId());
    }

    /**
     * Saves a CTL schema to the database.
     *
     * @throws Exception
     */
    @Test
    public void saveCTLSchemaBodyTest() throws Exception {
        this.loginKaaAdmin();
        CTLSchemaInfoDto beta = client.saveCTLSchema(getResourceAsString(TEST_CTL_SCHEMA_BETA));
        Assert.assertNotNull(beta.getId());

        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaInfoDto alpha = client.saveCTLSchema(getResourceAsString(TEST_CTL_SCHEMA_ALPHA));
        Assert.assertNotNull(alpha.getId());
    }

    /**
     * Tries to save a CTL schema with a dependency that is missing from the
     * database. (This action is prohibited).
     *
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void saveCTLSchemaWithMissingDependenciesTest() throws Exception {
        // Declare a dependency
        String dependencyFqn = DEFAULT_NAMESPACE + "." + this.randomFieldType();
        Integer dependencyVersion = 1;
        Set<CTLSchemaMetaInfoDto> dependencies = new HashSet<>();
        dependencies.add(new CTLSchemaMetaInfoDto(dependencyFqn, dependencyVersion));

        // Map a CTL schema field name to its type
        Map<String, String> fields = new HashMap<>();
        fields.put(this.randomFieldName(), dependencyFqn);

        this.loginTenantDeveloper(tenantDeveloperUser);
        this.createCTLSchema(this.randomFieldType(), DEFAULT_NAMESPACE, 1, CTLSchemaScopeDto.TENANT, dependencies, fields);
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
        final CTLSchemaInfoDto saved = this.createCTLSchema(this.randomFieldType(), DEFAULT_NAMESPACE, 1, CTLSchemaScopeDto.TENANT, null, null);
        client.deleteCTLSchemaByFqnAndVersion(saved.getFqn(), saved.getVersion());
        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getCTLSchemaByFqnAndVersion(saved.getFqn(), saved.getVersion()).toString();
            }
        });
    }

    /**
     * Tries to remove a CTL schema that is referenced by something else. (This
     * action is prohibited).
     *
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void deleteCTLSchemaWithDependents() throws Exception {
        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaInfoDto dependency = this.createCTLSchema(this.randomFieldType(), DEFAULT_NAMESPACE, 1, CTLSchemaScopeDto.TENANT, null, null);

        Set<CTLSchemaMetaInfoDto> dependencies = new HashSet<>();
        dependencies.add(new CTLSchemaMetaInfoDto(dependency.getFqn(), dependency.getVersion()));

        Map<String, String> fields = new HashMap<>();
        fields.put(this.randomFieldName(), dependency.getFqn());
        this.createCTLSchema(this.randomFieldName(), DEFAULT_NAMESPACE, 1, CTLSchemaScopeDto.TENANT, dependencies, fields);

        client.deleteCTLSchemaByFqnAndVersion(dependency.getFqn(), dependency.getVersion());
    }

    /**
     * Tries to remove a system CTL schema by its fully qualified name and
     * version number as a regular user. (This action is prohibited).
     *
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void deleteSystemCTLSchemaByFqnAndVersionTest() throws Exception {
        this.loginKaaAdmin();
        CTLSchemaInfoDto saved = this.createCTLSchema(this.randomFieldType(), DEFAULT_NAMESPACE, 1, CTLSchemaScopeDto.SYSTEM, null, null);
        this.loginTenantDeveloper(tenantDeveloperUser);
        client.deleteCTLSchemaByFqnAndVersion(saved.getFqn(), saved.getVersion());
    }

    /**
     * Retrieves a CTL schema by its fully qualified name and version number.
     *
     * @throws Exception
     */
    @Test
    public void getCTLSchemaByFqnAndVersionTest() throws Exception {
        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaInfoDto saved = this.createCTLSchema(this.randomFieldType(), DEFAULT_NAMESPACE, 1, CTLSchemaScopeDto.TENANT, null, null);
        CTLSchemaInfoDto loaded = client.getCTLSchemaByFqnAndVersion(saved.getFqn(), saved.getVersion());
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
        CTLSchemaInfoDto saved = this.createCTLSchema(this.randomFieldType(), DEFAULT_NAMESPACE, 1, CTLSchemaScopeDto.SYSTEM, null, null);
        this.loginTenantDeveloper(tenantDeveloperUser);
        CTLSchemaInfoDto loaded = client.getCTLSchemaByFqnAndVersion(saved.getFqn(), saved.getVersion());
        Assert.assertNotNull(loaded);
        Assert.assertEquals(saved, loaded);
    }
}
