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

package org.kaaproject.kaa.common.channels.communication;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Andrey Panasenko
 *
 */
public class IPParametersTest {

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.communication.IPParameters#hashCode()}.
     */
    @Test
    public void testHashCode() {
        IPParameters p1 = new IPParameters();
        IPParameters p2 = new IPParameters();
        p1.setHostName("host1");
        p2.setHostName("host1");
        p1.setPort(10);
        p2.setPort(10);
        assertEquals(p1.hashCode(), p2.hashCode());

        IPParameters p3 = new IPParameters();
        p3.setPort(10);
        assertNotEquals(p1.hashCode(), p3.hashCode());
        assertNotEquals(p2.hashCode(), p3.hashCode());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.communication.IPParameters#getHostName()}.
     */
    @Test
    public void testGetHostName() {
        IPParameters p1 = new IPParameters();
        p1.setHostName("host1");
        assertEquals("host1", p1.getHostName());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.communication.IPParameters#getPort()}.
     */
    @Test
    public void testGetPort() {
        IPParameters p1 = new IPParameters();
        p1.setPort(100);
        assertEquals(100, p1.getPort());
    }


    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.communication.IPParameters#toString()}.
     */
    @Test
    public void testToString() {
        IPParameters p1 = new IPParameters();
        p1.setHostName("host1");
        p1.setPort(100);
        assertEquals("IPParameters [hostName=host1, port=100]", p1.toString());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.communication.IPParameters#equals(java.lang.Object)}.
     */
    @Test
    public void testEqualsObject() {
        IPParameters p1 = new IPParameters();
        IPParameters p2 = new IPParameters();
        p1.setHostName("host1");
        p2.setHostName("host1");
        p1.setPort(10);
        p2.setPort(10);
        if (!p1.equals(p2)) {
            fail("TestEquals to objects failed");
        }
        if (!p1.equals(p1)) {
            fail("TestEquals to himself to objects failed");
        }
        if (p1.equals(null)) {
            fail("TestEquals to null objects failed");
        }
        if (p1.equals(new Object())) {
            fail("TestEquals to Object() objects failed");
        }
        IPParameters p3 = new IPParameters();
        IPParameters p4 = new IPParameters();
        if (p3.equals(p1)) {
            fail("TestEquals to not equals objects failed");
        }
        if (!p3.equals(p4)) {
            fail("TestEquals to not equals objects failed");
        }
        p3.setHostName("host2");
        if (p1.equals(p3)) {
            fail("TestEquals to not equals objects failed");
        }
        p4.setHostName("host1");
        p4.setPort(20);
        if (p1.equals(p4)) {
            fail("TestEquals to not equals objects failed");
        }
    }

}
