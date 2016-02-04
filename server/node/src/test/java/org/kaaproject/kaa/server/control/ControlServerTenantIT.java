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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;

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
        TenantUserDto tenant = createTenant();
        Assert.assertFalse(strIsEmpty(tenant.getId()));
        client.deleteTenant(tenant.getId());
    }
    
    /**
     * Test get tenant.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetTenant() throws Exception {
        TenantUserDto tenant = createTenant();
        
        TenantUserDto storedTenant = client.getTenant(tenant.getId());
        
        Assert.assertNotNull(storedTenant);
        assertTenantsEquals(tenant, storedTenant);
        client.deleteTenant(tenant.getId());
    }
    
    /**
     * Test get tenants.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetTenants() throws Exception {
        List<TenantUserDto> tenants  = new ArrayList<TenantUserDto>(10);
        for (int i=0;i<10;i++) {
            TenantUserDto tenant = createTenant();
            tenants.add(tenant);
        }
        
        Collections.sort(tenants, new IdComparator());
        
        List<TenantUserDto> storedTenants = client.getTenants();
        Collections.sort(storedTenants, new IdComparator());
        
        Assert.assertEquals(tenants.size(), storedTenants.size());
        for (int i=0;i<tenants.size();i++) {
            TenantUserDto tenant = tenants.get(i);
            TenantUserDto storedTenant = storedTenants.get(i);
            assertTenantsEquals(tenant, storedTenant);
        }
        for (int i=0;i<storedTenants.size();i++) {
            client.deleteTenant(storedTenants.get(i).getId());
        }
    }
    
    /**
     * Test update tenant.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpdateTenant() throws Exception {
        TenantUserDto tenant = createTenant();
        
        tenant.setTenantName(generateString(TENANT));
        
        TenantUserDto updatedTenant = client.editTenant(tenant);

        assertTenantsEquals(updatedTenant, tenant);
        client.deleteTenant(tenant.getId());
    }
    
    /**
     * Test delete tenant.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeleteTenant() throws Exception {
        final TenantUserDto tenant = createTenant();
        client.deleteTenant(tenant.getId());
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
    private void assertTenantsEquals(TenantUserDto tenant, TenantUserDto otherTenant) {
        Assert.assertEquals(tenant.getId(), otherTenant.getId());
        Assert.assertEquals(tenant.getTenantId(), otherTenant.getTenantId());
        Assert.assertEquals(tenant.getTenantName(), otherTenant.getTenantName());
        Assert.assertEquals(tenant.getUsername(), otherTenant.getUsername());
        Assert.assertEquals(tenant.getFirstName(), otherTenant.getFirstName());
        Assert.assertEquals(tenant.getLastName(), otherTenant.getLastName());        
        Assert.assertEquals(tenant.getMail(), otherTenant.getMail());
        Assert.assertEquals(tenant.getExternalUid(), otherTenant.getExternalUid());
        Assert.assertEquals(tenant.getAuthority(), otherTenant.getAuthority());
    }
    
}
