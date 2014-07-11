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
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toGenericDataStruct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlServerEventClassFamilyIT extends AbstractTestControlServer {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(ControlServerEventClassFamilyIT.class);
    
    /**
     * Test create event class family.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testCreateEventClassFamily() throws TException {
        EventClassFamilyDto eventClassFamily = createEventClassFamily();
        Assert.assertFalse(strIsEmpty(eventClassFamily.getId()));
    }
    
    /**
     * Test get event class family.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetEventClassFamily() throws TException {
        EventClassFamilyDto eventClassFamily = createEventClassFamily();
        
        EventClassFamilyDto storedEventClassFamily = toDto(client.getEventClassFamily(eventClassFamily.getId()));
        
        Assert.assertNotNull(storedEventClassFamily);
        assertEventClassFamiliesEquals(eventClassFamily, storedEventClassFamily);
    }
    
    /**
     * Test get event class families by tenant id.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetEventClassFamiliesByTenantId() throws TException {
        List<EventClassFamilyDto> eventClassFamilies  = new ArrayList<EventClassFamilyDto>(10);
        TenantDto tenant = createTenant();
        for (int i=0;i<10;i++) {
            EventClassFamilyDto eventClassFamily = createEventClassFamily(tenant.getId(), ""+i);
            eventClassFamilies.add(eventClassFamily);
        }
        
        Collections.sort(eventClassFamilies, new IdComparator());
        
        List<EventClassFamilyDto> storedEventClassFamilies = toDtoList(client.getEventClassFamiliesByTenantId(tenant.getId()));

        Collections.sort(storedEventClassFamilies, new IdComparator());
        
        Assert.assertEquals(eventClassFamilies.size(), storedEventClassFamilies.size());
        for (int i=0;i<eventClassFamilies.size();i++) {
            EventClassFamilyDto eventClassFamily = eventClassFamilies.get(i);
            EventClassFamilyDto storedEventClassFamily = storedEventClassFamilies.get(i);
            assertEventClassFamiliesEquals(eventClassFamily, storedEventClassFamily);
        }
    }
    
    /**
     * Test update event class family.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testUpdateEventClassFamily() throws TException {
        EventClassFamilyDto eventClassFamily = createEventClassFamily();
        
        eventClassFamily.setName(generateString(EVENT_CLASS_FAMILY));
        
        EventClassFamilyDto updatedEventClassFamily = toDto(client
                .editEventClassFamily(toDataStruct(eventClassFamily)));
        
        assertEventClassFamiliesEquals(updatedEventClassFamily, eventClassFamily);
    }
    
    /**
     * Test get Event Classes by family id and version
     * 
     * @throws TException
     *             the t exception
     * @throws IOException 
     */
    @Test
    public void testGetEventClassesByFamilyIdVersionAndType() throws TException, IOException {
        EventClassFamilyDto eventClassFamily = createEventClassFamily();
        String schema = getResourceAsString(TEST_EVENT_CLASS_FAMILY_SCHEMA);
        client.addEventClassFamilySchema(eventClassFamily.getId(), schema, null);
        List<EventClassDto> eventClasses = toDtoList(client.getEventClassesByFamilyIdVersionAndType(eventClassFamily.getId(), 1, toGenericDataStruct(EventClassType.EVENT)));
        Assert.assertNotNull(eventClasses);
        Assert.assertEquals(4, eventClasses.size());
        for (EventClassDto eventClass : eventClasses) {
            Assert.assertEquals(eventClassFamily.getId(), eventClass.getEcfId());
            Assert.assertEquals(1, eventClass.getVersion());
        }
    }
    
    /**
     * Test duplicate event class family name.
     * 
     * @throws TException
     *             the t exception
     */
    @Test(expected = ControlThriftException.class)
    public void testDuplicateEventClassFamilyName() throws TException {
        TenantDto tenant = createTenant();
        EventClassFamilyDto eventClassFamily = createEventClassFamily(tenant.getId());
        EventClassFamilyDto secondEventClassFamily = createEventClassFamily(tenant.getId());
        secondEventClassFamily.setName(eventClassFamily.getName());
        toDto(client
                .editEventClassFamily(toDataStruct(secondEventClassFamily)));
    }
    
    /**
     * Test add event class family schema.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException 
     */
    @Test
    public void testAddEventClassFamilySchema() throws TException, IOException {
        EventClassFamilyDto eventClassFamily = createEventClassFamily();
        String schema = getResourceAsString(TEST_EVENT_CLASS_FAMILY_SCHEMA);

        client.addEventClassFamilySchema(eventClassFamily.getId(), schema, null);
        EventClassFamilyDto storedEventClassFamily = toDto(client.getEventClassFamily(eventClassFamily.getId()));
        List<EventSchemaVersionDto> schemas = storedEventClassFamily.getSchemas();
        Assert.assertNotNull(schemas);
        Assert.assertEquals(1, schemas.size());
        EventSchemaVersionDto eventSchema = schemas.get(0);
        Assert.assertNotNull(eventSchema);
        Assert.assertEquals(1, eventSchema.getVersion());
        Assert.assertEquals(schema, eventSchema.getSchema());
        
        client.addEventClassFamilySchema(eventClassFamily.getId(), schema, null);
        storedEventClassFamily = toDto(client.getEventClassFamily(eventClassFamily.getId()));
        schemas = storedEventClassFamily.getSchemas();
        Assert.assertNotNull(schemas);
        Assert.assertEquals(2, schemas.size());
        eventSchema = schemas.get(1);
        Assert.assertNotNull(eventSchema);
        Assert.assertEquals(2, eventSchema.getVersion());
        Assert.assertEquals(schema, eventSchema.getSchema());
    }
    
    /**
     * Test duplicate event class family fqns.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException 
     */
    @Test(expected = ControlThriftException.class)
    public void testDuplicateEventClassFamilyFqns() throws TException, IOException {
        TenantDto tenant = createTenant();
        EventClassFamilyDto eventClassFamily = createEventClassFamily(tenant.getId());
        String schema = getResourceAsString(TEST_EVENT_CLASS_FAMILY_SCHEMA);
        client.addEventClassFamilySchema(eventClassFamily.getId(), schema, null);

        EventClassFamilyDto secondEventClassFamily = createEventClassFamily(tenant.getId());
        client.addEventClassFamilySchema(secondEventClassFamily.getId(), schema, null);
    }
    
    /**
     * Assert event class families equals.
     *
     * @param eventClassFamily the event class family
     * @param storedEventClassFamily the stored event class family
     */
    private void assertEventClassFamiliesEquals(EventClassFamilyDto eventClassFamily, EventClassFamilyDto storedEventClassFamily) {
        Assert.assertEquals(eventClassFamily.getId(), storedEventClassFamily.getId());
        Assert.assertEquals(eventClassFamily.getName(), storedEventClassFamily.getName());
        Assert.assertEquals(eventClassFamily.getTenantId(), storedEventClassFamily.getTenantId());
    }

}
