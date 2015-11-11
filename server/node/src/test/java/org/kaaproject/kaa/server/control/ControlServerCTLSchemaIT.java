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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScope;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 */
public class ControlServerCTLSchemaIT extends AbstractTestControlServer {

    private final String FQN = this.getClass().getName();
    private final Integer VERSION = 42;

    private final String UNKNOWN_ID = "schemaId";
    private final String UNKNOWN_FQN = "schemaFqn";

    private final String UNKNOWN_TENANT_ID = "tenantId";
    private final String UNKNOWN_APPLICATION_ID = "applicationId";

    @Test
    public void saveCTLSchemaPositiveTest() throws Exception {
        CTLSchemaDto schema = this.createCTLSchema(null, null, null);
        Assert.assertNotNull(schema.getId());
    }

    /*
     * A CTL schema update is forbidden. Throw an exception if a CTL schema
     * already exists in the database.
     */
    @Test(expected = Exception.class)
    public void saveCTLSchemaNegativeTest() throws Exception {
        this.createCTLSchema(FQN, VERSION, null);
        this.createCTLSchema(FQN, VERSION, null);
    }

    @Test
    public void deleteCTLSchemaByIdPositiveTest() throws Exception {
        final CTLSchemaDto schema = this.createCTLSchema(null, null, null);
        client.deleteCTLSchemaById(schema.getId());

        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getCTLSchemaById(schema.getId());
            }
        });
    }

    @Test
    public void deleteCTLSchemaByIdNegativeTest() throws Exception {
        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.deleteCTLSchemaById(UNKNOWN_ID);
            }
        });
    }

    @Test
    public void deleteCTLSchemaByFqnAndVersionPositiveTest() throws Exception {
        final CTLSchemaDto schema = this.createCTLSchema(FQN, VERSION, null);
        client.deleteCTLSchemaByFqnAndVersion(FQN, VERSION);

        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getCTLSchemaById(schema.getId());
            }
        });
    }

    @Test
    public void deleteCTLSchemaByFqnAndVersionNegativeTest() throws Exception {
        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.deleteCTLSchemaByFqnAndVersion(UNKNOWN_FQN, VERSION);
            }
        });
    }

    @Test
    public void getCTLSchemaByIdPositiveTest() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema(null, null, CTLSchemaScope.APPLICATION);
        CTLSchemaDto loaded = client.getCTLSchemaById(saved.getId());
        Assert.assertEquals(saved, loaded);
    }

    @Test
    public void getCTLSchemaByIdNegativeTest() throws Exception {
        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getCTLSchemaById(UNKNOWN_ID);
            }
        });
    }

    @Test
    public void getCTLSchemaByFqnAndVersionPositiveTest() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema(null, null, null);
        CTLSchemaDto loaded = client.getCTLSchemaByFqnAndVersion(saved.getFqn(), saved.getVersion());
        Assert.assertEquals(saved, loaded);
    }

    @Test
    public void getCTLSchemaByFqnAndVersionNegativeTest() throws Exception {
        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getCTLSchemaByFqnAndVersion(UNKNOWN_ID, VERSION);
            }
        });
    }

    @Test
    public void getCTLSchemasByTenantIdPositiveTest() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema(null, null, null);
        List<CTLSchemaDto> loaded = client.getCTLSchemasByTenantId(saved.getTenantId());
        Assert.assertNotNull(loaded);
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(saved, loaded.get(0));
    }

    @Test
    public void getCTLSchemasByTenantIdPositiveNegativeTest() throws Exception {
        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getCTLSchemasByTenantId(UNKNOWN_TENANT_ID);
            }
        });
    }

    @Test
    public void getCTLSchemasByApplicationIdPositiveTest() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema(null, null, null);
        List<CTLSchemaDto> loaded = client.getCTLSchemasByApplicationId(saved.getApplicationId());
        Assert.assertNotNull(loaded);
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(saved, loaded.get(0));
    }

    @Test
    public void getCTLSchemasByApplicationIdPositiveNegativeTest() throws Exception {
        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getCTLSchemasByApplicationId(UNKNOWN_APPLICATION_ID);
            }
        });
    }

    @Test
    public void getCTLSchemasByFqnPositiveTest() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema(null, null, null);
        List<CTLSchemaDto> loaded = client.getCTLSchemasByFqn(saved.getFqn());
        Assert.assertNotNull(loaded);
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(saved, loaded.get(0));
    }

    @Test
    public void getCTLSchemasByFqnNegativeTest() throws Exception {
        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getCTLSchemasByFqn(UNKNOWN_FQN);
            }
        });
    }

    @Test
    public void getSystemCTLSchemas() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema(null, null, CTLSchemaScope.SYSTEM);
        List<CTLSchemaDto> loaded = client.getSystemCTLSchemas();
        Assert.assertNotNull(loaded);
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(saved, loaded.get(0));
    }
}
