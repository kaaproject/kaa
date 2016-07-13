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
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.springframework.web.client.ResourceAccessException;

/**
 * The Class ControlServerApplicationEventMapIT.
 */
public class ControlServerApplicationEventMapIT extends AbstractTestControlServer {

    /**
     * Test create application event family map.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateApplicationEventFamilyMap() throws Exception {
        ApplicationEventFamilyMapDto applicationEventFamilyMap = createApplicationEventFamilyMap();
        Assert.assertFalse(strIsEmpty(applicationEventFamilyMap.getId()));
    }

    /**
     * Test get application event family map.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetApplicationEventFamilyMap() throws Exception {
        ApplicationEventFamilyMapDto applicationEventFamilyMap = createApplicationEventFamilyMap();
        
        ApplicationEventFamilyMapDto storedApplicationEventFamilyMap = client.getApplicationEventFamilyMap(applicationEventFamilyMap.getId());
        
        Assert.assertNotNull(storedApplicationEventFamilyMap);
        Assert.assertEquals(applicationEventFamilyMap, storedApplicationEventFamilyMap);
    }

    /**
     * Test get application event family maps by application token.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetApplicationEventFamilyMapsByApplicationToken() throws Exception {
        List<ApplicationEventFamilyMapDto> applicationEventFamilyMaps  = new ArrayList<>(10);
        ApplicationDto application = createApplication(tenantAdminDto);
        EventClassFamilyDto eventClassFamily = createEventClassFamily(application.getTenantId());
        for (int i=0;i<10;i++) {
            ApplicationEventFamilyMapDto applicationEventFamilyMap = createApplicationEventFamilyMap(
                    application.getApplicationToken(), eventClassFamily.getId(), (i+1));
            applicationEventFamilyMaps.add(applicationEventFamilyMap);
        }

        Collections.sort(applicationEventFamilyMaps, new IdComparator());

        loginTenantDeveloper(tenantDeveloperUser);

        List<ApplicationEventFamilyMapDto> storedApplicationEventFamilyMaps = client.getApplicationEventFamilyMapsByApplicationToken(
                application.getApplicationToken());

        Collections.sort(storedApplicationEventFamilyMaps, new IdComparator());

        Assert.assertEquals(applicationEventFamilyMaps, storedApplicationEventFamilyMaps);
    }

    /**
     * Test update application event family map.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpdateApplicationEventFamilyMap() throws Exception {
        final ApplicationEventFamilyMapDto applicationEventFamilyMap = createApplicationEventFamilyMap();
        checkBadRequest(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.editApplicationEventFamilyMap(applicationEventFamilyMap);
            }
        });
    }

    /**
     * Test get vacant event class families by application token.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetVacantEventClassFamiliesByApplicationToken() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        EventClassFamilyDto eventClassFamily = createEventClassFamily(application.getTenantId());
        createApplicationEventFamilyMap(application.getApplicationToken(), eventClassFamily.getId(), 1);

        loginTenantDeveloper(tenantDeveloperUser);
        List<EcfInfoDto> vacantEcfs = client.getVacantEventClassFamiliesByApplicationToken(application.getApplicationToken());
        Assert.assertNotNull(vacantEcfs);
        Assert.assertEquals(0, vacantEcfs.size());

        loginTenantAdmin(tenantAdminUser);
        client.addEventClassFamilySchema(eventClassFamily.getId(), TEST_EVENT_CLASS_FAMILY_SCHEMA);

        loginTenantDeveloper(tenantDeveloperUser);
        vacantEcfs = client.getVacantEventClassFamiliesByApplicationToken(application.getApplicationToken());
        Assert.assertNotNull(vacantEcfs);
        Assert.assertEquals(1, vacantEcfs.size());
        Assert.assertNotNull(vacantEcfs.get(0));
        Assert.assertEquals(eventClassFamily.getId(), vacantEcfs.get(0).getEcfId());
        Assert.assertEquals(eventClassFamily.getName(), vacantEcfs.get(0).getEcfName());
        Assert.assertEquals(2, vacantEcfs.get(0).getVersion());
    }

    /**
     * Test get event class families by application token.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEventClassFamiliesByApplicationToken() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        EventClassFamilyDto eventClassFamily = createEventClassFamily(application.getTenantId());
        createApplicationEventFamilyMap(application.getApplicationToken(), eventClassFamily.getId(), 1);

        loginTenantDeveloper(tenantDeveloperUser);

        List<AefMapInfoDto> applicationEcfs = client.getEventClassFamiliesByApplicationToken(application.getApplicationToken());
        Assert.assertNotNull(applicationEcfs);
        Assert.assertEquals(1, applicationEcfs.size());
        Assert.assertNotNull(applicationEcfs.get(0));
        Assert.assertEquals(eventClassFamily.getId(), applicationEcfs.get(0).getEcfId());
        Assert.assertEquals(eventClassFamily.getName(), applicationEcfs.get(0).getEcfName());
        Assert.assertEquals(1, applicationEcfs.get(0).getVersion());
    }
}
