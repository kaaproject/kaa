/*
 * Copyright 2014 CyberVision, Inc.
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

import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDataStruct;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDto;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDtoList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.TenantAdminDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlServerTenantIT extends AbstractTestControlServer {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(ControlServerTenantIT.class);
    
    /**
     * Test create tenant.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testCreateTenant() throws TException {
        TenantDto tenant = createTenant();
        Assert.assertFalse(strIsEmpty(tenant.getId()));
        client.deleteTenant(tenant.getId());
    }
    
    /**
     * Test create tenant admin.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testCreateTenantAdmin() throws TException {
        TenantAdminDto tenantAdmin = createTenantAdmin();
        Assert.assertFalse(strIsEmpty(tenantAdmin.getId()));
        client.deleteTenantAdmin(tenantAdmin.getId());
    }
    
    /**
     * Test get tenant.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetTenant() throws TException {
        TenantDto tenant = createTenant();
        
        TenantDto storedTenant = toDto(client.getTenant(tenant.getId()));
        
        Assert.assertNotNull(storedTenant);
        assertTenantsEquals(tenant, storedTenant);
        client.deleteTenant(tenant.getId());
    }
    
    /**
     * Test get tenant admin.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetTenantAdmin() throws TException {
        TenantAdminDto tenantAdmin = createTenantAdmin();
        
        TenantAdminDto storedTenantAdmin = toDto(client.getTenantAdmin(tenantAdmin.getId()));
        
        Assert.assertNotNull(storedTenantAdmin);
        assertTenantAdminsEquals(tenantAdmin, storedTenantAdmin);
        client.deleteTenantAdmin(tenantAdmin.getId());
    }
    
    /**
     * Test get tenants.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetTenants() throws TException {
        List<TenantDto> tenants  = new ArrayList<TenantDto>(10);
        for (int i=0;i<10;i++) {
            TenantDto tenant = createTenant();
            tenants.add(tenant);
        }
        
        Collections.sort(tenants, new IdComparator());
        
        List<TenantDto> storedTenants = toDtoList(client.getTenants());
        Collections.sort(storedTenants, new IdComparator());
        
        Assert.assertEquals(tenants.size(), storedTenants.size());
        for (int i=0;i<tenants.size();i++) {
            TenantDto tenant = tenants.get(i);
            TenantDto storedTenant = storedTenants.get(i);
            assertTenantsEquals(tenant, storedTenant);
        }
        for (int i=0;i<storedTenants.size();i++) {
            client.deleteTenant(storedTenants.get(i).getId());
        }
    }
    
    /**
     * Test get tenant admins.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetTenantAdmins() throws TException {
        List<TenantAdminDto> tenantAdmins  = new ArrayList<TenantAdminDto>(10);
        for (int i=0;i<10;i++) {
            TenantAdminDto tenantAdmin = createTenantAdmin();
            tenantAdmins.add(tenantAdmin);
        }
        
        Collections.sort(tenantAdmins, new IdComparator());
        
        List<TenantAdminDto> storedTenantAdmins = toDtoList(client.getTenantAdmins());
        Collections.sort(storedTenantAdmins, new IdComparator());
        
        Assert.assertEquals(tenantAdmins.size(), storedTenantAdmins.size());
        for (int i=0;i<tenantAdmins.size();i++) {
            TenantAdminDto tenantAdmin = tenantAdmins.get(i);
            TenantAdminDto storedTenantAdmin = storedTenantAdmins.get(i);
            assertTenantAdminsEquals(tenantAdmin, storedTenantAdmin);
        }
        for (int i=0;i<storedTenantAdmins.size();i++) {
            client.deleteTenantAdmin(storedTenantAdmins.get(i).getId());
        }
    }
    
    /**
     * Test update tenant.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testUpdateTenant() throws TException {
        TenantDto tenant = createTenant();
        
        tenant.setName(generateString(TENANT));
        
        TenantDto updatedTenant = toDto(client
                .editTenant(toDataStruct(tenant)));

        assertTenantsEquals(updatedTenant, tenant);
        client.deleteTenant(tenant.getId());
    }
    
    /**
     * Test update tenant admin.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testUpdateTenantAdmin() throws TException {
        TenantAdminDto tenantAdmin = createTenantAdmin();
        
        tenantAdmin.setName(generateString(TENANT));
        
        TenantAdminDto updatedTenantAdmin = toDto(client
                .editTenantAdmin(toDataStruct(tenantAdmin)));

        assertTenantAdminsEquals(updatedTenantAdmin, tenantAdmin);
        client.deleteTenantAdmin(tenantAdmin.getId());
    }
    
    /**
     * Test delete tenant.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testDeleteTenant() throws TException {
        TenantDto tenant = createTenant();
        client.deleteTenant(tenant.getId());
        TenantDto storedTenant = toDto(client.getTenant(tenant.getId()));
        Assert.assertNull(storedTenant);
   }
    
    /**
     * Test delete tenant admin.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testDeleteTenantAdmin() throws TException {
        TenantAdminDto tenantAdmin = createTenantAdmin();
        client.deleteTenantAdmin(tenantAdmin.getId());
        TenantAdminDto storedTenantAdmin = toDto(client.getTenantAdmin(tenantAdmin.getId()));
        Assert.assertNull(storedTenantAdmin);
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
    
    /**
     * Assert tenant admins equals.
     *
     * @param tenantAdmin the tenant admin
     * @param otherTenantAdmin the other tenant admin
     */
    private void assertTenantAdminsEquals(TenantAdminDto tenantAdmin, TenantAdminDto otherTenantAdmin) {
        Assert.assertEquals(tenantAdmin.getId(), otherTenantAdmin.getId());
        Assert.assertEquals(tenantAdmin.getExternalUid(), otherTenantAdmin.getExternalUid());
        Assert.assertEquals(tenantAdmin.getUsername(), otherTenantAdmin.getUsername());
        Assert.assertEquals(tenantAdmin.getUserId(), otherTenantAdmin.getUserId());
        assertTenantsEquals(tenantAdmin.getTenant(), otherTenantAdmin.getTenant());
    }
 
}
