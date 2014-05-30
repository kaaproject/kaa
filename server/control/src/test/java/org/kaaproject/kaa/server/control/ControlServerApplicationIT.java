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
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlServerApplicationIT extends AbstractTestControlServer {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(ControlServerApplicationIT.class);
    
    /**
     * Test create application.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testCreateApplication() throws TException {
        ApplicationDto application = createApplication();
        Assert.assertFalse(strIsEmpty(application.getId()));
        Assert.assertFalse(strIsEmpty(application.getApplicationToken()));
    }
    
    /**
     * Test get application.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetApplication() throws TException {
        ApplicationDto application = createApplication();
        
        ApplicationDto storedApplication = toDto(client.getApplication(application.getId()));
        
        Assert.assertNotNull(storedApplication);
        assertApplicationsEquals(application, storedApplication);
    }
    
    /**
     * Test get applications by tenant id.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetApplicationsByTenantId() throws TException {
        List<ApplicationDto> applications  = new ArrayList<ApplicationDto>(10);
        TenantDto tenant = createTenant();
        for (int i=0;i<10;i++) {
            ApplicationDto application = createApplication(tenant.getId());
            applications.add(application);
        }
        
        Collections.sort(applications, new IdComparator());
        
        List<ApplicationDto> storedApplications = toDtoList(client.getApplicationsByTenantId(tenant.getId()));

        Collections.sort(storedApplications, new IdComparator());
        
        Assert.assertEquals(applications.size(), storedApplications.size());
        for (int i=0;i<applications.size();i++) {
            ApplicationDto application = applications.get(i);
            ApplicationDto storedApplication = storedApplications.get(i);
            assertApplicationsEquals(application, storedApplication);
        }
    }
    
    /**
     * Test update application.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testUpdateApplication() throws TException {
        ApplicationDto application = createApplication();
        
        application.setName(generateString(APPLICATION));
        
        ApplicationDto updatedApplication = toDto(client
                .editApplication(toDataStruct(application)));
        
        assertApplicationsEquals(updatedApplication, application);
    }
    
    /**
     * Test delete application.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testDeleteApplication() throws TException {
        ApplicationDto application = createApplication();
        client.deleteApplication(application.getId());
        ApplicationDto storedApplication = toDto(client.getApplication(application.getId()));
        Assert.assertNull(storedApplication);
   }

    /**
     * Assert applications equals.
     *
     * @param application the application
     * @param storedApplication the stored application
     */
    private void assertApplicationsEquals(ApplicationDto application, ApplicationDto storedApplication) {
        Assert.assertEquals(application.getId(), storedApplication.getId());
        Assert.assertEquals(application.getName(), storedApplication.getName());
        Assert.assertEquals(application.getTenantId(), storedApplication.getTenantId());
        Assert.assertEquals(application.getApplicationToken(), storedApplication.getApplicationToken());
    }

}
