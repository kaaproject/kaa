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

package org.kaaproject.kaa.server.operations.service.event;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class RouteTableTest {

    private static final String SERVER1 = "server1";
    private static final String ECF1 = "ECF1";
    private static final String APP_TOKEN = "APP_TOKEN";
    private static final EndpointObjectHash endpoint = EndpointObjectHash.fromSHA1("endpoint1");

    private RouteTable testTable;

    @Before
    public void before() {
        testTable = new RouteTable();
    }

    @Test
    public void testAddLocal() {
        RouteTableKey localKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1, 1));
        RouteTableAddress localAddress = new RouteTableAddress(endpoint, APP_TOKEN);
        testTable.add(localKey, localAddress);

        Assert.assertNotNull(testTable.getAllLocalRoutes());
        Assert.assertEquals(1, testTable.getAllLocalRoutes().size());
        Assert.assertEquals(localAddress, testTable.getAllLocalRoutes().iterator().next());

        Assert.assertNotNull(testTable.getRoutes(localKey, null));
        Assert.assertEquals(1, testTable.getRoutes(localKey, null).size());
        Assert.assertEquals(localAddress, testTable.getRoutes(localKey, null).iterator().next());

        Assert.assertNotNull(testTable.getRoutes(Collections.singleton(localKey), null));
        Assert.assertEquals(1, testTable.getRoutes(Collections.singleton(localKey), null).size());
        Assert.assertEquals(localAddress, testTable.getRoutes(Collections.singleton(localKey), null).iterator().next());

        Assert.assertNotNull(testTable.getLocalRouteTableKeys(localAddress));
        Assert.assertEquals(1, testTable.getLocalRouteTableKeys(localAddress).size());
        Assert.assertEquals(localKey, testTable.getLocalRouteTableKeys(localAddress).iterator().next());
    }

    @Test
    public void testAddRemote() {
        RouteTableKey remoteKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1, 1));
        RouteTableAddress remoteAddress = new RouteTableAddress(endpoint, APP_TOKEN, SERVER1);
        testTable.add(remoteKey, remoteAddress);

        Assert.assertNotNull(testTable.getRemoteServers());
        Assert.assertEquals(1, testTable.getRemoteServers().size());
        Assert.assertEquals(SERVER1, testTable.getRemoteServers().iterator().next());

        Assert.assertNotNull(testTable.getRoutes(remoteKey, null));
        Assert.assertEquals(1, testTable.getRoutes(remoteKey, null).size());
        Assert.assertEquals(remoteAddress, testTable.getRoutes(remoteKey, null).iterator().next());

        Assert.assertNotNull(testTable.getRoutes(Collections.singleton(remoteKey), null));
        Assert.assertEquals(1, testTable.getRoutes(Collections.singleton(remoteKey), null).size());
        Assert.assertEquals(remoteAddress, testTable.getRoutes(Collections.singleton(remoteKey), null).iterator().next());
    }

    @Test
    public void testRemoveLocal() {
        RouteTableKey localKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1, 1));
        RouteTableAddress localAddress = new RouteTableAddress(endpoint, APP_TOKEN);
        testTable.add(localKey, localAddress);
        testTable.removeLocal(endpoint);

        Assert.assertNotNull(testTable.getAllLocalRoutes());
        Assert.assertEquals(0, testTable.getAllLocalRoutes().size());

        Assert.assertNotNull(testTable.getRoutes(localKey, null));
        Assert.assertEquals(0, testTable.getRoutes(localKey, null).size());

        Assert.assertNotNull(testTable.getLocalRouteTableKeys(localAddress));
        Assert.assertEquals(0, testTable.getLocalRouteTableKeys(localAddress).size());
    }

    @Test
    public void testRemoveRemote() {
        RouteTableKey remoteKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1, 1));
        RouteTableAddress remoteAddress = new RouteTableAddress(endpoint, APP_TOKEN, SERVER1);
        testTable.add(remoteKey, remoteAddress);
        testTable.removeByAddress(remoteAddress);

        Assert.assertNotNull(testTable.getRoutes(remoteKey, null));
        Assert.assertEquals(0, testTable.getRoutes(remoteKey, null).size());

        Assert.assertNotNull(testTable.getRoutes(Collections.singleton(remoteKey), null));
        Assert.assertEquals(0, testTable.getRoutes(Collections.singleton(remoteKey), null).size());

        //still should be 1 until user will trigger clearRemoteServerData
        Assert.assertNotNull(testTable.getRemoteServers());
        Assert.assertEquals(1, testTable.getRemoteServers().size());
    }

    @Test
    public void testClearServerData() {
        RouteTableKey remoteKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1, 1));
        RouteTableAddress remoteAddress = new RouteTableAddress(endpoint, APP_TOKEN, SERVER1);
        testTable.add(remoteKey, remoteAddress);
        testTable.clearRemoteServerData(SERVER1);

        Assert.assertNotNull(testTable.getRoutes(remoteKey, null));
        Assert.assertEquals(0, testTable.getRoutes(remoteKey, null).size());

        Assert.assertNotNull(testTable.getRoutes(Collections.singleton(remoteKey), null));
        Assert.assertEquals(0, testTable.getRoutes(Collections.singleton(remoteKey), null).size());

        //still should be 1 until user will trigger clearRemoteServerData
        Assert.assertNotNull(testTable.getRemoteServers());
        Assert.assertEquals(0, testTable.getRemoteServers().size());
    }

    @Test
    public void testRouteInfoReport(){
        RouteTableKey localKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1, 1));
        RouteTableAddress localAddress = new RouteTableAddress(endpoint, APP_TOKEN);
        testTable.add(localKey, localAddress);

        Assert.assertTrue(testTable.isDeliveryRequired(SERVER1, localAddress));
        testTable.registerRouteInfoReport(Collections.singleton(localAddress), SERVER1);
        Assert.assertFalse(testTable.isDeliveryRequired(SERVER1, localAddress));
    }

}
