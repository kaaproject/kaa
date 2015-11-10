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

    @Test
    public void saveCTLSchemaTest() throws Exception {
        CTLSchemaDto schema = this.createCTLSchema();
        Assert.assertNotNull(schema.getId());
    }

    @Test
    public void deleteCTLSchemaByIdTest() throws Exception {
        final CTLSchemaDto schema = this.createCTLSchema();
        client.deleteCTLSchemaById(schema.getId());
        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getCTLSchemaById(schema.getId());
            }
        });
    }

    @Test
    public void deleteCTLSchemaByFqnAndVesionTest() throws Exception {
        final CTLSchemaDto schema = this.createCTLSchema();
        client.deleteCTLSchemaByFqnAndVersion(schema.getFqn(), schema.getVersion());
        this.checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getCTLSchemaByFqnAndVersion(schema.getFqn(), schema.getVersion());
            }
        });
    }

    @Test
    public void getCTLSchemaByIdTest() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema();
        CTLSchemaDto loaded = client.getCTLSchemaById(saved.getId());
        Assert.assertEquals(saved, loaded);
    }

    @Test
    public void getCTLSchemaByFqnAndVersionTest() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema();
        CTLSchemaDto loaded = client.getCTLSchemaByFqnAndVersion(saved.getFqn(), saved.getVersion());
        Assert.assertEquals(saved, loaded);
    }

    @Test
    public void getCTLSchemasByTenantIdTest() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema();
        List<CTLSchemaDto> loaded = client.getCTLSchemasByTenantId(saved.getTenantId());
        Assert.assertNotNull(loaded);
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(saved, loaded.get(0));
    }

    @Test
    public void getCTLSchemasByApplicationIdTest() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema();
        List<CTLSchemaDto> loaded = client.getCTLSchemasByApplicationId(saved.getApplicationId());
        Assert.assertNotNull(loaded);
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(saved, loaded.get(0));
    }

    @Test
    public void getCTLSchemasByFqnTest() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema();
        List<CTLSchemaDto> loaded = client.getCTLSchemasByFqn(saved.getFqn());
        Assert.assertNotNull(loaded);
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(saved, loaded.get(0));
    }

    @Test
    public void getSystemCTLSchemas() throws Exception {
        CTLSchemaDto saved = this.createCTLSchema(CTLSchemaScope.SYSTEM);
        List<CTLSchemaDto> loaded = client.getSystemCTLSchemas();
        Assert.assertNotNull(loaded);
        Assert.assertEquals(1, loaded.size());
        Assert.assertEquals(saved, loaded.get(0));

    }
}
