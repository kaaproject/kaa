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

package org.kaaproject.kaa.server.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.TenantDto;

/**
 * The Class ControlServerTenantIT.
 */
public class ControlServerTenantIT extends AbstractTestControlServer {

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.control.AbstractTestControlServer#createTenantAdminNeeded()
     */
    @Override
    protected boolean createTenantAdminNeeded() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.control.AbstractTestControlServer#createTenantDeveloperNeeded()
     */
    @Override
    protected boolean createTenantDeveloperNeeded() {
        return false;
    }

    /**
     * Test create tenant.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateTenant() throws Exception {
        TenantDto tenant =  createTenant();
        Assert.assertFalse(strIsEmpty(tenant.getId()));
    }

    /**
     * Test get tenant.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetTenant() throws Exception {
        TenantDto tenant = createTenant();

        TenantDto storedTenant = client.getTenant(tenant.getId());

        Assert.assertNotNull(storedTenant);
        assertTenantsEquals(tenant, storedTenant);
    }

    /**
     * Test get tenants.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetTenants() throws Exception {
        List<TenantDto> tenants  = new ArrayList<TenantDto>(10);
        for (int i=0;i<10;i++) {
            TenantDto tenant = createTenant();
            tenants.add(tenant);
        }

        Collections.sort(tenants, new IdComparator());

        List<TenantDto> storedTenants = client.getTenants();
        Collections.sort(storedTenants, new IdComparator());

        Assert.assertEquals(tenants.size(), storedTenants.size());
        for (int i=0;i<tenants.size();i++) {
            TenantDto tenant = tenants.get(i);
            TenantDto storedTenant = storedTenants.get(i);
            assertTenantsEquals(tenant, storedTenant);
        }
    }

    /**
     * Test update tenant.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpdateTenant() throws Exception {

        TenantDto tenant = createTenant();

        tenant.setName(generateString(TENANT));

        TenantDto updatedTenant = client.editTenant(tenant);

        assertTenantsEquals(updatedTenant, tenant);
    }

    /**
     * Test delete tenant.
     *
     * @throws Exception the exception
     */
    @Ignore
    @Test
    public void testDeleteTenant() throws Exception {
        final TenantDto tenant = createTenant();
//        client.deleteTenant(tenant.getId());
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getTenant(tenant.getId());
            }
        });
   }

    /**
     * Assert tenants equals.
     *
     * @param tenant the tenant
     * @param otherTenant the other tenant
     */
    private void assertTenantsEquals(TenantDto tenant, TenantDto otherTenant) {
        Assert.assertEquals(tenant.getId(), otherTenant.getId());
        Assert.assertEquals(tenant.getName(), otherTenant.getName());
    }

}
