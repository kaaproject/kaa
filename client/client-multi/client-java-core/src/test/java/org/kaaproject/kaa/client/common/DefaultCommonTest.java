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

package org.kaaproject.kaa.client.common;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.UUID;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.apache.avro.Schema;
import org.junit.Test;
import org.kaaproject.kaa.client.common.CommonEnum;
import org.kaaproject.kaa.client.common.CommonRecord;
import org.kaaproject.kaa.client.common.CommonValue;
import org.kaaproject.kaa.client.common.DefaultCommonArray;
import org.kaaproject.kaa.client.common.DefaultCommonEnum;
import org.kaaproject.kaa.client.common.DefaultCommonFixed;
import org.kaaproject.kaa.client.common.DefaultCommonRecord;
import org.kaaproject.kaa.client.common.DefaultCommonValue;

import static org.mockito.Mockito.mock;

public class DefaultCommonTest {

    @Test
    public void testCommonValue() {
        CommonValue value = new DefaultCommonValue(null);
        assertTrue(value.isNull());
        assertNull(value.getInteger());
        assertEquals("null", value.toString());

        assertNull(value.getNumber());
        value = new DefaultCommonValue(new Integer(5));
        assertTrue(value.isNumber());
        assertEquals(new Integer(5), value.getNumber());

        assertNull(value.getBytes());
        ByteBuffer expectedBytes = ByteBuffer.wrap(new byte [] {1, 2, 3});
        value = new DefaultCommonValue(expectedBytes);
        assertTrue(value.isBytes());
        assertEquals(expectedBytes, value.getBytes());

        assertNull(value.getBoolean());
        value = new DefaultCommonValue(new Boolean(true));
        assertTrue(value.isBoolean());
        assertTrue(value.getBoolean());

        assertNull(value.getDouble());
        value = new DefaultCommonValue(new Double(5.0));
        assertTrue(value.isDouble());
        assertEquals(new Double(5.0), value.getDouble());

        assertNull(value.getLong());
        value = new DefaultCommonValue(new Long(123));
        assertTrue(value.isLong());
        assertEquals(new Long(123), value.getLong());

        assertNull(value.getFloat());
        value = new DefaultCommonValue(new Float(5.0));
        assertTrue(value.isFloat());
        assertEquals(new Float(5.0), value.getFloat());

        assertNull(value.getEnum());
        Schema schema = mock(Schema.class);
        value = new DefaultCommonValue(new DefaultCommonEnum(schema, "enum"));
        assertTrue(value.isEnum());
        assertEquals("enum", value.getEnum().getSymbol());
        assertEquals("enum", value.toString());

        assertNull(value.getString());
        assertNull(value.getRecord());
        assertNull(value.getArray());
        assertNull(value.getFixed());

        EqualsVerifier.forClass(DefaultCommonValue.class).verify();
    }

    @Test
    public void testCommonRecord() {
        EqualsVerifier.forClass(DefaultCommonRecord.class).verify();
        Schema schema = mock(Schema.class);
        CommonRecord record = new DefaultCommonRecord(schema);
        UUID uuid = new UUID(1234, 5678);
        record.setUuid(uuid);
        assertEquals(uuid, record.getUuid());
    }

    @Test
    public void testCommonArray() {
        EqualsVerifier.forClass(DefaultCommonArray.class).verify();
    }

    @Test
    public void testCommonEnum() {
        EqualsVerifier.forClass(DefaultCommonEnum.class).verify();
        Schema schema = mock(Schema.class);
        CommonEnum e = new DefaultCommonEnum(schema, "enum");
        assertEquals(schema, e.getSchema());
    }

    @Test
    public void testCommonFixed() {
        EqualsVerifier.forClass(DefaultCommonFixed.class).verify();
    }

}
