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
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toGenericDtoList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlServerApplicationEventMapIT extends AbstractTestControlServer {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(ControlServerApplicationEventMapIT.class);
    
    /**
     * Test create application event family map.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException 
     */
    @Test
    public void testCreateApplicationEventFamilyMap() throws TException, IOException {
        ApplicationEventFamilyMapDto applicationEventFamilyMap = createApplicationEventFamilyMap();
        Assert.assertFalse(strIsEmpty(applicationEventFamilyMap.getId()));
    }
    
    /**
     * Test get application event family map.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException 
     */
    @Test
    public void testGetApplicationEventFamilyMap() throws TException, IOException {
        ApplicationEventFamilyMapDto applicationEventFamilyMap = createApplicationEventFamilyMap();
        
        ApplicationEventFamilyMapDto storedApplicationEventFamilyMap = toDto(client.getApplicationEventFamilyMap(applicationEventFamilyMap.getId()));
        
        Assert.assertNotNull(storedApplicationEventFamilyMap);
        Assert.assertEquals(applicationEventFamilyMap, storedApplicationEventFamilyMap);
    }
    
    /**
     * Test get application event family maps by application id.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException 
     */
    @Test
    public void testGetApplicationEventFamilyMapsByApplicationId() throws TException, IOException {
        List<ApplicationEventFamilyMapDto> applicationEventFamilyMaps  = new ArrayList<ApplicationEventFamilyMapDto>(10);
        ApplicationDto application = createApplication();
        EventClassFamilyDto eventClassFamily = createEventClassFamily(application.getTenantId());
        for (int i=0;i<10;i++) {
            ApplicationEventFamilyMapDto applicationEventFamilyMap = createApplicationEventFamilyMap(application.getId(), eventClassFamily.getId(), (i+1));
            applicationEventFamilyMaps.add(applicationEventFamilyMap);
        }
        
        Collections.sort(applicationEventFamilyMaps, new IdComparator());
        
        List<ApplicationEventFamilyMapDto> storedApplicationEventFamilyMaps = toDtoList(client.getApplicationEventFamilyMapsByApplicationId(application.getId()));

        Collections.sort(storedApplicationEventFamilyMaps, new IdComparator());
        
        Assert.assertEquals(applicationEventFamilyMaps, storedApplicationEventFamilyMaps);
    }
    
    /**
     * Test update application event family map.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException 
     */
    @Test(expected = ControlThriftException.class)
    public void testUpdateApplicationEventFamilyMap() throws TException, IOException {
        ApplicationEventFamilyMapDto applicationEventFamilyMap = createApplicationEventFamilyMap();
        toDto(client.editApplicationEventFamilyMap(toDataStruct(applicationEventFamilyMap)));
    }
    
    /**
     * Test get vacant event class families by application id.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException 
     */
    @Test
    public void testGetVacantEventClassFamiliesByApplicationId() throws TException, IOException {
        ApplicationDto application = createApplication();
        EventClassFamilyDto eventClassFamily = createEventClassFamily(application.getTenantId());
        createApplicationEventFamilyMap(application.getId(), eventClassFamily.getId(), 1);
        List<EcfInfoDto> vacantEcfs = toGenericDtoList(client.getVacantEventClassFamiliesByApplicationId(application.getId()));
        Assert.assertNotNull(vacantEcfs);
        Assert.assertEquals(0, vacantEcfs.size());
        
        String schema = getResourceAsString(TEST_EVENT_CLASS_FAMILY_SCHEMA);
        client.addEventClassFamilySchema(eventClassFamily.getId(), schema, null);
        
        vacantEcfs = toGenericDtoList(client.getVacantEventClassFamiliesByApplicationId(application.getId()));
        Assert.assertNotNull(vacantEcfs);
        Assert.assertEquals(1, vacantEcfs.size());
        Assert.assertNotNull(vacantEcfs.get(0));
        Assert.assertEquals(eventClassFamily.getId(), vacantEcfs.get(0).getEcfId());
        Assert.assertEquals(eventClassFamily.getName(), vacantEcfs.get(0).getEcfName());
        Assert.assertEquals(2, vacantEcfs.get(0).getVersion());
    }
    
    /**
     * Test get event class families by application id.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException 
     */
    @Test
    public void testGetEventClassFamiliesByApplicationId() throws TException, IOException {
        ApplicationDto application = createApplication();
        EventClassFamilyDto eventClassFamily = createEventClassFamily(application.getTenantId());
        createApplicationEventFamilyMap(application.getId(), eventClassFamily.getId(), 1);
        List<AefMapInfoDto> applicationEcfs = toGenericDtoList(client.getEventClassFamiliesByApplicationId(application.getId()));
        Assert.assertNotNull(applicationEcfs);
        Assert.assertEquals(1, applicationEcfs.size());
        Assert.assertNotNull(applicationEcfs.get(0));
        Assert.assertEquals(eventClassFamily.getId(), applicationEcfs.get(0).getEcfId());
        Assert.assertEquals(eventClassFamily.getName(), applicationEcfs.get(0).getEcfName());
        Assert.assertEquals(1, applicationEcfs.get(0).getVersion());
    }

}
